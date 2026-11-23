package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppPreferencesDataStore
import com.example.data.local.UserPreferencesState
import com.example.data.model.AdminRole
import com.example.data.model.AppNotification
import com.example.data.model.AuditLog
import com.example.data.model.Campaign
import com.example.data.model.CampaignStatus
import com.example.data.model.CatalogProduct
import com.example.data.model.ReceiptSubmission
import com.example.data.model.RewardTransaction
import com.example.data.model.RewardWallet
import com.example.data.model.ScanRecord
import com.example.data.model.UserProfile
import com.example.data.model.VerificationStatus
import com.example.data.repository.AdminRepository
import com.example.data.repository.ProductRepository
import com.example.data.repository.ReceiptAndCampaignRepository
import com.example.data.repository.UserWalletRepository
import com.example.domain.MarketCatalog
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

private fun <T> Flow<T>.toUiStateFlow(tag: String): Flow<UiState<T>> =
    map<T, UiState<T>> { UiState.Success(it) }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            Log.w(tag, "Stream error", rootError)
            emit(UiState.Error(rootError.message ?: "Unable to load data"))
        }

class PreferencesViewModel(
    private val prefs: AppPreferencesDataStore
) : ViewModel() {
    val preferences: StateFlow<UserPreferencesState> = prefs.preferencesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UserPreferencesState())

    fun completeOnboarding() {
        viewModelScope.launch { prefs.setOnboardingCompleted(true) }
    }

    fun setGuestMode(enabled: Boolean) {
        viewModelScope.launch { prefs.setGuestMode(enabled) }
    }

    fun setDarkTheme(enabled: Boolean) {
        viewModelScope.launch { prefs.setDarkTheme(enabled) }
    }

    fun setReducedMotion(enabled: Boolean) {
        viewModelScope.launch { prefs.setReducedMotion(enabled) }
    }

    fun setMarketingConsent(enabled: Boolean) {
        viewModelScope.launch { prefs.setMarketingConsent(enabled) }
    }

    fun setCountryAndLanguage(countryCode: String, languageCode: String) {
        viewModelScope.launch { prefs.setCountryAndLanguage(countryCode, languageCode) }
    }
}

class DiscoverViewModel(
    private val productRepository: ProductRepository,
    receiptAndCampaignRepository: ReceiptAndCampaignRepository,
    isAuthenticated: Boolean
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<CatalogProduct>>(emptyList())
    val searchResults: StateFlow<List<CatalogProduct>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    val cachedCatalog: StateFlow<List<CatalogProduct>> = productRepository.observeCachedCatalog()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val activeCampaigns: StateFlow<UiState<List<Campaign>>> = if (isAuthenticated) {
        receiptAndCampaignRepository.observeActiveCampaigns()
            .toUiStateFlow("DiscoverVM")
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)
    } else {
        MutableStateFlow(UiState.Success(emptyList()))
    }

    fun updateQuery(query: String) {
        _searchQuery.value = query
    }

    fun executeSearch() {
        val query = _searchQuery.value.trim()
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }
        viewModelScope.launch {
            _isSearching.value = true
            try {
                _searchResults.value = productRepository.searchCatalog(query)
            } finally {
                _isSearching.value = false
            }
        }
    }
}

