package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CurrencyExchange
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RewardedAdCompletion
import com.example.data.model.WithdrawalRequest
import com.example.data.model.WithdrawalStatus
import com.example.domain.EarningsEngine
import com.example.domain.WithdrawalValidationResult
import com.example.ui.components.AdMobBannerSlot
import com.example.ui.components.AnimatedButton
import com.example.ui.components.AnimatedOutlinedButton
import com.example.ui.components.AnimatedTextButton
import com.example.ui.components.GlowingPillBadge
import com.example.ui.components.GuestModeBanner
import com.example.ui.components.RewardedAdLoadState
import com.example.ui.components.SectionHeader
import com.example.ui.components.atmosphericBackdrop
import com.example.ui.components.formatTimestamp
import com.example.ui.theme.HeroMintVaultBrush
import com.example.ui.theme.LedgerJadeDark
import com.example.ui.theme.MintGlowBorderBrush
import com.example.ui.theme.MintOnDarkText
import com.example.ui.theme.PrecisionAmberDark
import com.example.ui.theme.ReticleAmber
import com.example.ui.theme.SignalIndigoDark
import com.example.ui.theme.TabularDataStyle
import com.example.ui.theme.TabularFigureLargeStyle
import com.example.ui.theme.ViewfinderMuted
import com.example.ui.viewmodel.MainUiState
import java.math.BigDecimal
import java.math.RoundingMode

