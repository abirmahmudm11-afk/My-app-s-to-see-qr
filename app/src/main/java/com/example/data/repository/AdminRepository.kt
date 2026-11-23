package com.example.data.repository

import com.example.data.model.AdminRole
import com.example.data.model.AppNotification
import com.example.data.model.AuditLog
import com.example.data.model.Campaign
import com.example.data.model.CampaignStatus
import com.example.data.model.CatalogProduct
import com.example.data.model.NotificationCategory
import com.example.data.model.ReceiptReviewStatus
import com.example.data.model.ReceiptSubmission
import com.example.data.model.RewardTransaction
import com.example.data.model.TransactionStatus
import com.example.data.model.TransactionType
import com.example.data.model.VerificationStatus
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class AdminRepository(
    private val db: FirebaseFirestore
) {
    private val auth = Firebase.auth

    companion object {
        private const val BOOTSTRAPPED_ADMIN_EMAIL = "m28044963@gmail.com"
    }

    suspend fun resolveAdminRole(): AdminRole? {
        val user = auth.currentUser ?: return null
        if (user.isEmailVerified && user.email == BOOTSTRAPPED_ADMIN_EMAIL) {
            return AdminRole(
                id = user.uid,
                userId = user.uid,
                roleName = "SUPER_ADMIN",
                permissions = listOf("REVIEW_RECEIPTS", "MANAGE_CAMPAIGNS", "REVERSE_LEDGER", "MANAGE_CATALOG", "VIEW_AUDIT")
            )
        }
        return try {
            val snap = db.collection("admins").document(user.uid).get().await()
            if (snap.exists()) {
                snap.toObject(AdminRole::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    fun observePendingReceipts(): Flow<List<ReceiptSubmission>> = flow {
        val path = "receipts"
        emitAll(
            db.collection("receipts")
                .whereEqualTo("reviewStatus", ReceiptReviewStatus.PENDING.name)
                .snapshots()
                .map { snap ->
                    snap.toObjects(
                        ReceiptSubmission::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.submittedAt ?: Timestamp(0, 0) }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    handleFirestoreError(firestoreError ?: error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    fun observeAllCampaigns(): Flow<List<Campaign>> = flow {
        val path = "campaigns"
        emitAll(
            db.collection("campaigns")
                .snapshots()
                .map { snap ->
                    snap.toObjects(
                        Campaign::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    handleFirestoreError(firestoreError ?: error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    fun observeUnverifiedCatalog(): Flow<List<CatalogProduct>> = flow {
        val path = "products_catalog"
        emitAll(
            db.collection("products_catalog")
                .whereEqualTo("verificationStatus", VerificationStatus.USER_SUBMITTED_UNVERIFIED.name)
                .snapshots()
                .map { snap ->
                    snap.toObjects(
                        CatalogProduct::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    handleFirestoreError(firestoreError ?: error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    fun observeAuditLogs(): Flow<List<AuditLog>> = flow {
        val path = "audit_logs"
        emitAll(
            db.collection("audit_logs")
                .snapshots()
                .map { snap ->
                    snap.toObjects(
                        AuditLog::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.timestamp ?: Timestamp(0, 0) }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    handleFirestoreError(firestoreError ?: error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun approveReceiptAndCreditPoints(
        receipt: ReceiptSubmission,
        pointsToCredit: Long
    ): Result<Unit> {
        val path = "receipts/${receipt.id}"
        return try {
            val adminUid = auth.currentUser?.uid ?: error("Not signed in")
            require(pointsToCredit in 1L..10000L) { "Points must be between 1 and 10,000." }

            val walletRef = db.collection("users").document(receipt.userId)
                .collection("wallet").document("summary")
            val walletSnap = walletRef.get().await()
            val currentAvailable = walletSnap.getLong("availablePoints") ?: 0L

            val idempotencyKey = "receipt_${receipt.id}"
            val txRef = db.collection("users").document(receipt.userId)
                .collection("transactions").document(idempotencyKey)
            val existingTx = txRef.get().await()
            if (existingTx.exists()) {
                return Result.failure(IllegalStateException("Idempotency guard: receipt already credited."))
            }

            val receiptRef = db.collection("receipts").document(receipt.id)
            val notifRef = db.collection("users").document(receipt.userId)
                .collection("notifications").document()
            val auditRef = db.collection("audit_logs").document()

            val tx = RewardTransaction(
                userId = receipt.userId,
                transactionType = TransactionType.RECEIPT_REWARD.name,
                amount = pointsToCredit,
                unitCode = "PTS",
                sourceActivity = "Verified Receipt: ${receipt.storeName}",
                campaignId = receipt.campaignId,
                status = TransactionStatus.CREDITED.name,
                idempotencyKey = idempotencyKey,
                auditReference = "AUDIT-${auditRef.id.take(8).uppercase()}",
                notes = "Approved receipt ${receipt.receiptReference}"
            )
            val notif = AppNotification(
                userId = receipt.userId,
                category = NotificationCategory.RECEIPT_REVIEW.name,
                title = "Receipt approved (+$pointsToCredit pts)",
                body = "Your ${receipt.storeName} receipt was verified and credited to your sandbox wallet.",
                read = false,
                referenceId = receipt.id
            )
            val audit = AuditLog(
                actorId = adminUid,
                action = "APPROVE_RECEIPT",
                entityType = "RECEIPT",
                entityId = receipt.id,
                reason = "Credited $pointsToCredit PTS for ${receipt.storeName} (${receipt.receiptReference})"
            )

            val batch = db.batch()
            batch.update(
                receiptRef,
                mapOf(
                    "reviewStatus" to ReceiptReviewStatus.APPROVED.name,
                    "reviewedBy" to adminUid,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            if (walletSnap.exists()) {
                batch.update(
                    walletRef,
                    mapOf(
                        "availablePoints" to (currentAvailable + pointsToCredit),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            } else {
                batch.set(
                    walletRef,
                    mapOf(
                        "userId" to receipt.userId,
                        "unitCode" to "PTS",
                        "availablePoints" to pointsToCredit,
                        "pendingPoints" to 0L,
                        "redeemedPoints" to 0L,
                        "reversedPoints" to 0L,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            }
            batch.set(txRef, tx.toCreateMap())
            batch.set(notifRef, notif.toCreateMap())
            batch.set(auditRef, audit.toCreateMap())
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun rejectReceipt(
        receipt: ReceiptSubmission,
        reason: String
    ): Result<Unit> {
        val path = "receipts/${receipt.id}"
        return try {
            val adminUid = auth.currentUser?.uid ?: error("Not signed in")
            require(reason.isNotBlank() && reason.length <= 300) { "Rejection reason required." }

            val receiptRef = db.collection("receipts").document(receipt.id)
            val notifRef = db.collection("users").document(receipt.userId)
                .collection("notifications").document()
            val auditRef = db.collection("audit_logs").document()

            val notif = AppNotification(
                userId = receipt.userId,
                category = NotificationCategory.RECEIPT_REVIEW.name,
                title = "Receipt review update",
                body = "Your ${receipt.storeName} receipt was rejected: $reason",
                read = false,
                referenceId = receipt.id
            )
            val audit = AuditLog(
                actorId = adminUid,
                action = "REJECT_RECEIPT",
                entityType = "RECEIPT",
                entityId = receipt.id,
                reason = reason.trim().take(300)
            )

            val batch = db.batch()
            batch.update(
                receiptRef,
                mapOf(
                    "reviewStatus" to ReceiptReviewStatus.REJECTED.name,
                    "rejectionReason" to reason.trim().take(300),
                    "reviewedBy" to adminUid,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.set(notifRef, notif.toCreateMap())
            batch.set(auditRef, audit.toCreateMap())
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun createCampaign(
        partnerName: String,
        title: String,
        terms: String,
        rewardPoints: Long,
        maxPerUser: Long,
        eligibleCountries: List<String>,
        eligibleCategories: List<String>,
        status: CampaignStatus
    ): Result<Unit> {
        val path = "campaigns"
        return try {
            val adminUid = auth.currentUser?.uid ?: error("Not signed in")
            val docRef = db.collection("campaigns").document()
            val auditRef = db.collection("audit_logs").document()
            val campaign = Campaign(
                id = docRef.id,
                partnerId = "partner_${partnerName.lowercase().replace(Regex("[^a-z0-9]"), "").take(16).ifBlank { "sandbox" }}",
                partnerName = partnerName.trim().take(100),
                title = title.trim().take(120),
                terms = terms.trim().take(1000),
                rewardType = "SANDBOX_POINTS",
                rewardPoints = rewardPoints.coerceIn(1L, 10000L),
                maxPerUser = maxPerUser.coerceIn(1L, 50L),
                budgetLimitPoints = 50000L,
                eligibleCountries = eligibleCountries.ifEmpty { listOf("GLOBAL") },
                eligibleBarcodes = emptyList(),
                eligibleCategories = eligibleCategories,
                status = status.name
            )
            val batch = db.batch()
            batch.set(docRef, campaign.toCreateMap())
            batch.set(
                auditRef,
                AuditLog(
                    actorId = adminUid,
                    action = "CREATE_CAMPAIGN",
                    entityType = "CAMPAIGN",
                    entityId = docRef.id,
                    reason = "Created ${campaign.title} (${campaign.status})"
                ).toCreateMap()
            )
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun updateCampaignStatus(campaignId: String, newStatus: CampaignStatus): Result<Unit> {
        val path = "campaigns/$campaignId"
        return try {
            val adminUid = auth.currentUser?.uid ?: error("Not signed in")
            val batch = db.batch()
            batch.update(
                db.collection("campaigns").document(campaignId),
                mapOf(
                    "status" to newStatus.name,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.set(
                db.collection("audit_logs").document(),
                AuditLog(
                    actorId = adminUid,
                    action = "CAMPAIGN_STATUS_${newStatus.name}",
                    entityType = "CAMPAIGN",
                    entityId = campaignId,
                    reason = "Transitioned campaign to ${newStatus.name}"
                ).toCreateMap()
            )
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun promoteCatalogProduct(barcode: String, verificationStatus: VerificationStatus): Result<Unit> {
        val path = "products_catalog/$barcode"
        return try {
            val adminUid = auth.currentUser?.uid ?: error("Not signed in")
            val batch = db.batch()
            batch.update(
                db.collection("products_catalog").document(barcode),
                mapOf(
                    "verificationStatus" to verificationStatus.name,
                    "attributionText" to "Verified by SCAN2MONEY catalog review (${verificationStatus.label})",
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.set(
                db.collection("audit_logs").document(),
                AuditLog(
                    actorId = adminUid,
                    action = "VERIFY_PRODUCT",
                    entityType = "PRODUCT",
                    entityId = barcode,
                    reason = "Updated verification status to ${verificationStatus.name}"
                ).toCreateMap()
            )
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun reverseTransaction(
        originalTx: RewardTransaction,
        reason: String
    ): Result<Unit> {
        val path = "users/${originalTx.userId}/transactions"
        return try {
            val adminUid = auth.currentUser?.uid ?: error("Not signed in")
            require(reason.isNotBlank() && reason.length <= 300) { "Reversal reason required." }

            val walletRef = db.collection("users").document(originalTx.userId)
                .collection("wallet").document("summary")
            val walletSnap = walletRef.get().await()
            val available = walletSnap.getLong("availablePoints") ?: 0L
            val reversed = walletSnap.getLong("reversedPoints") ?: 0L
            val newAvailable = (available - originalTx.amount).coerceAtLeast(0L)

            val revId = "rev_${originalTx.id}"
            val revRef = db.collection("users").document(originalTx.userId)
                .collection("transactions").document(revId)
            if (revRef.get().await().exists()) {
                return Result.failure(IllegalStateException("Transaction already reversed."))
            }

            val auditRef = db.collection("audit_logs").document()
            val reversalTx = RewardTransaction(
                userId = originalTx.userId,
                transactionType = TransactionType.REVERSAL.name,
                amount = originalTx.amount,
                unitCode = "PTS",
                sourceActivity = "Reversal: ${originalTx.sourceActivity}".take(120),
                campaignId = originalTx.campaignId,
                status = TransactionStatus.REVERSED.name,
                idempotencyKey = revId,
                auditReference = "REV-${auditRef.id.take(8).uppercase()}",
                relatedTransactionId = originalTx.id,
                notes = reason.trim().take(300)
            )

            val batch = db.batch()
            if (walletSnap.exists()) {
                batch.update(
                    walletRef,
                    mapOf(
                        "availablePoints" to newAvailable,
                        "reversedPoints" to (reversed + originalTx.amount),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            }
            batch.set(revRef, reversalTx.toCreateMap())
            batch.set(
                db.collection("users").document(originalTx.userId).collection("notifications").document(),
                AppNotification(
                    userId = originalTx.userId,
                    category = NotificationCategory.REWARD_STATUS.name,
                    title = "Reward ledger reversal (-${originalTx.amount} pts)",
                    body = reason.trim().take(300),
                    read = false,
                    referenceId = originalTx.id
                ).toCreateMap()
            )
            batch.set(
                auditRef,
                AuditLog(
                    actorId = adminUid,
                    action = "REVERSE_TRANSACTION",
                    entityType = "TRANSACTION",
                    entityId = originalTx.id,
                    reason = reason.trim().take(300)
                ).toCreateMap()
            )
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }
}
