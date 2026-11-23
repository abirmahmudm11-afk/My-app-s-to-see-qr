package com.example.domain

import com.example.data.model.Campaign
import com.example.data.model.CampaignStatus
import com.example.data.model.CatalogProduct
import com.example.data.model.RewardTransaction
import com.example.data.model.TransactionStatus
import com.google.firebase.Timestamp

data class EligibilityResult(
    val campaign: Campaign,
    val eligible: Boolean,
    val reason: String
)

object CampaignEligibilityEngine {

    fun evaluate(
        campaign: Campaign,
        countryCode: String,
        product: CatalogProduct?,
        userTransactions: List<RewardTransaction>,
        nowSeconds: Long = Timestamp.now().seconds
    ): EligibilityResult {
        val status = CampaignStatus.safeValueOf(campaign.status)
        if (status != CampaignStatus.ACTIVE) {
            return EligibilityResult(campaign, false, "Campaign is ${status.name.lowercase()}.")
        }
        val start = campaign.startAt?.seconds ?: 0L
        val end = campaign.endAt?.seconds ?: Long.MAX_VALUE
        if (nowSeconds < start) {
            return EligibilityResult(campaign, false, "Campaign has not started yet.")
        }
        if (nowSeconds > end) {
            return EligibilityResult(campaign, false, "Campaign has expired.")
        }

        val countryMatch = campaign.eligibleCountries.any {
            it.equals("GLOBAL", ignoreCase = true) || it.equals(countryCode, ignoreCase = true)
        }
        if (!countryMatch) {
            return EligibilityResult(campaign, false, "Not available in $countryCode.")
        }

        val userClaimCount = userTransactions.count { tx ->
            tx.campaignId == campaign.id &&
                TransactionStatus.safeValueOf(tx.status) in setOf(
                    TransactionStatus.PENDING,
                    TransactionStatus.APPROVED,
                    TransactionStatus.CREDITED,
                    TransactionStatus.REDEEMED
                )
        }
        if (userClaimCount >= campaign.maxPerUser) {
            return EligibilityResult(
                campaign,
                false,
                "Per-user cap reached ($userClaimCount/${campaign.maxPerUser})."
            )
        }

        if (product != null) {
            val hasBarcodeFilter = campaign.eligibleBarcodes.isNotEmpty()
            val hasCategoryFilter = campaign.eligibleCategories.isNotEmpty()
            if (hasBarcodeFilter || hasCategoryFilter) {
                val barcodeMatches = campaign.eligibleBarcodes.any { it == product.barcode }
                val categoryMatches = campaign.eligibleCategories.any { cat ->
                    product.category.contains(cat, ignoreCase = true)
                }
                if (!barcodeMatches && !categoryMatches) {
                    return EligibilityResult(
                        campaign,
                        false,
                        "Product barcode or category does not match campaign rules."
                    )
                }
            }
        }

        return EligibilityResult(
            campaign,
            true,
            "Eligible for +${campaign.rewardPoints} sandbox pts upon verified receipt review."
        )
    }
}
