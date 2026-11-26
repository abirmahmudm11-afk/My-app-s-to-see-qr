package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.CatalogProduct
import com.example.data.model.PriceHistoryEntry
import com.example.data.model.ProductOffer
import com.example.data.repository.ComparedOffer
import com.example.data.repository.ProductApiMetadata
import com.example.data.repository.ProductLookupOutcome
import com.example.di.AppContainer
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

data class ProductDetailUiState(
  val barcode: String = "",
  val isLoading: Boolean = true,
  val product: CatalogProduct? = null,
  val apiMetadata: ProductApiMetadata = ProductApiMetadata(),
  val fromOfflineCache: Boolean = false,
  val networkError: String? = null,
  val offers: List<ProductOffer> = emptyList(),
  val priceHistory: List<PriceHistoryEntry> = emptyList(),
  val isComparingOffers: Boolean = false,
  val hasComparedOffers: Boolean = false,
  val comparedOffers: List<ComparedOffer> = emptyList(),
  val includesSimulatedRetailers: Boolean = false,
  val compareNotice: String? = null,
  val feedback: String? = null,
)

class ProductDetailViewModel(private val container: AppContainer) : ViewModel() {
  private val _uiState = MutableStateFlow(ProductDetailUiState())
  val uiState: StateFlow<ProductDetailUiState> = _uiState.asStateFlow()

  private var offersJob: Job? = null
  private var priceHistoryJob: Job? = null

  fun load(barcode: String, countryCode: String) {
    val clean = barcode.trim()
    _uiState.value = ProductDetailUiState(barcode = clean, isLoading = true)
    viewModelScope.launch {
      var resolvedName = ""
      var resolvedBrand = ""
      when (val outcome = runCatching { container.productRepository.lookupByBarcode(clean) }.getOrNull()) {
        is ProductLookupOutcome.Found -> {
          resolvedName = outcome.product.name
          resolvedBrand = outcome.product.brand
          launch { runCatching { container.productRepository.syncScannedProductToFirestore(outcome.product) } }
          _uiState.value = _uiState.value.copy(
            isLoading = false,
            product = outcome.product,
            apiMetadata = outcome.apiMetadata,
            fromOfflineCache = outcome.fromOfflineCache,
            networkError = null,
          )
        }
        is ProductLookupOutcome.NetworkError -> {
          _uiState.value = _uiState.value.copy(
            isLoading = false,
            product = null,
            networkError = outcome.message,
          )
        }
        else -> {
          _uiState.value = _uiState.value.copy(isLoading = false, product = null, networkError = null)
        }
      }
      compareOffers(clean, countryCode, resolvedName, resolvedBrand)
    }

    offersJob?.cancel()
    offersJob = viewModelScope.launch {
      container.productRepository.observeOffersForProduct(clean, countryCode)
        .catch { emit(emptyList()) }
        .collect { offers ->
          _uiState.value = _uiState.value.copy(offers = offers)
        }
    }

    priceHistoryJob?.cancel()
    priceHistoryJob = viewModelScope.launch {
      container.productRepository.observePriceHistory(clean)
        .catch { emit(emptyList()) }
        .collect { history ->
          _uiState.value = _uiState.value.copy(priceHistory = history)
        }
    }
  }

  fun compareOffers(
    barcode: String,
    countryCode: String,
    productName: String = _uiState.value.product?.name.orEmpty(),
    brand: String = _uiState.value.product?.brand.orEmpty(),
  ) {
    val clean = barcode.trim()
    if (clean.isEmpty() || _uiState.value.isComparingOffers) return
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(
        isComparingOffers = true,
        hasComparedOffers = true,
        compareNotice = null,
      )
      val result = runCatching {
        container.productRepository.compareOffersForBarcode(
          barcode = clean,
          countryCode = countryCode,
          productName = productName,
          brand = brand,
        )
      }.getOrElse { err ->
        com.example.data.repository.OfferComparisonResult(
          offers = emptyList(),
          apiNotice = err.message ?: "Unable to search for offers right now.",
        )
      }
      _uiState.value = _uiState.value.copy(
        isComparingOffers = false,
        hasComparedOffers = true,
        comparedOffers = result.offers,
        includesSimulatedRetailers = result.includesSimulatedRetailers,
        compareNotice = result.apiNotice,
      )
      launch {
        runCatching {
          container.productRepository.recordObservedPriceHistory(clean, countryCode, result.offers)
        }
      }
    }
  }

  fun submitUnverifiedProduct(
    barcode: String,
    name: String,
    brand: String,
    category: String,
    description: String,
  ) {
    viewModelScope.launch {
      runCatching {
        container.productRepository.submitUnverifiedProduct(barcode, name, brand, category, description)
      }.onSuccess { created ->
        _uiState.value = _uiState.value.copy(
          product = created,
          networkError = null,
          feedback = "Community entry added (marked Unverified until reviewed).",
        )
      }.onFailure { err ->
        _uiState.value = _uiState.value.copy(feedback = err.message ?: "Failed to submit product.")
      }
    }
  }

  fun submitUnverifiedOffer(
    barcode: String,
    storeName: String,
    price: Double,
    currencyCode: String,
    countryCode: String,
    offerUrl: String,
  ) {
    viewModelScope.launch {
      runCatching {
        container.productRepository.submitUnverifiedOffer(
          barcode = barcode,
          storeName = storeName,
          price = price,
          currencyCode = currencyCode,
          countryCode = countryCode,
          offerUrl = offerUrl,
        )
      }.onSuccess {
        _uiState.value = _uiState.value.copy(feedback = "Price observation submitted (Unverified).")
      }.onFailure { err ->
        _uiState.value = _uiState.value.copy(feedback = err.message ?: "Failed to submit offer.")
      }
    }
  }

  fun clearFeedback() {
    _uiState.value = _uiState.value.copy(feedback = null)
  }

  companion object {
    fun factory(container: AppContainer): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
      @Suppress("UNCHECKED_CAST")
      override fun <T : ViewModel> create(modelClass: Class<T>): T = ProductDetailViewModel(container) as T
    }
  }
}
