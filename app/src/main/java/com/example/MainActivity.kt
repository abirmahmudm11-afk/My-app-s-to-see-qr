package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
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
import com.example.ui.theme.ElectricLime
import com.example.ui.theme.ObsidianOnLime
import com.example.ui.theme.Scan2MoneyTheme
import com.example.ui.viewmodel.MainAppViewModel
import com.example.ui.viewmodel.ProductDetailViewModel
import com.example.ui.viewmodel.ScannerViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val appContainer = remember { AppContainer(applicationContext) }
      Scan2MoneyRoot(appContainer)
    }
  }
}

private data class BottomTab(
  val route: String,
  val label: String,
  val icon: ImageVector,
)

@Composable
private fun Scan2MoneyRoot(container: AppContainer) {
  val activityContext = LocalContext.current
  val mainViewModel: MainAppViewModel = viewModel(factory = MainAppViewModel.factory(container))
  val scannerViewModel: ScannerViewModel = viewModel(factory = ScannerViewModel.factory(container))
  val productDetailViewModel: ProductDetailViewModel = viewModel(factory = ProductDetailViewModel.factory(container))

  val uiState by mainViewModel.uiState.collectAsStateWithLifecycle()
  val snackbarHostState = remember { SnackbarHostState() }
  var forceAuthModal by remember { mutableStateOf(false) }

  LaunchedEffect(Unit) {
    mainViewModel.attemptAutoSignIn(activityContext)
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

  Scan2MoneyTheme(darkTheme = uiState.preferences.darkThemeEnabled) {
    when {
      !uiState.preferences.onboardingCompleted -> {
        Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
          Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            OnboardingScreen(
              selectedCountryCode = uiState.preferences.countryCode,
              reducedMotion = uiState.preferences.reducedMotionEnabled,
              onComplete = { country -> mainViewModel.completeOnboarding(country) },
              onContinueGuest = { mainViewModel.enterGuestMode() },
            )
          }
        }
      }

      forceAuthModal || (uiState.firebaseUser == null && !uiState.preferences.guestMode) -> {
        Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
          Box(modifier = Modifier.padding(padding).fillMaxSize()) {
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

      else -> {
        key(uiState.firebaseUser?.uid ?: "guest") {
          val navController = rememberNavController()
          val navBackStackEntry by navController.currentBackStackEntryAsState()
          val currentRoute = navBackStackEntry?.destination?.route

          val tabs = remember {
            listOf(
              BottomTab(Routes.HOME, "Home", Icons.Rounded.Home),
              BottomTab(Routes.SCAN, "Scan", Icons.Rounded.QrCodeScanner),
              BottomTab(Routes.SCAN_HISTORY, "History", Icons.Rounded.History),
              BottomTab(Routes.REWARDS, "Rewards", Icons.Rounded.AccountBalanceWallet),
              BottomTab(Routes.PROFILE, "Profile", Icons.Rounded.Person),
            )
          }
          val showBottomBar = currentRoute in tabs.map { it.route } ||
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

          Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
              if (showBottomBar) {
                androidx.compose.foundation.layout.Column {
                  androidx.compose.material3.HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
                    thickness = androidx.compose.ui.unit.Dp.Hairline,
                  )
                  NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = androidx.compose.ui.unit.Dp(0f),
                  ) {
                    tabs.forEach { tab ->
                      val selected = currentRoute == tab.route
                      NavigationBarItem(
                        selected = selected,
                        onClick = { navigateTopLevel(tab.route) },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = {
                          Text(
                            tab.label,
                            maxLines = 1,
                            fontWeight = if (selected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Medium,
                          )
                        },
                        modifier = Modifier.testTag("nav_tab_${tab.label.lowercase()}"),
                        colors = NavigationBarItemDefaults.colors(
                          selectedIconColor = ObsidianOnLime,
                          selectedTextColor = MaterialTheme.colorScheme.primary,
                          indicatorColor = ElectricLime,
                          unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                          unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                      )
                    }
                  }
                }
              }
            },
          ) { innerPadding ->
            NavHost(
              navController = navController,
              startDestination = Routes.HOME,
              modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            ) {
            composable(Routes.HOME) {
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

            composable(Routes.DISCOVER) {
              DiscoverScreen(
                uiState = uiState,
                onSearch = { q -> mainViewModel.searchCatalog(q) },
                onOpenProduct = { barcode -> navController.navigate(Routes.productDetail(barcode)) },
                onOpenScan = { navigateTopLevel(Routes.SCAN) },
              )
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
                onUpdateCampaignStatus = { camp, status -> mainViewModel.updateCampaignStatus(camp, status) },
                onVerifyProduct = { product -> mainViewModel.verifyCatalogProduct(product) },
                onCompleteDeletion = { req -> mainViewModel.completeDeletionRequest(req) },
              )
            }
          }
        }
        }
      }
    }
  }
}
