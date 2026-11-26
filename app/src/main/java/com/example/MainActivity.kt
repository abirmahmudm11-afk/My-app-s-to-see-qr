@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.example

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.di.AppContainer
import com.example.domain.BarcodeValidator
import com.example.domain.SupportedBarcodeFormat
import com.example.ui.navigation.Routes
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
import com.example.ui.theme.LocalAnimatedVisibilityScope
import com.example.ui.theme.LocalSharedTransitionScope
import com.example.ui.theme.Scan2MoneyTheme
import com.example.ui.viewmodel.MainAppViewModel
import com.example.ui.viewmodel.ProductDetailViewModel
import com.example.ui.viewmodel.ScannerViewModel
import com.google.android.gms.ads.MobileAds

class MainActivity : ComponentActivity() {
  private val appContainer: AppContainer by lazy(LazyThreadSafetyMode.NONE) {
    AppContainer(applicationContext)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    if (!Build.FINGERPRINT.contains("robolectric", ignoreCase = true)) {
      runCatching { MobileAds.initialize(this) {} }
    }
    setContent {
      Scan2MoneyRoot(appContainer)
    }
  }
}

private data class NavDestinationTab(
  val route: String,
  val label: String,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector,
)

private enum class RootStage {
  LOADING,
  ONBOARDING,
  AUTH,
  MAIN,
}

