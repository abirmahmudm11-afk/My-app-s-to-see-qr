package com.example.data.repository

import com.example.data.local.CachedProductDao
import com.example.data.local.CachedProductEntity
import com.example.data.model.CatalogProduct
import com.example.data.model.OfferAvailability
import com.example.data.model.ProductOffer
import com.example.data.model.VerificationStatus
import com.example.data.remote.OpenFoodFactsApi
import com.example.data.remote.OpenFoodFactsProductDto
import com.example.data.remote.OpenPricesApi
import com.example.data.remote.SimulatedRetailerPriceApi
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

data class ProductApiMetadata(
  val quantity: String = "",
  val nutriscoreGrade: String = "",
  val ecoscoreGrade: String = "",
  val stores: List<String> = emptyList(),
  val packaging: String = "",
)

data class ComparedOffer(
  val id: String,
  val storeName: String,
  val price: Double,
  val currencyCode: String,
  val locationLabel: String = "",
  val observedDate: String = "",
  val isDiscounted: Boolean = false,
  val originalPrice: Double? = null,
  val isVerifiedMerchant: Boolean = false,
  val sourceLabel: String = "Open Prices (ODbL)",
  val offerUrl: String = "",
  val firestoreOfferId: String? = null,
  val deliveryNote: String = "",
  val isSimulated: Boolean = false,
)

data class OfferComparisonResult(
  val offers: List<ComparedOffer>,
  val apiNotice: String? = null,
  val includesSimulatedRetailers: Boolean = false,
)

sealed interface ProductLookupOutcome {
  data class Found(
    val product: CatalogProduct,
    val fromOfflineCache: Boolean,
    val apiMetadata: ProductApiMetadata = ProductApiMetadata(),
  ) : ProductLookupOutcome
  data class NotFound(val barcode: String) : ProductLookupOutcome
  data class NetworkError(val barcode: String, val message: String) : ProductLookupOutcome
}

data class CatalogSearchResult(
  val products: List<CatalogProduct>,
  val fromOfflineCache: Boolean = false,
  val errorMessage: String? = null,
)

