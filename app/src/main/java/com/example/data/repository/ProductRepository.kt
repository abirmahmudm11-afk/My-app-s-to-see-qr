package com.example.data.repository

import com.example.data.local.CachedProductEntity
import com.example.data.local.ProductCacheDao
import com.example.data.model.CatalogProduct
import com.example.data.model.OfferAvailability
import com.example.data.model.ProductOffer
import com.example.data.model.VerificationStatus
import com.example.data.remote.OpenFoodFactsApi
import com.example.data.remote.OpenFoodFactsProductDto
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

interface ProductDataAdapter {
    val providerName: String
    suspend fun fetchByBarcode(barcode: String): CatalogProduct?
    suspend fun search(query: String): List<CatalogProduct>
}

class OpenFoodFactsAdapter(
    private val api: OpenFoodFactsApi
) : ProductDataAdapter {
    override val providerName: String = "Open Food Facts"

    override suspend fun fetchByBarcode(barcode: String): CatalogProduct? {
        return try {
            val response = api.getProductByBarcode(barcode)
            if (response.status == 1 && response.product != null) {
                mapDto(barcode, response.product)
            } else {
                null
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun search(query: String): List<CatalogProduct> {
        return try {
            val response = api.searchProducts(query)
            response.products.mapNotNull { dto ->
                val code = dto.code?.trim().orEmpty()
                if (code.isBlank()) null else mapDto(code, dto)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun mapDto(barcode: String, dto: OpenFoodFactsProductDto): CatalogProduct? {
        val name = dto.productName?.trim()?.takeIf { it.isNotBlank() }
            ?: dto.genericName?.trim()?.takeIf { it.isNotBlank() }
            ?: return null
        val descriptionParts = listOfNotNull(
            dto.quantity?.trim()?.takeIf { it.isNotBlank() }?.let { "Package: $it" },
            dto.genericName?.trim()?.takeIf { it.isNotBlank() && it != name },
            dto.ingredientsText?.trim()?.takeIf { it.isNotBlank() }?.let { "Ingredients: ${it.take(260)}" }
        )
        return CatalogProduct(
            id = barcode,
            barcode = barcode,
            name = name.take(160),
            brand = dto.brands?.trim()?.take(100).orEmpty(),
            category = dto.categories?.split(",")?.firstOrNull()?.trim()?.take(80).orEmpty(),
            description = descriptionParts.joinToString(" • ").take(600),
            imageUrl = (dto.imageFrontUrl ?: dto.imageUrl)?.trim()?.take(500).orEmpty(),
            manufacturer = dto.manufacturingPlaces?.trim()?.take(120).orEmpty(),
            countryOfOrigin = (dto.origins ?: dto.countries)?.trim()?.take(80).orEmpty(),
            dataProvider = "Open Food Facts",
            attributionText = "Data provided by Open Food Facts contributors under ODbL 1.0",
            verificationStatus = VerificationStatus.OPEN_FOOD_FACTS.name,
            submittedBy = "openfoodfacts"
        )
    }
}

sealed interface ProductLookupOutcome {
    data class Found(val product: CatalogProduct, val fromOfflineCache: Boolean = false) : ProductLookupOutcome
    data class NotFound(val barcode: String) : ProductLookupOutcome
}

class ProductRepository(
    private val db: FirebaseFirestore,
    private val productCacheDao: ProductCacheDao,
    private val primaryAdapter: ProductDataAdapter = OpenFoodFactsAdapter(OpenFoodFactsApi.create())
) {
    private val auth = Firebase.auth

    fun observeCachedCatalog(): Flow<List<CatalogProduct>> =
        productCacheDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    suspend fun lookupProduct(barcode: String): ProductLookupOutcome {
        val cleanBarcode = barcode.trim()
        val isAuthenticated = auth.currentUser != null

        // Priority 1 & 2: Verified merchant / licensed provider in Firestore (if signed in)
        val firestoreDoc = if (isAuthenticated) {
            fetchCatalogFromFirestore(cleanBarcode)
        } else {
            null
        }
        if (firestoreDoc != null) {
            val status = VerificationStatus.safeValueOf(firestoreDoc.verificationStatus)
            if (status == VerificationStatus.VERIFIED_MERCHANT ||
                status == VerificationStatus.LICENSED_PROVIDER
            ) {
                productCacheDao.upsert(firestoreDoc.toEntity())
                return ProductLookupOutcome.Found(firestoreDoc)
            }
        }

        // Priority 3: Open Food Facts (live REST API)
        val remoteProduct = primaryAdapter.fetchByBarcode(cleanBarcode)
        if (remoteProduct != null) {
            productCacheDao.upsert(remoteProduct.toEntity())
            return ProductLookupOutcome.Found(remoteProduct)
        }

        // Offline fallback from local Room cache
        val cached = productCacheDao.getByBarcode(cleanBarcode)
        if (cached != null) {
            return ProductLookupOutcome.Found(cached.toDomain(), fromOfflineCache = true)
        }

        // Priority 4: User-submitted unverified record from Firestore
        if (firestoreDoc != null) {
            productCacheDao.upsert(firestoreDoc.toEntity())
            return ProductLookupOutcome.Found(firestoreDoc)
        }

        return ProductLookupOutcome.NotFound(cleanBarcode)
    }

    suspend fun searchCatalog(query: String): List<CatalogProduct> {
        val clean = query.trim()
        if (clean.isBlank()) return emptyList()
        val remote = primaryAdapter.search(clean)
        if (remote.isNotEmpty()) {
            productCacheDao.upsertAll(remote.map { it.toEntity() })
            return remote
        }
        return productCacheDao.searchLocal(clean).map { it.toDomain() }
    }

    suspend fun submitUnverifiedProduct(
        barcode: String,
        name: String,
        brand: String,
        category: String,
        description: String
    ): Result<CatalogProduct> {
        val path = "products_catalog/$barcode"
        return try {
            val uid = auth.currentUser?.uid
                ?: error("Sign in with Google to submit missing product details.")
            val cleanBarcode = barcode.trim()
            require(isSafeFirestoreId(cleanBarcode)) {
                "Only alphanumeric, hyphen, and underscore barcodes (1-128 chars) can be published to the shared catalog."
            }
            require(name.isNotBlank() && name.length <= 160) { "Product name is required (max 160 chars)" }
            val product = CatalogProduct(
                id = cleanBarcode,
                barcode = cleanBarcode,
                name = name.trim().take(160),
                brand = brand.trim().take(100),
                category = category.trim().take(80),
                description = description.trim().take(600),
                imageUrl = "",
                manufacturer = "",
                countryOfOrigin = "",
                dataProvider = "Community Submission",
                attributionText = "User-submitted product entry (Unverified)",
                verificationStatus = VerificationStatus.USER_SUBMITTED_UNVERIFIED.name,
                submittedBy = uid
            )
            db.collection("products_catalog").document(barcode.trim())
                .set(product.toCreateMap())
                .await()
            productCacheDao.upsert(product.toEntity())
            Result.success(product)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    fun observeOffersForProduct(barcode: String, countryCode: String): Flow<List<ProductOffer>> = flow {
        if (auth.currentUser == null || !isSafeFirestoreId(barcode)) {
            emit(emptyList())
            return@flow
        }
        val path = "products_catalog/$barcode/offers"
        db.collection("products_catalog").document(barcode).collection("offers")
            .whereEqualTo("countryCode", countryCode.uppercase())
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(
                    ProductOffer::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                ).sortedBy { it.price }
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                if (auth.currentUser != null) {
                    handleFirestoreError(firestoreError ?: error, OperationType.LIST, path)
                }
                emit(emptyList())
            }
            .collect { emit(it) }
    }

    suspend fun submitUnverifiedOffer(
        barcode: String,
        storeName: String,
        price: Double,
        currencyCode: String,
        countryCode: String,
        offerUrl: String
    ): Result<Unit> {
        val path = "products_catalog/$barcode/offers"
        return try {
            val uid = auth.currentUser?.uid
                ?: error("Sign in with Google to submit a store price observation.")
            require(isSafeFirestoreId(barcode)) {
                "Offers can only be submitted for valid catalog barcode IDs."
            }
            require(storeName.isNotBlank() && storeName.length <= 100) { "Store name required" }
            require(price > 0.0 && price <= 1_000_000.0) { "Price must be greater than zero" }
            val docRef = db.collection("products_catalog").document(barcode)
                .collection("offers").document()
            val offer = ProductOffer(
                id = docRef.id,
                barcode = barcode,
                storeName = storeName.trim().take(100),
                price = price,
                currencyCode = currencyCode.uppercase().take(3),
                countryCode = countryCode.uppercase().take(2),
                availabilityStatus = OfferAvailability.UNVERIFIED.name,
                verified = false,
                offerUrl = offerUrl.trim().take(500),
                submittedBy = uid
            )
            docRef.set(offer.toCreateMap()).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    private fun isSafeFirestoreId(id: String): Boolean {
        return id.length in 1..128 && SAFE_DOC_ID_REGEX.matches(id)
    }

    private suspend fun fetchCatalogFromFirestore(barcode: String): CatalogProduct? {
        if (!isSafeFirestoreId(barcode)) return null
        return try {
            val snap = db.collection("products_catalog").document(barcode).get().await()
            if (snap.exists()) {
                snap.toObject(
                    CatalogProduct::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
            } else {
                null
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, "products_catalog/$barcode")
            null
        }
    }

    private companion object {
        val SAFE_DOC_ID_REGEX = Regex("^[a-zA-Z0-9_\\-]+$")
    }

    private fun CatalogProduct.toEntity() = CachedProductEntity(
        barcode = barcode,
        name = name,
        brand = brand,
        category = category,
        description = description,
        imageUrl = imageUrl,
        manufacturer = manufacturer,
        countryOfOrigin = countryOfOrigin,
        dataProvider = dataProvider,
        attributionText = attributionText,
        verificationStatus = verificationStatus,
        lastUpdatedMillis = System.currentTimeMillis()
    )

    private fun CachedProductEntity.toDomain() = CatalogProduct(
        id = barcode,
        barcode = barcode,
        name = name,
        brand = brand,
        category = category,
        description = description,
        imageUrl = imageUrl,
        manufacturer = manufacturer,
        countryOfOrigin = countryOfOrigin,
        dataProvider = dataProvider,
        attributionText = attributionText,
        verificationStatus = verificationStatus
    )
}
