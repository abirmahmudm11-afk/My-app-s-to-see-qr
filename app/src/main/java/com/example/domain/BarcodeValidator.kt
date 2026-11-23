package com.example.domain

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
    MANUAL("Manual GTIN")
}

sealed interface BarcodeValidationResult {
    data class Valid(
        val normalizedValue: String,
        val format: SupportedBarcodeFormat
    ) : BarcodeValidationResult

    data class Invalid(val reason: String) : BarcodeValidationResult
}

object BarcodeValidator {

    fun validate(rawValue: String, format: SupportedBarcodeFormat): BarcodeValidationResult {
        val trimmed = rawValue.trim()
        if (trimmed.isEmpty()) {
            return BarcodeValidationResult.Invalid("Barcode cannot be empty.")
        }
        if (trimmed.length > 128) {
            return BarcodeValidationResult.Invalid("Barcode exceeds maximum 128-character limit.")
        }

        return when (format) {
            SupportedBarcodeFormat.EAN_13 -> validateNumericGtin(trimmed, 13, format)
            SupportedBarcodeFormat.EAN_8 -> validateNumericGtin(trimmed, 8, format)
            SupportedBarcodeFormat.UPC_A -> validateNumericGtin(trimmed, 12, format)
            SupportedBarcodeFormat.UPC_E -> {
                if (!trimmed.all { it.isDigit() } || (trimmed.length != 6 && trimmed.length != 8)) {
                    BarcodeValidationResult.Invalid("UPC-E must be 6 or 8 digits.")
                } else {
                    val expanded = expandUpcE(trimmed)
                    if (expanded != null && isValidGs1CheckDigit(expanded)) {
                        BarcodeValidationResult.Valid(trimmed, format)
                    } else if (trimmed.length == 6) {
                        BarcodeValidationResult.Valid(trimmed, format)
                    } else {
                        BarcodeValidationResult.Invalid("Invalid UPC-E check digit.")
                    }
                }
            }
            SupportedBarcodeFormat.ITF -> {
                if (!trimmed.all { it.isDigit() } || trimmed.length < 6 || trimmed.length % 2 != 0) {
                    BarcodeValidationResult.Invalid("ITF barcode must have an even number of at least 6 digits.")
                } else {
                    BarcodeValidationResult.Valid(trimmed, format)
                }
            }
            SupportedBarcodeFormat.CODE_39 -> {
                val allowed = Regex("^[A-Z0-9\\-. $/+%*]+$")
                if (!allowed.matches(trimmed.uppercase())) {
                    BarcodeValidationResult.Invalid("Code 39 contains unsupported characters.")
                } else {
                    BarcodeValidationResult.Valid(trimmed, format)
                }
            }
            SupportedBarcodeFormat.CODABAR -> {
                val allowed = Regex("^[A-Da-d]?[0-9\\-$:/.+]+[A-Da-d]?$")
                if (!allowed.matches(trimmed)) {
                    BarcodeValidationResult.Invalid("Codabar contains invalid characters.")
                } else {
                    BarcodeValidationResult.Valid(trimmed, format)
                }
            }
            SupportedBarcodeFormat.CODE_128,
            SupportedBarcodeFormat.CODE_93,
            SupportedBarcodeFormat.QR_CODE,
            SupportedBarcodeFormat.DATA_MATRIX,
            SupportedBarcodeFormat.PDF417,
            SupportedBarcodeFormat.AZTEC -> {
                if (trimmed.any { it.isISOControl() }) {
                    BarcodeValidationResult.Invalid("Barcode contains invalid control characters.")
                } else {
                    BarcodeValidationResult.Valid(trimmed, format)
                }
            }
            SupportedBarcodeFormat.MANUAL -> validateManualInput(trimmed)
        }
    }

