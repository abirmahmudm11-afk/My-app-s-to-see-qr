package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.Paid
import androidx.compose.material.icons.rounded.PauseCircle
import androidx.compose.material.icons.rounded.PlayCircle
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.Campaign
import com.example.data.model.CampaignStatus
import com.example.data.model.CatalogProduct
import com.example.data.model.DeletionRequest
import com.example.data.model.PayoutMethod
import com.example.data.model.PayoutRequest
import com.example.data.model.PayoutStatus
import com.example.data.model.ReceiptReviewStatus
import com.example.data.model.ReceiptSubmission
import com.example.data.model.UserProfile
import com.example.domain.MarketConfig
import com.example.ui.components.ReceiptStatusBadge
import com.example.ui.components.SectionHeader
import com.example.ui.components.formatTimestamp
import com.example.ui.theme.AmberNotice
import com.example.ui.theme.AmberSoftTint
import com.example.ui.theme.CobaltInfo
import com.example.ui.theme.CobaltSoftTint
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.CrimsonSoftTint
import com.example.ui.theme.DeepObsidian
import com.example.ui.theme.ElectricLime
import com.example.ui.theme.EmeraldSoftTint
import com.example.ui.theme.EmeraldTrust
import com.example.ui.theme.ObsidianElevated
import com.example.ui.viewmodel.MainUiState

