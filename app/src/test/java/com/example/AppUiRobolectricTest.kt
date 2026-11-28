package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.AdminRole
import com.example.data.model.AdminRoleName
import com.example.data.model.EarningsWallet
import com.example.data.model.PayoutRequest
import com.example.data.model.RewardTransaction
import com.example.data.model.RewardWallet
import com.example.data.model.RewardedAdCompletion
import com.example.data.model.TransactionStatus
import com.example.data.model.TransactionType
import com.example.data.model.UserProfile
import com.example.data.model.WithdrawalRequest
import com.example.data.model.WithdrawalStatus
import com.example.di.AppContainer
import com.example.ui.components.RewardedAdLoadState
import com.example.ui.screens.AdminConsoleScreen
import com.example.ui.screens.EarningsScreen
import com.example.ui.screens.RewardsScreen
import com.example.ui.screens.ScanScreen
import com.example.ui.theme.Scan2MoneyTheme
import com.example.ui.viewmodel.MainUiState
import com.example.ui.viewmodel.ScannerViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w411dp-h915dp-xxhdpi")
class AppUiRobolectricTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  @Test
  fun rewardsScreenDisplaysTotalEarnedPointsAndFiltersLedger() {
    val state = MainUiState(
      wallet = RewardWallet(
        availablePoints = 150L,
        pendingPoints = 50L,
        redeemedPoints = 200L,
        reversedPoints = 70L,
      ),
      transactions = listOf(
        RewardTransaction(
          id = "tx_earn",
          transactionType = TransactionType.RECEIPT_REWARD.name,
          amount = 320L,
          sourceActivity = "Receipt Bonus Alpha",
          status = TransactionStatus.CREDITED.name,
        ),
        RewardTransaction(
          id = "tx_redeem",
          transactionType = TransactionType.REDEMPTION.name,
          amount = 200L,
          sourceActivity = "Grocery Voucher Beta",
          status = TransactionStatus.REDEEMED.name,
        ),
        RewardTransaction(
          id = "tx_rev",
          transactionType = TransactionType.REVERSAL.name,
          amount = 70L,
          sourceActivity = "Audit Reversal Gamma",
          status = TransactionStatus.REVERSED.name,
        ),
      ),
    )

    composeTestRule.setContent {
      Scan2MoneyTheme(darkTheme = true, reducedMotion = true) {
        RewardsScreen(
          uiState = state,
          onSubmitReceipt = {},
          onRedeemVoucher = { _, _ -> },
          onRequestPayout = { _, _, _, _ -> },
          onOpenTransaction = {},
          onSignInRequested = {},
        )
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("wallet_total_earned_points")
      .performScrollTo()
      .assertIsDisplayed()
      .assertTextContains("420 PTS")

    composeTestRule.onNode(hasScrollToNodeAction())
      .performScrollToNode(hasTestTag("rewards_ledger_filter"))
    composeTestRule.onNodeWithTag("rewards_ledger_filter").assertIsDisplayed()

    composeTestRule.onNode(
      hasText("Earned") and hasAnyAncestor(hasTestTag("rewards_ledger_filter")),
    ).performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNode(hasScrollToNodeAction())
      .performScrollToNode(hasText("Receipt Bonus Alpha"))
    composeTestRule.onNodeWithText("Receipt Bonus Alpha").assertIsDisplayed()
    assertTrue(composeTestRule.onAllNodesWithText("Grocery Voucher Beta").fetchSemanticsNodes().isEmpty())

    composeTestRule.onNode(hasScrollToNodeAction())
      .performScrollToNode(hasTestTag("rewards_ledger_filter"))
    composeTestRule.onNode(
      hasText("Redemptions") and hasAnyAncestor(hasTestTag("rewards_ledger_filter")),
    ).performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNode(hasScrollToNodeAction())
      .performScrollToNode(hasText("Grocery Voucher Beta"))
    composeTestRule.onNodeWithText("Grocery Voucher Beta").assertIsDisplayed()
    assertTrue(composeTestRule.onAllNodesWithText("Receipt Bonus Alpha").fetchSemanticsNodes().isEmpty())

    composeTestRule.onNode(hasScrollToNodeAction())
      .performScrollToNode(hasTestTag("rewards_ledger_filter"))
    composeTestRule.onNode(
      hasText("Reversals") and hasAnyAncestor(hasTestTag("rewards_ledger_filter")),
    ).performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNode(hasScrollToNodeAction())
      .performScrollToNode(hasText("Audit Reversal Gamma"))
    composeTestRule.onNodeWithText("Audit Reversal Gamma").assertIsDisplayed()
  }

  @Test
  fun adminConsoleRequiresVerifiedAdminRole() {
    composeTestRule.setContent {
      Scan2MoneyTheme(darkTheme = true, reducedMotion = true) {
        AdminConsoleScreen(
          uiState = MainUiState(adminRole = null),
          onBack = {},
          onReviewPayout = { _, _, _ -> },
          onUpdateUserStatus = { _, _, _ -> },
          onCreditBonusPoints = { _, _, _ -> },
          onApproveReceipt = { _, _ -> },
          onRejectReceipt = { _, _ -> },
          onCreateCampaign = { _, _, _, _, _ -> },
          onUpdateCampaignStatus = { _, _ -> },
          onVerifyProduct = {},
          onCompleteDeletion = {},
        )
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Administrative Access Required").assertIsDisplayed()
    assertTrue(composeTestRule.onAllNodesWithText("Firebase Project & AdMob Config").fetchSemanticsNodes().isEmpty())
  }

  @Test
  fun adminConsoleSuperAdminDisplaysProjectConfigAndAllTabs() {
    val state = MainUiState(
      adminRole = AdminRole(
        id = "uid_abir",
        userId = "uid_abir",
        roleName = AdminRoleName.SUPER_ADMIN.name,
        permissions = listOf("USER_MANAGE", "PAYOUT_REVIEW", "RECEIPT_REVIEW", "CAMPAIGN_MANAGE"),
      ),
      allUsers = listOf(
        UserProfile(
          id = "uid_user_1",
          userId = "uid_user_1",
          displayName = "Abir Mahmud",
          countryCode = "US",
          currencyCode = "USD",
          accountStatus = "ACTIVE",
        ),
      ),
      allPayoutRequests = listOf(
        PayoutRequest(
          id = "pay_1",
          userId = "uid_user_1",
          userDisplayName = "Abir Mahmud",
          rewardTitle = "Grocery Voucher",
          pointsAmount = 150L,
        ),
      ),
    )

    composeTestRule.setContent {
      Scan2MoneyTheme(darkTheme = true, reducedMotion = true) {
        AdminConsoleScreen(
          uiState = state,
          onBack = {},
          onReviewPayout = { _, _, _ -> },
          onUpdateUserStatus = { _, _, _ -> },
          onCreditBonusPoints = { _, _, _ -> },
          onApproveReceipt = { _, _ -> },
          onRejectReceipt = { _, _ -> },
          onCreateCampaign = { _, _, _, _, _ -> },
          onUpdateCampaignStatus = { _, _ -> },
          onVerifyProduct = {},
          onCompleteDeletion = {},
        )
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("admin_tab_overview").assertIsDisplayed()
    composeTestRule.onNodeWithTag("admin_tab_users").assertIsDisplayed()
    composeTestRule.onNodeWithTag("admin_tab_payouts").assertIsDisplayed()
    composeTestRule.onNodeWithTag("admin_tab_receipts").assertIsDisplayed()
    composeTestRule.onNodeWithTag("admin_tab_more").assertIsDisplayed()

    val expectedProjectId = ApplicationProvider.getApplicationContext<Context>().getString(R.string.project_id)
    composeTestRule.onNode(hasScrollToNodeAction())
      .performScrollToNode(hasTestTag("admin_project_config_card"))
    composeTestRule.onNodeWithTag("admin_project_config_card").assertIsDisplayed()
    composeTestRule.onNodeWithText(expectedProjectId, substring = true).assertIsDisplayed()
    composeTestRule.onNodeWithText("abirmahmudm11@gmail.com", substring = true).assertIsDisplayed()

    composeTestRule.onNodeWithTag("admin_tab_users").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Registered Users (1)").assertIsDisplayed()
    composeTestRule.onNodeWithText("Abir Mahmud").assertIsDisplayed()

    composeTestRule.onNodeWithTag("admin_tab_payouts").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("All Payout Requests (1)").assertIsDisplayed()
  }

  @Test
  fun scanScreenManualDialogValidatesEmptyInputAndPromoWorks() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val container = AppContainer(context)
    val scannerViewModel = ScannerViewModel(container)

    var watchedSource: String? = null
    var watchedCode: String? = null
    composeTestRule.setContent {
      Scan2MoneyTheme(darkTheme = false, reducedMotion = true) {
        ScanScreen(
          scannerViewModel = scannerViewModel,
          scanContext = null,
          reducedMotion = true,
          onClearScanContext = {},
          onOpenProductDetail = {},
          onOpenHistory = {},
          onOpenRewardsOrPromo = {},
          onWatchRewardedAdForScan = { source, code ->
            watchedSource = source
            watchedCode = code
          },
          onShowMessage = {},
        )
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("enable_camera_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("scanner_manual_entry_button").assertIsDisplayed().performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("manual_barcode_submit_button").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("manual_barcode_error")
      .assertIsDisplayed()
      .assertTextContains("Enter a barcode or QR value first.")

    composeTestRule.onNodeWithTag("manual_barcode_input").performTextInput("PROMO:BONUS100")
    composeTestRule.onNodeWithTag("manual_barcode_submit_button").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Promo Code Detected").assertIsDisplayed()
    composeTestRule.onNodeWithText("Code: BONUS100").assertIsDisplayed()
    composeTestRule.onNodeWithTag("scanner_watch_rewarded_ad_button").assertIsDisplayed().performClick()
    composeTestRule.waitForIdle()
    assertEquals("QR_SCAN", watchedSource)
    assertEquals("BONUS100", watchedCode)
  }

  @Test
  fun adminConsoleMoreTabValidatesCampaignFormInline() {
    val state = MainUiState(
      adminRole = AdminRole(
        id = "uid_abir",
        userId = "uid_abir",
        roleName = AdminRoleName.SUPER_ADMIN.name,
        permissions = listOf("CAMPAIGN_MANAGE"),
      ),
    )

    composeTestRule.setContent {
      Scan2MoneyTheme(darkTheme = true, reducedMotion = true) {
        AdminConsoleScreen(
          uiState = state,
          onBack = {},
          onReviewPayout = { _, _, _ -> },
          onUpdateUserStatus = { _, _, _ -> },
          onCreditBonusPoints = { _, _, _ -> },
          onApproveReceipt = { _, _ -> },
          onRejectReceipt = { _, _ -> },
          onCreateCampaign = { _, _, _, _, _ -> },
          onUpdateCampaignStatus = { _, _ -> },
          onVerifyProduct = {},
          onCompleteDeletion = {},
        )
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("admin_tab_more").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNode(hasScrollToNodeAction())
      .performScrollToNode(hasText("Publish Active Campaign"))
    composeTestRule.onNodeWithText("Publish Active Campaign").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Enter a partner brand name.").assertIsDisplayed()
  }

  @Test
  fun earningsScreenDisplaysBalanceAdHistoryAndWithdrawals() {
    val state = MainUiState(
      earningsWallet = EarningsWallet(
        availableMicros = 1_450_000L, // $1.45
        lifetimeEarnedMicros = 2_450_000L,
        pendingWithdrawalMicros = 1_000_000L,
        withdrawnMicros = 0L,
        adCompletionCount = 4L,
      ),
      adRewards = listOf(
        RewardedAdCompletion(
          id = "ad_1",
          triggerSource = "QR_SCAN",
          scannedCode = "PROMO:BONUS100",
          adValueMicros = 500_000L,
          userShareMicros = 50_000L,
          currencyCode = "USD",
        ),
      ),
      withdrawalRequests = listOf(
        WithdrawalRequest(
          id = "wd_1",
          network = "BEP20",
          asset = "USDT",
          walletAddress = "0x71C7656EC7ab88b098defB751B7401B5f6d8976F",
          amountMicros = 1_000_000L,
          status = WithdrawalStatus.PENDING.name,
        ),
      ),
    )

    composeTestRule.setContent {
      Scan2MoneyTheme(darkTheme = true, reducedMotion = true) {
        EarningsScreen(
          uiState = state,
          adLoadState = RewardedAdLoadState.Ready,
          onWatchRewardedAd = {},
          onOpenScan = {},
          onOpenRewards = {},
          onRequestWithdrawal = { _, _ -> },
          onSignInRequested = {},
          isSignedIn = true,
        )
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("earnings_total_balance").assertIsDisplayed().assertTextContains("$1.45")

    composeTestRule.onNode(hasScrollToNodeAction())
      .performScrollToNode(hasTestTag("withdrawal_history_section"))
    composeTestRule.onNodeWithText("$1.00 USDT (BEP20)").assertIsDisplayed()
    composeTestRule.onNodeWithText("PENDING").assertIsDisplayed()

    composeTestRule.onNode(hasScrollToNodeAction())
      .performScrollToNode(hasTestTag("ad_rewards_history_section"))
    composeTestRule.onNodeWithText("QR Code Scan Reward").assertIsDisplayed()
    composeTestRule.onNodeWithText("+$0.05").assertIsDisplayed()
  }

  @Test
  fun earningsScreenValidatesOneDollarMinimumAndBep20Address() {
    var submittedAddress: String? = null
    var submittedAmountMicros: Long? = null

    val lowBalanceState = MainUiState(
      earningsWallet = EarningsWallet(availableMicros = 500_000L), // $0.50
    )

    composeTestRule.setContent {
      Scan2MoneyTheme(darkTheme = true, reducedMotion = true) {
        EarningsScreen(
          uiState = lowBalanceState,
          adLoadState = RewardedAdLoadState.Ready,
          onWatchRewardedAd = {},
          onOpenScan = {},
          onOpenRewards = {},
          onRequestWithdrawal = { addr, amt ->
            submittedAddress = addr
            submittedAmountMicros = amt
          },
          onSignInRequested = {},
          isSignedIn = true,
        )
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNode(hasScrollToNodeAction())
      .performScrollToNode(hasTestTag("withdrawal_submit_button"))
    composeTestRule.onNodeWithTag("withdrawal_address_input")
      .performTextInput("0x71C7656EC7ab88b098defB751B7401B5f6d8976F")
    composeTestRule.onNodeWithTag("withdrawal_submit_button").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("withdrawal_validation_error")
      .assertIsDisplayed()
      .assertTextContains("Minimum balance of $1.00 USD", substring = true)
    assertEquals(null, submittedAddress)
    assertEquals(null, submittedAmountMicros)
  }

  @Test
  fun earningsScreenSubmitsValidBinanceBep20Withdrawal() {
    var submittedAddress: String? = null
    var submittedAmountMicros: Long? = null

    val eligibleState = MainUiState(
      earningsWallet = EarningsWallet(availableMicros = 2_500_000L), // $2.50
    )

    composeTestRule.setContent {
      Scan2MoneyTheme(darkTheme = true, reducedMotion = true) {
        EarningsScreen(
          uiState = eligibleState,
          adLoadState = RewardedAdLoadState.Ready,
          onWatchRewardedAd = {},
          onOpenScan = {},
          onOpenRewards = {},
          onRequestWithdrawal = { addr, amt ->
            submittedAddress = addr
            submittedAmountMicros = amt
          },
          onSignInRequested = {},
          isSignedIn = true,
        )
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNode(hasScrollToNodeAction())
      .performScrollToNode(hasTestTag("withdrawal_submit_button"))

    // Reject invalid address first
    composeTestRule.onNodeWithTag("withdrawal_address_input").performTextInput("0xBAD")
    composeTestRule.onNodeWithTag("withdrawal_submit_button").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("withdrawal_validation_error")
      .assertIsDisplayed()
      .assertTextContains("BEP-20", substring = true)

    // Enter valid Binance USDT BEP-20 address & amount $1.50
    val validAddress = "0x71C7656EC7ab88b098defB751B7401B5f6d8976F"
    composeTestRule.onNodeWithTag("withdrawal_address_input").performTextClearance()
    composeTestRule.onNodeWithTag("withdrawal_address_input").performTextInput(validAddress)
    composeTestRule.onNodeWithTag("withdrawal_amount_input").performTextClearance()
    composeTestRule.onNodeWithTag("withdrawal_amount_input").performTextInput("1.50")
    composeTestRule.onNodeWithTag("withdrawal_submit_button").performClick()
    composeTestRule.waitForIdle()

    assertEquals(validAddress, submittedAddress)
    assertEquals(1_500_000L, submittedAmountMicros)
  }

  @Test
  fun lightModeRendersHomeAndEarningsWithExtendedLightColors() {
    var observedDarkFlag: Boolean? = null
    val state = MainUiState(
      earningsWallet = EarningsWallet(availableMicros = 2_100_000L),
    )

    composeTestRule.setContent {
      Scan2MoneyTheme(darkTheme = false, reducedMotion = true) {
        observedDarkFlag = com.example.ui.theme.LocalExtendedColors.current.isDark
        com.example.ui.screens.HomeScreen(
          uiState = state,
          onOpenScan = {},
          onRescan = {},
          onOpenDiscover = {},
          onOpenEarnings = {},
          onWatchRewardedAd = {},
          onOpenRewards = {},
          onOpenReceiptSubmit = {},
          onOpenProduct = {},
          onOpenNotifications = {},
          onOpenScanHistory = {},
          onOpenAdmin = {},
          onSignInRequested = {},
        )
      }
    }

    composeTestRule.waitForIdle()
    assertEquals(false, observedDarkFlag)
    composeTestRule.onNodeWithTag("home_earnings_balance_value")
      .assertIsDisplayed()
      .assertTextContains("$2.10")
    composeTestRule.onNodeWithTag("home_scan_item_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("home_open_earnings_button").assertIsDisplayed()
  }

  @Test
  fun onboardingScreenRendersCompactWhiteModeByDefault() {
    var darkModeActive: Boolean? = null
    var completedCountry: String? = null

    composeTestRule.setContent {
      Scan2MoneyTheme(reducedMotion = true) {
        darkModeActive = com.example.ui.theme.LocalExtendedColors.current.isDark
        com.example.ui.screens.OnboardingScreen(
          selectedCountryCode = "US",
          reducedMotion = true,
          onComplete = { completedCountry = it },
        )
      }
    }

    composeTestRule.waitForIdle()
    assertEquals(false, darkModeActive)
    composeTestRule.onNodeWithText("Live Barcode & QR Radar").assertIsDisplayed()

    composeTestRule.onNodeWithTag("onboarding_step_tab_1").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("10% Share on Every Ad").assertIsDisplayed()

    composeTestRule.onNodeWithTag("onboarding_market_GB").performScrollTo().performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("onboarding_continue_button").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    assertEquals("GB", completedCountry)
  }

  @Test
  fun authScreenTriggersGoogleSignInAndGuestMode() {
    var googleClicked = false
    var guestClicked = false

    composeTestRule.setContent {
      Scan2MoneyTheme(reducedMotion = true) {
        com.example.ui.screens.AuthScreen(
          isSigningIn = false,
          onGoogleSignIn = { googleClicked = true },
          onContinueGuest = { guestClicked = true },
        )
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Protected by Google Sign-In").assertIsDisplayed()
    composeTestRule.onNodeWithTag("google_sign_in_button").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    assertTrue(googleClicked)

    composeTestRule.onNodeWithTag("continue_guest_button").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    assertTrue(guestClicked)
  }

  @Test
  fun profileScreenTogglesDarkAppearanceSwitch() {
    var toggledTo: Boolean? = null
    val lightState = MainUiState(
      preferences = com.example.data.model.AppPreferences(darkThemeEnabled = false),
    )

    composeTestRule.setContent {
      Scan2MoneyTheme(darkTheme = false, reducedMotion = true) {
        com.example.ui.screens.ProfileScreen(
          uiState = lightState,
          onOpenScanHistory = {},
          onOpenNotifications = {},
          onOpenCountryLanguage = {},
          onOpenPrivacy = {},
          onOpenTerms = {},
          onOpenHelp = {},
          onOpenAdmin = {},
          onToggleDarkTheme = { toggledTo = it },
          onToggleReducedMotion = {},
          onSignIn = {},
          onSignOut = {},
        )
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNode(hasScrollToNodeAction())
      .performScrollToNode(hasTestTag("profile_dark_theme_switch"))
    composeTestRule.onNodeWithText("Clean white light theme").assertIsDisplayed()
    composeTestRule.onNodeWithTag("profile_dark_theme_switch").performClick()
    composeTestRule.waitForIdle()
    assertEquals(true, toggledTo)
  }
}