    fun validateManualInput(rawValue: String): BarcodeValidationResult {
        val cleaned = rawValue.trim()
        if (cleaned.isEmpty()) {
            return BarcodeValidationResult.Invalid("Enter a barcode number or code.")
        }
        if (cleaned.all { it.isDigit() }) {
            return when (cleaned.length) {
                8 -> validateNumericGtin(cleaned, 8, SupportedBarcodeFormat.EAN_8)
                12 -> validateNumericGtin(cleaned, 12, SupportedBarcodeFormat.UPC_A)
                13 -> validateNumericGtin(cleaned, 13, SupportedBarcodeFormat.EAN_13)
                14 -> validateNumericGtin(cleaned, 14, SupportedBarcodeFormat.ITF)
                in 6..18 -> BarcodeValidationResult.Valid(cleaned, SupportedBarcodeFormat.CODE_128)
                else -> BarcodeValidationResult.Invalid("Numeric GTIN must be 8, 12, 13, or 14 digits (or 6-18 digits for Code 128).")
            }
        }
        if (cleaned.length in 3..64 && !cleaned.any { it.isISOControl() }) {
            return BarcodeValidationResult.Valid(cleaned, SupportedBarcodeFormat.CODE_128)
        }
        return BarcodeValidationResult.Invalid("Unsupported barcode format or length.")
    }

    private fun validateNumericGtin(
        value: String,
        expectedLength: Int,
        format: SupportedBarcodeFormat
    ): BarcodeValidationResult {
        if (!value.all { it.isDigit() } || value.length != expectedLength) {
            return BarcodeValidationResult.Invalid("${format.label} must be exactly $expectedLength digits.")
        }
        if (!isValidGs1CheckDigit(value)) {
            return BarcodeValidationResult.Invalid("${format.label} failed GS1 check-digit verification.")
        }
        return BarcodeValidationResult.Valid(value, format)
    }

    /**
     * Standard GS1 Modulo-10 check-digit verification for EAN-8, UPC-A (GTIN-12), EAN-13, GTIN-14.
     */
    fun isValidGs1CheckDigit(digits: String): Boolean {
        if (digits.length < 2 || !digits.all { it.isDigit() }) return false
        val body = digits.dropLast(1)
        val checkDigit = digits.last().digitToInt()
        var sum = 0
        // Traverse right-to-left: rightmost payload digit has weight 3, alternating 3 and 1
        for (i in body.indices) {
            val digit = body[body.length - 1 - i].digitToInt()
            val weight = if (i % 2 == 0) 3 else 1
            sum += digit * weight
        }
        val expected = (10 - (sum % 10)) % 10
        return checkDigit == expected
    }

    private fun expandUpcE(upce: String): String? {
        val core = when (upce.length) {
            6 -> upce
            8 -> upce.substring(1, 7)
            else -> return null
        }
        val numberSystem = if (upce.length == 8) upce[0] else '0'
        val check = if (upce.length == 8) upce[7].toString() else ""
        val d1 = core[0]
        val d2 = core[1]
        val d3 = core[2]
        val d4 = core[3]
        val d5 = core[4]
        val d6 = core[5]
        val manufacturerAndItem = when (d6) {
            '0', '1', '2' -> "$d1$d2${d6}0000$d3$d4$d5"
            '3' -> "$d1$d2${d3}00000$d4$d5"
            '4' -> "$d1$d2$d3${d4}00000$d5"
            else -> "$d1$d2$d3$d4${d5}0000$d6"
        }
        return "$numberSystem$manufacturerAndItem$check"
    }
}

object ReceiptHashUtil {
    fun sha256ReferenceHash(
        storeName: String,
        transactionDate: String,
        totalAmount: Double,
        currencyCode: String,
        receiptReference: String
    ): String {
        val canonical = listOf(
            storeName.trim().lowercase(),
            transactionDate.trim(),
            String.format(java.util.Locale.US, "%.2f", totalAmount),
            currencyCode.trim().uppercase(),
            receiptReference.trim().uppercase()
        ).joinToString("|")
        val bytes = MessageDigest.getInstance("SHA-256").digest(canonical.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
