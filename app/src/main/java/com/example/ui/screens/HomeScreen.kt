package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.Campaign
import com.example.data.model.ScanContext
import com.example.data.model.ScanRecord
import com.example.domain.MarketConfig
import com.example.ui.components.GuestModeBanner
import com.example.ui.components.ProductSummaryCard
import com.example.ui.components.QuestMissionsSectionCard
import com.example.ui.components.SectionHeader
import com.example.ui.components.VerificationLabel
import com.example.ui.components.formatTimestamp
import com.example.ui.theme.AmberGold
import com.example.ui.theme.AmberSoftTint
import com.example.ui.theme.CobaltInfo
import com.example.ui.theme.CobaltSoftTint
import com.example.ui.theme.CoralAccent
import com.example.ui.theme.CyanInfo
import com.example.ui.theme.DeepObsidian
import com.example.ui.theme.ElectricLime
import com.example.ui.theme.EmeraldSoftTint
import com.example.ui.theme.EmeraldTrust
import com.example.ui.theme.HeaderEmeraldCobaltBrush
import com.example.ui.theme.HeroObsidianEmeraldBrush
import com.example.ui.theme.ObsidianDivider
import com.example.ui.theme.ObsidianElevated
import com.example.ui.theme.ObsidianMutedText
import com.example.ui.theme.ObsidianOnLime
import com.example.ui.theme.TangerineAccent
import com.example.ui.theme.TealVivid
import com.example.ui.theme.VioletAccent
import com.example.ui.theme.VioletSoftTint
import com.example.ui.viewmodel.MainUiState