@Composable
fun AdminConsoleScreen(
  uiState: MainUiState,
  onBack: () -> Unit,
  onReviewPayout: (PayoutRequest, PayoutStatus, String) -> Unit,
  onUpdateUserStatus: (String, String, String) -> Unit,
  onCreditBonusPoints: (String, Long, String) -> Unit,
  onApproveReceipt: (ReceiptSubmission, Long) -> Unit,
  onRejectReceipt: (ReceiptSubmission, String) -> Unit,
  onCreateCampaign: (partnerName: String, title: String, terms: String, rewardPoints: Long, countries: List<String>) -> Unit,
  onUpdateCampaignStatus: (Campaign, CampaignStatus) -> Unit,
  onVerifyProduct: (CatalogProduct) -> Unit,
  onCompleteDeletion: (DeletionRequest) -> Unit,
) {
  var selectedTab by remember { mutableIntStateOf(0) } // 0=Overview, 1=Users, 2=Payouts, 3=Receipts, 4=Catalog & Campaigns
  var userSearch by remember { mutableStateOf("") }
  var bonusTargetUser by remember { mutableStateOf<UserProfile?>(null) }
  var bonusPointsInput by remember { mutableStateOf("100") }
  var bonusReasonInput by remember { mutableStateOf("Admin loyalty bonus") }

  var partnerName by remember { mutableStateOf("") }
  var campaignTitle by remember { mutableStateOf("") }
  var campaignTerms by remember { mutableStateOf("") }
  var campaignPts by remember { mutableStateOf("150") }
  var campaignCountry by remember { mutableStateOf("US") }

  val pendingPayoutsCount = uiState.allPayoutRequests.count {
    it.status == PayoutStatus.PENDING.name || it.status == PayoutStatus.APPROVED.name
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background),
  ) {
    // Header + Tab Bar
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .background(DeepObsidian)
        .padding(horizontal = 12.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) {
          Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White)
        }
        Column(modifier = Modifier.weight(1f)) {
          Text("Admin Control Center", style = MaterialTheme.typography.titleLarge, color = Color.White)
          Text(
            text = "Role: ${uiState.adminRole?.roleName ?: "UNAUTHORIZED"} • Full Governance",
            style = MaterialTheme.typography.labelSmall,
            color = ElectricLime,
          )
        }
      }

      if (uiState.adminRole != null) {
        val tabs = listOf(
          "Overview",
          "Users (${uiState.allUsers.size})",
          "Payouts ($pendingPayoutsCount)",
          "Receipts (${uiState.pendingReceipts.size})",
          "More",
        )
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          tabs.forEachIndexed { idx, label ->
            val selected = selectedTab == idx
            Surface(
              modifier = Modifier.clickable { selectedTab = idx },
              color = if (selected) ElectricLime else ObsidianElevated,
              shape = RoundedCornerShape(10.dp),
            ) {
              Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (selected) DeepObsidian else Color.White,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
              )
            }
          }
        }
      }
    }

    if (uiState.adminRole == null) {
      Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Administrative Access Required", style = MaterialTheme.typography.titleLarge)
        Text(
          "Sign in with a verified admin account (such as altabmahmud172@gmail.com) to manage users, payouts, receipts, campaigns, and audit logs.",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    } else {
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        when (selectedTab) {
          // TAB 0: OVERVIEW
          0 -> {
            item { SectionHeader("Platform Snapshot") }
            item {
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AdminKpiTile("Total Users", "${uiState.allUsers.size}", Icons.Rounded.Group, CobaltSoftTint, CobaltInfo, Modifier.weight(1f)) {
                  selectedTab = 1
                }
                AdminKpiTile("Open Payouts", "$pendingPayoutsCount", Icons.Rounded.AccountBalance, AmberSoftTint, AmberNotice, Modifier.weight(1f)) {
                  selectedTab = 2
                }
              }
            }
            item {
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AdminKpiTile("Pending Receipts", "${uiState.pendingReceipts.size}", Icons.AutoMirrored.Rounded.ReceiptLong, EmeraldSoftTint, EmeraldTrust, Modifier.weight(1f)) {
                  selectedTab = 3
                }
                AdminKpiTile("Active Campaigns", "${uiState.allCampaigns.count { it.status == "ACTIVE" }}", Icons.Rounded.Paid, CobaltSoftTint, CobaltInfo, Modifier.weight(1f)) {
                  selectedTab = 4
                }
              }
            }
            item {
              SectionHeader("Recent Payout Activity")
            }
            if (uiState.allPayoutRequests.isEmpty()) {
              item { Text("No payout requests logged yet.", style = MaterialTheme.typography.bodySmall) }
            } else {
              items(uiState.allPayoutRequests.take(3), key = { it.id }) { req ->
                AdminPayoutCard(request = req, onReview = onReviewPayout)
              }
            }
            item { SectionHeader("Latest Audit Trail (${uiState.auditLogs.size})") }
            items(uiState.auditLogs.take(5), key = { it.id }) { log ->
              AuditLogCard(log)
            }
          }

          // TAB 1: ALL USERS & DELETION REQUESTS
          1 -> {
            item {
              OutlinedTextField(
                value = userSearch,
                onValueChange = { userSearch = it },
                label = { Text("Search users by name, UID, country") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
              )
            }
            val filteredUsers = uiState.allUsers.filter { u ->
              val q = userSearch.trim().lowercase()
              q.isEmpty() || u.displayName.lowercase().contains(q) || u.userId.lowercase().contains(q) || u.countryCode.lowercase().contains(q)
            }
            item { SectionHeader("Registered Users (${filteredUsers.size})") }
            if (filteredUsers.isEmpty()) {
              item { Text("No users match filter.", style = MaterialTheme.typography.bodySmall) }
            } else {
              items(filteredUsers, key = { it.userId }) { user ->
                AdminUserCard(
                  user = user,
                  onToggleSuspend = {
                    val next = if (user.accountStatus == "SUSPENDED") "ACTIVE" else "SUSPENDED"
                    onUpdateUserStatus(user.userId, next, "Admin toggled status to $next")
                  },
                  onCreditBonus = { bonusTargetUser = user },
                )
              }
            }
            if (uiState.deletionRequests.isNotEmpty()) {
              item { SectionHeader("Account Deletion Requests (${uiState.deletionRequests.size})") }
              items(uiState.deletionRequests, key = { it.id }) { req ->
                Card(
                  shape = RoundedCornerShape(16.dp),
                  colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                  border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                ) {
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                  ) {
                    Column(modifier = Modifier.weight(1f)) {
                      Text("User: ${req.userId}", style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                      Text("${req.status} • ${req.reason}", style = MaterialTheme.typography.bodySmall)
                    }
                    if (req.status == "REQUESTED") {
                      Button(onClick = { onCompleteDeletion(req) }, shape = RoundedCornerShape(10.dp)) {
                        Text("Complete")
                      }
                    }
                  }
                }
              }
            }
          }

          // TAB 2: ALL PAYOUT REQUESTS
          2 -> {
            item { SectionHeader("All User Payout Requests (${uiState.allPayoutRequests.size})") }
            if (uiState.allPayoutRequests.isEmpty()) {
              item {
                Text(
                  "No payout requests submitted by users yet.",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            } else {
              items(uiState.allPayoutRequests, key = { it.id }) { req ->
                AdminPayoutCard(request = req, onReview = onReviewPayout)
              }
            }
          }

          // TAB 3: RECEIPTS (PENDING + HISTORY)
          3 -> {
            item { SectionHeader("Pending Receipts Queue (${uiState.pendingReceipts.size})") }
            if (uiState.pendingReceipts.isEmpty()) {
              item { Text("No receipts waiting in review queue.", style = MaterialTheme.typography.bodySmall) }
            } else {
              items(uiState.pendingReceipts, key = { it.id }) { receipt ->
                PendingReceiptAdminCard(
                  receipt = receipt,
                  onApprove = { pts -> onApproveReceipt(receipt, pts) },
                  onReject = { reason -> onRejectReceipt(receipt, reason) },
                )
              }
            }
            val reviewedReceipts = uiState.allReceipts.filter { it.reviewStatus != ReceiptReviewStatus.PENDING.name }
            if (reviewedReceipts.isNotEmpty()) {
              item { SectionHeader("Reviewed Receipts (${reviewedReceipts.size})") }
              items(reviewedReceipts.take(15), key = { it.id }) { r ->
                Card(
                  shape = RoundedCornerShape(14.dp),
                  colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                  border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                ) {
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                  ) {
                    Column(modifier = Modifier.weight(1f)) {
                      Text(r.storeName, style = MaterialTheme.typography.titleMedium)
                      Text("${MarketConfig.formatMoney(r.totalAmount, r.currencyCode)} • ${r.receiptReference}", style = MaterialTheme.typography.bodySmall)
                    }
                    ReceiptStatusBadge(statusRaw = r.reviewStatus)
                  }
                }
              }
            }
          }

          // TAB 4: CAMPAIGNS, CATALOG VERIFICATION & AUDIT LOGS
          4 -> {
            item { SectionHeader("Unverified Community Products (${uiState.unverifiedCatalog.size})") }
            items(uiState.unverifiedCatalog, key = { it.barcode }) { product ->
              Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                  Column(modifier = Modifier.weight(1f)) {
                    Text(product.name, style = MaterialTheme.typography.titleMedium)
                    Text("${product.brand} • ${product.barcode}", style = MaterialTheme.typography.labelSmall)
                  }
                  Button(onClick = { onVerifyProduct(product) }, shape = RoundedCornerShape(10.dp)) {
                    Text("Verify")
                  }
                }
              }
            }

            item { SectionHeader("Manage Campaigns (${uiState.allCampaigns.size})") }
            items(uiState.allCampaigns, key = { it.id }) { camp ->
              val isActive = camp.status == CampaignStatus.ACTIVE.name
              Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                  Column(modifier = Modifier.weight(1f)) {
                    Text(camp.title, style = MaterialTheme.typography.titleMedium)
                    Text("${camp.partnerName} • +${camp.rewardPoints} PTS • ${camp.status}", style = MaterialTheme.typography.bodySmall)
                  }
                  OutlinedButton(
                    onClick = {
                      onUpdateCampaignStatus(
                        camp,
                        if (isActive) CampaignStatus.PAUSED else CampaignStatus.ACTIVE,
                      )
                    },
                    shape = RoundedCornerShape(10.dp),
                  ) {
                    Icon(
                      imageVector = if (isActive) Icons.Rounded.PauseCircle else Icons.Rounded.PlayCircle,
                      contentDescription = null,
                      modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(if (isActive) "Pause" else "Activate")
                  }
                }
              }
            }

            item { SectionHeader("Launch Partner Campaign") }
            item {
              Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
              ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                  OutlinedTextField(
                    value = partnerName,
                    onValueChange = { partnerName = it },
                    label = { Text("Partner Brand") },
                    modifier = Modifier.fillMaxWidth(),
                  )
                  OutlinedTextField(
                    value = campaignTitle,
                    onValueChange = { campaignTitle = it },
                    label = { Text("Campaign Title") },
                    modifier = Modifier.fillMaxWidth(),
                  )
                  OutlinedTextField(
                    value = campaignTerms,
                    onValueChange = { campaignTerms = it },
                    label = { Text("Eligibility Terms") },
                    modifier = Modifier.fillMaxWidth(),
                  )
                  Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                      value = campaignPts,
                      onValueChange = { campaignPts = it },
                      label = { Text("Reward PTS") },
                      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                      modifier = Modifier.weight(1f),
                    )
                    OutlinedTextField(
                      value = campaignCountry,
                      onValueChange = { campaignCountry = it },
                      label = { Text("Country (US, GLOBAL)") },
                      modifier = Modifier.weight(1f),
                    )
                  }
                  Button(
                    onClick = {
                      onCreateCampaign(
                        partnerName,
                        campaignTitle,
                        campaignTerms,
                        campaignPts.toLongOrNull() ?: 100L,
                        campaignCountry.split(",").map { it.trim().uppercase() }.filter { it.isNotEmpty() },
                      )
                      partnerName = ""
                      campaignTitle = ""
                      campaignTerms = ""
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                  ) {
                    Text("Publish Active Campaign")
                  }
                }
              }
            }

            item { SectionHeader("Immutable Audit Logs (${uiState.auditLogs.size})") }
            items(uiState.auditLogs.take(25), key = { it.id }) { log ->
              AuditLogCard(log)
            }
          }
        }
      }
    }
  }

  bonusTargetUser?.let { target ->
    AlertDialog(
      onDismissRequest = { bonusTargetUser = null },
      title = { Text("Credit Bonus to ${target.displayName}") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = bonusPointsInput,
            onValueChange = { bonusPointsInput = it },
            label = { Text("Bonus PTS") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
          )
          OutlinedTextField(
            value = bonusReasonInput,
            onValueChange = { bonusReasonInput = it },
            label = { Text("Audit Reason") },
            modifier = Modifier.fillMaxWidth(),
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val pts = bonusPointsInput.toLongOrNull() ?: 100L
            bonusTargetUser = null
            onCreditBonusPoints(target.userId, pts, bonusReasonInput)
          },
        ) {
          Text("Credit PTS")
        }
      },
      dismissButton = {
        TextButton(onClick = { bonusTargetUser = null }) { Text("Cancel") }
      },
    )
  }
}

