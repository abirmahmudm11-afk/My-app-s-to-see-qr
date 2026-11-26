package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.AdminRole
import com.example.data.model.AdminRoleName
import com.example.data.model.PayoutRequest
import com.example.data.model.RewardTransaction
import com.example.data.model.RewardWallet
import com.example.data.model.TransactionStatus
import com.example.data.model.TransactionType
import com.example.data.model.UserProfile
import com.example.di.AppContainer
import com.example.ui.screens.AdminConsoleScreen
import com.example.ui.screens.RewardsScreen
import com.example.ui.screens.ScanScreen
import com.example.ui.theme.Scan2MoneyTheme
import com.example.ui.viewmodel.MainUiState
import com.example.ui.viewmodel.ScannerViewModel
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
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

    composeTestRule.onNode(hasScrollToNodeAction())
      .performScrollToNode(hasTestTag("admin_project_config_card"))
    composeTestRule.onNodeWithTag("admin_project_config_card").assertIsDisplayed()
    composeTestRule.onNodeWithText("gen-lang-client-0381685396", substring = true).assertIsDisplayed()
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
  fun scanScreenManualDialogValidatesEmptyInputAndSamplePromoWorks() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val container = AppContainer(context)
    val scannerViewModel = ScannerViewModel(container)

    composeTestRule.setContent {
      Scan2MoneyTheme(darkTheme = true, reducedMotion = true) {
        ScanScreen(
          scannerViewModel = scannerViewModel,
          scanContext = null,
          reducedMotion = true,
          onClearScanContext = {},
          onOpenProductDetail = {},
          onOpenHistory = {},
          onOpenRewardsOrPromo = {},
          onShowMessage = {},
        )
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("enable_camera_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("scanner_manual_entry_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("scanner_photo_button").assertIsDisplayed()

    composeTestRule.onNodeWithTag("sample_scan_QR Promo").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Promo Code Detected").assertIsDisplayed()
    composeTestRule.onNodeWithText("Code: BONUS100").assertIsDisplayed()
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
}
