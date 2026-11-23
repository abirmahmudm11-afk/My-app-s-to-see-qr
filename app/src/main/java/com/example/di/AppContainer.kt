package com.example.di

import android.content.Context
import android.util.Log
import com.example.R
import com.example.auth.CredentialManagerAuth
import com.example.data.local.AppPreferencesDataStore
import com.example.data.local.LocalCacheDatabase
import com.example.data.remote.OpenFoodFactsApi
import com.example.data.repository.AdminRepository
import com.example.data.repository.ProductRepository
import com.example.data.repository.ReceiptAndCampaignRepository
import com.example.data.repository.ScanHistoryRepository
import com.example.data.repository.UserWalletRepository
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

class AppContainer(context: Context) {
  private val appContext = context.applicationContext

  val firebaseApp: FirebaseApp = FirebaseApp.getApps(appContext).firstOrNull() ?: run {
    val options = FirebaseOptions.Builder()
      .setProjectId(appContext.getString(R.string.firebase_project_id))
      .setApplicationId(appContext.getString(R.string.firebase_app_id))
      .setApiKey(appContext.getString(R.string.firebase_api_key))
      .setStorageBucket(appContext.getString(R.string.firebase_storage_bucket))
      .setGcmSenderId(appContext.getString(R.string.firebase_messaging_sender_id))
      .build()
    FirebaseApp.initializeApp(appContext, options)
  }

  val auth: FirebaseAuth = FirebaseAuth.getInstance(firebaseApp)
  val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(
    firebaseApp,
    appContext.getString(R.string.firebase_database_id),
  )

  init {
    CoroutineScope(Dispatchers.IO).launch {
      try {
        firestore.collection("test").document("connection").get(Source.SERVER)
      } catch (e: Exception) {
        if (e.message?.contains("client is offline", ignoreCase = true) == true) {
          Log.e("AppContainer", "Firestore client is offline; check Firebase configuration.")
        }
      }
    }
  }

  val preferencesDataStore = AppPreferencesDataStore(appContext)
  private val localDatabase = LocalCacheDatabase.build(appContext)

  private val okHttpClient = OkHttpClient.Builder()
    .connectTimeout(20, TimeUnit.SECONDS)
    .readTimeout(20, TimeUnit.SECONDS)
    .addInterceptor { chain ->
      val req = chain.request().newBuilder()
        .header("User-Agent", "SCAN2MONEY-Android/1.0 (contact@scan2money.app)")
        .build()
      chain.proceed(req)
    }
    .addInterceptor(
      HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC },
    )
    .build()

  private val moshi = Moshi.Builder()
    .addLast(KotlinJsonAdapterFactory())
    .build()

  private val openFoodFactsApi: OpenFoodFactsApi = Retrofit.Builder()
    .baseUrl("https://world.openfoodfacts.org/")
    .client(okHttpClient)
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .build()
    .create(OpenFoodFactsApi::class.java)

  val credentialAuth = CredentialManagerAuth(auth)
  val scanHistoryRepository = ScanHistoryRepository(localDatabase.scanHistoryDao())
  val productRepository = ProductRepository(
    db = firestore,
    auth = auth,
    api = openFoodFactsApi,
    cachedProductDao = localDatabase.cachedProductDao(),
  )
  val userWalletRepository = UserWalletRepository(
    db = firestore,
    auth = auth,
    scanHistoryRepository = scanHistoryRepository,
  )
  val receiptCampaignRepository = ReceiptAndCampaignRepository(
    db = firestore,
    auth = auth,
  )
  val adminRepository = AdminRepository(
    db = firestore,
    auth = auth,
  )
}