@Composable
private fun AdminKpiTile(
  label: String,
  value: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  bg: Color,
  fg: Color,
  modifier: Modifier = Modifier,
  onClick: () -> Unit,
) {
  Card(
    modifier = modifier.clickable(onClick = onClick),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
  ) {
    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
      Surface(color = bg, shape = RoundedCornerShape(8.dp)) {
        Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.padding(6.dp).size(18.dp))
      }
      Text(value, style = MaterialTheme.typography.headlineLarge)
      Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
  }
}

@Composable
private fun AdminUserCard(
  user: UserProfile,
  onToggleSuspend: () -> Unit,
  onCreditBonus: () -> Unit,
) {
  val isSuspended = user.accountStatus == "SUSPENDED"
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
  ) {
    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(user.displayName, style = MaterialTheme.typography.titleMedium)
          Text(
            "${user.countryCode} (${user.currencyCode}) • UID: ${user.userId.take(12)}...",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        Surface(
          color = if (isSuspended) CrimsonSoftTint else EmeraldSoftTint,
          shape = RoundedCornerShape(8.dp),
        ) {
          Text(
            text = user.accountStatus,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSuspended) CrimsonAlert else EmeraldTrust,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
          )
        }
      }
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(
          onClick = onCreditBonus,
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(10.dp),
        ) {
          Icon(Icons.Rounded.AddCircle, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(Modifier.width(4.dp))
          Text("Credit PTS")
        }
        OutlinedButton(
          onClick = onToggleSuspend,
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(10.dp),
        ) {
          Text(
            text = if (isSuspended) "Reactivate" else "Suspend",
            color = if (isSuspended) EmeraldTrust else CrimsonAlert,
          )
        }
      }
    }
  }
}

