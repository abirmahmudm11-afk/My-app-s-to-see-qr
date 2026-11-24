package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ExitToApp
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.PrivacyTip
import androidx.compose.material.icons.rounded.QrCode2
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.ScanContext
import com.example.data.model.ScanRecord
import com.example.domain.MarketConfig
import com.example.ui.components.VerificationLabel
import com.example.ui.components.formatDetailedTimestamp
import com.example.ui.components.formatTimestamp
import com.example.ui.theme.AmberNotice
import com.example.ui.theme.AmberSoftTint
import com.example.ui.theme.CobaltInfo
import com.example.ui.theme.CobaltSoftTint
import com.example.ui.theme.CoralAccent
import com.example.ui.theme.CoralSoftTint
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.CrimsonSoftTint
import com.example.ui.theme.DeepObsidian
import com.example.ui.theme.ElectricLime
import com.example.ui.theme.EmeraldSoftTint
import com.example.ui.theme.EmeraldTrust
import com.example.ui.theme.HeaderTealVioletBrush
import com.example.ui.theme.HeroObsidianEmeraldBrush
import com.example.ui.theme.ObsidianMutedText
import com.example.ui.theme.ObsidianOnLime
import com.example.ui.theme.TangerineAccent
import com.example.ui.theme.TangerineSoftTint
import com.example.ui.theme.TealSoftTint
import com.example.ui.theme.TealVivid
import com.example.ui.theme.VioletAccent
import com.example.ui.theme.VioletSoftTint
import com.example.ui.viewmodel.MainUiState

