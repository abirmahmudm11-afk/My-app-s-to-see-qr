package com.example.data.remote

import com.example.data.repository.ComparedOffer
import com.example.domain.MarketConfig
import kotlinx.coroutines.delay
import java.net.URLEncoder
import kotlin.math.abs
import kotlin.math.roundToInt

private data class RetailerTemplate(
  val name: String,
  val deliveryNote: String,
  val ratingLabel: String,
  val baseMultiplier: Double,
  val searchUrlPattern: String,
)

class SimulatedRetailerPriceApi {

  suspend fun searchPricesByBarcode(
    barcode: String,
    productName: String,
    brand: String,
    countryCode: String,
    currencyCode: String? = null,
  ): List<ComparedOffer> {
    val cleanBarcode = barcode.trim()
    if (cleanBarcode.isEmpty()) return emptyList()

    // Simulate realistic asynchronous network latency for online retailer price aggregation
    delay(420L)

    val market = MarketConfig.findByCountry(countryCode)
    val resolvedCurrency = currencyCode?.trim()?.uppercase()?.take(3)?.ifEmpty { market.currencyCode }
      ?: market.currencyCode
    val rawQuery = listOf(brand, productName, cleanBarcode)
      .filter { it.isNotBlank() }
      .joinToString(" ")
    val searchTerm = URLEncoder.encode(rawQuery, "UTF-8")

    val seed = abs(cleanBarcode.hashCode())
    val baseUsdPrice = 2.49 + ((seed % 1450) / 100.0)
    val currencyScale = when (resolvedCurrency) {
      "EUR" -> 0.92
      "GBP" -> 0.79
      "CAD" -> 1.36
      "AUD" -> 1.52
      "BDT" -> 119.0
      else -> 1.0
    }
    val baseLocalPrice = baseUsdPrice * currencyScale

    val retailers = retailersForMarket(market.countryCode)
    return retailers.mapIndexed { index, retailer ->
      val jitterSteps = ((seed / (index + 3)) % 17) - 8 // -8..+8
      val jitterFactor = 1.0 + (jitterSteps * 0.014)
      val rawPrice = baseLocalPrice * retailer.baseMultiplier * jitterFactor
      val roundedPrice = roundPrice(rawPrice, resolvedCurrency)

      val discounted = ((seed + index) % 3 == 0)
      val originalPrice = if (discounted) {
        roundPrice(roundedPrice * (1.12 + ((index % 3) * 0.05)), resolvedCurrency)
      } else {
        null
      }

      val url = retailer.searchUrlPattern.replace("{query}", searchTerm)
      ComparedOffer(
        id = "sim_${cleanBarcode}_${index}_${retailer.name.lowercase().replace(Regex("[^a-z0-9]"), "")}",
        storeName = retailer.name,
        price = roundedPrice,
        currencyCode = resolvedCurrency,
        locationLabel = retailer.deliveryNote,
        observedDate = retailer.ratingLabel,
        isDiscounted = discounted,
        originalPrice = originalPrice,
        isVerifiedMerchant = false,
        sourceLabel = "Simulated Retailer API",
        offerUrl = url,
        deliveryNote = retailer.deliveryNote,
        isSimulated = true,
      )
    }.sortedBy { it.price }
  }

  private fun roundPrice(value: Double, currencyCode: String): Double {
    return if (currencyCode == "BDT") {
      ((value / 5.0).roundToInt() * 5).coerceAtLeast(25).toDouble()
    } else {
      val cents = (value * 100.0).roundToInt().coerceAtLeast(99)
      cents / 100.0
    }
  }

