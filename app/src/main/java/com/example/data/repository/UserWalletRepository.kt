package com.example.data.repository

import com.example.data.local.LocalScanDao
import com.example.data.local.LocalScanEntity
import com.example.data.model.AppNotification
import com.example.data.model.NotificationCategory
import com.example.data.model.RewardTransaction
import com.example.data.model.RewardWallet
import com.example.data.model.ScanRecord
import com.example.data.model.TransactionStatus
import com.example.data.model.TransactionType
import com.example.data.model.UserProfile
import com.example.domain.MarketCatalog
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
import java.util.UUID

class UserWalletRepository(
    private val db: FirebaseFirestore,
    private val localScanDao: LocalScanDao
) {
    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing account data.")
    }

    suspend fun ensureUserInitialized(
        userId: String,
        displayName: String,
        countryCode: String,
        languageCode: String
    ): Result<Unit> {
        val userPath = "users/$userId"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Signed-in user mismatch." }
            val userRef = db.collection("users").document(uid)
            val walletRef = userRef.collection("wallet").document("summary")
            val market = MarketCatalog.findByCountry(countryCode)

            val userSnap = userRef.get().await()
            if (!userSnap.exists()) {
                val profile = UserProfile(
                    userId = uid,
                    displayName = displayName.ifBlank { "Shopper" },
                    preferredLanguage = languageCode.ifBlank { market.defaultLanguage },
                    countryCode = market.countryCode,
                    currencyCode = market.currencyCode,
                    accountStatus = "ACTIVE",
                    marketingConsent = false,
                    darkThemeEnabled = false,
                    reducedMotionEnabled = false
                )
                userRef.set(profile.toCreateMap()).await()
            }

            val walletSnap = walletRef.get().await()
            if (!walletSnap.exists()) {
                val wallet = RewardWallet(userId = uid)
                walletRef.set(wallet.toCreateMap()).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, userPath)
            Result.failure(e)
        }
    }

    fun observeProfile(userId: String): Flow<UserProfile?> = flow {
        val uid = requireUserId()
        check(uid == userId)
        val path = "users/$uid"
        emitAll(
            db.collection("users").document(uid)
                .snapshots()
                .map { snap ->
                    if (snap.exists()) {
                        snap.toObject(
                            UserProfile::class.java,
                            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                        )
                    } else null
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    if (auth.currentUser?.uid == uid) {
                        handleFirestoreError(firestoreError ?: error, OperationType.GET, path)
                    }
                    throw error
                }
        )
    }

    fun observeWallet(userId: String): Flow<RewardWallet> = flow {
        val uid = requireUserId()
        check(uid == userId)
        val path = "users/$uid/wallet/summary"
        emitAll(
            db.collection("users").document(uid).collection("wallet").document("summary")
                .snapshots()
                .map { snap ->
                    snap.toObject(
                        RewardWallet::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ) ?: RewardWallet(userId = uid)
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    if (auth.currentUser?.uid == uid) {
                        handleFirestoreError(firestoreError ?: error, OperationType.GET, path)
                    }
                    throw error
                }
        )
    }

    fun observeTransactions(userId: String): Flow<List<RewardTransaction>> = flow {
        val uid = requireUserId()
        check(uid == userId)
        val path = "users/$uid/transactions"
        emitAll(
            db.collection("users").document(uid).collection("transactions")
                .snapshots()
                .map { snap ->
                    snap.toObjects(
                        RewardTransaction::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    if (auth.currentUser?.uid == uid) {
                        handleFirestoreError(firestoreError ?: error, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    fun observeLocalScans(): Flow<List<ScanRecord>> =
        localScanDao.observeScans().map { list ->
            list.map {
                ScanRecord(
                    id = it.id.toString(),
                    userId = "local",
                    barcodeValue = it.barcodeValue,
                    barcodeFormat = it.barcodeFormat,
                    productName = it.productName,
                    brand = it.brand,
                    sourceProvider = it.sourceProvider,
                    verificationStatus = it.verificationStatus,
                    scanResult = it.scanResult,
                    scannedAt = Timestamp(it.scannedAtMillis / 1000L, 0)
                )
            }
        }

    fun observeCloudScans(userId: String): Flow<List<ScanRecord>> = flow {
        val uid = requireUserId()
        check(uid == userId)
        val path = "users/$uid/scans"
        emitAll(
            db.collection("users").document(uid).collection("scans")
                .snapshots()
                .map { snap ->
                    snap.toObjects(
                        ScanRecord::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.scannedAt ?: Timestamp(0, 0) }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    if (auth.currentUser?.uid == uid) {
                        handleFirestoreError(firestoreError ?: error, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    fun observeNotifications(userId: String): Flow<List<AppNotification>> = flow {
        val uid = requireUserId()
        check(uid == userId)
        val path = "users/$uid/notifications"
        emitAll(
            db.collection("users").document(uid).collection("notifications")
                .snapshots()
                .map { snap ->
                    snap.toObjects(
                        AppNotification::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    if (auth.currentUser?.uid == uid) {
                        handleFirestoreError(firestoreError ?: error, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun recordScan(
        barcodeValue: String,
        barcodeFormat: String,
        productName: String,
        brand: String,
        sourceProvider: String,
        verificationStatus: String,
        scanResult: String
    ): Result<Unit> {
        return try {
            localScanDao.insert(
                LocalScanEntity(
                    barcodeValue = barcodeValue,
                    barcodeFormat = barcodeFormat,
                    productName = productName,
                    brand = brand,
                    sourceProvider = sourceProvider,
                    verificationStatus = verificationStatus,
                    scanResult = scanResult,
                    scannedAtMillis = System.currentTimeMillis()
                )
            )
            val uid = auth.currentUser?.uid
            if (uid != null) {
                val docRef = db.collection("users").document(uid).collection("scans").document()
                val record = ScanRecord(
                    userId = uid,
                    barcodeValue = barcodeValue,
                    barcodeFormat = barcodeFormat,
                    productName = productName,
                    brand = brand,
                    sourceProvider = sourceProvider,
                    verificationStatus = verificationStatus,
                    scanResult = scanResult
                )
                docRef.set(record.toCreateMap()).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "users/${auth.currentUser?.uid}/scans")
            Result.failure(e)
        }
    }

    suspend fun updateProfilePreferences(
        userId: String,
        countryCode: String? = null,
        languageCode: String? = null,
        currencyCode: String? = null,
        marketingConsent: Boolean? = null,
        darkThemeEnabled: Boolean? = null,
        reducedMotionEnabled: Boolean? = null
    ): Result<Unit> {
        val path = "users/$userId"
        return try {
            val uid = requireUserId()
            check(uid == userId)
            val updates = mutableMapOf<String, Any>(
                "updatedAt" to FieldValue.serverTimestamp()
            )
            countryCode?.let { updates["countryCode"] = it.uppercase().take(2) }
            languageCode?.let { updates["preferredLanguage"] = it.lowercase().take(10) }
            currencyCode?.let { updates["currencyCode"] = it.uppercase().take(3) }
            marketingConsent?.let { updates["marketingConsent"] = it }
            darkThemeEnabled?.let { updates["darkThemeEnabled"] = it }
            reducedMotionEnabled?.let { updates["reducedMotionEnabled"] = it }
            db.collection("users").document(uid).update(updates).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun redeemSandboxVoucher(
        userId: String,
        currentWallet: RewardWallet,
        amountPoints: Long,
        rewardTitle: String
    ): Result<Unit> {
        val path = "users/$userId/transactions"
        return try {
            val uid = requireUserId()
            check(uid == userId)
            require(amountPoints > 0L) { "Redemption amount must be positive." }
            require(currentWallet.availablePoints >= amountPoints) {
                "Insufficient available points (${currentWallet.availablePoints} pts)."
            }

            val walletRef = db.collection("users").document(uid).collection("wallet").document("summary")
            val txRef = db.collection("users").document(uid).collection("transactions").document()
            val tx = RewardTransaction(
                userId = uid,
                transactionType = TransactionType.REDEMPTION.name,
                amount = amountPoints,
                unitCode = "PTS",
                sourceActivity = "Sandbox Voucher: $rewardTitle",
                status = TransactionStatus.REDEEMED.name,
                idempotencyKey = "redeem_${uid}_${UUID.randomUUID()}",
                auditReference = "SANDBOX-VOUCHER-${txRef.id.take(8).uppercase()}",
                notes = "Non-cash sandbox catalog reward redemption"
            )

            val batch = db.batch()
            batch.update(
                walletRef,
                mapOf(
                    "availablePoints" to (currentWallet.availablePoints - amountPoints),
                    "redeemedPoints" to (currentWallet.redeemedPoints + amountPoints),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.set(txRef, tx.toCreateMap())
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun markNotificationRead(userId: String, notificationId: String): Result<Unit> {
        val path = "users/$userId/notifications/$notificationId"
        return try {
            val uid = requireUserId()
            check(uid == userId)
            db.collection("users").document(uid).collection("notifications")
                .document(notificationId)
                .update("read", true)
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun requestAccountDeletion(userId: String, reason: String): Result<Unit> {
        val path = "deletion_requests"
        return try {
            val uid = requireUserId()
            check(uid == userId)
            val batch = db.batch()
            val reqRef = db.collection("deletion_requests").document()
            batch.set(
                reqRef,
                mapOf(
                    "userId" to uid,
                    "reason" to reason.trim().take(300),
                    "status" to "REQUESTED",
                    "requestedAt" to FieldValue.serverTimestamp()
                )
            )
            val userRef = db.collection("users").document(uid)
            batch.update(
                userRef,
                mapOf(
                    "accountStatus" to "DELETION_REQUESTED",
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            val notifRef = userRef.collection("notifications").document()
            val notif = AppNotification(
                userId = uid,
                category = NotificationCategory.ACCOUNT.name,
                title = "Account deletion request received",
                body = "Your account deletion request has been logged for compliance processing.",
                read = false,
                referenceId = reqRef.id
            )
            batch.set(notifRef, notif.toCreateMap())
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
