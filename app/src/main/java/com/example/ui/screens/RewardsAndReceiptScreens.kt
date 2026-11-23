package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Verified
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.Campaign
import com.example.data.model.ReceiptReviewStatus
import com.example.data.model.ReceiptSubmission
import com.example.data.model.RewardTransaction
import com.example.data.model.RewardWallet
import com.example.data.model.TransactionStatus
import com.example.domain.MarketCatalog
import com.example.ui.components.GuestModeBanner
import com.example.ui.components.SandboxCashbackBanner
import com.example.ui.components.SectionHeader
import com.example.ui.components.formatTimestamp
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.BentoButter
import com.example.ui.theme.BentoLime
import com.example.ui.theme.BentoMint
import com.example.ui.theme.BentoPeach
import com.example.ui.theme.DarkBentoButter
import com.example.ui.theme.DarkBentoPeach
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Emerald800
import com.example.ui.theme.Emerald900
import com.example.ui.theme.InkBlack
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.Mint100
import com.example.ui.theme.OnBentoButter
import com.example.ui.theme.OnBentoMint
import com.example.ui.theme.OnBentoPeach

@Composable
fun RewardsScreen(
    isGuest: Boolean,
    wallet: RewardWallet?,
    transactions: List<RewardTransaction>,
    receipts: List<ReceiptSubmission>,
    feedbackMessage: String?,
    onClearFeedback: () -> Unit,
    onRedeemVoucher: (RewardWallet, Long, String) -> Unit,
    onNavigateSubmitReceipt: () -> Unit,
    onOpenTransaction: (String) -> Unit,
    onNavigateTerms: () -> Unit,
    onRequestSignIn: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()

    var walletInfoTab by remember { mutableStateOf("Breakdown") }

    if (isGuest) {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Rewards Ledger", style = MaterialTheme.typography.headlineLarge)
                    OutlinedButton(onClick = onNavigateTerms) {
                        Text("Rules")
                    }
                }
            }

            // Layered Stacked Card Deck Preview
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    Surface(
                        color = BentoLime,
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp)
                            .height(48.dp)
                    ) {}
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                            .clip(RoundedCornerShape(26.dp))
                            .background(Brush.linearGradient(listOf(InkBlack, Emerald800)))
                            .padding(20.dp)
                    ) {
                        Canvas(modifier = Modifier.matchParentSize()) {
                            drawCircle(
                                color = BentoLime.copy(alpha = 0.08f),
                                radius = size.width * 0.48f,
                                center = Offset(size.width * 0.92f, size.height * 0.15f),
                                style = Stroke(width = 2.dp.toPx())
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Surface(
                                    color = BentoLime,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "SANDBOX PASS",
                                        fontFamily = JetBrainsMonoFontFamily,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = InkBlack,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Audited Points\n& Voucher Deck",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Sign in to submit receipts & unlock ledger tracking",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.78f)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = BentoMint,
                                modifier = Modifier.size(width = 96.dp, height = 84.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_wallet_trophy),
                                    contentDescription = "Rewards wallet illustration",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }

            item {
                VerificationPipelineCard()
            }

            item {
                GuestModeBanner(onSignInClick = onRequestSignIn)
            }
            item {
                SandboxCashbackBanner()
            }
        }
        return
    }

    val safeWallet = wallet ?: RewardWallet()
    val progressRatio = (safeWallet.availablePoints.toFloat() / 100f).coerceIn(0.06f, 1f)
    val totalVolume = (safeWallet.availablePoints + safeWallet.pendingPoints + safeWallet.redeemedPoints + safeWallet.reversedPoints)
        .coerceAtLeast(1L).toFloat()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Rewards Ledger", style = MaterialTheme.typography.headlineLarge)
                OutlinedButton(onClick = onNavigateTerms) {
                    Text("Rules & Expiry")
                }
            }
        }

        if (feedbackMessage != null) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth().clickable { onClearFeedback() }
                ) {
                    Text(
                        text = feedbackMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        }

        // Layered Stacked Card Deck (Volt-Lime peek card behind Metallic Obsidian Pass)
        item {
            Box(modifier = Modifier.fillMaxWidth().padding(top = 6.dp)) {
                Surface(
                    color = BentoLime,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp)
                        .height(48.dp)
                ) {}
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .clip(RoundedCornerShape(26.dp))
                        .background(Brush.linearGradient(listOf(InkBlack, Emerald800)))
                        .padding(20.dp)
                ) {
                    Canvas(modifier = Modifier.matchParentSize()) {
                        drawCircle(
                            color = BentoLime.copy(alpha = 0.09f),
                            radius = size.width * 0.45f,
                            center = Offset(size.width * 0.9f, size.height * 0.1f),
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Surface(
                                    color = BentoLime,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "SANDBOX PASS",
                                        fontFamily = JetBrainsMonoFontFamily,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = InkBlack,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "${safeWallet.availablePoints} PTS",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    style = MaterialTheme.typography.displayMedium,
                                    color = Color.White
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = BentoMint,
                                modifier = Modifier.size(width = 88.dp, height = 70.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_wallet_trophy),
                                    contentDescription = "Wallet trophy",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Voucher threshold",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.72f)
                            )
                            Text(
                                text = "${safeWallet.availablePoints.coerceAtMost(100)} / 100 PTS",
                                fontFamily = JetBrainsMonoFontFamily,
                                style = MaterialTheme.typography.labelSmall,
                                color = BentoLime
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.White.copy(alpha = 0.16f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(progressRatio)
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(BentoLime)
                            )
                        }
                    }
                }
            }
        }

        // Interactive Breakdown vs Pipeline Card
        item {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                shape = RoundedCornerShape(22.dp),
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (walletInfoTab == "Breakdown") "Ledger Breakdown" else "Verification Flow",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Row(modifier = Modifier.padding(3.dp)) {
                                listOf("Breakdown", "How It Works").forEach { tab ->
                                    val selected = walletInfoTab == tab
                                    Surface(
                                        color = if (selected) InkBlack else Color.Transparent,
                                        shape = RoundedCornerShape(11.dp),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(11.dp))
                                            .clickable { walletInfoTab = tab }
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

                    Spacer(modifier = Modifier.height(14.dp))

                    if (walletInfoTab == "Breakdown") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp))
                        ) {
                            val availW = (safeWallet.availablePoints / totalVolume).coerceAtLeast(0.05f)
                            val pendW = (safeWallet.pendingPoints / totalVolume).coerceAtLeast(0.05f)
                            val redW = (safeWallet.redeemedPoints / totalVolume).coerceAtLeast(0.05f)
                            val revW = (safeWallet.reversedPoints / totalVolume).coerceAtLeast(0.05f)
                            Box(modifier = Modifier.weight(availW).height(12.dp).background(Emerald600))
                            Box(modifier = Modifier.weight(pendW).height(12.dp).background(AmberWarning))
                            Box(modifier = Modifier.weight(redW).height(12.dp).background(BentoLime))
                            Box(modifier = Modifier.weight(revW).height(12.dp).background(BentoPeach))
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            WalletBentoTile(
                                label = "Pending",
                                points = safeWallet.pendingPoints,
                                bgColor = if (isDark) DarkBentoButter else BentoButter,
                                inkColor = if (isDark) Color(0xFFFFE58F) else OnBentoButter,
                                modifier = Modifier.weight(1f)
                            )
                            WalletBentoTile(
                                label = "Redeemed",
                                points = safeWallet.redeemedPoints,
                                bgColor = if (isDark) Emerald900 else BentoMint,
                                inkColor = if (isDark) Color.White else OnBentoMint,
                                modifier = Modifier.weight(1f)
                            )
                            WalletBentoTile(
                                label = "Reversed",
                                points = safeWallet.reversedPoints,
                                bgColor = if (isDark) DarkBentoPeach else BentoPeach,
                                inkColor = if (isDark) Color(0xFFFFC1AA) else OnBentoPeach,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            PipelineStepNode(
                                icon = Icons.AutoMirrored.Filled.ReceiptLong,
                                label = "1. Submit",
                                bg = BentoButter,
                                ink = OnBentoButter,
                                modifier = Modifier.weight(1f)
                            )
                            HorizontalDivider(
                                modifier = Modifier.width(18.dp),
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                            PipelineStepNode(
                                icon = Icons.Default.HourglassTop,
                                label = "2. Review",
                                bg = BentoPeach,
                                ink = OnBentoPeach,
                                modifier = Modifier.weight(1f)
                            )
                            HorizontalDivider(
                                modifier = Modifier.width(18.dp),
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                            PipelineStepNode(
                                icon = Icons.Default.Verified,
                                label = "3. Credit",
                                bg = BentoMint,
                                ink = OnBentoMint,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // Submit Receipt CTA + Sandbox Voucher Redemption
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onNavigateSubmitReceipt,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Emerald600,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("rewards_submit_receipt_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Submit Receipt")
                }
                OutlinedButton(
                    onClick = { onRedeemVoucher(safeWallet, 100L, "Eco Partner Sandbox Pass") },
                    enabled = safeWallet.availablePoints >= 100L,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("rewards_redeem_voucher_button")
                ) {
                    Icon(Icons.Default.CardGiftcard, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Redeem 100")
                }
            }
        }

        item {
            SandboxCashbackBanner()
        }

        // Receipt Verification Queue Status
        item {
            SectionHeader(title = "Receipt Submissions (${receipts.size})")
        }
        if (receipts.isEmpty()) {
            item {
                Text(
                    text = "No receipts submitted yet. Receipts require human review before any points are credited.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(receipts, key = { it.id }) { receipt ->
                val status = ReceiptReviewStatus.safeValueOf(receipt.reviewStatus)
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(receipt.storeName, style = MaterialTheme.typography.titleMedium)
                            ReceiptStatusChip(status = status)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${MarketCatalog.formatMoney(receipt.totalAmount, receipt.currencyCode)} • Ref: ${receipt.receiptReference} • ${receipt.transactionDate}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (!receipt.rejectionReason.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Reason: ${receipt.rejectionReason}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }

        // Ledger Transactions
        item {
            SectionHeader(title = "Ledger History (${transactions.size})")
        }
        if (transactions.isEmpty()) {
            item {
                Text(
                    text = "No ledger entries yet. Approved receipts and sandbox redemptions appear here with idempotency and audit IDs.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(transactions, key = { it.id }) { tx ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenTransaction(tx.id) }
                        .testTag("tx_card_${tx.id}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(tx.sourceActivity, style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = "${tx.status} • ${formatTimestamp(tx.createdAt)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Audit: ${tx.auditReference}",
                                fontFamily = JetBrainsMonoFontFamily,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        val sign = if (tx.transactionType == "REDEMPTION" || tx.transactionType == "REVERSAL") "-" else "+"
                        Text(
                            text = "$sign${tx.amount} PTS",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = if (sign == "+") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VerificationPipelineCard() {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Receipt Verification Lifecycle",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                PipelineStepNode(
                    icon = Icons.AutoMirrored.Filled.ReceiptLong,
                    label = "1. Submit",
                    bg = BentoButter,
                    ink = OnBentoButter,
                    modifier = Modifier.weight(1f)
                )
                HorizontalDivider(
                    modifier = Modifier.width(18.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                PipelineStepNode(
                    icon = Icons.Default.HourglassTop,
                    label = "2. Review",
                    bg = BentoPeach,
                    ink = OnBentoPeach,
                    modifier = Modifier.weight(1f)
                )
                HorizontalDivider(
                    modifier = Modifier.width(18.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                PipelineStepNode(
                    icon = Icons.Default.Verified,
                    label = "3. Credit",
                    bg = BentoMint,
                    ink = OnBentoMint,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun PipelineStepNode(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    bg: Color,
    ink: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            color = bg,
            shape = CircleShape,
            modifier = Modifier.size(38.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = ink, modifier = Modifier.size(18.dp))
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun WalletBentoTile(
    label: String,
    points: Long,
    bgColor: Color,
    inkColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = bgColor,
        shape = RoundedCornerShape(18.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = inkColor.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$points",
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge,
                color = inkColor
            )
            Text(
                text = "PTS",
                fontFamily = JetBrainsMonoFontFamily,
                style = MaterialTheme.typography.labelSmall,
                color = inkColor.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun ReceiptStatusChip(status: ReceiptReviewStatus) {
    val (bg, fg) = when (status) {
        ReceiptReviewStatus.APPROVED -> Mint100 to Emerald800
        ReceiptReviewStatus.PENDING -> AmberContainer to AmberWarning
        ReceiptReviewStatus.REJECTED -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
    }
    Surface(color = bg, shape = MaterialTheme.shapes.small) {
        Text(
            text = status.name,
            style = MaterialTheme.typography.labelMedium,
            color = fg,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptSubmissionScreen(
    defaultCurrency: String,
    activeCampaigns: List<Campaign>,
    preselectedCampaignId: String?,
    feedbackMessage: String?,
    onClearFeedback: () -> Unit,
    onSubmitReceipt: (store: String, date: String, currency: String, total: String, ref: String, campaignId: String?) -> Unit,
    onBack: () -> Unit
) {
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var storeName by remember { mutableStateOf("") }
    var transactionDate by remember { mutableStateOf("2026-11-22") }
    var currencyCode by remember { mutableStateOf(defaultCurrency) }
    var totalAmount by remember { mutableStateOf("") }
    var receiptRef by remember { mutableStateOf("") }
    var selectedCampaignId by remember { mutableStateOf(preselectedCampaignId) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> selectedImageUri = uri }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Submit Receipt") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                color = AmberContainer,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Uploading a receipt does NOT automatically credit rewards. Every receipt enters a human review queue and is checked for duplicate reference hashes.",
                    style = MaterialTheme.typography.bodySmall,
                    color = AmberWarning,
                    modifier = Modifier.padding(12.dp)
                )
            }

            if (feedbackMessage != null) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth().clickable { onClearFeedback() }
                ) {
                    Text(
                        text = feedbackMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            OutlinedButton(
                onClick = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                modifier = Modifier.fillMaxWidth().height(52.dp).testTag("receipt_pick_photo_button")
            ) {
                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (selectedImageUri == null) "Attach Receipt Photo (Photo Picker)" else "Change Receipt Photo")
            }

            if (selectedImageUri != null) {
                AsyncImage(
                    model = selectedImageUri,
                    contentDescription = "Selected receipt image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(MaterialTheme.shapes.medium)
                )
            }

            OutlinedTextField(
                value = storeName,
                onValueChange = { storeName = it },
                label = { Text("Store Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("receipt_store_input")
            )
            OutlinedTextField(
                value = transactionDate,
                onValueChange = { transactionDate = it },
                label = { Text("Transaction Date (YYYY-MM-DD)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("receipt_date_input")
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = currencyCode,
                    onValueChange = { currencyCode = it.uppercase().take(3) },
                    label = { Text("Currency (ISO)") },
                    singleLine = true,
                    modifier = Modifier.weight(1f).testTag("receipt_currency_input")
                )
                OutlinedTextField(
                    value = totalAmount,
                    onValueChange = { totalAmount = it },
                    label = { Text("Total Amount") },
                    singleLine = true,
                    modifier = Modifier.weight(2f).testTag("receipt_total_input")
                )
            }
            OutlinedTextField(
                value = receiptRef,
                onValueChange = { receiptRef = it },
                label = { Text("Receipt / Invoice Reference #") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("receipt_reference_input")
            )

            if (activeCampaigns.isNotEmpty()) {
                Text("Optional Campaign Tag", style = MaterialTheme.typography.titleMedium)
                activeCampaigns.forEach { camp ->
                    val selected = selectedCampaignId == camp.id
                    Surface(
                        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            1.dp,
                            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                        ),
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedCampaignId = if (selected) null else camp.id }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${camp.title} (+${camp.rewardPoints} PTS)", style = MaterialTheme.typography.bodyMedium)
                            if (selected) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }

            Button(
                onClick = {
                    onSubmitReceipt(storeName, transactionDate, currencyCode, totalAmount, receiptRef, selectedCampaignId)
                },
                modifier = Modifier.fillMaxWidth().height(52.dp).testTag("receipt_submit_button")
            ) {
                Text("Submit for Verification")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailScreen(
    transaction: RewardTransaction?,
    isAdmin: Boolean,
    onReverseTransaction: (RewardTransaction, String) -> Unit,
    onBack: () -> Unit
) {
    var reversalReason by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transaction Ledger Detail") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (transaction == null) {
            Column(
                modifier = Modifier.fillMaxSize().padding(innerPadding).padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.HourglassTop, contentDescription = null)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Transaction not found.")
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(transaction.sourceActivity, style = MaterialTheme.typography.headlineMedium)
                    Text(
                        text = "${transaction.amount} ${transaction.unitCode} • ${transaction.status}",
                        fontFamily = JetBrainsMonoFontFamily,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    HorizontalDivider()
                    DetailRow("Transaction ID", transaction.id)
                    DetailRow("User ID", transaction.userId)
                    DetailRow("Type", transaction.transactionType)
                    DetailRow("Status", transaction.status)
                    DetailRow("Campaign ID", transaction.campaignId ?: "None")
                    DetailRow("Idempotency Key", transaction.idempotencyKey)
                    DetailRow("Audit Reference", transaction.auditReference)
                    DetailRow("Related Tx ID", transaction.relatedTransactionId ?: "None")
                    DetailRow("Created At", formatTimestamp(transaction.createdAt))
                    if (!transaction.notes.isNullOrBlank()) {
                        DetailRow("Notes", transaction.notes)
                    }
                }
            }

            if (isAdmin && TransactionStatus.safeValueOf(transaction.status) == TransactionStatus.CREDITED) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Admin Ledger Reversal", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "Reversals append a new REVERSAL transaction referencing this ID. Historical records are never overwritten.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        OutlinedTextField(
                            value = reversalReason,
                            onValueChange = { reversalReason = it },
                            label = { Text("Audit reason for reversal") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            onClick = { onReverseTransaction(transaction, reversalReason) },
                            enabled = reversalReason.isNotBlank()
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Post Ledger Reversal")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontFamily = JetBrainsMonoFontFamily, style = MaterialTheme.typography.bodySmall)
    }
}