  private fun retailersForMarket(countryCode: String): List<RetailerTemplate> {
    return when (countryCode.uppercase()) {
      "GB" -> listOf(
        RetailerTemplate("Amazon UK", "Free Prime Next-Day", "In Stock • 4.8★", 0.96, "https://www.amazon.co.uk/s?k={query}"),
        RetailerTemplate("Tesco Groceries", "Same-day Click & Collect", "In Stock • 4.6★", 1.00, "https://www.tesco.com/groceries/en-GB/search?query={query}"),
        RetailerTemplate("Sainsbury's", "Delivery from £1.50", "In Stock • 4.5★", 1.04, "https://www.sainsburys.co.uk/gol-ui/SearchResults/{query}"),
        RetailerTemplate("Ocado", "1-hour delivery slot", "Limited Stock • 4.7★", 1.08, "https://www.ocado.com/search?entry={query}"),
      )
      "FR", "DE" -> listOf(
        RetailerTemplate("Amazon EU", "Free Express Delivery", "In Stock • 4.8★", 0.95, "https://www.amazon.de/s?k={query}"),
        RetailerTemplate("Carrefour Online", "Drive pickup in 2h", "In Stock • 4.6★", 1.01, "https://www.carrefour.fr/s?q={query}"),
        RetailerTemplate("Auchan Direct", "Home delivery today", "In Stock • 4.5★", 1.04, "https://www.auchan.fr/recherche?text={query}"),
        RetailerTemplate("REWE Online", "Scheduled delivery", "In Stock • 4.7★", 1.07, "https://shop.rewe.de/productList?search={query}"),
      )
      "CA" -> listOf(
        RetailerTemplate("Amazon.ca", "Free Prime Shipping", "In Stock • 4.8★", 0.96, "https://www.amazon.ca/s?k={query}"),
        RetailerTemplate("Walmart Canada", "Free store pickup", "In Stock • 4.5★", 0.99, "https://www.walmart.ca/search?q={query}"),
        RetailerTemplate("Loblaws Online", "PC Express pickup", "In Stock • 4.6★", 1.05, "https://www.loblaws.ca/search?search-bar={query}"),
        RetailerTemplate("Well.ca", "Free shipping $35+", "In Stock • 4.7★", 1.09, "https://well.ca/searchresult.html?keyword={query}"),
      )
      "AU" -> listOf(
        RetailerTemplate("Amazon AU", "Free Prime Delivery", "In Stock • 4.8★", 0.95, "https://www.amazon.com.au/s?k={query}"),
        RetailerTemplate("Woolworths", "Direct to Boot pickup", "In Stock • 4.6★", 1.01, "https://www.woolworths.com.au/shop/search/products?searchTerm={query}"),
        RetailerTemplate("Coles Online", "Same-day delivery", "In Stock • 4.5★", 1.03, "https://www.coles.com.au/search?q={query}"),
        RetailerTemplate("Catch.com.au", "Standard shipping", "Limited Stock • 4.4★", 1.08, "https://www.catch.com.au/search?query={query}"),
      )
      "BD" -> listOf(
        RetailerTemplate("Chaldal", "1-hour Dhaka delivery", "In Stock • 4.8★", 0.95, "https://chaldal.com/search/{query}"),
        RetailerTemplate("Daraz Mall", "Official store shipping", "In Stock • 4.6★", 0.98, "https://www.daraz.com.bd/catalog/?q={query}"),
        RetailerTemplate("Shwapno Online", "Same-day slot delivery", "In Stock • 4.5★", 1.03, "https://www.shwapno.com/search?q={query}"),
        RetailerTemplate("Pandamart", "25-min express drop", "Limited Stock • 4.7★", 1.07, "https://www.foodpanda.com.bd/"),
      )
      else -> listOf(
        RetailerTemplate("Amazon", "Free Prime 1-Day Delivery", "In Stock • 4.8★", 0.95, "https://www.amazon.com/s?k={query}"),
        RetailerTemplate("Walmart.com", "Free pickup today", "In Stock • 4.6★", 0.98, "https://www.walmart.com/search?q={query}"),
        RetailerTemplate("Target", "Order Pickup in 2 hrs", "In Stock • 4.7★", 1.02, "https://www.target.com/s?searchTerm={query}"),
        RetailerTemplate("Instacart Market", "Express 1-hr delivery", "In Stock • 4.5★", 1.06, "https://www.instacart.com/store/s?k={query}"),
        RetailerTemplate("Whole Foods Online", "Prime member savings", "Limited Stock • 4.8★", 1.11, "https://www.amazon.com/s?k={query}&i=wholefoods"),
      )
    }
  }
}
