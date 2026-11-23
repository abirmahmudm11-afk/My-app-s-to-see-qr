package com.example.data.repository

import com.example.data.local.LocalScanDao
import com.example.data.local.LocalScanEntity
import com.example.data.model.AppNotification
import com.example.data.model.PayoutMethod
import com.example.data.model.PayoutRequest
import com.example.data.model.PayoutStatus
import com.example.data.model.RewardTransaction
import com.example.data.model.RewardWallet
import com.example.data.model.ScanRecord
import com.example.data.model.TransactionStatus
import com.example.data.model.TransactionType
import com.example.data.model.UserProfile
import com.example.domain.MarketConfig
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class UserWalletRepository(
  private val db: FirebaseFirestore,
  private val auth: FirebaseAuth,
  private val localScanDao: LocalScanDao,
) {
  fun observeLocalScans(): Flow<List<ScanRecord>> =
    localScanDao.observeRecent().map { list -> list.map { it.toScanRecord() } }

  suspend fun ensureAccountInitialized(
    userId: String,
    displayName: String,
    countryCode: String,
    languageCode: String,
  ) {
    val uid = auth.currentUser?.uid ?: return
    if (uid != userId) return
    val market = MarketConfig.findByCountry(countryCode)
    val userRef = db.collection("users").document(uid)
    val walletRef = userRef.collection("wallet").document("summary")
    try {
      if (!userRef.get().await().exists()) {
        userRef.set(
          hashMapOf(
            "userId" to uid,
            "displayName" to displayName.trim().ifEmpty { "Shopper" }.take(80),
            "preferredLanguage" to languageCode.lowercase().ifEmpty { market.defaultLanguage }.take(10),
            "countryCode" to market.countryCode,
            "currencyCode" to market.currencyCode,
            "accountStatus" to "ACTIVE",
            "marketingConsent" to false,
            "darkThemeEnabled" to false,
            "reducedMotionEnabled" to false,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp(),
          ),
        ).await()
      }
      if (!walletRef.get().await().exists()) {
        walletRef.set(
          hashMapOf(
            "userId" to uid,
            "unitCode" to "PTS",
            "availablePoints" to 0L,
            "pendingPoints" to 0L,
            "redeemedPoints" to 0L,
            "reversedPoints" to 0L,
            "updatedAt" to FieldValue.serverTimestamp(),
          ),
        ).await()
      }
    } catch (e: Exception) {
      FirestoreErrorHandler.handleFirestoreError(e, OperationType.WRITE, "users/$uid", auth)
    }
  }

  fun observeUserProfile(userId: String): Flow<UserProfile?> = callbackFlow {
    val reg = db.collection("users").document(userId).addSnapshotListener { snap, error ->
      if (error != null) {
        if (auth.currentUser?.uid == userId) {
          close(error)
        }
        return@addSnapshotListener
      }
      trySend(if (snap != null && snap.exists()) snap.toObject(UserProfile::class.java)?.copy(id = snap.id) else null)
    }
    awaitClose { reg.remove() }
  }

  fun observeWallet(userId: String): Flow<RewardWallet> = callbackFlow {
    val reg = db.collection("users").document(userId).collection("wallet").document("summary")
      .addSnapshotListener { snap, error ->
        if (error != null) {
          if (auth.currentUser?.uid == userId) close(error)
          return@addSnapshotListener
        }
        val wallet = if (snap != null && snap.exists()) {
          snap.toObject(RewardWallet::class.java)?.copy(id = snap.id) ?: RewardWallet(userId = userId)
        } else {
          RewardWallet(userId = userId)
        }
        trySend(wallet)
      }
    awaitClose { reg.remove() }
  }

  fun observeTransactions(userId: String): Flow<List<RewardTransaction>> = callbackFlow {
    val reg = db.collection("users").document(userId).collection("transactions")
      .addSnapshotListener { snap, error ->
        if (error != null) {
          if (auth.currentUser?.uid == userId) close(error)
          return@addSnapshotListener
        }
        val list = snap?.documents?.mapNotNull { doc ->
          doc.toObject(RewardTransaction::class.java)?.copy(id = doc.id)
        }?.sortedByDescending { it.createdAt?.seconds ?: 0L } ?: emptyList()
        trySend(list)
      }
    awaitClose { reg.remove() }
  }

  fun observeCloudScans(userId: String): Flow<List<ScanRecord>> = callbackFlow {
    val reg = db.collection("users").document(userId).collection("scans")
      .addSnapshotListener { snap, error ->
        if (error != null) {
          if (auth.currentUser?.uid == userId) close(error)
          return@addSnapshotListener
        }
        val list = snap?.documents?.mapNotNull { doc ->
          doc.toObject(ScanRecord::class.java)?.copy(id = doc.id)
        }?.sortedByDescending { it.scannedAt?.seconds ?: 0L } ?: emptyList()
        trySend(list)
      }
    awaitClose { reg.remove() }
  }

  fun observeNotifications(userId: String): Flow<List<AppNotification>> = callbackFlow {
    val reg = db.collection("users").document(userId).collection("notifications")
      .addSnapshotListener { snap, error ->
        if (error != null) {
          if (auth.currentUser?.uid == userId) close(error)
          return@addSnapshotListener
        }
        val list = snap?.documents?.mapNotNull { doc ->
          doc.toObject(AppNotification::class.java)?.copy(id = doc.id)
        }?.sortedByDescending { it.createdAt?.seconds ?: 0L } ?: emptyList()
        trySend(list)
      }
    awaitClose { reg.remove() }
  }

  suspend fun recordScan(
    barcodeValue: String,
    barcodeFormat: String,
    productName: String,
    brand: String,
    sourceProvider: String,
    verificationStatus: String,
    scanResult: String,
  ) {
    val uid = auth.currentUser?.uid
    localScanDao.insert(
      LocalScanEntity(
        userId = uid ?: "local",
        barcodeValue = barcodeValue.take(128),
        barcodeFormat = barcodeFormat.take(32),
        productName = productName.take(160),
        brand = brand.take(100),
        sourceProvider = sourceProvider.ifEmpty { "None" }.take(80),
        verificationStatus = verificationStatus,
        scanResult = scanResult,
        scannedAtMs = System.currentTimeMillis(),
      ),
    )

    if (uid != null) {
      val path = "users/$uid/scans"
      try {
        db.collection("users").document(uid).collection("scans").document().set(
          hashMapOf(
            "userId" to uid,
            "barcodeValue" to barcodeValue.take(128),
            "barcodeFormat" to barcodeFormat.take(32),
            "productName" to productName.take(160),
            "brand" to brand.take(100),
            "sourceProvider" to sourceProvider.ifEmpty { "None" }.take(80),
            "verificationStatus" to verificationStatus,
            "scanResult" to scanResult,
            "scannedAt" to FieldValue.serverTimestamp(),
          ),
        ).await()
      } catch (e: Exception) {
        FirestoreErrorHandler.handleFirestoreError(e, OperationType.CREATE, path, auth)
      }
    }
  }

  suspend fun updatePreferences(
    countryCode: String? = null,
    languageCode: String? = null,
    currencyCode: String? = null,
    marketingConsent: Boolean? = null,
    darkThemeEnabled: Boolean? = null,
    reducedMotionEnabled: Boolean? = null,
  ) {
    val uid = auth.currentUser?.uid ?: return
    val updates = hashMapOf<String, Any>("updatedAt" to FieldValue.serverTimestamp())
    if (countryCode != null) updates["countryCode"] = countryCode.uppercase().take(2)
    if (languageCode != null) updates["preferredLanguage"] = languageCode.lowercase().take(10)
    if (currencyCode != null) updates["currencyCode"] = currencyCode.uppercase().take(3)
    if (marketingConsent != null) updates["marketingConsent"] = marketingConsent
    if (darkThemeEnabled != null) updates["darkThemeEnabled"] = darkThemeEnabled
    if (reducedMotionEnabled != null) updates["reducedMotionEnabled"] = reducedMotionEnabled

    try {
      db.collection("users").document(uid).update(updates).await()
    } catch (e: Exception) {
      FirestoreErrorHandler.handleFirestoreError(e, OperationType.UPDATE, "users/$uid", auth)
    }
  }

  fun observeUserPayoutRequests(userId: String): Flow<List<PayoutRequest>> = callbackFlow {
    val reg = db.collection("payout_requests")
      .whereEqualTo("userId", userId)
      .addSnapshotListener { snap, error ->
        if (error != null) {
          if (auth.currentUser?.uid == userId) close(error)
          return@addSnapshotListener
        }
        val list = snap?.documents?.mapNotNull { doc ->
          doc.toObject(PayoutRequest::class.java)?.copy(id = doc.id)
        }?.sortedByDescending { it.requestedAt?.seconds ?: 0L } ?: emptyList()
        trySend(list)
      }
    awaitClose { reg.remove() }
  }

  suspend fun submitPayoutRequest(
    currentWallet: RewardWallet,
    amountPoints: Long,
    rewardTitle: String,
    payoutMethod: PayoutMethod,
    destinationHandle: String,
    userDisplayName: String,
  ) {
    val uid = auth.currentUser?.uid ?: throw IllegalStateException("Sign in first to request a payout.")
    val cleanTitle = rewardTitle.trim().ifEmpty { payoutMethod.label }.take(120)
    val cleanHandle = destinationHandle.trim().take(140)
    val cleanName = userDisplayName.trim().ifEmpty { "Shopper" }.take(80)
    require(amountPoints >= 100L) { "Minimum payout threshold is 100 PTS." }
    require(cleanHandle.length >= 2) { "Enter a valid destination email, tag, or voucher handle." }
    require(currentWallet.availablePoints >= amountPoints) {
      "Insufficient available points (${currentWallet.availablePoints} PTS)."
    }

    val path = "payout_requests"
    try {
      val userRef = db.collection("users").document(uid)
      val walletRef = userRef.collection("wallet").document("summary")
      val txRef = userRef.collection("transactions").document()
      val payoutRef = db.collection("payout_requests").document()
      val batch = db.batch()
      batch.update(
        walletRef,
        mapOf(
          "availablePoints" to (currentWallet.availablePoints - amountPoints),
          "redeemedPoints" to (currentWallet.redeemedPoints + amountPoints),
          "updatedAt" to FieldValue.serverTimestamp(),
        ),
      )
      batch.set(
        txRef,
        hashMapOf(
          "userId" to uid,
          "transactionType" to TransactionType.REDEMPTION.name,
          "amount" to amountPoints,
          "unitCode" to "PTS",
          "sourceActivity" to "${payoutMethod.label}: $cleanTitle".take(120),
          "status" to TransactionStatus.REDEEMED.name,
          "idempotencyKey" to "pay_${UUID.randomUUID().toString().take(18)}",
          "auditReference" to "PAY-${payoutRef.id.take(8).uppercase()}",
          "notes" to "Destination: $cleanHandle".take(300),
          "createdAt" to FieldValue.serverTimestamp(),
        ),
      )
      batch.set(
        payoutRef,
        hashMapOf(
          "userId" to uid,
          "userDisplayName" to cleanName,
          "rewardTitle" to cleanTitle,
          "pointsAmount" to amountPoints,
          "payoutMethod" to payoutMethod.name,
          "destinationHandle" to cleanHandle,
          "status" to PayoutStatus.PENDING.name,
          "requestedAt" to FieldValue.serverTimestamp(),
          "updatedAt" to FieldValue.serverTimestamp(),
        ),
      )
      batch.commit().await()
    } catch (e: Exception) {
      FirestoreErrorHandler.handleFirestoreError(e, OperationType.WRITE, path, auth)
    }
  }

  suspend fun redeemSandboxVoucher(
    currentWallet: RewardWallet,
    amountPoints: Long,
    rewardTitle: String,
  ) {
    submitPayoutRequest(
      currentWallet = currentWallet,
      amountPoints = amountPoints,
      rewardTitle = rewardTitle,
      payoutMethod = PayoutMethod.GIFT_VOUCHER,
      destinationHandle = auth.currentUser?.email ?: "In-App Sandbox Voucher",
      userDisplayName = auth.currentUser?.displayName ?: "Shopper",
    )
  }

  suspend fun markNotificationRead(notificationId: String) {
    val uid = auth.currentUser?.uid ?: return
    val path = "users/$uid/notifications/$notificationId"
    try {
      db.collection("users").document(uid).collection("notifications").document(notificationId)
        .update("read", true).await()
    } catch (e: Exception) {
      FirestoreErrorHandler.handleFirestoreError(e, OperationType.UPDATE, path, auth)
    }
  }

  suspend fun requestAccountDeletion(reason: String) {
    val uid = auth.currentUser?.uid ?: throw IllegalStateException("Sign in with Google first.")
    val path = "deletion_requests"
    try {
      db.collection("deletion_requests").document().set(
        hashMapOf(
          "userId" to uid,
          "reason" to reason.trim().ifEmpty { "User requested deletion" }.take(300),
          "status" to "REQUESTED",
          "requestedAt" to FieldValue.serverTimestamp(),
        ),
      ).await()
      db.collection("users").document(uid).update(
        mapOf(
          "accountStatus" to "DELETION_REQUESTED",
          "updatedAt" to FieldValue.serverTimestamp(),
        ),
      ).await()
    } catch (e: Exception) {
      FirestoreErrorHandler.handleFirestoreError(e, OperationType.CREATE, path, auth)
    }
  }
}
