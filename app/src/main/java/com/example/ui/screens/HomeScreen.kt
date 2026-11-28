package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Paid
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.Campaign
import com.example.data.model.ScanContext
import com.example.data.model.ScanRecord
import com.example.domain.EarningsEngine
import com.example.domain.MarketConfig
import com.example.ui.components.AdMobBannerSlot
import com.example.ui.components.AnimatedButton
import com.example.ui.components.AnimatedIconButton
import com.example.ui.components.AnimatedOutlinedButton
import com.example.ui.components.GlowingPillBadge
import com.example.ui.components.GuestModeBanner
import com.example.ui.components.LivePulseDot
import com.example.ui.components.ProductSummaryCard
import com.example.ui.components.QuestMissionsSectionCard
import com.example.ui.components.SectionHeader
import com.example.ui.components.SlidingSegmentedPillBar
import com.example.ui.components.VerificationLabel
import com.example.ui.components.atmosphericBackdrop
import com.example.ui.components.bounceClickable
import com.example.ui.components.formatTimestamp
import com.example.ui.components.sharedProductBounds
import com.example.ui.theme.LocalExtendedColors
import com.example.ui.theme.TabularDataStyle
import com.example.ui.theme.TabularFigureLargeStyle
import com.example.ui.viewmodel.MainUiState

