package com.example.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
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
    val lastUpdatedMillis: Long
)

@Entity(tableName = "local_scans")
data class LocalScanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val barcodeValue: String,
    val barcodeFormat: String,
    val productName: String,
    val brand: String,
    val sourceProvider: String,
    val verificationStatus: String,
    val scanResult: String,
    val scannedAtMillis: Long
)

@Dao
interface ProductCacheDao {
    @Query("SELECT * FROM cached_products WHERE barcode = :barcode LIMIT 1")
    suspend fun getByBarcode(barcode: String): CachedProductEntity?

    @Query("SELECT * FROM cached_products ORDER BY lastUpdatedMillis DESC LIMIT 50")
    fun observeAll(): Flow<List<CachedProductEntity>>

    @Query("SELECT * FROM cached_products WHERE name LIKE '%' || :query || '%' OR brand LIKE '%' || :query || '%' OR barcode LIKE '%' || :query || '%' ORDER BY lastUpdatedMillis DESC")
    suspend fun searchLocal(query: String): List<CachedProductEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(product: CachedProductEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(products: List<CachedProductEntity>)
}

@Dao
interface LocalScanDao {
    @Query("SELECT * FROM local_scans ORDER BY scannedAtMillis DESC LIMIT 100")
    fun observeScans(): Flow<List<LocalScanEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(scan: LocalScanEntity)

    @Query("DELETE FROM local_scans WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM local_scans")
    suspend fun clearAll()
}

@Database(
    entities = [CachedProductEntity::class, LocalScanEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productCacheDao(): ProductCacheDao
    abstract fun localScanDao(): LocalScanDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "scan2money_local.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
