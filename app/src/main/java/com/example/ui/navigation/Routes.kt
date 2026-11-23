package com.example.ui.navigation

import kotlinx.serialization.Serializable

sealed interface AppRoute {
    // 5 Primary Bottom Navigation Destinations
    @Serializable
    data object Home : AppRoute

    @Serializable
    data object Scan : AppRoute

    @Serializable
    data object Rewards : AppRoute

    @Serializable
    data object Discover : AppRoute

    @Serializable
    data object Profile : AppRoute

    // Secondary Destinations
    @Serializable
    data class ProductDetail(val barcode: String) : AppRoute

    @Serializable
    data object ScanHistory : AppRoute

    @Serializable
    data class OfferDetail(
        val barcode: String,
        val storeName: String,
        val price: Double,
        val currencyCode: String,
        val countryCode: String,
        val availabilityStatus: String,
        val verified: Boolean,
        val offerUrl: String,
        val lastVerifiedText: String
    ) : AppRoute

    @Serializable
    data class ReceiptSubmissionRoute(val preselectedCampaignId: String? = null) : AppRoute

    @Serializable
    data class TransactionDetail(val transactionId: String) : AppRoute

    @Serializable
    data object Notifications : AppRoute

    @Serializable
    data object HelpSupport : AppRoute

    @Serializable
    data object PrivacySettings : AppRoute

    @Serializable
    data object TermsRules : AppRoute

    @Serializable
    data object CountryLanguageSettings : AppRoute

    @Serializable
    data object AdminConsole : AppRoute
}
