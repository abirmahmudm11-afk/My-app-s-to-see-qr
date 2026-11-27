package com.example.data.repository

import com.example.data.model.EarningsWallet
import com.example.data.model.RewardedAdCompletion
import com.example.data.model.WithdrawalRequest
import com.example.data.model.WithdrawalStatus
import com.example.domain.EarningsEngine
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class EarningsRepository(
  private val db: FirebaseFirestore,
  private val auth: FirebaseAuth,
) {
  suspend fun ensureEarningsInitialized(userId: String) {
    val uid = auth.currentUser?.uid ?: return
    if (uid != userId) return
    val summaryRef = db.collection("users").document(uid).collection("earnings").document("summary")
    try {
      if (!summaryRef.get().await().exists()) {
        summaryRef.set(
          hashMapOf(
            "userId" to uid,
            "currencyCode" to "USD",
            "availableMicros" to 0L,
            "lifetimeEarnedMicros" to 0L,
            "pendingWithdrawalMicros" to 0L,
            "withdrawnMicros" to 0L,
            "adCompletionCount" to 0L,
            "updatedAt" to FieldValue.serverTimestamp(),
          ),
        ).await()
      }
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      FirestoreErrorHandler.handleFirestoreError(e, OperationType.WRITE, "users/$uid/earnings/summary", auth)
    }
  }

  fun observeEarningsWallet(userId: String): Flow<EarningsWallet> = callbackFlow {
    val reg = db.collection("users").document(userId).collection("earnings").document("summary")
      .addSnapshotListener { snap, error ->
        if (error != null) {
          if (auth.currentUser?.uid == userId) close(error)
          return@addSnapshotListener
        }
        val wallet = if (snap != null && snap.exists()) {
          snap.toObject(EarningsWallet::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            ?.copy(id = snap.id)
            ?: EarningsWallet(userId = userId)
        } else {
          EarningsWallet(userId = userId)
        }
        trySend(wallet)
      }
    awaitClose { reg.remove() }
  }

  fun observeRewardedAdCompletions(userId: String): Flow<List<RewardedAdCompletion>> = callbackFlow {
    val reg = db.collection("users").document(userId).collection("ad_rewards")
      .addSnapshotListener { snap, error ->
        if (error != null) {
          if (auth.currentUser?.uid == userId) close(error)
          return@addSnapshotListener
        }
        val list = snap?.documents?.mapNotNull { doc ->
          doc.toObject(RewardedAdCompletion::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            ?.copy(id = doc.id)
        }?.sortedByDescending { it.createdAt?.seconds ?: 0L } ?: emptyList()
        trySend(list)
      }
    awaitClose { reg.remove() }
  }

  fun observeUserWithdrawals(userId: String): Flow<List<WithdrawalRequest>> = callbackFlow {
    val reg = db.collection("withdrawal_requests")
      .whereEqualTo("userId", userId)
      .addSnapshotListener { snap, error ->
        if (error != null) {
          if (auth.currentUser?.uid == userId) close(error)
          return@addSnapshotListener
        }
        val list = snap?.documents?.mapNotNull { doc ->
          doc.toObject(WithdrawalRequest::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            ?.copy(id = doc.id)
        }?.sortedByDescending { it.requestedAt?.seconds ?: 0L } ?: emptyList()
        trySend(list)
      }
    awaitClose { reg.remove() }
  }

  suspend fun recordRewardedAdCompletion(
    adUnitId: String,
    triggerSource: String,
    scannedCode: String,
    adValueMicros: Long,
    currencyCode: String,
    precisionType: Int,
    rewardType: String,
    rewardAmount: Int,
  ): Result<RewardedAdCompletion> {
    val uid = auth.currentUser?.uid
      ?: return Result.failure(IllegalStateException("Sign in with Google to save ad earnings to your balance."))
    val safeAdValue = adValueMicros.coerceIn(0L, 10_000_000L)
    val userShareMicros = EarningsEngine.calculateTenPercentShareMicros(safeAdValue)
    val safeTrigger = if (triggerSource in setOf("QR_SCAN", "BARCODE_SCAN", "EARNINGS_BONUS")) {
      triggerSource
    } else {
      "QR_SCAN"
    }
    val safeCurrency = currencyCode.trim().uppercase().take(6).ifEmpty { "USD" }
    val safeUnitId = adUnitId.trim().take(128).ifEmpty { "ca-app-pub-5802460072817241/1598376798" }
    val safeScannedCode = scannedCode.trim().take(128)
    val safeRewardType = rewardType.trim().take(64)
    val safeRewardAmount = rewardAmount.coerceIn(0, 100_000)
    val safePrecision = precisionType.coerceIn(0, 3)

    val userRef = db.collection("users").document(uid)
    val summaryRef = userRef.collection("earnings").document("summary")
    val rewardRef = userRef.collection("ad_rewards").document()
    val path = "users/$uid/ad_rewards/${rewardRef.id}"

    return try {
      val snap = summaryRef.get().await()
      if (!snap.exists()) {
        summaryRef.set(
          hashMapOf(
            "userId" to uid,
            "currencyCode" to "USD",
            "availableMicros" to 0L,
            "lifetimeEarnedMicros" to 0L,
            "pendingWithdrawalMicros" to 0L,
            "withdrawnMicros" to 0L,
            "adCompletionCount" to 0L,
            "updatedAt" to FieldValue.serverTimestamp(),
          ),
        ).await()
      }
      val current = if (snap.exists()) {
        snap.toObject(EarningsWallet::class.java) ?: EarningsWallet(userId = uid)
      } else {
        EarningsWallet(userId = uid)
      }

      val batch = db.batch()
      batch.update(
        summaryRef,
        mapOf(
          "availableMicros" to (current.availableMicros + userShareMicros),
          "lifetimeEarnedMicros" to (current.lifetimeEarnedMicros + userShareMicros),
          "adCompletionCount" to (current.adCompletionCount + 1L),
          "updatedAt" to FieldValue.serverTimestamp(),
        ),
      )
      batch.set(
        rewardRef,
        hashMapOf(
          "userId" to uid,
          "adUnitId" to safeUnitId,
          "triggerSource" to safeTrigger,
          "scannedCode" to safeScannedCode,
          "adValueMicros" to safeAdValue,
          "userShareMicros" to userShareMicros,
          "currencyCode" to safeCurrency,
          "precisionType" to safePrecision,
          "rewardType" to safeRewardType,
          "rewardAmount" to safeRewardAmount,
          "createdAt" to FieldValue.serverTimestamp(),
        ),
      )
      batch.commit().await()

      Result.success(
        RewardedAdCompletion(
          id = rewardRef.id,
          userId = uid,
          adUnitId = safeAdValue.let { safeUnitId },
          triggerSource = safeTrigger,
          scannedCode = safeScannedCode,
          adValueMicros = safeAdValue,
          userShareMicros = userShareMicros,
          currencyCode = safeCurrency,
          precisionType = safePrecision,
          rewardType = safeRewardType,
          rewardAmount = safeRewardAmount,
        ),
      )
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      FirestoreErrorHandler.handleFirestoreError(e, OperationType.WRITE, path, auth)
      Result.failure(e)
    }
  }

  suspend fun submitUsdtWithdrawal(
    walletAddress: String,
    amountMicros: Long,
    userDisplayName: String,
  ): Result<WithdrawalRequest> {
    val uid = auth.currentUser?.uid
      ?: return Result.failure(IllegalStateException("Sign in with Google first to request a USDT withdrawal."))
    val cleanAddress = walletAddress.trim()
    if (!EarningsEngine.isValidBep20Address(cleanAddress)) {
      return Result.failure(IllegalArgumentException("Enter a valid Binance USDT BEP-20 wallet address (0x + 40 hex chars)."))
    }
    if (amountMicros < EarningsEngine.MIN_PAYOUT_MICROS) {
      return Result.failure(IllegalArgumentException("Minimum payout is $1.00 USD."))
    }

    val cleanName = userDisplayName.trim().ifEmpty { "Shopper" }.take(80)
    val summaryRef = db.collection("users").document(uid).collection("earnings").document("summary")
    val withdrawalRef = db.collection("withdrawal_requests").document()
    val path = "withdrawal_requests/${withdrawalRef.id}"

    return try {
      val snap = summaryRef.get().await()
      val current = if (snap.exists()) {
        snap.toObject(EarningsWallet::class.java) ?: EarningsWallet(userId = uid)
      } else {
        EarningsWallet(userId = uid)
      }
      if (current.availableMicros < EarningsEngine.MIN_PAYOUT_MICROS) {
        return Result.failure(
          IllegalStateException(
            "Balance must be at least $1.00 USD before withdrawing (${EarningsEngine.formatUsdFromMicros(current.availableMicros)} available).",
          ),
        )
      }
      if (amountMicros > current.availableMicros) {
        return Result.failure(
          IllegalStateException(
            "Insufficient available balance (${EarningsEngine.formatUsdFromMicros(current.availableMicros)} available).",
          ),
        )
      }

      val batch = db.batch()
      batch.update(
        summaryRef,
        mapOf(
          "availableMicros" to (current.availableMicros - amountMicros),
          "pendingWithdrawalMicros" to (current.pendingWithdrawalMicros + amountMicros),
          "updatedAt" to FieldValue.serverTimestamp(),
        ),
      )
      batch.set(
        withdrawalRef,
        hashMapOf(
          "userId" to uid,
          "userDisplayName" to cleanName,
          "network" to EarningsEngine.NETWORK_BEP20,
          "asset" to EarningsEngine.ASSET_USDT,
          "walletAddress" to cleanAddress,
          "amountMicros" to amountMicros,
          "status" to WithdrawalStatus.PENDING.name,
          "requestedAt" to FieldValue.serverTimestamp(),
          "updatedAt" to FieldValue.serverTimestamp(),
        ),
      )
      batch.commit().await()

      Result.success(
        WithdrawalRequest(
          id = withdrawalRef.id,
          userId = uid,
          userDisplayName = cleanName,
          network = EarningsEngine.NETWORK_BEP20,
          asset = EarningsEngine.ASSET_USDT,
          walletAddress = cleanAddress,
          amountMicros = amountMicros,
          status = WithdrawalStatus.PENDING.name,
        ),
      )
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      FirestoreErrorHandler.handleFirestoreError(e, OperationType.WRITE, path, auth)
      Result.failure(e)
    }
  }
}
