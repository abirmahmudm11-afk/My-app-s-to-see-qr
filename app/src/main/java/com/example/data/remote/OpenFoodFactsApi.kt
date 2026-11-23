package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class OpenFoodFactsProductResponse(
    @Json(name = "status") val status: Int = 0,
    @Json(name = "code") val code: String? = null,
    @Json(name = "product") val product: OpenFoodFactsProductDto? = null
)

@JsonClass(generateAdapter = true)
data class OpenFoodFactsSearchResponse(
    @Json(name = "count") val count: Int = 0,
    @Json(name = "products") val products: List<OpenFoodFactsProductDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class OpenFoodFactsProductDto(
    @Json(name = "code") val code: String? = null,
    @Json(name = "product_name") val productName: String? = null,
    @Json(name = "brands") val brands: String? = null,
    @Json(name = "categories") val categories: String? = null,
    @Json(name = "generic_name") val genericName: String? = null,
    @Json(name = "ingredients_text") val ingredientsText: String? = null,
    @Json(name = "image_front_url") val imageFrontUrl: String? = null,
    @Json(name = "image_url") val imageUrl: String? = null,
    @Json(name = "manufacturing_places") val manufacturingPlaces: String? = null,
    @Json(name = "origins") val origins: String? = null,
    @Json(name = "countries") val countries: String? = null,
    @Json(name = "quantity") val quantity: String? = null
)

interface OpenFoodFactsApi {
    @GET("api/v2/product/{barcode}.json")
    suspend fun getProductByBarcode(
        @Path("barcode") barcode: String,
        @Query("fields") fields: String =
            "code,product_name,brands,categories,generic_name,ingredients_text,image_front_url,image_url,manufacturing_places,origins,countries,quantity"
    ): OpenFoodFactsProductResponse

    @GET("cgi/search.pl")
    suspend fun searchProducts(
        @Query("search_terms") searchTerms: String,
        @Query("search_simple") searchSimple: Int = 1,
        @Query("action") action: String = "process",
        @Query("json") json: Int = 1,
        @Query("page_size") pageSize: Int = 20,
        @Query("fields") fields: String =
            "code,product_name,brands,categories,generic_name,image_front_url,image_url,manufacturing_places,origins,quantity"
    ): OpenFoodFactsSearchResponse

    companion object {
        private const val BASE_URL = "https://world.openfoodfacts.org/"

        fun create(): OpenFoodFactsApi {
            val userAgentInterceptor = Interceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("User-Agent", "SCAN2MONEY-Android/1.0 (https://scan2money.app)")
                    .build()
                chain.proceed(request)
            }
            val client = OkHttpClient.Builder()
                .addInterceptor(userAgentInterceptor)
                .connectTimeout(12, TimeUnit.SECONDS)
                .readTimeout(12, TimeUnit.SECONDS)
                .build()

            val moshi = com.squareup.moshi.Moshi.Builder()
                .add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(OpenFoodFactsApi::class.java)
        }
    }
}
