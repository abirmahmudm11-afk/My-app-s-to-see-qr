package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ExitToApp
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.model.ScanContext
import com.example.domain.MarketConfig
import com.example.ui.components.VerificationLabel
import com.example.ui.components.formatTimestamp
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.DeepObsidian
import com.example.ui.theme.ElectricLime
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

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    item { Text("Profile & Controls", style = MaterialTheme.typography.headlineMedium) }

    item {
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = user?.displayName?.takeIf { it.isNotBlank() } ?: user?.email?.substringBefore("@") ?: "Guest Shopper",
            style = MaterialTheme.typography.titleLarge,
          )
          Text(
            text = user?.email ?: "Exploring in local Guest Mode",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          if (user == null) {
            Button(
              onClick = onSignIn,
              modifier = Modifier
                .fillMaxWidth()
                .testTag("profile_sign_in_button"),
              shape = RoundedCornerShape(14.dp),
            ) {
              Text("Continue with Google")
            }
          } else {
            OutlinedButton(
              onClick = onSignOut,
              modifier = Modifier
                .fillMaxWidth()
                .testTag("profile_sign_out_button"),
              shape = RoundedCornerShape(14.dp),
            ) {
              Icon(Icons.AutoMirrored.Rounded.ExitToApp, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(Modifier.width(8.dp))
              Text("Sign Out")
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
          onClick = onOpenAdmin,
        )
      }
    }

    item {
      ProfileNavTile(
        icon = Icons.Rounded.History,
        title = "Scan History",
        subtitle = "${uiState.recentScans.size} Room & cloud barcode records",
        onClick = onOpenScanHistory,
      )
    }
    item {
      ProfileNavTile(
        icon = Icons.Rounded.Notifications,
        title = "Notifications",
        subtitle = "${uiState.notifications.count { !it.read }} unread alerts",
        onClick = onOpenNotifications,
      )
    }
    item {
      ProfileNavTile(
        icon = Icons.Rounded.Language,
        title = "Country & Language",
        subtitle = "${market.countryName} • ${market.currencyCode} (${market.languageLabel})",
        onClick = onOpenCountryLanguage,
      )
    }

    item {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
      ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
              Icon(Icons.Rounded.DarkMode, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
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
        onClick = onOpenPrivacy,
      )
    }
    item {
      ProfileNavTile(
        icon = Icons.Rounded.Description,
        title = "Terms, Sandbox Rules & Attribution",
        subtitle = "Open Food Facts ODbL & point rules",
        onClick = onOpenTerms,
      )
    }
    item {
      ProfileNavTile(
        icon = Icons.AutoMirrored.Rounded.HelpOutline,
        title = "Help & Scanner Diagnostics",
        subtitle = "Supported formats & receipt review SLA",
        onClick = onOpenHelp,
      )
    }
  }
}

@Composable
private fun ProfileNavTile(
  icon: ImageVector,
  title: String,
  subtitle: String,
  onClick: () -> Unit,
) {
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
      horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
      Column(modifier = Modifier.weight(1f)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
    }
  }
}

@Composable
fun ScanHistoryScreen(
  uiState: MainUiState,
  onBack: () -> Unit,
  onOpenProduct: (String) -> Unit,
  onRescan: (ScanContext) -> Unit,
  onDeleteEntry: (Long) -> Unit,
  onClearAll: () -> Unit,
  onOpenScan: () -> Unit,
) {
  var query by remember { mutableStateOf("") }
  var selectedFilter by remember { mutableStateOf("All") }
  var showClearDialog by remember { mutableStateOf(false) }

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
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
          IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
          }
          Column {
            Text("Scan History (${uiState.recentScans.size})", style = MaterialTheme.typography.titleLarge)
            Text(
              "Stored in Room local database",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.primary,
            )
          }
        }
        if (uiState.localScanHistory.isNotEmpty()) {
          IconButton(
            onClick = { showClearDialog = true },
            modifier = Modifier.testTag("clear_history_button"),
          ) {
            Icon(Icons.Rounded.DeleteSweep, contentDescription = "Clear Scan History", tint = CrimsonAlert)
          }
        }
      }
    }

    item {
      OutlinedTextField(
        value = query,
        onValueChange = { query = it },
        placeholder = { Text("Search product, brand, category, or barcode") },
        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("history_search_input"),
      )
    }

    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        listOf("All", "Matched", "QR", "Unlisted").forEach { filter ->
          val selected = selectedFilter == filter
          Surface(
            modifier = Modifier
              .clip(RoundedCornerShape(10.dp))
              .clickable { selectedFilter = filter },
            color = if (selected) DeepObsidian else MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(10.dp),
          ) {
            Text(
              text = filter,
              style = MaterialTheme.typography.labelMedium,
              color = if (selected) ElectricLime else MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            )
          }
        }
      }
    }

    if (filteredScans.isEmpty()) {
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
            Text(
              text = if (uiState.recentScans.isEmpty()) "No barcode scans logged yet" else "No matching history entries",
              style = MaterialTheme.typography.titleMedium,
            )
            Text(
              text = "Every barcode or QR code you scan is saved automatically to Room with its product details and timestamp.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(
              onClick = onOpenScan,
              shape = RoundedCornerShape(12.dp),
            ) {
              Text("Open Scanner")
            }
          }
        }
      }
    } else {
      items(filteredScans, key = { "${it.id}_${it.barcodeValue}_${it.localTimestampMs}" }) { scan ->
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenProduct(scan.barcodeValue) },
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
              horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              if (scan.imageUrl.isNotBlank()) {
                AsyncImage(
                  model = scan.imageUrl,
                  contentDescription = scan.productName,
                  modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                  contentScale = ContentScale.Crop,
                )
              } else {
                Box(
                  modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                  contentAlignment = Alignment.Center,
                ) {
                  Icon(
                    Icons.Rounded.QrCode2,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp),
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
                Text(
                  text = listOfNotNull(
                    scan.brand.takeIf { it.isNotBlank() },
                    scan.category.takeIf { it.isNotBlank() },
                    scan.barcodeFormat.ifBlank { "GTIN" },
                  ).joinToString(" • "),
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
                Text(
                  text = "${scan.barcodeValue} • ${formatTimestamp(scan.scannedAt, scan.localTimestampMs)}",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.primary,
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
              OutlinedButton(
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
        border = BorderStroke(1.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline),
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
