package com.example.domain

import java.math.BigDecimal
import java.math.RoundingMode

sealed interface WithdrawalValidationResult {
  data class Valid(
    val normalizedAddress: String,
    val amountMicros: Long,
  ) : WithdrawalValidationResult

  data class Invalid(val message: String) : WithdrawalValidationResult
}

object EarningsEngine {
  const val MICROS_PER_DOLLAR: Long = 1_000_000L
  const val MIN_PAYOUT_MICROS: Long = 1_000_000L // $1.00 USD minimum payout
  const val MAX_SINGLE_AD_SHARE_MICROS: Long = 1_000_000L // $1.00 USD max user share per ad impression
  const val USER_SHARE_PERCENT: Int = 10
  const val NETWORK_BEP20: String = "BEP20"
  const val ASSET_USDT: String = "USDT"

  private val BEP20_ADDRESS_REGEX = Regex("^0x[a-fA-F0-9]{40}$")

  /**
   * Computes the user's exact 10% share of the ad impression revenue in micros ($1 = 1,000,000 micros).
   * Using integer division in micros avoids floating-point drift and guarantees exact 1:1 parity
   * with Firestore security rules.
   */
  fun calculateTenPercentShareMicros(adValueMicros: Long): Long {
    if (adValueMicros <= 0L) return 0L
    return (adValueMicros / 10L).coerceAtMost(MAX_SINGLE_AD_SHARE_MICROS)
  }

  fun isValidBep20Address(address: String): Boolean {
    return BEP20_ADDRESS_REGEX.matches(address.trim())
  }

  fun parseUsdToMicros(rawInput: String): Long? {
    val clean = rawInput.trim().removePrefix("$").replace(",", "").trim()
    if (clean.isEmpty()) return null
    val decimal = clean.toBigDecimalOrNull() ?: return null
    if (decimal <= BigDecimal.ZERO) return null
    return runCatching {
      decimal.setScale(6, RoundingMode.DOWN)
        .multiply(BigDecimal(MICROS_PER_DOLLAR))
        .longValueExact()
    }.getOrNull()
  }

  fun formatUsdFromMicros(micros: Long, showCurrencySuffix: Boolean = false): String {
    val clamped = micros.coerceAtLeast(0L)
    val bd = BigDecimal.valueOf(clamped, 6)
    val formatted = when {
      clamped % 10_000L == 0L -> bd.setScale(2, RoundingMode.DOWN).toPlainString()
      clamped % 100L == 0L -> bd.setScale(4, RoundingMode.DOWN).toPlainString()
      else -> bd.stripTrailingZeros().let {
        if (it.scale() < 2) it.setScale(2, RoundingMode.DOWN) else it
      }.toPlainString()
    }
    return if (showCurrencySuffix) "$$formatted USD" else "$$formatted"
  }

  fun thresholdProgressFraction(availableMicros: Long): Float {
    if (availableMicros <= 0L) return 0f
    return (availableMicros.toFloat() / MIN_PAYOUT_MICROS.toFloat()).coerceIn(0f, 1f)
  }

  fun validateWithdrawal(
    availableMicros: Long,
    amountText: String,
    walletAddress: String,
  ): WithdrawalValidationResult {
    val trimmedAddress = walletAddress.trim()
    if (availableMicros < MIN_PAYOUT_MICROS) {
      return WithdrawalValidationResult.Invalid(
        "Minimum balance of $1.00 USD is required before withdrawing (current balance: ${formatUsdFromMicros(availableMicros)}).",
      )
    }
    if (!isValidBep20Address(trimmedAddress)) {
      return WithdrawalValidationResult.Invalid(
        "Enter a valid Binance USDT BEP-20 wallet address (0x followed by 40 hex characters).",
      )
    }
    val amountMicros = parseUsdToMicros(amountText)
      ?: return WithdrawalValidationResult.Invalid("Enter a valid USD/USDT withdrawal amount (minimum $1.00).")
    if (amountMicros < MIN_PAYOUT_MICROS) {
      return WithdrawalValidationResult.Invalid("Minimum withdrawal amount is $1.00 USDT.")
    }
    if (amountMicros > availableMicros) {
      return WithdrawalValidationResult.Invalid(
        "Requested amount exceeds available balance (${formatUsdFromMicros(availableMicros)}).",
      )
    }
    return WithdrawalValidationResult.Valid(
      normalizedAddress = trimmedAddress,
      amountMicros = amountMicros,
    )
  }
}
