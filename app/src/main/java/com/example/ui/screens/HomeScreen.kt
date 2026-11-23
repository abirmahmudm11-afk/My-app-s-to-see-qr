package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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
import com.example.data.model.RewardWallet
import com.example.data.model.ScanRecord
import com.example.data.model.VerificationStatus
import com.example.domain.MarketCatalog
import com.example.ui.components.SectionHeader
import com.example.ui.components.VerificationDotLabel
import com.example.ui.components.formatTimestamp
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.BentoButter
import com.example.ui.theme.BentoCobalt
import com.example.ui.theme.BentoCoral
import com.example.ui.theme.BentoIndigo
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
import com.example.ui.theme.Emerald400
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Emerald700
import com.example.ui.theme.Emerald800
import com.example.ui.theme.Emerald900
import com.example.ui.theme.InfoBlue
import com.example.ui.theme.InkBlack
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.ObsidianMid
import com.example.ui.theme.OnBentoButter
import com.example.ui.theme.OnBentoLime
import com.example.ui.theme.OnBentoMint
import com.example.ui.theme.OnBentoOrchid
import com.example.ui.theme.OnBentoPeach
import com.example.ui.theme.OnBentoSky
import java.util.Calendar

private data class FeaturedCatalogItem(
    val title: String,
    val brand: String,
    val barcode: String,
    val category: String
)

private val FeaturedCatalogItems = listOf(
    FeaturedCatalogItem("Nutella Hazelnut Spread", "Ferrero • 400g", "3017620422003", "Spreads"),
    FeaturedCatalogItem("Coca-Cola Original", "Coca-Cola • Classic", "5449000000996", "Beverages"),
    FeaturedCatalogItem("Kikkoman Soy Sauce", "Kikkoman • Pantry", "737628064502", "Pantry"),
    FeaturedCatalogItem("Evian Natural Water", "Evian • Mineral", "3068320115160", "Beverages")
)

