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
)

interface OpenFoodFactsApi {
  @GET("api/v2/product/{barcode}.json")
  suspend fun getProductByBarcode(
    @Path("barcode") barcode: String,
    @Query("fields") fields: String = "code,product_name,brands,categories,ingredients_text,generic_name,image_front_url,image_url,countries,manufacturing_places",
  ): OpenFoodFactsProductResponse

  @GET("cgi/search.pl")
  suspend fun searchProducts(
    @Query("search_terms") searchTerms: String,
    @Query("search_simple") searchSimple: Int = 1,
    @Query("action") action: String = "process",
    @Query("json") json: Int = 1,
    @Query("page_size") pageSize: Int = 20,
    @Query("fields") fields: String = "code,product_name,brands,categories,ingredients_text,generic_name,image_front_url,image_url,countries,manufacturing_places",
  ): OpenFoodFactsSearchResponse
}
