package com.example.domain

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

data class MarketInfo(
  val countryCode: String,
  val countryName: String,
  val currencyCode: String,
  val defaultLanguage: String,
  val languageLabel: String,
)

object MarketConfig {
  val SUPPORTED_MARKETS: List<MarketInfo> = listOf(
    MarketInfo("US", "United States", "USD", "en", "English"),
    MarketInfo("GB", "United Kingdom", "GBP", "en", "English (UK)"),
    MarketInfo("DE", "Germany", "EUR", "de", "Deutsch"),
    MarketInfo("JP", "Japan", "JPY", "ja", "日本語"),
    MarketInfo("SG", "Singapore", "SGD", "en", "English (SG)"),
    MarketInfo("CA", "Canada", "CAD", "en", "English / Français"),
  )

  fun findByCountry(countryCode: String): MarketInfo =
    SUPPORTED_MARKETS.firstOrNull { it.countryCode.equals(countryCode, ignoreCase = true) }
      ?: SUPPORTED_MARKETS.first()

  fun formatMoney(amount: Double, currencyCode: String): String {
    return try {
      val format = NumberFormat.getCurrencyInstance(Locale.US)
      format.currency = Currency.getInstance(currencyCode.uppercase())
      format.format(amount)
    } catch (_: Exception) {
      "%.2f %s".format(Locale.US, amount, currencyCode.uppercase())
    }
  }
}
