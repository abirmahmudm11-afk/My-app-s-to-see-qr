package com.example.data.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties

enum class VerificationStatus(val label: String) {
  VERIFIED_MERCHANT("Verified Merchant"),
  LICENSED_PROVIDER("Licensed Provider"),
  OPEN_FOOD_FACTS("Open Food Facts"),
  USER_SUBMITTED_UNVERIFIED("User-Submitted (Unverified)"),
  UNKNOWN("Unknown Source");

  companion object {
    fun fromString(raw: String?): VerificationStatus =
      entries.firstOrNull { it.name == raw } ?: UNKNOWN
  }
}

enum class OfferAvailability(val label: String) {
  IN_STOCK("In Stock"),
  LIMITED("Limited Stock"),
  OUT_OF_STOCK("Out of Stock"),
  UNVERIFIED("Unverified Availability");

  companion object {
    fun fromString(raw: String?): OfferAvailability =
      entries.firstOrNull { it.name == raw } ?: UNVERIFIED
  }
}

enum class ReceiptReviewStatus {
  PENDING,
  APPROVED,
  REJECTED;

  companion object {
    fun fromString(raw: String?): ReceiptReviewStatus =
      entries.firstOrNull { it.name == raw } ?: PENDING
  }
}

enum class TransactionType {
  CAMPAIGN_BONUS,
  RECEIPT_REWARD,
  REDEMPTION,
  REVERSAL
}

enum class TransactionStatus {
  PENDING,
  APPROVED,
  CREDITED,
  REDEEMED,
  REJECTED,
  EXPIRED,
  REVERSED;

  companion object {
    fun fromString(raw: String?): TransactionStatus =
      entries.firstOrNull { it.name == raw } ?: PENDING
  }
}

enum class CampaignStatus {
  DRAFT,
  REVIEW,
  APPROVED,
  ACTIVE,
  PAUSED,
  EXPIRED,
  ARCHIVED;

  companion object {
    fun fromString(raw: String?): CampaignStatus =
      entries.firstOrNull { it.name == raw } ?: DRAFT
  }
}

enum class AdminRoleName {
  SUPER_ADMIN,
  RECEIPT_REVIEWER,
  CAMPAIGN_MANAGER,
  CATALOG_MANAGER;

  companion object {
    fun fromString(raw: String?): AdminRoleName =
      entries.firstOrNull { it.name == raw } ?: RECEIPT_REVIEWER
  }
}

