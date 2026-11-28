package com.example.domain

import com.example.data.model.Campaign
import com.example.data.model.CampaignStatus
import com.example.data.model.CatalogProduct
import com.example.data.model.RewardTransaction
import com.example.data.model.TransactionStatus

data class CampaignEligibilityResult(
  val campaign: Campaign,
  val eligible: Boolean,
  val reason: String,
)

object CampaignEligibilityEngine {
  fun evaluate(
    campaign: Campaign,
    userCountryCode: String,
    product: CatalogProduct?,
    userTransactions: List<RewardTransaction>,
    nowSeconds: Long = System.currentTimeMillis() / 1000L,
  ): CampaignEligibilityResult {
    if (CampaignStatus.fromString(campaign.status) != CampaignStatus.ACTIVE) {
      return CampaignEligibilityResult(campaign, false, "Campaign is not active (${campaign.status}).")
    }

    val startSec = campaign.startAt?.seconds
    val endSec = campaign.endAt?.seconds
    if (startSec != null && nowSeconds < startSec) {
      return CampaignEligibilityResult(campaign, false, "Campaign has not started yet.")
    }
    if (endSec != null && nowSeconds > endSec) {
      return CampaignEligibilityResult(campaign, false, "Campaign window has expired.")
    }

    val countryMatches = campaign.eligibleCountries.isEmpty() ||
      campaign.eligibleCountries.any {
        it.equals(userCountryCode, ignoreCase = true) || it.equals("GLOBAL", ignoreCase = true)
      }
    if (!countryMatches) {
      return CampaignEligibilityResult(
        campaign,
        false,
        "Available in ${campaign.eligibleCountries.joinToString(", ")} only.",
      )
    }

    val claimsCount = userTransactions.count { tx ->
      tx.campaignId == campaign.id &&
        TransactionStatus.fromString(tx.status) !in setOf(
          TransactionStatus.REJECTED,
          TransactionStatus.REVERSED,
          TransactionStatus.EXPIRED,
        )
    }
    if (claimsCount >= campaign.maxPerUser) {
      return CampaignEligibilityResult(
        campaign,
        false,
        "Per-user claim limit reached ($claimsCount / ${campaign.maxPerUser}).",
      )
    }

    if (product != null && campaign.eligibleBarcodes.isNotEmpty()) {
      val barcodeMatch = campaign.eligibleBarcodes.any { it.trim() == product.barcode.trim() }
      if (!barcodeMatch) {
        return CampaignEligibilityResult(campaign, false, "Product barcode is not in campaign list.")
      }
    }

    if (product != null && campaign.eligibleCategories.isNotEmpty()) {
      val categoryMatch = campaign.eligibleCategories.any { cat ->
        product.category.contains(cat, ignoreCase = true)
      }
      if (!categoryMatch) {
        return CampaignEligibilityResult(
          campaign,
          false,
          "Eligible categories: ${campaign.eligibleCategories.joinToString(", ")}.",
        )
      }
    }

    return CampaignEligibilityResult(
      campaign,
      true,
      "Eligible for +${campaign.rewardPoints} PTS upon verified receipt.",
    )
  }
}
