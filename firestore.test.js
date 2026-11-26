import { createRequire } from "node:module";
import { test, before, after, beforeEach } from "node:test";
import fs from "node:fs";

const require = createRequire(import.meta.url);
const { initializeTestEnvironment, assertFails, assertSucceeds } = require("@firebase/rules-unit-testing");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";
const ADMIN_UID = "admin_789";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated users cannot read or write any collection", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("campaigns").where("status", "==", "ACTIVE").get());
  await assertFails(unauthDb.collection("receipts").where("userId", "==", ALICE_UID).get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("scans").doc("scan_1").set({
      userId: ALICE_UID,
      barcodeValue: "3017620422003",
      barcodeFormat: "EAN_13",
      productName: "Nutella",
      brand: "Ferrero",
      sourceProvider: "Open Food Facts",
      verificationStatus: "OPEN_FOOD_FACTS",
      scanResult: "MATCHED",
      scannedAt: new Date(Date.now() - 1000),
    })
  );
});

test("User profile isolation, valid create/update, and anti-spoofing", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID, { email: "alice@example.com", email_verified: true }).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID, { email: "bob@example.com", email_verified: true }).firestore();
  const past = new Date(Date.now() - 5000);

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice",
      preferredLanguage: "en",
      countryCode: "US",
      currencyCode: "USD",
      accountStatus: "ACTIVE",
      marketingConsent: false,
      darkThemeEnabled: false,
      reducedMotionEnabled: false,
      createdAt: past,
      updatedAt: past,
    })
  );

  // Partial update allowed fields
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      countryCode: "GB",
      currencyCode: "GBP",
      marketingConsent: true,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Reject ghost field or immutable createdAt mutation
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      createdAt: new Date(Date.now() - 500),
      updatedAt: new Date(Date.now() - 500),
    })
  );
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      isAdmin: true,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Cross-user read/write blocked
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).update({ displayName: "Hacked", updatedAt: new Date(Date.now() - 500) }));
});

test("Email spoofing test: unverified admin email cannot access admin actions", async () => {
  const spoofDb = testEnv.authenticatedContext("spoof_uid", {
    email: "m28044963@gmail.com",
    email_verified: false,
  }).firestore();

  await assertFails(
    spoofDb.collection("campaigns").doc("camp_1").set({
      partnerId: "p1",
      partnerName: "Partner",
      title: "Fake Camp",
      terms: "Terms",
      rewardType: "SANDBOX_POINTS",
      rewardPoints: 100,
      maxPerUser: 1,
      budgetLimitPoints: 1000,
      eligibleCountries: ["US"],
      eligibleBarcodes: [],
      eligibleCategories: [],
      status: "ACTIVE",
      startAt: new Date(Date.now() - 10000),
      endAt: new Date(Date.now() + 100000),
      createdAt: new Date(Date.now() - 1000),
      updatedAt: new Date(Date.now() - 1000),
    })
  );
});

test("Wallet & Ledger integrity: users cannot self-credit points or tamper with ledger", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date(Date.now() - 1000);

  // Wallet initialization with 0 balances succeeds
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("wallet").doc("summary").set({
      userId: ALICE_UID,
      unitCode: "PTS",
      availablePoints: 0,
      pendingPoints: 0,
      redeemedPoints: 0,
      reversedPoints: 0,
      updatedAt: now,
    })
  );

  // Self-crediting availablePoints fails
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("wallet").doc("summary").update({
      availablePoints: 500,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Creating a CREDITED transaction as regular user fails
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("transactions").doc("tx_1").set({
      userId: ALICE_UID,
      transactionType: "CAMPAIGN_BONUS",
      amount: 500,
      unitCode: "PTS",
      sourceActivity: "Self credit",
      status: "CREDITED",
      idempotencyKey: "key_1234",
      auditReference: "ref_1",
      createdAt: now,
    })
  );
});