@Composable
private fun AdminPayoutCard(
  request: PayoutRequest,
  onReview: (PayoutRequest, PayoutStatus, String) -> Unit,
) {
  var noteInput by remember { mutableStateOf(request.adminNotes.orEmpty()) }
  val status = PayoutStatus.fromString(request.status)
  val (bg, fg) = when (status) {
    PayoutStatus.PAID, PayoutStatus.APPROVED -> EmeraldSoftTint to EmeraldTrust
    PayoutStatus.REJECTED -> CrimsonSoftTint to CrimsonAlert
    PayoutStatus.PENDING -> AmberSoftTint to AmberNotice
  }

  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
  ) {
    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text("${request.userDisplayName} • ${request.pointsAmount} PTS", style = MaterialTheme.typography.titleMedium)
          Text(
            "${request.rewardTitle} (${PayoutMethod.fromString(request.payoutMethod).label})",
            style = MaterialTheme.typography.bodySmall,
          )
        }
        Surface(color = bg, shape = RoundedCornerShape(8.dp)) {
          Text(
            text = status.name,
            style = MaterialTheme.typography.labelSmall,
            color = fg,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
          )
        }
      }
      Text(
        "Destination: ${request.destinationHandle} • ${formatTimestamp(request.requestedAt)}",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )

      if (status == PayoutStatus.PENDING || status == PayoutStatus.APPROVED) {
        OutlinedTextField(
          value = noteInput,
          onValueChange = { noteInput = it },
          label = { Text("Admin note / reference") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          OutlinedButton(
            onClick = { onReview(request, PayoutStatus.REJECTED, noteInput) },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(10.dp),
          ) {
            Text("Reject+Refund", color = CrimsonAlert, style = MaterialTheme.typography.labelSmall)
          }
          if (status == PayoutStatus.PENDING) {
            OutlinedButton(
              onClick = { onReview(request, PayoutStatus.APPROVED, noteInput) },
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(10.dp),
            ) {
              Text("Approve", style = MaterialTheme.typography.labelSmall)
            }
          }
          Button(
            onClick = { onReview(request, PayoutStatus.PAID, noteInput) },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(10.dp),
          ) {
            Text("Mark Paid", style = MaterialTheme.typography.labelSmall)
          }
        }
      }
    }
  }
}

