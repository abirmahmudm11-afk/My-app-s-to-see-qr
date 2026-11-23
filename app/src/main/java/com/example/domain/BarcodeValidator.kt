package com.example.domain

import com.google.mlkit.vision.barcode.common.Barcode
import java.security.MessageDigest

enum class SupportedBarcodeFormat(val label: String) {
  EAN_13("EAN-13"),
  EAN_8("EAN-8"),
  UPC_A("UPC-A"),
  UPC_E("UPC-E"),
  CODE_128("Code 128"),
  CODE_39("Code 39"),
  CODE_93("Code 93"),
  QR_CODE("QR Code"),
  DATA_MATRIX("Data Matrix"),
  PDF417("PDF417"),
  AZTEC("Aztec"),
  ITF("ITF"),
  CODABAR("Codabar"),
  MANUAL("Manual GTIN");

  companion object {
    fun fromMlKitFormat(format: Int): SupportedBarcodeFormat = when (format) {
      Barcode.FORMAT_EAN_13 -> EAN_13
      Barcode.FORMAT_EAN_8 -> EAN_8
      Barcode.FORMAT_UPC_A -> UPC_A
      Barcode.FORMAT_UPC_E -> UPC_E
      Barcode.FORMAT_CODE_128 -> CODE_128
      Barcode.FORMAT_CODE_39 -> CODE_39
      Barcode.FORMAT_CODE_93 -> CODE_93
      Barcode.FORMAT_QR_CODE -> QR_CODE
      Barcode.FORMAT_DATA_MATRIX -> DATA_MATRIX
      Barcode.FORMAT_PDF417 -> PDF417
      Barcode.FORMAT_AZTEC -> AZTEC
      Barcode.FORMAT_ITF -> ITF
      Barcode.FORMAT_CODABAR -> CODABAR
      else -> CODE_128
    }
  }
}

sealed interface BarcodeValidationResult {
  data class Valid(
    val normalizedValue: String,
    val format: SupportedBarcodeFormat,
  ) : BarcodeValidationResult

  data class Invalid(val reason: String) : BarcodeValidationResult
}

object BarcodeValidator {
  fun validate(rawValue: String, format: SupportedBarcodeFormat): BarcodeValidationResult {
    val clean = rawValue.trim()
    if (clean.isEmpty()) {
      return BarcodeValidationResult.Invalid("Barcode value cannot be empty.")
    }
    if (clean.length > 128) {
      return BarcodeValidationResult.Invalid("Barcode exceeds maximum supported length (128 characters).")
    }

    val effectiveFormat = if (format == SupportedBarcodeFormat.MANUAL && clean.all { it.isDigit() }) {
      when (clean.length) {
        8 -> SupportedBarcodeFormat.EAN_8
        12 -> SupportedBarcodeFormat.UPC_A
        13 -> SupportedBarcodeFormat.EAN_13
        else -> SupportedBarcodeFormat.MANUAL
      }
    } else {
      format
    }

    return when (effectiveFormat) {
      SupportedBarcodeFormat.EAN_13 -> {
        if (!clean.matches(Regex("^\\d{13}$"))) {
          BarcodeValidationResult.Invalid("EAN-13 must be 13 numeric digits.")
        } else if (!verifyGs1CheckDigit(clean)) {
          BarcodeValidationResult.Invalid("EAN-13 check digit failed GS1 Modulo-10 validation.")
        } else {
          BarcodeValidationResult.Valid(clean, SupportedBarcodeFormat.EAN_13)
        }
      }
      SupportedBarcodeFormat.EAN_8 -> {
        if (!clean.matches(Regex("^\\d{8}$"))) {
          BarcodeValidationResult.Invalid("EAN-8 must be 8 numeric digits.")
        } else if (!verifyGs1CheckDigit(clean)) {
          BarcodeValidationResult.Invalid("EAN-8 check digit failed GS1 Modulo-10 validation.")
        } else {
          BarcodeValidationResult.Valid(clean, SupportedBarcodeFormat.EAN_8)
        }
      }
      SupportedBarcodeFormat.UPC_A -> {
        if (!clean.matches(Regex("^\\d{12}$"))) {
          BarcodeValidationResult.Invalid("UPC-A must be 12 numeric digits.")
        } else if (!verifyGs1CheckDigit(clean)) {
          BarcodeValidationResult.Invalid("UPC-A check digit failed GS1 Modulo-10 validation.")
        } else {
          BarcodeValidationResult.Valid(clean, SupportedBarcodeFormat.UPC_A)
        }
      }
      SupportedBarcodeFormat.UPC_E -> {
        if (!clean.matches(Regex("^\\d{8}$"))) {
          BarcodeValidationResult.Invalid("UPC-E must be 8 numeric digits.")
        } else {
          val expanded = expandUpcEToUpcA(clean)
          if (verifyGs1CheckDigit(expanded)) {
            BarcodeValidationResult.Valid(clean, SupportedBarcodeFormat.UPC_E)
          } else {
            BarcodeValidationResult.Invalid("UPC-E check digit is invalid.")
          }
        }
      }
      SupportedBarcodeFormat.ITF -> {
        if (!clean.matches(Regex("^\\d{6,14}$")) || clean.length % 2 != 0) {
          BarcodeValidationResult.Invalid("ITF barcode must have an even number of digits (6-14).")
        } else {
          BarcodeValidationResult.Valid(clean, SupportedBarcodeFormat.ITF)
        }
      }
      SupportedBarcodeFormat.MANUAL -> {
        if (clean.length < 3) {
          BarcodeValidationResult.Invalid("Enter at least 3 characters for manual lookup.")
        } else {
          BarcodeValidationResult.Valid(clean, SupportedBarcodeFormat.MANUAL)
        }
      }
      else -> BarcodeValidationResult.Valid(clean, effectiveFormat)
    }
  }