@Composable
fun HomeScreen(
  uiState: MainUiState,
  onOpenScan: () -> Unit,
  onRescan: (ScanContext) -> Unit,
  onOpenDiscover: () -> Unit,
  onOpenRewards: () -> Unit,
  onOpenReceiptSubmit: (String?) -> Unit,
  onOpenProduct: (String) -> Unit,
  onOpenNotifications: () -> Unit,
  onOpenScanHistory: () -> Unit,
  onOpenAdmin: () -> Unit,
  onSignInRequested: () -> Unit,
) {
  val unreadCount = uiState.notifications.count { !it.read }
  val market = MarketConfig.findByCountry(uiState.preferences.countryCode)
  var chartTab by remember { mutableIntStateOf(0) } // 0 = 7d Scans, 1 = Format Breakdown

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    // Rich Atmospheric Top Header Strip
    item {
      Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .background(HeaderEmeraldCobaltBrush)
            .padding(horizontal = 16.dp, vertical = 14.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Surface(
              color = ElectricLime,
              shape = RoundedCornerShape(8.dp),
            ) {
              Text(
                text = "SCAN2MONEY • ${market.countryCode} (${market.currencyCode})",
                style = MaterialTheme.typography.labelSmall,
                color = ObsidianOnLime,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
              )
            }
            Text(
              text = uiState.firebaseUser?.displayName?.let { "Welcome back, $it" } ?: "Product & Rewards Hub",
              style = MaterialTheme.typography.headlineMedium,
              color = Color.White,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            if (uiState.adminRole != null) {
              Surface(
                color = Color.White.copy(alpha = 0.16f),
                shape = CircleShape,
              ) {
                IconButton(onClick = onOpenAdmin) {
                  Icon(
                    Icons.Rounded.AdminPanelSettings,
                    contentDescription = "Admin Console",
                    tint = ElectricLime,
                  )
                }
              }
            }
            Surface(
              color = Color.White.copy(alpha = 0.16f),
              shape = CircleShape,
            ) {
              IconButton(onClick = onOpenNotifications) {
                Box {
                  Icon(
                    Icons.Rounded.Notifications,
                    contentDescription = "Notifications",
                    tint = Color.White,
                  )
                  if (unreadCount > 0) {
                    Box(
                      modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(9.dp)
                        .clip(CircleShape)
                        .background(CoralAccent),
                    )
                  }
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

    // Hero Emerald-Obsidian Gradient Balance Command Card with Glow Rings
    item {
      Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, ElectricLime.copy(alpha = 0.35f)),
      ) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .background(HeroObsidianEmeraldBrush),
        ) {
          Canvas(modifier = Modifier.matchParentSize()) {
            drawCircle(
              color = ElectricLime.copy(alpha = 0.10f),
              radius = size.width * 0.34f,
              center = Offset(size.width * 0.92f, size.height * 0.14f),
            )
            drawCircle(
              color = CobaltInfo.copy(alpha = 0.18f),
              radius = size.width * 0.28f,
              center = Offset(size.width * 0.08f, size.height * 0.95f),
            )
          }

          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Column {
                Text(
                  text = "SANDBOX LEDGER BALANCE",
                  style = MaterialTheme.typography.labelSmall,
                  color = EmeraldSoftTint,
                )
                Row(
                  verticalAlignment = Alignment.Bottom,
                  horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                  Text(
                    text = "${uiState.wallet.availablePoints}",
                    style = MaterialTheme.typography.displayLarge,
                    color = ElectricLime,
                  )
                  Text(
                    text = "PTS available",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 6.dp),
                  )
                }
              }
              Surface(
                color = Color.White.copy(alpha = 0.12f),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, AmberGold.copy(alpha = 0.45f)),
              ) {
                Column(
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                  horizontalAlignment = Alignment.End,
                ) {
                  Text(
                    text = "${uiState.wallet.pendingPoints} PTS",
                    style = MaterialTheme.typography.titleMedium,
                    color = AmberGold,
                  )
                  Text(
                    text = "Pending review",
                    style = MaterialTheme.typography.labelSmall,
                    color = ObsidianMutedText,
                  )
                }
              }
            }

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              AnchorPillCard("$0", "to scan", AmberGold, modifier = Modifier.weight(1f))
              AnchorPillCard("< 2s", "catalog lookup", Color(0xFF38BDF8), modifier = Modifier.weight(1f))
              AnchorPillCard("100p", "voucher start", ElectricLime, modifier = Modifier.weight(1f))
            }

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              Button(
                onClick = onOpenScan,
                modifier = Modifier
                  .weight(1f)
                  .height(48.dp)
                  .testTag("home_scan_item_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                  containerColor = ElectricLime,
                  contentColor = ObsidianOnLime,
                ),
              ) {
                Icon(Icons.Rounded.QrCodeScanner, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Scan Item", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
              }
              OutlinedButton(
                onClick = { onOpenReceiptSubmit(null) },
                modifier = Modifier
                  .weight(1f)
                  .height(48.dp)
                  .testTag("home_log_receipt_button"),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
                colors = ButtonDefaults.outlinedButtonColors(
                  containerColor = Color.White.copy(alpha = 0.10f),
                  contentColor = Color.White,
                ),
              ) {
                Icon(
                  Icons.AutoMirrored.Rounded.ReceiptLong,
                  contentDescription = null,
                  tint = Color.White,
                  modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text("Log Receipt", color = Color.White, style = MaterialTheme.typography.labelLarge)
              }
            }
          }
        }
      }
    }

    // Vibrant Color-Coded Bento Capability Dock
    item {
      val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        BentoActionTile(
          icon = Icons.Rounded.Search,
          label = "Discover",
          cardBg = if (isDark) Color(0xFF132846) else Color(0xFFDCEBFF),
          iconBg = CobaltInfo,
          borderColor = CobaltInfo.copy(alpha = 0.35f),
          onClick = onOpenDiscover,
          modifier = Modifier.weight(1f).testTag("home_bento_discover"),
        )
        BentoActionTile(
          icon = Icons.Rounded.AccountBalanceWallet,
          label = "Wallet",
          cardBg = if (isDark) Color(0xFF0E2F26) else Color(0xFFCCF8E6),
          iconBg = EmeraldTrust,
          borderColor = EmeraldTrust.copy(alpha = 0.35f),
          onClick = onOpenRewards,
          modifier = Modifier.weight(1f).testTag("home_bento_wallet"),
        )
        BentoActionTile(
          icon = Icons.AutoMirrored.Rounded.ReceiptLong,
          label = "Receipts",
          cardBg = if (isDark) Color(0xFF35200E) else Color(0xFFFFEBC7),
          iconBg = TangerineAccent,
          borderColor = TangerineAccent.copy(alpha = 0.35f),
          onClick = { onOpenReceiptSubmit(null) },
          modifier = Modifier.weight(1f).testTag("home_bento_receipts"),
        )
        BentoActionTile(
          icon = Icons.Rounded.History,
          label = "History",
          cardBg = if (isDark) Color(0xFF271944) else Color(0xFFEBDFFF),
          iconBg = VioletAccent,
          borderColor = VioletAccent.copy(alpha = 0.35f),
          onClick = onOpenScanHistory,
          modifier = Modifier.weight(1f).testTag("home_bento_history"),
        )
      }
    }

    // Quest & Mission Progress Card
    item {
      QuestMissionsSectionCard(
        missions = uiState.shopperMissions,
        reducedMotion = uiState.preferences.reducedMotionEnabled,
        onOpenScan = onOpenScan,
        onOpenReceiptSubmit = { onOpenReceiptSubmit(null) },
      )
    }

    // Activity Breakdown Card with Colorful Bars
    item {
      ScanActivityAnalyticsCard(
        scans = uiState.recentScans,
        chartTab = chartTab,
        onSelectTab = { chartTab = it },
      )
    }

    if (uiState.campaigns.isNotEmpty()) {
      item {
        SectionHeader(
          title = "Active Partner Campaigns",
          actionLabel = "All Rewards",
          onAction = onOpenRewards,
          accentColor = TangerineAccent,
        )
      }
      item {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
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
        title = "Recent Activity & Quick Re-scan",
        actionLabel = if (uiState.recentScans.isNotEmpty()) "Full History" else null,
        onAction = onOpenScanHistory,
        accentColor = VioletAccent,
      )
    }

    if (uiState.recentScans.isEmpty()) {
      item {
        IllustratedEmptyScanCard(
          onOpenScan = onOpenScan,
          onTryPopularCode = onOpenProduct,
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
        )
      }
    }

    if (uiState.cachedCatalog.isNotEmpty()) {
      item {
        SectionHeader(
          title = "Offline-Ready Catalog",
          actionLabel = "Search",
          onAction = onOpenDiscover,
          accentColor = CobaltInfo,
        )
      }
      items(uiState.cachedCatalog.take(3), key = { it.barcode }) { product ->
        ProductSummaryCard(
          product = product,
          onClick = { onOpenProduct(product.barcode) },
        )
      }
    }

    item { Spacer(Modifier.height(16.dp)) }
  }
}