@Composable
fun ProfileScreen(
  uiState: MainUiState,
  onOpenScanHistory: () -> Unit,
  onOpenNotifications: () -> Unit,
  onOpenCountryLanguage: () -> Unit,
  onOpenPrivacy: () -> Unit,
  onOpenTerms: () -> Unit,
  onOpenHelp: () -> Unit,
  onOpenAdmin: () -> Unit,
  onToggleDarkTheme: (Boolean) -> Unit,
  onToggleReducedMotion: (Boolean) -> Unit,
  onSignIn: () -> Unit,
  onSignOut: () -> Unit,
) {
  val market = MarketConfig.findByCountry(uiState.preferences.countryCode)
  val user = uiState.firebaseUser
  val displayName = user?.displayName?.takeIf { it.isNotBlank() }
    ?: user?.email?.substringBefore("@")
    ?: "Guest Shopper"
  val initials = displayName.split(" ").mapNotNull { it.firstOrNull()?.uppercase() }.take(2).joinToString("").ifEmpty { "G" }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    // Rich Gradient Profile Hero Banner
    item {
      Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .background(HeroObsidianEmeraldBrush)
            .padding(18.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
          ) {
            Box(
              modifier = Modifier
                .size(58.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(ElectricLime, EmeraldTrust))),
              contentAlignment = Alignment.Center,
            ) {
              Text(
                text = initials,
                style = MaterialTheme.typography.headlineMedium,
                color = ObsidianOnLime,
                fontWeight = FontWeight.Bold,
              )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
              Text(
                text = displayName,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
              )
              Text(
                text = user?.email ?: "Exploring in local Guest Mode",
                style = MaterialTheme.typography.bodySmall,
                color = ObsidianMutedText,
              )
            }
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            ProfileStatPill("${uiState.wallet.availablePoints} PTS", "Balance", ElectricLime, Modifier.weight(1f))
            ProfileStatPill("${uiState.recentScans.size}", "Scans", Color(0xFF38BDF8), Modifier.weight(1f))
            ProfileStatPill(market.countryCode, market.currencyCode, Color(0xFFA78BFA), Modifier.weight(1f))
          }

          if (user == null) {
            Button(
              onClick = onSignIn,
              modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("profile_sign_in_button"),
              shape = RoundedCornerShape(14.dp),
              colors = ButtonDefaults.buttonColors(containerColor = ElectricLime, contentColor = ObsidianOnLime),
            ) {
              Text("Continue with Google", fontWeight = FontWeight.Bold)
            }
          } else {
            OutlinedButton(
              onClick = onSignOut,
              modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .testTag("profile_sign_out_button"),
              shape = RoundedCornerShape(14.dp),
              border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
            ) {
              Icon(Icons.AutoMirrored.Rounded.ExitToApp, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
              Spacer(Modifier.width(8.dp))
              Text("Sign Out", color = Color.White)
            }
          }
        }
      }
    }

    if (uiState.adminRole != null) {
      item {
        ProfileNavTile(
          icon = Icons.Rounded.AdminPanelSettings,
          title = "Admin Control Center (${uiState.adminRole.roleName})",
          subtitle = "Users (${uiState.allUsers.size}) • Payouts (${uiState.allPayoutRequests.size}) • Receipts (${uiState.pendingReceipts.size})",
          iconBg = DeepObsidian,
          iconTint = ElectricLime,
          onClick = onOpenAdmin,
        )
      }
    }

    item {
      ProfileNavTile(
        icon = Icons.Rounded.History,
        title = "Scan History",
        subtitle = "${uiState.recentScans.size} Room & cloud barcode records",
        iconBg = VioletSoftTint,
        iconTint = VioletAccent,
        onClick = onOpenScanHistory,
      )
    }
    item {
      ProfileNavTile(
        icon = Icons.Rounded.Notifications,
        title = "Notifications",
        subtitle = "${uiState.notifications.count { !it.read }} unread alerts",
        iconBg = CoralSoftTint,
        iconTint = CoralAccent,
        onClick = onOpenNotifications,
      )
    }
    item {
      ProfileNavTile(
        icon = Icons.Rounded.Language,
        title = "Country & Language",
        subtitle = "${market.countryName} • ${market.currencyCode} (${market.languageLabel})",
        iconBg = CobaltSoftTint,
        iconTint = CobaltInfo,
        onClick = onOpenCountryLanguage,
      )
    }

    item {
      Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
      ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
              Surface(color = TealSoftTint, shape = RoundedCornerShape(10.dp)) {
                Icon(Icons.Rounded.DarkMode, contentDescription = null, tint = TealVivid, modifier = Modifier.padding(8.dp).size(20.dp))
              }
              Text("Obsidian Night Theme", style = MaterialTheme.typography.titleMedium)
            }
            Switch(
              checked = uiState.preferences.darkThemeEnabled,
              onCheckedChange = onToggleDarkTheme,
            )
          }
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text("Reduced Motion", style = MaterialTheme.typography.titleMedium)
              Text("Freezes scanner laser sweep, ripples & transitions", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(
              checked = uiState.preferences.reducedMotionEnabled,
              onCheckedChange = onToggleReducedMotion,
            )
          }
        }
      }
    }

    item {
      ProfileNavTile(
        icon = Icons.Rounded.PrivacyTip,
        title = "Privacy & Account Deletion",
        subtitle = "Marketing consent & deletion request flow",
        iconBg = CrimsonSoftTint,
        iconTint = CrimsonAlert,
        onClick = onOpenPrivacy,
      )
    }
    item {
      ProfileNavTile(
        icon = Icons.Rounded.Description,
        title = "Terms, Sandbox Rules & Attribution",
        subtitle = "Open Food Facts ODbL & point rules",
        iconBg = AmberSoftTint,
        iconTint = AmberNotice,
        onClick = onOpenTerms,
      )
    }
    item {
      ProfileNavTile(
        icon = Icons.AutoMirrored.Rounded.HelpOutline,
        title = "Help & Scanner Diagnostics",
        subtitle = "Supported formats & receipt review SLA",
        iconBg = EmeraldSoftTint,
        iconTint = EmeraldTrust,
        onClick = onOpenHelp,
      )
    }
  }
}

