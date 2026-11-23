package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.icons.rounded.ReceiptLong
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.Campaign
import com.example.data.model.ScanContext
import com.example.data.model.ScanRecord
import com.example.domain.MarketConfig
import com.example.ui.components.GuestModeBanner
import com.example.ui.components.ProductSummaryCard
import com.example.ui.components.SectionHeader
import com.example.ui.components.VerificationLabel
import com.example.ui.components.formatTimestamp
import com.example.ui.theme.DeepObsidian
import com.example.ui.theme.ElectricLime
import com.example.ui.theme.EmeraldSoftTint
import com.example.ui.theme.EmeraldTrust
import com.example.ui.theme.ObsidianOnLime
import com.example.ui.theme.SwatchMint
import com.example.ui.theme.SwatchOrchid
import com.example.ui.theme.SwatchPeach
import com.example.ui.theme.SwatchSky
import com.example.ui.theme.SwatchSunshine
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
    // Top Editorial Header
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "SCAN2MONEY • ${market.countryCode} (${market.currencyCode})",
            style = MaterialTheme.typography.labelSmall,
            color = EmeraldTrust,
          )
          Text(
            text = uiState.firebaseUser?.displayName?.let { "Welcome back, $it" } ?: "Product & Rewards Hub",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
          if (uiState.adminRole != null) {
            IconButton(onClick = onOpenAdmin) {
              Icon(
                Icons.Rounded.AdminPanelSettings,
                contentDescription = "Admin Console",
                tint = EmeraldTrust,
              )
            }
          }
          IconButton(onClick = onOpenNotifications) {
            Box {
              Icon(Icons.Rounded.Notifications, contentDescription = "Notifications")
              if (unreadCount > 0) {
                Box(
                  modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(EmeraldTrust),
                )
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

    // Hero Obsidian Command Card with Concrete Anchors
    item {
      Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DeepObsidian),
      ) {
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
                color = Color(0xFFA2B3AA),
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
              color = Color(0xFF192420),
              shape = RoundedCornerShape(12.dp),
            ) {
              Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.End,
              ) {
                Text(
                  text = "${uiState.wallet.pendingPoints} PTS",
                  style = MaterialTheme.typography.titleMedium,
                  color = SwatchSunshine,
                )
                Text(
                  text = "Pending review",
                  style = MaterialTheme.typography.labelSmall,
                  color = Color(0xFFA2B3AA),
                )
              }
            }
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            AnchorPillCard("$0", "to scan", modifier = Modifier.weight(1f))
            AnchorPillCard("< 2s", "catalog lookup", modifier = Modifier.weight(1f))
            AnchorPillCard("100p", "voucher start", modifier = Modifier.weight(1f))
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Button(
              onClick = onOpenScan,
              modifier = Modifier
                .weight(1f)
                .height(48.dp),
              shape = RoundedCornerShape(14.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = ElectricLime,
                contentColor = ObsidianOnLime,
              ),
            ) {
              Icon(Icons.Rounded.QrCodeScanner, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(Modifier.width(6.dp))
              Text("Scan Item", style = MaterialTheme.typography.labelLarge)
            }
            OutlinedButton(
              onClick = { onOpenReceiptSubmit(null) },
              modifier = Modifier
                .weight(1f)
                .height(48.dp),
              shape = RoundedCornerShape(14.dp),
              border = BorderStroke(1.dp, Color(0xFF31443D)),
            ) {
              Icon(
                Icons.Rounded.ReceiptLong,
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

    // Bento Capability Dock
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        BentoActionTile(
          icon = Icons.Rounded.Search,
          label = "Discover",
          bg = SwatchSky,
          onClick = onOpenDiscover,
          modifier = Modifier.weight(1f),
        )
        BentoActionTile(
          icon = Icons.Rounded.AccountBalanceWallet,
          label = "Wallet",
          bg = SwatchMint,
          onClick = onOpenRewards,
          modifier = Modifier.weight(1f),
        )
        BentoActionTile(
          icon = Icons.Rounded.ReceiptLong,
          label = "Receipts",
          bg = SwatchPeach,
          onClick = { onOpenReceiptSubmit(null) },
          modifier = Modifier.weight(1f),
        )
        BentoActionTile(
          icon = Icons.Rounded.QrCodeScanner,
          label = "History",
          bg = SwatchOrchid,
          onClick = onOpenScanHistory,
          modifier = Modifier.weight(1f),
        )
      }
    }

    // Activity Breakdown Card (Clean Compose bars, no clutter)
    item {
      ScanActivityAnalyticsCard(
        scans = uiState.recentScans,
        chartTab = chartTab,
        onSelectTab = { chartTab = it },
      )
    }

    // Active Campaigns
    if (uiState.campaigns.isNotEmpty()) {
      item {
        SectionHeader(
          title = "Active Partner Campaigns",
          actionLabel = "All Rewards",
          onAction = onOpenRewards,
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

    // Recent Scans with Re-scan Button & Context Menu
    item {
      SectionHeader(
        title = "Recent Activity & Quick Re-scan",
        actionLabel = if (uiState.recentScans.isNotEmpty()) "Full History" else null,
        onAction = onOpenScanHistory,
      )
    }

    if (uiState.recentScans.isEmpty()) {
      item {
        Card(
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Text("No scans yet", style = MaterialTheme.typography.titleMedium)
            Text(
              "Scan any retail barcode or test a sample EAN-13 code in the scanner to populate your quick Re-scan shelf.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(
              onClick = onOpenScan,
              shape = RoundedCornerShape(12.dp),
            ) {
              Text("Open Barcode Scanner")
            }
          }
        }
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

    // Featured Local Cached Catalog
    if (uiState.cachedCatalog.isNotEmpty()) {
      item {
        SectionHeader(
          title = "Offline-Ready Catalog",
          actionLabel = "Search",
          onAction = onOpenDiscover,
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
private fun AnchorPillCard(value: String, label: String, modifier: Modifier = Modifier) {
  Surface(
    modifier = modifier,
    color = Color(0xFF192420),
    shape = RoundedCornerShape(12.dp),
  ) {
    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
      Text(value, style = MaterialTheme.typography.titleMedium, color = ElectricLime)
      Text(label, style = MaterialTheme.typography.labelSmall, color = Color(0xFFA2B3AA), maxLines = 1)
    }
  }
}

@Composable
private fun BentoActionTile(
  icon: ImageVector,
  label: String,
  bg: Color,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier
      .clip(RoundedCornerShape(16.dp))
      .clickable(onClick = onClick),
    color = bg,
    shape = RoundedCornerShape(16.dp),
  ) {
    Column(
      modifier = Modifier.padding(vertical = 14.dp, horizontal = 8.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Icon(icon, contentDescription = label, tint = DeepObsidian, modifier = Modifier.size(22.dp))
      Text(label, style = MaterialTheme.typography.labelLarge, color = DeepObsidian, maxLines = 1)
    }
  }
}

@Composable
private fun ScanActivityAnalyticsCard(
  scans: List<ScanRecord>,
  chartTab: Int,
  onSelectTab: (Int) -> Unit,
) {
  Card(
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
        Column {
          Text("Scan Pulse", style = MaterialTheme.typography.titleMedium)
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
              modifier = Modifier.clickable { onSelectTab(idx) },
              color = if (selected) DeepObsidian else MaterialTheme.colorScheme.surfaceVariant,
              shape = RoundedCornerShape(8.dp),
            ) {
              Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (selected) ElectricLime else MaterialTheme.colorScheme.onSurfaceVariant,
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
            .height(68.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.Bottom,
        ) {
          dayBuckets.forEachIndexed { i, count ->
            val fraction = (count.toFloat() / maxVal.toFloat()).coerceIn(0.12f, 1f)
            Column(
              modifier = Modifier.weight(1f),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.Bottom,
            ) {
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .height((44f * fraction).dp)
                  .clip(RoundedCornerShape(6.dp))
                  .background(if (count > 0) EmeraldTrust else MaterialTheme.colorScheme.surfaceVariant),
              )
              Spacer(Modifier.height(4.dp))
              Text(
                text = if (i == 6) "Today" else "-${6 - i}d",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            formatCounts.forEach { (fmt, count) ->
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Text(fmt, style = MaterialTheme.typography.labelMedium)
                Text("$count scans", style = MaterialTheme.typography.labelSmall, color = EmeraldTrust)
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
    modifier = Modifier.width(260.dp),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
  ) {
    Column(
      modifier = Modifier.padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          campaign.partnerName.uppercase(),
          style = MaterialTheme.typography.labelSmall,
          color = EmeraldTrust,
        )
        Surface(color = EmeraldSoftTint, shape = RoundedCornerShape(8.dp)) {
          Text(
            "+${campaign.rewardPoints} PTS",
            style = MaterialTheme.typography.labelSmall,
            color = EmeraldTrust,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
          )
        }
      }
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
      OutlinedButton(
        onClick = onSubmitReceipt,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
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
) {
  var menuExpanded by remember { mutableStateOf(false) }

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onInspect),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
        OutlinedButton(
          onClick = onRescan,
          shape = RoundedCornerShape(12.dp),
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
