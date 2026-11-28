package com.example

import com.example.data.local.ScanHistoryEntity
import com.example.data.model.Campaign
import com.example.data.model.CampaignStatus
import com.example.data.model.CatalogProduct
import com.example.data.model.PayoutMethod
import com.example.data.model.PayoutRequest
import com.example.data.model.PayoutStatus
import com.example.data.model.ReceiptSubmission
import com.example.data.model.RewardTransaction
import com.example.data.model.RewardWallet
import com.example.data.model.ScanRecord
import com.example.data.model.TransactionStatus
import com.example.domain.BarcodeValidationResult
import com.example.domain.BarcodeValidator
import com.example.domain.CampaignEligibilityEngine
import com.example.domain.EarningsEngine
import com.example.domain.MarketConfig
import com.example.domain.QuestMissionEngine
import com.example.domain.SupportedBarcodeFormat
import com.example.domain.WithdrawalValidationResult
import com.google.firebase.Timestamp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DomainLogicUnitTest {
  @Test
  fun ean13CheckDigitValidAndInvalid() {
    val valid = BarcodeValidator.validate("3017620422003", SupportedBarcodeFormat.EAN_13)
    assertTrue(valid is BarcodeValidationResult.Valid)

    val invalid = BarcodeValidator.validate("3017620422004", SupportedBarcodeFormat.EAN_13)
    assertTrue(invalid is BarcodeValidationResult.Invalid)
  }

  @Test
  fun upcEExpandsAndValidates() {
    val expanded = BarcodeValidator.expandUpcEToUpcA("04210007")
    assertEquals("042000001007", expanded)
    val result = BarcodeValidator.validate("04210007", SupportedBarcodeFormat.UPC_E)
    assertTrue(result is BarcodeValidationResult.Valid)

    val badCheckDigit = BarcodeValidator.validate("04210005", SupportedBarcodeFormat.UPC_E)
    assertTrue(badCheckDigit is BarcodeValidationResult.Invalid)
  }

  @Test
  fun sha256ReceiptHashIsDeterministicAndNormalized() {
    val h1 = BarcodeValidator.sha256ReceiptHash("FreshMart", "2026-11-20", 14.5, "usd", "INV-901")
    val h2 = BarcodeValidator.sha256ReceiptHash(" freshmart ", "2026-11-20", 14.50, "USD", " inv-901 ")
    assertEquals(h1, h2)
    assertEquals(64, h1.length)
  }

  @Test
  fun campaignEligibilityChecksCountryAndMaxClaims() {
    val campaign = Campaign(
      id = "camp1",
      partnerName = "Oatly",
      title = "Oat Milk Bonus",
      rewardPoints = 150L,
      maxPerUser = 1,
      eligibleCountries = listOf("US"),
      status = CampaignStatus.ACTIVE.name,
      startAt = Timestamp(1000L, 0),
      endAt = Timestamp(5000L, 0),
    )
    val product = CatalogProduct(barcode = "3017620422003", name = "Hazelnut Spread", category = "Snacks")

    val eligible = CampaignEligibilityEngine.evaluate(
      campaign = campaign,
      userCountryCode = "US",
      product = product,
      userTransactions = emptyList(),
      nowSeconds = 2000L,
    )
    assertTrue(eligible.eligible)

    val wrongCountry = CampaignEligibilityEngine.evaluate(
      campaign = campaign,
      userCountryCode = "JP",
      product = product,
      userTransactions = emptyList(),
      nowSeconds = 2000L,
    )
    assertFalse(wrongCountry.eligible)

    val alreadyClaimed = CampaignEligibilityEngine.evaluate(
      campaign = campaign,
      userCountryCode = "US",
      product = product,
      userTransactions = listOf(
        RewardTransaction(campaignId = "camp1", status = TransactionStatus.CREDITED.name),
      ),
      nowSeconds = 2000L,
    )
    assertFalse(alreadyClaimed.eligible)
  }

  @Test
  fun marketConfigFormatsSupportedCurrencies() {
    val us = MarketConfig.findByCountry("US")
    assertEquals("USD", us.currencyCode)
    val formatted = MarketConfig.formatMoney(12.5, "USD")
    assertTrue(formatted.contains("12.50"))
  }

  @Test
  fun qrPayloadClassifierRoutesUrlsPromosAndRejectsUnsafeSchemes() {
    val urlWithGtin = BarcodeValidator.classifyQrPayload(
      "https://shop.example.com/item?gtin=3017620422003",
      SupportedBarcodeFormat.QR_CODE,
    )
    assertTrue(urlWithGtin is BarcodeValidator.QrPayloadResolution.WebUrl)
    val web = urlWithGtin as BarcodeValidator.QrPayloadResolution.WebUrl
    assertEquals("3017620422003", web.embeddedBarcode)

    val promo = BarcodeValidator.classifyQrPayload("PROMO:bonus100", SupportedBarcodeFormat.QR_CODE)
    assertTrue(promo is BarcodeValidator.QrPayloadResolution.PromoCode)
    assertEquals("BONUS100", (promo as BarcodeValidator.QrPayloadResolution.PromoCode).code)

    val campaignQr = BarcodeValidator.classifyQrPayload("scan2money:campaign:oatly_fall", SupportedBarcodeFormat.QR_CODE)
    assertTrue(campaignQr is BarcodeValidator.QrPayloadResolution.PromoCode)
    assertEquals("oatly_fall", (campaignQr as BarcodeValidator.QrPayloadResolution.PromoCode).campaignHint)

    val unsafeJs = BarcodeValidator.classifyQrPayload("javascript:alert(1)", SupportedBarcodeFormat.QR_CODE)
    assertFalse(unsafeJs is BarcodeValidator.QrPayloadResolution.WebUrl)

    val numericQr = BarcodeValidator.classifyQrPayload("3017620422003", SupportedBarcodeFormat.QR_CODE)
    assertTrue(numericQr is BarcodeValidator.QrPayloadResolution.ProductBarcode)
  }

  @Test
  fun payoutRequestDefaultsAreConsistent() {
    val req = PayoutRequest(
      id = "req_1",
      userId = "u_1",
      pointsAmount = 250L,
      payoutMethod = PayoutMethod.PAYPAL_SANDBOX.name,
      destinationHandle = "user@example.com",
    )
    assertEquals(PayoutStatus.PENDING.name, req.status)
    assertEquals(250L, req.pointsAmount)
    assertEquals("PayPal", PayoutMethod.fromString(req.payoutMethod).label)
  }

  @Test
  fun questMissionsComputeProgressAndCompletion() {
    val scans = listOf(
      ScanRecord(barcodeValue = "3017620422003"),
      ScanRecord(barcodeValue = "5449000000996"),
      ScanRecord(barcodeValue = "3017620422003"),
    )
    val receipts = listOf(ReceiptSubmission(id = "r1"))
    val wallet = RewardWallet(availablePoints = 120L)

    val missions = QuestMissionEngine.computeMissions(scans, receipts, wallet)
    assertEquals(4, missions.size)

    val scanCadence = missions.first { it.id == "scan_cadence" }
    assertTrue(scanCadence.isCompleted)
    assertEquals(1f, scanCadence.progressFraction, 0.001f)

    val catalogExplorer = missions.first { it.id == "catalog_discovery" }
    assertFalse(catalogExplorer.isCompleted)
    assertEquals(2, catalogExplorer.currentStep)
    assertEquals(0.4f, catalogExplorer.progressFraction, 0.001f)

    val voucherReadiness = missions.first { it.id == "voucher_threshold" }
    assertTrue(voucherReadiness.isCompleted)
    assertEquals(100, voucherReadiness.currentStep)
  }

  @Test
  fun scanHistoryEntityMapsRichMetadataToScanRecord() {
    val entity = ScanHistoryEntity(
      id = 42L,
      userId = "guest",
      barcodeValue = "3017620422003",
      barcodeFormat = "EAN_13",
      productName = "Nutella Hazelnut Spread",
      brand = "Ferrero",
      category = "Spreads",
      description = "400g jar",
      imageUrl = "https://example.com/img.jpg",
      dataProvider = "Open Food Facts",
      verificationStatus = "UNVERIFIED",
      scanResult = "MATCHED",
      scannedAtMs = 1700000000000L,
      quantity = "400 g",
      nutriscoreGrade = "E",
    )
    val record = entity.toScanRecord()
    assertEquals(42L, record.localHistoryId)
    assertEquals("local_42", record.id)
    assertEquals("Nutella Hazelnut Spread", record.productName)
    assertEquals("Ferrero", record.brand)
    assertEquals("Spreads", record.category)
    assertEquals("400g jar", record.description)
    assertEquals("400 g", record.quantity)
    assertEquals("E", record.nutriscoreGrade)
    assertEquals(1700000000000L, record.localTimestampMs)
  }

  @Test
  fun priceHistoryEntryPreservesMetadataAndSortsChronologically() {
    val older = com.example.data.model.PriceHistoryEntry(
      id = "ph_1",
      barcode = "3017620422003",
      storeName = "Target",
      price = 5.49,
      currencyCode = "USD",
      countryCode = "US",
      sourceProvider = "Simulated Market",
      verified = false,
      recordedAt = Timestamp(1_700_000_000L, 0),
    )
    val newer = com.example.data.model.PriceHistoryEntry(
      id = "ph_2",
      barcode = "3017620422003",
      storeName = "Walmart",
      price = 4.99,
      currencyCode = "USD",
      countryCode = "US",
      sourceProvider = "User Report",
      verified = true,
      recordedAt = Timestamp(1_700_100_000L, 0),
    )
    val sorted = listOf(newer, older).sortedBy { it.recordedAt?.seconds ?: 0L }
    assertEquals(listOf(older, newer), sorted)
    assertEquals(4.99, sorted.minOf { it.price }, 0.001)
    assertEquals(5.49, sorted.maxOf { it.price }, 0.001)
    assertTrue(newer.verified)
  }

  @Test
  fun walletEarnedPointsCombinesActiveRedeemedAndReversedBalances() {
    val wallet = RewardWallet(
      availablePoints = 150L,
      pendingPoints = 50L,
      redeemedPoints = 200L,
      reversedPoints = 70L,
    )
    val txs = listOf(
      RewardTransaction(id = "t1", transactionType = "RECEIPT_REWARD", amount = 320L, status = "CREDITED"),
      RewardTransaction(id = "t2", transactionType = "CAMPAIGN_BONUS", amount = 100L, status = "CREDITED"),
      RewardTransaction(id = "t3", transactionType = "REDEMPTION", amount = 200L, status = "REDEEMED"),
    )
    val fromLedger = txs.filter {
      (it.transactionType == "RECEIPT_REWARD" || it.transactionType == "CAMPAIGN_BONUS") &&
        (it.status == "CREDITED" || it.status == "APPROVED" || it.status == "REVERSED")
    }.sumOf { it.amount }
    val totalEarned = maxOf(
      fromLedger,
      wallet.availablePoints + wallet.redeemedPoints + wallet.reversedPoints,
    )
    assertEquals(420L, totalEarned)
  }

  @Test
  fun earningsEngineCalculatesExactTenPercentShareAndFormatsUsd() {
    assertEquals(0L, EarningsEngine.calculateTenPercentShareMicros(0L))
    assertEquals(0L, EarningsEngine.calculateTenPercentShareMicros(-500L))
    assertEquals(1L, EarningsEngine.calculateTenPercentShareMicros(15L))
    assertEquals(50_000L, EarningsEngine.calculateTenPercentShareMicros(500_000L))
    assertEquals(100_000L, EarningsEngine.calculateTenPercentShareMicros(1_000_000L))
    assertEquals(EarningsEngine.MAX_SINGLE_AD_SHARE_MICROS, EarningsEngine.calculateTenPercentShareMicros(50_000_000L))

    assertEquals("$0.00", EarningsEngine.formatUsdFromMicros(0L))
    assertEquals("$0.05", EarningsEngine.formatUsdFromMicros(50_000L))
    assertEquals("$1.00", EarningsEngine.formatUsdFromMicros(1_000_000L))
    assertEquals("$1.2345", EarningsEngine.formatUsdFromMicros(1_234_500L))
  }

  @Test
  fun earningsEngineValidatesBep20AddressAndOneDollarMinimumWithdrawal() {
    val validAddress = "0x71C7656EC7ab88b098defB751B7401B5f6d8976F"
    assertTrue(EarningsEngine.isValidBep20Address(validAddress))
    assertFalse(EarningsEngine.isValidBep20Address("71C7656EC7ab88b098defB751B7401B5f6d8976F"))
    assertFalse(EarningsEngine.isValidBep20Address("0x1234"))
    assertFalse(EarningsEngine.isValidBep20Address("TQn9Y2khEsLJW1ChVWFMSMeRDow5KcbLSE")) // TRC20 rejected

    val belowMinBalance = EarningsEngine.validateWithdrawal(
      availableMicros = 990_000L, // $0.99
      amountText = "1.00",
      walletAddress = validAddress,
    )
    assertTrue(belowMinBalance is WithdrawalValidationResult.Invalid)

    val badAddress = EarningsEngine.validateWithdrawal(
      availableMicros = 2_500_000L,
      amountText = "1.50",
      walletAddress = "invalid-address",
    )
    assertTrue(badAddress is WithdrawalValidationResult.Invalid)

    val belowMinRequest = EarningsEngine.validateWithdrawal(
      availableMicros = 2_500_000L,
      amountText = "0.50",
      walletAddress = validAddress,
    )
    assertTrue(belowMinRequest is WithdrawalValidationResult.Invalid)

    val exceedsBalance = EarningsEngine.validateWithdrawal(
      availableMicros = 1_500_000L,
      amountText = "2.00",
      walletAddress = validAddress,
    )
    assertTrue(exceedsBalance is WithdrawalValidationResult.Invalid)

    val valid = EarningsEngine.validateWithdrawal(
      availableMicros = 1_500_000L,
      amountText = "1.25",
      walletAddress = "  $validAddress ",
    )
    assertTrue(valid is WithdrawalValidationResult.Valid)
    val ok = valid as WithdrawalValidationResult.Valid
    assertEquals(validAddress, ok.normalizedAddress)
    assertEquals(1_250_000L, ok.amountMicros)
  }
}