@Composable
private fun PendingReceiptAdminCard(
  receipt: ReceiptSubmission,
  onApprove: (Long) -> Unit,
  onReject: (String) -> Unit,
) {
  var pointsText by remember { mutableStateOf("100") }
  var rejectReason by remember { mutableStateOf("") }

  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
  ) {
    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Text(receipt.storeName, style = MaterialTheme.typography.titleMedium)
      Text(
        "${MarketConfig.formatMoney(receipt.totalAmount, receipt.currencyCode)} • ${receipt.transactionDate} • Ref ${receipt.receiptReference}",
        style = MaterialTheme.typography.bodySmall,
      )
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
          value = pointsText,
          onValueChange = { pointsText = it },
          label = { Text("PTS") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          modifier = Modifier.weight(1f),
        )
        OutlinedTextField(
          value = rejectReason,
          onValueChange = { rejectReason = it },
          label = { Text("Reject note") },
          modifier = Modifier.weight(1.5f),
        )
      }
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(
          onClick = { onReject(rejectReason) },
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(10.dp),
        ) {
          Icon(Icons.Rounded.Close, contentDescription = null, tint = CrimsonAlert)
          Spacer(Modifier.width(4.dp))
          Text("Reject", color = CrimsonAlert)
        }
        Button(
          onClick = { onApprove(pointsText.toLongOrNull() ?: 100L) },
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(10.dp),
        ) {
          Icon(Icons.Rounded.Check, contentDescription = null)
          Spacer(Modifier.width(4.dp))
          Text("Approve")
        }
      }
    }
  }
}

@Composable
private fun AuditLogCard(log: com.example.data.model.AuditLog) {
  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Text("${log.action} • ${log.entityType} (${log.entityId})", style = MaterialTheme.typography.labelMedium)
      Text(log.reason, style = MaterialTheme.typography.bodySmall)
      Text(formatTimestamp(log.timestamp), style = MaterialTheme.typography.labelSmall)
    }
  }
}
