package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.CatalogProduct
import com.example.data.model.ProductOffer
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
  val fromOfflineCache: Boolean = false,
  val networkError: String? = null,
  val offers: List<ProductOffer> = emptyList(),
  val feedback: String? = null,
)

class ProductDetailViewModel(private val container: AppContainer) : ViewModel() {
  private val _uiState = MutableStateFlow(ProductDetailUiState())
  val uiState: StateFlow<ProductDetailUiState> = _uiState.asStateFlow()

  private var offersJob: Job? = null

  fun load(barcode: String, countryCode: String) {
    val clean = barcode.trim()
    _uiState.value = ProductDetailUiState(barcode = clean, isLoading = true)
    viewModelScope.launch {
      when (val outcome = runCatching { container.productRepository.lookupByBarcode(clean) }.getOrNull()) {
        is ProductLookupOutcome.Found -> {
          _uiState.value = _uiState.value.copy(
            isLoading = false,
            product = outcome.product,
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
    }

    offersJob?.cancel()
    offersJob = viewModelScope.launch {
      container.productRepository.observeOffersForProduct(clean, countryCode)
        .catch { emit(emptyList()) }
        .collect { offers ->
          _uiState.value = _uiState.value.copy(offers = offers)
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
