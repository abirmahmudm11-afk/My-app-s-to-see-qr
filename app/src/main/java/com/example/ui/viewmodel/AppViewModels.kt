package com.example.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.AdminRole
import com.example.data.model.AppNotification
import com.example.data.model.AppPreferences
import com.example.data.model.AuditLog
import com.example.data.model.Campaign
import com.example.data.model.CampaignStatus
import com.example.data.model.CatalogProduct
import com.example.data.model.DeletionRequest
import com.example.data.model.PayoutMethod
import com.example.data.model.PayoutRequest
import com.example.data.model.PayoutStatus
import com.example.data.model.ReceiptSubmission
import com.example.data.model.RewardTransaction
import com.example.data.model.RewardWallet
import com.example.data.model.ScanContext
import com.example.data.model.ScanRecord
import com.example.data.model.UserProfile
import com.example.di.AppContainer
import com.example.domain.MarketConfig
import com.example.domain.QuestMissionEngine
import com.example.domain.ShopperMission
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MainUiState(
  val firebaseUser: FirebaseUser? = null,
  val preferencesLoaded: Boolean = false,
  val preferences: AppPreferences = AppPreferences(),
  val userProfile: UserProfile? = null,
  val wallet: RewardWallet = RewardWallet(),
  val transactions: List<RewardTransaction> = emptyList(),
  val receipts: List<ReceiptSubmission> = emptyList(),
  val payoutRequests: List<PayoutRequest> = emptyList(),
  val notifications: List<AppNotification> = emptyList(),
  val campaigns: List<Campaign> = emptyList(),
  val recentScans: List<ScanRecord> = emptyList(),
  val localScanHistory: List<ScanRecord> = emptyList(),
  val cachedCatalog: List<CatalogProduct> = emptyList(),
  val shopperMissions: List<ShopperMission> = emptyList(),
  val adminRole: AdminRole? = null,
  val allUsers: List<UserProfile> = emptyList(),
  val allPayoutRequests: List<PayoutRequest> = emptyList(),
  val pendingReceipts: List<ReceiptSubmission> = emptyList(),
  val allReceipts: List<ReceiptSubmission> = emptyList(),
  val allCampaigns: List<Campaign> = emptyList(),
  val unverifiedCatalog: List<CatalogProduct> = emptyList(),
  val deletionRequests: List<DeletionRequest> = emptyList(),
  val auditLogs: List<AuditLog> = emptyList(),
  val searchResults: List<CatalogProduct> = emptyList(),
  val isSearching: Boolean = false,
  val searchError: String? = null,
  val isSigningIn: Boolean = false,
  val activeScanContext: ScanContext? = null,
  val feedbackMessage: String? = null,
)

private data class CoreSessionData(
  val user: FirebaseUser?,
  val prefsLoaded: Boolean,
  val prefs: AppPreferences,
  val profile: UserProfile?,
  val wallet: RewardWallet,
  val transactions: List<RewardTransaction>,
)

private data class SecondarySessionData(
  val receipts: List<ReceiptSubmission>,
  val payoutRequests: List<PayoutRequest>,
  val notifications: List<AppNotification>,
  val campaigns: List<Campaign>,
  val cloudScans: List<ScanRecord>,
  val localScans: List<ScanRecord>,
  val cachedCatalog: List<CatalogProduct>,
)

private data class AdminSessionData(
  val role: AdminRole?,
  val allUsers: List<UserProfile>,
  val allPayoutRequests: List<PayoutRequest>,
  val pendingReceipts: List<ReceiptSubmission>,
  val allReceipts: List<ReceiptSubmission>,
  val allCampaigns: List<Campaign>,
  val unverifiedCatalog: List<CatalogProduct>,
  val deletionRequests: List<DeletionRequest>,
  val auditLogs: List<AuditLog>,
)

