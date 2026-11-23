package com.example.ui.viewmodel

import com.example.data.model.CatalogProduct
import com.example.data.repository.ProductLookupOutcome
import com.example.data.repository.ProductRepository
import com.example.data.repository.UserWalletRepository
import com.example.domain.BarcodeValidationResult
import com.example.domain.BarcodeValidator
import com.example.domain.SupportedBarcodeFormat
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

sealed interface ScannerLookupState {
    data object Scanning : ScannerLookupState
    data class ValidatingAndLookingUp(val barcode: String, val formatLabel: String) : ScannerLookupState
    data class Found(
        val product: CatalogProduct,
        val formatLabel: String,
        val fromOfflineCache: Boolean
    ) : ScannerLookupState
    data class NotFound(val barcode: String, val formatLabel: String) : ScannerLookupState
    data class Error(val message: String) : ScannerLookupState
    data object TimedOut : ScannerLookupState
}

class ScannerViewModel(
    private val productRepository: ProductRepository,
    private val userWalletRepository: UserWalletRepository
) : ViewModel() {

    private val _lookupState = MutableStateFlow<ScannerLookupState>(ScannerLookupState.Scanning)
    val lookupState: StateFlow<ScannerLookupState> = _lookupState.asStateFlow()

    private val _torchEnabled = MutableStateFlow(false)
    val torchEnabled: StateFlow<Boolean> = _torchEnabled.asStateFlow()

    private var lastProcessedBarcode: String? = null
    private var lastProcessedTimestampMs: Long = 0L

    fun toggleTorch() {
        _torchEnabled.value = !_torchEnabled.value
    }

    fun setTorch(enabled: Boolean) {
        _torchEnabled.value = enabled
    }

    fun onTimeoutReached() {
        if (_lookupState.value is ScannerLookupState.Scanning) {
            _lookupState.value = ScannerLookupState.TimedOut
        }
    }

    fun resetScanner() {
        lastProcessedBarcode = null
        lastProcessedTimestampMs = 0L
        _lookupState.value = ScannerLookupState.Scanning
    }

    fun onBarcodeDetected(
        rawValue: String,
        format: SupportedBarcodeFormat,
        fromCameraFrame: Boolean = true
    ): Boolean {
        val now = System.currentTimeMillis()
        if (fromCameraFrame) {
            if (_lookupState.value !is ScannerLookupState.Scanning) return false
            if (rawValue == lastProcessedBarcode && (now - lastProcessedTimestampMs) < DUPLICATE_COOLDOWN_MS) {
                return false
            }
        }

        when (val validation = BarcodeValidator.validate(rawValue, format)) {
            is BarcodeValidationResult.Invalid -> {
                if (!fromCameraFrame) {
                    _lookupState.value = ScannerLookupState.Error(validation.reason)
                }
                return false
            }
            is BarcodeValidationResult.Valid -> {
                lastProcessedBarcode = validation.normalizedValue
                lastProcessedTimestampMs = now
                lookupNormalizedBarcode(validation.normalizedValue, validation.format.label)
                return true
            }
        }
    }

    fun lookupManualBarcode(rawInput: String) {
        onBarcodeDetected(rawInput, SupportedBarcodeFormat.MANUAL, fromCameraFrame = false)
    }

    fun reportError(message: String) {
        _lookupState.value = ScannerLookupState.Error(message)
    }

    private fun lookupNormalizedBarcode(barcode: String, formatLabel: String) {
        viewModelScope.launch {
            _lookupState.value = ScannerLookupState.ValidatingAndLookingUp(barcode, formatLabel)
            try {
                when (val outcome = productRepository.lookupProduct(barcode)) {
                    is ProductLookupOutcome.Found -> {
                        userWalletRepository.recordScan(
                            barcodeValue = barcode,
                            barcodeFormat = formatLabel,
                            productName = outcome.product.name,
                            brand = outcome.product.brand,
                            sourceProvider = outcome.product.dataProvider,
                            verificationStatus = outcome.product.verificationStatus,
                            scanResult = "MATCHED"
                        )
                        _lookupState.value = ScannerLookupState.Found(
                            product = outcome.product,
                            formatLabel = formatLabel,
                            fromOfflineCache = outcome.fromOfflineCache
                        )
                    }
                    is ProductLookupOutcome.NotFound -> {
                        userWalletRepository.recordScan(
                            barcodeValue = barcode,
                            barcodeFormat = formatLabel,
                            productName = "",
                            brand = "",
                            sourceProvider = "None",
                            verificationStatus = "UNKNOWN",
                            scanResult = "NOT_FOUND"
                        )
                        _lookupState.value = ScannerLookupState.NotFound(
                            barcode = barcode,
                            formatLabel = formatLabel
                        )
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _lookupState.value = ScannerLookupState.Error(
                    e.localizedMessage ?: "Product lookup failed. Check network or retry."
                )
            }
        }
    }

    private companion object {
        const val DUPLICATE_COOLDOWN_MS = 2500L
    }
}