class AuthenticatedAccountViewModel(
    private val currentUserId: String,
    displayName: String,
    countryCode: String,
    languageCode: String,
    private val userWalletRepository: UserWalletRepository,
    private val receiptAndCampaignRepository: ReceiptAndCampaignRepository,
    private val adminRepository: AdminRepository
) : ViewModel() {

    val profileState: StateFlow<UiState<UserProfile?>> = userWalletRepository.observeProfile(currentUserId)
        .toUiStateFlow("AccountVM_Profile")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    val walletState: StateFlow<UiState<RewardWallet>> = userWalletRepository.observeWallet(currentUserId)
        .toUiStateFlow("AccountVM_Wallet")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    val transactionsState: StateFlow<UiState<List<RewardTransaction>>> =
        userWalletRepository.observeTransactions(currentUserId)
            .toUiStateFlow("AccountVM_Tx")
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    val cloudScansState: StateFlow<UiState<List<ScanRecord>>> =
        userWalletRepository.observeCloudScans(currentUserId)
            .toUiStateFlow("AccountVM_Scans")
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    val notificationsState: StateFlow<UiState<List<AppNotification>>> =
        userWalletRepository.observeNotifications(currentUserId)
            .toUiStateFlow("AccountVM_Notif")
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    val receiptsState: StateFlow<UiState<List<ReceiptSubmission>>> =
        receiptAndCampaignRepository.observeUserReceipts(currentUserId)
            .toUiStateFlow("AccountVM_Receipts")
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    val activeCampaignsState: StateFlow<UiState<List<Campaign>>> =
        receiptAndCampaignRepository.observeActiveCampaigns()
            .toUiStateFlow("AccountVM_Campaigns")
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    private val _adminRole = MutableStateFlow<AdminRole?>(null)
    val adminRole: StateFlow<AdminRole?> = _adminRole.asStateFlow()

    private val _feedbackMessage = MutableStateFlow<String?>(null)
    val feedbackMessage: StateFlow<String?> = _feedbackMessage.asStateFlow()

    init {
        viewModelScope.launch {
            userWalletRepository.ensureUserInitialized(
                userId = currentUserId,
                displayName = displayName,
                countryCode = countryCode,
                languageCode = languageCode
            )
            _adminRole.value = adminRepository.resolveAdminRole()
        }
    }

    fun submitReceipt(
        storeName: String,
        transactionDate: String,
        currencyCode: String,
        totalAmountText: String,
        receiptReference: String,
        campaignId: String?
    ) {
        val total = totalAmountText.toDoubleOrNull()
        if (total == null || total <= 0.0) {
            _feedbackMessage.value = "Enter a valid receipt total greater than zero."
            return
        }
        viewModelScope.launch {
            val res = receiptAndCampaignRepository.submitReceipt(
                storeName = storeName,
                transactionDate = transactionDate,
                currencyCode = currencyCode,
                totalAmount = total,
                receiptReference = receiptReference,
                campaignId = campaignId
            )
            res.onSuccess {
                _feedbackMessage.value = "Receipt submitted for human verification (Pending review)."
            }.onFailure {
                _feedbackMessage.value = it.localizedMessage ?: "Receipt submission rejected."
            }
        }
    }

    fun redeemVoucher(currentWallet: RewardWallet, amountPoints: Long, rewardTitle: String) {
        viewModelScope.launch {
            val res = userWalletRepository.redeemSandboxVoucher(
                userId = currentUserId,
                currentWallet = currentWallet,
                amountPoints = amountPoints,
                rewardTitle = rewardTitle
            )
            res.onSuccess {
                _feedbackMessage.value = "Redeemed $amountPoints sandbox pts for $rewardTitle."
            }.onFailure {
                _feedbackMessage.value = it.localizedMessage ?: "Redemption failed."
            }
        }
    }

    fun syncPreferencesToProfile(
        countryCode: String? = null,
        languageCode: String? = null,
        marketingConsent: Boolean? = null,
        darkThemeEnabled: Boolean? = null,
        reducedMotionEnabled: Boolean? = null
    ) {
        val currency = countryCode?.let { MarketCatalog.findByCountry(it).currencyCode }
        viewModelScope.launch {
            userWalletRepository.updateProfilePreferences(
                userId = currentUserId,
                countryCode = countryCode,
                languageCode = languageCode,
                currencyCode = currency,
                marketingConsent = marketingConsent,
                darkThemeEnabled = darkThemeEnabled,
                reducedMotionEnabled = reducedMotionEnabled
            )
        }
    }

    fun markNotificationRead(notificationId: String) {
        viewModelScope.launch {
            userWalletRepository.markNotificationRead(currentUserId, notificationId)
        }
    }

    fun requestDeletion(reason: String) {
        viewModelScope.launch {
            val res = userWalletRepository.requestAccountDeletion(currentUserId, reason)
            res.onSuccess {
                _feedbackMessage.value = "Account deletion request logged."
            }.onFailure {
                _feedbackMessage.value = it.localizedMessage ?: "Could not submit deletion request."
            }
        }
    }

    fun clearFeedback() {
        _feedbackMessage.value = null
    }
}