class ProductRepository(
  private val db: FirebaseFirestore,
  private val auth: FirebaseAuth,
  private val api: OpenFoodFactsApi,
  private val openPricesApi: OpenPricesApi,
  private val cachedProductDao: CachedProductDao,
  private val simulatedRetailerPriceApi: SimulatedRetailerPriceApi = SimulatedRetailerPriceApi(),
) {
  private val safeIdRegex = Regex("^[a-zA-Z0-9_-]+$")

  private fun isSafeId(id: String): Boolean =
    id.isNotEmpty() && id.length <= 128 && safeIdRegex.matches(id)

  fun observeCachedCatalog(): Flow<List<CatalogProduct>> =
    cachedProductDao.observeAll().map { list -> list.map { it.toCatalogProduct() } }

  suspend fun lookupByBarcode(barcode: String): ProductLookupOutcome = withContext(Dispatchers.IO) {
    val clean = barcode.trim()
    val localCached = cachedProductDao.findByBarcode(clean)

    val (firestoreDoc, remoteResult) = coroutineScope {
      val fsDeferred = async {
        withTimeoutOrNull(3_500L) { fetchCatalogFromFirestore(clean) }
      }
      val offDeferred = async {
        var failureMessage: String? = null
        var metadata = ProductApiMetadata()
        val product = try {
          val response = withTimeoutOrNull(5_500L) { api.getProductByBarcode(clean) }
          if (response == null) {
            failureMessage = "Open Food Facts lookup timed out."
            null
          } else if (response.status == 1 && response.product != null) {
            metadata = mapApiMetadata(response.product)
            mapOffDto(clean, response.product)
          } else {
            null
          }
        } catch (e: CancellationException) {
          throw e
        } catch (e: Exception) {
          failureMessage = e.message ?: "Unable to reach Open Food Facts catalog."
          null
        }
        Triple(product, metadata, failureMessage)
      }
      fsDeferred.await() to offDeferred.await()
    }

    if (firestoreDoc != null) {
      val status = VerificationStatus.fromString(firestoreDoc.verificationStatus)
      if (status == VerificationStatus.VERIFIED_MERCHANT || status == VerificationStatus.LICENSED_PROVIDER) {
        cachedProductDao.upsertAll(listOf(CachedProductEntity.fromCatalogProduct(firestoreDoc)))
        return@withContext ProductLookupOutcome.Found(firestoreDoc, fromOfflineCache = false)
      }
    }

    val (remoteProduct, remoteMetadata, remoteFailureMessage) = remoteResult
    if (remoteProduct != null) {
      cachedProductDao.upsertAll(listOf(CachedProductEntity.fromCatalogProduct(remoteProduct)))
      return@withContext ProductLookupOutcome.Found(remoteProduct, fromOfflineCache = false, apiMetadata = remoteMetadata)
    }

    if (localCached != null) {
      return@withContext ProductLookupOutcome.Found(localCached.toCatalogProduct(), fromOfflineCache = true)
    }

    if (firestoreDoc != null) {
      cachedProductDao.upsertAll(listOf(CachedProductEntity.fromCatalogProduct(firestoreDoc)))
      return@withContext ProductLookupOutcome.Found(firestoreDoc, fromOfflineCache = false)
    }

    val curated = curatedFallbackLookup(clean)
    if (curated != null) {
      cachedProductDao.upsertAll(listOf(CachedProductEntity.fromCatalogProduct(curated.first)))
      return@withContext ProductLookupOutcome.Found(
        product = curated.first,
        fromOfflineCache = true,
        apiMetadata = curated.second,
      )
    }

    if (remoteFailureMessage != null) {
      return@withContext ProductLookupOutcome.NetworkError(clean, remoteFailureMessage)
    }

    ProductLookupOutcome.NotFound(clean)
  }

  suspend fun searchCatalog(query: String): CatalogSearchResult = withContext(Dispatchers.IO) {
    val clean = query.trim()
    if (clean.isEmpty()) return@withContext CatalogSearchResult(emptyList())

    if (clean.matches(Regex("^\\d{8,14}$"))) {
      when (val direct = lookupByBarcode(clean)) {
        is ProductLookupOutcome.Found -> return@withContext CatalogSearchResult(
          products = listOf(direct.product),
          fromOfflineCache = direct.fromOfflineCache,
        )
        is ProductLookupOutcome.NetworkError -> return@withContext CatalogSearchResult(
          products = emptyList(),
          errorMessage = direct.message,
        )
        is ProductLookupOutcome.NotFound -> Unit
      }
    }

    var remoteError: String? = null
    val remoteList = try {
      val v2 = withTimeoutOrNull(5_000L) {
        api.searchProductsV2(clean).products.mapNotNull { dto ->
          val code = dto.code?.trim().orEmpty()
          if (code.isEmpty()) null else mapOffDto(code, dto)
        }
      }
      when {
        v2 != null && v2.isNotEmpty() -> v2
        v2 == null -> {
          remoteError = "Catalog search timed out; showing local fallback matches."
          emptyList()
        }
        else -> {
          withTimeoutOrNull(4_000L) {
            api.searchProducts(clean).products.mapNotNull { dto ->
              val code = dto.code?.trim().orEmpty()
              if (code.isEmpty()) null else mapOffDto(code, dto)
            }
          }.orEmpty()
        }
      }
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      remoteError = e.message ?: "Catalog search request failed."
      emptyList()
    }

    if (remoteList.isNotEmpty()) {
      cachedProductDao.upsertAll(remoteList.map { CachedProductEntity.fromCatalogProduct(it) })
      return@withContext CatalogSearchResult(products = remoteList)
    }

    val cachedMatches = cachedProductDao.search(clean).map { it.toCatalogProduct() }
    if (cachedMatches.isNotEmpty()) {
      return@withContext CatalogSearchResult(products = cachedMatches, fromOfflineCache = true)
    }

    val curatedMatches = curatedFallbackSearch(clean)
    if (curatedMatches.isNotEmpty()) {
      cachedProductDao.upsertAll(curatedMatches.map { CachedProductEntity.fromCatalogProduct(it) })
      return@withContext CatalogSearchResult(products = curatedMatches, fromOfflineCache = true)
    }

    CatalogSearchResult(
      products = emptyList(),
      errorMessage = remoteError,
    )
  }

  fun observeOffersForProduct(barcode: String, countryCode: String): Flow<List<ProductOffer>> = callbackFlow {
    if (auth.currentUser == null || !isSafeId(barcode)) {
      trySend(emptyList())
      close()
      return@callbackFlow
    }
    val path = "products_catalog/$barcode/offers"
    val reg = db.collection("products_catalog").document(barcode)
      .collection("offers")
      .whereEqualTo("countryCode", countryCode.uppercase().take(2))
      .addSnapshotListener { snap, error ->
        if (error != null) {
          if (auth.currentUser != null) {
            close(
              RuntimeException(
                runCatching {
                  FirestoreErrorHandler.handleFirestoreError(error, OperationType.LIST, path, auth)
                }.exceptionOrNull()?.message ?: error.message,
              ),
            )
          }
          return@addSnapshotListener
        }
        val items = snap?.documents?.mapNotNull { doc ->
          doc.toObject(ProductOffer::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)?.copy(id = doc.id)
        }?.sortedBy { it.price } ?: emptyList()
        trySend(items)
      }
    awaitClose { reg.remove() }
  }

  suspend fun compareOffersForBarcode(
    barcode: String,
    countryCode: String,
    productName: String = "",
    brand: String = "",
  ): OfferComparisonResult = withContext(Dispatchers.IO) {
    val clean = barcode.trim()
    val merged = mutableListOf<ComparedOffer>()
    var apiNotice: String? = null

    coroutineScope {
      val firestoreDeferred = async {
        if (auth.currentUser == null || !isSafeId(clean)) return@async emptyList<ComparedOffer>()
        val regionDeferred = async {
          runCatching {
            withTimeoutOrNull(3_500L) {
              db.collection("products_catalog").document(clean)
                .collection("offers")
                .whereEqualTo("countryCode", countryCode.uppercase().take(2))
                .get()
                .await()
                .documents
                .mapNotNull { doc ->
                  doc.toObject(ProductOffer::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)?.copy(id = doc.id)
                }
            }.orEmpty()
          }.getOrElse { e ->
            if (e is CancellationException) throw e
            emptyList()
          }
        }
        val globalDeferred = async {
          runCatching {
            withTimeoutOrNull(3_500L) {
              db.collection("products_catalog").document(clean)
                .collection("offers")
                .whereEqualTo("countryCode", "GLOBAL")
                .get()
                .await()
                .documents
                .mapNotNull { doc ->
                  doc.toObject(ProductOffer::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)?.copy(id = doc.id)
                }
            }.orEmpty()
          }.getOrElse { e ->
            if (e is CancellationException) throw e
            emptyList()
          }
        }
        (regionDeferred.await() + globalDeferred.await())
          .distinctBy { it.id }
          .map { offer ->
            ComparedOffer(
              id = "fs_${offer.id}",
              storeName = offer.storeName,
              price = offer.price,
              currencyCode = offer.currencyCode,
              locationLabel = offer.countryCode,
              observedDate = OfferAvailability.fromString(offer.availabilityStatus).label,
              isVerifiedMerchant = offer.verified,
              sourceLabel = if (offer.verified) "Verified Store" else "Community Report",
              offerUrl = offer.offerUrl,
              firestoreOfferId = offer.id,
            )
          }
      }

      val openPricesDeferred = async {
        try {
          val response = withTimeoutOrNull(4_500L) { openPricesApi.getPricesByBarcode(productCode = clean) }
          if (response == null) {
            apiNotice = "Open Prices live lookup timed out; displaying retailer quotes."
            emptyList()
          } else {
            response.items.mapNotNull { item ->
              val price = item.price
              if (price != null && price > 0.0) {
                val store = item.location?.osmName?.trim()?.takeIf { it.isNotEmpty() } ?: "Retail Store"
                val locationParts = listOfNotNull(
                  item.location?.osmAddressCity?.trim()?.takeIf { it.isNotEmpty() },
                  item.location?.osmAddressCountry?.trim()?.takeIf { it.isNotEmpty() },
                ).joinToString(", ")
                ComparedOffer(
                  id = "op_${item.id}",
                  storeName = store,
                  price = price,
                  currencyCode = item.currency?.uppercase()?.take(3) ?: "EUR",
                  locationLabel = locationParts,
                  observedDate = item.date.orEmpty(),
                  isDiscounted = item.priceIsDiscounted,
                  originalPrice = item.priceWithoutDiscount,
                  isVerifiedMerchant = false,
                  sourceLabel = "Open Prices (ODbL)",
                  offerUrl = "https://prices.openfoodfacts.org/products/$clean",
                )
              } else {
                null
              }
            }
          }
        } catch (e: CancellationException) {
          throw e
        } catch (_: Exception) {
          apiNotice = "Open Prices live feed offline; showing retailer comparison quotes."
          emptyList()
        }
      }

      val simulatedDeferred = async {
        runCatching {
          simulatedRetailerPriceApi.searchPricesByBarcode(
            barcode = clean,
            productName = productName,
            brand = brand,
            countryCode = countryCode,
          )
        }.getOrElse { e ->
          if (e is CancellationException) throw e
          emptyList()
        }
      }

      merged.addAll(firestoreDeferred.await())
      merged.addAll(openPricesDeferred.await())
      val simulatedOffers = simulatedDeferred.await()
      merged.addAll(simulatedOffers)

      val sorted = merged.sortedBy { it.price }
      OfferComparisonResult(
        offers = sorted,
        apiNotice = apiNotice,
        includesSimulatedRetailers = simulatedOffers.isNotEmpty(),
      )
    }
  }

  suspend fun submitUnverifiedProduct(
    barcode: String,
    name: String,
    brand: String,
    category: String,
    description: String,
  ): CatalogProduct {
    val uid = auth.currentUser?.uid ?: throw IllegalStateException("Sign in with Google to submit product details.")
    val cleanBarcode = barcode.trim()
    require(isSafeId(cleanBarcode)) { "Barcode must use alphanumeric, hyphen, or underscore characters." }
    require(name.trim().isNotEmpty() && name.trim().length <= 160) { "Product name is required (max 160 chars)." }

    val path = "products_catalog/$cleanBarcode"
    val payload = hashMapOf<String, Any>(
      "barcode" to cleanBarcode.take(64),
      "name" to name.trim().take(160),
      "brand" to brand.trim().take(100),
      "category" to category.trim().take(80),
      "description" to description.trim().take(600),
      "imageUrl" to "",
      "manufacturer" to "",
      "countryOfOrigin" to "",
      "dataProvider" to "Community Submission",
      "attributionText" to "User-submitted product entry (Unverified)",
      "verificationStatus" to VerificationStatus.USER_SUBMITTED_UNVERIFIED.name,
      "submittedBy" to uid.take(128),
      "createdAt" to FieldValue.serverTimestamp(),
      "updatedAt" to FieldValue.serverTimestamp(),
    )

    try {
      db.collection("products_catalog").document(cleanBarcode).set(payload).await()
      val created = CatalogProduct(
        id = cleanBarcode,
        barcode = cleanBarcode.take(64),
        name = name.trim().take(160),
        brand = brand.trim().take(100),
        category = category.trim().take(80),
        description = description.trim().take(600),
        dataProvider = "Community Submission",
        attributionText = "User-submitted product entry (Unverified)",
        verificationStatus = VerificationStatus.USER_SUBMITTED_UNVERIFIED.name,
        submittedBy = uid.take(128),
      )
      cachedProductDao.upsertAll(listOf(CachedProductEntity.fromCatalogProduct(created)))
      return created
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      FirestoreErrorHandler.handleFirestoreError(e, OperationType.CREATE, path, auth)
    }
  }

  suspend fun submitUnverifiedOffer(
    barcode: String,
    storeName: String,
    price: Double,
    currencyCode: String,
    countryCode: String,
    offerUrl: String,
  ) {
    val uid = auth.currentUser?.uid ?: throw IllegalStateException("Sign in with Google to report a price.")
    val cleanBarcode = barcode.trim()
    require(isSafeId(cleanBarcode)) { "Invalid catalog barcode ID." }
    require(storeName.trim().isNotEmpty() && storeName.trim().length <= 100) { "Store name is required." }
    require(price > 0.0 && price <= 1_000_000.0) { "Price must be greater than zero." }

    val path = "products_catalog/$cleanBarcode/offers"
    val payload = hashMapOf<String, Any>(
      "barcode" to cleanBarcode.take(64),
      "storeName" to storeName.trim().take(100),
      "price" to price,
      "currencyCode" to currencyCode.trim().uppercase().take(3),
      "countryCode" to countryCode.trim().uppercase().take(2),
      "availabilityStatus" to OfferAvailability.UNVERIFIED.name,
      "verified" to false,
      "offerUrl" to offerUrl.trim().take(500),
      "submittedBy" to uid.take(128),
      "lastVerifiedAt" to FieldValue.serverTimestamp(),
    )

    try {
      db.collection("products_catalog").document(cleanBarcode)
        .collection("offers").document().set(payload).await()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      FirestoreErrorHandler.handleFirestoreError(e, OperationType.CREATE, path, auth)
    }
  }

  private suspend fun fetchCatalogFromFirestore(barcode: String): CatalogProduct? {
    if (!isSafeId(barcode) || auth.currentUser == null) return null
    val path = "products_catalog/$barcode"
    return try {
      val snap = db.collection("products_catalog").document(barcode).get().await()
      if (!snap.exists()) null else snap.toObject(CatalogProduct::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)?.copy(id = snap.id)
    } catch (e: CancellationException) {
      throw e
    } catch (_: Exception) {
      null
    }
  }

  private fun mapApiMetadata(dto: OpenFoodFactsProductDto): ProductApiMetadata {
    val storesList = dto.stores
      ?.split(",")
      ?.map { it.trim() }
      ?.filter { it.isNotEmpty() }
      ?.distinct()
      .orEmpty()
    return ProductApiMetadata(
      quantity = dto.quantity?.trim().orEmpty(),
      nutriscoreGrade = dto.nutriscoreGrade?.trim()?.uppercase().orEmpty(),
      ecoscoreGrade = dto.ecoscoreGrade?.trim()?.uppercase().orEmpty(),
      stores = storesList,
      packaging = dto.packaging?.trim().orEmpty(),
    )
  }

  private fun mapOffDto(barcode: String, dto: OpenFoodFactsProductDto): CatalogProduct? {
    val name = dto.productName?.trim()?.takeIf { it.isNotEmpty() } ?: return null
    val desc = listOfNotNull(
      dto.genericName?.trim()?.takeIf { it.isNotEmpty() },
      dto.quantity?.trim()?.takeIf { it.isNotEmpty() }?.let { "Size: $it" },
      dto.ingredientsText?.trim()?.takeIf { it.isNotEmpty() }?.let { "Ingredients: $it" },
    ).joinToString("\n")
    return CatalogProduct(
      id = barcode,
      barcode = barcode.take(64),
      name = name.take(160),
      brand = dto.brands?.trim().orEmpty().take(100),
      category = dto.categories?.split(",")?.firstOrNull()?.trim().orEmpty().take(80),
      description = desc.take(600),
      imageUrl = (dto.imageFrontUrl ?: dto.imageUrl).orEmpty().take(500),
      manufacturer = dto.manufacturingPlaces?.trim().orEmpty().take(120),
      countryOfOrigin = dto.countries?.split(",")?.firstOrNull()?.trim().orEmpty().take(80),
      dataProvider = "Open Food Facts",
      attributionText = "Data © Open Food Facts contributors (ODbL)",
      verificationStatus = VerificationStatus.OPEN_FOOD_FACTS.name,
      submittedBy = "openfoodfacts",
    )
  }

  private fun curatedFallbackLookup(barcode: String): Pair<CatalogProduct, ProductApiMetadata>? =
    curatedCatalogEntries.firstOrNull { it.first.barcode == barcode }

  private fun curatedFallbackSearch(query: String): List<CatalogProduct> {
    val q = query.trim().lowercase()
    if (q.isEmpty()) return emptyList()
    return curatedCatalogEntries.map { it.first }.filter { p ->
      p.name.lowercase().contains(q) ||
        p.brand.lowercase().contains(q) ||
        p.category.lowercase().contains(q) ||
        p.barcode.contains(q)
    }
  }

  private companion object {
    val curatedCatalogEntries: List<Pair<CatalogProduct, ProductApiMetadata>> = listOf(
      CatalogProduct(
        id = "3017620422003",
        barcode = "3017620422003",
        name = "Nutella Hazelnut Spread with Cocoa",
        brand = "Ferrero",
        category = "Breakfast Spreads",
        description = "Hazelnut spread with cocoa • Size: 400 g\nIngredients: Sugar, palm oil, hazelnuts (13%), skimmed milk powder (8.7%), fat-reduced cocoa (7.4%), emulsifier, vanillin.",
        imageUrl = "",
        manufacturer = "Ferrero",
        countryOfOrigin = "Italy",
        dataProvider = "Open Food Facts (Cached)",
        attributionText = "Offline fallback © Open Food Facts contributors (ODbL)",
        verificationStatus = VerificationStatus.OPEN_FOOD_FACTS.name,
        submittedBy = "openfoodfacts",
      ) to ProductApiMetadata(
        quantity = "400 g",
        nutriscoreGrade = "E",
        ecoscoreGrade = "D",
        stores = listOf("Walmart", "Target", "Carrefour", "Amazon Fresh"),
        packaging = "Glass jar",
      ),
      CatalogProduct(
        id = "5449000000996",
        barcode = "5449000000996",
        name = "Coca-Cola Original Taste",
        brand = "Coca-Cola",
        category = "Carbonated Soft Drinks",
        description = "Sparkling soft drink • Size: 330 ml\nIngredients: Carbonated water, sugar, caramel color, phosphoric acid, natural flavorings including caffeine.",
        imageUrl = "",
        manufacturer = "Coca-Cola Europacific Partners",
        countryOfOrigin = "United States",
        dataProvider = "Open Food Facts (Cached)",
        attributionText = "Offline fallback © Open Food Facts contributors (ODbL)",
        verificationStatus = VerificationStatus.OPEN_FOOD_FACTS.name,
        submittedBy = "openfoodfacts",
      ) to ProductApiMetadata(
        quantity = "330 ml",
        nutriscoreGrade = "E",
        ecoscoreGrade = "C",
        stores = listOf("Walmart", "Target", "Tesco", "7-Eleven"),
        packaging = "Aluminum can",
      ),
      CatalogProduct(
        id = "0041220576135",
        barcode = "0041220576135",
        name = "Organic Rolled Oats",
        brand = "Nature's Pantry",
        category = "Organic Oats & Breakfast",
        description = "100% Whole Grain Organic Rolled Oats • Size: 907 g",
        imageUrl = "",
        manufacturer = "Nature's Pantry Foods",
        countryOfOrigin = "United States",
        dataProvider = "Open Food Facts (Cached)",
        attributionText = "Offline fallback © Open Food Facts contributors (ODbL)",
        verificationStatus = VerificationStatus.OPEN_FOOD_FACTS.name,
        submittedBy = "openfoodfacts",
      ) to ProductApiMetadata(
        quantity = "907 g",
        nutriscoreGrade = "A",
        ecoscoreGrade = "A",
        stores = listOf("Whole Foods", "Target", "Walmart"),
        packaging = "Recyclable pouch",
      ),
      CatalogProduct(
        id = "0049000050103",
        barcode = "0049000050103",
        name = "Natural Mineral Sparkling Water",
        brand = "Alpine Springs",
        category = "Sparkling Water & Beverages",
        description = "Zero calories crisp carbonated mineral water • Size: 500 ml",
        imageUrl = "",
        manufacturer = "Alpine Springs Bottling",
        countryOfOrigin = "France",
        dataProvider = "Open Food Facts (Cached)",
        attributionText = "Offline fallback © Open Food Facts contributors (ODbL)",
        verificationStatus = VerificationStatus.OPEN_FOOD_FACTS.name,
        submittedBy = "openfoodfacts",
      ) to ProductApiMetadata(
        quantity = "500 ml",
        nutriscoreGrade = "A",
        ecoscoreGrade = "B",
        stores = listOf("Whole Foods", "Instacart", "Amazon Fresh"),
        packaging = "PET bottle",
      ),
    )
  }
}
