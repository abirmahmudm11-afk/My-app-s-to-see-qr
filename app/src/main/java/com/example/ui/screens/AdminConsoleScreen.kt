package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AdminRole
import com.example.data.model.AuditLog
import com.example.data.model.Campaign
import com.example.data.model.CampaignStatus
import com.example.data.model.CatalogProduct
import com.example.data.model.ReceiptSubmission
import com.example.domain.MarketCatalog
import com.example.ui.components.formatTimestamp
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.viewmodel.AdminConsoleViewModel
import com.example.ui.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminConsoleScreen(
    adminRole: AdminRole?,
    viewModel: AdminConsoleViewModel,
    onBack: () -> Unit
) {
    val pendingReceipts by viewModel.pendingReceipts.collectAsStateWithLifecycle()
    val allCampaigns by viewModel.allCampaigns.collectAsStateWithLifecycle()
    val unverifiedCatalog by viewModel.unverifiedCatalog.collectAsStateWithLifecycle()
    val auditLogs by viewModel.auditLogs.collectAsStateWithLifecycle()
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Receipts", "Campaigns", "Catalog", "Audit Logs")

    var rejectingReceipt by remember { mutableStateOf<ReceiptSubmission?>(null) }
    var rejectReason by remember { mutableStateOf("") }
    var showCreateCampaign by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Admin Console")
                        Text(
                            text = "RBAC Role: ${adminRole?.roleName ?: "NONE"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (adminRole == null) {
                Text(
                    text = "Access denied: Role-based admin permission required.",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
                return@Scaffold
            }

            if (statusMessage != null) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth().clickable { viewModel.clearStatusMessage() }
                ) {
                    Text(
                        text = statusMessage.orEmpty(),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, maxLines = 1) }
                    )
                }
            }

            when (selectedTab) {
                0 -> {
                    val list = (pendingReceipts as? UiState.Success<List<ReceiptSubmission>>)?.data.orEmpty()
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (list.isEmpty()) {
                            item { Text("No pending receipts in queue.", style = MaterialTheme.typography.bodyMedium) }
                        } else {
                            items(list, key = { it.id }) { receipt ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(receipt.storeName, style = MaterialTheme.typography.titleMedium)
                                        Text(
                                            text = "${MarketCatalog.formatMoney(receipt.totalAmount, receipt.currencyCode)} • Ref: ${receipt.receiptReference}",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        Text(
                                            text = "Hash: ${receipt.receiptReferenceHash.take(18)}...",
                                            fontFamily = JetBrainsMonoFontFamily,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Button(
                                                onClick = { viewModel.approveReceipt(receipt, 50L) },
                                                modifier = Modifier.weight(1f).testTag("admin_approve_receipt_${receipt.id}")
                                            ) {
                                                Text("Approve (+50 PTS)")
                                            }
                                            OutlinedButton(
                                                onClick = { rejectingReceipt = receipt },
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("Reject")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    val list = (allCampaigns as? UiState.Success<List<Campaign>>)?.data.orEmpty()
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Button(
                                onClick = { showCreateCampaign = true },
                                modifier = Modifier.fillMaxWidth().testTag("admin_create_campaign_button")
                            ) {
                                Text("Create Sandbox Campaign")
                            }
                        }
                        items(list, key = { it.id }) { camp ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("${camp.title} [${camp.status}]", style = MaterialTheme.typography.titleMedium)
                                    Text("${camp.partnerName} • +${camp.rewardPoints} PTS", style = MaterialTheme.typography.bodySmall)
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        if (camp.status != CampaignStatus.ACTIVE.name) {
                                            Button(onClick = { viewModel.updateCampaignStatus(camp.id, CampaignStatus.ACTIVE) }) {
                                                Text("Activate")
                                            }
                                        }
                                        if (camp.status == CampaignStatus.ACTIVE.name) {
                                            OutlinedButton(onClick = { viewModel.updateCampaignStatus(camp.id, CampaignStatus.PAUSED) }) {
                                                Text("Pause")
                                            }
                                        }
                                        OutlinedButton(onClick = { viewModel.updateCampaignStatus(camp.id, CampaignStatus.ARCHIVED) }) {
                                            Text("Archive")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> {
                    val list = (unverifiedCatalog as? UiState.Success<List<CatalogProduct>>)?.data.orEmpty()
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (list.isEmpty()) {
                            item { Text("No unverified community product submissions pending.", style = MaterialTheme.typography.bodyMedium) }
                        } else {
                            items(list, key = { it.barcode }) { prod ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(prod.name, style = MaterialTheme.typography.titleMedium)
                                        Text("GTIN: ${prod.barcode} • Brand: ${prod.brand}", fontFamily = JetBrainsMonoFontFamily, style = MaterialTheme.typography.labelSmall)
                                        Button(onClick = { viewModel.verifyProduct(prod.barcode) }) {
                                            Text("Verify as Merchant Record")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                3 -> {
                    val logs = (auditLogs as? UiState.Success<List<AuditLog>>)?.data.orEmpty()
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(logs, key = { it.id }) { log ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("${log.action} • ${log.entityType}", style = MaterialTheme.typography.titleMedium)
                                    Text(log.reason, style = MaterialTheme.typography.bodySmall)
                                    Text(
                                        text = "Actor: ${log.actorId.take(10)} • ${formatTimestamp(log.timestamp)}",
                                        fontFamily = JetBrainsMonoFontFamily,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (rejectingReceipt != null) {
        AlertDialog(
            onDismissRequest = { rejectingReceipt = null },
            title = { Text("Reject Receipt") },
            text = {
                OutlinedTextField(
                    value = rejectReason,
                    onValueChange = { rejectReason = it },
                    label = { Text("Mandatory rejection reason") }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val target = rejectingReceipt!!
                        rejectingReceipt = null
                        viewModel.rejectReceipt(target, rejectReason)
                        rejectReason = ""
                    },
                    enabled = rejectReason.isNotBlank()
                ) { Text("Reject") }
            },
            dismissButton = {
                TextButton(onClick = { rejectingReceipt = null }) { Text("Cancel") }
            }
        )
    }

    if (showCreateCampaign) {
        var partner by remember { mutableStateOf("GreenMarket Pilot") }
        var title by remember { mutableStateOf("") }
        var terms by remember { mutableStateOf("Submit a verified grocery receipt to earn sandbox points.") }
        var pointsText by remember { mutableStateOf("50") }
        var countries by remember { mutableStateOf("US,GB,DE,JP,SG,CA") }
        var categories by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showCreateCampaign = false },
            title = { Text("Create Sandbox Campaign") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = partner, onValueChange = { partner = it }, label = { Text("Partner Name*") })
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Campaign Title*") })
                    OutlinedTextField(value = pointsText, onValueChange = { pointsText = it }, label = { Text("Reward Points*") })
                    OutlinedTextField(value = countries, onValueChange = { countries = it }, label = { Text("Countries CSV*") })
                    OutlinedTextField(value = categories, onValueChange = { categories = it }, label = { Text("Categories CSV (optional)") })
                    OutlinedTextField(value = terms, onValueChange = { terms = it }, label = { Text("Terms*") })
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCreateCampaign = false
                        viewModel.createCampaign(
                            partnerName = partner,
                            title = title,
                            terms = terms,
                            rewardPoints = pointsText.toLongOrNull() ?: 50L,
                            countriesCsv = countries,
                            categoriesCsv = categories
                        )
                    },
                    enabled = partner.isNotBlank() && title.isNotBlank()
                ) { Text("Publish Active") }
            },
            dismissButton = {
                TextButton(onClick = { showCreateCampaign = false }) { Text("Cancel") }
            }
        )
    }
}
