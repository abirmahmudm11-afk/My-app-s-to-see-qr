package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.AdminRole
import com.example.data.model.AppNotification
import com.example.data.model.ScanRecord
import com.example.domain.MarketCatalog
import com.example.ui.components.GuestModeBanner
import com.example.ui.components.VerificationBadge
import com.example.ui.components.formatTimestamp
import com.example.ui.theme.BentoButter
import com.example.ui.theme.BentoLime
import com.example.ui.theme.BentoMint
import com.example.ui.theme.BentoOrchid
import com.example.ui.theme.BentoPeach
import com.example.ui.theme.BentoSky
import com.example.ui.theme.DarkBentoButter
import com.example.ui.theme.DarkBentoLime
import com.example.ui.theme.DarkBentoOrchid
import com.example.ui.theme.DarkBentoPeach
import com.example.ui.theme.DarkBentoSky
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Emerald700
import com.example.ui.theme.Emerald800
import com.example.ui.theme.InkBlack
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.OnBentoButter
import com.example.ui.theme.OnBentoMint
import com.example.ui.theme.OnBentoOrchid
import com.example.ui.theme.OnBentoPeach
import com.example.ui.theme.OnBentoSky

import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R

@Composable
fun ProfileScreen(
    isGuest: Boolean,
    displayName: String,
    email: String?,
    countryCode: String,
    languageCode: String,
    darkThemeEnabled: Boolean,
    reducedMotionEnabled: Boolean,
    marketingConsent: Boolean,
    unreadNotificationCount: Int,
    adminRole: AdminRole?,
    onToggleDarkTheme: (Boolean) -> Unit,
    onToggleReducedMotion: (Boolean) -> Unit,
    onOpenScanHistory: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenCountryLanguage: () -> Unit,
    onOpenPrivacy: () -> Unit,
    onOpenTerms: () -> Unit,
    onOpenHelp: () -> Unit,
    onOpenAdminConsole: () -> Unit,
    onRequestDeletion: (String) -> Unit,
    onSignInClick: () -> Unit,
    onSignOutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var deleteReason by remember { mutableStateOf("") }
    val market = MarketCatalog.findByCountry(countryCode)

    val isDark = isSystemInDarkTheme()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Screenshot 2 Style: Hero Backdrop + Overlapping Centered Avatar + Status Pill + 3 Icon-Topped Stats
        item {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                shape = RoundedCornerShape(26.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(92.dp)
                                .background(Brush.horizontalGradient(listOf(BentoMint, Emerald600)))
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_wallet_trophy),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                alpha = 0.25f,
                                modifier = Modifier.fillMaxSize()
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Surface(
                                    color = Color.White.copy(alpha = 0.9f),
                                    shape = CircleShape,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clickable(onClick = onOpenCountryLanguage)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Language, contentDescription = "Region", tint = InkBlack, modifier = Modifier.size(18.dp))
                                    }
                                }
                                Surface(
                                    color = Color.White.copy(alpha = 0.9f),
                                    shape = CircleShape,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clickable(onClick = onOpenNotifications)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = InkBlack, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }

                        // Centered Overlapping Circular Avatar
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .size(74.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(BentoLime),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isGuest) "G" else displayName.firstOrNull()?.uppercaseChar()?.toString() ?: "S",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = InkBlack
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = if (isGuest) BentoButter else BentoMint,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = InkBlack,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isGuest) "Guest Browsing" else "Verified Account",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = InkBlack
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isGuest) "Guest Shopper" else displayName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isGuest) "Local mode • Sign in to sync" else (email ?: "Google Identity"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp, horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ProfileStatMetric(icon = Icons.Default.Public, label = "Market", value = market.countryCode)
                        ProfileStatMetric(icon = Icons.Default.Paid, label = "Currency", value = market.currencyCode)
                        ProfileStatMetric(icon = Icons.Default.Notifications, label = "Alerts", value = "$unreadNotificationCount")
                    }
                }
            }
        }

        // Screenshot 2 Style: Tinted Readiness / Compliance Card with Gradient Bar
        item {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .clickable(onClick = onOpenTerms)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = BentoButter,
                            shape = CircleShape,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Bolt, contentDescription = null, tint = OnBentoButter, modifier = Modifier.size(18.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Sandbox Ledger Active",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Marketing consent: ${if (marketingConsent) "Opted in" else "Off"} • Cashback disabled",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(if (isGuest) 0.45f else 0.85f)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Brush.horizontalGradient(listOf(Emerald600, BentoLime)))
                        )
                    }
                }
            }
        }

        if (isGuest) {
            item {
                GuestModeBanner(onSignInClick = onSignInClick)
            }
        }

        if (adminRole != null) {
            item {
                Surface(
                    color = if (isDark) DarkBentoLime else BentoLime,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .clickable(onClick = onOpenAdminConsole)
                        .testTag("profile_admin_console_button")
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = InkBlack,
                            shape = CircleShape,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AdminPanelSettings,
                                    contentDescription = "Admin Console",
                                    tint = BentoLime,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Admin Operations Console",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) BentoLime else InkBlack
                            )
                            Text(
                                text = "Role: ${adminRole.roleName} • Receipt review & audit",
                                style = MaterialTheme.typography.bodySmall,
                                color = (if (isDark) BentoLime else InkBlack).copy(alpha = 0.8f)
                            )
                        }
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = if (isDark) BentoLime else InkBlack
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "ACCOUNT & PREFERENCES",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                shape = RoundedCornerShape(22.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    ProfileNavRow(
                        icon = Icons.Default.Notifications,
                        iconBg = if (isDark) DarkBentoPeach else BentoPeach,
                        iconTint = if (isDark) Color(0xFFFFC1AA) else OnBentoPeach,
                        title = "Notifications",
                        subtitle = if (unreadNotificationCount > 0) "$unreadNotificationCount unread alerts" else "Rewards, receipts & security",
                        onClick = onOpenNotifications
                    )
                    HorizontalDivider()
                    ProfileNavRow(
                        icon = Icons.Default.History,
                        iconBg = if (isDark) DarkBentoSky else BentoSky,
                        iconTint = if (isDark) Color(0xFFB3D6FF) else OnBentoSky,
                        title = "Scan History",
                        subtitle = "All validated barcode lookups",
                        onClick = onOpenScanHistory
                    )
                    HorizontalDivider()
                    ProfileNavRow(
                        icon = Icons.Default.Language,
                        iconBg = if (isDark) Emerald800 else BentoMint,
                        iconTint = if (isDark) Color.White else OnBentoMint,
                        title = "Country & Language",
                        subtitle = "${market.countryName} (${market.currencyCode}) • $languageCode",
                        onClick = onOpenCountryLanguage
                    )
                    HorizontalDivider()
                    ProfileNavRow(
                        icon = Icons.Default.PrivacyTip,
                        iconBg = if (isDark) DarkBentoOrchid else BentoOrchid,
                        iconTint = if (isDark) Color(0xFFF6C2EE) else OnBentoOrchid,
                        title = "Privacy & Consent",
                        subtitle = "Camera processing, marketing consent & retention",
                        onClick = onOpenPrivacy
                    )
                    HorizontalDivider()
                    ProfileNavRow(
                        icon = Icons.Default.Gavel,
                        iconBg = if (isDark) DarkBentoButter else BentoButter,
                        iconTint = if (isDark) Color(0xFFFFE58F) else OnBentoButter,
                        title = "Terms & Reward Rules",
                        subtitle = "Sandbox points lifecycle & country limits",
                        onClick = onOpenTerms
                    )
                    HorizontalDivider()
                    ProfileNavRow(
                        icon = Icons.AutoMirrored.Filled.HelpOutline,
                        iconBg = MaterialTheme.colorScheme.surfaceVariant,
                        iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                        title = "Help & Support",
                        subtitle = "Scanner formats, attributions & troubleshooting",
                        onClick = onOpenHelp
                    )
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Appearance & Accessibility", style = MaterialTheme.typography.titleMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Dark Theme", style = MaterialTheme.typography.bodyLarge)
                            Text("High-contrast dark emerald palette", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = darkThemeEnabled,
                            onCheckedChange = onToggleDarkTheme,
                            modifier = Modifier.testTag("dark_theme_switch")
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Reduced Motion", style = MaterialTheme.typography.bodyLarge)
                            Text("Disable animated scanner beam", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = reducedMotionEnabled,
                            onCheckedChange = onToggleReducedMotion,
                            modifier = Modifier.testTag("reduced_motion_switch")
                        )
                    }
                }
            }
        }

        if (!isGuest) {
            item {
                OutlinedButton(
                    onClick = onSignOutClick,
                    modifier = Modifier.fillMaxWidth().height(50.dp).testTag("profile_sign_out_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sign Out")
                }
            }
            item {
                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth().height(50.dp).testTag("profile_delete_account_button")
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Request Account Deletion")
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Request Account Deletion") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "This logs a formal deletion request and marks your account status as DELETION_REQUESTED.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = deleteReason,
                        onValueChange = { deleteReason = it },
                        label = { Text("Reason (optional)") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        onRequestDeletion(deleteReason.ifBlank { "User requested account deletion" })
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Confirm Request")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun ProfileStatMetric(
    icon: ImageVector,
    label: String,
    value: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontFamily = JetBrainsMonoFontFamily,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ProfileNavRow(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = iconBg,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.size(38.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanHistoryScreen(
    scans: List<ScanRecord>,
    onOpenProduct: (String) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan History (${scans.size})") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (scans.isEmpty()) {
            BoxEmptyMessage(
                text = "No scans recorded yet. Scan a barcode or lookup a product to build your verified history.",
                modifier = Modifier.padding(innerPadding)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(scans, key = { it.id + it.barcodeValue + (it.scannedAt?.seconds ?: 0L) }) { scan ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth().clickable { onOpenProduct(scan.barcodeValue) }
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(scan.productName.ifBlank { "Unlisted Product" }, style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = "${scan.barcodeFormat} • ${scan.barcodeValue}",
                                fontFamily = JetBrainsMonoFontFamily,
                                style = MaterialTheme.typography.labelSmall
                            )
                            Text(
                                text = "Provider: ${scan.sourceProvider} • ${formatTimestamp(scan.scannedAt)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            VerificationBadge(statusRaw = scan.verificationStatus)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    isGuest: Boolean,
    notifications: List<AppNotification>,
    onMarkRead: (String) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (isGuest) {
            BoxEmptyMessage("Sign in with Google to receive reward, receipt review, and security alerts.", Modifier.padding(innerPadding))
        } else if (notifications.isEmpty()) {
            BoxEmptyMessage("No notifications yet.", Modifier.padding(innerPadding))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(notifications, key = { it.id }) { item ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (item.read) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth().clickable {
                            if (!item.read) onMarkRead(item.id)
                        }
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(item.category, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                Text(formatTimestamp(item.createdAt), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(item.title, style = MaterialTheme.typography.titleMedium)
                            Text(item.body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountryLanguageScreen(
    selectedCountryCode: String,
    onSelectCountry: (countryCode: String, languageCode: String) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Country & Market Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(MarketCatalog.markets, key = { it.countryCode }) { market ->
                val selected = market.countryCode.equals(selectedCountryCode, ignoreCase = true)
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth().clickable {
                        onSelectCountry(market.countryCode, market.defaultLanguage)
                    }
                ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                        RadioButton(selected = selected, onClick = {
                            onSelectCountry(market.countryCode, market.defaultLanguage)
                        })
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("${market.countryName} (${market.countryCode} / ${market.currencyCode})", style = MaterialTheme.typography.titleMedium)
                            Text("Language: ${market.languageLabel}", style = MaterialTheme.typography.bodySmall)
                            Text("Catalog: ${market.productCoverageNote}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Payouts: ${market.payoutStatusNote}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacySettingsScreen(
    marketingConsent: Boolean,
    onToggleMarketingConsent: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacy & Consent") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Promotional Campaign Notifications", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "Marketing announcements require explicit opt-in consent. Security and receipt review notices remain enabled.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(checked = marketingConsent, onCheckedChange = onToggleMarketingConsent)
                }
            }
            Text("Privacy-First Guarantees", style = MaterialTheme.typography.titleMedium)
            Text(
                text = "• Camera frames are analyzed strictly on-device with ML Kit; video is never uploaded.\n" +
                    "• Receipt photos use Android's zero-permission Photo Picker.\n" +
                    "• Precise GPS location is never requested or collected.\n" +
                    "• User data is isolated by Firebase Auth UID and zero-trust Firestore security rules.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsRulesScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Terms & Reward Rules") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("SCAN2MONEY Sandbox v1.0 Rules", style = MaterialTheme.typography.headlineMedium)
            Text(
                text = "1. Cashback Disabled: Live financial payouts and cash withdrawals are disabled in this sandbox release.\n\n" +
                    "2. Ledger Lifecycle: Pending -> Approved -> Credited -> Redeemed (or Rejected / Expired / Reversed). Reversals always append a linked audit record.\n\n" +
                    "3. Receipt Verification: Uploading a receipt never auto-credits points. Every submission undergoes duplicate-hash checks and human review.\n\n" +
                    "4. Source Attribution: Product catalog metadata is attributed to Open Food Facts (ODbL 1.0), verified merchants, or labeled unverified.\n\n" +
                    "5. Country Limitations: Exchange rates and merchant catalogs vary by country. Sandbox points do not carry monetary value.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpSupportScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Help & Support") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Supported Barcode Formats", style = MaterialTheme.typography.titleLarge)
            Text(
                text = "EAN-13, EAN-8, UPC-A, UPC-E, Code 128, Code 39, Code 93, QR Code, Data Matrix, PDF417, Aztec, ITF, and Codabar.",
                style = MaterialTheme.typography.bodyMedium
            )
            Text("Troubleshooting Tips", style = MaterialTheme.typography.titleLarge)
            Text(
                text = "• If camera preview is unavailable, use 'Photo' to scan an image via the Photo Picker or 'Manual' to enter a GTIN directly.\n" +
                    "• For EAN-13 / UPC-A / EAN-8, SCAN2MONEY enforces GS1 Modulo-10 check-digit validation before looking up products.\n" +
                    "• Missing product details are never fabricated; use 'Submit Unverified Product Info' to contribute missing entries.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun BoxEmptyMessage(text: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