@Composable
private fun ProfileStatPill(
  value: String,
  label: String,
  accent: Color,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier,
    color = Color.White.copy(alpha = 0.11f),
    shape = RoundedCornerShape(12.dp),
    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.14f)),
  ) {
    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
      Text(value, style = MaterialTheme.typography.titleMedium, color = accent, fontWeight = FontWeight.Bold)
      Text(label, style = MaterialTheme.typography.labelSmall, color = ObsidianMutedText)
    }
  }
}

@Composable
private fun ProfileNavTile(
  icon: ImageVector,
  title: String,
  subtitle: String,
  iconBg: Color,
  iconTint: Color,
  onClick: () -> Unit,
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
  ) {
    Row(
      modifier = Modifier.padding(14.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Surface(color = iconBg, shape = RoundedCornerShape(12.dp)) {
        Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.padding(9.dp).size(20.dp))
      }
      Column(modifier = Modifier.weight(1f)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
      Icon(
        Icons.AutoMirrored.Rounded.KeyboardArrowRight,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}

@Composable
fun ScanHistoryScreen(
  uiState: MainUiState,
  isTopLevelTab: Boolean = false,
  onBack: () -> Unit,
  onOpenDiscover: () -> Unit = {},
  onOpenScanItem: (ScanRecord) -> Unit,
  onRescan: (ScanContext) -> Unit,
  onDeleteEntry: (Long) -> Unit,
  onClearAll: () -> Unit,
  onOpenScan: () -> Unit,
) {
  var query by remember { mutableStateOf("") }
  var selectedFilter by remember { mutableStateOf("All") }
  var showClearDialog by remember { mutableStateOf(false) }

  val matchedCount = uiState.recentScans.count { it.scanResult == "MATCHED" }
  val qrCount = uiState.recentScans.count {
    it.barcodeFormat.contains("QR", ignoreCase = true) || it.sourceProvider.startsWith("QR")
  }

  val filteredScans = remember(uiState.recentScans, query, selectedFilter) {
    val q = query.trim().lowercase()
    uiState.recentScans.filter { scan ->
      val matchesFilter = when (selectedFilter) {
        "Matched" -> scan.scanResult == "MATCHED"
        "QR" -> scan.barcodeFormat.contains("QR", ignoreCase = true) || scan.sourceProvider.startsWith("QR")
        "Unlisted" -> scan.scanResult == "NOT_FOUND"
        else -> true
      }
      val matchesQuery = q.isEmpty() ||
        scan.productName.lowercase().contains(q) ||
        scan.brand.lowercase().contains(q) ||
        scan.category.lowercase().contains(q) ||
        scan.barcodeValue.lowercase().contains(q)
      matchesFilter && matchesQuery
    }
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .testTag("scan_history_screen"),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    // Rich Gradient History Banner
    item {
      Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .background(HeaderTealVioletBrush)
            .padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
              if (!isTopLevelTab) {
                IconButton(onClick = onBack) {
                  Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
              }
              Column {
                Text(
                  "ROOM & CLOUD LEDGER",
                  style = MaterialTheme.typography.labelSmall,
                  color = ElectricLime,
                  fontWeight = FontWeight.Bold,
                )
                Text(
                  "Scan History (${uiState.recentScans.size})",
                  style = MaterialTheme.typography.headlineMedium,
                  color = Color.White,
                )
              }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              Surface(color = Color.White.copy(alpha = 0.16f), shape = CircleShape) {
                IconButton(
                  onClick = onOpenDiscover,
                  modifier = Modifier.testTag("history_discover_button"),
                ) {
                  Icon(Icons.Rounded.Search, contentDescription = "Discover Catalog", tint = Color.White)
                }
              }
              if (uiState.localScanHistory.isNotEmpty()) {
                Surface(color = Color.White.copy(alpha = 0.16f), shape = CircleShape) {
                  IconButton(
                    onClick = { showClearDialog = true },
                    modifier = Modifier.testTag("clear_history_button"),
                  ) {
                    Icon(Icons.Rounded.DeleteSweep, contentDescription = "Clear Scan History", tint = Color(0xFFFFB4BE))
                  }
                }
              }
            }
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            ProfileStatPill("${uiState.recentScans.size}", "Total", ElectricLime, Modifier.weight(1f))
            ProfileStatPill("$matchedCount", "Matched", Color(0xFF6EE7B7), Modifier.weight(1f))
            ProfileStatPill("$qrCount", "QR Codes", Color(0xFFFDE68A), Modifier.weight(1f))
          }
        }
      }
    }

    item {
      OutlinedTextField(
        value = query,
        onValueChange = { query = it },
        placeholder = { Text("Search product, brand, category, or barcode") },
        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = VioletAccent) },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("history_search_input"),
      )
    }

    item {
      val filterColors = mapOf(
        "All" to (DeepObsidian to ElectricLime),
        "Matched" to (EmeraldTrust to Color.White),
        "QR" to (VioletAccent to Color.White),
        "Unlisted" to (TangerineAccent to Color.White),
      )
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        listOf("All", "Matched", "QR", "Unlisted").forEach { filter ->
          val selected = selectedFilter == filter
          val (selBg, selFg) = filterColors[filter] ?: (DeepObsidian to ElectricLime)
          Surface(
            modifier = Modifier
              .clip(RoundedCornerShape(10.dp))
              .clickable { selectedFilter = filter },
            color = if (selected) selBg else MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(10.dp),
          ) {
            Text(
              text = filter,
              style = MaterialTheme.typography.labelMedium,
              color = if (selected) selFg else MaterialTheme.colorScheme.onSurfaceVariant,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            )
          }
        }
      }
    }

    if (filteredScans.isEmpty()) {
      item {
        val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
        Card(
          shape = RoundedCornerShape(22.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = BorderStroke(1.dp, VioletAccent.copy(alpha = 0.35f)),
        ) {
          Column(modifier = Modifier.fillMaxWidth()) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .background(
                  Brush.horizontalGradient(
                    if (isDark) listOf(Color(0xFF251942), Color(0xFF13263F)) else listOf(VioletSoftTint, CobaltSoftTint),
                  ),
                )
                .padding(16.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
              Image(
                painter = painterResource(id = R.drawable.img_scan_hero),
                contentDescription = "Empty scan history",
                modifier = Modifier
                  .size(76.dp)
                  .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop,
              )
              Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                  text = if (uiState.recentScans.isEmpty()) "No barcode scans logged yet" else "No matching history entries",
                  style = MaterialTheme.typography.titleMedium,
                )
                Text(
                  text = "Every barcode or QR code you scan is saved automatically to Room with its product details and timestamp.",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            }
            Box(modifier = Modifier.padding(14.dp)) {
              Button(
                onClick = onOpenScan,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VioletAccent, contentColor = Color.White),
              ) {
                Icon(Icons.Rounded.QrCodeScanner, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Open Scanner", fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }
    } else {
      items(filteredScans, key = { "${it.id}_${it.barcodeValue}_${it.localTimestampMs}" }) { scan ->
        val isQr = scan.barcodeFormat.contains("QR", ignoreCase = true)
        val badgeBg = if (isQr) VioletSoftTint else CobaltSoftTint
        val badgeFg = if (isQr) VioletAccent else CobaltInfo
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenScanItem(scan) }
            .testTag("history_item_${scan.barcodeValue}"),
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
              horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              if (scan.imageUrl.isNotBlank()) {
                AsyncImage(
                  model = scan.imageUrl,
                  contentDescription = scan.productName,
                  modifier = Modifier
                    .size(58.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(badgeBg),
                  contentScale = ContentScale.Crop,
                )
              } else {
                Box(
                  modifier = Modifier
                    .size(58.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Brush.linearGradient(listOf(badgeFg, TealVivid))),
                  contentAlignment = Alignment.Center,
                ) {
                  Icon(
                    Icons.Rounded.QrCode2,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp),
                  )
                }
              }

              Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                  text = scan.productName.ifBlank { scan.barcodeValue },
                  style = MaterialTheme.typography.titleMedium,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                  Surface(color = badgeBg, shape = RoundedCornerShape(6.dp)) {
                    Text(
                      text = scan.barcodeFormat.ifBlank { "GTIN" },
                      style = MaterialTheme.typography.labelSmall,
                      color = badgeFg,
                      fontWeight = FontWeight.Bold,
                      modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                  }
                  Text(
                    text = listOfNotNull(
                      scan.brand.takeIf { it.isNotBlank() },
                      scan.barcodeValue,
                    ).joinToString(" • "),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                  )
                }
                Text(
                  text = "Scanned ${formatDetailedTimestamp(scan.scannedAt, scan.localTimestampMs)}",
                  style = MaterialTheme.typography.labelSmall,
                  color = EmeraldTrust,
                  fontWeight = FontWeight.Medium,
                )
              }

              scan.localHistoryId?.let { localId ->
                IconButton(
                  onClick = { onDeleteEntry(localId) },
                  modifier = Modifier.testTag("delete_history_$localId"),
                ) {
                  Icon(
                    Icons.Rounded.Delete,
                    contentDescription = "Delete scan entry",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                  )
                }
              }
            }

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              VerificationLabel(statusRaw = scan.verificationStatus)
              Button(
                onClick = {
                  onRescan(
                    ScanContext(
                      barcodeValue = scan.barcodeValue,
                      barcodeFormat = scan.barcodeFormat,
                      productName = scan.productName,
                      brand = scan.brand,
                    ),
                  )
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                  containerColor = MaterialTheme.colorScheme.primaryContainer,
                  contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
              ) {
                Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Re-scan", fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }
    }
  }

  if (showClearDialog) {
    AlertDialog(
      onDismissRequest = { showClearDialog = false },
      title = { Text("Clear Local Scan History?") },
      text = {
        Text("This removes all saved local barcode history entries stored in your Room database.")
      },
      confirmButton = {
        Button(
          onClick = {
            showClearDialog = false
            onClearAll()
          },
          colors = ButtonDefaults.buttonColors(containerColor = CrimsonAlert),
        ) {
          Text("Clear History")
        }
      },
      dismissButton = {
        TextButton(onClick = { showClearDialog = false }) { Text("Cancel") }
      },
    )
  }
}

@Composable
fun NotificationsScreen(
  uiState: MainUiState,
  onBack: () -> Unit,
  onMarkRead: (String) -> Unit,
) {
  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    item {
      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back") }
        Text("Notifications", style = MaterialTheme.typography.titleLarge)
      }
    }
    if (uiState.notifications.isEmpty()) {
      item { Text("No notifications right now.", style = MaterialTheme.typography.bodyMedium) }
    } else {
      items(uiState.notifications, key = { it.id }) { item ->
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { if (!item.read) onMarkRead(item.id) },
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(
            containerColor = if (item.read) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
          ),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        ) {
          Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(item.title, style = MaterialTheme.typography.titleMedium)
            Text(item.body, style = MaterialTheme.typography.bodySmall)
            Text(formatTimestamp(item.createdAt), style = MaterialTheme.typography.labelSmall)
          }
        }
      }
    }
  }
}