@Composable
fun HomeScreen(
    displayName: String,
    isGuest: Boolean,
    countryCode: String,
    wallet: RewardWallet?,
    campaigns: List<Campaign>,
    recentScans: List<ScanRecord>,
    unreadNotificationCount: Int,
    onNavigateScan: () -> Unit,
    onNavigateDiscover: () -> Unit,
    onNavigateRewards: () -> Unit,
    onNavigateSubmitReceipt: () -> Unit,
    onNavigateNotifications: () -> Unit,
    onNavigateScanHistory: () -> Unit,
    onOpenProduct: (String) -> Unit,
    onRequestSignIn: () -> Unit,
    modifier: Modifier = Modifier
) {
    val market = MarketCatalog.findByCountry(countryCode)
    val isDark = isSystemInDarkTheme()
    var insightsTab by remember { mutableStateOf("Activity") }

    val availablePoints = wallet?.availablePoints ?: 0L
    val pendingPoints = wallet?.pendingPoints ?: 0L
    val voucherProgress = (availablePoints.toFloat() / 100f).coerceIn(0.06f, 1f)

    // Compute real 7-day scan counts from recentScans timestamps
    val weeklyBuckets = remember(recentScans) {
        val counts = IntArray(7)
        val now = Calendar.getInstance()
        now.set(Calendar.HOUR_OF_DAY, 0)
        now.set(Calendar.MINUTE, 0)
        now.set(Calendar.SECOND, 0)
        now.set(Calendar.MILLISECOND, 0)
        val todayStart = now.timeInMillis
        val dayMs = 86_400_000L
        recentScans.forEach { scan ->
            val ts = scan.scannedAt?.toDate()?.time ?: return@forEach
            val diffDays = ((todayStart - ts) / dayMs).toInt()
            if (diffDays in 0..6) {
                counts[6 - diffDays]++
            }
        }
        counts.toList()
    }

    val tilePalettes = if (isDark) {
        listOf(
            DarkBentoButter to Color(0xFFFFE58F),
            DarkBentoSky to Color(0xFFB3D6FF),
            DarkBentoOrchid to Color(0xFFF6C2EE),
            DarkBentoPeach to Color(0xFFFFB496)
        )
    } else {
        listOf(
            BentoButter to OnBentoButter,
            BentoSky to OnBentoSky,
            BentoOrchid to OnBentoOrchid,
            BentoPeach to OnBentoPeach
        )
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // 1. Flagship Top Header with Double-Ring Monogram & Market Pill
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(Emerald400, BentoLime)))
                            .padding(2.5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(InkBlack),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isGuest) "S" else displayName.firstOrNull()?.uppercaseChar()?.toString() ?: "S",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = BentoLime
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isGuest) "SCAN2MONEY" else "Welcome back, $displayName",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                color = if (isDark) Emerald900 else BentoMint,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "${market.countryCode} • ${market.currencyCode}",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) BentoLime else OnBentoMint,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                )
                            }
                            Text(
                                text = if (isGuest) "Guest Explorer" else "Verified Account",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = CircleShape,
                    shadowElevation = 2.dp,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    IconButton(
                        onClick = onNavigateNotifications,
                        modifier = Modifier.testTag("home_notifications_button")
                    ) {
                        Box {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications"
                            )
                            if (unreadNotificationCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(BentoCoral)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Elevated Search & Instant Camera Trigger Capsule
        item {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(22.dp),
                shadowElevation = 3.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onNavigateDiscover)
                        .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Search barcode, product, or GTIN...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    Surface(
                        color = InkBlack,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable(onClick = onNavigateScan)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = "Scan barcode",
                                tint = BentoLime,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Scan",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // 3. Flagship Obsidian-Emerald Command Card with Precision Laser Barcode Canvas
        item {
            Surface(
                shape = RoundedCornerShape(28.dp),
                shadowElevation = 8.dp,
                color = InkBlack,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .clickable(onClick = if (isGuest) onRequestSignIn else onNavigateRewards)
                    .testTag("home_wallet_card")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.linearGradient(listOf(InkBlack, ObsidianMid, Emerald900)))
                ) {
                    // Right-side Laser Barcode Wave Graphic
                    Canvas(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(width = 156.dp, height = 112.dp)
                            .padding(top = 18.dp, end = 18.dp)
                    ) {
                        val bars = listOf(3f, 1.5f, 5f, 2f, 3f, 6f, 1.5f, 4f, 2f, 5f, 2f, 3f)
                        var bx = 8.dp.toPx()
                        bars.forEachIndexed { idx, units ->
                            val w = units * 2.1.dp.toPx()
                            val h = size.height * (0.45f + (idx % 3) * 0.14f)
                            drawRoundRect(
                                color = Color.White.copy(alpha = 0.13f),
                                topLeft = Offset(bx, 6.dp.toPx()),
                                size = Size(w, h),
                                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                            )
                            bx += w + 4.dp.toPx()
                        }
                        // Glowing Volt-Lime scan beam
                        drawLine(
                            color = BentoLime,
                            start = Offset(0f, size.height * 0.44f),
                            end = Offset(size.width, size.height * 0.44f),
                            strokeWidth = 2.5.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(22.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                color = BentoLime,
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = if (isGuest) "GUEST SESSION" else "SANDBOX WALLET",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = InkBlack,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            if (isGuest) {
                                Text(
                                    text = "Sign in →",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Emerald400,
                                    modifier = Modifier
                                        .clickable(onClick = onRequestSignIn)
                                        .testTag("guest_banner_sign_in_button")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        if (isGuest) {
                            Text(
                                text = "Scan & Earn\nVerified Points",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "13 barcode formats • Live product lookup",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.72f)
                            )
                        } else {
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "$availablePoints",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    style = MaterialTheme.typography.displayMedium.copy(fontSize = 40.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "PTS",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = BentoLime,
                                    modifier = Modifier.padding(bottom = 7.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.58f)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color.White.copy(alpha = 0.16f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(voucherProgress)
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(BentoLime)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (pendingPoints > 0) "$pendingPoints PTS pending review" else "${availablePoints.coerceAtMost(100)} / 100 PTS to voucher",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.72f)
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = onNavigateScan,
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = BentoLime,
                                    contentColor = InkBlack
                                ),
                                modifier = Modifier
                                    .weight(1.15f)
                                    .height(48.dp)
                                    .testTag("home_scan_now_button")
                            ) {
                                Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Scan Now", fontWeight = FontWeight.Bold)
                            }
                            Surface(
                                color = Color.White.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.16f)),
                                modifier = Modifier
                                    .weight(0.85f)
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { if (isGuest) onRequestSignIn() else onNavigateSubmitReceipt() }
                                    .testTag("home_quick_receipt")
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(17.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Receipt",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Tactile 4-Column Quick Capability Dock
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CapabilityDockItem(
                    icon = Icons.Default.QrCodeScanner,
                    label = "Scan",
                    bgColor = if (isDark) Emerald900 else BentoMint,
                    tint = if (isDark) BentoLime else OnBentoMint,
                    onClick = onNavigateScan
                )
                CapabilityDockItem(
                    icon = Icons.Default.Explore,
                    label = "Catalog",
                    bgColor = if (isDark) DarkBentoSky else BentoSky,
                    tint = if (isDark) Color(0xFFB3D6FF) else OnBentoSky,
                    onClick = onNavigateDiscover,
                    modifier = Modifier.testTag("home_quick_discover")
                )
                CapabilityDockItem(
                    icon = Icons.AutoMirrored.Filled.ReceiptLong,
                    label = "Receipt",
                    bgColor = if (isDark) DarkBentoButter else BentoButter,
                    tint = if (isDark) Color(0xFFFFE58F) else OnBentoButter,
                    onClick = { if (isGuest) onRequestSignIn() else onNavigateSubmitReceipt() }
                )
                CapabilityDockItem(
                    icon = Icons.Default.History,
                    label = "History",
                    bgColor = if (isDark) DarkBentoOrchid else BentoOrchid,
                    tint = if (isDark) Color(0xFFF6C2EE) else OnBentoOrchid,
                    onClick = onNavigateScanHistory,
                    modifier = Modifier.testTag("home_quick_history")
                )
            }
        }

        // 5. Interactive Dual-Tab Analytics Deck ("Activity" 7-Day Bar Chart vs "Formats" Radial Gauge)
        item {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(24.dp),
                shadowElevation = 2.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (insightsTab == "Activity") "7-Day Scan Activity" else "Symbology Engine",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (insightsTab == "Activity") "${recentScans.size} validated lookups logged" else "9 Retail 1D + 4 Matrix 2D formats",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Segmented Pill Switcher
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Row(modifier = Modifier.padding(3.dp)) {
                                listOf("Activity", "Formats").forEach { tab ->
                                    val selected = insightsTab == tab
                                    Surface(
                                        color = if (selected) InkBlack else Color.Transparent,
                                        shape = RoundedCornerShape(11.dp),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(11.dp))
                                            .clickable { insightsTab = tab }
                                    ) {
                                        Text(
                                            text = tab,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (selected) BentoLime else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (insightsTab == "Activity") {
                        // Custom Canvas 7-Day Vertical Rounded Bar Chart
                        val maxCount = (weeklyBuckets.maxOrNull() ?: 0).coerceAtLeast(1)
                        val dayLabels = listOf("D-6", "D-5", "D-4", "D-3", "D-2", "Yest", "Now")
                        Column {
                            Canvas(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(92.dp)
                            ) {
                                val slotW = size.width / 7f
                                val barW = 20.dp.toPx().coerceAtMost(slotW * 0.56f)
                                weeklyBuckets.forEachIndexed { idx, count ->
                                    val x = idx * slotW + (slotW - barW) / 2f
                                    // Background track bar
                                    drawRoundRect(
                                        color = Color(0xFF94A39D).copy(alpha = 0.16f),
                                        topLeft = Offset(x, 0f),
                                        size = Size(barW, size.height),
                                        cornerRadius = CornerRadius(barW / 2f, barW / 2f)
                                    )
                                    val frac = if (count == 0) 0.12f else (count.toFloat() / maxCount).coerceIn(0.22f, 1f)
                                    val activeH = size.height * frac
                                    val barColor = when {
                                        count > 0 && idx == 6 -> Emerald600
                                        count > 0 -> BentoCobalt
                                        else -> Color(0xFF94A39D).copy(alpha = 0.35f)
                                    }
                                    drawRoundRect(
                                        color = barColor,
                                        topLeft = Offset(x, size.height - activeH),
                                        size = Size(barW, activeH),
                                        cornerRadius = CornerRadius(barW / 2f, barW / 2f)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                dayLabels.forEachIndexed { idx, label ->
                                    Text(
                                        text = label,
                                        fontFamily = JetBrainsMonoFontFamily,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (idx == 6) Emerald600 else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = if (idx == 6) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    } else {
                        // Radial Format Coverage Gauge + Breakdown Chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.size(96.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.size(86.dp)) {
                                    val stroke = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                                    val pad = 5.dp.toPx()
                                    val sz = Size(size.width - pad * 2, size.height - pad * 2)
                                    drawArc(
                                        color = Emerald600,
                                        startAngle = -90f,
                                        sweepAngle = 235f,
                                        useCenter = false,
                                        topLeft = Offset(pad, pad),
                                        size = sz,
                                        style = stroke
                                    )
                                    drawArc(
                                        color = BentoCobalt,
                                        startAngle = 158f,
                                        sweepAngle = 98f,
                                        useCenter = false,
                                        topLeft = Offset(pad, pad),
                                        size = sz,
                                        style = stroke
                                    )
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "13",
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                    Text(
                                        text = "ACTIVE",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FormatBreakdownRow("1D Retail (9)", "EAN, UPC, Code128, ITF", Emerald600)
                                FormatBreakdownRow("2D Matrix (4)", "QR, Aztec, DataMatrix, PDF417", BentoCobalt)
                            }
                        }
                    }
                }
            }
        }

        // 6. Illustrated Featured Product Carousel (Vertical Storefront Cards!)
        item {
            SectionHeader(
                title = if (campaigns.isNotEmpty()) "Live Partner Campaigns" else "Verified GTIN Showcase",
                actionLabel = "Explorer",
                onActionClick = onNavigateDiscover
            )
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (campaigns.isNotEmpty()) {
                    items(campaigns.size) { idx ->
                        val campaign = campaigns[idx]
                        val (bg, ink) = tilePalettes[idx % tilePalettes.size]
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(22.dp),
                            shadowElevation = 2.dp,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier
                                .width(228.dp)
                                .clip(RoundedCornerShape(22.dp))
                                .clickable(onClick = onNavigateRewards)
                        ) {
                            Column {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(bg)
                                        .padding(14.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Campaign, contentDescription = null, tint = ink, modifier = Modifier.size(18.dp))
                                        Surface(color = InkBlack, shape = RoundedCornerShape(8.dp)) {
                                            Text(
                                                text = "+${campaign.rewardPoints} PTS",
                                                fontFamily = JetBrainsMonoFontFamily,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = BentoLime,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                }
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(campaign.partnerName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(campaign.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                    }
                } else {
                    items(FeaturedCatalogItems.size) { idx ->
                        val item = FeaturedCatalogItems[idx]
                        val (bg, ink) = tilePalettes[idx % tilePalettes.size]
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(22.dp),
                            shadowElevation = 2.dp,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier
                                .width(178.dp)
                                .clip(RoundedCornerShape(22.dp))
                                .clickable { onOpenProduct(item.barcode) }
                        ) {
                            Column {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(82.dp)
                                        .background(bg)
                                        .padding(12.dp)
                                ) {
                                    Surface(
                                        color = Color.White.copy(alpha = 0.85f),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.align(Alignment.TopStart)
                                    ) {
                                        Text(
                                            text = item.category.uppercase(),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = InkBlack,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                        )
                                    }
                                    Surface(
                                        color = InkBlack,
                                        shape = CircleShape,
                                        modifier = Modifier
                                            .size(30.dp)
                                            .align(Alignment.BottomEnd)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = BentoLime, modifier = Modifier.size(15.dp))
                                        }
                                    }
                                }
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = item.brand,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = item.barcode,
                                        fontFamily = JetBrainsMonoFontFamily,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Emerald600
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 7. Recent Scans Section
        item {
            SectionHeader(
                title = "Recent Activity",
                actionLabel = if (recentScans.isNotEmpty()) "View All" else null,
                onActionClick = onNavigateScanHistory
            )
        }

        if (recentScans.isEmpty()) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(22.dp),
                    shadowElevation = 2.dp,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = BentoButter,
                            modifier = Modifier.size(64.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_discover_basket),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Start scanning products",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Inspect ingredients, merchant attribution & store offers.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        } else {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    shape = RoundedCornerShape(22.dp),
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        val visibleScans = recentScans.take(5)
                        visibleScans.forEachIndexed { index, scan ->
                            val (badgeColor, badgeInk) = tilePalettes[index % tilePalettes.size]
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onOpenProduct(scan.barcodeValue) }
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = badgeColor,
                                    shape = RoundedCornerShape(13.dp),
                                    modifier = Modifier.size(46.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.QrCode2,
                                            contentDescription = null,
                                            tint = badgeInk,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = scan.productName.ifBlank { scan.barcodeValue },
                                        style = MaterialTheme.typography.titleMedium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${scan.barcodeFormat} • ${formatTimestamp(scan.scannedAt)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    VerificationDotLabel(statusRaw = scan.verificationStatus)
                                }
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (index < visibleScans.lastIndex) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CapabilityDockItem(
    icon: ImageVector,
    label: String,
    bgColor: Color,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            color = bgColor,
            shape = RoundedCornerShape(18.dp),
            shadowElevation = 2.dp,
            modifier = Modifier.size(58.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(24.dp))
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun FormatBreakdownRow(
    title: String,
    subtitle: String,
    color: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(title, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