class GuestScansViewModel(
    userWalletRepository: UserWalletRepository
) : ViewModel() {
    val localScans: StateFlow<List<ScanRecord>> = userWalletRepository.observeLocalScans()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())
}

class AdminConsoleViewModel(
    private val adminRepository: AdminRepository
) : ViewModel() {
    val pendingReceipts: StateFlow<UiState<List<ReceiptSubmission>>> =
        adminRepository.observePendingReceipts()
            .toUiStateFlow("AdminVM_Receipts")
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    val allCampaigns: StateFlow<UiState<List<Campaign>>> =
        adminRepository.observeAllCampaigns()
            .toUiStateFlow("AdminVM_Campaigns")
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    val unverifiedCatalog: StateFlow<UiState<List<CatalogProduct>>> =
        adminRepository.observeUnverifiedCatalog()
            .toUiStateFlow("AdminVM_Catalog")
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    val auditLogs: StateFlow<UiState<List<AuditLog>>> =
        adminRepository.observeAuditLogs()
            .toUiStateFlow("AdminVM_Audit")
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    fun approveReceipt(receipt: ReceiptSubmission, points: Long) {
        viewModelScope.launch {
            adminRepository.approveReceiptAndCreditPoints(receipt, points)
                .onSuccess { _statusMessage.value = "Receipt approved & $points pts credited." }
                .onFailure { _statusMessage.value = it.localizedMessage ?: "Approval failed." }
        }
    }

    fun rejectReceipt(receipt: ReceiptSubmission, reason: String) {
        viewModelScope.launch {
            adminRepository.rejectReceipt(receipt, reason)
                .onSuccess { _statusMessage.value = "Receipt rejected & audit logged." }
                .onFailure { _statusMessage.value = it.localizedMessage ?: "Rejection failed." }
        }
    }

    fun createCampaign(
        partnerName: String,
        title: String,
        terms: String,
        rewardPoints: Long,
        countriesCsv: String,
        categoriesCsv: String
    ) {
        val countries = countriesCsv.split(",").map { it.trim().uppercase() }.filter { it.isNotBlank() }
        val categories = categoriesCsv.split(",").map { it.trim() }.filter { it.isNotBlank() }
        viewModelScope.launch {
            adminRepository.createCampaign(
                partnerName = partnerName,
                title = title,
                terms = terms,
                rewardPoints = rewardPoints,
                maxPerUser = 3L,
                eligibleCountries = countries.ifEmpty { listOf("GLOBAL") },
                eligibleCategories = categories,
                status = CampaignStatus.ACTIVE
            ).onSuccess {
                _statusMessage.value = "Campaign '$title' published as ACTIVE."
            }.onFailure {
                _statusMessage.value = it.localizedMessage ?: "Campaign creation failed."
            }
        }
    }

    fun updateCampaignStatus(campaignId: String, status: CampaignStatus) {
        viewModelScope.launch {
            adminRepository.updateCampaignStatus(campaignId, status)
                .onSuccess { _statusMessage.value = "Campaign updated to ${status.name}." }
                .onFailure { _statusMessage.value = it.localizedMessage ?: "Update failed." }
        }
    }

    fun verifyProduct(barcode: String) {
        viewModelScope.launch {
            adminRepository.promoteCatalogProduct(barcode, VerificationStatus.VERIFIED_MERCHANT)
                .onSuccess { _statusMessage.value = "Product $barcode promoted to Verified Merchant." }
                .onFailure { _statusMessage.value = it.localizedMessage ?: "Verification failed." }
        }
    }

    fun reverseTransaction(tx: RewardTransaction, reason: String) {
        viewModelScope.launch {
            adminRepository.reverseTransaction(tx, reason)
                .onSuccess { _statusMessage.value = "Ledger reversal posted with audit reference." }
                .onFailure { _statusMessage.value = it.localizedMessage ?: "Reversal failed." }
        }
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }
}