@Composable
fun CountryLanguageScreen(
  currentCountryCode: String,
  onBack: () -> Unit,
  onSelectMarket: (countryCode: String, languageCode: String) -> Unit,
) {
  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    item {
      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back") }
        Text("Country & Currency", style = MaterialTheme.typography.titleLarge)
      }
    }
    items(MarketConfig.SUPPORTED_MARKETS) { market ->
      val selected = currentCountryCode.equals(market.countryCode, ignoreCase = true)
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { onSelectMarket(market.countryCode, market.defaultLanguage) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(if (selected) 1.5.dp else 1.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline),
      ) {
        Row(
          modifier = Modifier.padding(16.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Column {
            Text("${market.countryName} (${market.countryCode})", style = MaterialTheme.typography.titleMedium)
            Text("${market.currencyCode} • ${market.languageLabel}", style = MaterialTheme.typography.bodySmall)
          }
          if (selected) {
            Icon(Icons.Rounded.CheckCircle, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary)
          }
        }
      }
    }
  }
}

@Composable
fun PrivacySettingsScreen(
  uiState: MainUiState,
  onBack: () -> Unit,
  onToggleMarketing: (Boolean) -> Unit,
  onRequestDeletion: (String) -> Unit,
) {
  var reason by remember { mutableStateOf("") }
  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back") }
      Text("Privacy & Data Controls", style = MaterialTheme.typography.titleLarge)
    }
    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text("Campaign & Partner Updates", style = MaterialTheme.typography.titleMedium)
          Text("Optional notices when new eligible retail offers launch", style = MaterialTheme.typography.bodySmall)
        }
        Switch(
          checked = uiState.preferences.marketingConsent,
          onCheckedChange = onToggleMarketing,
        )
      }
    }

    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      border = BorderStroke(1.dp, CrimsonAlert.copy(alpha = 0.4f)),
    ) {
      Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Request Account Deletion", style = MaterialTheme.typography.titleMedium, color = CrimsonAlert)
        Text(
          "Submits an auditable deletion request for your Firestore user profile and marks your account status as DELETION_REQUESTED.",
          style = MaterialTheme.typography.bodySmall,
        )
        OutlinedTextField(
          value = reason,
          onValueChange = { reason = it },
          label = { Text("Optional reason") },
          modifier = Modifier.fillMaxWidth(),
        )
        Button(
          onClick = { onRequestDeletion(reason) },
          enabled = uiState.firebaseUser != null,
          colors = ButtonDefaults.buttonColors(containerColor = CrimsonAlert),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Icon(Icons.Rounded.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(Modifier.width(8.dp))
          Text(if (uiState.firebaseUser == null) "Sign In Required" else "Submit Deletion Request")
        }
      }
    }
  }
}

