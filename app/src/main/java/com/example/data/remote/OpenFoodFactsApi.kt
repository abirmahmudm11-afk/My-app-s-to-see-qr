package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

@JsonClass(generateAdapter = true)
data class OpenFoodFactsProductResponse(
  val status: Int = 0,
  val product: OpenFoodFactsProductDto? = null,
)

@JsonClass(generateAdapter = true)
data class OpenFoodFactsSearchResponse(
  val products: List<OpenFoodFactsProductDto> = emptyList(),
)

@JsonClass(generateAdapter = true)
data class OpenFoodFactsProductDto(
  val code: String? = null,
  @param:Json(name = "product_name") val productName: String? = null,
  val brands: String? = null,
  val categories: String? = null,
  @param:Json(name = "ingredients_text") val ingredientsText: String? = null,
  @param:Json(name = "generic_name") val genericName: String? = null,
  @param:Json(name = "image_front_url") val imageFrontUrl: String? = null,
  @param:Json(name = "image_url") val imageUrl: String? = null,
  val countries: String? = null,
  @param:Json(name = "manufacturing_places") val manufacturingPlaces: String? = null,
  val quantity: String? = null,
  @param:Json(name = "nutriscore_grade") val nutriscoreGrade: String? = null,
  @param:Json(name = "ecoscore_grade") val ecoscoreGrade: String? = null,
  val stores: String? = null,
  val packaging: String? = null,
)

@JsonClass(generateAdapter = true)
data class OpenPricesResponse(
  val items: List<OpenPriceItemDto> = emptyList(),
  val total: Int = 0,
)

@JsonClass(generateAdapter = true)
data class OpenPriceItemDto(
  val id: Long = 0L,
  @param:Json(name = "product_code") val productCode: String? = null,
  val price: Double? = null,
  @param:Json(name = "price_without_discount") val priceWithoutDiscount: Double? = null,
  @param:Json(name = "price_is_discounted") val priceIsDiscounted: Boolean = false,
  val currency: String? = null,
  @param:Json(name = "price_per") val pricePer: String? = null,
  val date: String? = null,
  val location: OpenPriceLocationDto? = null,
)

@JsonClass(generateAdapter = true)
data class OpenPriceLocationDto(
  @param:Json(name = "osm_name") val osmName: String? = null,
  @param:Json(name = "osm_address_city") val osmAddressCity: String? = null,
  @param:Json(name = "osm_address_country") val osmAddressCountry: String? = null,
)

interface OpenFoodFactsApi {
  @GET("api/v2/product/{barcode}.json")
  suspend fun getProductByBarcode(
    @Path("barcode") barcode: String,
    @Query("fields") fields: String = "code,product_name,brands,categories,ingredients_text,generic_name,image_front_url,image_url,countries,manufacturing_places,quantity,nutriscore_grade,ecoscore_grade,stores,packaging",
  ): OpenFoodFactsProductResponse

  @GET("api/v2/search")
  suspend fun searchProductsV2(
    @Query("search_terms") searchTerms: String = "",
    @Query("categories_tags_en") categoriesTagsEn: String? = null,
    @Query("sort_by") sortBy: String = "unique_scans_n",
    @Query("page_size") pageSize: Int = 24,
    @Query("fields") fields: String = "code,product_name,brands,categories,ingredients_text,generic_name,image_front_url,image_url,countries,manufacturing_places,quantity,nutriscore_grade,ecoscore_grade,stores,packaging",
  ): OpenFoodFactsSearchResponse

  @GET("cgi/search.pl")
  suspend fun searchProducts(
    @Query("search_terms") searchTerms: String,
    @Query("search_simple") searchSimple: Int = 1,
    @Query("action") action: String = "process",
    @Query("json") json: Int = 1,
    @Query("page_size") pageSize: Int = 24,
    @Query("fields") fields: String = "code,product_name,brands,categories,ingredients_text,generic_name,image_front_url,image_url,countries,manufacturing_places,quantity,nutriscore_grade,ecoscore_grade,stores,packaging",
  ): OpenFoodFactsSearchResponse
}

interface OpenPricesApi {
  @GET("api/v1/prices")
  suspend fun getPricesByBarcode(
    @Query("product_code") productCode: String,
    @Query("size") size: Int = 20,
    @Query("order_by") orderBy: String = "price",
  ): OpenPricesResponse

  @GET("api/v1/prices")
  suspend fun getLatestPrices(
    @Query("size") size: Int = 20,
    @Query("order_by") orderBy: String = "-date",
  ): OpenPricesResponse
}