@Composable
private fun IllustratedEmptyScanCard(
  onOpenScan: () -> Unit,
  onTryPopularCode: (String) -> Unit,
) {
  val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
  Card(
    shape = RoundedCornerShape(22.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    border = BorderStroke(1.dp, TealVivid.copy(alpha = 0.3f)),
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(
            Brush.horizontalGradient(
              if (isDark) {
                listOf(Color(0xFF0F2D27), Color(0xFF172C47))
              } else {
                listOf(Color(0xFFD5F9EC), Color(0xFFDBEAFE))
              },
            ),
          )
          .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        Image(
          painter = painterResource(id = R.drawable.img_scan_hero),
          contentDescription = "Scan any product barcode",
          modifier = Modifier
            .size(84.dp)
            .clip(RoundedCornerShape(16.dp)),
          contentScale = ContentScale.Crop,
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Text(
            "Ready to scan your first item",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            "Scan a retail barcode or tap a popular sample product below to inspect nutrition & compare live store prices.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
      Column(
        modifier = Modifier.padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
          Surface(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(10.dp))
              .clickable { onTryPopularCode("3017620422003") },
            color = CobaltSoftTint,
            shape = RoundedCornerShape(10.dp),
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center,
            ) {
              Icon(Icons.Rounded.Bolt, contentDescription = null, tint = CobaltInfo, modifier = Modifier.size(15.dp))
              Spacer(Modifier.width(4.dp))
              Text("Try Nutella", style = MaterialTheme.typography.labelSmall, color = CobaltInfo, fontWeight = FontWeight.Bold)
            }
          }
          Surface(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(10.dp))
              .clickable { onTryPopularCode("5449000000996") },
            color = VioletSoftTint,
            shape = RoundedCornerShape(10.dp),
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center,
            ) {
              Icon(Icons.Rounded.Bolt, contentDescription = null, tint = VioletAccent, modifier = Modifier.size(15.dp))
              Spacer(Modifier.width(4.dp))
              Text("Try Coca-Cola", style = MaterialTheme.typography.labelSmall, color = VioletAccent, fontWeight = FontWeight.Bold)
            }
          }
        }
        Button(
          onClick = onOpenScan,
          modifier = Modifier.fillMaxWidth().height(48.dp),
          shape = RoundedCornerShape(14.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = EmeraldTrust,
            contentColor = Color.White,
          ),
        ) {
          Icon(Icons.Rounded.QrCodeScanner, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(Modifier.width(8.dp))
          Text("Open Barcode Scanner", fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

@Composable
private fun AnchorPillCard(
  value: String,
  label: String,
  accentColor: Color,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier,
    color = Color.White.copy(alpha = 0.11f),
    shape = RoundedCornerShape(12.dp),
    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.14f)),
  ) {
    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
      Text(value, style = MaterialTheme.typography.titleMedium, color = accentColor, fontWeight = FontWeight.Bold)
      Text(label, style = MaterialTheme.typography.labelSmall, color = ObsidianMutedText, maxLines = 1)
    }
  }
}