@Composable
fun HomeScreen(
  uiState: MainUiState,
  onOpenScan: () -> Unit,
  onRescan: (ScanContext) -> Unit,
  onOpenDiscover: () -> Unit,
  onOpenEarnings: () -> Unit = {},
  onWatchRewardedAd: () -> Unit = {},
  onOpenRewards: () -> Unit,
  onOpenReceiptSubmit: (String?) -> Unit,
  onOpenProduct: (String) -> Unit,
  onOpenNotifications: () -> Unit,
  onOpenScanHistory: () -> Unit,
  onOpenAdmin: () -> Unit,
  onSignInRequested: () -> Unit,
) {
  val unreadCount = remember(uiState.notifications) { uiState.notifications.count { !it.read } }
  val market = remember(uiState.preferences.countryCode) { MarketConfig.findByCountry(uiState.preferences.countryCode) }
  val reducedMotion = uiState.preferences.reducedMotionEnabled
  val extended = LocalExtendedColors.current
  var chartTab by remember { mutableIntStateOf(0) }
  val animatedAvailablePoints by animateIntAsState(
    targetValue = uiState.wallet.availablePoints.toInt(),
    animationSpec = tween(durationMillis = if (reducedMotion) 0 else 280, easing = FastOutSlowInEasing),
    label = "heroAvailablePts",
  )
  val animatedPendingPoints by animateIntAsState(
    targetValue = uiState.wallet.pendingPoints.toInt(),
    animationSpec = tween(durationMillis = if (reducedMotion) 0 else 280, easing = FastOutSlowInEasing),
    label = "heroPendingPts",
  )

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
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      // Expressive Top Header
      item {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              GlowingPillBadge(
                text = "${market.countryCode} · ${market.currencyCode}",
                icon = Icons.Rounded.Public,
              )
              LivePulseDot(color = MaterialTheme.colorScheme.primary, size = 5.dp)
            }
            Text(
              text = uiState.firebaseUser?.displayName?.let { "Hey, $it" } ?: "SCAN2MONEY",
              style = MaterialTheme.typography.displayMedium,
              color = MaterialTheme.colorScheme.onBackground,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            if (uiState.adminRole != null) {
              Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = CircleShape,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
              ) {
                AnimatedIconButton(
                  onClick = onOpenAdmin,
                  modifier = Modifier.testTag("home_admin_button"),
                ) {
                  Icon(
                    Icons.Rounded.AdminPanelSettings,
                    contentDescription = "Admin Console",
                    tint = MaterialTheme.colorScheme.primary,
                  )
                }
              }
            }
            Surface(
              color = MaterialTheme.colorScheme.surface,
              shape = CircleShape,
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            ) {
              AnimatedIconButton(onClick = onOpenNotifications) {
                Box {
                  Icon(
                    Icons.Rounded.Notifications,
                    contentDescription = "Notifications",
                    tint = MaterialTheme.colorScheme.onSurface,
                  )
                  if (unreadCount > 0) {
                    Box(
                      modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.error),
                    )
                  }
                }
              }
            }
          }
        }
      }

      if (uiState.firebaseUser == null) {
        item {
          GuestModeBanner(onSignInClick = onSignInRequested)
        }
      }

      // Theme-Adaptive Vault Balance Card
      item {
        Surface(
          shape = RoundedCornerShape(24.dp),
          color = Color.Transparent,
          border = BorderStroke(1.dp, extended.heroBorderBrush),
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .background(extended.heroBrush)
              .drawBehind {
                drawCircle(
                  brush = Brush.radialGradient(
                    colors = listOf(extended.heroRadialTint, Color.Transparent),
                    center = Offset(size.width * 0.9f, size.height * 0.1f),
                    radius = size.width * 0.52f,
                  ),
                  radius = size.width * 0.52f,
                  center = Offset(size.width * 0.9f, size.height * 0.1f),
                )
              }
              .padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.Top,
            ) {
              Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                  text = "AD EARNINGS (10% SHARE)",
                  style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                  color = extended.heroSecondaryText,
                )
                Row(
                  verticalAlignment = Alignment.Bottom,
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                  Text(
                    text = EarningsEngine.formatUsdFromMicros(uiState.earningsWallet.availableMicros),
                    style = TabularFigureLargeStyle,
                    color = extended.heroPrimaryText,
                    modifier = Modifier.testTag("home_earnings_balance_value"),
                  )
                  Surface(
                    color = extended.heroBadgeBg,
                    shape = RoundedCornerShape(999.dp),
                    modifier = Modifier.padding(bottom = 7.dp),
                  ) {
                    Text(
                      text = "USDT",
                      style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                      color = extended.heroBadgeText,
                      modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
                    )
                  }
                }
                Text(
                  text = "Min $1.00 Binance BEP-20 payout · $animatedAvailablePoints PTS",
                  style = MaterialTheme.typography.labelSmall,
                  color = extended.heroSecondaryText,
                )
              }

              Surface(
                modifier = Modifier
                  .clip(RoundedCornerShape(12.dp))
                  .bounceClickable(onClick = onOpenRewards),
                color = extended.heroChipBg,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, extended.heroChipBorder),
              ) {
                Column(
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                  horizontalAlignment = Alignment.End,
                ) {
                  Text(
                    text = "${uiState.earningsWallet.adCompletionCount} Ads",
                    style = TabularDataStyle.copy(fontWeight = FontWeight.Bold),
                    color = extended.heroPositiveAccent,
                  )
                  Text(
                    text = "$animatedPendingPoints PTS pend.",
                    style = MaterialTheme.typography.labelSmall,
                    color = extended.heroWarningAccent,
                  )
                }
              }
            }

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              AnimatedButton(
                onClick = onOpenScan,
                modifier = Modifier
                  .weight(1f)
                  .height(50.dp)
                  .testTag("home_scan_item_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                  containerColor = extended.heroCtaBg,
                  contentColor = extended.heroCtaFg,
                ),
              ) {
                Icon(Icons.Rounded.QrCodeScanner, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(7.dp))
                Text("Scan & Earn", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
              }

              AnimatedOutlinedButton(
                onClick = onOpenEarnings,
                modifier = Modifier
                  .weight(1f)
                  .height(50.dp)
                  .testTag("home_open_earnings_button"),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, extended.heroSecondaryBtnBorder),
                colors = ButtonDefaults.outlinedButtonColors(
                  containerColor = extended.heroSecondaryBtnBg,
                  contentColor = extended.heroSecondaryBtnFg,
                ),
              ) {
                Icon(
                  Icons.Rounded.Paid,
                  contentDescription = null,
                  tint = extended.heroSecondaryBtnFg,
                  modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(7.dp))
                Text("Withdraw", color = extended.heroSecondaryBtnFg, style = MaterialTheme.typography.labelLarge)
              }
            }

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              AnimatedOutlinedButton(
                onClick = onWatchRewardedAd,
                modifier = Modifier
                  .weight(1f)
                  .height(42.dp)
                  .testTag("home_watch_ad_button"),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, extended.heroBadgeText.copy(alpha = 0.35f)),
                colors = ButtonDefaults.outlinedButtonColors(
                  containerColor = extended.heroBadgeBg,
                  contentColor = extended.heroBadgeText,
                ),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
              ) {
                Icon(Icons.Rounded.PlayCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Watch Ad (+10%)", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
              }
              AnimatedOutlinedButton(
                onClick = { onOpenReceiptSubmit(null) },
                modifier = Modifier
                  .weight(1f)
                  .height(42.dp)
                  .testTag("home_log_receipt_button"),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, extended.heroChipBorder),
                colors = ButtonDefaults.outlinedButtonColors(
                  containerColor = extended.heroChipBg,
                  contentColor = extended.heroPrimaryText,
                ),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
              ) {
                Icon(Icons.AutoMirrored.Rounded.ReceiptLong, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Log Receipt", style = MaterialTheme.typography.labelMedium)
              }
            }
          }
        }
      }

      // Tinted Quick-Action Bento Strip
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          QuickAccessTile(
            icon = Icons.Rounded.Search,
            label = "Discover",
            accent = extended.bentoDiscoverAccent,
            onClick = onOpenDiscover,
            modifier = Modifier
              .weight(1f)
              .testTag("home_bento_discover"),
          )
          QuickAccessTile(
            icon = Icons.Rounded.Paid,
            label = "Earnings",
            accent = extended.bentoEarningsAccent,
            onClick = onOpenEarnings,
            modifier = Modifier
              .weight(1f)
              .testTag("home_bento_earnings"),
          )
          QuickAccessTile(
            icon = Icons.Rounded.AccountBalanceWallet,
            label = "Wallet",
            accent = extended.bentoWalletAccent,
            onClick = onOpenRewards,
            modifier = Modifier
              .weight(1f)
              .testTag("home_bento_wallet"),
          )
          QuickAccessTile(
            icon = Icons.Rounded.History,
            label = "History",
            accent = extended.bentoHistoryAccent,
            onClick = onOpenScanHistory,
            modifier = Modifier
              .weight(1f)
              .testTag("home_bento_history"),
          )
        }
      }

      if (isWide) {
        item {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top,
          ) {
            QuestMissionsSectionCard(
              missions = uiState.shopperMissions,
              reducedMotion = reducedMotion,
              onOpenScan = onOpenScan,
              onOpenReceiptSubmit = { onOpenReceiptSubmit(null) },
              modifier = Modifier.weight(1f),
            )
            ScanActivityAnalyticsCard(
              scans = uiState.recentScans,
              chartTab = chartTab,
              reducedMotion = reducedMotion,
              onSelectTab = { chartTab = it },
              modifier = Modifier.weight(1f),
            )
          }
        }
      } else {
        item {
          QuestMissionsSectionCard(
            missions = uiState.shopperMissions,
            reducedMotion = reducedMotion,
            onOpenScan = onOpenScan,
            onOpenReceiptSubmit = { onOpenReceiptSubmit(null) },
          )
        }
        item {
          ScanActivityAnalyticsCard(
            scans = uiState.recentScans,
            chartTab = chartTab,
            reducedMotion = reducedMotion,
            onSelectTab = { chartTab = it },
          )
        }
      }

      if (uiState.campaigns.isNotEmpty()) {
        item {
          SectionHeader(
            title = "Active Partner Campaigns",
            actionLabel = "All Rewards",
            onAction = onOpenRewards,
          )
        }
        item {
          LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(uiState.campaigns, key = { it.id }) { campaign ->
              CampaignHighlightCard(
                campaign = campaign,
                onSubmitReceipt = { onOpenReceiptSubmit(campaign.id) },
              )
            }
          }
        }
      }

      item {
        SectionHeader(
          title = "Recent Scans",
          actionLabel = if (uiState.recentScans.isNotEmpty()) "All History" else null,
          onAction = onOpenScanHistory,
        )
      }

      if (uiState.recentScans.isEmpty()) {
        item {
          EmptyScanWelcomeCard(
            onOpenScan = onOpenScan,
            onOpenDiscover = onOpenDiscover,
          )
        }
      } else {
        items(
          items = uiState.recentScans.take(5),
          key = { "${it.id}_${it.barcodeValue}_${it.localTimestampMs}" },
        ) { scan ->
          RecentScanWithRescanCard(
            scan = scan,
            onInspect = { onOpenProduct(scan.barcodeValue) },
            onRescan = {
              onRescan(
                ScanContext(
                  barcodeValue = scan.barcodeValue,
                  barcodeFormat = scan.barcodeFormat,
                  productName = scan.productName,
                  brand = scan.brand,
                ),
              )
            },
            modifier = Modifier.animateItem(),
          )
        }
      }

      if (uiState.cachedCatalog.isNotEmpty()) {
        item {
          SectionHeader(
            title = "Live Retail Catalog",
            actionLabel = "Browse All",
            onAction = onOpenDiscover,
          )
        }
        val homeCatalog = uiState.cachedCatalog.take(6)
        if (isWide) {
          val catalogRows = homeCatalog.chunked(2)
          items(catalogRows) { pair ->
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              pair.forEach { product ->
                ProductSummaryCard(
                  product = product,
                  onClick = { onOpenProduct(product.barcode) },
                  modifier = Modifier.weight(1f),
                )
              }
              if (pair.size == 1) {
                Spacer(Modifier.weight(1f))
              }
            }
          }
        } else {
          items(homeCatalog.take(4), key = { it.barcode }) { product ->
            ProductSummaryCard(
              product = product,
              onClick = { onOpenProduct(product.barcode) },
              modifier = Modifier.animateItem(),
            )
          }
        }
      }

      item { AdMobBannerSlot() }

      item { Spacer(Modifier.height(12.dp)) }
    }
  }
}

