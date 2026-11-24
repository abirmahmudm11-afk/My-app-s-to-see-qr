package com.example.data.repository

import com.example.data.local.ScanHistoryDao
import com.example.data.local.ScanHistoryEntity
import com.example.data.model.ScanRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ScanHistoryRepository(
  private val scanHistoryDao: ScanHistoryDao,
) {
  fun observeHistory(): Flow<List<ScanRecord>> =
    scanHistoryDao.observeAll().map { entities -> entities.map { it.toScanRecord() } }

  fun searchHistory(query: String): Flow<List<ScanRecord>> =
    scanHistoryDao.search(query.trim()).map { entities -> entities.map { it.toScanRecord() } }

  suspend fun saveScan(
    userId: String,
    barcodeValue: String,
    barcodeFormat: String,
    productName: String,
    brand: String,
    category: String,
    description: String,
    imageUrl: String,
    dataProvider: String,
    verificationStatus: String,
    scanResult: String,
    scannedAtMs: Long = System.currentTimeMillis(),
    quantity: String = "",
    nutriscoreGrade: String = "",
  ): Long {
    return scanHistoryDao.insert(
      ScanHistoryEntity(
        userId = userId,
        barcodeValue = barcodeValue.take(128),
        barcodeFormat = barcodeFormat.take(32),
        productName = productName.take(160),
        brand = brand.take(100),
        category = category.take(80),
        description = description.take(600),
        imageUrl = imageUrl.take(500),
        dataProvider = dataProvider.ifBlank { "Local Scanner" }.take(80),
        verificationStatus = verificationStatus,
        scanResult = scanResult,
        scannedAtMs = scannedAtMs,
        quantity = quantity.take(64),
        nutriscoreGrade = nutriscoreGrade.take(8),
      ),
    )
  }

  suspend fun deleteEntry(id: Long) {
    scanHistoryDao.deleteById(id)
  }

  suspend fun clearHistory() {
    scanHistoryDao.clearAll()
  }
}
