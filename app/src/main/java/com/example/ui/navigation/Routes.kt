package com.example.ui.navigation

object Routes {
  const val ONBOARDING = "onboarding"
  const val AUTH = "auth"
  const val HOME = "home"
  const val DISCOVER = "discover"
  const val SCAN = "scan"
  const val EARNINGS = "earnings"
  const val REWARDS = "rewards"
  const val PROFILE = "profile"

  const val PRODUCT_DETAIL = "product/{barcode}"
  const val OFFER_DETAIL = "offer/{barcode}/{offerId}"
  const val RECEIPT_SUBMIT = "receipt_submit?campaignId={campaignId}"
  const val TRANSACTION_DETAIL = "transaction/{txId}"
  const val SCAN_HISTORY = "scan_history"
  const val NOTIFICATIONS = "notifications"
  const val COUNTRY_LANGUAGE = "country_language"
  const val PRIVACY_SETTINGS = "privacy_settings"
  const val TERMS_RULES = "terms_rules"
  const val HELP_SUPPORT = "help_support"
  const val ADMIN_CONSOLE = "admin_console"

  fun productDetail(barcode: String): String = "product/${android.net.Uri.encode(barcode)}"

  fun offerDetail(barcode: String, offerId: String): String =
    "offer/${android.net.Uri.encode(barcode)}/${android.net.Uri.encode(offerId)}"

  fun receiptSubmit(campaignId: String? = null): String =
    if (campaignId.isNullOrBlank()) "receipt_submit" else "receipt_submit?campaignId=${android.net.Uri.encode(campaignId)}"

  fun transactionDetail(txId: String): String = "transaction/${android.net.Uri.encode(txId)}"
}