@Composable
private fun Scan2MoneyRoot(container: AppContainer) {
  val activityContext = LocalContext.current
  val mainViewModel: MainAppViewModel = viewModel(factory = MainAppViewModel.factory(container))
  val scannerViewModel: ScannerViewModel = viewModel(factory = ScannerViewModel.factory(container))
  val productDetailViewModel: ProductDetailViewModel = viewModel(factory = ProductDetailViewModel.factory(container))

  val uiState by mainViewModel.uiState.collectAsStateWithLifecycle()
  val snackbarHostState = remember { SnackbarHostState() }
  var forceAuthModal by remember { mutableStateOf(false) }

  LaunchedEffect(uiState.preferencesLoaded) {
    if (uiState.preferencesLoaded) {
      mainViewModel.attemptAutoSignIn(activityContext)
    }
  }

  LaunchedEffect(uiState.feedbackMessage) {
    val msg = uiState.feedbackMessage
    if (!msg.isNullOrBlank()) {
      snackbarHostState.showSnackbar(msg)
      mainViewModel.clearFeedback()
    }
  }

  LaunchedEffect(uiState.firebaseUser) {
    if (uiState.firebaseUser != null) {
      forceAuthModal = false
    }
  }

  val reducedMotion = uiState.preferences.reducedMotionEnabled
  val rootStage = when {
    !uiState.preferencesLoaded -> RootStage.LOADING
    !uiState.preferences.onboardingCompleted -> RootStage.ONBOARDING
    forceAuthModal || (uiState.firebaseUser == null && !uiState.preferences.guestMode) -> RootStage.AUTH
    else -> RootStage.MAIN
  }

  Scan2MoneyTheme(
    darkTheme = uiState.preferences.darkThemeEnabled,
    reducedMotion = reducedMotion,
  ) {
    AnimatedContent(
      targetState = rootStage,
      transitionSpec = {
        val dur = if (reducedMotion) 0 else 200
        fadeIn(tween(dur, easing = FastOutSlowInEasing))
          .togetherWith(fadeOut(tween(dur / 2)))
      },
      label = "rootStageTransition",
    ) { stage ->
      when (stage) {
        RootStage.LOADING -> {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(com.example.ui.theme.CarbonCanvas),
            contentAlignment = Alignment.Center,
          ) {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
              androidx.compose.material3.Surface(
                shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
                color = com.example.ui.theme.CarbonSurface,
                border = androidx.compose.foundation.BorderStroke(1.2.dp, com.example.ui.theme.MintGlowBorderBrush),
              ) {
                Icon(
                  imageVector = Icons.Rounded.QrCodeScanner,
                  contentDescription = "SCAN2MONEY",
                  tint = com.example.ui.theme.SignalIndigoDark,
                  modifier = Modifier.padding(16.dp).size(40.dp),
                )
              }
              Text(
                text = "SCAN2MONEY",
                style = MaterialTheme.typography.headlineMedium,
                color = com.example.ui.theme.CarbonInkPrimary,
              )
              CircularProgressIndicator(
                color = com.example.ui.theme.SignalIndigoDark,
                strokeWidth = 2.5.dp,
                modifier = Modifier.size(24.dp),
              )
            }
          }
        }

        RootStage.ONBOARDING -> {
          Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            contentWindowInsets = WindowInsets.safeDrawing,
          ) { padding ->
            Box(
              modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            ) {
              OnboardingScreen(
                selectedCountryCode = uiState.preferences.countryCode,
                reducedMotion = reducedMotion,
                onComplete = { country -> mainViewModel.completeOnboarding(country) },
                onContinueGuest = { mainViewModel.enterGuestMode() },
              )
            }
          }
        }

        RootStage.AUTH -> {
          Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            contentWindowInsets = WindowInsets.safeDrawing,
          ) { padding ->
            Box(
              modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            ) {
              AuthScreen(
                isSigningIn = uiState.isSigningIn,
                onGoogleSignIn = { mainViewModel.signInWithGoogle(activityContext) },
                onContinueGuest = {
                  forceAuthModal = false
                  mainViewModel.enterGuestMode()
                },
              )
            }
          }
        }

        RootStage.MAIN -> {
          key(uiState.firebaseUser?.uid ?: "guest") {
            val navController = rememberNavController()
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            val tabs = remember {
              listOf(
                NavDestinationTab(Routes.HOME, "Home", Icons.Rounded.Home, Icons.Outlined.Home),
                NavDestinationTab(Routes.SCAN, "Scan", Icons.Rounded.QrCodeScanner, Icons.Outlined.QrCodeScanner),
                NavDestinationTab(Routes.SCAN_HISTORY, "History", Icons.Rounded.History, Icons.Outlined.History),
                NavDestinationTab(Routes.REWARDS, "Rewards", Icons.Rounded.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet),
                NavDestinationTab(Routes.PROFILE, "Profile", Icons.Rounded.Person, Icons.Outlined.Person),
              )
            }
            val topLevelRoutes = remember { tabs.map { it.route }.toSet() }
            val showTopNavigation = currentRoute in topLevelRoutes ||
              currentRoute == Routes.DISCOVER ||
              currentRoute == Routes.ADMIN_CONSOLE

            val navigateTopLevel: (String) -> Unit = { route ->
              navController.navigate(route) {
                popUpTo(navController.graph.findStartDestination().id) {
                  saveState = true
                }
                launchSingleTop = true
                restoreState = true
              }
            }

            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
              val useNavRail = maxWidth >= 600.dp

              Row(modifier = Modifier.fillMaxSize()) {
                if (useNavRail && showTopNavigation) {
                  AdaptiveNavigationRail(
                    tabs = tabs,
                    currentRoute = currentRoute,
                    onSelectTab = navigateTopLevel,
                  )
                  VerticalDivider(
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.fillMaxHeight(),
                  )
                }

                Scaffold(
                  modifier = Modifier.weight(1f),
                  snackbarHost = { SnackbarHost(snackbarHostState) },
                  contentWindowInsets = WindowInsets.safeDrawing,
                  bottomBar = {
                    if (!useNavRail && showTopNavigation) {
                      AdaptiveBottomNavigationBar(
                        tabs = tabs,
                        currentRoute = currentRoute,
                        onSelectTab = navigateTopLevel,
                      )
                    }
                  },
                ) { innerPadding ->
                  val motionDur = if (reducedMotion) 0 else 220
                  val tabDur = if (reducedMotion) 0 else 150
                  SharedTransitionLayout {
                    CompositionLocalProvider(LocalSharedTransitionScope provides this@SharedTransitionLayout) {
                      NavHost(
                        navController = navController,
                        startDestination = Routes.HOME,
                        modifier = Modifier
                          .fillMaxSize()
                          .padding(innerPadding),
                        enterTransition = {
                          val bothTabs = initialState.destination.route in topLevelRoutes &&
                            targetState.destination.route in topLevelRoutes
                          if (bothTabs) {
                            fadeIn(tween(tabDur, easing = FastOutSlowInEasing))
                          } else {
                            slideInHorizontally(
                              initialOffsetX = { fullWidth -> fullWidth / 5 },
                              animationSpec = tween(motionDur, easing = FastOutSlowInEasing),
                            ) + fadeIn(tween(motionDur))
                          }
                        },
                        exitTransition = {
                          val bothTabs = initialState.destination.route in topLevelRoutes &&
                            targetState.destination.route in topLevelRoutes
                          if (bothTabs) {
                            fadeOut(tween(tabDur / 2))
                          } else {
                            slideOutHorizontally(
                              targetOffsetX = { fullWidth -> -fullWidth / 8 },
                              animationSpec = tween(motionDur, easing = FastOutSlowInEasing),
                            ) + fadeOut(tween(motionDur / 2))
                          }
                        },
                        popEnterTransition = {
                          slideInHorizontally(
                            initialOffsetX = { fullWidth -> -fullWidth / 6 },
                            animationSpec = tween(motionDur, easing = FastOutSlowInEasing),
                          ) + fadeIn(tween(motionDur))
                        },
                        popExitTransition = {
                          slideOutHorizontally(
                            targetOffsetX = { fullWidth -> fullWidth / 4 },
                            animationSpec = tween(motionDur, easing = FastOutSlowInEasing),
                          ) + fadeOut(tween(motionDur))
                        },
                      ) {
                        composable(Routes.HOME) {
                          CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) {
                            HomeScreen(
                              uiState = uiState,
                              onOpenScan = { navigateTopLevel(Routes.SCAN) },
                              onRescan = { scanContext ->
                                mainViewModel.setScanContext(scanContext)
                                navigateTopLevel(Routes.SCAN)
                              },
                              onOpenDiscover = { navigateTopLevel(Routes.DISCOVER) },
                              onOpenRewards = { navigateTopLevel(Routes.REWARDS) },
                              onOpenReceiptSubmit = { campId -> navController.navigate(Routes.receiptSubmit(campId)) },
                              onOpenProduct = { barcode -> navController.navigate(Routes.productDetail(barcode)) },
                              onOpenNotifications = { navController.navigate(Routes.NOTIFICATIONS) },
                              onOpenScanHistory = { navigateTopLevel(Routes.SCAN_HISTORY) },
                              onOpenAdmin = { navController.navigate(Routes.ADMIN_CONSOLE) },
                              onSignInRequested = { forceAuthModal = true },
                            )
                          }
                        }

                        composable(Routes.DISCOVER) {
                          CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) {
                            DiscoverScreen(
                              uiState = uiState,
                              onSearch = { q -> mainViewModel.searchCatalog(q) },
                              onSelectCategory = { tag, fallback ->
                                mainViewModel.filterCatalogCategory(tag, fallback)
                              },
                              onRefreshCatalog = { mainViewModel.refreshLiveCatalog() },
                              onOpenProduct = { barcode -> navController.navigate(Routes.productDetail(barcode)) },
                              onOpenScan = { navigateTopLevel(Routes.SCAN) },
                            )
                          }
                        }

                        composable(Routes.SCAN) {
                          ScanScreen(
                            scannerViewModel = scannerViewModel,
                            scanContext = uiState.activeScanContext,
                            reducedMotion = uiState.preferences.reducedMotionEnabled,
                            onClearScanContext = { mainViewModel.clearScanContext() },
                            onOpenProductDetail = { barcode -> navController.navigate(Routes.productDetail(barcode)) },
                            onOpenHistory = { navigateTopLevel(Routes.SCAN_HISTORY) },
                            onOpenRewardsOrPromo = { campHint ->
                              if (!campHint.isNullOrBlank()) {
                                navController.navigate(Routes.receiptSubmit(campHint))
                              } else {
                                navigateTopLevel(Routes.REWARDS)
                              }
                            },
                            onShowMessage = { msg -> mainViewModel.showFeedback(msg) },
                          )
                        }

                        composable(Routes.REWARDS) {
                          RewardsScreen(
                            uiState = uiState,
                            onSubmitReceipt = { campId -> navController.navigate(Routes.receiptSubmit(campId)) },
                            onRedeemVoucher = { cost, title -> mainViewModel.redeemSandboxVoucher(cost, title) },
                            onRequestPayout = { pts, title, method, handle ->
                              mainViewModel.submitPayoutRequest(pts, title, method, handle)
                            },
                            onOpenTransaction = { txId -> navController.navigate(Routes.transactionDetail(txId)) },
                            onSignInRequested = { forceAuthModal = true },
                          )
                        }

                        composable(Routes.PROFILE) {
                          ProfileScreen(
                            uiState = uiState,
                            onOpenScanHistory = { navigateTopLevel(Routes.SCAN_HISTORY) },
                            onOpenNotifications = { navController.navigate(Routes.NOTIFICATIONS) },
                            onOpenCountryLanguage = { navController.navigate(Routes.COUNTRY_LANGUAGE) },
                            onOpenPrivacy = { navController.navigate(Routes.PRIVACY_SETTINGS) },
                            onOpenTerms = { navController.navigate(Routes.TERMS_RULES) },
                            onOpenHelp = { navController.navigate(Routes.HELP_SUPPORT) },
                            onOpenAdmin = { navController.navigate(Routes.ADMIN_CONSOLE) },
                            onToggleDarkTheme = { mainViewModel.setDarkTheme(it) },
                            onToggleReducedMotion = { mainViewModel.setReducedMotion(it) },
                            onSignIn = { forceAuthModal = true },
                            onSignOut = { mainViewModel.signOut(activityContext) },
                          )
                        }

                        composable(
                          route = Routes.PRODUCT_DETAIL,
                          arguments = listOf(navArgument("barcode") { type = NavType.StringType }),
                        ) { entry ->
                          val barcode = Uri.decode(entry.arguments?.getString("barcode").orEmpty())
                          CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) {
                            ProductDetailScreen(
                              barcode = barcode,
                              mainUiState = uiState,
                              productDetailViewModel = productDetailViewModel,
                              onBack = { navController.popBackStack() },
                              onOpenOfferDetail = { code, offerId ->
                                navController.navigate(Routes.offerDetail(code, offerId))
                              },
                              onOpenReceiptSubmit = { campId -> navController.navigate(Routes.receiptSubmit(campId)) },
                              onSignInRequested = { forceAuthModal = true },
                              onShowMessage = { msg -> mainViewModel.showFeedback(msg) },
                            )
                          }
                        }

                        composable(
                          route = Routes.OFFER_DETAIL,
                          arguments = listOf(
                            navArgument("barcode") { type = NavType.StringType },
                            navArgument("offerId") { type = NavType.StringType },
                          ),
                        ) { entry ->
                          val barcode = Uri.decode(entry.arguments?.getString("barcode").orEmpty())
                          val offerId = Uri.decode(entry.arguments?.getString("offerId").orEmpty())
                          OfferDetailScreen(
                            barcode = barcode,
                            offerId = offerId,
                            productDetailViewModel = productDetailViewModel,
                            countryCode = uiState.preferences.countryCode,
                            onBack = { navController.popBackStack() },
                          )
                        }

                        composable(
                          route = Routes.RECEIPT_SUBMIT,
                          arguments = listOf(
                            navArgument("campaignId") {
                              type = NavType.StringType
                              nullable = true
                              defaultValue = null
                            },
                          ),
                        ) { entry ->
                          val campId = entry.arguments?.getString("campaignId")?.let { Uri.decode(it) }
                          ReceiptSubmissionScreen(
                            uiState = uiState,
                            preselectedCampaignId = campId,
                            onBack = { navController.popBackStack() },
                            onSignInRequested = { forceAuthModal = true },
                            onSubmitReceipt = { store, date, currency, total, ref, cId, onDone ->
                              mainViewModel.submitReceipt(store, date, currency, total, ref, cId, onDone)
                            },
                          )
                        }

                        composable(
                          route = Routes.TRANSACTION_DETAIL,
                          arguments = listOf(navArgument("txId") { type = NavType.StringType }),
                        ) { entry ->
                          val txId = Uri.decode(entry.arguments?.getString("txId").orEmpty())
                          TransactionDetailScreen(
                            txId = txId,
                            uiState = uiState,
                            onBack = { navController.popBackStack() },
                          )
                        }

                        composable(Routes.SCAN_HISTORY) {
                          CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) {
                            ScanHistoryScreen(
                              uiState = uiState,
                              isTopLevelTab = true,
                              onBack = { navController.popBackStack() },
                              onOpenDiscover = { navController.navigate(Routes.DISCOVER) },
                              onOpenScanItem = { scan ->
                                when (val qr = BarcodeValidator.classifyQrPayload(scan.barcodeValue, SupportedBarcodeFormat.QR_CODE)) {
                                  is BarcodeValidator.QrPayloadResolution.WebUrl -> {
                                    if (!qr.embeddedBarcode.isNullOrBlank()) {
                                      navController.navigate(Routes.productDetail(qr.embeddedBarcode))
                                    } else {
                                      runCatching {
                                        activityContext.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(qr.url)))
                                      }.onFailure {
                                        mainViewModel.showFeedback("No browser available to open URL.")
                                      }
                                    }
                                  }
                                  is BarcodeValidator.QrPayloadResolution.PromoCode -> {
                                    mainViewModel.showFeedback("Promo '${qr.code}' selected.")
                                    navController.navigate(Routes.receiptSubmit(qr.campaignHint))
                                  }
                                  BarcodeValidator.QrPayloadResolution.ProductBarcode -> {
                                    navController.navigate(Routes.productDetail(scan.barcodeValue))
                                  }
                                }
                              },
                              onRescan = { scanContext ->
                                mainViewModel.setScanContext(scanContext)
                                navigateTopLevel(Routes.SCAN)
                              },
                              onDeleteEntry = { localId -> mainViewModel.deleteHistoryEntry(localId) },
                              onClearAll = { mainViewModel.clearScanHistory() },
                              onOpenScan = { navigateTopLevel(Routes.SCAN) },
                            )
                          }
                        }

                        composable(Routes.NOTIFICATIONS) {
                          NotificationsScreen(
                            uiState = uiState,
                            onBack = { navController.popBackStack() },
                            onMarkRead = { id -> mainViewModel.markNotificationRead(id) },
                          )
                        }

                        composable(Routes.COUNTRY_LANGUAGE) {
                          CountryLanguageScreen(
                            currentCountryCode = uiState.preferences.countryCode,
                            onBack = { navController.popBackStack() },
                            onSelectMarket = { country, lang ->
                              mainViewModel.updateRegion(country, lang)
                              navController.popBackStack()
                            },
                          )
                        }

                        composable(Routes.PRIVACY_SETTINGS) {
                          PrivacySettingsScreen(
                            uiState = uiState,
                            onBack = { navController.popBackStack() },
                            onToggleMarketing = { mainViewModel.setMarketingConsent(it) },
                            onRequestDeletion = { mainViewModel.requestDeletion(it) },
                          )
                        }

                        composable(Routes.TERMS_RULES) {
                          TermsRulesScreen(onBack = { navController.popBackStack() })
                        }

                        composable(Routes.HELP_SUPPORT) {
                          HelpSupportScreen(onBack = { navController.popBackStack() })
                        }

                        composable(Routes.ADMIN_CONSOLE) {
                          AdminConsoleScreen(
                            uiState = uiState,
                            onBack = { navController.popBackStack() },
                            onReviewPayout = { req, status, notes -> mainViewModel.reviewPayoutRequest(req, status, notes) },
                            onUpdateUserStatus = { uid, status, reason -> mainViewModel.updateUserAccountStatus(uid, status, reason) },
                            onCreditBonusPoints = { uid, pts, reason -> mainViewModel.adminCreditBonusPoints(uid, pts, reason) },
                            onApproveReceipt = { r, pts -> mainViewModel.approveReceipt(r, pts) },
                            onRejectReceipt = { r, reason -> mainViewModel.rejectReceipt(r, reason) },
                            onCreateCampaign = { partner, title, terms, pts, countries ->
                              mainViewModel.createCampaign(partner, title, terms, pts, countries)
                            },
                            onSeedStarterCampaigns = { mainViewModel.seedStarterCampaigns() },
                            onUpdateCampaignStatus = { camp, status -> mainViewModel.updateCampaignStatus(camp, status) },
                            onVerifyProduct = { product -> mainViewModel.verifyCatalogProduct(product) },
                            onCompleteDeletion = { req -> mainViewModel.completeDeletionRequest(req) },
                            onLoadUserWalletLedger = { uid -> mainViewModel.fetchUserLedgerForAdmin(uid) },
                            onReverseTransaction = { tx, reason -> mainViewModel.reverseTransaction(tx, reason) },
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
      }
    }
  }
}

