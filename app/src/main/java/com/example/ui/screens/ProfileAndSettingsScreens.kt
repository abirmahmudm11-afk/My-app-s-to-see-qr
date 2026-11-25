package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.example.ui.components.AnimatedButton
import com.example.ui.components.AnimatedIconButton
import com.example.ui.components.AnimatedOutlinedButton
import com.example.ui.components.AnimatedTextButton
import com.example.ui.components.SlidingSegmentedPillBar
import com.example.ui.components.VerificationLabel
import com.example.ui.components.atmosphericBackdrop
import com.example.ui.components.bounceClickable
import com.example.ui.components.formatDetailedTimestamp
import com.example.ui.components.formatTimestamp
import com.example.ui.components.sharedProductBounds
import com.example.ui.theme.CobaltAccent
import com.example.ui.theme.HeroMintVaultBrush
import com.example.ui.theme.LedgerDeckBrush
import com.example.ui.theme.LedgerJadeDark
import com.example.ui.theme.MintGlowBorderBrush
import com.example.ui.theme.MintOnDarkText
import com.example.ui.theme.SignalIndigo
import com.example.ui.theme.SignalIndigoDark
import com.example.ui.theme.TabularDataStyle
import com.example.ui.theme.ViewfinderMuted
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
        Text("Profile & Settings", style = MaterialTheme.typography.displayMedium)
      }

      // Account Identity Deck
      item {
        Surface(
          shape = RoundedCornerShape(22.dp),
          color = Color.Transparent,
          border = BorderStroke(1.2.dp, MintGlowBorderBrush),
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .background(HeroMintVaultBrush)
              .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
              Surface(
                modifier = Modifier.size(54.dp),
                shape = CircleShape,
                color = SignalIndigoDark.copy(alpha = 0.2f),
                border = BorderStroke(1.5.dp, SignalIndigoDark),
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Text(
                    text = initials,
                    style = MaterialTheme.typography.titleLarge,
                    color = SignalIndigoDark,
                  )
                }
              }
              Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                  text = displayName,
                  style = MaterialTheme.typography.titleLarge,
                  color = Color.White,
                )
                Text(
                  text = user?.email ?: "Local guest mode · on-device storage",
                  style = MaterialTheme.typography.bodySmall,
                  color = ViewfinderMuted,
                )
              }
            }

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              ProfileStatCell("${uiState.wallet.availablePoints} PTS", "Balance", SignalIndigoDark, Modifier.weight(1f))
              ProfileStatCell("${uiState.recentScans.size}", "Scans", CobaltAccent, Modifier.weight(1f))
              ProfileStatCell(market.countryCode, market.currencyCode, Color.White, Modifier.weight(1f))
            }

            if (user == null) {
              AnimatedButton(
                onClick = onSignIn,
                modifier = Modifier
                  .fillMaxWidth()
                  .height(50.dp)
                  .testTag("profile_sign_in_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SignalIndigoDark, contentColor = MintOnDarkText),
              ) {
                Text("Continue with Google", fontWeight = FontWeight.Bold)
              }
            } else {
              AnimatedOutlinedButton(
                onClick = onSignOut,
                modifier = Modifier
                  .fillMaxWidth()
                  .height(50.dp)
                  .testTag("profile_sign_out_button"),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.28f)),
                colors = ButtonDefaults.outlinedButtonColors(
                  containerColor = Color.White.copy(alpha = 0.08f),
                  contentColor = Color.White,
                ),
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
          Surface(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(14.dp))
              .bounceClickable(onClick = onOpenAdmin),
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)),
          ) {
            SettingsRowItem(
              icon = Icons.Rounded.AdminPanelSettings,
              title = "Admin Control Center (${uiState.adminRole.roleName})",
              subtitle = "Users (${uiState.allUsers.size}) · Payouts (${uiState.allPayoutRequests.size}) · Receipts (${uiState.pendingReceipts.size})",
            )
          }
        }
      }

      // Grouped Settings: Activity & Region
      item {
        Surface(
          shape = RoundedCornerShape(18.dp),
          color = MaterialTheme.colorScheme.surface,
          border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
        ) {
          Column {
            Box(modifier = Modifier.clickable(onClick = onOpenScanHistory)) {
              SettingsRowItem(
                icon = Icons.Rounded.History,
                title = "Scan History",
                subtitle = "${uiState.recentScans.size} Room & cloud barcode records",
              )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            Box(modifier = Modifier.clickable(onClick = onOpenNotifications)) {
              SettingsRowItem(
                icon = Icons.Rounded.Notifications,
                title = "Notifications",
                subtitle = "${uiState.notifications.count { !it.read }} unread alerts",
              )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            Box(modifier = Modifier.clickable(onClick = onOpenCountryLanguage)) {
              SettingsRowItem(
                icon = Icons.Rounded.Language,
                title = "Country & Currency",
                subtitle = "${market.countryName} · ${market.currencyCode} (${market.languageLabel})",
              )
            }
          }
        }
      }

      // Grouped Settings: Display & Motion
      item {
        Surface(
          shape = RoundedCornerShape(18.dp),
          color = MaterialTheme.colorScheme.surface,
          border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
        ) {
          Column {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
              ) {
                Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(8.dp)) {
                  Icon(
                    Icons.Rounded.DarkMode,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(8.dp).size(18.dp),
                  )
                }
                Text("Dark Appearance", style = MaterialTheme.typography.titleMedium)
              }
              Switch(
                checked = uiState.preferences.darkThemeEnabled,
                onCheckedChange = onToggleDarkTheme,
              )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Reduce Motion", style = MaterialTheme.typography.titleMedium)
                Text(
                  text = "Freezes scanner laser sweep and screen animations",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
              Switch(
                checked = uiState.preferences.reducedMotionEnabled,
                onCheckedChange = onToggleReducedMotion,
              )
            }
          }
        }
      }

      // Grouped Settings: Legal & Support
      item {
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = MaterialTheme.colorScheme.surface,
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        ) {
          Column {
            Box(modifier = Modifier.clickable(onClick = onOpenPrivacy)) {
              SettingsRowItem(
                icon = Icons.Rounded.PrivacyTip,
                title = "Privacy & Account Deletion",
                subtitle = "Marketing consent & deletion request flow",
              )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            Box(modifier = Modifier.clickable(onClick = onOpenTerms)) {
              SettingsRowItem(
                icon = Icons.Rounded.Description,
                title = "Terms & Attribution",
                subtitle = "Open Food Facts ODbL & point rules",
              )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            Box(modifier = Modifier.clickable(onClick = onOpenHelp)) {
              SettingsRowItem(
                icon = Icons.AutoMirrored.Rounded.HelpOutline,
                title = "Help & Scanner Guide",
                subtitle = "Supported formats & troubleshooting",
              )
            }
          }
        }
      }

      item { Spacer(Modifier.height(12.dp)) }
    }
  }
}

