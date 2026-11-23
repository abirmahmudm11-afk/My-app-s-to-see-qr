package com.example.domain

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

data class CountryMarket(
    val countryCode: String,
    val countryName: String,
    val defaultLanguage: String,
    val languageLabel: String,
    val currencyCode: String,
    val currencyPrecision: Int,
    val productCoverageNote: String,
    val merchantCoverageNote: String,
    val payoutStatusNote: String,
    val sandboxExchangeNote: String
)

object MarketCatalog {
    val markets: List<CountryMarket> = listOf(
        CountryMarket(
            countryCode = "US",
            countryName = "United States",
            defaultLanguage = "en",
            languageLabel = "English (US)",
            currencyCode = "USD",
            currencyPrecision = 2,
            productCoverageNote = "UPC-A, UPC-E, EAN-13 via Open Food Facts & partner feeds",
            merchantCoverageNote = "Sandbox grocery & retail partners",
            payoutStatusNote = "Cashback payouts disabled in Sandbox v1.0",
            sandboxExchangeNote = "Points tracked in PTS only; no cash conversion active"
        ),
        CountryMarket(
            countryCode = "GB",
            countryName = "United Kingdom",
            defaultLanguage = "en",
            languageLabel = "English (UK)",
            currencyCode = "GBP",
            currencyPrecision = 2,
            productCoverageNote = "EAN-13 & EAN-8 via Open Food Facts UK",
            merchantCoverageNote = "Limited UK test retail catalog",
            payoutStatusNote = "Cashback payouts disabled in Sandbox v1.0",
            sandboxExchangeNote = "Points tracked in PTS only; UK consumer terms apply"
        ),
        CountryMarket(
            countryCode = "DE",
            countryName = "Germany",
            defaultLanguage = "de",
            languageLabel = "Deutsch",
            currencyCode = "EUR",
            currencyPrecision = 2,
            productCoverageNote = "EAN-13 via Open Food Facts EU (ODbL 1.0)",
            merchantCoverageNote = "EU pilot catalog coverage",
            payoutStatusNote = "Cashback payouts disabled in Sandbox v1.0",
            sandboxExchangeNote = "GDPR-minimized local camera processing; PTS ledger only"
        ),
        CountryMarket(
            countryCode = "JP",
            countryName = "Japan",
            defaultLanguage = "ja",
            languageLabel = "日本語 / English",
            currencyCode = "JPY",
            currencyPrecision = 0,
            productCoverageNote = "JAN (EAN-13 / EAN-8) coverage via Open Food Facts JP",
            merchantCoverageNote = "Sandbox pilot catalog only",
            payoutStatusNote = "Cashback payouts disabled in Sandbox v1.0",
            sandboxExchangeNote = "Zero-decimal JPY offer display; PTS ledger only"
        ),
        CountryMarket(
            countryCode = "SG",
            countryName = "Singapore",
            defaultLanguage = "en",
            languageLabel = "English (SG)",
            currencyCode = "SGD",
            currencyPrecision = 2,
            productCoverageNote = "EAN-13 & EAN-8 coverage via Open Food Facts",
            merchantCoverageNote = "APAC pilot discovery catalog",
            payoutStatusNote = "Cashback payouts disabled in Sandbox v1.0",
            sandboxExchangeNote = "PDPA-compliant consent controls; PTS ledger only"
        ),
        CountryMarket(
            countryCode = "CA",
            countryName = "Canada",
            defaultLanguage = "en",
            languageLabel = "English / Français",
            currencyCode = "CAD",
            currencyPrecision = 2,
            productCoverageNote = "UPC-A & EAN-13 via Open Food Facts CA",
            merchantCoverageNote = "North America pilot catalog",
            payoutStatusNote = "Cashback payouts disabled in Sandbox v1.0",
            sandboxExchangeNote = "Points tracked in PTS only; no cash conversion active"
        )
    )

    fun findByCountry(code: String): CountryMarket {
        return markets.firstOrNull { it.countryCode.equals(code, ignoreCase = true) } ?: markets.first()
    }

    fun formatMoney(amount: Double, currencyCode: String): String {
        return try {
            val format = NumberFormat.getCurrencyInstance(Locale.US)
            val currency = Currency.getInstance(currencyCode)
            format.currency = currency
            format.maximumFractionDigits = currency.defaultFractionDigits.coerceAtLeast(0)
            format.minimumFractionDigits = currency.defaultFractionDigits.coerceAtLeast(0)
            format.format(amount)
        } catch (_: Exception) {
            String.format(Locale.US, "%s %.2f", currencyCode, amount)
        }
    }
}
