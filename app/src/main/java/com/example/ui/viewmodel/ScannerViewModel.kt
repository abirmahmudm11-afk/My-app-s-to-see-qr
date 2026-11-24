package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.CatalogProduct
import com.example.data.model.VerificationStatus
import com.example.data.repository.ProductLookupOutcome
import com.example.di.AppContainer
import com.example.domain.BarcodeValidationResult
import com.example.domain.BarcodeValidator
import com.example.domain.SupportedBarcodeFormat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ScannerUiState {
  data object Idle : ScannerUiState
  data class LookingUp(val barcode: String, val format: SupportedBarcodeFormat) : ScannerUiState
  data class Matched(
    val barcode: String,
    val format: SupportedBarcodeFormat,
    val product: CatalogProduct,
    val fromOfflineCache: Boolean,
  ) : ScannerUiState

  data class QrUrlDetected(
    val rawValue: String,
    val url: String,
    val embeddedBarcode: String?,
    val format: SupportedBarcodeFormat,
  ) : ScannerUiState

  data class QrPromoDetected(
    val rawValue: String,
    val promoCode: String,
    val campaignHint: String?,
    val format: SupportedBarcodeFormat,
  ) : ScannerUiState

  data class NotFound(val barcode: String, val format: SupportedBarcodeFormat) : ScannerUiState
  data class Error(val message: String) : ScannerUiState
}

class ScannerViewModel(private val container: AppContainer) : ViewModel() {
  private val _uiState = MutableStateFlow<ScannerUiState>(ScannerUiState.Idle)
  val uiState: StateFlow<ScannerUiState> = _uiState.asStateFlow()

  private val _detectionPulseSeq = MutableStateFlow(0L)
  val detectionPulseSeq: StateFlow<Long> = _detectionPulseSeq.asStateFlow()

  private var lastProcessedBarcode: String? = null
  private var lastProcessedAtMs: Long = 0L

  fun processScannedBarcode(
    rawValue: String,
    format: SupportedBarcodeFormat,
    fromCamera: Boolean = true,
  ) {
    val clean = rawValue.trim()
    val now = System.currentTimeMillis()
    if (fromCamera && clean == lastProcessedBarcode && (now - lastProcessedAtMs) < 3_000L) {
      return
    }
    if (_uiState.value is ScannerUiState.LookingUp) return

    when (val qrResolution = BarcodeValidator.classifyQrPayload(clean, format)) {
      is BarcodeValidator.QrPayloadResolution.WebUrl -> {
        lastProcessedBarcode = clean
        lastProcessedAtMs = now
        _detectionPulseSeq.value = _detectionPulseSeq.value + 1L
        viewModelScope.launch {
          runCatching {
            container.userWalletRepository.recordScan(
              barcodeValue = clean.take(128),
              barcodeFormat = format.label,
              productName = "QR Product Link",
              brand = "",
              category = "QR Web Link",
              description = qrResolution.url,
              imageUrl = "",
              sourceProvider = "QR URL",
              verificationStatus = VerificationStatus.UNKNOWN.name,
              scanResult = "MATCHED",
            )
          }
        }
        _uiState.value = ScannerUiState.QrUrlDetected(
          rawValue = clean,
          url = qrResolution.url,
          embeddedBarcode = qrResolution.embeddedBarcode,
          format = format,
        )
        return
      }
      is BarcodeValidator.QrPayloadResolution.PromoCode -> {
        lastProcessedBarcode = clean
        lastProcessedAtMs = now
        _detectionPulseSeq.value = _detectionPulseSeq.value + 1L
        viewModelScope.launch {
          runCatching {
            container.userWalletRepository.recordScan(
              barcodeValue = clean.take(128),
              barcodeFormat = format.label,
              productName = "Promo: ${qrResolution.code}",
              brand = qrResolution.campaignHint.orEmpty(),
              category = "Promotional QR",
              description = "Promo code ${qrResolution.code}",
              imageUrl = "",
              sourceProvider = "QR Promo",
              verificationStatus = VerificationStatus.UNKNOWN.name,
              scanResult = "MATCHED",
            )
          }
        }
        _uiState.value = ScannerUiState.QrPromoDetected(
          rawValue = clean,
          promoCode = qrResolution.code,
          campaignHint = qrResolution.campaignHint,
          format = format,
        )
        return
      }
      BarcodeValidator.QrPayloadResolution.ProductBarcode -> Unit
    }

    when (val validation = BarcodeValidator.validate(clean, format)) {
      is BarcodeValidationResult.Invalid -> {
        if (!fromCamera) {
          _uiState.value = ScannerUiState.Error(validation.reason)
        }
      }
      is BarcodeValidationResult.Valid -> {
        lastProcessedBarcode = validation.normalizedValue
        lastProcessedAtMs = now
        _detectionPulseSeq.value = _detectionPulseSeq.value + 1L
        lookupValidatedBarcode(validation.normalizedValue, validation.format)
      }
    }
  }

