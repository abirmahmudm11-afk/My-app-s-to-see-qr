package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.PayoutMethod
import com.example.data.model.PayoutStatus
import com.example.data.model.RewardTransaction
import com.example.data.model.TransactionStatus
import com.example.domain.MarketConfig
import com.example.ui.components.GuestModeBanner
import com.example.ui.components.ReceiptStatusBadge
import com.example.ui.components.SandboxCashbackBanner
import com.example.ui.components.SectionHeader
import com.example.ui.components.formatTimestamp
import com.example.ui.theme.AmberNotice
import com.example.ui.theme.AmberSoftTint
import com.example.ui.theme.CobaltInfo
import com.example.ui.theme.CobaltSoftTint
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.CrimsonSoftTint
import com.example.ui.theme.DeepObsidian
import com.example.ui.theme.ElectricLime
import com.example.ui.theme.EmeraldSoftTint
import com.example.ui.theme.EmeraldTrust
import com.example.ui.theme.ObsidianElevated
import com.example.ui.theme.ObsidianMutedText
import com.example.ui.theme.ObsidianOnLime
import com.example.ui.viewmodel.MainUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class RewardTierSpec(
  val title: String,
  val thresholdPts: Long,
)

@Composable
fun RewardsScreen(
  uiState: MainUiState,
  onSubmitReceipt: (String?) -> Unit,
  onRedeemVoucher: (Long, String) -> Unit,
  onRequestPayout: (Long, String, PayoutMethod, String) -> Unit,
  onOpenTransaction: (String) -> Unit,
  onSignInRequested: () -> Unit,
) {
  val vouchers = remember {
    listOf(
      Triple("Grocery Sandbox Voucher", 100L, "$0 redemption fee • Instant payout ticket"),
      Triple("Coffee Partner Credit", 250L, "$0 redemption fee • Auditable sandbox pass"),
      Triple("Supermarket Bonus Tier", 500L, "$0 redemption fee • Priority sandbox reward"),
    )
  }

  var showPayoutDialog by remember { mutableStateOf(false) }
  var payoutPointsInput by remember { mutableStateOf("100") }
  var payoutTitleInput by remember { mutableStateOf("Cashout Payout") }
  var payoutHandleInput by remember { mutableStateOf(uiState.firebaseUser?.email ?: "") }
  var selectedMethod by remember { mutableStateOf(PayoutMethod.PAYPAL_SANDBOX) }
  val isSignedIn = uiState.firebaseUser != null

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    item {
      Text("Rewards & Payouts", style = MaterialTheme.typography.headlineMedium)
      Spacer(Modifier.height(6.dp))
      SandboxCashbackBanner()
    }

    if (!isSignedIn) {
      item {
        GuestModeBanner(onSignInClick = onSignInRequested)
      }
    }

    // Visual Radial Progress Ring toward Next Tier / Reward Threshold
    item {
      RewardTierProgressGauge(
        availablePoints = uiState.wallet.availablePoints,
        reducedMotion = uiState.preferences.reducedMotionEnabled,
      )
    }

    // Obsidian Wallet Summary Card with Payout Trigger
    item {
      Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = DeepObsidian),
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          Text("WALLET LEDGER SUMMARY", style = MaterialTheme.typography.labelSmall, color = ElectricLime)
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Column {
              Text("${uiState.wallet.availablePoints} PTS", style = MaterialTheme.typography.displayMedium, color = ElectricLime)
              Text("Available balance", style = MaterialTheme.typography.bodySmall, color = ObsidianMutedText)
            }
            Column(horizontalAlignment = Alignment.End) {
              Text("${uiState.wallet.pendingPoints} PTS", style = MaterialTheme.typography.titleLarge, color = Color.White)
              Text("Pending review", style = MaterialTheme.typography.bodySmall, color = ObsidianMutedText)
            }
          }
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Surface(modifier = Modifier.weight(1f), color = ObsidianElevated, shape = RoundedCornerShape(10.dp)) {
              Column(Modifier.padding(10.dp)) {
                Text("${uiState.wallet.redeemedPoints} PTS", style = MaterialTheme.typography.titleMedium, color = EmeraldSoftTint)
                Text("Redeemed", style = MaterialTheme.typography.labelSmall, color = ObsidianMutedText)
              }
            }
            Surface(modifier = Modifier.weight(1f), color = ObsidianElevated, shape = RoundedCornerShape(10.dp)) {
              Column(Modifier.padding(10.dp)) {
                Text("100 PTS", style = MaterialTheme.typography.titleMedium, color = ElectricLime)
                Text("Payout minimum ($0 fee)", style = MaterialTheme.typography.labelSmall, color = ObsidianMutedText)
              }
            }
          }
          Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
              onClick = { onSubmitReceipt(null) },
              modifier = Modifier
                .weight(1f)
                .testTag("rewards_log_receipt_button"),
              shape = RoundedCornerShape(14.dp),
              colors = ButtonDefaults.buttonColors(containerColor = ElectricLime, contentColor = ObsidianOnLime),
            ) {
              Icon(Icons.AutoMirrored.Rounded.ReceiptLong, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(Modifier.width(6.dp))
              Text("Log Receipt")
            }
            OutlinedButton(
              onClick = {
                if (isSignedIn) {
                  showPayoutDialog = true
                } else {
                  onSignInRequested()
                }
              },
              modifier = Modifier
                .weight(1f)
                .testTag("rewards_request_payout_button"),
              shape = RoundedCornerShape(14.dp),
              border = BorderStroke(1.dp, ElectricLime.copy(alpha = 0.55f)),
            ) {
              Icon(Icons.Rounded.AccountBalance, contentDescription = null, tint = ElectricLime, modifier = Modifier.size(18.dp))
              Spacer(Modifier.width(6.dp))
              Text(if (isSignedIn) "Request Payout" else "Sign In to Claim", color = ElectricLime)
            }
          }
        }
      }
    }

    // Quick Vouchers
    item { SectionHeader("Redeem Sandbox Vouchers") }
    items(vouchers) { (title, cost, subtitle) ->
      val hasBalance = uiState.wallet.availablePoints >= cost
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Icon(Icons.Rounded.CardGiftcard, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Column {
              Text(title, style = MaterialTheme.typography.titleMedium)
              Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }
          Spacer(Modifier.width(8.dp))
          Button(
            onClick = {
              if (!isSignedIn) onSignInRequested() else onRedeemVoucher(cost, title)
            },
            enabled = !isSignedIn || hasBalance,
            shape = RoundedCornerShape(12.dp),
          ) {
            Text(if (!isSignedIn) "Sign In" else "$cost PTS")
          }
        }
      }
    }

    // Payout Requests Queue
    item { SectionHeader("My Payout Requests (${uiState.payoutRequests.size})") }
    if (uiState.payoutRequests.isEmpty()) {
      item {
        Text(
          "No payout requests yet. Once you reach 100 PTS, submit a payout or voucher request for admin fulfillment.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    } else {
      items(uiState.payoutRequests, key = { it.id }) { req ->
        val pStatus = PayoutStatus.fromString(req.status)
        val (bg, fg) = when (pStatus) {
          PayoutStatus.PAID, PayoutStatus.APPROVED -> EmeraldSoftTint to EmeraldTrust
          PayoutStatus.REJECTED -> CrimsonSoftTint to CrimsonAlert
          PayoutStatus.PENDING -> AmberSoftTint to AmberNotice
        }
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        ) {
          Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text("${req.rewardTitle} (${req.pointsAmount} PTS)", style = MaterialTheme.typography.titleMedium)
              Surface(color = bg, shape = RoundedCornerShape(8.dp)) {
                Text(
                  text = pStatus.name,
                  style = MaterialTheme.typography.labelSmall,
                  color = fg,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                )
              }
            }
            Text(
              "${PayoutMethod.fromString(req.payoutMethod).label} • ${req.destinationHandle}",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            req.adminNotes?.takeIf { it.isNotBlank() }?.let { note ->
              Text("Admin note: $note", style = MaterialTheme.typography.bodySmall, color = CobaltInfo)
            }
          }
        }
      }
    }

    // Submitted Receipts Queue
    item { SectionHeader("Receipt Submissions (${uiState.receipts.size})") }
    if (uiState.receipts.isEmpty()) {
      item {
        Text(
          "No receipts submitted yet. Every receipt is SHA-256 hashed to prevent duplicate claims.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    } else {
      items(uiState.receipts, key = { it.id }) { receipt ->
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        ) {
          Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(receipt.storeName, style = MaterialTheme.typography.titleMedium)
              ReceiptStatusBadge(statusRaw = receipt.reviewStatus)
            }
            Text(
              "${MarketConfig.formatMoney(receipt.totalAmount, receipt.currencyCode)} • ${receipt.transactionDate} • Ref ${receipt.receiptReference}",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            receipt.rejectionReason?.takeIf { it.isNotBlank() }?.let { reason ->
              Text("Note: $reason", style = MaterialTheme.typography.bodySmall, color = CrimsonAlert)
            }
          }
        }
      }
    }

    // Point Ledger History
    item { SectionHeader("Point Ledger (${uiState.transactions.size})") }
    if (uiState.transactions.isEmpty()) {
      item {
        Text(
          "Ledger transactions will appear once a receipt or payout is processed.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    } else {
      items(uiState.transactions, key = { it.id }) { tx ->
        TransactionRowCard(tx = tx, onClick = { onOpenTransaction(tx.id) })
      }
    }

    item { Spacer(Modifier.height(16.dp)) }
  }

  if (showPayoutDialog) {
    AlertDialog(
      onDismissRequest = { showPayoutDialog = false },
      title = { Text("Request Reward Payout") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            "Available: ${uiState.wallet.availablePoints} PTS • Minimum: 100 PTS",
            style = MaterialTheme.typography.bodySmall,
          )
          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            PayoutMethod.entries.forEach { method ->
              val selected = selectedMethod == method
              Surface(
                modifier = Modifier
                  .weight(1f)
                  .clickable { selectedMethod = method },
                color = if (selected) DeepObsidian else MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(10.dp),
              ) {
                Text(
                  text = method.label.substringBefore(" "),
                  style = MaterialTheme.typography.labelSmall,
                  color = if (selected) ElectricLime else MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                )
              }
            }
          }
          OutlinedTextField(
            value = payoutPointsInput,
            onValueChange = { payoutPointsInput = it },
            label = { Text("Points to Redeem (>= 100)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
          )
          OutlinedTextField(
            value = payoutTitleInput,
            onValueChange = { payoutTitleInput = it },
            label = { Text("Reward / Payout Label") },
            modifier = Modifier.fillMaxWidth(),
          )
          OutlinedTextField(
            value = payoutHandleInput,
            onValueChange = { payoutHandleInput = it },
            label = { Text("Destination Email / Account Handle") },
            modifier = Modifier.fillMaxWidth(),
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val pts = payoutPointsInput.toLongOrNull() ?: 100L
            showPayoutDialog = false
            onRequestPayout(pts, payoutTitleInput, selectedMethod, payoutHandleInput)
          },
        ) {
          Text("Submit Request")
        }
      },
      dismissButton = {
        TextButton(onClick = { showPayoutDialog = false }) { Text("Cancel") }
      },
    )
  }
}

