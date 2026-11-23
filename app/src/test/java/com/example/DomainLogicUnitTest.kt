package com.example

import com.example.data.model.Campaign
import com.example.data.model.CampaignStatus
import com.example.data.model.CatalogProduct
import com.example.data.model.RewardTransaction
import com.example.data.model.TransactionStatus
import com.example.domain.BarcodeValidationResult
import com.example.domain.BarcodeValidator
import com.example.domain.CampaignEligibilityEngine
import com.example.domain.ReceiptHashUtil
import com.example.domain.SupportedBarcodeFormat
import com.google.firebase.Timestamp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DomainLogicUnitTest {

    @Test
    fun `barcode validator validates valid and invalid EAN-13 check digits`() {
        val valid = BarcodeValidator.validate("3017620422003", SupportedBarcodeFormat.EAN_13)
        assertTrue(valid is BarcodeValidationResult.Valid)

        val invalid = BarcodeValidator.validate("3017620422004", SupportedBarcodeFormat.EAN_13)
        assertTrue(invalid is BarcodeValidationResult.Invalid)
    }

    @Test
    fun `barcode validator validates EAN-8, UPC-A, UPC-E, and 1D or 2D formats`() {
        assertTrue(BarcodeValidator.validate("96385074", SupportedBarcodeFormat.EAN_8) is BarcodeValidationResult.Valid)
        assertTrue(BarcodeValidator.validate("036000291452", SupportedBarcodeFormat.UPC_A) is BarcodeValidationResult.Valid)
        assertTrue(BarcodeValidator.validate("04252614", SupportedBarcodeFormat.UPC_E) is BarcodeValidationResult.Valid)
        assertTrue(BarcodeValidator.validate("ABC-1234", SupportedBarcodeFormat.CODE_128) is BarcodeValidationResult.Valid)
        assertTrue(BarcodeValidator.validate("CODE39-TEST", SupportedBarcodeFormat.CODE_39) is BarcodeValidationResult.Valid)
        assertTrue(BarcodeValidator.validate("CODE93-TEST", SupportedBarcodeFormat.CODE_93) is BarcodeValidationResult.Valid)
        assertTrue(BarcodeValidator.validate("https://example.org/p/1", SupportedBarcodeFormat.QR_CODE) is BarcodeValidationResult.Valid)
        assertTrue(BarcodeValidator.validate("DM-998877", SupportedBarcodeFormat.DATA_MATRIX) is BarcodeValidationResult.Valid)
        assertTrue(BarcodeValidator.validate("PDF417-PAYLOAD", SupportedBarcodeFormat.PDF417) is BarcodeValidationResult.Valid)
        assertTrue(BarcodeValidator.validate("AZTEC-PAYLOAD", SupportedBarcodeFormat.AZTEC) is BarcodeValidationResult.Valid)
        assertTrue(BarcodeValidator.validate("12345678", SupportedBarcodeFormat.ITF) is BarcodeValidationResult.Valid)
        assertTrue(BarcodeValidator.validate("A12345B", SupportedBarcodeFormat.CODABAR) is BarcodeValidationResult.Valid)
    }

    @Test
    fun `barcode validator rejects empty and malformed inputs`() {
        assertTrue(BarcodeValidator.validate("   ", SupportedBarcodeFormat.MANUAL) is BarcodeValidationResult.Invalid)
        assertTrue(BarcodeValidator.validate("12345", SupportedBarcodeFormat.ITF) is BarcodeValidationResult.Invalid)
        assertTrue(BarcodeValidator.validate("INVALID@CHAR", SupportedBarcodeFormat.CODE_39) is BarcodeValidationResult.Invalid)
    }

    @Test
    fun `campaign eligibility enforces active status, window, country, product match, and user cap`() {
        val now = 1_700_000_000L
        val campaign = Campaign(
            id = "camp_1",
            partnerId = "p_1",
            partnerName = "EcoMart",
            title = "Oat Milk Bonus",
            terms = "Valid on select beverages",
            rewardPoints = 50,
            maxPerUser = 2,
            budgetLimitPoints = 5000,
            eligibleCountries = listOf("US", "CA"),
            eligibleBarcodes = listOf("3017620422003"),
            eligibleCategories = listOf("Spreads"),
            status = CampaignStatus.ACTIVE.name,
            startAt = Timestamp(now - 3600, 0),
            endAt = Timestamp(now + 3600, 0)
        )
        val matchingProduct = CatalogProduct(barcode = "3017620422003", category = "Breakfast")
        val nonMatchingProduct = CatalogProduct(barcode = "0000000000000", category = "Snacks")

        // Eligible US match
        val ok = CampaignEligibilityEngine.evaluate(campaign, "US", matchingProduct, emptyList(), now)
        assertTrue(ok.eligible)

        // Wrong country
        val wrongCountry = CampaignEligibilityEngine.evaluate(campaign, "JP", matchingProduct, emptyList(), now)
        assertFalse(wrongCountry.eligible)

        // Wrong product
        val wrongProduct = CampaignEligibilityEngine.evaluate(campaign, "US", nonMatchingProduct, emptyList(), now)
        assertFalse(wrongProduct.eligible)

        // Per-user cap reached
        val previousClaims = listOf(
            RewardTransaction(id = "t1", campaignId = "camp_1", status = TransactionStatus.CREDITED.name),
            RewardTransaction(id = "t2", campaignId = "camp_1", status = TransactionStatus.CREDITED.name)
        )
        val capped = CampaignEligibilityEngine.evaluate(campaign, "US", matchingProduct, previousClaims, now)
        assertFalse(capped.eligible)

        // Expired campaign
        val expired = CampaignEligibilityEngine.evaluate(campaign, "US", matchingProduct, emptyList(), now + 7200)
        assertFalse(expired.eligible)

        // Paused campaign
        val paused = CampaignEligibilityEngine.evaluate(
            campaign.copy(status = CampaignStatus.PAUSED.name),
            "US",
            matchingProduct,
            emptyList(),
            now
        )
        assertFalse(paused.eligible)
    }

    @Test
    fun `receipt hash normalizes whitespace and casing deterministically`() {
        val h1 = ReceiptHashUtil.sha256ReferenceHash("  FreshMart ", "2026-11-22", 18.5, "usd", " inv-001 ")
        val h2 = ReceiptHashUtil.sha256ReferenceHash("freshmart", "2026-11-22", 18.50, "USD", "INV-001")
        val h3 = ReceiptHashUtil.sha256ReferenceHash("freshmart", "2026-11-22", 18.51, "USD", "INV-001")

        assertEquals(h1, h2)
        assertEquals(64, h1.length)
        assertNotEquals(h1, h3)
    }
}