@Composable
private fun EmptyScanWelcomeCard(
  onOpenScan: () -> Unit,
  onOpenDiscover: () -> Unit,
) {
  Surface(
    shape = RoundedCornerShape(18.dp),
    color = MaterialTheme.colorScheme.surface,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.28f)),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        Image(
          painter = painterResource(id = R.drawable.img_scan_hero),
          contentDescription = "Scan any product barcode",
          modifier = Modifier
            .size(74.dp)
            .clip(RoundedCornerShape(14.dp)),
          contentScale = ContentScale.Crop,
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Text(
            text = "Scan your first product",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = "Aim the barcode camera or search the live Open Food Facts catalog to compare real store prices.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }

      Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        AnimatedOutlinedButton(
          onClick = onOpenDiscover,
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(10.dp),
        ) {
          Icon(Icons.Rounded.Search, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(Modifier.width(6.dp))
          Text("Live Catalog")
        }
        AnimatedButton(
          onClick = onOpenScan,
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(10.dp),
        ) {
          Icon(Icons.Rounded.QrCodeScanner, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(Modifier.width(6.dp))
          Text("Scan Now", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
        }
      }
    }
  }
}

@Composable
private fun QuickAccessTile(
  icon: ImageVector,
  label: String,
  accent: Color,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier
      .clip(RoundedCornerShape(18.dp))
      .bounceClickable(onClick = onClick),
    color = MaterialTheme.colorScheme.surface,
    shape = RoundedCornerShape(18.dp),
    border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
  ) {
    Column(
      modifier = Modifier.padding(vertical = 14.dp, horizontal = 6.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Surface(
        color = accent.copy(alpha = 0.14f),
        shape = RoundedCornerShape(11.dp),
      ) {
        Icon(
          icon,
          contentDescription = label,
          tint = accent,
          modifier = Modifier
            .padding(9.dp)
            .size(19.dp),
        )
      }
      Text(
        text = label,
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}

@Composable
private fun ScanActivityAnalyticsCard(
  scans: List<ScanRecord>,
  chartTab: Int,
  reducedMotion: Boolean,
  onSelectTab: (Int) -> Unit,
  modifier: Modifier = Modifier,
) {
  val dayBuckets = remember(scans) {
    val nowMs = System.currentTimeMillis()
    (6 downTo 0).map { daysAgo ->
      val start = nowMs - (daysAgo + 1) * 86_400_000L
      val end = nowMs - daysAgo * 86_400_000L
      scans.count { scan ->
        val t = scan.scannedAt?.toDate()?.time ?: scan.localTimestampMs
        t in start..end
      }
    }
  }
  val formatCounts = remember(scans) {
    scans.groupingBy { it.barcodeFormat.ifEmpty { "Unknown" } }
      .eachCount()
      .entries
      .sortedByDescending { it.value }
      .take(4)
  }

  Surface(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(18.dp),
    color = MaterialTheme.colorScheme.surface,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text("Scan Activity", style = MaterialTheme.typography.titleLarge)
          Text(
            text = "${scans.size} scans saved",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        SlidingSegmentedPillBar(
          options = listOf("7 Days", "Formats"),
          selectedIndex = chartTab,
          onSelect = onSelectTab,
          modifier = Modifier.width(164.dp),
          height = 38.dp,
        )
      }

      if (chartTab == 0) {
        val maxVal = maxOf(1, dayBuckets.maxOrNull() ?: 1)
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .height(82.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.Bottom,
        ) {
          dayBuckets.forEachIndexed { i, count ->
            val targetFraction = if (count == 0) 0.14f else (count.toFloat() / maxVal.toFloat()).coerceIn(0.2f, 1f)
            val animatedFraction by animateFloatAsState(
              targetValue = targetFraction,
              animationSpec = tween(durationMillis = if (reducedMotion) 0 else 280, easing = FastOutSlowInEasing),
              label = "scanBarHeight_$i",
            )
            val isToday = i == 6
            val barColor = when {
              count == 0 -> MaterialTheme.colorScheme.surfaceVariant
              isToday -> MaterialTheme.colorScheme.primary
              else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
            }
            Column(
              modifier = Modifier.weight(1f),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.Bottom,
            ) {
              if (count > 0) {
                Text(
                  text = "$count",
                  style = TabularDataStyle.copy(fontSize = MaterialTheme.typography.labelSmall.fontSize),
                  color = MaterialTheme.colorScheme.primary,
                )
              }
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .height((46f * animatedFraction).dp)
                  .clip(RoundedCornerShape(6.dp))
                  .background(barColor),
              )
              Spacer(Modifier.height(4.dp))
              Text(
                text = if (isToday) "Today" else "-${6 - i}d",
                style = MaterialTheme.typography.labelSmall,
                color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
        }
      } else {
        if (formatCounts.isEmpty()) {
          Text(
            text = "Scan items to view barcode symbology breakdown.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        } else {
          val maxFmt = maxOf(1, formatCounts.maxOf { it.value })
          Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            formatCounts.forEachIndexed { index, (fmt, count) ->
              val animatedWidth by animateFloatAsState(
                targetValue = (count.toFloat() / maxFmt.toFloat()).coerceIn(0.1f, 1f),
                animationSpec = tween(durationMillis = if (reducedMotion) 0 else 280, easing = FastOutSlowInEasing),
                label = "formatBarWidth_$index",
              )
              Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Text(fmt, style = MaterialTheme.typography.labelLarge)
                  Text(
                    text = "$count",
                    style = TabularDataStyle.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                  )
                }
                Box(
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                ) {
                  Box(
                    modifier = Modifier
                      .fillMaxWidth(animatedWidth)
                      .height(6.dp)
                      .clip(RoundedCornerShape(3.dp))
                      .background(MaterialTheme.colorScheme.primary),
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun CampaignHighlightCard(
  campaign: Campaign,
  onSubmitReceipt: () -> Unit,
) {
  Surface(
    modifier = Modifier.width(268.dp),
    shape = RoundedCornerShape(16.dp),
    color = MaterialTheme.colorScheme.surface,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = campaign.partnerName,
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.weight(1f),
        )
        Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(999.dp)) {
          Text(
            text = "+${campaign.rewardPoints} PTS",
            style = TabularDataStyle.copy(fontSize = MaterialTheme.typography.labelSmall.fontSize, fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
          )
        }
      }
      Text(
        text = campaign.title,
        style = MaterialTheme.typography.titleMedium,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
      )
      Text(
        text = campaign.terms,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
      )
      HorizontalDivider(color = MaterialTheme.colorScheme.outline)
      AnimatedButton(
        onClick = onSubmitReceipt,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.primaryContainer,
          contentColor = MaterialTheme.colorScheme.primary,
        ),
      ) {
        Text("Claim with Receipt", style = MaterialTheme.typography.labelLarge)
      }
    }
  }
}

@Composable
private fun RecentScanWithRescanCard(
  scan: ScanRecord,
  onInspect: () -> Unit,
  onRescan: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var menuExpanded by remember(scan.id, scan.barcodeValue, scan.localTimestampMs) { mutableStateOf(false) }

  Surface(
    modifier = modifier
      .fillMaxWidth()
      .sharedProductBounds("product_card_${scan.barcodeValue}")
      .clip(RoundedCornerShape(16.dp))
      .bounceClickable(onClick = onInspect),
    shape = RoundedCornerShape(16.dp),
    color = MaterialTheme.colorScheme.surface,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
  ) {
    Column(
      modifier = Modifier.padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
          Text(
            text = scan.productName.ifBlank { "Barcode ${scan.barcodeValue}" },
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            if (scan.brand.isNotBlank()) {
              Text(
                text = scan.brand,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
              Text("·", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
              text = "${scan.barcodeFormat.ifBlank { "GTIN" }} ${scan.barcodeValue}",
              style = TabularDataStyle,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }
        }

        Box {
          AnimatedIconButton(onClick = { menuExpanded = true }) {
            Icon(Icons.Rounded.MoreVert, contentDescription = "Scan options")
          }
          DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false },
          ) {
            DropdownMenuItem(
              text = { Text("Re-scan with ${scan.barcodeFormat.ifBlank { "product" }}") },
              leadingIcon = { Icon(Icons.Rounded.Refresh, contentDescription = null) },
              onClick = {
                menuExpanded = false
                onRescan()
              },
            )
            DropdownMenuItem(
              text = { Text("Inspect product & offers") },
              leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
              onClick = {
                menuExpanded = false
                onInspect()
              },
            )
          }
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          VerificationLabel(statusRaw = scan.verificationStatus)
          Text(
            text = formatTimestamp(scan.scannedAt, scan.localTimestampMs),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        AnimatedOutlinedButton(
          onClick = onRescan,
          shape = RoundedCornerShape(10.dp),
          contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        ) {
          Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(Modifier.width(4.dp))
          Text("Re-scan", style = MaterialTheme.typography.labelLarge)
        }
      }
    }
  }
}
