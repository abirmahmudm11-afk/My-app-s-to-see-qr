package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.material.icons.rounded.Stars
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.PayoutMethod
import com.example.data.model.PayoutStatus
import com.example.data.model.RewardTransaction
import com.example.data.model.TransactionStatus
import com.example.domain.MarketConfig
import com.example.ui.components.AnimatedButton
import com.example.ui.components.AnimatedIconButton
import com.example.ui.components.AnimatedOutlinedButton
import com.example.ui.components.AnimatedTextButton
import com.example.ui.components.GuestModeBanner
import com.example.ui.components.ReceiptStatusBadge
import com.example.ui.components.SandboxCashbackBanner
import com.example.ui.components.SectionHeader
import com.example.ui.components.SlidingSegmentedPillBar
import com.example.ui.components.atmosphericBackdrop
import com.example.ui.components.bounceClickable
import com.example.ui.components.formatTimestamp
import com.example.ui.theme.HeroMintVaultBrush
import com.example.ui.theme.LedgerDeckBrush
import com.example.ui.theme.LedgerJadeDark
import com.example.ui.theme.MintGlowBorderBrush
import com.example.ui.theme.MintOnDarkText
import com.example.ui.theme.ReticleAmber
import com.example.ui.theme.SignalIndigo
import com.example.ui.theme.SignalIndigoDark
import com.example.ui.theme.TabularDataStyle
import com.example.ui.theme.TabularFigureLargeStyle
import com.example.ui.theme.ViewfinderMuted
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
      Triple("Grocery Sandbox Voucher", 100L, "$0 redemption fee · Instant payout ticket"),
      Triple("Coffee Partner Credit", 250L, "$0 redemption fee · Auditable sandbox pass"),
      Triple("Supermarket Bonus Tier", 500L, "$0 redemption fee · Priority sandbox reward"),
    )
  }

  var showPayoutDialog by remember { mutableStateOf(false) }
  var payoutPointsInput by remember { mutableStateOf("100") }
  var payoutTitleInput by remember { mutableStateOf("Cashout Payout") }
  var payoutHandleInput by remember { mutableStateOf(uiState.firebaseUser?.email ?: "") }
  var selectedMethod by remember { mutableStateOf(PayoutMethod.PAYPAL_SANDBOX) }
  val isSignedIn = uiState.firebaseUser != null

  BoxWithConstraints(
    modifier = Modifier
      .fillMaxSize()
      .atmosphericBackdrop(),
    contentAlignment = Alignment.TopCenter,
  ) {
    val isWide = maxWidth >= 720.dp

    LazyColumn(
      modifier = Modifier
        .widthIn(max = 860.dp)
        .fillMaxSize(),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
          Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("Rewards & Ledger", style = MaterialTheme.typography.displayMedium)
            Text(
              text = "Earn PTS from verified receipts and redeem fee-free vouchers.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
          Image(
            painter = painterResource(id = R.drawable.img_wallet_trophy),
            contentDescription = "Rewards wallet",
            modifier = Modifier
              .size(52.dp)
              .clip(RoundedCornerShape(14.dp)),
            contentScale = ContentScale.Crop,
          )
        }
      }

      item {
        SandboxCashbackBanner()
      }

      if (!isSignedIn) {
        item {
          GuestModeBanner(onSignInClick = onSignInRequested)
        }
      }

      if (isWide) {
        item {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top,
          ) {
            Box(modifier = Modifier.weight(1f)) {
              RewardTierProgressGauge(
                availablePoints = uiState.wallet.availablePoints,
                reducedMotion = uiState.preferences.reducedMotionEnabled,
              )
            }
            Box(modifier = Modifier.weight(1.25f)) {
              Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.Transparent,
                border = BorderStroke(0.9.dp, MintGlowBorderBrush),
              ) {
                Column(
                  modifier = Modifier
                    .fillMaxWidth()
                    .background(HeroMintVaultBrush)
                    .padding(20.dp),
                  verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                  ) {
                    Column {
                      Text("AVAILABLE BALANCE", style = MaterialTheme.typography.labelSmall, color = ViewfinderMuted)
                      Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("${uiState.wallet.availablePoints}", style = TabularFigureLargeStyle, color = Color.White)
                        Text("PTS", style = MaterialTheme.typography.titleMedium, color = SignalIndigoDark, modifier = Modifier.padding(bottom = 5.dp))
                      }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                      Text("${uiState.wallet.pendingPoints} PTS", style = TabularDataStyle.copy(fontWeight = FontWeight.Bold), color = ReticleAmber)
                      Text("Pending review", style = MaterialTheme.typography.labelSmall, color = ViewfinderMuted)
                    }
                  }

                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                  ) {
                    Surface(
                      modifier = Modifier.weight(1f),
                      color = Color.White.copy(alpha = 0.08f),
                      shape = RoundedCornerShape(12.dp),
                    ) {
                      Column(Modifier.padding(11.dp)) {
                        Text("${uiState.wallet.redeemedPoints} PTS", style = TabularDataStyle.copy(fontWeight = FontWeight.Bold), color = Color.White)
                        Text("Redeemed", style = MaterialTheme.typography.labelSmall, color = ViewfinderMuted)
                      }
                    }
                    Surface(
                      modifier = Modifier.weight(1f),
                      color = Color.White.copy(alpha = 0.08f),
                      shape = RoundedCornerShape(12.dp),
                    ) {
                      Column(Modifier.padding(11.dp)) {
                        Text("100 PTS", style = TabularDataStyle.copy(fontWeight = FontWeight.Bold), color = SignalIndigoDark)
                        Text("Minimum cashout ($0 fee)", style = MaterialTheme.typography.labelSmall, color = ViewfinderMuted)
                      }
                    }
                  }

                  Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AnimatedButton(
                      onClick = { onSubmitReceipt(null) },
                      modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("rewards_log_receipt_button"),
                      shape = RoundedCornerShape(12.dp),
                      colors = ButtonDefaults.buttonColors(containerColor = SignalIndigoDark, contentColor = MintOnDarkText),
                    ) {
                      Icon(Icons.AutoMirrored.Rounded.ReceiptLong, contentDescription = null, modifier = Modifier.size(18.dp))
                      Spacer(Modifier.width(6.dp))
                      Text("Log Receipt", fontWeight = FontWeight.Bold)
                    }
                    AnimatedOutlinedButton(
                      onClick = {
                        if (isSignedIn) showPayoutDialog = true else onSignInRequested()
                      },
                      modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("rewards_request_payout_button"),
                      shape = RoundedCornerShape(12.dp),
                      border = BorderStroke(1.dp, Color.White.copy(alpha = 0.28f)),
                      colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White.copy(alpha = 0.08f),
                        contentColor = Color.White,
                      ),
                    ) {
                      Icon(Icons.Rounded.AccountBalance, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                      Spacer(Modifier.width(6.dp))
                      Text(if (isSignedIn) "Request Payout" else "Sign In", color = Color.White)
                    }
                  }
                }
              }
            }
          }
        }
      } else {
        item {
          RewardTierProgressGauge(
            availablePoints = uiState.wallet.availablePoints,
            reducedMotion = uiState.preferences.reducedMotionEnabled,
          )
        }

        // Authoritative Wallet Summary Deck
        item {
          Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.Transparent,
            border = BorderStroke(0.9.dp, MintGlowBorderBrush),
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .background(HeroMintVaultBrush)
                .padding(20.dp),
              verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
              ) {
                Column {
                  Text("AVAILABLE BALANCE", style = MaterialTheme.typography.labelSmall, color = ViewfinderMuted)
                  Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("${uiState.wallet.availablePoints}", style = TabularFigureLargeStyle, color = Color.White)
                    Text("PTS", style = MaterialTheme.typography.titleMedium, color = SignalIndigoDark, modifier = Modifier.padding(bottom = 5.dp))
                  }
                }
                Column(horizontalAlignment = Alignment.End) {
                  Text("${uiState.wallet.pendingPoints} PTS", style = TabularDataStyle.copy(fontWeight = FontWeight.Bold), color = ReticleAmber)
                  Text("Pending review", style = MaterialTheme.typography.labelSmall, color = ViewfinderMuted)
                }
              }

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
              ) {
                Surface(
                  modifier = Modifier.weight(1f),
                  color = Color.White.copy(alpha = 0.08f),
                  shape = RoundedCornerShape(12.dp),
                ) {
                  Column(Modifier.padding(11.dp)) {
                    Text("${uiState.wallet.redeemedPoints} PTS", style = TabularDataStyle.copy(fontWeight = FontWeight.Bold), color = Color.White)
                    Text("Redeemed", style = MaterialTheme.typography.labelSmall, color = ViewfinderMuted)
                  }
                }
                Surface(
                  modifier = Modifier.weight(1f),
                  color = Color.White.copy(alpha = 0.08f),
                  shape = RoundedCornerShape(12.dp),
                ) {
                  Column(Modifier.padding(11.dp)) {
                    Text("100 PTS", style = TabularDataStyle.copy(fontWeight = FontWeight.Bold), color = SignalIndigoDark)
                    Text("Minimum cashout ($0 fee)", style = MaterialTheme.typography.labelSmall, color = ViewfinderMuted)
                  }
                }
              }

              Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AnimatedButton(
                  onClick = { onSubmitReceipt(null) },
                  modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .testTag("rewards_log_receipt_button"),
                  shape = RoundedCornerShape(12.dp),
                  colors = ButtonDefaults.buttonColors(containerColor = SignalIndigoDark, contentColor = MintOnDarkText),
                ) {
                  Icon(Icons.AutoMirrored.Rounded.ReceiptLong, contentDescription = null, modifier = Modifier.size(18.dp))
                  Spacer(Modifier.width(6.dp))
                  Text("Log Receipt", fontWeight = FontWeight.Bold)
                }
                AnimatedOutlinedButton(
                  onClick = {
                    if (isSignedIn) showPayoutDialog = true else onSignInRequested()
                  },
                  modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .testTag("rewards_request_payout_button"),
                  shape = RoundedCornerShape(12.dp),
                  border = BorderStroke(1.dp, Color.White.copy(alpha = 0.28f)),
                  colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White.copy(alpha = 0.08f),
                    contentColor = Color.White,
                  ),
                ) {
                  Icon(Icons.Rounded.AccountBalance, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                  Spacer(Modifier.width(6.dp))
                  Text(if (isSignedIn) "Request Payout" else "Sign In", color = Color.White)
                }
              }
            }
          }
        }
      }

      item { SectionHeader("Redeem Vouchers") }
      if (isWide) {
        items(vouchers.chunked(2)) { pair ->
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            pair.forEach { (title, cost, subtitle) ->
              val hasBalance = uiState.wallet.availablePoints >= cost
              Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
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
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                  ) {
                    Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(8.dp)) {
                      Icon(
                        Icons.Rounded.CardGiftcard,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(9.dp).size(20.dp),
                      )
                    }
                    Column {
                      Text(title, style = MaterialTheme.typography.titleMedium)
                      Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                  }
                  Spacer(Modifier.width(8.dp))
                  AnimatedButton(
                    onClick = {
                      if (!isSignedIn) onSignInRequested() else onRedeemVoucher(cost, title)
                    },
                    enabled = !isSignedIn || hasBalance,
                    shape = RoundedCornerShape(8.dp),
                  ) {
                    Text(if (!isSignedIn) "Sign In" else "$cost PTS")
                  }
                }
              }
            }
            if (pair.size == 1) {
              Spacer(Modifier.weight(1f))
            }
          }
        }
      } else {
        items(vouchers.size) { index ->
          val (title, cost, subtitle) = vouchers[index]
          val hasBalance = uiState.wallet.availablePoints >= cost
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
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
                horizontalArrangement = Arrangement.spacedBy(12.dp),
              ) {
                Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(8.dp)) {
                  Icon(
                    Icons.Rounded.CardGiftcard,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(9.dp).size(20.dp),
                  )
                }
                Column {
                  Text(title, style = MaterialTheme.typography.titleMedium)
                  Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
              }
              Spacer(Modifier.width(8.dp))
              AnimatedButton(
                onClick = {
                  if (!isSignedIn) onSignInRequested() else onRedeemVoucher(cost, title)
                },
                enabled = !isSignedIn || hasBalance,
                shape = RoundedCornerShape(8.dp),
              ) {
                Text(if (!isSignedIn) "Sign In" else "$cost PTS")
              }
            }
          }
        }
      }

      item { SectionHeader("Payout Requests (${uiState.payoutRequests.size})") }
      if (uiState.payoutRequests.isEmpty()) {
        item {
          StyledEmptySectionCard(
            icon = Icons.Rounded.AccountBalance,
            title = "No payout requests yet",
            body = "Once you reach 100 PTS, submit a payout or voucher request for review.",
          )
        }
      } else {
        items(uiState.payoutRequests, key = { it.id }) { req ->
          val pStatus = PayoutStatus.fromString(req.status)
          val (bg, fg) = when (pStatus) {
            PayoutStatus.PAID, PayoutStatus.APPROVED -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.secondary
            PayoutStatus.REJECTED -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.error
            PayoutStatus.PENDING -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.tertiary
          }
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
          ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Text("${req.rewardTitle} (${req.pointsAmount} PTS)", style = MaterialTheme.typography.titleMedium)
                Surface(color = bg, shape = RoundedCornerShape(6.dp)) {
                  Text(
                    text = pStatus.name,
                    style = MaterialTheme.typography.labelSmall,
                    color = fg,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                  )
                }
              }
              Text(
                text = "${PayoutMethod.fromString(req.payoutMethod).label} · ${req.destinationHandle}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
              req.adminNotes?.takeIf { it.isNotBlank() }?.let { note ->
                Text("Admin note: $note", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
              }
            }
          }
        }
      }

      item { SectionHeader("Receipt Submissions (${uiState.receipts.size})") }
      if (uiState.receipts.isEmpty()) {
        item {
          StyledEmptySectionCard(
            icon = Icons.AutoMirrored.Rounded.ReceiptLong,
            title = "No receipts submitted yet",
            body = "Every receipt generates a SHA-256 fingerprint to prevent duplicate claims.",
          )
        }
      } else {
        items(uiState.receipts, key = { it.id }) { receipt ->
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
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
                text = "${MarketConfig.formatMoney(receipt.totalAmount, receipt.currencyCode)} · ${receipt.transactionDate} · Ref ${receipt.receiptReference}",
                style = TabularDataStyle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
              receipt.rejectionReason?.takeIf { it.isNotBlank() }?.let { reason ->
                Text("Note: $reason", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
              }
            }
          }
        }
      }

      item { SectionHeader("Point Ledger (${uiState.transactions.size})") }
      if (uiState.transactions.isEmpty()) {
        item {
          StyledEmptySectionCard(
            icon = Icons.Rounded.Stars,
            title = "Ledger awaiting first transaction",
            body = "Audited ledger entries appear here once a receipt or payout is processed.",
          )
        }
      } else {
        items(uiState.transactions, key = { it.id }) { tx ->
          TransactionRowCard(tx = tx, onClick = { onOpenTransaction(tx.id) })
        }
      }

      item { Spacer(Modifier.height(12.dp)) }
    }
  }

  if (showPayoutDialog) {
    AlertDialog(
      onDismissRequest = { showPayoutDialog = false },
      title = { Text("Request Reward Payout") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = "Available: ${uiState.wallet.availablePoints} PTS · Minimum: 100 PTS",
            style = TabularDataStyle,
          )
          val methods = PayoutMethod.entries
          SlidingSegmentedPillBar(
            options = methods.map { it.label.substringBefore(" ") },
            selectedIndex = methods.indexOf(selectedMethod).coerceAtLeast(0),
            onSelect = { idx -> selectedMethod = methods[idx] },
            modifier = Modifier.fillMaxWidth(),
          )
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
        AnimatedButton(
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
        AnimatedTextButton(onClick = { showPayoutDialog = false }) { Text("Cancel") }
      },
    )
  }
}

