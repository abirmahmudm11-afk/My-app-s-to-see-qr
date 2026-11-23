package com.example.data.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class VerificationStatus(val label: String, val priority: Int) {
    VERIFIED_MERCHANT("Verified Merchant", 1),
    LICENSED_PROVIDER("Licensed Provider", 2),
    OPEN_FOOD_FACTS("Open Food Facts (ODbL)", 3),
    USER_SUBMITTED_UNVERIFIED("User Submitted (Unverified)", 4),
    UNKNOWN("Unattributed", 5);

    companion object {
        fun safeValueOf(raw: String?): VerificationStatus =
            entries.firstOrNull { it.name == raw } ?: UNKNOWN
    }
}

enum class CampaignStatus {
    DRAFT, REVIEW, APPROVED, ACTIVE, PAUSED, EXPIRED, ARCHIVED;

    companion object {
        fun safeValueOf(raw: String?): CampaignStatus =
            entries.firstOrNull { it.name == raw } ?: DRAFT
    }
}

enum class TransactionStatus {
    PENDING, APPROVED, CREDITED, REDEEMED, REJECTED, EXPIRED, REVERSED;

    companion object {
        fun safeValueOf(raw: String?): TransactionStatus =
            entries.firstOrNull { it.name == raw } ?: PENDING
    }
}

enum class TransactionType {
    CAMPAIGN_BONUS, RECEIPT_REWARD, REDEMPTION, REVERSAL;

    companion object {
        fun safeValueOf(raw: String?): TransactionType =
            entries.firstOrNull { it.name == raw } ?: RECEIPT_REWARD
    }
}

enum class ReceiptReviewStatus {
    PENDING, APPROVED, REJECTED;

    companion object {
        fun safeValueOf(raw: String?): ReceiptReviewStatus =
            entries.firstOrNull { it.name == raw } ?: PENDING
    }
}

enum class OfferAvailability(val label: String) {
    IN_STOCK("In Stock"),
    LIMITED("Limited Stock"),
    OUT_OF_STOCK("Out of Stock"),
    UNVERIFIED("Unverified");

    companion object {
        fun safeValueOf(raw: String?): OfferAvailability =
            entries.firstOrNull { it.name == raw } ?: UNVERIFIED
    }
}

enum class NotificationCategory(val label: String) {
    REWARD_STATUS("Reward Status"),
    CAMPAIGN_ANNOUNCEMENT("Campaign"),
    RECEIPT_REVIEW("Receipt Review"),
    SECURITY_ALERT("Security Alert"),
    ACCOUNT("Account");

    companion object {
        fun safeValueOf(raw: String?): NotificationCategory =
            entries.firstOrNull { it.name == raw } ?: ACCOUNT
    }
}

@Keep
@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val preferredLanguage: String = "en",
    val countryCode: String = "US",
    val currencyCode: String = "USD",
    val accountStatus: String = "ACTIVE",
    val marketingConsent: Boolean = false,
    val darkThemeEnabled: Boolean = false,
    val reducedMotionEnabled: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> = mapOf(
        "userId" to userId,
        "displayName" to displayName.take(80).ifBlank { "Shopper" },
        "preferredLanguage" to preferredLanguage,
        "countryCode" to countryCode,
        "currencyCode" to currencyCode,
        "accountStatus" to accountStatus,
        "marketingConsent" to marketingConsent,
        "darkThemeEnabled" to darkThemeEnabled,
        "reducedMotionEnabled" to reducedMotionEnabled,
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
    )
}

@Keep
@IgnoreExtraProperties
data class ScanRecord(
    @DocumentId val id: String = "",
    val userId: String = "",
    val barcodeValue: String = "",
    val barcodeFormat: String = "",
    val productName: String = "",
    val brand: String = "",
    val sourceProvider: String = "",
    val verificationStatus: String = VerificationStatus.UNKNOWN.name,
    val scanResult: String = "MATCHED",
    @ServerTimestamp val scannedAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> = mapOf(
        "userId" to userId,
        "barcodeValue" to barcodeValue.take(128),
        "barcodeFormat" to barcodeFormat.take(32),
        "productName" to productName.take(160),
        "brand" to brand.take(100),
        "sourceProvider" to sourceProvider.take(80).ifBlank { "None" },
        "verificationStatus" to verificationStatus,
        "scanResult" to scanResult,
        "scannedAt" to FieldValue.serverTimestamp()
    )
}

@Keep
@IgnoreExtraProperties
data class RewardWallet(
    @DocumentId val id: String = "",
    val userId: String = "",
    val unitCode: String = "PTS",
    val availablePoints: Long = 0L,
    val pendingPoints: Long = 0L,
    val redeemedPoints: Long = 0L,
    val reversedPoints: Long = 0L,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> = mapOf(
        "userId" to userId,
        "unitCode" to "PTS",
        "availablePoints" to 0L,
        "pendingPoints" to 0L,
        "redeemedPoints" to 0L,
        "reversedPoints" to 0L,
        "updatedAt" to FieldValue.serverTimestamp()
    )
}

@Keep
@IgnoreExtraProperties
data class RewardTransaction(
    @DocumentId val id: String = "",
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
    @ServerTimestamp val createdAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> = mapOf(
        "userId" to userId,
        "transactionType" to transactionType,
        "amount" to amount,
        "unitCode" to "PTS",
        "sourceActivity" to sourceActivity.take(120),
        "campaignId" to campaignId?.take(128),
        "status" to status,
        "idempotencyKey" to idempotencyKey.take(128),
        "auditReference" to auditReference.take(128),
        "relatedTransactionId" to relatedTransactionId?.take(128),
        "notes" to notes?.take(300),
        "createdAt" to FieldValue.serverTimestamp()
    ).filterValues { it != null }.mapValues { it.value!! }
}