@Composable
private fun ProfileStatCell(
  value: String,
  label: String,
  accent: Color,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier,
    color = Color.White.copy(alpha = 0.08f),
    shape = RoundedCornerShape(10.dp),
  ) {
    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
      Text(value, style = TabularDataStyle.copy(fontWeight = FontWeight.Bold), color = accent)
      Text(label, style = MaterialTheme.typography.labelSmall, color = ViewfinderMuted)
    }
  }
}

@Composable
private fun SettingsRowItem(
  icon: ImageVector,
  title: String,
  subtitle: String,
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 14.dp, vertical = 13.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(8.dp)) {
      Icon(
        icon,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(8.dp).size(18.dp),
      )
    }
    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
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

  val reducedMotion = uiState.preferences.reducedMotionEnabled
  val matchedCount = remember(uiState.recentScans) {
    uiState.recentScans.count { it.scanResult == "MATCHED" }
  }
  val qrCount = remember(uiState.recentScans) {
    uiState.recentScans.count {
      it.barcodeFormat.contains("QR", ignoreCase = true) || it.sourceProvider.startsWith("QR")
    }
  }
  val animatedTotalCount by animateIntAsState(
    targetValue = uiState.recentScans.size,
    animationSpec = tween(if (reducedMotion) 0 else 240, easing = FastOutSlowInEasing),
    label = "historyTotalCount",
  )
  val animatedMatchedCount by animateIntAsState(
    targetValue = matchedCount,
    animationSpec = tween(if (reducedMotion) 0 else 240, easing = FastOutSlowInEasing),
    label = "historyMatchedCount",
  )
  val animatedQrCount by animateIntAsState(
    targetValue = qrCount,
    animationSpec = tween(if (reducedMotion) 0 else 240, easing = FastOutSlowInEasing),
    label = "historyQrCount",
  )

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

  BoxWithConstraints(
    modifier = Modifier
      .fillMaxSize()
      .atmosphericBackdrop(),
    contentAlignment = Alignment.TopCenter,
  ) {
    val isWide = maxWidth >= 680.dp

    LazyColumn(
      modifier = Modifier
        .widthIn(max = 860.dp)
        .fillMaxSize()
        .testTag("scan_history_screen"),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            if (!isTopLevelTab) {
              AnimatedIconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
              }
            }
            Column {
              Text("Scan History", style = MaterialTheme.typography.displayMedium)
              Text(
                text = "Room local database & synced records",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Surface(
              color = MaterialTheme.colorScheme.surface,
              shape = CircleShape,
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            ) {
              AnimatedIconButton(
                onClick = onOpenDiscover,
                modifier = Modifier.testTag("history_discover_button"),
              ) {
                Icon(Icons.Rounded.Search, contentDescription = "Discover Catalog")
              }
            }
            if (uiState.localScanHistory.isNotEmpty()) {
              Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = CircleShape,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
              ) {
                AnimatedIconButton(
                  onClick = { showClearDialog = true },
                  modifier = Modifier.testTag("clear_history_button"),
                ) {
                  Icon(Icons.Rounded.DeleteSweep, contentDescription = "Clear Scan History", tint = MaterialTheme.colorScheme.error)
                }
              }
            }
          }
        }
      }

      // Clean Tabular History Counter Strip
      item {
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = MaterialTheme.colorScheme.surface,
          border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 12.dp),
          ) {
            HistoryMetricCell("$animatedTotalCount", "Total", Modifier.weight(1f))
            HistoryMetricCell("$animatedMatchedCount", "Matched", Modifier.weight(1f))
            HistoryMetricCell("$animatedQrCount", "QR Codes", Modifier.weight(1f))
          }
        }
      }

      item {
        OutlinedTextField(
          value = query,
          onValueChange = { query = it },
          placeholder = { Text("Filter by product, brand, or barcode") },
          leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
          singleLine = true,
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("history_search_input"),
        )
      }

      item {
        val filters = listOf("All", "Matched", "QR", "Unlisted")
        SlidingSegmentedPillBar(
          options = filters,
          selectedIndex = filters.indexOf(selectedFilter).coerceAtLeast(0),
          onSelect = { idx -> selectedFilter = filters[idx] },
          modifier = Modifier.fillMaxWidth(),
        )
      }

      if (filteredScans.isEmpty()) {
        item {
          Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
          ) {
            Column(
              modifier = Modifier.padding(16.dp),
              verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
              ) {
                Image(
                  painter = painterResource(id = R.drawable.img_scan_hero),
                  contentDescription = "Empty scan history",
                  modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(10.dp)),
                  contentScale = ContentScale.Crop,
                )
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                  Text(
                    text = if (uiState.recentScans.isEmpty()) "No scans logged yet" else "No matching entries",
                    style = MaterialTheme.typography.titleMedium,
                  )
                  Text(
                    text = "Scanned barcodes and QR codes are saved locally in Room with their timestamp and product metadata.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }
              }
              AnimatedButton(
                onClick = onOpenScan,
                modifier = Modifier
                  .fillMaxWidth()
                  .height(48.dp),
                shape = RoundedCornerShape(10.dp),
              ) {
                Icon(Icons.Rounded.QrCodeScanner, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Open Scanner")
              }
            }
          }
        }
      } else if (isWide) {
        items(filteredScans.chunked(2), key = { row -> row.joinToString("_") { "${it.id}_${it.barcodeValue}_${it.localTimestampMs}" } }) { rowItems ->
          Row(
            modifier = Modifier
              .animateItem()
              .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            rowItems.forEach { scan ->
              ScanHistoryItemCard(
                scan = scan,
                onOpenScanItem = onOpenScanItem,
                onDeleteEntry = onDeleteEntry,
                onRescan = onRescan,
                modifier = Modifier.weight(1f),
              )
            }
            if (rowItems.size == 1) {
              Spacer(Modifier.weight(1f))
            }
          }
        }
      } else {
        items(filteredScans, key = { "${it.id}_${it.barcodeValue}_${it.localTimestampMs}" }) { scan ->
          ScanHistoryItemCard(
            scan = scan,
            onOpenScanItem = onOpenScanItem,
            onDeleteEntry = onDeleteEntry,
            onRescan = onRescan,
            modifier = Modifier
              .animateItem()
              .fillMaxWidth(),
          )
        }
      }
    }
  }

  if (showClearDialog) {
    AlertDialog(
      onDismissRequest = { showClearDialog = false },
      title = { Text("Clear Local Scan History?") },
      text = {
        Text("This removes all saved barcode entries from your device's Room database.")
      },
      confirmButton = {
        AnimatedButton(
          onClick = {
            showClearDialog = false
            onClearAll()
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
        ) {
          Text("Clear History")
        }
      },
      dismissButton = {
        AnimatedTextButton(onClick = { showClearDialog = false }) { Text("Cancel") }
      },
    )
  }
}