@Keep
@IgnoreExtraProperties
data class UserProfile(
  val id: String = "",
  val userId: String = "",
  val displayName: String = "",
  val preferredLanguage: String = "en",
  val countryCode: String = "US",
  val currencyCode: String = "USD",
  val accountStatus: String = "ACTIVE",
  val marketingConsent: Boolean = false,
  val darkThemeEnabled: Boolean = false,
  val reducedMotionEnabled: Boolean = false,
  val createdAt: Timestamp? = null,
  val updatedAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class ScanRecord(
  val id: String = "",
  val localHistoryId: Long? = null,
  val userId: String = "",
  val barcodeValue: String = "",
  val barcodeFormat: String = "",
  val productName: String = "",
  val brand: String = "",
  val category: String = "",
  val description: String = "",
  val imageUrl: String = "",
  val quantity: String = "",
  val nutriscoreGrade: String = "",
  val sourceProvider: String = "",
  val verificationStatus: String = VerificationStatus.UNKNOWN.name,
  val scanResult: String = "MATCHED",
  val scannedAt: Timestamp? = null,
  val localTimestampMs: Long = System.currentTimeMillis(),
)

data class ScanContext(
  val barcodeValue: String,
  val barcodeFormat: String,
  val productName: String,
  val brand: String,
)

@Keep
@IgnoreExtraProperties
data class RewardWallet(
  val id: String = "summary",
  val userId: String = "",
  val unitCode: String = "PTS",
  val availablePoints: Long = 0L,
  val pendingPoints: Long = 0L,
  val redeemedPoints: Long = 0L,
  val reversedPoints: Long = 0L,
  val updatedAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class RewardTransaction(
  val id: String = "",
  val userId: String = "",
  val transactionType: String = TransactionType.RECEIPT_REWARD.name,
  val amount: Long = 0L,
  val unitCode: String = "PTS",
  val sourceActivity: String = "",
  val campaignId: String? = null,
  val status: String = TransactionStatus.PENDING.name,
  val idempotencyKey: String = "",
  val auditReference: String = "",
  val relatedTransactionId: String? = null,
  val notes: String? = null,
  val createdAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class AppNotification(
  val id: String = "",
  val userId: String = "",
  val category: String = "ACCOUNT",
  val title: String = "",
  val body: String = "",
  val read: Boolean = false,
  val referenceId: String? = null,
  val createdAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class ReceiptSubmission(
  val id: String = "",
  val userId: String = "",
  val storeName: String = "",
  val transactionDate: String = "",
  val currencyCode: String = "USD",
  val totalAmount: Double = 0.0,
  val receiptReference: String = "",
  val receiptReferenceHash: String = "",
  val campaignId: String? = null,
  val reviewStatus: String = ReceiptReviewStatus.PENDING.name,
  val rejectionReason: String? = null,
  val reviewedBy: String? = null,
  val submittedAt: Timestamp? = null,
  val updatedAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class Campaign(
  val id: String = "",
  val partnerId: String = "",
  val partnerName: String = "",
  val title: String = "",
  val terms: String = "",
  val rewardType: String = "SANDBOX_POINTS",
  val rewardPoints: Long = 0L,
  val maxPerUser: Int = 1,
  val budgetLimitPoints: Long = 1000L,
  val eligibleCountries: List<String> = emptyList(),
  val eligibleBarcodes: List<String> = emptyList(),
  val eligibleCategories: List<String> = emptyList(),
  val status: String = CampaignStatus.ACTIVE.name,
  val startAt: Timestamp? = null,
  val endAt: Timestamp? = null,
  val createdAt: Timestamp? = null,
  val updatedAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class CatalogProduct(
  val id: String = "",
  val barcode: String = "",
  val name: String = "",
  val brand: String = "",
  val category: String = "",
  val description: String = "",
  val imageUrl: String = "",
  val manufacturer: String = "",
  val countryOfOrigin: String = "",
  val dataProvider: String = "",
  val attributionText: String = "",
  val verificationStatus: String = VerificationStatus.OPEN_FOOD_FACTS.name,
  val submittedBy: String = "",
  val createdAt: Timestamp? = null,
  val updatedAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class ProductOffer(
  val id: String = "",
  val barcode: String = "",
  val storeName: String = "",
  val price: Double = 0.0,
  val currencyCode: String = "USD",
  val countryCode: String = "US",
  val availabilityStatus: String = OfferAvailability.UNVERIFIED.name,
  val verified: Boolean = false,
  val offerUrl: String = "",
  val submittedBy: String = "",
  val lastVerifiedAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class AdminRole(
  val id: String = "",
  val userId: String = "",
  val roleName: String = AdminRoleName.SUPER_ADMIN.name,
  val permissions: List<String> = emptyList(),
  val createdAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class AuditLog(
  val id: String = "",
  val actorId: String = "",
  val action: String = "",
  val entityType: String = "",
  val entityId: String = "",
  val reason: String = "",
  val timestamp: Timestamp? = null,
)

enum class PayoutMethod(val label: String) {
  GIFT_VOUCHER("Gift Voucher"),
  PAYPAL_SANDBOX("PayPal Sandbox"),
  BANK_SANDBOX("Bank Transfer Sandbox");

  companion object {
    fun fromString(raw: String?): PayoutMethod =
      entries.firstOrNull { it.name == raw } ?: GIFT_VOUCHER
  }
}

enum class PayoutStatus {
  PENDING,
  APPROVED,
  PAID,
  REJECTED;

  companion object {
    fun fromString(raw: String?): PayoutStatus =
      entries.firstOrNull { it.name == raw } ?: PENDING
  }
}

@Keep
@IgnoreExtraProperties
data class PayoutRequest(
  val id: String = "",
  val userId: String = "",
  val userDisplayName: String = "",
  val rewardTitle: String = "",
  val pointsAmount: Long = 100L,
  val payoutMethod: String = PayoutMethod.GIFT_VOUCHER.name,
  val destinationHandle: String = "",
  val status: String = PayoutStatus.PENDING.name,
  val adminNotes: String? = null,
  val reviewedBy: String? = null,
  val requestedAt: Timestamp? = null,
  val updatedAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class DeletionRequest(
  val id: String = "",
  val userId: String = "",
  val reason: String = "",
  val status: String = "REQUESTED",
  val requestedAt: Timestamp? = null,
)

data class AppPreferences(
  val onboardingCompleted: Boolean = false,
  val guestMode: Boolean = false,
  val countryCode: String = "US",
  val languageCode: String = "en",
  val marketingConsent: Boolean = false,
  val darkThemeEnabled: Boolean = false,
  val reducedMotionEnabled: Boolean = false,
)