@Composable
fun EarningsScreen(
  uiState: MainUiState,
  adLoadState: RewardedAdLoadState,
  onWatchRewardedAd: () -> Unit,
  onOpenScan: () -> Unit,
  onOpenRewards: () -> Unit,
  onRequestWithdrawal: (walletAddress: String, amountMicros: Long) -> Unit,
  onSignInRequested: () -> Unit,
  isSignedIn: Boolean = uiState.firebaseUser != null,
) {
  val wallet = uiState.earningsWallet
  val reducedMotion = uiState.preferences.reducedMotionEnabled
  val canWithdraw = wallet.availableMicros >= EarningsEngine.MIN_PAYOUT_MICROS
  val thresholdProgress by animateFloatAsState(
    targetValue = EarningsEngine.thresholdProgressFraction(wallet.availableMicros),
    animationSpec = tween(durationMillis = if (reducedMotion) 0 else 320, easing = FastOutSlowInEasing),
    label = "minPayoutProgress",
  )

  var bep20Address by remember { mutableStateOf("") }
  var amountText by remember { mutableStateOf("1.00") }
  var validationError by remember { mutableStateOf<String?>(null) }

  BoxWithConstraints(
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
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
              text = "Earnings & Cashout",
              style = MaterialTheme.typography.headlineMedium,
              color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
              text = "Earn 10% of rewarded ad value per scan · Binance USDT BEP-20",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
          GlowingPillBadge(
            text = "10% Share",
            icon = Icons.Rounded.CurrencyExchange,
          )
        }
      }

      if (!isSignedIn) {
        item {
          GuestModeBanner(onSignInClick = onSignInRequested)
        }
      }

      // Hero Earnings Vault Card
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
              .drawBehind {
                drawCircle(
                  brush = Brush.radialGradient(
                    colors = listOf(SignalIndigoDark.copy(alpha = 0.24f), Color.Transparent),
                    center = Offset(size.width * 0.88f, size.height * 0.12f),
                    radius = size.width * 0.55f,
                  ),
                  radius = size.width * 0.55f,
                  center = Offset(size.width * 0.88f, size.height * 0.12f),
                )
              }
              .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.Top,
            ) {
              Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                  text = "AVAILABLE EARNINGS",
                  style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                  color = ViewfinderMuted,
                )
                Row(
                  verticalAlignment = Alignment.Bottom,
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                  Text(
                    text = EarningsEngine.formatUsdFromMicros(wallet.availableMicros),
                    style = TabularFigureLargeStyle,
                    color = Color.White,
                    modifier = Modifier.testTag("earnings_total_balance"),
                  )
                  Surface(
                    color = SignalIndigoDark.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(999.dp),
                    modifier = Modifier.padding(bottom = 7.dp),
                  ) {
                    Text(
                      text = "USD",
                      style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                      color = SignalIndigoDark,
                      modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
                    )
                  }
                }
              }

              Surface(
                color = Color.White.copy(alpha = 0.08f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
              ) {
                Column(
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                  horizontalAlignment = Alignment.End,
                ) {
                  Text(
                    text = "${wallet.adCompletionCount} Ads",
                    style = TabularDataStyle.copy(fontWeight = FontWeight.Bold),
                    color = LedgerJadeDark,
                  )
                  Text(
                    text = "Completed",
                    style = MaterialTheme.typography.labelSmall,
                    color = ViewfinderMuted,
                  )
                }
              }
            }

            // Lifetime / Pending / Withdrawn strip
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              EarningsStatPill(
                label = "Lifetime",
                value = EarningsEngine.formatUsdFromMicros(wallet.lifetimeEarnedMicros),
                valueColor = Color.White,
                modifier = Modifier.weight(1f),
              )
              EarningsStatPill(
                label = "Pending",
                value = EarningsEngine.formatUsdFromMicros(wallet.pendingWithdrawalMicros),
                valueColor = ReticleAmber,
                modifier = Modifier.weight(1f),
              )
              EarningsStatPill(
                label = "Withdrawn",
                value = EarningsEngine.formatUsdFromMicros(wallet.withdrawnMicros),
                valueColor = LedgerJadeDark,
                modifier = Modifier.weight(1f),
              )
            }

            // $1.00 Min Payout Progress
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Text(
                  text = if (canWithdraw) {
                    "Binance USDT BEP-20 payout unlocked"
                  } else {
                    val remaining = (EarningsEngine.MIN_PAYOUT_MICROS - wallet.availableMicros).coerceAtLeast(0L)
                    "${EarningsEngine.formatUsdFromMicros(remaining)} more needed for $1.00 min"
                  },
                  style = MaterialTheme.typography.labelMedium,
                  color = if (canWithdraw) LedgerJadeDark else Color.White.copy(alpha = 0.88f),
                )
                Text(
                  text = "${(thresholdProgress * 100f).toInt()}%",
                  style = TabularDataStyle.copy(fontWeight = FontWeight.Bold),
                  color = if (canWithdraw) LedgerJadeDark else ReticleAmber,
                )
              }
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .height(8.dp)
                  .clip(RoundedCornerShape(4.dp))
                  .background(Color.White.copy(alpha = 0.12f)),
              ) {
                Box(
                  modifier = Modifier
                    .fillMaxWidth(thresholdProgress)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (canWithdraw) LedgerJadeDark else SignalIndigoDark),
                )
              }
            }

            // Rewarded Ad + Scan CTAs
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              AnimatedButton(
                onClick = onWatchRewardedAd,
                enabled = adLoadState !is RewardedAdLoadState.Showing,
                modifier = Modifier
                  .weight(1.25f)
                  .height(50.dp)
                  .testTag("earnings_watch_ad_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                  containerColor = SignalIndigoDark,
                  contentColor = MintOnDarkText,
                ),
              ) {
                if (adLoadState is RewardedAdLoadState.Loading || adLoadState is RewardedAdLoadState.Showing) {
                  CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = MintOnDarkText,
                    strokeWidth = 2.dp,
                  )
                } else {
                  Icon(Icons.Rounded.PlayCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(6.dp))
                Text(
                  text = when (adLoadState) {
                    is RewardedAdLoadState.Loading -> "Loading Ad…"
                    is RewardedAdLoadState.Showing -> "Playing Ad…"
                    else -> "Watch Ad (+10%)"
                  },
                  style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
              }

              AnimatedOutlinedButton(
                onClick = onOpenScan,
                modifier = Modifier
                  .weight(1f)
                  .height(50.dp)
                  .testTag("earnings_open_scan_button"),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
                colors = ButtonDefaults.outlinedButtonColors(
                  containerColor = Color.White.copy(alpha = 0.08f),
                  contentColor = Color.White,
                ),
              ) {
                Icon(Icons.Rounded.QrCodeScanner, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Scan QR", color = Color.White, style = MaterialTheme.typography.labelLarge)
              }
            }

            if (adLoadState is RewardedAdLoadState.Unavailable) {
              Text(
                text = adLoadState.message,
                style = MaterialTheme.typography.bodySmall,
                color = ReticleAmber,
              )
            }
          }
        }
      }

      // How 10% scan & ad rewards work notice
      item {
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f)),
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Icon(
              Icons.Rounded.Verified,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(22.dp),
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
              Text(
                text = "Transparent 10% Reward Share",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
              )
              Text(
                text = "Scanning a QR or barcode unlocks a rewarded video ad. Every completed ad credits exactly 10% of its ad impression value directly to your USD earnings balance.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
        }
      }

      // Binance USDT (BEP-20) Withdrawal Card
      item {
        SectionHeader(title = "Withdraw via Binance USDT (BEP-20)")
      }
      item {
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("withdrawal_card"),
          shape = RoundedCornerShape(16.dp),
          color = MaterialTheme.colorScheme.surface,
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(8.dp),
              ) {
                Text(
                  text = "USDT · BNB Smart Chain (BEP-20)",
                  style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                  color = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                )
              }
              Text(
                text = "Min $1.00 USD",
                style = TabularDataStyle.copy(fontWeight = FontWeight.Bold),
                color = if (canWithdraw) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }

            if (!canWithdraw) {
              Surface(
                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f),
                shape = RoundedCornerShape(10.dp),
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Icon(
                    Icons.Rounded.WarningAmber,
                    contentDescription = null,
                    tint = PrecisionAmberDark,
                    modifier = Modifier.size(18.dp),
                  )
                  Text(
                    text = "Withdrawals require at least $1.00 USD available balance. Watch rewarded ads after scanning to reach $1.00.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                  )
                }
              }
            }

            OutlinedTextField(
              value = bep20Address,
              onValueChange = {
                bep20Address = it
                validationError = null
              },
              label = { Text("Binance USDT BEP-20 Address (0x...)") },
              placeholder = { Text("0x71C7656EC7ab88b098defB751B7401B5f6d8976F") },
              singleLine = true,
              isError = validationError != null && !EarningsEngine.isValidBep20Address(bep20Address),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("withdrawal_address_input"),
            )

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              OutlinedTextField(
                value = amountText,
                onValueChange = {
                  amountText = it
                  validationError = null
                },
                label = { Text("Amount (USD / USDT)") },
                placeholder = { Text("1.00") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier
                  .weight(1f)
                  .testTag("withdrawal_amount_input"),
              )
              AnimatedOutlinedButton(
                onClick = {
                  val maxFormatted = BigDecimal.valueOf(wallet.availableMicros.coerceAtLeast(0L), 6)
                    .setScale(4, RoundingMode.DOWN)
                    .stripTrailingZeros()
                    .let { if (it.scale() < 2) it.setScale(2, RoundingMode.DOWN) else it }
                    .toPlainString()
                  amountText = maxFormatted
                  validationError = null
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("withdrawal_max_button"),
              ) {
                Text("Max")
              }
            }

            validationError?.let { err ->
              Text(
                text = err,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.testTag("withdrawal_validation_error"),
              )
            }

            AnimatedButton(
              onClick = {
                if (!isSignedIn) {
                  onSignInRequested()
                  return@AnimatedButton
                }
                when (
                  val check = EarningsEngine.validateWithdrawal(
                    availableMicros = wallet.availableMicros,
                    amountText = amountText,
                    walletAddress = bep20Address,
                  )
                ) {
                  is WithdrawalValidationResult.Invalid -> {
                    validationError = check.message
                  }
                  is WithdrawalValidationResult.Valid -> {
                    validationError = null
                    onRequestWithdrawal(check.normalizedAddress, check.amountMicros)
                    bep20Address = ""
                  }
                }
              },
              modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("withdrawal_submit_button"),
              shape = RoundedCornerShape(12.dp),
            ) {
              Icon(Icons.Rounded.AccountBalanceWallet, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(Modifier.width(8.dp))
              Text(
                text = if (isSignedIn) "Request USDT BEP-20 Withdrawal" else "Sign in to Withdraw USDT",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
              )
            }
          }
        }
      }

      // Withdrawal Requests Status History
      item {
        SectionHeader(
          title = "Withdrawal Requests (${uiState.withdrawalRequests.size})",
          modifier = Modifier.testTag("withdrawal_history_section"),
        )
      }
      if (uiState.withdrawalRequests.isEmpty()) {
        item {
          Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
          ) {
            Text(
              text = "No Binance USDT BEP-20 withdrawals requested yet. Reach $1.00 USD to request your first payout.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            )
          }
        }
      } else {
        items(uiState.withdrawalRequests, key = { it.id }) { req ->
          WithdrawalHistoryCard(request = req)
        }
      }

      // Rewarded Ad Completions History
      item {
        SectionHeader(
          title = "Rewarded Ad History (${uiState.adRewards.size})",
          modifier = Modifier.testTag("ad_rewards_history_section"),
        )
      }
      if (uiState.adRewards.isEmpty()) {
        item {
          Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Text("No rewarded ads completed yet", style = MaterialTheme.typography.titleMedium)
              Text(
                text = "Scan any QR or barcode or tap 'Watch Ad (+10%)' above. Every finished ad records your 10% share here.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
        }
      } else {
        items(uiState.adRewards, key = { it.id }) { item ->
          AdRewardHistoryCard(completion = item)
        }
      }

      // Secondary link to PTS vouchers & receipt rewards
      item {
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = MaterialTheme.colorScheme.surface,
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
              Text("Receipt Bonus & Points Wallet", style = MaterialTheme.typography.titleSmall)
              Text(
                text = "${uiState.wallet.availablePoints} PTS available in vouchers & receipt rewards",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
            AnimatedTextButton(onClick = onOpenRewards) {
              Text("Open")
              Spacer(Modifier.width(4.dp))
              Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
            }
          }
        }
      }

      item { AdMobBannerSlot() }
      item { Spacer(Modifier.height(12.dp)) }
    }
  }
}