@OptIn(ExperimentalCoroutinesApi::class)
class MainAppViewModel(private val container: AppContainer) : ViewModel() {
  private val authUserFlow = MutableStateFlow(container.auth.currentUser)
  private val adminRoleFlow = MutableStateFlow<AdminRole?>(null)
  private val searchResultsFlow = MutableStateFlow<List<CatalogProduct>>(emptyList())
  private val isSearchingFlow = MutableStateFlow(false)
  private val searchErrorFlow = MutableStateFlow<String?>(null)
  private val isSigningInFlow = MutableStateFlow(false)
  private val activeScanContextFlow = MutableStateFlow<ScanContext?>(null)
  private val feedbackMessageFlow = MutableStateFlow<String?>(null)

  private val nullablePrefsState: StateFlow<AppPreferences?> = container.preferencesDataStore.preferencesFlow
    .map<AppPreferences, AppPreferences?> { it }
    .stateIn(viewModelScope, SharingStarted.Eagerly, null)

  private val currentPrefs: AppPreferences
    get() = nullablePrefsState.value ?: AppPreferences()

  private val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
    val user = firebaseAuth.currentUser
    authUserFlow.value = user
    if (user != null) {
      viewModelScope.launch {
        runCatching {
          val prefs = currentPrefs
          container.userWalletRepository.ensureAccountInitialized(
            userId = user.uid,
            displayName = user.displayName ?: user.email?.substringBefore("@") ?: "Shopper",
            countryCode = prefs.countryCode,
            languageCode = prefs.languageCode,
            darkThemeEnabled = prefs.darkThemeEnabled,
          )
        }
        adminRoleFlow.value = runCatching { container.adminRepository.resolveAdminRole() }.getOrNull()
      }
    } else {
      adminRoleFlow.value = null
    }
  }

  init {
    container.auth.addAuthStateListener(authListener)
    viewModelScope.launch {
      runCatching { container.productRepository.preloadLiveCatalog() }
    }
    container.auth.currentUser?.let { existing ->
      viewModelScope.launch {
        runCatching {
          val prefs = currentPrefs
          container.userWalletRepository.ensureAccountInitialized(
            userId = existing.uid,
            displayName = existing.displayName ?: existing.email?.substringBefore("@") ?: "Shopper",
            countryCode = prefs.countryCode,
            languageCode = prefs.languageCode,
            darkThemeEnabled = prefs.darkThemeEnabled,
          )
        }
        adminRoleFlow.value = runCatching { container.adminRepository.resolveAdminRole() }.getOrNull()
      }
    }
  }

  override fun onCleared() {
    container.auth.removeAuthStateListener(authListener)
    super.onCleared()
  }

  private fun <T> Flow<T>.safeOr(default: T): Flow<T> = catch { emit(default) }

  private val coreFlow = authUserFlow.flatMapLatest { user ->
    if (user == null) {
      combine(authUserFlow, nullablePrefsState) { u, prefs ->
        CoreSessionData(u, prefs != null, prefs ?: AppPreferences(), null, RewardWallet(), emptyList())
      }
    } else {
      combine(
        authUserFlow,
        nullablePrefsState,
        container.userWalletRepository.observeUserProfile(user.uid).safeOr(null),
        container.userWalletRepository.observeWallet(user.uid).safeOr(RewardWallet(userId = user.uid)),
        container.userWalletRepository.observeTransactions(user.uid).safeOr(emptyList()),
      ) { u, prefs, profile, wallet, txs ->
        CoreSessionData(u, prefs != null, prefs ?: AppPreferences(), profile, wallet, txs)
      }
    }
  }

  private val secondaryFlow = authUserFlow.flatMapLatest { user ->
    val localScansFlow = container.userWalletRepository.observeLocalScans().safeOr(emptyList())
    val catalogFlow = container.productRepository.observeCachedCatalog().safeOr(emptyList())
    if (user == null) {
      combine(localScansFlow, catalogFlow) { localScans, catalog ->
        SecondarySessionData(
          receipts = emptyList(),
          payoutRequests = emptyList(),
          notifications = emptyList(),
          campaigns = emptyList(),
          cloudScans = emptyList(),
          localScans = localScans,
          cachedCatalog = catalog,
        )
      }
    } else {
      combine(
        combine(
          container.receiptCampaignRepository.observeUserReceipts(user.uid).safeOr(emptyList()),
          container.userWalletRepository.observeUserPayoutRequests(user.uid).safeOr(emptyList()),
          container.userWalletRepository.observeNotifications(user.uid).safeOr(emptyList()),
          container.receiptCampaignRepository.observeActiveCampaigns().safeOr(emptyList()),
        ) { receipts, payouts, notifs, campaigns ->
          arrayOf(receipts, payouts, notifs, campaigns)
        },
        combine(
          container.userWalletRepository.observeCloudScans(user.uid).safeOr(emptyList()),
          localScansFlow,
          catalogFlow,
        ) { cloudScans, localScans, catalog -> Triple(cloudScans, localScans, catalog) },
      ) { first, second ->
        @Suppress("UNCHECKED_CAST")
        SecondarySessionData(
          receipts = first[0] as List<ReceiptSubmission>,
          payoutRequests = first[1] as List<PayoutRequest>,
          notifications = first[2] as List<AppNotification>,
          campaigns = first[3] as List<Campaign>,
          cloudScans = second.first,
          localScans = second.second,
          cachedCatalog = second.third,
        )
      }
    }
  }

  private val adminFlow = adminRoleFlow.flatMapLatest { role ->
    if (role == null) {
      flowOf(
        AdminSessionData(
          role = null,
          allUsers = emptyList(),
          allPayoutRequests = emptyList(),
          pendingReceipts = emptyList(),
          allReceipts = emptyList(),
          allCampaigns = emptyList(),
          unverifiedCatalog = emptyList(),
          deletionRequests = emptyList(),
          auditLogs = emptyList(),
        ),
      )
    } else {
      combine(
        combine(
          container.adminRepository.observeAllUsers().safeOr(emptyList()),
          container.adminRepository.observeAllPayoutRequests().safeOr(emptyList()),
          container.adminRepository.observePendingReceipts().safeOr(emptyList()),
          container.adminRepository.observeAllReceipts().safeOr(emptyList()),
        ) { users, payouts, pendingR, allR -> arrayOf(users, payouts, pendingR, allR) },
        combine(
          container.adminRepository.observeAllCampaigns().safeOr(emptyList()),
          container.adminRepository.observeUnverifiedCatalog().safeOr(emptyList()),
          container.adminRepository.observeDeletionRequests().safeOr(emptyList()),
          container.adminRepository.observeAuditLogs().safeOr(emptyList()),
        ) { camps, unverified, deletions, logs -> arrayOf(camps, unverified, deletions, logs) },
      ) { a, b ->
        @Suppress("UNCHECKED_CAST")
        AdminSessionData(
          role = role,
          allUsers = a[0] as List<UserProfile>,
          allPayoutRequests = a[1] as List<PayoutRequest>,
          pendingReceipts = a[2] as List<ReceiptSubmission>,
          allReceipts = a[3] as List<ReceiptSubmission>,
          allCampaigns = b[0] as List<Campaign>,
          unverifiedCatalog = b[1] as List<CatalogProduct>,
          deletionRequests = b[2] as List<DeletionRequest>,
          auditLogs = b[3] as List<AuditLog>,
        )
      }
    }
  }

  val uiState: StateFlow<MainUiState> = combine(
    combine(coreFlow, secondaryFlow, adminFlow) { c, s, a -> Triple(c, s, a) },
    combine(
      searchResultsFlow,
      isSearchingFlow,
      searchErrorFlow,
      isSigningInFlow,
      activeScanContextFlow,
    ) { results, searching, searchErr, signingIn, scanCtx ->
      arrayOf<Any?>(results, searching, searchErr, signingIn, scanCtx)
    },
    feedbackMessageFlow,
  ) { session, uiFlags, feedback ->
    val core = session.first
    val sec = session.second
    val admin = session.third
    @Suppress("UNCHECKED_CAST")
    val searchResults = uiFlags[0] as List<CatalogProduct>
    val isSearching = uiFlags[1] as Boolean
    val searchError = uiFlags[2] as String?
    val isSigningIn = uiFlags[3] as Boolean
    val scanContext = uiFlags[4] as ScanContext?

    // Prefer Room localScans when deduplicating because localScans carry richer product details & delete IDs
    val mergedScans = (sec.localScans + sec.cloudScans)
      .distinctBy { "${it.barcodeValue}|${it.scannedAt?.seconds ?: (it.localTimestampMs / 1000L)}" }
      .sortedByDescending { it.scannedAt?.seconds ?: (it.localTimestampMs / 1000L) }

    val missions = QuestMissionEngine.computeMissions(
      scans = mergedScans,
      receipts = sec.receipts,
      wallet = core.wallet,
    )

    MainUiState(
      firebaseUser = core.user,
      preferencesLoaded = core.prefsLoaded,
      preferences = core.prefs,
      userProfile = core.profile,
      wallet = core.wallet,
      transactions = core.transactions,
      receipts = sec.receipts,
      payoutRequests = sec.payoutRequests,
      notifications = sec.notifications,
      campaigns = sec.campaigns,
      recentScans = mergedScans,
      localScanHistory = sec.localScans,
      cachedCatalog = sec.cachedCatalog,
      shopperMissions = missions,
      adminRole = admin.role,
      allUsers = admin.allUsers,
      allPayoutRequests = admin.allPayoutRequests,
      pendingReceipts = admin.pendingReceipts,
      allReceipts = admin.allReceipts,
      allCampaigns = admin.allCampaigns,
      unverifiedCatalog = admin.unverifiedCatalog,
      deletionRequests = admin.deletionRequests,
      auditLogs = admin.auditLogs,
      searchResults = searchResults,
      isSearching = isSearching,
      searchError = searchError,
      isSigningIn = isSigningIn,
      activeScanContext = scanContext,
      feedbackMessage = feedback,
    )
  }.flowOn(Dispatchers.Default)
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), MainUiState())

  fun setScanContext(context: ScanContext) {
    activeScanContextFlow.value = context
  }

  fun clearScanContext() {
    activeScanContextFlow.value = null
  }

  fun clearFeedback() {
    feedbackMessageFlow.value = null
  }

  fun showFeedback(message: String) {
    feedbackMessageFlow.value = message
  }

  fun completeOnboarding(countryCode: String? = null) {
    viewModelScope.launch {
      if (countryCode != null) {
        val market = MarketConfig.findByCountry(countryCode)
        container.preferencesDataStore.setRegion(market.countryCode, market.defaultLanguage)
      }
      container.preferencesDataStore.setOnboardingCompleted(true)
    }
  }

  fun enterGuestMode() {
    viewModelScope.launch {
      container.preferencesDataStore.setOnboardingCompleted(true)
      container.preferencesDataStore.setGuestMode(true)
    }
  }

  fun attemptAutoSignIn(activityContext: Context) {
    viewModelScope.launch {
      val prefs = currentPrefs
      if (prefs.onboardingCompleted && !prefs.guestMode && container.auth.currentUser == null) {
        val user = container.credentialAuth.attemptAutoSignIn(activityContext)
        if (user != null) {
          handleAuthenticatedUser(user, "Signed in as ${user.displayName ?: user.email ?: "Shopper"}")
        }
      }
    }
  }

  fun signInWithGoogle(activityContext: Context) {
    viewModelScope.launch {
      isSigningInFlow.value = true
      val result = container.credentialAuth.signInWithGoogle(activityContext)
      isSigningInFlow.value = false
      result.onSuccess { user ->
        handleAuthenticatedUser(user, "Signed in as ${user.displayName ?: user.email ?: "Shopper"}")
      }.onFailure { err ->
        feedbackMessageFlow.value = err.message ?: "Google sign-in cancelled or unavailable."
      }
    }
  }

  private suspend fun handleAuthenticatedUser(user: FirebaseUser, message: String) {
    container.preferencesDataStore.setOnboardingCompleted(true)
    container.preferencesDataStore.setGuestMode(false)
    val prefs = currentPrefs
    runCatching {
      container.userWalletRepository.ensureAccountInitialized(
        userId = user.uid,
        displayName = user.displayName ?: user.email?.substringBefore("@") ?: "Shopper",
        countryCode = prefs.countryCode,
        languageCode = prefs.languageCode,
        darkThemeEnabled = prefs.darkThemeEnabled,
      )
    }
    adminRoleFlow.value = runCatching { container.adminRepository.resolveAdminRole() }.getOrNull()
    feedbackMessageFlow.value = message
  }

  fun signOut(context: Context) {
    viewModelScope.launch {
      container.credentialAuth.signOut(context)
      container.preferencesDataStore.setGuestMode(false)
      feedbackMessageFlow.value = "Signed out of SCAN2MONEY."
    }
  }

  fun deleteHistoryEntry(localHistoryId: Long) {
    viewModelScope.launch {
      runCatching { container.userWalletRepository.deleteLocalHistoryEntry(localHistoryId) }
        .onSuccess { feedbackMessageFlow.value = "Removed entry from Scan History." }
        .onFailure { feedbackMessageFlow.value = it.message ?: "Could not delete entry." }
    }
  }

  fun clearScanHistory() {
    viewModelScope.launch {
      runCatching { container.userWalletRepository.clearLocalHistory() }
        .onSuccess { feedbackMessageFlow.value = "Cleared local Scan History." }
        .onFailure { feedbackMessageFlow.value = it.message ?: "Could not clear history." }
    }
  }

  fun updateRegion(countryCode: String, languageCode: String) {
    viewModelScope.launch {
      val market = MarketConfig.findByCountry(countryCode)
      container.preferencesDataStore.setRegion(market.countryCode, languageCode)
      runCatching {
        container.userWalletRepository.updatePreferences(
          countryCode = market.countryCode,
          languageCode = languageCode,
          currencyCode = market.currencyCode,
        )
      }
      feedbackMessageFlow.value = "Region updated to ${market.countryName} (${market.currencyCode})."
    }
  }

  fun setDarkTheme(enabled: Boolean) {
    viewModelScope.launch {
      container.preferencesDataStore.setDarkTheme(enabled)
      runCatching { container.userWalletRepository.updatePreferences(darkThemeEnabled = enabled) }
    }
  }

  fun setReducedMotion(enabled: Boolean) {
    viewModelScope.launch {
      container.preferencesDataStore.setReducedMotion(enabled)
      runCatching { container.userWalletRepository.updatePreferences(reducedMotionEnabled = enabled) }
    }
  }

  fun setMarketingConsent(enabled: Boolean) {
    viewModelScope.launch {
      container.preferencesDataStore.setMarketingConsent(enabled)
      runCatching { container.userWalletRepository.updatePreferences(marketingConsent = enabled) }
      feedbackMessageFlow.value = if (enabled) "Marketing notices enabled." else "Marketing notices turned off."
    }
  }

  fun searchCatalog(query: String) {
    if (query.trim().isEmpty()) {
      searchResultsFlow.value = emptyList()
      searchErrorFlow.value = null
      return
    }
    viewModelScope.launch {
      isSearchingFlow.value = true
      searchErrorFlow.value = null
      val res = runCatching { container.productRepository.searchCatalog(query) }
        .getOrElse { err ->
          com.example.data.repository.CatalogSearchResult(
            products = emptyList(),
            errorMessage = err.message ?: "Catalog search failed.",
          )
        }
      searchResultsFlow.value = res.products
      searchErrorFlow.value = res.errorMessage
      isSearchingFlow.value = false
    }
  }

  fun filterCatalogCategory(categoryTag: String?, fallbackKeyword: String) {
    if (categoryTag.isNullOrBlank()) {
      searchResultsFlow.value = emptyList()
      searchErrorFlow.value = null
      return
    }
    viewModelScope.launch {
      isSearchingFlow.value = true
      searchErrorFlow.value = null
      val res = runCatching {
        container.productRepository.searchByCategory(categoryTag, fallbackKeyword)
      }.getOrElse { err ->
        com.example.data.repository.CatalogSearchResult(
          products = emptyList(),
          errorMessage = err.message ?: "Category lookup failed.",
        )
      }
      searchResultsFlow.value = res.products
      searchErrorFlow.value = res.errorMessage
      isSearchingFlow.value = false
    }
  }

  fun refreshLiveCatalog() {
    viewModelScope.launch {
      isSearchingFlow.value = true
      runCatching { container.productRepository.preloadLiveCatalog(forceRefresh = true) }
      isSearchingFlow.value = false
    }
  }

  fun submitReceipt(
    storeName: String,
    transactionDate: String,
    currencyCode: String,
    totalAmount: Double,
    receiptReference: String,
    campaignId: String?,
    onSuccess: () -> Unit,
  ) {
    viewModelScope.launch {
      runCatching {
        container.receiptCampaignRepository.submitReceipt(
          storeName = storeName,
          transactionDate = transactionDate,
          currencyCode = currencyCode,
          totalAmount = totalAmount,
          receiptReference = receiptReference,
          campaignId = campaignId,
        )
      }.onSuccess {
        feedbackMessageFlow.value = "Receipt submitted for human review (1-3 min typical check)."
        onSuccess()
      }.onFailure { err ->
        feedbackMessageFlow.value = err.message ?: "Could not submit receipt."
      }
    }
  }

  fun submitPayoutRequest(
    amountPoints: Long,
    rewardTitle: String,
    payoutMethod: PayoutMethod,
    destinationHandle: String,
  ) {
    viewModelScope.launch {
      val name = uiState.value.userProfile?.displayName
        ?: uiState.value.firebaseUser?.displayName
        ?: "Shopper"
      runCatching {
        container.userWalletRepository.submitPayoutRequest(
          currentWallet = uiState.value.wallet,
          amountPoints = amountPoints,
          rewardTitle = rewardTitle,
          payoutMethod = payoutMethod,
          destinationHandle = destinationHandle,
          userDisplayName = name,
        )
      }.onSuccess {
        feedbackMessageFlow.value = "Payout request ($amountPoints PTS) submitted for admin review."
      }.onFailure { err ->
        feedbackMessageFlow.value = err.message ?: "Could not submit payout request."
      }
    }
  }

  fun redeemSandboxVoucher(amountPoints: Long, rewardTitle: String) {
    viewModelScope.launch {
      runCatching {
        container.userWalletRepository.redeemSandboxVoucher(
          currentWallet = uiState.value.wallet,
          amountPoints = amountPoints,
          rewardTitle = rewardTitle,
        )
      }.onSuccess {
        feedbackMessageFlow.value = "Redeemed $amountPoints PTS for $rewardTitle."
      }.onFailure { err ->
        feedbackMessageFlow.value = err.message ?: "Redemption failed."
      }
    }
  }

  fun markNotificationRead(id: String) {
    viewModelScope.launch {
      runCatching { container.userWalletRepository.markNotificationRead(id) }
    }
  }

  fun requestDeletion(reason: String) {
    viewModelScope.launch {
      runCatching { container.userWalletRepository.requestAccountDeletion(reason) }
        .onSuccess { feedbackMessageFlow.value = "Deletion request logged." }
        .onFailure { feedbackMessageFlow.value = it.message ?: "Could not submit deletion request." }
    }
  }

  // --- Admin Governance Actions ---
  fun reviewPayoutRequest(request: PayoutRequest, newStatus: PayoutStatus, adminNotes: String) {
    viewModelScope.launch {
      runCatching { container.adminRepository.reviewPayoutRequest(request, newStatus, adminNotes) }
        .onSuccess { feedbackMessageFlow.value = "Payout marked ${newStatus.name}." }
        .onFailure { feedbackMessageFlow.value = it.message ?: "Payout update failed." }
    }
  }

  fun updateUserAccountStatus(targetUserId: String, newStatus: String, reason: String) {
    viewModelScope.launch {
      runCatching { container.adminRepository.updateUserAccountStatus(targetUserId, newStatus, reason) }
        .onSuccess { feedbackMessageFlow.value = "User account updated to $newStatus." }
        .onFailure { feedbackMessageFlow.value = it.message ?: "Could not update user status." }
    }
  }

  fun adminCreditBonusPoints(targetUserId: String, bonusPoints: Long, reason: String) {
    viewModelScope.launch {
      runCatching { container.adminRepository.adminCreditBonusPoints(targetUserId, bonusPoints, reason) }
        .onSuccess { feedbackMessageFlow.value = "+$bonusPoints PTS credited to user wallet." }
        .onFailure { feedbackMessageFlow.value = it.message ?: "Could not credit bonus points." }
    }
  }

  fun approveReceipt(receipt: ReceiptSubmission, rewardPoints: Long) {
    viewModelScope.launch {
      runCatching { container.adminRepository.approveReceiptAndCredit(receipt, rewardPoints) }
        .onSuccess { feedbackMessageFlow.value = "Receipt approved & +$rewardPoints PTS credited." }
        .onFailure { feedbackMessageFlow.value = it.message ?: "Approval failed." }
    }
  }

  fun rejectReceipt(receipt: ReceiptSubmission, reason: String) {
    viewModelScope.launch {
      runCatching { container.adminRepository.rejectReceipt(receipt, reason) }
        .onSuccess { feedbackMessageFlow.value = "Receipt rejected with audit log." }
        .onFailure { feedbackMessageFlow.value = it.message ?: "Rejection failed." }
    }
  }

  fun createCampaign(
    partnerName: String,
    title: String,
    terms: String,
    rewardPoints: Long,
    countries: List<String>,
  ) {
    viewModelScope.launch {
      runCatching {
        container.adminRepository.createCampaign(partnerName, title, terms, rewardPoints, countries)
      }.onSuccess {
        feedbackMessageFlow.value = "Campaign '$title' published."
      }.onFailure {
        feedbackMessageFlow.value = it.message ?: "Campaign creation failed."
      }
    }
  }

  fun seedStarterCampaigns() {
    viewModelScope.launch {
      runCatching { container.adminRepository.seedStarterCampaigns() }
        .onSuccess { feedbackMessageFlow.value = "Published 3 starter partner campaigns." }
        .onFailure { feedbackMessageFlow.value = it.message ?: "Failed to seed starter campaigns." }
    }
  }

  fun updateCampaignStatus(campaign: Campaign, newStatus: CampaignStatus) {
    viewModelScope.launch {
      runCatching { container.adminRepository.updateCampaignStatus(campaign, newStatus) }
        .onSuccess { feedbackMessageFlow.value = "Campaign updated to ${newStatus.name}." }
        .onFailure { feedbackMessageFlow.value = it.message ?: "Campaign update failed." }
    }
  }

  fun verifyCatalogProduct(product: CatalogProduct) {
    viewModelScope.launch {
      runCatching { container.adminRepository.verifyProductRecord(product) }
        .onSuccess { feedbackMessageFlow.value = "${product.name} marked Verified Merchant." }
        .onFailure { feedbackMessageFlow.value = it.message ?: "Verification failed." }
    }
  }

  fun completeDeletionRequest(request: DeletionRequest) {
    viewModelScope.launch {
      runCatching { container.adminRepository.completeDeletionRequest(request) }
        .onSuccess { feedbackMessageFlow.value = "Deletion request marked COMPLETED." }
        .onFailure { feedbackMessageFlow.value = it.message ?: "Update failed." }
    }
  }

  fun reverseTransaction(tx: RewardTransaction, reason: String) {
    viewModelScope.launch {
      runCatching { container.adminRepository.reverseTransaction(tx, reason) }
        .onSuccess { feedbackMessageFlow.value = "Transaction reversed with audit entry." }
        .onFailure { feedbackMessageFlow.value = it.message ?: "Reversal failed." }
    }
  }

  companion object {
    fun factory(container: AppContainer): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
      @Suppress("UNCHECKED_CAST")
      override fun <T : ViewModel> create(modelClass: Class<T>): T = MainAppViewModel(container) as T
    }
  }
}
