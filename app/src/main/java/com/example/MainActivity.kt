package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import com.example.ui.theme.BentoLime
import com.example.ui.theme.Emerald600
import com.example.ui.theme.InkBlack
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.auth.attemptAutoSignIn
import com.example.auth.signOutUser
import com.example.data.model.AppNotification
import com.example.data.model.Campaign
import com.example.data.model.ReceiptSubmission
import com.example.data.model.RewardTransaction
import com.example.data.model.RewardWallet
import com.example.data.model.ScanRecord
import com.example.di.AppContainer
import com.example.domain.MarketCatalog
import com.example.ui.navigation.AppRoute
import com.example.ui.screens.AdminConsoleScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.CountryLanguageScreen
import com.example.ui.screens.DiscoverScreen
import com.example.ui.screens.HelpSupportScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.OfferDetailScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.PrivacySettingsScreen
import com.example.ui.screens.ProductDetailScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ReceiptSubmissionScreen
import com.example.ui.screens.RewardsScreen
import com.example.ui.screens.ScanHistoryScreen
import com.example.ui.screens.ScanScreen
import com.example.ui.screens.TermsRulesScreen
import com.example.ui.screens.TransactionDetailScreen
import com.example.ui.theme.Scan2MoneyTheme
import com.example.ui.viewmodel.AdminConsoleViewModel
import com.example.ui.viewmodel.AuthenticatedAccountViewModel
import com.example.ui.viewmodel.DiscoverViewModel
import com.example.ui.viewmodel.GuestScansViewModel
import com.example.ui.viewmodel.PreferencesViewModel
import com.example.ui.viewmodel.ProductDetailViewModel
import com.example.ui.viewmodel.ScannerViewModel
import com.example.ui.viewmodel.UiState
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

private data class BottomNavTab(
    val route: AppRoute,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val tag: String
)

class MainActivity : ComponentActivity() {
    private val appContainer by lazy { AppContainer(applicationContext) }

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)
            val prefsViewModel: PreferencesViewModel = viewModel(
                factory = viewModelFactory {
                    initializer { PreferencesViewModel(appContainer.preferencesDataStore) }
                }
            )
            val prefs by prefsViewModel.preferences.collectAsStateWithLifecycle()

            Scan2MoneyTheme(darkTheme = prefs.darkThemeEnabled) {
                Scan2MoneyRoot(
                    appContainer = appContainer,
                    prefsViewModel = prefsViewModel,
                    useNavRail = windowSizeClass.widthSizeClass != WindowWidthSizeClass.Compact
                )
            }
        }
    }
}

