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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

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
)

data class OfferComparisonResult(
  val offers: List<ComparedOffer>,
  val apiNotice: String? = null,
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
) {
  private val safeIdRegex = Regex("^[a-zA-Z0-9_-]+$")

  private fun isSafeId(id: String): Boolean =
    id.isNotEmpty() && id.length <= 128 && safeIdRegex.matches(id)

  fun observeCachedCatalog(): Flow<List<CatalogProduct>> =
    cachedProductDao.observeAll().map { list -> list.map { it.toCatalogProduct() } }

  suspend fun lookupByBarcode(barcode: String): ProductLookupOutcome {
    val clean = barcode.trim()
    val firestoreDoc = fetchCatalogFromFirestore(clean)

    if (firestoreDoc != null) {
      val status = VerificationStatus.fromString(firestoreDoc.verificationStatus)
      if (status == VerificationStatus.VERIFIED_MERCHANT || status == VerificationStatus.LICENSED_PROVIDER) {
        cachedProductDao.upsertAll(listOf(CachedProductEntity.fromCatalogProduct(firestoreDoc)))
        return ProductLookupOutcome.Found(firestoreDoc, fromOfflineCache = false)
      }
    }

    var remoteFailureMessage: String? = null
    var remoteMetadata = ProductApiMetadata()
    val remoteProduct = try {
      val response = api.getProductByBarcode(clean)
      if (response.status == 1 && response.product != null) {
        remoteMetadata = mapApiMetadata(response.product)
        mapOffDto(clean, response.product)
      } else null
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      remoteFailureMessage = e.message ?: "Unable to reach Open Food Facts catalog."
      null
    }

    if (remoteProduct != null) {
      cachedProductDao.upsertAll(listOf(CachedProductEntity.fromCatalogProduct(remoteProduct)))
      return ProductLookupOutcome.Found(remoteProduct, fromOfflineCache = false, apiMetadata = remoteMetadata)
    }

    val cached = cachedProductDao.findByBarcode(clean)
    if (cached != null) {
      return ProductLookupOutcome.Found(cached.toCatalogProduct(), fromOfflineCache = true)
    }

    if (firestoreDoc != null) {
      cachedProductDao.upsertAll(listOf(CachedProductEntity.fromCatalogProduct(firestoreDoc)))
      return ProductLookupOutcome.Found(firestoreDoc, fromOfflineCache = false)
    }

    if (remoteFailureMessage != null) {
      return ProductLookupOutcome.NetworkError(clean, remoteFailureMessage)
    }

    return ProductLookupOutcome.NotFound(clean)
  }

  suspend fun searchCatalog(query: String): CatalogSearchResult {
    val clean = query.trim()
    if (clean.isEmpty()) return CatalogSearchResult(emptyList())

    if (clean.matches(Regex("^\\d{8,14}$"))) {
      when (val direct = lookupByBarcode(clean)) {
        is ProductLookupOutcome.Found -> return CatalogSearchResult(
          products = listOf(direct.product),
          fromOfflineCache = direct.fromOfflineCache,
        )
        is ProductLookupOutcome.NetworkError -> return CatalogSearchResult(
          products = emptyList(),
          errorMessage = direct.message,
        )
        is ProductLookupOutcome.NotFound -> Unit
      }
    }

    var remoteError: String? = null
    val remoteList = try {
      val v2 = api.searchProductsV2(clean).products.mapNotNull { dto ->
        val code = dto.code?.trim().orEmpty()
        if (code.isEmpty()) null else mapOffDto(code, dto)
      }
      if (v2.isNotEmpty()) {
        v2
      } else {
        api.searchProducts(clean).products.mapNotNull { dto ->
          val code = dto.code?.trim().orEmpty()
          if (code.isEmpty()) null else mapOffDto(code, dto)
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
      return CatalogSearchResult(products = remoteList)
    }

    val cachedMatches = cachedProductDao.search(clean).map { it.toCatalogProduct() }
    if (cachedMatches.isNotEmpty()) {
      return CatalogSearchResult(products = cachedMatches, fromOfflineCache = true)
    }

    return CatalogSearchResult(
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

  suspend fun compareOffersForBarcode(barcode: String, countryCode: String): OfferComparisonResult {
    val clean = barcode.trim()
    val merged = mutableListOf<ComparedOffer>()
    var apiNotice: String? = null

    if (auth.currentUser != null && isSafeId(clean)) {
      val regionOffers = runCatching {
        db.collection("products_catalog").document(clean)
          .collection("offers")
          .whereEqualTo("countryCode", countryCode.uppercase().take(2))
          .get()
          .await()
          .documents
          .mapNotNull { doc ->
            doc.toObject(ProductOffer::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)?.copy(id = doc.id)
          }
      }.getOrElse { e ->
        if (e is CancellationException) throw e
        emptyList()
      }

      val globalOffers = runCatching {
        db.collection("products_catalog").document(clean)
          .collection("offers")
          .whereEqualTo("countryCode", "GLOBAL")
          .get()
          .await()
          .documents
          .mapNotNull { doc ->
            doc.toObject(ProductOffer::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)?.copy(id = doc.id)
          }
      }.getOrElse { e ->
        if (e is CancellationException) throw e
        emptyList()
      }

      (regionOffers + globalOffers)
        .distinctBy { it.id }
        .forEach { offer ->
          merged.add(
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
            ),
          )
        }
    }

    try {
      val response = openPricesApi.getPricesByBarcode(productCode = clean)
      response.items.forEach { item ->
        val price = item.price
        if (price != null && price > 0.0) {
          val store = item.location?.osmName?.trim()?.takeIf { it.isNotEmpty() }
            ?: "Retail Store"
          val locationParts = listOfNotNull(
            item.location?.osmAddressCity?.trim()?.takeIf { it.isNotEmpty() },
            item.location?.osmAddressCountry?.trim()?.takeIf { it.isNotEmpty() },
          ).joinToString(", ")
          merged.add(
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
            ),
          )
        }
      }
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      apiNotice = "Open Prices live lookup unavailable: ${e.message ?: "network error"}."
    }

    val sorted = merged.sortedBy { it.price }
    return OfferComparisonResult(offers = sorted, apiNotice = apiNotice)
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
}
