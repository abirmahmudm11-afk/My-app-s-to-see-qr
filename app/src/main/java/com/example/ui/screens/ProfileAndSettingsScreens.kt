package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ExitToApp
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.PrivacyTip
import androidx.compose.material.icons.rounded.Refresh
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.data.model.ScanContext
import com.example.domain.MarketConfig
import com.example.ui.components.VerificationLabel
import com.example.ui.components.formatTimestamp
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.EmeraldTrust
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
  onResendVerification: () -> Unit,
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
            text = user?.email ?: "Not signed in yet",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          if (user != null && !user.isEmailVerified) {
            Surface(
              color = MaterialTheme.colorScheme.tertiaryContainer,
              shape = RoundedCornerShape(12.dp),
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
              ) {
                Text(
                  "Verify email to unlock admin & cloud checks",
                  style = MaterialTheme.typography.bodySmall,
                  modifier = Modifier.weight(1f),
                )
                androidx.compose.material3.TextButton(onClick = onResendVerification) {
                  Text("Send Link")
                }
              }
            }
          }
          if (user == null) {
            Button(onClick = onSignIn, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
              Text("Sign In / Sign Up")
            }
          } else {
            OutlinedButton(
              onClick = onSignOut,
              modifier = Modifier.fillMaxWidth(),
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
        subtitle = "${uiState.recentScans.size} local & cloud barcode records",
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
              Icon(Icons.Rounded.DarkMode, contentDescription = null)
              Text("Obsidian Dark Theme", style = MaterialTheme.typography.titleMedium)
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
              Text("Freezes scanner laser sweep & transitions", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
      Icon(icon, contentDescription = null, tint = EmeraldTrust)
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
) {
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
        Text("Scan History (${uiState.recentScans.size})", style = MaterialTheme.typography.titleLarge)
      }
    }
    if (uiState.recentScans.isEmpty()) {
      item { Text("No scans recorded yet.", style = MaterialTheme.typography.bodyMedium) }
    } else {
      items(uiState.recentScans, key = { "${it.id}_${it.barcodeValue}_${it.localTimestampMs}" }) { scan ->
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenProduct(scan.barcodeValue) },
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
              Text(scan.productName.ifBlank { scan.barcodeValue }, style = MaterialTheme.typography.titleMedium)
              Text(
                "${scan.barcodeFormat} • ${scan.barcodeValue} • ${formatTimestamp(scan.scannedAt, scan.localTimestampMs)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
              VerificationLabel(statusRaw = scan.verificationStatus)
            }
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
            containerColor = if (item.read) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.secondaryContainer,
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
        border = BorderStroke(1.dp, if (selected) EmeraldTrust else MaterialTheme.colorScheme.outline),
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
            Icon(Icons.Rounded.CheckCircle, contentDescription = "Selected", tint = EmeraldTrust)
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
          Text("Submit Deletion Request")
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