@Composable
private fun AdaptiveBottomNavigationBar(
  tabs: List<NavDestinationTab>,
  currentRoute: String?,
  onSelectTab: (String) -> Unit,
) {
  Column {
    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f))
    NavigationBar(
      containerColor = MaterialTheme.colorScheme.surface,
      tonalElevation = 0.dp,
    ) {
      tabs.forEachIndexed { idx, tab ->
        val selected = currentRoute == tab.route || (currentRoute == Routes.DISCOVER && idx == 0)
        val isScanTab = tab.route == Routes.SCAN
        NavigationBarItem(
          selected = selected,
          onClick = { onSelectTab(tab.route) },
          modifier = Modifier.testTag("nav_tab_${tab.label.lowercase()}"),
          icon = {
            if (isScanTab && !selected) {
              androidx.compose.material3.Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
              ) {
                Icon(
                  imageVector = tab.selectedIcon,
                  contentDescription = tab.label,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp).size(20.dp),
                )
              }
            } else {
              Icon(
                imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                contentDescription = tab.label,
              )
            }
          },
          label = {
            Text(
              text = tab.label,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              fontWeight = if (selected || isScanTab) FontWeight.Bold else FontWeight.Medium,
            )
          },
          colors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
            unselectedIconColor = if (isScanTab) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = if (isScanTab) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
          ),
        )
      }
    }
  }
}