@Composable
private fun ScanHistoryItemCard(
  scan: ScanRecord,
  onOpenScanItem: (ScanRecord) -> Unit,
  onDeleteEntry: (Long) -> Unit,
  onRescan: (ScanContext) -> Unit,
  modifier: Modifier = Modifier,
) {
  val isQr = scan.barcodeFormat.contains("QR", ignoreCase = true)
  Surface(
    modifier = modifier
      .sharedProductBounds("product_card_${scan.barcodeValue}")
      .clip(RoundedCornerShape(16.dp))
      .bounceClickable { onOpenScanItem(scan) }
      .testTag("history_item_${scan.barcodeValue}"),
    shape = RoundedCornerShape(16.dp),
    color = MaterialTheme.colorScheme.surface,
    border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
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
              .size(52.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(MaterialTheme.colorScheme.surfaceVariant),
            contentScale = ContentScale.Crop,
          )
        } else {
          Box(
            modifier = Modifier
              .size(52.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              Icons.Rounded.QrCode2,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(24.dp),
            )
          }
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
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
            Surface(
              color = if (isQr) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
              shape = RoundedCornerShape(4.dp),
            ) {
              Text(
                text = scan.barcodeFormat.ifBlank { "GTIN" },
                style = MaterialTheme.typography.labelSmall,
                color = if (isQr) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
              )
            }
            Text(
              text = listOfNotNull(
                scan.brand.takeIf { it.isNotBlank() },
                scan.barcodeValue,
              ).joinToString(" · "),
              style = TabularDataStyle,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }
          Text(
            text = "Scanned ${formatDetailedTimestamp(scan.scannedAt, scan.localTimestampMs)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }

        scan.localHistoryId?.let { localId ->
          AnimatedIconButton(
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

      val metaTags = listOfNotNull(
        scan.category.takeIf { it.isNotBlank() },
        scan.quantity.takeIf { it.isNotBlank() }?.let { "Size: $it" },
        scan.nutriscoreGrade.takeIf { it.isNotBlank() }?.let { "Nutri-Score: $it" },
        scan.sourceProvider.takeIf { it.isNotBlank() && it != "None" },
      )
      if (metaTags.isNotEmpty()) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          metaTags.forEach { tag ->
            Surface(
              color = MaterialTheme.colorScheme.surfaceVariant,
              shape = RoundedCornerShape(6.dp),
            ) {
              Text(
                text = tag,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
              )
            }
          }
        }
      }

      if (scan.description.isNotBlank()) {
        Text(
          text = scan.description,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
        )
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        VerificationLabel(statusRaw = scan.verificationStatus)
        AnimatedOutlinedButton(
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
          shape = RoundedCornerShape(8.dp),
          contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        ) {
          Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(Modifier.width(4.dp))
          Text("Re-scan")
        }
      }
    }
  }
}

