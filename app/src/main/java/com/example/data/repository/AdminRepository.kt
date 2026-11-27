package com.example.data.repository

import com.example.data.model.AdminRole
import com.example.data.model.AdminRoleName
import com.example.data.model.AuditLog
import com.example.data.model.Campaign
import com.example.data.model.CampaignStatus
import com.example.data.model.CatalogProduct
import com.example.data.model.DeletionRequest
import com.example.data.model.EarningsWallet
import com.example.data.model.PayoutRequest
import com.example.data.model.PayoutStatus
import com.example.data.model.ReceiptReviewStatus
import com.example.data.model.ReceiptSubmission
import com.example.data.model.RewardTransaction
import com.example.data.model.RewardWallet
import com.example.data.model.TransactionStatus
import com.example.data.model.TransactionType
import com.example.data.model.UserProfile
import com.example.data.model.VerificationStatus
import com.example.data.model.WithdrawalRequest
import com.example.data.model.WithdrawalStatus
import com.example.domain.EarningsEngine
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date
import java.util.UUID

class AdminRepository(
  private val db: FirebaseFirestore,
  private val auth: FirebaseAuth,
) {
  private val adminEmails = setOf(
    "abirmahmudm11@gmail.com",
    "altabmahmud172@gmail.com",
    "m28044963@gmail.com",
  )

  suspend fun resolveAdminRole(): AdminRole? {
    val user = auth.currentUser ?: return null
    val email = user.email?.trim()?.lowercase().orEmpty()
    if (user.isEmailVerified && email in adminEmails) {
      return AdminRole(
        id = user.uid,
        userId = user.uid,
        roleName = AdminRoleName.SUPER_ADMIN.name,
        permissions = listOf("USER_MANAGE", "PAYOUT_REVIEW", "RECEIPT_REVIEW", "CAMPAIGN_MANAGE", "CATALOG_VERIFY", "LEDGER_REVERSE"),
      )
    }
    return try {
      val snap = db.collection("admins").document(user.uid).get().await()
      if (snap.exists()) snap.toObject(AdminRole::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)?.copy(id = snap.id) else null
    } catch (_: Exception) {
      null
    }
  }

  fun observeAllUsers(): Flow<List<UserProfile>> = callbackFlow {
    val reg = db.collection("users").addSnapshotListener { snap, error ->
      if (error != null) {
        android.util.Log.w("AdminRepository", "observeAllUsers error: ${error.message}")
        return@addSnapshotListener
      }
      val items = snap?.documents?.mapNotNull { it.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)?.copy(id = it.id) }
        ?.sortedByDescending { it.createdAt?.seconds ?: 0L } ?: emptyList()
      trySend(items)
    }
    awaitClose { reg.remove() }
  }

  fun observeAllPayoutRequests(): Flow<List<PayoutRequest>> = callbackFlow {
    val reg = db.collection("payout_requests").addSnapshotListener { snap, error ->
      if (error != null) return@addSnapshotListener
      val items = snap?.documents?.mapNotNull { it.toObject(PayoutRequest::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)?.copy(id = it.id) }
        ?.sortedByDescending { it.requestedAt?.seconds ?: 0L } ?: emptyList()
      trySend(items)
    }
    awaitClose { reg.remove() }
  }

  fun observeAllWithdrawalRequests(): Flow<List<WithdrawalRequest>> = callbackFlow {
    val reg = db.collection("withdrawal_requests").addSnapshotListener { snap, error ->
      if (error != null) return@addSnapshotListener
      val items = snap?.documents?.mapNotNull { it.toObject(WithdrawalRequest::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)?.copy(id = it.id) }
        ?.sortedByDescending { it.requestedAt?.seconds ?: 0L } ?: emptyList()
      trySend(items)
    }
    awaitClose { reg.remove() }
  }

  fun observeAllReceipts(): Flow<List<ReceiptSubmission>> = callbackFlow {
    val reg = db.collection("receipts").addSnapshotListener { snap, error ->
      if (error != null) return@addSnapshotListener
      val items = snap?.documents?.mapNotNull { it.toObject(ReceiptSubmission::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)?.copy(id = it.id) }
        ?.sortedByDescending { it.submittedAt?.seconds ?: 0L } ?: emptyList()
      trySend(items)
    }
    awaitClose { reg.remove() }
  }

  fun observePendingReceipts(): Flow<List<ReceiptSubmission>> = callbackFlow {
    val reg = db.collection("receipts").whereEqualTo("reviewStatus", ReceiptReviewStatus.PENDING.name)
      .addSnapshotListener { snap, error ->
        if (error != null) return@addSnapshotListener
        val items = snap?.documents?.mapNotNull { it.toObject(ReceiptSubmission::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)?.copy(id = it.id) }
          ?.sortedByDescending { it.submittedAt?.seconds ?: 0L } ?: emptyList()
        trySend(items)
      }
    awaitClose { reg.remove() }
  }

  fun observeAllCampaigns(): Flow<List<Campaign>> = callbackFlow {
    val reg = db.collection("campaigns").addSnapshotListener { snap, error ->
      if (error != null) return@addSnapshotListener
      val items = snap?.documents?.mapNotNull { it.toObject(Campaign::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)?.copy(id = it.id) }
        ?.sortedByDescending { it.updatedAt?.seconds ?: 0L } ?: emptyList()
      trySend(items)
    }
    awaitClose { reg.remove() }
  }

  fun observeUnverifiedCatalog(): Flow<List<CatalogProduct>> = callbackFlow {
    val reg = db.collection("products_catalog")
      .whereEqualTo("verificationStatus", VerificationStatus.USER_SUBMITTED_UNVERIFIED.name)
      .addSnapshotListener { snap, error ->
        if (error != null) return@addSnapshotListener
        val items = snap?.documents?.mapNotNull { it.toObject(CatalogProduct::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)?.copy(id = it.id) }
          ?: emptyList()
        trySend(items)
      }
    awaitClose { reg.remove() }
  }

  fun observeDeletionRequests(): Flow<List<DeletionRequest>> = callbackFlow {
    val reg = db.collection("deletion_requests").addSnapshotListener { snap, error ->
      if (error != null) return@addSnapshotListener
      val items = snap?.documents?.mapNotNull { it.toObject(DeletionRequest::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)?.copy(id = it.id) }
        ?.sortedByDescending { it.requestedAt?.seconds ?: 0L } ?: emptyList()
      trySend(items)
    }
    awaitClose { reg.remove() }
  }

  fun observeAuditLogs(): Flow<List<AuditLog>> = callbackFlow {
    val reg = db.collection("audit_logs").addSnapshotListener { snap, error ->
      if (error != null) return@addSnapshotListener
      val items = snap?.documents?.mapNotNull { it.toObject(AuditLog::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)?.copy(id = it.id) }
        ?.sortedByDescending { it.timestamp?.seconds ?: 0L } ?: emptyList()
      trySend(items)
    }
    awaitClose { reg.remove() }
  }

  suspend fun fetchUserWalletSnapshot(userId: String): RewardWallet {
    return try {
      val snap = db.collection("users").document(userId).collection("wallet").document("summary").get().await()
      if (snap.exists()) snap.toObject(RewardWallet::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) ?: RewardWallet(userId = userId)
      else RewardWallet(userId = userId)
    } catch (_: Exception) {
      RewardWallet(userId = userId)
    }
  }

  suspend fun fetchUserTransactions(userId: String): List<RewardTransaction> {
    return try {
      db.collection("users").document(userId).collection("transactions")
        .get()
        .await()
        .documents
        .mapNotNull { doc ->
          doc.toObject(RewardTransaction::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)?.copy(id = doc.id)
        }
        .sortedByDescending { it.createdAt?.seconds ?: 0L }
    } catch (_: Exception) {
      emptyList()
    }
  }

  suspend fun reviewPayoutRequest(
    request: PayoutRequest,
    newStatus: PayoutStatus,
    adminNotes: String,
  ) {
    val adminUid = auth.currentUser?.uid ?: throw IllegalStateException("Admin sign-in required.")
    val cleanNotes = adminNotes.trim().ifEmpty {
      when (newStatus) {
        PayoutStatus.APPROVED -> "Approved for payout fulfillment"
        PayoutStatus.PAID -> "Marked fulfilled & paid in sandbox ledger"
        PayoutStatus.REJECTED -> "Payout declined & points refunded"
        PayoutStatus.PENDING -> "Pending review"
      }
    }.take(300)

    val payoutRef = db.collection("payout_requests").document(request.id)
    val userRef = db.collection("users").document(request.userId)
    val walletRef = userRef.collection("wallet").document("summary")
    val notifRef = userRef.collection("notifications").document()
    val auditRef = db.collection("audit_logs").document()

    try {
      val batch = db.batch()
      batch.update(
        payoutRef,
        mapOf(
          "status" to newStatus.name,
          "adminNotes" to cleanNotes,
          "reviewedBy" to adminUid,
          "updatedAt" to FieldValue.serverTimestamp(),
        ),
      )

      if (newStatus == PayoutStatus.REJECTED) {
        val walletSnap = walletRef.get().await()
        val wallet = walletSnap.toObject(RewardWallet::class.java) ?: RewardWallet(userId = request.userId)
        if (walletSnap.exists()) {
          batch.update(
            walletRef,
            mapOf(
              "availablePoints" to (wallet.availablePoints + request.pointsAmount),
              "redeemedPoints" to maxOf(0L, wallet.redeemedPoints - request.pointsAmount),
              "updatedAt" to FieldValue.serverTimestamp(),
            ),
          )
        } else {
          batch.set(
            walletRef,
            hashMapOf(
              "userId" to request.userId,
              "unitCode" to "PTS",
              "availablePoints" to request.pointsAmount,
              "pendingPoints" to 0L,
              "redeemedPoints" to 0L,
              "reversedPoints" to 0L,
              "updatedAt" to FieldValue.serverTimestamp(),
            ),
          )
        }
        batch.set(
          userRef.collection("transactions").document(),
          hashMapOf(
            "userId" to request.userId,
            "transactionType" to TransactionType.REVERSAL.name,
            "amount" to request.pointsAmount,
            "unitCode" to "PTS",
            "sourceActivity" to "Payout Refund: ${request.rewardTitle}".take(120),
            "status" to TransactionStatus.REVERSED.name,
            "idempotencyKey" to "payrev_${request.id}",
            "auditReference" to "PRV-${request.id.take(8).uppercase()}",
            "notes" to cleanNotes,
            "createdAt" to FieldValue.serverTimestamp(),
          ),
        )
      }

      batch.set(
        notifRef,
        hashMapOf(
          "userId" to request.userId,
          "category" to "REWARD_STATUS",
          "title" to "Payout ${newStatus.name}: ${request.rewardTitle}".take(120),
          "body" to "${request.pointsAmount} PTS • $cleanNotes".take(500),
          "read" to false,
          "referenceId" to request.id,
          "createdAt" to FieldValue.serverTimestamp(),
        ),
      )

      batch.set(
        auditRef,
        hashMapOf(
          "actorId" to adminUid,
          "action" to "PAYOUT_${newStatus.name}",
          "entityType" to "payout_request",
          "entityId" to request.id,
          "reason" to cleanNotes,
          "timestamp" to FieldValue.serverTimestamp(),
        ),
      )

      batch.commit().await()
    } catch (e: Exception) {
      FirestoreErrorHandler.handleFirestoreError(e, OperationType.WRITE, "payout_requests/${request.id}", auth)
    }
  }

  suspend fun reviewWithdrawalRequest(
    request: WithdrawalRequest,
    newStatus: WithdrawalStatus,
    adminNotes: String,
    txHash: String = "",
  ) {
    val adminUid = auth.currentUser?.uid ?: throw IllegalStateException("Admin sign-in required.")
    val cleanNotes = adminNotes.trim().ifEmpty {
      when (newStatus) {
        WithdrawalStatus.APPROVED -> "Approved for Binance USDT BEP-20 transfer"
        WithdrawalStatus.PAID -> "USDT (BEP-20) transfer completed"
        WithdrawalStatus.REJECTED -> "Withdrawal declined & USD balance refunded"
        WithdrawalStatus.PENDING -> "Pending review"
      }
    }.take(300)
    val cleanTxHash = txHash.trim().take(128)

    val withdrawalRef = db.collection("withdrawal_requests").document(request.id)
    val userRef = db.collection("users").document(request.userId)
    val earningsRef = userRef.collection("earnings").document("summary")
    val notifRef = userRef.collection("notifications").document()
    val auditRef = db.collection("audit_logs").document()

    try {
      val batch = db.batch()
      val updatePayload = hashMapOf<String, Any>(
        "status" to newStatus.name,
        "adminNotes" to cleanNotes,
        "reviewedBy" to adminUid,
        "updatedAt" to FieldValue.serverTimestamp(),
      )
      if (cleanTxHash.isNotEmpty()) {
        updatePayload["txHash"] = cleanTxHash
      }
      batch.update(withdrawalRef, updatePayload)

      if (newStatus == WithdrawalStatus.PAID || newStatus == WithdrawalStatus.REJECTED) {
        val snap = earningsRef.get().await()
        val current = snap.toObject(EarningsWallet::class.java) ?: EarningsWallet(userId = request.userId)
        val newPending = maxOf(0L, current.pendingWithdrawalMicros - request.amountMicros)
        if (snap.exists()) {
          val walletUpdates = if (newStatus == WithdrawalStatus.PAID) {
            mapOf(
              "pendingWithdrawalMicros" to newPending,
              "withdrawnMicros" to (current.withdrawnMicros + request.amountMicros),
              "updatedAt" to FieldValue.serverTimestamp(),
            )
          } else {
            mapOf(
              "availableMicros" to (current.availableMicros + request.amountMicros),
              "pendingWithdrawalMicros" to newPending,
              "updatedAt" to FieldValue.serverTimestamp(),
            )
          }
          batch.update(earningsRef, walletUpdates)
        } else {
          batch.set(
            earningsRef,
            hashMapOf(
              "userId" to request.userId,
              "currencyCode" to "USD",
              "availableMicros" to if (newStatus == WithdrawalStatus.REJECTED) request.amountMicros else 0L,
              "lifetimeEarnedMicros" to request.amountMicros,
              "pendingWithdrawalMicros" to 0L,
              "withdrawnMicros" to if (newStatus == WithdrawalStatus.PAID) request.amountMicros else 0L,
              "adCompletionCount" to 0L,
              "updatedAt" to FieldValue.serverTimestamp(),
            ),
          )
        }
      }

      val formattedAmount = EarningsEngine.formatUsdFromMicros(request.amountMicros)
      batch.set(
        notifRef,
        hashMapOf(
          "userId" to request.userId,
          "category" to "REWARD_STATUS",
          "title" to "USDT Withdrawal ${newStatus.name} ($formattedAmount)".take(120),
          "body" to "BEP-20 ${request.walletAddress.take(10)}... • $cleanNotes".take(500),
          "read" to false,
          "referenceId" to request.id,
          "createdAt" to FieldValue.serverTimestamp(),
        ),
      )
      batch.set(
        auditRef,
        hashMapOf(
          "actorId" to adminUid,
          "action" to "WITHDRAWAL_${newStatus.name}",
          "entityType" to "withdrawal_request",
          "entityId" to request.id,
          "reason" to "$formattedAmount USDT BEP-20: $cleanNotes".take(300),
          "timestamp" to FieldValue.serverTimestamp(),
        ),
      )
      batch.commit().await()
    } catch (e: Exception) {
      FirestoreErrorHandler.handleFirestoreError(e, OperationType.WRITE, "withdrawal_requests/${request.id}", auth)
    }
  }

  suspend fun updateUserAccountStatus(targetUserId: String, newStatus: String, reason: String) {
    val adminUid = auth.currentUser?.uid ?: throw IllegalStateException("Admin sign-in required.")
    val cleanReason = reason.trim().ifEmpty { "Status updated to $newStatus by admin" }.take(300)
    try {
      val batch = db.batch()
      batch.update(
        db.collection("users").document(targetUserId),
        mapOf(
          "accountStatus" to newStatus,
          "updatedAt" to FieldValue.serverTimestamp(),
        ),
      )
      batch.set(
        db.collection("users").document(targetUserId).collection("notifications").document(),
        hashMapOf(
          "userId" to targetUserId,
          "category" to "ACCOUNT",
          "title" to "Account Status: $newStatus",
          "body" to cleanReason,
          "read" to false,
          "referenceId" to targetUserId,
          "createdAt" to FieldValue.serverTimestamp(),
        ),
      )
      batch.set(
        db.collection("audit_logs").document(),
        hashMapOf(
          "actorId" to adminUid,
          "action" to "USER_STATUS_$newStatus",
          "entityType" to "user",
          "entityId" to targetUserId,
          "reason" to cleanReason,
          "timestamp" to FieldValue.serverTimestamp(),
        ),
      )
      batch.commit().await()
    } catch (e: Exception) {
      FirestoreErrorHandler.handleFirestoreError(e, OperationType.UPDATE, "users/$targetUserId", auth)
    }
  }

  suspend fun adminCreditBonusPoints(targetUserId: String, bonusPoints: Long, reason: String) {
    val adminUid = auth.currentUser?.uid ?: throw IllegalStateException("Admin sign-in required.")
    require(bonusPoints in 1..50_000) { "Bonus points must be between 1 and 50,000." }
    val cleanReason = reason.trim().ifEmpty { "Admin loyalty adjustment" }.take(300)

    val userRef = db.collection("users").document(targetUserId)
    val walletRef = userRef.collection("wallet").document("summary")
    try {
      val walletSnap = walletRef.get().await()
      val wallet = walletSnap.toObject(RewardWallet::class.java) ?: RewardWallet(userId = targetUserId)
      val batch = db.batch()
      if (walletSnap.exists()) {
        batch.update(
          walletRef,
          mapOf(
            "availablePoints" to (wallet.availablePoints + bonusPoints),
            "updatedAt" to FieldValue.serverTimestamp(),
          ),
        )
      } else {
        batch.set(
          walletRef,
          hashMapOf(
            "userId" to targetUserId,
            "unitCode" to "PTS",
            "availablePoints" to bonusPoints,
            "pendingPoints" to 0L,
            "redeemedPoints" to 0L,
            "reversedPoints" to 0L,
            "updatedAt" to FieldValue.serverTimestamp(),
          ),
        )
      }
      val txId = UUID.randomUUID().toString().take(12)
      batch.set(
        userRef.collection("transactions").document(),
        hashMapOf(
          "userId" to targetUserId,
          "transactionType" to TransactionType.CAMPAIGN_BONUS.name,
          "amount" to bonusPoints,
          "unitCode" to "PTS",
          "sourceActivity" to "Admin Credit: $cleanReason".take(120),
          "status" to TransactionStatus.CREDITED.name,
          "idempotencyKey" to "adm_$txId",
          "auditReference" to "ADM-${txId.uppercase()}",
          "notes" to cleanReason,
          "createdAt" to FieldValue.serverTimestamp(),
        ),
      )
      batch.set(
        userRef.collection("notifications").document(),
        hashMapOf(
          "userId" to targetUserId,
          "category" to "REWARD_STATUS",
          "title" to "Bonus Credited (+$bonusPoints PTS)",
          "body" to cleanReason,
          "read" to false,
          "referenceId" to targetUserId,
          "createdAt" to FieldValue.serverTimestamp(),
        ),
      )
      batch.set(
        db.collection("audit_logs").document(),
        hashMapOf(
          "actorId" to adminUid,
          "action" to "ADMIN_CREDIT_POINTS",
          "entityType" to "wallet",
          "entityId" to targetUserId,
          "reason" to "+$bonusPoints PTS: $cleanReason".take(300),
          "timestamp" to FieldValue.serverTimestamp(),
        ),
      )
      batch.commit().await()
    } catch (e: Exception) {
      FirestoreErrorHandler.handleFirestoreError(e, OperationType.WRITE, "users/$targetUserId/wallet", auth)
    }
  }

  suspend fun approveReceiptAndCredit(receipt: ReceiptSubmission, rewardPoints: Long) {
    val adminUid = auth.currentUser?.uid ?: throw IllegalStateException("Admin sign-in required.")
    require(rewardPoints in 1..10_000) { "Reward points must be between 1 and 10,000." }

    val receiptRef = db.collection("receipts").document(receipt.id)
    val userRef = db.collection("users").document(receipt.userId)
    val walletRef = userRef.collection("wallet").document("summary")
    val txRef = userRef.collection("transactions").document()
    val notifRef = userRef.collection("notifications").document()
    val auditRef = db.collection("audit_logs").document()

    try {
      val walletSnap = walletRef.get().await()
      val wallet = walletSnap.toObject(RewardWallet::class.java) ?: RewardWallet(userId = receipt.userId)
      val batch = db.batch()
      batch.update(
        receiptRef,
        mapOf(
          "reviewStatus" to ReceiptReviewStatus.APPROVED.name,
          "reviewedBy" to adminUid,
          "updatedAt" to FieldValue.serverTimestamp(),
        ),
      )
      if (walletSnap.exists()) {
        batch.update(
          walletRef,
          mapOf(
            "availablePoints" to (wallet.availablePoints + rewardPoints),
            "updatedAt" to FieldValue.serverTimestamp(),
          ),
        )
      } else {
        batch.set(
          walletRef,
          hashMapOf(
            "userId" to receipt.userId,
            "unitCode" to "PTS",
            "availablePoints" to rewardPoints,
            "pendingPoints" to 0L,
            "redeemedPoints" to 0L,
            "reversedPoints" to 0L,
            "updatedAt" to FieldValue.serverTimestamp(),
          ),
        )
      }
      val txPayload = hashMapOf<String, Any>(
        "userId" to receipt.userId,
        "transactionType" to TransactionType.RECEIPT_REWARD.name,
        "amount" to rewardPoints,
        "unitCode" to "PTS",
        "sourceActivity" to "Approved Receipt: ${receipt.storeName}".take(120),
        "status" to TransactionStatus.CREDITED.name,
        "idempotencyKey" to "rcpt_${receipt.id}",
        "auditReference" to "AUD-${receipt.id.take(8).uppercase()}",
        "notes" to "Approved by reviewer",
        "createdAt" to FieldValue.serverTimestamp(),
      )
      if (!receipt.campaignId.isNullOrBlank()) {
        txPayload["campaignId"] = receipt.campaignId
      }
      batch.set(txRef, txPayload)
      batch.set(
        notifRef,
        hashMapOf(
          "userId" to receipt.userId,
          "category" to "RECEIPT_REVIEW",
          "title" to "Receipt Approved (+$rewardPoints PTS)",
          "body" to "Your ${receipt.storeName} receipt passed human review.",
          "read" to false,
          "referenceId" to receipt.id,
          "createdAt" to FieldValue.serverTimestamp(),
        ),
      )
      batch.set(
        auditRef,
        hashMapOf(
          "actorId" to adminUid,
          "action" to "APPROVE_RECEIPT",
          "entityType" to "receipt",
          "entityId" to receipt.id,
          "reason" to "Approved +$rewardPoints sandbox PTS",
          "timestamp" to FieldValue.serverTimestamp(),
        ),
      )
      batch.commit().await()
    } catch (e: Exception) {
      FirestoreErrorHandler.handleFirestoreError(e, OperationType.WRITE, "receipts/${receipt.id}", auth)
    }
  }

  suspend fun rejectReceipt(receipt: ReceiptSubmission, reason: String) {
    val adminUid = auth.currentUser?.uid ?: throw IllegalStateException("Admin sign-in required.")
    val cleanReason = reason.trim().ifEmpty { "Receipt did not match eligibility criteria" }.take(300)
    try {
      val batch = db.batch()
      batch.update(
        db.collection("receipts").document(receipt.id),
        mapOf(
          "reviewStatus" to ReceiptReviewStatus.REJECTED.name,
          "rejectionReason" to cleanReason,
          "reviewedBy" to adminUid,
          "updatedAt" to FieldValue.serverTimestamp(),
        ),
      )
      batch.set(
        db.collection("users").document(receipt.userId).collection("notifications").document(),
        hashMapOf(
          "userId" to receipt.userId,
          "category" to "RECEIPT_REVIEW",
          "title" to "Receipt Rejected",
          "body" to "${receipt.storeName}: $cleanReason".take(500),
          "read" to false,
          "referenceId" to receipt.id,
          "createdAt" to FieldValue.serverTimestamp(),
        ),
      )
      batch.set(
        db.collection("audit_logs").document(),
        hashMapOf(
          "actorId" to adminUid,
          "action" to "REJECT_RECEIPT",
          "entityType" to "receipt",
          "entityId" to receipt.id,
          "reason" to cleanReason,
          "timestamp" to FieldValue.serverTimestamp(),
        ),
      )
      batch.commit().await()
    } catch (e: Exception) {
      FirestoreErrorHandler.handleFirestoreError(e, OperationType.UPDATE, "receipts/${receipt.id}", auth)
    }
  }

  suspend fun createCampaign(
    partnerName: String,
    title: String,
    terms: String,
    rewardPoints: Long,
    countries: List<String>,
  ) {
    val adminUid = auth.currentUser?.uid ?: throw IllegalStateException("Admin sign-in required.")
    val now = Timestamp.now()
    val end = Timestamp(Date(System.currentTimeMillis() + 30L * 86_400_000L))
    try {
      val ref = db.collection("campaigns").document()
      val batch = db.batch()
      batch.set(
        ref,
        hashMapOf(
          "partnerId" to adminUid,
          "partnerName" to partnerName.trim().take(100),
          "title" to title.trim().take(120),
          "terms" to terms.trim().take(1000),
          "rewardType" to "SANDBOX_POINTS",
          "rewardPoints" to rewardPoints.coerceIn(1L, 10_000L),
          "maxPerUser" to 3,
          "budgetLimitPoints" to 50_000L,
          "eligibleCountries" to countries.ifEmpty { listOf("US") },
          "eligibleBarcodes" to emptyList<String>(),
          "eligibleCategories" to emptyList<String>(),
          "status" to "ACTIVE",
          "startAt" to now,
          "endAt" to end,
          "createdAt" to FieldValue.serverTimestamp(),
          "updatedAt" to FieldValue.serverTimestamp(),
        ),
      )
      batch.set(
        db.collection("audit_logs").document(),
        hashMapOf(
          "actorId" to adminUid,
          "action" to "CREATE_CAMPAIGN",
          "entityType" to "campaign",
          "entityId" to ref.id,
          "reason" to "Launched ${title.trim()}".take(300),
          "timestamp" to FieldValue.serverTimestamp(),
        ),
      )
      batch.commit().await()
    } catch (e: Exception) {
      FirestoreErrorHandler.handleFirestoreError(e, OperationType.CREATE, "campaigns", auth)
    }
  }

  suspend fun seedStarterCampaigns() {
    val adminUid = auth.currentUser?.uid ?: throw IllegalStateException("Admin sign-in required.")
    val now = Timestamp.now()
    val end = Timestamp(Date(System.currentTimeMillis() + 60L * 86_400_000L))
    val starters = listOf(
      Triple("Ferrero Retail", "Breakfast Favorites Receipt Bonus", "Upload any grocery receipt containing hazelnut spreads or breakfast staples.") to 150L,
      Triple("Beverage Collective", "Sparkling & Hydration Club", "Scan and submit any qualifying 6-pack or single bottle beverage receipt.") to 120L,
      Triple("Organic Pantry Co.", "Whole Grain & Eco-Score A Bonus", "Earn bonus PTS on certified organic pantry purchases with Nutri-Score A or B.") to 200L,
    )
    try {
      val batch = db.batch()
      starters.forEach { (meta, pts) ->
        val (partner, title, terms) = meta
        val ref = db.collection("campaigns").document()
        batch.set(
          ref,
          hashMapOf(
            "partnerId" to adminUid,
            "partnerName" to partner,
            "title" to title,
            "terms" to terms,
            "rewardType" to "SANDBOX_POINTS",
            "rewardPoints" to pts,
            "maxPerUser" to 3,
            "budgetLimitPoints" to 100_000L,
            "eligibleCountries" to listOf("US", "GB", "CA", "DE", "FR", "AU", "BD", "GLOBAL"),
            "eligibleBarcodes" to emptyList<String>(),
            "eligibleCategories" to emptyList<String>(),
            "status" to "ACTIVE",
            "startAt" to now,
            "endAt" to end,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp(),
          ),
        )
      }
      batch.set(
        db.collection("audit_logs").document(),
        hashMapOf(
          "actorId" to adminUid,
          "action" to "SEED_STARTER_CAMPAIGNS",
          "entityType" to "campaign",
          "entityId" to "starter_pack",
          "reason" to "Published 3 starter partner campaigns",
          "timestamp" to FieldValue.serverTimestamp(),
        ),
      )
      batch.commit().await()
    } catch (e: Exception) {
      FirestoreErrorHandler.handleFirestoreError(e, OperationType.CREATE, "campaigns", auth)
    }
  }

  suspend fun updateCampaignStatus(campaign: Campaign, newStatus: CampaignStatus) {
    val adminUid = auth.currentUser?.uid ?: throw IllegalStateException("Admin sign-in required.")
    try {
      val batch = db.batch()
      batch.update(
        db.collection("campaigns").document(campaign.id),
        mapOf(
          "status" to newStatus.name,
          "updatedAt" to FieldValue.serverTimestamp(),
        ),
      )
      batch.set(
        db.collection("audit_logs").document(),
        hashMapOf(
          "actorId" to adminUid,
          "action" to "CAMPAIGN_${newStatus.name}",
          "entityType" to "campaign",
          "entityId" to campaign.id,
          "reason" to "Campaign '${campaign.title}' set to ${newStatus.name}".take(300),
          "timestamp" to FieldValue.serverTimestamp(),
        ),
      )
      batch.commit().await()
    } catch (e: Exception) {
      FirestoreErrorHandler.handleFirestoreError(e, OperationType.UPDATE, "campaigns/${campaign.id}", auth)
    }
  }

  suspend fun verifyProductRecord(product: CatalogProduct) {
    val adminUid = auth.currentUser?.uid ?: throw IllegalStateException("Admin sign-in required.")
    try {
      val batch = db.batch()
      batch.update(
        db.collection("products_catalog").document(product.barcode),
        mapOf(
          "verificationStatus" to VerificationStatus.VERIFIED_MERCHANT.name,
          "attributionText" to "Verified by SCAN2MONEY catalog steward",
          "updatedAt" to FieldValue.serverTimestamp(),
        ),
      )
      batch.set(
        db.collection("audit_logs").document(),
        hashMapOf(
          "actorId" to adminUid,
          "action" to "VERIFY_PRODUCT",
          "entityType" to "product",
          "entityId" to product.barcode,
          "reason" to "Promoted community submission to VERIFIED_MERCHANT",
          "timestamp" to FieldValue.serverTimestamp(),
        ),
      )
      batch.commit().await()
    } catch (e: Exception) {
      FirestoreErrorHandler.handleFirestoreError(e, OperationType.UPDATE, "products_catalog/${product.barcode}", auth)
    }
  }

  suspend fun completeDeletionRequest(request: DeletionRequest) {
    val adminUid = auth.currentUser?.uid ?: throw IllegalStateException("Admin sign-in required.")
    try {
      val batch = db.batch()
      batch.update(
        db.collection("deletion_requests").document(request.id),
        mapOf("status" to "COMPLETED"),
      )
      batch.set(
        db.collection("audit_logs").document(),
        hashMapOf(
          "actorId" to adminUid,
          "action" to "COMPLETE_DELETION_REQUEST",
          "entityType" to "deletion_request",
          "entityId" to request.id,
          "reason" to "Completed deletion request for user ${request.userId}".take(300),
          "timestamp" to FieldValue.serverTimestamp(),
        ),
      )
      batch.commit().await()
    } catch (e: Exception) {
      FirestoreErrorHandler.handleFirestoreError(e, OperationType.UPDATE, "deletion_requests/${request.id}", auth)
    }
  }

  suspend fun reverseTransaction(tx: RewardTransaction, reason: String) {
    val adminUid = auth.currentUser?.uid ?: throw IllegalStateException("Admin sign-in required.")
    val cleanReason = reason.trim().ifEmpty { "Administrative ledger reversal" }.take(300)
    val userRef = db.collection("users").document(tx.userId)
    val walletRef = userRef.collection("wallet").document("summary")
    val reversalRef = userRef.collection("transactions").document("rev_${tx.id}")
    try {
      if (reversalRef.get().await().exists()) {
        throw IllegalStateException("This transaction has already been reversed.")
      }
      val walletSnap = walletRef.get().await()
      val wallet = walletSnap.toObject(RewardWallet::class.java) ?: RewardWallet(userId = tx.userId)
      val batch = db.batch()
      if (walletSnap.exists()) {
        batch.update(
          walletRef,
          mapOf(
            "availablePoints" to maxOf(0L, wallet.availablePoints - tx.amount),
            "reversedPoints" to (wallet.reversedPoints + tx.amount),
            "updatedAt" to FieldValue.serverTimestamp(),
          ),
        )
      } else {
        batch.set(
          walletRef,
          hashMapOf(
            "userId" to tx.userId,
            "unitCode" to "PTS",
            "availablePoints" to 0L,
            "pendingPoints" to 0L,
            "redeemedPoints" to 0L,
            "reversedPoints" to tx.amount,
            "updatedAt" to FieldValue.serverTimestamp(),
          ),
        )
      }
      batch.set(
        reversalRef,
        hashMapOf(
          "userId" to tx.userId,
          "transactionType" to TransactionType.REVERSAL.name,
          "amount" to tx.amount,
          "unitCode" to "PTS",
          "sourceActivity" to "Reversal: ${tx.sourceActivity}".take(120),
          "status" to TransactionStatus.REVERSED.name,
          "idempotencyKey" to "rev_${tx.id}",
          "auditReference" to "REV-${tx.id.take(8).uppercase()}",
          "relatedTransactionId" to tx.id,
          "notes" to cleanReason,
          "createdAt" to FieldValue.serverTimestamp(),
        ),
      )
      batch.set(
        db.collection("audit_logs").document(),
        hashMapOf(
          "actorId" to adminUid,
          "action" to "REVERSE_TRANSACTION",
          "entityType" to "transaction",
          "entityId" to tx.id,
          "reason" to cleanReason,
          "timestamp" to FieldValue.serverTimestamp(),
        ),
      )
      batch.commit().await()
    } catch (e: Exception) {
      FirestoreErrorHandler.handleFirestoreError(e, OperationType.WRITE, "users/${tx.userId}/transactions", auth)
    }
  }
}