test("Receipt submission enforces PENDING status and admin-only review + atomic reward credit", async () => {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    const db = ctx.firestore();
    await db.collection("admins").doc(ADMIN_UID).set({
      userId: ADMIN_UID,
      roleName: "SUPER_ADMIN",
      permissions: ["REVIEW_RECEIPTS", "MANAGE_CAMPAIGNS"],
      createdAt: new Date(Date.now() - 10000),
    });
    await db.collection("users").doc(ALICE_UID).collection("wallet").doc("summary").set({
      userId: ALICE_UID,
      unitCode: "PTS",
      availablePoints: 0,
      pendingPoints: 0,
      redeemedPoints: 0,
      reversedPoints: 0,
      updatedAt: new Date(Date.now() - 10000),
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const adminDb = testEnv.authenticatedContext(ADMIN_UID).firestore();
  const now = new Date(Date.now() - 2000);

  // Auto-approved receipt by user fails
  await assertFails(
    aliceDb.collection("receipts").doc("rec_bad").set({
      userId: ALICE_UID,
      storeName: "FreshMart",
      transactionDate: "2026-11-22",
      currencyCode: "USD",
      totalAmount: 24.5,
      receiptReference: "INV-001",
      receiptReferenceHash: "hash_1234567890abcdef",
      reviewStatus: "APPROVED",
      submittedAt: now,
      updatedAt: now,
    })
  );

  // Valid PENDING receipt succeeds
  await assertSucceeds(
    aliceDb.collection("receipts").doc("rec_1").set({
      userId: ALICE_UID,
      storeName: "FreshMart",
      transactionDate: "2026-11-22",
      currencyCode: "USD",
      totalAmount: 24.5,
      receiptReference: "INV-001",
      receiptReferenceHash: "hash_1234567890abcdef",
      reviewStatus: "PENDING",
      submittedAt: now,
      updatedAt: now,
    })
  );

  // Owner cannot self-approve
  await assertFails(
    aliceDb.collection("receipts").doc("rec_1").update({
      reviewStatus: "APPROVED",
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Unfiltered query on receipts by regular user fails
  await assertFails(aliceDb.collection("receipts").get());
  // Filtered query by owner succeeds
  await assertSucceeds(aliceDb.collection("receipts").where("userId", "==", ALICE_UID).get());

  // Admin approves receipt
  await assertSucceeds(
    adminDb.collection("receipts").doc("rec_1").update({
      reviewStatus: "APPROVED",
      reviewedBy: ADMIN_UID,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Terminal state lock: cannot update APPROVED receipt again
  await assertFails(
    adminDb.collection("receipts").doc("rec_1").update({
      reviewStatus: "REJECTED",
      rejectionReason: "Changed mind",
      reviewedBy: ADMIN_UID,
      updatedAt: new Date(Date.now() - 200),
    })
  );
});

test("Campaigns & Product Catalog query alignment and user-submitted restrictions", async () => {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    const db = ctx.firestore();
    await db.collection("campaigns").doc("camp_active").set({
      partnerId: "p1",
      partnerName: "GreenGrocer",
      title: "Organic Oat Bonus",
      terms: "Scan eligible products.",
      rewardType: "SANDBOX_POINTS",
      rewardPoints: 50,
      maxPerUser: 3,
      budgetLimitPoints: 5000,
      eligibleCountries: ["US"],
      eligibleBarcodes: ["3017620422003"],
      eligibleCategories: ["beverages"],
      status: "ACTIVE",
      startAt: new Date(Date.now() - 10000),
      endAt: new Date(Date.now() + 86400000),
      createdAt: new Date(Date.now() - 10000),
      updatedAt: new Date(Date.now() - 10000),
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("campaigns").get());
  await assertSucceeds(aliceDb.collection("campaigns").where("status", "==", "ACTIVE").get());

  // Regular user cannot submit a VERIFIED_MERCHANT product
  await assertFails(
    aliceDb.collection("products_catalog").doc("3017620422003").set({
      barcode: "3017620422003",
      name: "Test",
      brand: "Brand",
      category: "Snacks",
      description: "Desc",
      imageUrl: "",
      manufacturer: "",
      countryOfOrigin: "",
      dataProvider: "Merchant",
      attributionText: "Merchant",
      verificationStatus: "VERIFIED_MERCHANT",
      submittedBy: ALICE_UID,
      createdAt: new Date(Date.now() - 1000),
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Regular user can submit USER_SUBMITTED_UNVERIFIED product
  await assertSucceeds(
    aliceDb.collection("products_catalog").doc("9999999999999").set({
      barcode: "9999999999999",
      name: "Community Snack",
      brand: "Local",
      category: "Snacks",
      description: "Reported by shopper",
      imageUrl: "",
      manufacturer: "",
      countryOfOrigin: "",
      dataProvider: "Community Submission",
      attributionText: "User submitted (unverified)",
      verificationStatus: "USER_SUBMITTED_UNVERIFIED",
      submittedBy: ALICE_UID,
      createdAt: new Date(Date.now() - 1000),
      updatedAt: new Date(Date.now() - 1000),
    })
  );
});

test("User REDEMPTION WriteBatch validates getAfter wallet balance decrement and blocks tampering", async () => {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    const db = ctx.firestore();
    await db.collection("users").doc(ALICE_UID).collection("wallet").doc("summary").set({
      userId: ALICE_UID,
      unitCode: "PTS",
      availablePoints: 250,
      pendingPoints: 0,
      redeemedPoints: 0,
      reversedPoints: 0,
      updatedAt: new Date(Date.now() - 10000),
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date(Date.now() - 500);
  const walletRef = aliceDb.collection("users").doc(ALICE_UID).collection("wallet").doc("summary");

  // Valid atomic WriteBatch redemption of 100 PTS
  const validBatch = aliceDb.batch();
  validBatch.update(walletRef, {
    availablePoints: 150,
    redeemedPoints: 100,
    updatedAt: now,
  });
  validBatch.set(aliceDb.collection("users").doc(ALICE_UID).collection("transactions").doc("tx_redeem_1"), {
    userId: ALICE_UID,
    transactionType: "REDEMPTION",
    amount: 100,
    unitCode: "PTS",
    sourceActivity: "Sandbox Voucher: Transit Pass",
    status: "REDEEMED",
    idempotencyKey: "redeem_alice_1",
    auditReference: "SANDBOX-VOUCHER-001",
    notes: "Non-cash sandbox catalog reward redemption",
    createdAt: now,
  });
  await assertSucceeds(validBatch.commit());

  // Redemption without matching wallet decrement fails getAfter check
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("transactions").doc("tx_redeem_bad").set({
      userId: ALICE_UID,
      transactionType: "REDEMPTION",
      amount: 50,
      unitCode: "PTS",
      sourceActivity: "Sandbox Voucher: Fake",
      status: "REDEEMED",
      idempotencyKey: "redeem_alice_bad",
      auditReference: "SANDBOX-VOUCHER-BAD",
      createdAt: now,
    })
  );

  // Batch exceeding available balance (150 - 200 < 0) fails
  const overdrawBatch = aliceDb.batch();
  overdrawBatch.update(walletRef, {
    availablePoints: -50,
    redeemedPoints: 300,
    updatedAt: now,
  });
  overdrawBatch.set(aliceDb.collection("users").doc(ALICE_UID).collection("transactions").doc("tx_redeem_over"), {
    userId: ALICE_UID,
    transactionType: "REDEMPTION",
    amount: 200,
    unitCode: "PTS",
    sourceActivity: "Overdraw",
    status: "REDEEMED",
    idempotencyKey: "redeem_alice_over",
    auditReference: "SANDBOX-VOUCHER-OVER",
    createdAt: now,
  });
  await assertFails(overdrawBatch.commit());
});

test("Admin atomic receipt approval initializes wallet if missing and logs audit entry", async () => {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    const db = ctx.firestore();
    await db.collection("admins").doc(ADMIN_UID).set({
      userId: ADMIN_UID,
      roleName: "SUPER_ADMIN",
      permissions: ["REVIEW_RECEIPTS"],
      createdAt: new Date(Date.now() - 10000),
    });
    await db.collection("receipts").doc("rec_init_1").set({
      userId: BOB_UID,
      storeName: "Corner Market",
      transactionDate: "2026-11-22",
      currencyCode: "USD",
      totalAmount: 19.99,
      receiptReference: "CM-900",
      receiptReferenceHash: "abcdef1234567890abcdef1234567890",
      reviewStatus: "PENDING",
      submittedAt: new Date(Date.now() - 5000),
      updatedAt: new Date(Date.now() - 5000),
    });
  });

  const adminDb = testEnv.authenticatedContext(ADMIN_UID).firestore();
  const now = new Date(Date.now() - 500);
  const batch = adminDb.batch();
  batch.update(adminDb.collection("receipts").doc("rec_init_1"), {
    reviewStatus: "APPROVED",
    reviewedBy: ADMIN_UID,
    updatedAt: now,
  });
  batch.set(adminDb.collection("users").doc(BOB_UID).collection("wallet").doc("summary"), {
    userId: BOB_UID,
    unitCode: "PTS",
    availablePoints: 75,
    pendingPoints: 0,
    redeemedPoints: 0,
    reversedPoints: 0,
    updatedAt: now,
  });
  batch.set(adminDb.collection("users").doc(BOB_UID).collection("transactions").doc("receipt_rec_init_1"), {
    userId: BOB_UID,
    transactionType: "RECEIPT_REWARD",
    amount: 75,
    unitCode: "PTS",
    sourceActivity: "Verified Receipt: Corner Market",
    status: "CREDITED",
    idempotencyKey: "receipt_rec_init_1",
    auditReference: "AUDIT-INIT01",
    createdAt: now,
  });
  batch.set(adminDb.collection("audit_logs").doc("audit_init_1"), {
    actorId: ADMIN_UID,
    action: "APPROVE_RECEIPT",
    entityType: "RECEIPT",
    entityId: "rec_init_1",
    reason: "Approved in test",
    timestamp: now,
  });
  await assertSucceeds(batch.commit());
});

test("abirmahmudm11@gmail.com has verified SUPER_ADMIN access and unverified spoof is denied", async () => {
  const verifiedAdminDb = testEnv.authenticatedContext("abir_uid", {
    email: "abirmahmudm11@gmail.com",
    email_verified: true,
  }).firestore();
  const spoofAdminDb = testEnv.authenticatedContext("abir_spoof", {
    email: "abirmahmudm11@gmail.com",
    email_verified: false,
  }).firestore();
  const now = new Date(Date.now() - 1000);

  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    const db = ctx.firestore();
    await db.collection("payout_requests").doc("pay_abir_1").set({
      userId: ALICE_UID,
      userDisplayName: "Alice",
      rewardTitle: "Grocery Voucher",
      pointsAmount: 100,
      payoutMethod: "GIFT_VOUCHER",
      destinationHandle: "alice@example.com",
      status: "PENDING",
      requestedAt: now,
      updatedAt: now,
    });
  });

  // Unverified spoof cannot read all payout_requests or approve
  await assertFails(spoofAdminDb.collection("payout_requests").get());
  await assertFails(
    spoofAdminDb.collection("payout_requests").doc("pay_abir_1").update({
      status: "PAID",
      reviewedBy: "abir_spoof",
      adminNotes: "Spoof",
      updatedAt: now,
    })
  );

  // Verified abirmahmudm11@gmail.com can read all payouts and mark PAID
  await assertSucceeds(verifiedAdminDb.collection("payout_requests").get());
  await assertSucceeds(
    verifiedAdminDb.collection("payout_requests").doc("pay_abir_1").update({
      status: "PAID",
      reviewedBy: "abir_uid",
      adminNotes: "Paid by Abir",
      updatedAt: now,
    })
  );
});

test("Scanned product details, ScanRecord description/category, and price_history rules", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date(Date.now() - 1000);

  // ScanRecord with description & category succeeds
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("scans").doc("scan_desc_1").set({
      userId: ALICE_UID,
      barcodeValue: "3017620422003",
      barcodeFormat: "EAN_13",
      productName: "Nutella Hazelnut Spread",
      brand: "Ferrero",
      category: "Breakfast Spreads",
      description: "400 g hazelnut spread with cocoa",
      sourceProvider: "Open Food Facts",
      verificationStatus: "OPEN_FOOD_FACTS",
      scanResult: "MATCHED",
      scannedAt: now,
    })
  );

  // Scanned product snapshot with OPEN_FOOD_FACTS succeeds for authenticated user
  await assertSucceeds(
    aliceDb.collection("products_catalog").doc("3017620422003").set({
      barcode: "3017620422003",
      name: "Nutella Hazelnut Spread",
      brand: "Ferrero",
      category: "Breakfast Spreads",
      description: "400 g jar",
      imageUrl: "https://example.com/nutella.jpg",
      manufacturer: "Ferrero",
      countryOfOrigin: "Italy",
      dataProvider: "Open Food Facts",
      attributionText: "Data © Open Food Facts contributors (ODbL)",
      verificationStatus: "OPEN_FOOD_FACTS",
      submittedBy: ALICE_UID,
      createdAt: now,
      updatedAt: now,
    })
  );

  // Valid unverified price_history entry succeeds
  await assertSucceeds(
    aliceDb.collection("products_catalog").doc("3017620422003").collection("price_history").doc("ph_1").set({
      barcode: "3017620422003",
      storeName: "FreshMart",
      price: 4.49,
      currencyCode: "USD",
      countryCode: "US",
      sourceProvider: "Community Report",
      verified: false,
      recordedBy: ALICE_UID,
      recordedAt: now,
    })
  );

  // Negative/zero price or impersonation fails
  await assertFails(
    aliceDb.collection("products_catalog").doc("3017620422003").collection("price_history").doc("ph_bad_price").set({
      barcode: "3017620422003",
      storeName: "FreshMart",
      price: 0,
      currencyCode: "USD",
      countryCode: "US",
      sourceProvider: "Community Report",
      verified: false,
      recordedBy: ALICE_UID,
      recordedAt: now,
    })
  );
  await assertFails(
    aliceDb.collection("products_catalog").doc("3017620422003").collection("price_history").doc("ph_bad_user").set({
      barcode: "3017620422003",
      storeName: "FreshMart",
      price: 3.99,
      currencyCode: "USD",
      countryCode: "US",
      sourceProvider: "Community Report",
      verified: true,
      recordedBy: BOB_UID,
      recordedAt: now,
    })
  );
});


