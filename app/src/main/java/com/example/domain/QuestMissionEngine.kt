package com.example.domain

import com.example.data.model.ReceiptSubmission
import com.example.data.model.RewardWallet
import com.example.data.model.ScanRecord

data class ShopperMission(
  val id: String,
  val title: String,
  val subtitle: String,
  val currentStep: Int,
  val targetStep: Int,
  val rewardHint: String,
) {
  val progressFraction: Float
    get() = if (targetStep <= 0) 0f else (currentStep.toFloat() / targetStep.toFloat()).coerceIn(0f, 1f)

  val isCompleted: Boolean
    get() = currentStep >= targetStep
}

object QuestMissionEngine {
  fun computeMissions(
    scans: List<ScanRecord>,
    receipts: List<ReceiptSubmission>,
    wallet: RewardWallet,
  ): List<ShopperMission> {
    val totalScans = scans.size
    val uniqueBarcodes = scans.map { it.barcodeValue }.distinct().size
    val submittedReceipts = receipts.size
    val pointsTowardVoucher = wallet.availablePoints.toInt().coerceIn(0, 100)

    return listOf(
      ShopperMission(
        id = "scan_cadence",
        title = "Scan Cadence",
        subtitle = "Log 3 retail barcodes or QR items",
        currentStep = totalScans.coerceAtMost(3),
        targetStep = 3,
        rewardHint = "Room + Cloud Sync",
      ),
      ShopperMission(
        id = "catalog_discovery",
        title = "Catalog Explorer",
        subtitle = "Inspect 5 distinct GTIN items",
        currentStep = uniqueBarcodes.coerceAtMost(5),
        targetStep = 5,
        rewardHint = "Offline Ready",
      ),
      ShopperMission(
        id = "receipt_check",
        title = "Receipt Verification",
        subtitle = "Submit 1 receipt for audit review",
        currentStep = submittedReceipts.coerceAtMost(1),
        targetStep = 1,
        rewardHint = "Eligible Bonus",
      ),
      ShopperMission(
        id = "voucher_threshold",
        title = "Voucher Readiness",
        subtitle = "Accumulate 100 PTS balance",
        currentStep = pointsTowardVoucher,
        targetStep = 100,
        rewardHint = "100 PTS Unlock",
      ),
    )
  }
}