@Composable
private fun Scan2MoneyRoot(
    appContainer: AppContainer,
    prefsViewModel: PreferencesViewModel,
    useNavRail: Boolean
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }
    val prefs by prefsViewModel.preferences.collectAsStateWithLifecycle()

    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }
    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose { Firebase.auth.removeAuthStateListener(listener) }
    }

    LaunchedEffect(Unit) {
        if (Firebase.auth.currentUser == null) {
            attemptAutoSignIn(
                context = context,
                credentialManager = credentialManager,
                onAuthSuccess = {
                    prefsViewModel.setGuestMode(false)
                    currentUser = Firebase.auth.currentUser
                },
                onUnauthenticated = {},
                scope = scope
            )
        }
    }

    when {
        !prefs.onboardingCompleted -> {
            OnboardingScreen(
                onContinue = {
                    prefsViewModel.completeOnboarding()
                    if (Firebase.auth.currentUser == null) {
                        prefsViewModel.setGuestMode(true)
                    }
                }
            )
        }
        currentUser == null && !prefs.guestMode -> {
            AuthScreen(
                onAuthSuccess = {
                    prefsViewModel.setGuestMode(false)
                    currentUser = Firebase.auth.currentUser
                },
                onContinueAsGuest = {
                    prefsViewModel.setGuestMode(true)
                }
            )
        }
        else -> {
            val sessionKey = currentUser?.uid ?: "guest_session"
            key(sessionKey) {
                MainAppShell(
                    appContainer = appContainer,
                    prefsViewModel = prefsViewModel,
                    currentUserId = currentUser?.uid,
                    userDisplayName = currentUser?.displayName ?: "Shopper",
                    userEmail = currentUser?.email,
                    useNavRail = useNavRail,
                    onRequestSignIn = { prefsViewModel.setGuestMode(false) },
                    onSignOut = {
                        signOutUser(credentialManager, scope) {
                            prefsViewModel.setGuestMode(false)
                            currentUser = null
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun MainAppShell(
    appContainer: AppContainer,
    prefsViewModel: PreferencesViewModel,
    currentUserId: String?,
    userDisplayName: String,
    userEmail: String?,
    useNavRail: Boolean,
    onRequestSignIn: () -> Unit,
    onSignOut: () -> Unit
) {
    val prefs by prefsViewModel.preferences.collectAsStateWithLifecycle()
    val isGuest = currentUserId == null

    val accountViewModel: AuthenticatedAccountViewModel? = if (currentUserId != null) {
        viewModel(
            key = currentUserId,
            factory = viewModelFactory {
                initializer {
                    AuthenticatedAccountViewModel(
                        currentUserId = currentUserId,
                        displayName = userDisplayName,
                        countryCode = prefs.countryCode,
                        languageCode = prefs.languageCode,
                        userWalletRepository = appContainer.userWalletRepository,
                        receiptAndCampaignRepository = appContainer.receiptAndCampaignRepository,
                        adminRepository = appContainer.adminRepository
                    )
                }
            }
        )
    } else null

    val guestScansViewModel: GuestScansViewModel = viewModel(
        key = currentUserId ?: "guest",
        factory = viewModelFactory {
            initializer { GuestScansViewModel(appContainer.userWalletRepository) }
        }
    )

    val scannerViewModel: ScannerViewModel = viewModel(
        key = currentUserId ?: "guest",
        factory = viewModelFactory {
            initializer {
                ScannerViewModel(appContainer.productRepository, appContainer.userWalletRepository)
            }
        }
    )

    val discoverViewModel: DiscoverViewModel = viewModel(
        key = currentUserId ?: "guest",
        factory = viewModelFactory {
            initializer {
                DiscoverViewModel(
                    appContainer.productRepository,
                    appContainer.receiptAndCampaignRepository,
                    isAuthenticated = currentUserId != null
                )
            }
        }
    )

    val walletState = accountViewModel?.walletState?.collectAsStateWithLifecycle()?.value
    val transactionsState = accountViewModel?.transactionsState?.collectAsStateWithLifecycle()?.value
    val cloudScansState = accountViewModel?.cloudScansState?.collectAsStateWithLifecycle()?.value
    val notificationsState = accountViewModel?.notificationsState?.collectAsStateWithLifecycle()?.value
    val receiptsState = accountViewModel?.receiptsState?.collectAsStateWithLifecycle()?.value
    val campaignsState = accountViewModel?.activeCampaignsState?.collectAsStateWithLifecycle()?.value
    val adminRole = accountViewModel?.adminRole?.collectAsStateWithLifecycle()?.value
    val accountFeedback = accountViewModel?.feedbackMessage?.collectAsStateWithLifecycle()?.value
    val localScans by guestScansViewModel.localScans.collectAsStateWithLifecycle()

    val wallet: RewardWallet? = (walletState as? UiState.Success<RewardWallet>)?.data
    val transactions: List<RewardTransaction> = (transactionsState as? UiState.Success<List<RewardTransaction>>)?.data.orEmpty()
    val scans: List<ScanRecord> = if (currentUserId != null) {
        (cloudScansState as? UiState.Success<List<ScanRecord>>)?.data.orEmpty()
    } else {
        localScans
    }
    val notifications: List<AppNotification> = (notificationsState as? UiState.Success<List<AppNotification>>)?.data.orEmpty()
    val unreadNotifCount = notifications.count { !it.read }
    val receipts: List<ReceiptSubmission> = (receiptsState as? UiState.Success<List<ReceiptSubmission>>)?.data.orEmpty()
    val activeCampaigns: List<Campaign> = (campaignsState as? UiState.Success<List<Campaign>>)?.data.orEmpty()
    val market = MarketCatalog.findByCountry(prefs.countryCode)

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val tabs = remember {
        listOf(
            BottomNavTab(AppRoute.Home, "Home", Icons.Filled.Home, Icons.Outlined.Home, "nav_home"),
            BottomNavTab(AppRoute.Scan, "Scan", Icons.Filled.QrCodeScanner, Icons.Outlined.QrCodeScanner, "nav_scan"),
            BottomNavTab(AppRoute.Rewards, "Rewards", Icons.Filled.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet, "nav_rewards"),
            BottomNavTab(AppRoute.Discover, "Discover", Icons.Filled.Explore, Icons.Outlined.Explore, "nav_discover"),
            BottomNavTab(AppRoute.Profile, "Profile", Icons.Filled.Person, Icons.Outlined.Person, "nav_profile")
        )
    }

    val isTopLevelRoute = tabs.any { tab ->
        currentDestination?.hasRoute(tab.route::class) == true
    }

    fun navigateTopLevel(route: AppRoute) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (!useNavRail && isTopLevelRoute) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        color = InkBlack,
                        shape = RoundedCornerShape(32.dp),
                        shadowElevation = 10.dp,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.14f)),
                        modifier = Modifier
                            .widthIn(max = 460.dp)
                            .fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            tabs.forEach { tab ->
                                val selected = currentDestination?.hasRoute(tab.route::class) == true
                                val isScanTab = tab.route == AppRoute.Scan
                                val pillBg = when {
                                    selected -> BentoLime
                                    isScanTab -> Emerald600.copy(alpha = 0.25f)
                                    else -> Color.Transparent
                                }
                                val iconTint = when {
                                    selected -> InkBlack
                                    isScanTab -> BentoLime
                                    else -> Color(0xFFA7B5AF)
                                }

                                Surface(
                                    color = pillBg,
                                    shape = RoundedCornerShape(24.dp),
                                    modifier = Modifier
                                        .height(48.dp)
                                        .clickable { navigateTopLevel(tab.route) }
                                        .testTag(tab.tag)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = if (selected) 14.dp else 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                            contentDescription = tab.label,
                                            tint = iconTint,
                                            modifier = Modifier.size(21.dp)
                                        )
                                        if (selected) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = tab.label,
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = InkBlack,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (useNavRail && isTopLevelRoute) {
                NavigationRail {
                    tabs.forEach { tab ->
                        val selected = currentDestination?.hasRoute(tab.route::class) == true
                        NavigationRailItem(
                            selected = selected,
                            onClick = { navigateTopLevel(tab.route) },
                            icon = {
                                Icon(
                                    imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = tab.label
                                )
                            },
                            label = { Text(tab.label) },
                            modifier = Modifier.testTag(tab.tag)
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .then(if (isTopLevelRoute) Modifier.statusBarsPadding() else Modifier)
            ) {
                NavHost(
                    navController = navController,
                    startDestination = AppRoute.Home
                ) {
                    composable<AppRoute.Home> {
                        HomeScreen(
                            displayName = userDisplayName,
                            isGuest = isGuest,
                            countryCode = prefs.countryCode,
                            wallet = wallet,
                            campaigns = activeCampaigns,
                            recentScans = scans,
                            unreadNotificationCount = unreadNotifCount,
                            onNavigateScan = { navigateTopLevel(AppRoute.Scan) },
                            onNavigateDiscover = { navigateTopLevel(AppRoute.Discover) },
                            onNavigateRewards = { navigateTopLevel(AppRoute.Rewards) },
                            onNavigateSubmitReceipt = { navController.navigate(AppRoute.ReceiptSubmissionRoute()) },
                            onNavigateNotifications = { navController.navigate(AppRoute.Notifications) },
                            onNavigateScanHistory = { navController.navigate(AppRoute.ScanHistory) },
                            onOpenProduct = { barcode -> navController.navigate(AppRoute.ProductDetail(barcode)) },
                            onRequestSignIn = onRequestSignIn
                        )
                    }

                    composable<AppRoute.Scan> {
                        ScanScreen(
                            viewModel = scannerViewModel,
                            reducedMotion = prefs.reducedMotionEnabled,
                            onOpenProductDetail = { barcode ->
                                navController.navigate(AppRoute.ProductDetail(barcode))
                            },
                            onNavigateSearchFallback = { navigateTopLevel(AppRoute.Discover) }
                        )
                    }

                    composable<AppRoute.Rewards> {
                        RewardsScreen(
                            isGuest = isGuest,
                            wallet = wallet,
                            transactions = transactions,
                            receipts = receipts,
                            feedbackMessage = accountFeedback,
                            onClearFeedback = { accountViewModel?.clearFeedback() },
                            onRedeemVoucher = { w, pts, title ->
                                accountViewModel?.redeemVoucher(w, pts, title)
                            },
                            onNavigateSubmitReceipt = {
                                navController.navigate(AppRoute.ReceiptSubmissionRoute())
                            },
                            onOpenTransaction = { txId ->
                                navController.navigate(AppRoute.TransactionDetail(txId))
                            },
                            onNavigateTerms = { navController.navigate(AppRoute.TermsRules) },
                            onRequestSignIn = onRequestSignIn
                        )
                    }

                    composable<AppRoute.Discover> {
                        DiscoverScreen(
                            viewModel = discoverViewModel,
                            countryCode = prefs.countryCode,
                            onOpenProduct = { barcode ->
                                navController.navigate(AppRoute.ProductDetail(barcode))
                            }
                        )
                    }

                    composable<AppRoute.Profile> {
                        ProfileScreen(
                            isGuest = isGuest,
                            displayName = userDisplayName,
                            email = userEmail,
                            countryCode = prefs.countryCode,
                            languageCode = prefs.languageCode,
                            darkThemeEnabled = prefs.darkThemeEnabled,
                            reducedMotionEnabled = prefs.reducedMotionEnabled,
                            marketingConsent = prefs.marketingConsent,
                            unreadNotificationCount = unreadNotifCount,
                            adminRole = adminRole,
                            onToggleDarkTheme = { enabled ->
                                prefsViewModel.setDarkTheme(enabled)
                                accountViewModel?.syncPreferencesToProfile(darkThemeEnabled = enabled)
                            },
                            onToggleReducedMotion = { enabled ->
                                prefsViewModel.setReducedMotion(enabled)
                                accountViewModel?.syncPreferencesToProfile(reducedMotionEnabled = enabled)
                            },
                            onOpenScanHistory = { navController.navigate(AppRoute.ScanHistory) },
                            onOpenNotifications = { navController.navigate(AppRoute.Notifications) },
                            onOpenCountryLanguage = { navController.navigate(AppRoute.CountryLanguageSettings) },
                            onOpenPrivacy = { navController.navigate(AppRoute.PrivacySettings) },
                            onOpenTerms = { navController.navigate(AppRoute.TermsRules) },
                            onOpenHelp = { navController.navigate(AppRoute.HelpSupport) },
                            onOpenAdminConsole = { navController.navigate(AppRoute.AdminConsole) },
                            onRequestDeletion = { reason ->
                                accountViewModel?.requestDeletion(reason)
                            },
                            onSignInClick = onRequestSignIn,
                            onSignOutClick = onSignOut
                        )
                    }

                    composable<AppRoute.ProductDetail> { entry ->
                        val args = entry.toRoute<AppRoute.ProductDetail>()
                        val pdVm: ProductDetailViewModel = viewModel(
                            key = "${currentUserId ?: "guest"}_${args.barcode}",
                            factory = viewModelFactory {
                                initializer {
                                    ProductDetailViewModel(
                                        barcode = args.barcode,
                                        countryCode = prefs.countryCode,
                                        currentUserId = currentUserId,
                                        productRepository = appContainer.productRepository,
                                        receiptAndCampaignRepository = appContainer.receiptAndCampaignRepository,
                                        userWalletRepository = appContainer.userWalletRepository
                                    )
                                }
                            }
                        )
                        ProductDetailScreen(
                            viewModel = pdVm,
                            isGuest = isGuest,
                            countryCode = prefs.countryCode,
                            currencyCode = market.currencyCode,
                            onOpenOffer = { offer ->
                                navController.navigate(
                                    AppRoute.OfferDetail(
                                        barcode = offer.barcode,
                                        storeName = offer.storeName,
                                        price = offer.price,
                                        currencyCode = offer.currencyCode,
                                        countryCode = offer.countryCode,
                                        availabilityStatus = offer.availabilityStatus,
                                        verified = offer.verified,
                                        offerUrl = offer.offerUrl,
                                        lastVerifiedText = offer.lastVerifiedAt?.toDate()?.toString() ?: "Unverified"
                                    )
                                )
                            },
                            onSubmitReceiptForCampaign = { campId ->
                                if (isGuest) onRequestSignIn()
                                else navController.navigate(AppRoute.ReceiptSubmissionRoute(campId))
                            },
                            onRequestSignIn = onRequestSignIn,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable<AppRoute.OfferDetail> { entry ->
                        val args = entry.toRoute<AppRoute.OfferDetail>()
                        OfferDetailScreen(
                            barcode = args.barcode,
                            storeName = args.storeName,
                            price = args.price,
                            currencyCode = args.currencyCode,
                            countryCode = args.countryCode,
                            availabilityStatus = args.availabilityStatus,
                            verified = args.verified,
                            offerUrl = args.offerUrl,
                            lastVerifiedText = args.lastVerifiedText,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable<AppRoute.ScanHistory> {
                        ScanHistoryScreen(
                            scans = scans,
                            onOpenProduct = { barcode -> navController.navigate(AppRoute.ProductDetail(barcode)) },
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable<AppRoute.ReceiptSubmissionRoute> { entry ->
                        val args = entry.toRoute<AppRoute.ReceiptSubmissionRoute>()
                        ReceiptSubmissionScreen(
                            defaultCurrency = market.currencyCode,
                            activeCampaigns = activeCampaigns,
                            preselectedCampaignId = args.preselectedCampaignId,
                            feedbackMessage = accountFeedback,
                            onClearFeedback = { accountViewModel?.clearFeedback() },
                            onSubmitReceipt = { store, date, curr, total, ref, campId ->
                                accountViewModel?.submitReceipt(store, date, curr, total, ref, campId)
                            },
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable<AppRoute.TransactionDetail> { entry ->
                        val args = entry.toRoute<AppRoute.TransactionDetail>()
                        val tx = transactions.firstOrNull { it.id == args.transactionId }
                        val adminVm: AdminConsoleViewModel? = if (adminRole != null) {
                            viewModel(
                                key = "admin_${currentUserId}",
                                factory = viewModelFactory {
                                    initializer { AdminConsoleViewModel(appContainer.adminRepository) }
                                }
                            )
                        } else null

                        TransactionDetailScreen(
                            transaction = tx,
                            isAdmin = adminRole != null,
                            onReverseTransaction = { targetTx, reason ->
                                adminVm?.reverseTransaction(targetTx, reason)
                            },
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable<AppRoute.Notifications> {
                        NotificationsScreen(
                            isGuest = isGuest,
                            notifications = notifications,
                            onMarkRead = { id -> accountViewModel?.markNotificationRead(id) },
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable<AppRoute.CountryLanguageSettings> {
                        CountryLanguageScreen(
                            selectedCountryCode = prefs.countryCode,
                            onSelectCountry = { country, lang ->
                                prefsViewModel.setCountryAndLanguage(country, lang)
                                accountViewModel?.syncPreferencesToProfile(countryCode = country, languageCode = lang)
                            },
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable<AppRoute.PrivacySettings> {
                        PrivacySettingsScreen(
                            marketingConsent = prefs.marketingConsent,
                            onToggleMarketingConsent = { enabled ->
                                prefsViewModel.setMarketingConsent(enabled)
                                accountViewModel?.syncPreferencesToProfile(marketingConsent = enabled)
                            },
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable<AppRoute.TermsRules> {
                        TermsRulesScreen(onBack = { navController.popBackStack() })
                    }

                    composable<AppRoute.HelpSupport> {
                        HelpSupportScreen(onBack = { navController.popBackStack() })
                    }

                    composable<AppRoute.AdminConsole> {
                        val adminVm: AdminConsoleViewModel = viewModel(
                            key = "admin_${currentUserId ?: "none"}",
                            factory = viewModelFactory {
                                initializer { AdminConsoleViewModel(appContainer.adminRepository) }
                            }
                        )
                        AdminConsoleScreen(
                            adminRole = adminRole,
                            viewModel = adminVm,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