@Composable
private fun HistoryMetricCell(
  value: String,
  label: String,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(2.dp),
  ) {
    Text(
      text = value,
      style = TabularDataStyle.copy(
        fontSize = MaterialTheme.typography.titleLarge.fontSize,
        fontWeight = FontWeight.Bold,
      ),
      color = MaterialTheme.colorScheme.onSurface,
    )
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
  }
}

@Composable
fun NotificationsScreen(
  uiState: MainUiState,
  onBack: () -> Unit,
  onMarkRead: (String) -> Unit,
) {
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
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      item {
        Row(verticalAlignment = Alignment.CenterVertically) {
          AnimatedIconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back") }
          Text("Notifications", style = MaterialTheme.typography.titleLarge)
        }
      }
      if (uiState.notifications.isEmpty()) {
        item { Text("No notifications right now.", style = MaterialTheme.typography.bodyMedium) }
      } else {
        items(uiState.notifications, key = { it.id }) { item ->
          Surface(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .bounceClickable { if (!item.read) onMarkRead(item.id) },
            shape = RoundedCornerShape(12.dp),
            color = if (item.read) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
          ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
              Text(item.title, style = MaterialTheme.typography.titleMedium)
              Text(item.body, style = MaterialTheme.typography.bodySmall)
              Text(formatTimestamp(item.createdAt), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
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
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      item {
        Row(verticalAlignment = Alignment.CenterVertically) {
          AnimatedIconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back") }
          Text("Country & Currency", style = MaterialTheme.typography.titleLarge)
        }
      }
      items(MarketConfig.SUPPORTED_MARKETS) { market ->
        val selected = currentCountryCode.equals(market.countryCode, ignoreCase = true)
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .bounceClickable { onSelectMarket(market.countryCode, market.defaultLanguage) },
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.surface,
          border = BorderStroke(
            width = if (selected) 1.5.dp else 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
          ),
        ) {
          Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Column {
              Text("${market.countryName} (${market.countryCode})", style = MaterialTheme.typography.titleMedium)
              Text("${market.currencyCode} · ${market.languageLabel}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (selected) {
              Icon(Icons.Rounded.CheckCircle, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary)
            }
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
        AnimatedIconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back") }
        Text("Privacy & Data Controls", style = MaterialTheme.typography.titleLarge)
      }
      Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
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
            Text(
              text = "Optional notices when new retail offers launch",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
          Switch(
            checked = uiState.preferences.marketingConsent,
            onCheckedChange = onToggleMarketing,
          )
        }
      }

      Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.45f)),
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text("Request Account Deletion", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
          Text(
            text = "Submits an auditable deletion request for your Firestore user profile and marks your account as DELETION_REQUESTED.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          OutlinedTextField(
            value = reason,
            onValueChange = { reason = it },
            label = { Text("Optional reason") },
            modifier = Modifier.fillMaxWidth(),
          )
          AnimatedButton(
            onClick = { onRequestDeletion(reason) },
            enabled = uiState.firebaseUser != null,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            shape = RoundedCornerShape(10.dp),
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
}

@Composable
fun TermsRulesScreen(onBack: () -> Unit) {
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
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      item {
        Row(verticalAlignment = Alignment.CenterVertically) {
          AnimatedIconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back") }
          Text("Terms & Attribution", style = MaterialTheme.typography.titleLarge)
        }
      }
      item {
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = MaterialTheme.colorScheme.surface,
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        ) {
          Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("1. Open Food Facts Attribution", style = MaterialTheme.typography.titleMedium)
            Text(
              text = "Product metadata retrieved from Open Food Facts is provided under the Open Database License (ODbL) by Open Food Facts contributors.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text("2. Verification Tiers", style = MaterialTheme.typography.titleMedium)
            Text(
              text = "Every product and store offer declares whether it comes from a Verified Merchant, Licensed Provider, Open Food Facts, or Unverified Community Submission.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text("3. Sandbox Rewards Ledger", style = MaterialTheme.typography.titleMedium)
            Text(
              text = "Points (PTS) operate in Sandbox Rewards Mode and require admin review of SHA-256 hashed receipt submissions before credit.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }
    }
  }
}

@Composable
fun HelpSupportScreen(onBack: () -> Unit) {
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
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      item {
        Row(verticalAlignment = Alignment.CenterVertically) {
          AnimatedIconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back") }
          Text("Help & Scanner Guide", style = MaterialTheme.typography.titleLarge)
        }
      }
      item {
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = MaterialTheme.colorScheme.surface,
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        ) {
          Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Supported Symbologies (13)", style = MaterialTheme.typography.titleMedium)
            Text(
              text = "EAN-13, EAN-8, UPC-A, UPC-E, Code 128, Code 39, Code 93, QR Code, Data Matrix, PDF417, Aztec, ITF, Codabar + Manual GS1 Modulo-10 GTIN entry.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text("Low Light or Damaged Labels", style = MaterialTheme.typography.titleMedium)
            Text(
              text = "Use the Flash button in the viewfinder, pick a photo from your library, or enter digits via Manual GTIN.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }
    }
  }
}