@Composable
private fun EarningsStatPill(
  label: String,
  value: String,
  valueColor: Color,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier,
    color = Color.White.copy(alpha = 0.08f),
    shape = RoundedCornerShape(12.dp),
    border = BorderStroke(0.75.dp, Color.White.copy(alpha = 0.12f)),
  ) {
    Column(
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
      verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = ViewfinderMuted,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
      Text(
        text = value,
        style = TabularDataStyle.copy(fontWeight = FontWeight.Bold),
        color = valueColor,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}

@Composable
private fun WithdrawalHistoryCard(request: WithdrawalRequest) {
  val status = WithdrawalStatus.fromString(request.status)
  val (bg, fg, icon) = when (status) {
    WithdrawalStatus.PAID -> Triple(
      MaterialTheme.colorScheme.secondaryContainer,
      MaterialTheme.colorScheme.secondary,
      Icons.Rounded.CheckCircle,
    )
    WithdrawalStatus.APPROVED -> Triple(
      MaterialTheme.colorScheme.primaryContainer,
      MaterialTheme.colorScheme.primary,
      Icons.Rounded.CheckCircle,
    )
    WithdrawalStatus.REJECTED -> Triple(
      MaterialTheme.colorScheme.errorContainer,
      MaterialTheme.colorScheme.error,
      Icons.Rounded.WarningAmber,
    )
    WithdrawalStatus.PENDING -> Triple(
      MaterialTheme.colorScheme.tertiaryContainer,
      MaterialTheme.colorScheme.tertiary,
      Icons.Rounded.Schedule,
    )
  }
  val maskedAddress = if (request.walletAddress.length > 14) {
    "${request.walletAddress.take(8)}...${request.walletAddress.takeLast(6)}"
  } else {
    request.walletAddress
  }

  Surface(
    shape = RoundedCornerShape(14.dp),
    color = MaterialTheme.colorScheme.surface,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "${EarningsEngine.formatUsdFromMicros(request.amountMicros)} ${request.asset} (${request.network})",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          )
          Text(
            text = "Binance BEP-20 · $maskedAddress",
            style = TabularDataStyle,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        Surface(color = bg, shape = RoundedCornerShape(8.dp)) {
          Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(14.dp))
            Text(
              text = status.name,
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = fg,
            )
          }
        }
      }

      Text(
        text = "Requested: ${formatTimestamp(request.requestedAt)}",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )

      if (!request.txHash.isNullOrBlank()) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
        Text(
          text = "BSC TxHash: ${request.txHash}",
          style = TabularDataStyle,
          color = MaterialTheme.colorScheme.primary,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
      if (!request.adminNotes.isNullOrBlank()) {
        Text(
          text = "Note: ${request.adminNotes}",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
  }
}

@Composable
private fun AdRewardHistoryCard(completion: RewardedAdCompletion) {
  val sourceLabel = when (completion.triggerSource) {
    "QR_SCAN" -> "QR Code Scan Reward"
    "BARCODE_SCAN" -> "Barcode Scan Reward"
    "EARNINGS_BONUS" -> "Rewarded Ad Watch"
    else -> completion.triggerSource
  }
  val codeSubtitle = if (completion.scannedCode.isNotBlank()) {
    "Code: ${completion.scannedCode}"
  } else {
    "Ad Unit: ${completion.adUnitId.takeLast(10)}"
  }

  Surface(
    shape = RoundedCornerShape(14.dp),
    color = MaterialTheme.colorScheme.surface,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(sourceLabel, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold))
        Text(
          text = "$codeSubtitle · Ad Value ${EarningsEngine.formatUsdFromMicros(completion.adValueMicros)} (${completion.currencyCode})",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        Text(
          text = formatTimestamp(completion.createdAt),
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
      Spacer(Modifier.width(8.dp))
      Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = RoundedCornerShape(8.dp),
      ) {
        Column(
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
          horizontalAlignment = Alignment.End,
        ) {
          Text(
            text = "+${EarningsEngine.formatUsdFromMicros(completion.userShareMicros)}",
            style = TabularDataStyle.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.secondary,
          )
          Text(
            text = "10% share",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
          )
        }
      }
    }
  }
}