@Composable
fun TermsRulesScreen(onBack: () -> Unit) {
  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    item {
      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back") }
        Text("Terms & Attribution", style = MaterialTheme.typography.titleLarge)
      }
    }
    item {
      Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text("1. Open Food Facts Attribution", style = MaterialTheme.typography.titleMedium)
          Text(
            "Product metadata retrieved from Open Food Facts is provided under the Open Database License (ODbL) by Open Food Facts contributors.",
            style = MaterialTheme.typography.bodySmall,
          )
          Text("2. Verification Tiers", style = MaterialTheme.typography.titleMedium)
          Text(
            "Every product & store offer explicitly declares whether it comes from a Verified Merchant, Licensed Provider, Open Food Facts, or Unverified Community Submission.",
            style = MaterialTheme.typography.bodySmall,
          )
          Text("3. Sandbox Rewards Ledger", style = MaterialTheme.typography.titleMedium)
          Text(
            "Points (PTS) inside SCAN2MONEY operate in Sandbox Rewards Mode and require human admin review of SHA-256 hashed receipt submissions before credit.",
            style = MaterialTheme.typography.bodySmall,
          )
        }
      }
    }
  }
}

@Composable
fun HelpSupportScreen(onBack: () -> Unit) {
  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    item {
      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back") }
        Text("Help & Scanner Guide", style = MaterialTheme.typography.titleLarge)
      }
    }
    item {
      Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text("Supported Barcode Symbologies (13)", style = MaterialTheme.typography.titleMedium)
          Text(
            "EAN-13, EAN-8, UPC-A, UPC-E, Code 128, Code 39, Code 93, QR Code, Data Matrix, PDF417, Aztec, ITF, Codabar + Manual GS1 Modulo-10 GTIN entry.",
            style = MaterialTheme.typography.bodySmall,
          )
          Text("Tips for Low Light or Damaged Labels", style = MaterialTheme.typography.titleMedium)
          Text(
            "Use the Torch icon on supported devices, tap Photo Library to scan a saved label photo, or tap Manual GTIN to enter digits directly.",
            style = MaterialTheme.typography.bodySmall,
          )
        }
      }
    }
  }
}