@Composable
private fun StyledEmptySectionCard(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  title: String,
  body: String,
) {
  Surface(
    shape = RoundedCornerShape(12.dp),
    color = MaterialTheme.colorScheme.surface,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(8.dp)) {
        Icon(
          icon,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(9.dp).size(20.dp),
        )
      }
      Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall)
        Text(body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
    }
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
      durationMillis = if (reducedMotion) 0 else 420,
      easing = FastOutSlowInEasing,
    ),
    label = "tierGaugeProgress",
  )

  val remaining = (nextTier.thresholdPts - availablePoints).coerceAtLeast(0L)
  val percentLabel = "${(rawProgress * 100f).toInt()}%"
  val trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)
  val arcColor = MaterialTheme.colorScheme.primary

  Surface(
    shape = RoundedCornerShape(18.dp),
    color = MaterialTheme.colorScheme.surface,
    border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      Box(
        modifier = Modifier.size(104.dp),
        contentAlignment = Alignment.Center,
      ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
          val strokePx = 10.dp.toPx()
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
            color = arcColor,
            startAngle = startAngle,
            sweepAngle = totalSweep * animatedProgress.coerceAtLeast(0.03f),
            useCenter = false,
            topLeft = Offset(inset, inset),
            size = arcSize,
            style = Stroke(width = strokePx, cap = StrokeCap.Round),
          )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(percentLabel, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
          Text(
            text = "${availablePoints}p",
            style = TabularDataStyle.copy(fontSize = MaterialTheme.typography.labelSmall.fontSize),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }

      Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(nextTier.title, style = MaterialTheme.typography.titleLarge)
        Text(
          text = if (remaining == 0L) "Threshold reached · Ready to redeem" else "$remaining PTS to ${nextTier.thresholdPts} PTS goal",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          tiers.forEach { tier ->
            val reached = availablePoints >= tier.thresholdPts
            Surface(
              color = if (reached) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant,
              shape = RoundedCornerShape(6.dp),
            ) {
              Text(
                text = "${tier.thresholdPts}",
                style = TabularDataStyle.copy(fontSize = MaterialTheme.typography.labelSmall.fontSize),
                color = if (reached) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
              )
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
  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .clickable(onClick = onClick),
    shape = RoundedCornerShape(12.dp),
    color = MaterialTheme.colorScheme.surface,
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
          text = "${status.name} · ${tx.auditReference} · ${formatTimestamp(tx.createdAt)}",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
      Text(
        text = "${if (isCredit) "+" else "-"}${tx.amount} PTS",
        style = TabularDataStyle.copy(fontWeight = FontWeight.Bold),
        color = if (isCredit) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error,
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

  Box(
    modifier = Modifier
      .fillMaxSize()
      .atmosphericBackdrop(),
    contentAlignment = Alignment.TopCenter,
  ) {
    LazyColumn(
      modifier = Modifier
        .widthIn(max = 860.dp)
        .fillMaxSize(),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      item {
        Row(verticalAlignment = Alignment.CenterVertically) {
          AnimatedIconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
          }
          Text("Submit Receipt", style = MaterialTheme.typography.titleLarge)
        }
      }

      item {
        Surface(
          color = MaterialTheme.colorScheme.surface,
          shape = RoundedCornerShape(12.dp),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Icon(Icons.Rounded.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(
              text = "Every receipt generates a SHA-256 fingerprint from Store + Date + Total + Reference to prevent duplicate submissions.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
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
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = MaterialTheme.colorScheme.surface,
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
              Text("Optional Partner Campaign", style = MaterialTheme.typography.titleSmall)
              uiState.campaigns.forEach { camp ->
                val selected = selectedCampaignId == camp.id
                Surface(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .bounceClickable { selectedCampaignId = if (selected) null else camp.id },
                  color = if (selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                  shape = RoundedCornerShape(10.dp),
                  border = BorderStroke(
                    1.dp,
                    if (selected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline,
                  ),
                ) {
                  Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                  ) {
                    Text(camp.title, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    Text(
                      text = "+${camp.rewardPoints} PTS",
                      style = TabularDataStyle.copy(fontWeight = FontWeight.Bold),
                      color = MaterialTheme.colorScheme.secondary,
                    )
                  }
                }
              }
            }

            AnimatedButton(
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
                .height(50.dp)
                .testTag("submit_receipt_button"),
              shape = RoundedCornerShape(10.dp),
            ) {
              Text(if (!isSignedIn) "Sign In with Google to Submit" else "Submit Receipt for Review")
            }
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

  Box(
    modifier = Modifier
      .fillMaxSize()
      .atmosphericBackdrop(),
    contentAlignment = Alignment.TopCenter,
  ) {
    Column(
      modifier = Modifier
        .widthIn(max = 860.dp)
        .fillMaxSize()
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        AnimatedIconButton(onClick = onBack) {
          Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
        }
        Text("Ledger Entry", style = MaterialTheme.typography.titleLarge)
      }

      if (tx == null) {
        Text("Transaction not found in your wallet ledger.", style = MaterialTheme.typography.bodyMedium)
      } else {
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = MaterialTheme.colorScheme.surface,
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        ) {
          Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(tx.sourceActivity, style = MaterialTheme.typography.headlineMedium)
            Text(
              text = "${tx.amount} ${tx.unitCode}",
              style = MaterialTheme.typography.displayMedium,
              color = MaterialTheme.colorScheme.secondary,
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
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
}

@Composable
private fun LedgerLine(label: String, value: String) {
  Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
    Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Text(value, style = TabularDataStyle)
  }
}