@Composable
private fun BentoActionTile(
  icon: ImageVector,
  label: String,
  cardBg: Color,
  iconBg: Color,
  borderColor: Color,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier
      .clip(RoundedCornerShape(18.dp))
      .clickable(onClick = onClick),
    color = cardBg,
    shape = RoundedCornerShape(18.dp),
    border = BorderStroke(1.dp, borderColor),
  ) {
    Column(
      modifier = Modifier.padding(vertical = 14.dp, horizontal = 6.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Surface(
        color = iconBg,
        shape = RoundedCornerShape(12.dp),
      ) {
        Icon(
          icon,
          contentDescription = label,
          tint = Color.White,
          modifier = Modifier.padding(8.dp).size(20.dp),
        )
      }
      Text(
        label,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurface,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
      )
    }
  }
}

@Composable
private fun ScanActivityAnalyticsCard(
  scans: List<ScanRecord>,
  chartTab: Int,
  onSelectTab: (Int) -> Unit,
) {
  val barColors = listOf(CobaltInfo, TealVivid, EmeraldTrust, VioletAccent, CyanInfo, TangerineAccent, EmeraldTrust)
  Card(
    shape = RoundedCornerShape(22.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
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
        Column(modifier = Modifier.weight(1f)) {
          Text("Scan Pulse", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
          Text(
            "${scans.size} total scans recorded locally & in cloud",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          listOf("Recency", "Formats").forEachIndexed { idx, label ->
            val selected = chartTab == idx
            Surface(
              modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .clickable { onSelectTab(idx) },
              color = if (selected) DeepObsidian else MaterialTheme.colorScheme.surfaceVariant,
              shape = RoundedCornerShape(10.dp),
            ) {
              Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (selected) ElectricLime else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
              )
            }
          }
        }
      }

      if (chartTab == 0) {
        val nowMs = System.currentTimeMillis()
        val dayBuckets = (6 downTo 0).map { daysAgo ->
          val start = nowMs - (daysAgo + 1) * 86_400_000L
          val end = nowMs - daysAgo * 86_400_000L
          scans.count { scan ->
            val t = scan.scannedAt?.toDate()?.time ?: scan.localTimestampMs
            t in start..end
          }
        }
        val maxVal = maxOf(1, dayBuckets.maxOrNull() ?: 1)
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .height(82.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.Bottom,
        ) {
          dayBuckets.forEachIndexed { i, count ->
            val fraction = (count.toFloat() / maxVal.toFloat()).coerceIn(0.16f, 1f)
            val barColor = barColors[i % barColors.size]
            Column(
              modifier = Modifier.weight(1f),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.Bottom,
            ) {
              if (count > 0) {
                Text(
                  text = "$count",
                  style = MaterialTheme.typography.labelSmall,
                  color = barColor,
                  fontWeight = FontWeight.Bold,
                )
              }
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .height((46f * fraction).dp)
                  .clip(RoundedCornerShape(8.dp))
                  .background(
                    if (count > 0) {
                      Brush.verticalGradient(listOf(barColor, barColor.copy(alpha = 0.65f)))
                    } else {
                      Brush.verticalGradient(listOf(barColor.copy(alpha = 0.16f), barColor.copy(alpha = 0.08f)))
                    },
                  ),
              )
              Spacer(Modifier.height(4.dp))
              Text(
                text = if (i == 6) "Today" else "-${6 - i}d",
                style = MaterialTheme.typography.labelSmall,
                color = if (i == 6) EmeraldTrust else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (i == 6) FontWeight.Bold else FontWeight.Medium,
              )
            }
          }
        }
      } else {
        val formatCounts = scans.groupingBy { it.barcodeFormat.ifEmpty { "Unknown" } }
          .eachCount()
          .entries
          .sortedByDescending { it.value }
          .take(4)
        if (formatCounts.isEmpty()) {
          Text(
            "Scan items to view barcode symbology breakdown.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        } else {
          val maxFmt = maxOf(1, formatCounts.maxOf { it.value })
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            formatCounts.forEachIndexed { index, (fmt, count) ->
              val tint = barColors[index % barColors.size]
              Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Text(fmt, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                  Text("$count scans", style = MaterialTheme.typography.labelSmall, color = tint, fontWeight = FontWeight.Bold)
                }
                Box(
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(tint.copy(alpha = 0.15f)),
                ) {
                  Box(
                    modifier = Modifier
                      .fillMaxWidth((count.toFloat() / maxFmt.toFloat()).coerceIn(0.1f, 1f))
                      .height(6.dp)
                      .clip(RoundedCornerShape(3.dp))
                      .background(tint),
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
  Card(
    modifier = Modifier.width(268.dp),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    border = BorderStroke(1.dp, TealVivid.copy(alpha = 0.35f)),
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(Brush.horizontalGradient(listOf(EmeraldTrust, CobaltInfo)))
          .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          campaign.partnerName.uppercase(),
          style = MaterialTheme.typography.labelSmall,
          color = Color.White,
          fontWeight = FontWeight.Bold,
        )
        Surface(color = ElectricLime, shape = RoundedCornerShape(8.dp)) {
          Text(
            "+${campaign.rewardPoints} PTS",
            style = MaterialTheme.typography.labelSmall,
            color = ObsidianOnLime,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
          )
        }
      }
      Column(
        modifier = Modifier.padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Text(
          campaign.title,
          style = MaterialTheme.typography.titleMedium,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
        )
        Text(
          campaign.terms,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
        )
        Button(
          onClick = onSubmitReceipt,
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = EmeraldSoftTint,
            contentColor = EmeraldTrust,
          ),
        ) {
          Text("Claim with Receipt", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

@Composable
private fun RecentScanWithRescanCard(
  scan: ScanRecord,
  onInspect: () -> Unit,
  onRescan: () -> Unit,
) {
  var menuExpanded by remember { mutableStateOf(false) }

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onInspect),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
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
          Text(
            text = listOfNotNull(
              scan.brand.takeIf { it.isNotBlank() },
              scan.barcodeFormat.ifBlank { "GTIN" },
              scan.barcodeValue,
            ).joinToString(" • "),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }

        Box {
          IconButton(onClick = { menuExpanded = true }) {
            Icon(Icons.Rounded.MoreVert, contentDescription = "Scan options")
          }
          DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false },
          ) {
            DropdownMenuItem(
              text = { Text("Re-scan with ${scan.barcodeFormat.ifBlank { "product" }} context") },
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
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        Button(
          onClick = onRescan,
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
          ),
          contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        ) {
          Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(Modifier.width(4.dp))
          Text("Re-scan", fontWeight = FontWeight.SemiBold)
        }
      }
    }
  }
}
