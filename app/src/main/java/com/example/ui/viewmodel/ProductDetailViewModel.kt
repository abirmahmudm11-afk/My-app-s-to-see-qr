package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Campaign
import com.example.data.model.CatalogProduct
import com.example.data.model.ProductOffer
import com.example.data.model.RewardTransaction
import com.example.data.repository.ProductLookupOutcome
import com.example.data.repository.ProductRepository
import com.example.data.repository.ReceiptAndCampaignRepository
import com.example.data.repository.UserWalletRepository
import com.example.domain.CampaignEligibilityEngine
import com.example.domain.EligibilityResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface ProductDetailUiState {
    data object Loading : ProductDetailUiState
    data class Loaded(
        val barcode: String,
        val product: CatalogProduct?,
        val fromOfflineCache: Boolean,
        val offers: List<ProductOffer>,
        val eligibleCampaigns: List<EligibilityResult>
    ) : ProductDetailUiState
    data class Error(val message: String) : ProductDetailUiState
}

class ProductDetailViewModel(
    private val barcode: String,
    private val countryCode: String,
    private val currentUserId: String?,
    private val productRepository: ProductRepository,
    receiptAndCampaignRepository: ReceiptAndCampaignRepository,
    userWalletRepository: UserWalletRepository
) : ViewModel() {

    private val _productState = MutableStateFlow<Pair<CatalogProduct?, Boolean>?>(null)
    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    private val offersFlow = productRepository.observeOffersForProduct(barcode, countryCode)
    private val campaignsFlow = if (currentUserId != null) {
        receiptAndCampaignRepository.observeActiveCampaigns()
            .catch { if (it is CancellationException) throw it else emit(emptyList()) }
    } else {
        flowOf(emptyList<Campaign>())
    }
    private val txFlow = if (currentUserId != null) {
        userWalletRepository.observeTransactions(currentUserId)
            .catch { if (it is CancellationException) throw it else emit(emptyList()) }
    } else {
        flowOf(emptyList<RewardTransaction>())
    }

    val uiState: StateFlow<ProductDetailUiState> = combine(
        _productState,
        offersFlow,
        campaignsFlow,
        txFlow
    ) { productPair, offers, campaigns, transactions ->
        if (productPair == null) {
            ProductDetailUiState.Loading
        } else {
            val (product, cached) = productPair
            val eligibility = campaigns.map { campaign ->
                CampaignEligibilityEngine.evaluate(
                    campaign = campaign,
                    countryCode = countryCode,
                    product = product,
                    userTransactions = transactions
                )
            }
            ProductDetailUiState.Loaded(
                barcode = barcode,
                product = product,
                fromOfflineCache = cached,
                offers = offers,
                eligibleCampaigns = eligibility
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = ProductDetailUiState.Loading
    )

    init {
        refreshProduct()
    }

    fun refreshProduct() {
        viewModelScope.launch {
            try {
                when (val outcome = productRepository.lookupProduct(barcode)) {
                    is ProductLookupOutcome.Found -> _productState.value = outcome.product to outcome.fromOfflineCache
                    is ProductLookupOutcome.NotFound -> _productState.value = null to false
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _productState.value = null to false
            }
        }
    }

    fun submitMissingProduct(name: String, brand: String, category: String, description: String) {
        viewModelScope.launch {
            val res = productRepository.submitUnverifiedProduct(barcode, name, brand, category, description)
            res.onSuccess {
                _productState.value = it to false
                _actionMessage.value = "Submitted unverified community product entry."
            }.onFailure {
                _actionMessage.value = it.localizedMessage ?: "Submission failed."
            }
        }
    }

    fun submitPriceObservation(storeName: String, priceText: String, currencyCode: String, offerUrl: String) {
        val price = priceText.toDoubleOrNull()
        if (price == null || price <= 0.0) {
            _actionMessage.value = "Enter a valid positive price."
            return
        }
        viewModelScope.launch {
            val res = productRepository.submitUnverifiedOffer(
                barcode = barcode,
                storeName = storeName,
                price = price,
                currencyCode = currencyCode,
                countryCode = countryCode,
                offerUrl = offerUrl
            )
            res.onSuccess {
                _actionMessage.value = "Price observation saved (labeled Unverified)."
            }.onFailure {
                _actionMessage.value = it.localizedMessage ?: "Failed to save price observation."
            }
        }
    }

    fun clearActionMessage() {
        _actionMessage.value = null
    }
}