@Keep
@IgnoreExtraProperties
data class AppNotification(
    @DocumentId val id: String = "",
    val userId: String = "",
    val category: String = NotificationCategory.ACCOUNT.name,
    val title: String = "",
    val body: String = "",
    val read: Boolean = false,
    val referenceId: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> = mapOf(
        "userId" to userId,
        "category" to category,
        "title" to title.take(120),
        "body" to body.take(500),
        "read" to read,
        "referenceId" to referenceId?.take(128),
        "createdAt" to FieldValue.serverTimestamp()
    ).filterValues { it != null }.mapValues { it.value!! }
}

@Keep
@IgnoreExtraProperties
data class ReceiptSubmission(
    @DocumentId val id: String = "",
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
    @ServerTimestamp val submittedAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> = mapOf(
        "userId" to userId,
        "storeName" to storeName.take(100),
        "transactionDate" to transactionDate.take(30),
        "currencyCode" to currencyCode.take(3).uppercase(),
        "totalAmount" to totalAmount,
        "receiptReference" to receiptReference.take(100),
        "receiptReferenceHash" to receiptReferenceHash.take(128),
        "campaignId" to campaignId?.take(128),
        "reviewStatus" to ReceiptReviewStatus.PENDING.name,
        "submittedAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
    ).filterValues { it != null }.mapValues { it.value!! }
}

@Keep
@IgnoreExtraProperties
data class Campaign(
    @DocumentId val id: String = "",
    val partnerId: String = "",
    val partnerName: String = "",
    val title: String = "",
    val terms: String = "",
    val rewardType: String = "SANDBOX_POINTS",
    val rewardPoints: Long = 0L,
    val maxPerUser: Long = 1L,
    val budgetLimitPoints: Long = 1000L,
    val eligibleCountries: List<String> = emptyList(),
    val eligibleBarcodes: List<String> = emptyList(),
    val eligibleCategories: List<String> = emptyList(),
    val status: String = CampaignStatus.DRAFT.name,
    val startAt: Timestamp? = null,
    val endAt: Timestamp? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> = mapOf(
        "partnerId" to partnerId.take(128),
        "partnerName" to partnerName.take(100),
        "title" to title.take(120),
        "terms" to terms.take(1000),
        "rewardType" to "SANDBOX_POINTS",
        "rewardPoints" to rewardPoints,
        "maxPerUser" to maxPerUser,
        "budgetLimitPoints" to budgetLimitPoints,
        "eligibleCountries" to eligibleCountries.take(30),
        "eligibleBarcodes" to eligibleBarcodes.take(50),
        "eligibleCategories" to eligibleCategories.take(20),
        "status" to status,
        "startAt" to (startAt ?: Timestamp.now()),
        "endAt" to (endAt ?: Timestamp(Timestamp.now().seconds + 30L * 86400L, 0)),
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
    )
}

@Keep
@IgnoreExtraProperties
data class CatalogProduct(
    @DocumentId val id: String = "",
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
    val submittedBy: String = "system",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> = mapOf(
        "barcode" to barcode.take(64),
        "name" to name.take(160),
        "brand" to brand.take(100),
        "category" to category.take(80),
        "description" to description.take(600),
        "imageUrl" to imageUrl.take(500),
        "manufacturer" to manufacturer.take(120),
        "countryOfOrigin" to countryOfOrigin.take(80),
        "dataProvider" to dataProvider.take(100),
        "attributionText" to attributionText.take(240),
        "verificationStatus" to verificationStatus,
        "submittedBy" to submittedBy.take(128),
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
    )
}

@Keep
@IgnoreExtraProperties
data class ProductOffer(
    @DocumentId val id: String = "",
    val barcode: String = "",
    val storeName: String = "",
    val price: Double = 0.0,
    val currencyCode: String = "USD",
    val countryCode: String = "US",
    val availabilityStatus: String = OfferAvailability.UNVERIFIED.name,
    val verified: Boolean = false,
    val offerUrl: String = "",
    val submittedBy: String = "",
    @ServerTimestamp val lastVerifiedAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> = mapOf(
        "barcode" to barcode.take(64),
        "storeName" to storeName.take(100),
        "price" to price,
        "currencyCode" to currencyCode.take(3).uppercase(),
        "countryCode" to countryCode.take(2).uppercase(),
        "availabilityStatus" to availabilityStatus,
        "verified" to verified,
        "offerUrl" to offerUrl.take(500),
        "submittedBy" to submittedBy.take(128),
        "lastVerifiedAt" to FieldValue.serverTimestamp()
    )
}

@Keep
@IgnoreExtraProperties
data class AdminRole(
    @DocumentId val id: String = "",
    val userId: String = "",
    val roleName: String = "RECEIPT_REVIEWER",
    val permissions: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class AuditLog(
    @DocumentId val id: String = "",
    val actorId: String = "",
    val action: String = "",
    val entityType: String = "",
    val entityId: String = "",
    val reason: String = "",
    @ServerTimestamp val timestamp: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> = mapOf(
        "actorId" to actorId.take(128),
        "action" to action.take(80),
        "entityType" to entityType.take(60),
        "entityId" to entityId.take(128),
        "reason" to reason.take(300),
        "timestamp" to FieldValue.serverTimestamp()
    )
}