  fun verifyGs1CheckDigit(digits: String): Boolean {
    if (digits.length < 2 || !digits.all { it.isDigit() }) return false
    val body = digits.dropLast(1)
    val checkDigit = digits.last().digitToInt()
    var sum = 0
    for (i in body.indices) {
      val digit = body[body.length - 1 - i].digitToInt()
      sum += if (i % 2 == 0) digit * 3 else digit
    }
    val calculated = (10 - (sum % 10)) % 10
    return calculated == checkDigit
  }

  fun expandUpcEToUpcA(upcE: String): String {
    if (upcE.length != 8 || !upcE.all { it.isDigit() }) return upcE
    val numberSystem = upcE[0]
    val d1 = upcE[1]
    val d2 = upcE[2]
    val d3 = upcE[3]
    val d4 = upcE[4]
    val d5 = upcE[5]
    val d6 = upcE[6]
    val check = upcE[7]
    val body = when (d6) {
      '0', '1', '2' -> "$numberSystem$d1$d2${d6}0000$d3$d4$d5"
      '3' -> "$numberSystem$d1$d2${d3}00000$d4$d5"
      '4' -> "$numberSystem$d1$d2$d3${d4}00000$d5"
      else -> "$numberSystem$d1$d2$d3$d4${d5}0000$d6"
    }
    return "$body$check"
  }

  fun sha256ReceiptHash(
    storeName: String,
    transactionDate: String,
    totalAmount: Double,
    currencyCode: String,
    receiptReference: String,
  ): String {
    val formattedAmount = "%.2f".format(java.util.Locale.US, totalAmount)
    val raw = "${storeName.trim().lowercase()}|${transactionDate.trim()}|$formattedAmount|${currencyCode.trim().uppercase()}|${receiptReference.trim().lowercase()}"
    val digest = MessageDigest.getInstance("SHA-256").digest(raw.toByteArray(Charsets.UTF_8))
    return digest.joinToString("") { "%02x".format(it) }
  }

  sealed interface QrPayloadResolution {
    data class WebUrl(val url: String, val embeddedBarcode: String? = null) : QrPayloadResolution
    data class PromoCode(val code: String, val campaignHint: String? = null) : QrPayloadResolution
    data object ProductBarcode : QrPayloadResolution
  }

  fun classifyQrPayload(rawValue: String, format: SupportedBarcodeFormat): QrPayloadResolution {
    val clean = rawValue.trim()
    val lower = clean.lowercase()

    // Safe HTTP/HTTPS URL handling (blocks javascript:, file:, data:, intent:)
    if (lower.startsWith("https://") || lower.startsWith("http://")) {
      val embeddedBarcode = Regex("(?:barcode|gtin|ean|product)[=/](\\d{8,14})", RegexOption.IGNORE_CASE)
        .find(clean)
        ?.groupValues
        ?.getOrNull(1)
      return QrPayloadResolution.WebUrl(url = clean, embeddedBarcode = embeddedBarcode)
    }

    // Promo / App Deep Link QR payloads
    if (lower.startsWith("promo:") || lower.startsWith("scan2money:promo:") || lower.startsWith("coupon:")) {
      val code = clean.substringAfterLast(":").trim().uppercase().take(40)
      if (code.isNotEmpty()) {
        return QrPayloadResolution.PromoCode(code = code)
      }
    }
    if (lower.startsWith("scan2money:campaign:")) {
      val id = clean.substringAfterLast(":").trim().take(64)
      return QrPayloadResolution.PromoCode(code = id, campaignHint = id)
    }

    // Non-numeric QR/Aztec/DataMatrix text that isn't a pure GS1 numeric code
    if (format in setOf(SupportedBarcodeFormat.QR_CODE, SupportedBarcodeFormat.AZTEC, SupportedBarcodeFormat.DATA_MATRIX) &&
      !clean.all { it.isDigit() }
    ) {
      return QrPayloadResolution.PromoCode(code = clean.take(40))
    }

    return QrPayloadResolution.ProductBarcode
  }
}
