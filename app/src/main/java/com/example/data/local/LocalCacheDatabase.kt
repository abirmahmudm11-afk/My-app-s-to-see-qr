package com.example.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.CatalogProduct
import com.example.data.model.ScanRecord
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "cached_products")
data class CachedProductEntity(
  @PrimaryKey val barcode: String,
  val name: String,
  val brand: String,
  val category: String,
  val description: String,
  val imageUrl: String,
  val manufacturer: String,
  val countryOfOrigin: String,
  val dataProvider: String,
  val attributionText: String,
  val verificationStatus: String,
  val submittedBy: String,
  val updatedAtMs: Long,
) {
  fun toCatalogProduct(): CatalogProduct = CatalogProduct(
    id = barcode,
    barcode = barcode,
    name = name,
    brand = brand,
    category = category,
    description = description,
    imageUrl = imageUrl,
    manufacturer = manufacturer,
    countryOfOrigin = countryOfOrigin,
    dataProvider = dataProvider,
    attributionText = attributionText,
    verificationStatus = verificationStatus,
    submittedBy = submittedBy,
  )

  companion object {
    fun fromCatalogProduct(product: CatalogProduct): CachedProductEntity = CachedProductEntity(
      barcode = product.barcode,
      name = product.name,
      brand = product.brand,
      category = product.category,
      description = product.description,
      imageUrl = product.imageUrl,
      manufacturer = product.manufacturer,
      countryOfOrigin = product.countryOfOrigin,
      dataProvider = product.dataProvider,
      attributionText = product.attributionText,
      verificationStatus = product.verificationStatus,
      submittedBy = product.submittedBy,
      updatedAtMs = System.currentTimeMillis(),
    )
  }
}

@Entity(
  tableName = "scan_history",
  indices = [
    Index(value = ["scannedAtMs"]),
    Index(value = ["barcodeValue"]),
  ],
)
data class ScanHistoryEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0L,
  val userId: String,
  val barcodeValue: String,
  val barcodeFormat: String,
  val productName: String,
  val brand: String,
  val category: String,
  val description: String,
  val imageUrl: String,
  val dataProvider: String,
  val verificationStatus: String,
  val scanResult: String,
  val scannedAtMs: Long,
  val quantity: String = "",
  val nutriscoreGrade: String = "",
) {
  fun toScanRecord(): ScanRecord = ScanRecord(
    id = "local_$id",
    localHistoryId = id,
    userId = userId,
    barcodeValue = barcodeValue,
    barcodeFormat = barcodeFormat,
    productName = productName,
    brand = brand,
    category = category,
    description = description,
    imageUrl = imageUrl,
    quantity = quantity,
    nutriscoreGrade = nutriscoreGrade,
    sourceProvider = dataProvider,
    verificationStatus = verificationStatus,
    scanResult = scanResult,
    localTimestampMs = scannedAtMs,
  )
}

@Dao
interface CachedProductDao {
  @Query("SELECT * FROM cached_products WHERE barcode = :barcode LIMIT 1")
  suspend fun findByBarcode(barcode: String): CachedProductEntity?

  @Query("SELECT * FROM cached_products ORDER BY updatedAtMs DESC LIMIT 50")
  fun observeAll(): Flow<List<CachedProductEntity>>

  @Query("SELECT * FROM cached_products WHERE name LIKE '%' || :query || '%' OR brand LIKE '%' || :query || '%' OR barcode LIKE '%' || :query || '%' ORDER BY updatedAtMs DESC LIMIT 30")
  suspend fun search(query: String): List<CachedProductEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertAll(items: List<CachedProductEntity>)
}

@Dao
interface ScanHistoryDao {
  @Query("SELECT * FROM scan_history ORDER BY scannedAtMs DESC LIMIT 200")
  fun observeAll(): Flow<List<ScanHistoryEntity>>

  @Query(
    """
    SELECT * FROM scan_history
    WHERE productName LIKE '%' || :query || '%'
       OR brand LIKE '%' || :query || '%'
       OR barcodeValue LIKE '%' || :query || '%'
       OR category LIKE '%' || :query || '%'
    ORDER BY scannedAtMs DESC
    LIMIT 200
    """,
  )
  fun search(query: String): Flow<List<ScanHistoryEntity>>

  @Query("SELECT * FROM scan_history WHERE id = :id LIMIT 1")
  suspend fun findById(id: Long): ScanHistoryEntity?

  @Query("SELECT * FROM scan_history WHERE barcodeValue = :barcode ORDER BY scannedAtMs DESC LIMIT 1")
  suspend fun findLatestByBarcode(barcode: String): ScanHistoryEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(entry: ScanHistoryEntity): Long

  @Query("DELETE FROM scan_history WHERE id = :id")
  suspend fun deleteById(id: Long)

  @Query("DELETE FROM scan_history")
  suspend fun clearAll()
}

@Database(
  entities = [CachedProductEntity::class, ScanHistoryEntity::class],
  version = 3,
  exportSchema = false,
)
abstract class LocalCacheDatabase : RoomDatabase() {
  abstract fun cachedProductDao(): CachedProductDao
  abstract fun scanHistoryDao(): ScanHistoryDao

  companion object {
    fun build(context: Context): LocalCacheDatabase =
      Room.databaseBuilder(
        context.applicationContext,
        LocalCacheDatabase::class.java,
        "scan2money_cache.db",
      ).fallbackToDestructiveMigration(dropAllTables = true).build()
  }
}
