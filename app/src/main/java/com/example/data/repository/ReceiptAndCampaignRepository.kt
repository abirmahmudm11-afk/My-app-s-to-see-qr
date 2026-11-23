package com.example.data.repository

import com.example.data.model.Campaign
import com.example.data.model.ReceiptReviewStatus
import com.example.data.model.ReceiptSubmission
import com.example.domain.ReceiptHashUtil
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
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

class ReceiptAndCampaignRepository(
    private val db: FirebaseFirestore
) {
    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google.")
    }

    fun observeActiveCampaigns(): Flow<List<Campaign>> = flow {
        if (auth.currentUser == null) {
            emit(emptyList())
            return@flow
        }
        val path = "campaigns"
        emitAll(
            db.collection("campaigns")
                .whereEqualTo("status", "ACTIVE")
                .snapshots()
                .map { snap ->
                    snap.toObjects(
                        Campaign::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.rewardPoints }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    if (auth.currentUser != null) {
                        handleFirestoreError(firestoreError ?: error, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    fun observeUserReceipts(userId: String): Flow<List<ReceiptSubmission>> = flow {
        val uid = requireUserId()
        check(uid == userId)
        val path = "receipts"
        emitAll(
            db.collection("receipts")
                .whereEqualTo("userId", uid)
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
                    if (auth.currentUser?.uid == uid) {
                        handleFirestoreError(firestoreError ?: error, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun submitReceipt(
        storeName: String,
        transactionDate: String,
        currencyCode: String,
        totalAmount: Double,
        receiptReference: String,
        campaignId: String?
    ): Result<ReceiptSubmission> {
        val path = "receipts"
        return try {
            val uid = requireUserId()
            require(storeName.isNotBlank() && storeName.length <= 100) { "Store name is required." }
            require(transactionDate.isNotBlank() && transactionDate.length <= 30) { "Transaction date is required." }
            require(currencyCode.length == 3) { "Currency must be a 3-letter ISO code." }
            require(totalAmount > 0.0 && totalAmount <= 1_000_000.0) { "Total amount must be positive." }
            require(receiptReference.isNotBlank() && receiptReference.length <= 100) { "Receipt reference is required." }

            val hash = ReceiptHashUtil.sha256ReferenceHash(
                storeName = storeName,
                transactionDate = transactionDate,
                totalAmount = totalAmount,
                currencyCode = currencyCode,
                receiptReference = receiptReference
            )

            // Duplicate detection against user's existing receipts
            val existing = db.collection("receipts")
                .whereEqualTo("userId", uid)
                .get()
                .await()
                .toObjects(ReceiptSubmission::class.java)
            if (existing.any { it.receiptReferenceHash == hash }) {
                return Result.failure(
                    IllegalStateException("Duplicate receipt detected: a receipt with this store, date, total, and reference has already been submitted.")
                )
            }

            val docRef = db.collection("receipts").document()
            val receipt = ReceiptSubmission(
                id = docRef.id,
                userId = uid,
                storeName = storeName.trim().take(100),
                transactionDate = transactionDate.trim().take(30),
                currencyCode = currencyCode.uppercase().take(3),
                totalAmount = totalAmount,
                receiptReference = receiptReference.trim().take(100),
                receiptReferenceHash = hash,
                campaignId = campaignId?.takeIf { it.isNotBlank() },
                reviewStatus = ReceiptReviewStatus.PENDING.name
            )
            docRef.set(receipt.toCreateMap()).await()
            Result.success(receipt)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }
}
