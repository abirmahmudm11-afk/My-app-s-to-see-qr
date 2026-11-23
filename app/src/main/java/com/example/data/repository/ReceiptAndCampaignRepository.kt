package com.example.data.repository

import com.example.data.model.Campaign
import com.example.data.model.ReceiptReviewStatus
import com.example.data.model.ReceiptSubmission
import com.example.domain.BarcodeValidator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ReceiptAndCampaignRepository(
  private val db: FirebaseFirestore,
  private val auth: FirebaseAuth,
) {
  fun observeActiveCampaigns(): Flow<List<Campaign>> = callbackFlow {
    if (auth.currentUser == null) {
      trySend(emptyList())
      close()
      return@callbackFlow
    }
    val reg = db.collection("campaigns")
      .whereEqualTo("status", "ACTIVE")
      .addSnapshotListener { snap, error ->
        if (error != null) {
          if (auth.currentUser != null) close(error)
          return@addSnapshotListener
        }
        val items = snap?.documents?.mapNotNull { doc ->
          doc.toObject(Campaign::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)?.copy(id = doc.id)
        }?.sortedByDescending { it.rewardPoints } ?: emptyList()
        trySend(items)
      }
    awaitClose { reg.remove() }
  }

  fun observeUserReceipts(userId: String): Flow<List<ReceiptSubmission>> = callbackFlow {
    val reg = db.collection("receipts")
      .whereEqualTo("userId", userId)
      .addSnapshotListener { snap, error ->
        if (error != null) {
          if (auth.currentUser?.uid == userId) close(error)
          return@addSnapshotListener
        }
        val items = snap?.documents?.mapNotNull { doc ->
          doc.toObject(ReceiptSubmission::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)?.copy(id = doc.id)
        }?.sortedByDescending { it.submittedAt?.seconds ?: 0L } ?: emptyList()
        trySend(items)
      }
    awaitClose { reg.remove() }
  }

  suspend fun submitReceipt(
    storeName: String,
    transactionDate: String,
    currencyCode: String,
    totalAmount: Double,
    receiptReference: String,
    campaignId: String?,
  ) {
    val uid = auth.currentUser?.uid ?: throw IllegalStateException("Sign in with Google to submit receipts.")
    val cleanStore = storeName.trim()
    val cleanDate = transactionDate.trim()
    val cleanCurrency = currencyCode.trim().uppercase()
    val cleanRef = receiptReference.trim()

    require(cleanStore.isNotEmpty() && cleanStore.length <= 100) { "Store name is required." }
    require(cleanDate.length in 4..30) { "Transaction date is required." }
    require(cleanCurrency.length == 3) { "Currency must be a 3-letter ISO code." }
    require(totalAmount > 0.0 && totalAmount <= 1_000_000.0) { "Total amount must be positive." }
    require(cleanRef.isNotEmpty() && cleanRef.length <= 100) { "Receipt reference is required." }

    val hash = BarcodeValidator.sha256ReceiptHash(
      storeName = cleanStore,
      transactionDate = cleanDate,
      totalAmount = totalAmount,
      currencyCode = cleanCurrency,
      receiptReference = cleanRef,
    )

    val path = "receipts"
    try {
      val existing = db.collection("receipts").whereEqualTo("userId", uid).get().await()
      val isDuplicate = existing.documents.any { doc ->
        doc.getString("receiptReferenceHash") == hash
      }
      if (isDuplicate) {
        throw IllegalStateException(
          "Duplicate receipt detected: this store, date, total, and reference were already submitted.",
        )
      }

      val payload = hashMapOf<String, Any>(
        "userId" to uid,
        "storeName" to cleanStore.take(100),
        "transactionDate" to cleanDate.take(30),
        "currencyCode" to cleanCurrency.take(3),
        "totalAmount" to totalAmount,
        "receiptReference" to cleanRef.take(100),
        "receiptReferenceHash" to hash.take(128),
        "reviewStatus" to ReceiptReviewStatus.PENDING.name,
        "submittedAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp(),
      )
      if (!campaignId.isNullOrBlank()) {
        payload["campaignId"] = campaignId.trim().take(128)
      }
      db.collection("receipts").document().set(payload).await()
    } catch (e: CancellationException) {
      throw e
    } catch (e: IllegalStateException) {
      throw e
    } catch (e: Exception) {
      FirestoreErrorHandler.handleFirestoreError(e, OperationType.CREATE, path, auth)
    }
  }
}