  fun lookupEmbeddedBarcode(barcode: String) {
    _detectionPulseSeq.value = _detectionPulseSeq.value + 1L
    lookupValidatedBarcode(barcode.trim(), SupportedBarcodeFormat.MANUAL)
  }

  private fun lookupValidatedBarcode(barcode: String, format: SupportedBarcodeFormat) {
    viewModelScope.launch {
      _uiState.value = ScannerUiState.LookingUp(barcode, format)
      when (val outcome = runCatching { container.productRepository.lookupByBarcode(barcode) }.getOrNull()) {
        is ProductLookupOutcome.Found -> {
          val product = outcome.product
          runCatching {
            container.userWalletRepository.recordScan(
              barcodeValue = barcode,
              barcodeFormat = format.label,
              productName = product.name,
              brand = product.brand,
              category = product.category,
              description = product.description,
              imageUrl = product.imageUrl,
              sourceProvider = product.dataProvider,
              verificationStatus = product.verificationStatus,
              scanResult = "MATCHED",
              quantity = outcome.apiMetadata.quantity,
              nutriscoreGrade = outcome.apiMetadata.nutriscoreGrade,
            )
          }
          _uiState.value = ScannerUiState.Matched(
            barcode = barcode,
            format = format,
            product = product,
            fromOfflineCache = outcome.fromOfflineCache,
          )
        }
        is ProductLookupOutcome.NetworkError -> {
          runCatching {
            container.userWalletRepository.recordScan(
              barcodeValue = barcode,
              barcodeFormat = format.label,
              productName = "Pending Lookup ($barcode)",
              brand = "",
              category = "",
              description = outcome.message,
              imageUrl = "",
              sourceProvider = "Network Pending",
              verificationStatus = VerificationStatus.UNKNOWN.name,
              scanResult = "NOT_FOUND",
            )
          }
          _uiState.value = ScannerUiState.NotFound(barcode, format)
        }
        is ProductLookupOutcome.NotFound, null -> {
          runCatching {
            container.userWalletRepository.recordScan(
              barcodeValue = barcode,
              barcodeFormat = format.label,
              productName = "Unlisted Barcode",
              brand = "",
              category = "",
              description = "No catalog match returned",
              imageUrl = "",
              sourceProvider = "None",
              verificationStatus = VerificationStatus.UNKNOWN.name,
              scanResult = "NOT_FOUND",
            )
          }
          _uiState.value = ScannerUiState.NotFound(barcode, format)
        }
      }
    }
  }

  fun resetScanner() {
    lastProcessedBarcode = null
    _uiState.value = ScannerUiState.Idle
  }

  companion object {
    fun factory(container: AppContainer): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
      @Suppress("UNCHECKED_CAST")
      override fun <T : ViewModel> create(modelClass: Class<T>): T = ScannerViewModel(container) as T
    }
  }
}
