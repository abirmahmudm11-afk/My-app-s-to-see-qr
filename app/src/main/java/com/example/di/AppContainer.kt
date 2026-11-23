package com.example.di

import android.content.Context
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.local.AppPreferencesDataStore
import com.example.data.remote.OpenFoodFactsApi
import com.example.data.repository.AdminRepository
import com.example.data.repository.OpenFoodFactsAdapter
import com.example.data.repository.ProductRepository
import com.example.data.repository.ReceiptAndCampaignRepository
import com.example.data.repository.UserWalletRepository
import com.google.firebase.firestore.FirebaseFirestore

class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance(appContext.getString(R.string.firestore_database_id))
    }

    val localDb: AppDatabase by lazy {
        AppDatabase.getInstance(appContext)
    }

    val preferencesDataStore: AppPreferencesDataStore by lazy {
        AppPreferencesDataStore(appContext)
    }

    val productRepository: ProductRepository by lazy {
        ProductRepository(
            db = firestore,
            productCacheDao = localDb.productCacheDao(),
            primaryAdapter = OpenFoodFactsAdapter(OpenFoodFactsApi.create())
        )
    }

    val userWalletRepository: UserWalletRepository by lazy {
        UserWalletRepository(
            db = firestore,
            localScanDao = localDb.localScanDao()
        )
    }

    val receiptAndCampaignRepository: ReceiptAndCampaignRepository by lazy {
        ReceiptAndCampaignRepository(db = firestore)
    }

    val adminRepository: AdminRepository by lazy {
        AdminRepository(db = firestore)
    }
}