@Composable
private fun AdaptiveNavigationRail(
  tabs: List<NavDestinationTab>,
  currentRoute: String?,
  onSelectTab: (String) -> Unit,
) {
  NavigationRail(
    containerColor = MaterialTheme.colorScheme.surface,
    header = {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 12.dp),
      ) {
        androidx.compose.material3.Surface(
          color = MaterialTheme.colorScheme.primaryContainer,
          shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
        ) {
          Icon(
            imageVector = Icons.Rounded.QrCodeScanner,
            contentDescription = "SCAN2MONEY",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(8.dp).size(24.dp),
          )
        }
      }
    },
  ) {
    Spacer(Modifier.height(8.dp))
    tabs.forEachIndexed { idx, tab ->
      val selected = currentRoute == tab.route || (currentRoute == Routes.DISCOVER && idx == 0)
      NavigationRailItem(
        selected = selected,
        onClick = { onSelectTab(tab.route) },
        modifier = Modifier.testTag("nav_tab_${tab.label.lowercase()}"),
        icon = {
          Icon(
            imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
            contentDescription = tab.label,
          )
        },
        label = {
          Text(
            text = tab.label,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
          )
        },
        colors = NavigationRailItemDefaults.colors(
          selectedIconColor = MaterialTheme.colorScheme.primary,
          selectedTextColor = MaterialTheme.colorScheme.primary,
          indicatorColor = MaterialTheme.colorScheme.primaryContainer,
          unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
          unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
      )
    }
  }
}