@Composable
private fun RewardTierProgressGauge(
  availablePoints: Long,
  reducedMotion: Boolean,
) {
  val tiers = remember {
    listOf(
      RewardTierSpec("Grocery Voucher", 100L),
      RewardTierSpec("Partner Credit", 250L),
      RewardTierSpec("Supermarket Bonus", 500L),
      RewardTierSpec("Gold Ledger Tier", 1000L),
    )
  }
  val nextTier = tiers.firstOrNull { availablePoints < it.thresholdPts } ?: tiers.last()
  val prevThreshold = tiers.lastOrNull { availablePoints >= it.thresholdPts }?.thresholdPts ?: 0L
  val rawProgress = if (availablePoints >= nextTier.thresholdPts) {
    1f
  } else {
    val span = (nextTier.thresholdPts - prevThreshold).coerceAtLeast(1L)
    ((availablePoints - prevThreshold).toFloat() / span.toFloat()).coerceIn(0f, 1f)
  }

  val animatedProgress by animateFloatAsState(
    targetValue = rawProgress,
    animationSpec = tween(
      durationMillis = if (reducedMotion) 0 else 750,
      easing = FastOutSlowInEasing,
    ),
    label = "tierGaugeProgress",
  )

  val remaining = (nextTier.thresholdPts - availablePoints).coerceAtLeast(0L)
  val percentLabel = "${(rawProgress * 100f).toInt()}%"

  Card(
    shape = RoundedCornerShape(22.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(18.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column {
          Text("NEXT REWARD THRESHOLD", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
          Text(nextTier.title, style = MaterialTheme.typography.titleLarge)
        }
        Surface(color = CobaltSoftTint, shape = RoundedCornerShape(8.dp)) {
          Text(
            text = "${nextTier.thresholdPts} PTS Goal",
            style = MaterialTheme.typography.labelSmall,
            color = CobaltInfo,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
          )
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(18.dp),
      ) {
        Box(
          modifier = Modifier.size(124.dp),
          contentAlignment = Alignment.Center,
        ) {
          val trackColor = MaterialTheme.colorScheme.surfaceVariant
          val primaryTeal = MaterialTheme.colorScheme.primary
          Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = 12.dp.toPx()
            val inset = strokePx / 2f
            val arcSize = Size(size.width - strokePx, size.height - strokePx)
            val startAngle = 135f
            val totalSweep = 270f

            drawArc(
              color = trackColor,
              startAngle = startAngle,
              sweepAngle = totalSweep,
              useCenter = false,
              topLeft = Offset(inset, inset),
              size = arcSize,
              style = Stroke(width = strokePx, cap = StrokeCap.Round),
            )

            drawArc(
              brush = Brush.sweepGradient(listOf(primaryTeal, ElectricLime, primaryTeal)),
              startAngle = startAngle,
              sweepAngle = totalSweep * animatedProgress.coerceAtLeast(0.02f),
              useCenter = false,
              topLeft = Offset(inset, inset),
              size = arcSize,
              style = Stroke(width = strokePx, cap = StrokeCap.Round),
            )
          }

          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(percentLabel, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
            Text("${availablePoints}p", style = MaterialTheme.typography.labelSmall)
          }
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text(
            text = if (remaining == 0L) "Threshold unlocked! Ready to redeem." else "$remaining PTS away from ${nextTier.title}",
            style = MaterialTheme.typography.titleMedium,
          )
          Text(
            text = "Milestones: 100 • 250 • 500 • 1000 PTS",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            tiers.forEach { tier ->
              val reached = availablePoints >= tier.thresholdPts
              Surface(
                color = if (reached) EmeraldSoftTint else MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(6.dp),
              ) {
                Text(
                  text = "${tier.thresholdPts}",
                  style = MaterialTheme.typography.labelSmall,
                  color = if (reached) EmeraldTrust else MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun TransactionRowCard(tx: RewardTransaction, onClick: () -> Unit) {
  val status = TransactionStatus.fromString(tx.status)
  val isCredit = tx.transactionType == "RECEIPT_REWARD" || tx.transactionType == "CAMPAIGN_BONUS"
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
  ) {
    Row(
      modifier = Modifier.padding(14.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(tx.sourceActivity, style = MaterialTheme.typography.titleMedium)
        Text(
          "${status.name} • ${tx.auditReference} • ${formatTimestamp(tx.createdAt)}",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
      Text(
        text = "${if (isCredit) "+" else "-"}${tx.amount} PTS",
        style = MaterialTheme.typography.titleMedium,
        color = if (isCredit) EmeraldTrust else CrimsonAlert,
      )
    }
  }
}

@Composable
fun ReceiptSubmissionScreen(
  uiState: MainUiState,
  preselectedCampaignId: String?,
  onBack: () -> Unit,
  onSignInRequested: () -> Unit,
  onSubmitReceipt: (
    storeName: String,
    transactionDate: String,
    currencyCode: String,
    totalAmount: Double,
    receiptReference: String,
    campaignId: String?,
    onSuccess: () -> Unit,
  ) -> Unit,
) {
  val market = MarketConfig.findByCountry(uiState.preferences.countryCode)
  val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }
  val isSignedIn = uiState.firebaseUser != null

  var storeName by remember { mutableStateOf("") }
  var transactionDate by remember { mutableStateOf(todayStr) }
  var totalAmountText by remember { mutableStateOf("") }
  var receiptReference by remember { mutableStateOf("") }
  var selectedCampaignId by remember { mutableStateOf(preselectedCampaignId) }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    item {
      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) {
          Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
        }
        Text("Submit Receipt (1–3 min)", style = MaterialTheme.typography.titleLarge)
      }
    }

    item {
      Surface(
        color = EmeraldSoftTint,
        shape = RoundedCornerShape(14.dp),
      ) {
        Row(
          modifier = Modifier.padding(12.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Icon(Icons.Rounded.Shield, contentDescription = null, tint = EmeraldTrust)
          Text(
            "Every receipt generates a SHA-256 fingerprint from Store + Date + Total + Reference to prevent duplicate submissions.",
            style = MaterialTheme.typography.bodySmall,
            color = DeepObsidian,
          )
        }
      }
    }

    if (!isSignedIn) {
      item {
        GuestModeBanner(onSignInClick = onSignInRequested)
      }
    }

    item {
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
          OutlinedTextField(
            value = storeName,
            onValueChange = { storeName = it },
            label = { Text("Store Name *") },
            placeholder = { Text("e.g., FreshMart Downtown") },
            modifier = Modifier.fillMaxWidth(),
          )
          OutlinedTextField(
            value = transactionDate,
            onValueChange = { transactionDate = it },
            label = { Text("Transaction Date (YYYY-MM-DD) *") },
            modifier = Modifier.fillMaxWidth(),
          )
          OutlinedTextField(
            value = totalAmountText,
            onValueChange = { totalAmountText = it },
            label = { Text("Receipt Total (${market.currencyCode}) *") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
          )
          OutlinedTextField(
            value = receiptReference,
            onValueChange = { receiptReference = it },
            label = { Text("Receipt / Invoice Reference *") },
            placeholder = { Text("e.g., INV-88421") },
            modifier = Modifier.fillMaxWidth(),
          )

          if (uiState.campaigns.isNotEmpty()) {
            Text("Optional Partner Campaign:", style = MaterialTheme.typography.labelLarge)
            uiState.campaigns.forEach { camp ->
              val selected = selectedCampaignId == camp.id
              Surface(
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable { selectedCampaignId = if (selected) null else camp.id },
                color = if (selected) EmeraldSoftTint else MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(
                  1.dp,
                  if (selected) EmeraldTrust else MaterialTheme.colorScheme.outline,
                ),
              ) {
                Row(
                  modifier = Modifier.padding(12.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Text(camp.title, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                  Text("+${camp.rewardPoints} PTS", style = MaterialTheme.typography.labelMedium, color = EmeraldTrust)
                }
              }
            }
          }

          Button(
            onClick = {
              if (!isSignedIn) {
                onSignInRequested()
              } else {
                onSubmitReceipt(
                  storeName,
                  transactionDate,
                  market.currencyCode,
                  totalAmountText.toDoubleOrNull() ?: 0.0,
                  receiptReference,
                  selectedCampaignId,
                  onBack,
                )
              }
            },
            modifier = Modifier
              .fillMaxWidth()
              .height(52.dp)
              .testTag("submit_receipt_button"),
            shape = RoundedCornerShape(14.dp),
          ) {
            Text(if (!isSignedIn) "Sign In with Google to Submit" else "Submit for Review")
          }
        }
      }
    }
  }
}

@Composable
fun TransactionDetailScreen(
  txId: String,
  uiState: MainUiState,
  onBack: () -> Unit,
) {
  val tx = uiState.transactions.firstOrNull { it.id == txId }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      IconButton(onClick = onBack) {
        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
      }
      Text("Ledger Audit Detail", style = MaterialTheme.typography.titleLarge)
    }

    if (tx == null) {
      Text("Transaction not found in your wallet ledger.", style = MaterialTheme.typography.bodyMedium)
    } else {
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
      ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(tx.sourceActivity, style = MaterialTheme.typography.headlineMedium)
          Text("${tx.amount} ${tx.unitCode}", style = MaterialTheme.typography.displayMedium, color = EmeraldTrust)
          LedgerLine("Type", tx.transactionType)
          LedgerLine("Status", tx.status)
          LedgerLine("Audit Ref", tx.auditReference)
          LedgerLine("Idempotency Key", tx.idempotencyKey)
          LedgerLine("Recorded", formatTimestamp(tx.createdAt))
          tx.notes?.takeIf { it.isNotBlank() }?.let { LedgerLine("Notes", it) }
        }
      }
    }
  }
}

@Composable
private fun LedgerLine(label: String, value: String) {
  Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
    Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Text(value, style = MaterialTheme.typography.labelMedium)
  }
}
