package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.AddCircleOutline
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.CompareArrows
import androidx.compose.material.icons.rounded.LocalOffer
import androidx.compose.material.icons.rounded.QrCode2
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.OfferAvailability
import com.example.data.model.ProductOffer
import com.example.data.repository.ComparedOffer
import com.example.domain.CampaignEligibilityEngine
import com.example.domain.MarketConfig
import com.example.ui.components.AnimatedButton
import com.example.ui.components.AnimatedIconButton
import com.example.ui.components.AnimatedOutlinedButton
import com.example.ui.components.AnimatedTextButton
import com.example.ui.components.GuestModeBanner
import com.example.ui.components.ProductSummaryCard
import com.example.ui.components.SectionHeader
import com.example.ui.components.VerificationLabel
import com.example.ui.components.atmosphericBackdrop
import com.example.ui.components.bounceClickable
import com.example.ui.components.sharedProductBounds
import com.example.ui.theme.TabularDataStyle
import com.example.ui.viewmodel.MainUiState
import com.example.ui.viewmodel.ProductDetailViewModel
import kotlinx.coroutines.launch

private data class CatalogCategorySpec(
  val label: String,
  val apiTag: String?,
  val fallbackKeyword: String,
)

@Composable
fun DiscoverScreen(
  uiState: MainUiState,
  onSearch: (String) -> Unit,
  onSelectCategory: (String?, String) -> Unit = { _, _ -> },
  onRefreshCatalog: () -> Unit = {},
  onOpenProduct: (String) -> Unit,
  onOpenScan: () -> Unit,
) {
  var query by remember { mutableStateOf("") }
  var selectedCategoryLabel by remember { mutableStateOf("All") }
  val categories = remember {
    listOf(
      CatalogCategorySpec("All", null, ""),
      CatalogCategorySpec("Beverages", "en:beverages", "Beverages"),
      CatalogCategorySpec("Snacks", "en:snacks", "Snacks"),
      CatalogCategorySpec("Breakfast", "en:breakfasts", "Breakfast"),
      CatalogCategorySpec("Dairy & Plant", "en:dairies", "Oatly"),
      CatalogCategorySpec("Pantry", "en:pastas", "Pasta"),
    )
  }

  val isFiltering = query.trim().isNotEmpty() || selectedCategoryLabel != "All"
  val displayList = if (isFiltering) uiState.searchResults else uiState.cachedCatalog

  BoxWithConstraints(
    modifier = Modifier
      .fillMaxSize()
      .atmosphericBackdrop(),
    contentAlignment = Alignment.TopCenter,
  ) {
    val isWide = maxWidth >= 680.dp

    LazyColumn(
      modifier = Modifier
        .widthIn(max = 860.dp)
        .fillMaxSize(),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
              text = "Discover Catalog",
              style = MaterialTheme.typography.displayMedium,
              color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
              text = "Live Open Food Facts database, prices & local cache.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
          Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = CircleShape,
            border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.65f)),
          ) {
            AnimatedIconButton(
              onClick = {
                query = ""
                selectedCategoryLabel = "All"
                onSelectCategory(null, "")
                onRefreshCatalog()
              },
            ) {
              Icon(
                Icons.Rounded.Refresh,
                contentDescription = "Refresh live catalog",
                tint = MaterialTheme.colorScheme.primary,
              )
            }
          }
        }
      }

      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          OutlinedTextField(
            value = query,
            onValueChange = { next ->
              query = next
              if (next.isEmpty() && selectedCategoryLabel == "All") {
                onSearch("")
              }
            },
            placeholder = { Text("Name, brand, or GTIN barcode") },
            leadingIcon = {
              Icon(
                Icons.Rounded.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            },
            trailingIcon = {
              if (query.isNotEmpty()) {
                AnimatedIconButton(
                  onClick = {
                    query = ""
                    selectedCategoryLabel = "All"
                    onSearch("")
                  },
                ) {
                  Icon(Icons.Rounded.Close, contentDescription = "Clear search")
                }
              }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
              onSearch = {
                selectedCategoryLabel = "All"
                onSearch(query)
              },
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.weight(1f),
          )
          AnimatedButton(
            onClick = {
              selectedCategoryLabel = "All"
              onSearch(query)
            },
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.height(54.dp),
          ) {
            Text("Search")
          }
        }
      }

      item {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          categories.forEach { cat ->
            val selected = selectedCategoryLabel == cat.label && query.isBlank()
            Surface(
              modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .bounceClickable {
                  query = ""
                  selectedCategoryLabel = cat.label
                  onSelectCategory(cat.apiTag, cat.fallbackKeyword)
                },
              color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
              shape = RoundedCornerShape(999.dp),
              border = BorderStroke(
                width = 0.75.dp,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.65f),
              ),
            ) {
              Text(
                text = cat.label,
                style = MaterialTheme.typography.labelLarge.copy(
                  fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                ),
                color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
              )
            }
          }
        }
      }

      if (uiState.searchError != null && query.isNotBlank()) {
        item {
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.tertiaryContainer,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f)),
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              Icon(Icons.Rounded.CloudOff, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
              Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Search Notice", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onTertiaryContainer)
                Text(uiState.searchError, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onTertiaryContainer)
              }
              AnimatedTextButton(onClick = { onSearch(query) }) {
                Text("Retry")
              }
            }
          }
        }
      }

      if (uiState.isSearching) {
        item {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 24.dp),
            horizontalArrangement = Arrangement.Center,
          ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
          }
        }
      } else if (displayList.isEmpty()) {
        item {
          Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
          ) {
            Column(
              modifier = Modifier.padding(16.dp),
              verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              Text(
                text = if (query.isBlank()) "Explore verified products" else "No matching products",
                style = MaterialTheme.typography.titleMedium,
              )
              Text(
                text = "Scan a barcode or search a sample item to view nutrition details and store prices.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
              Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AnimatedOutlinedButton(
                  onClick = {
                    query = "3017620422003"
                    onSearch("3017620422003")
                  },
                  modifier = Modifier.weight(1f),
                  shape = RoundedCornerShape(10.dp),
                ) {
                  Text("Try Sample")
                }
                AnimatedButton(
                  onClick = onOpenScan,
                  modifier = Modifier.weight(1f),
                  shape = RoundedCornerShape(10.dp),
                ) {
                  Icon(Icons.Rounded.QrCodeScanner, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(Modifier.width(6.dp))
                  Text("Open Scanner")
                }
              }
            }
          }
        }
      } else {
        item {
          SectionHeader(
            title = if (!isFiltering) {
              "Live & Cached Products (${displayList.size})"
            } else if (query.isNotBlank()) {
              "Results (${displayList.size})"
            } else {
              "$selectedCategoryLabel (${displayList.size})"
            },
          )
        }
        if (isWide) {
          val rows = displayList.chunked(2)
          items(rows) { pair ->
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              pair.forEach { product ->
                ProductSummaryCard(
                  product = product,
                  onClick = { onOpenProduct(product.barcode) },
                  modifier = Modifier.weight(1f),
                )
              }
              if (pair.size == 1) {
                Spacer(Modifier.weight(1f))
              }
            }
          }
        } else {
          items(displayList, key = { it.barcode }) { product ->
            ProductSummaryCard(
              product = product,
              onClick = { onOpenProduct(product.barcode) },
              modifier = Modifier.animateItem(),
            )
          }
        }
      }
    }
  }
}

@Composable
fun ProductDetailScreen(
  barcode: String,
  mainUiState: MainUiState,
  productDetailViewModel: ProductDetailViewModel,
  onBack: () -> Unit,
  onOpenOfferDetail: (String, String) -> Unit,
  onOpenReceiptSubmit: (String?) -> Unit,
  onSignInRequested: () -> Unit,
  onShowMessage: (String) -> Unit,
) {
  val context = LocalContext.current
  val detailState by productDetailViewModel.uiState.collectAsStateWithLifecycle()
  val market = MarketConfig.findByCountry(mainUiState.preferences.countryCode)
  val isSignedIn = mainUiState.firebaseUser != null
  val listState = rememberLazyListState()
  val scope = rememberCoroutineScope()

  var showAddOfferDialog by remember { mutableStateOf(false) }
  var storeInput by remember { mutableStateOf("") }
  var priceInput by remember { mutableStateOf("") }
  var offerUrlInput by remember { mutableStateOf("") }

  var newName by remember { mutableStateOf("") }
  var newBrand by remember { mutableStateOf("") }
  var newCategory by remember { mutableStateOf("") }
  var newDescription by remember { mutableStateOf("") }

  LaunchedEffect(barcode, market.countryCode) {
    productDetailViewModel.load(barcode, market.countryCode)
  }

  LaunchedEffect(detailState.feedback) {
    detailState.feedback?.let {
      onShowMessage(it)
      productDetailViewModel.clearFeedback()
    }
  }

  val product = detailState.product
  val triggerCompareOffers: () -> Unit = {
    productDetailViewModel.compareOffers(barcode, market.countryCode)
    scope.launch {
      listState.animateScrollToItem(2)
    }
  }

  val openWebPriceSearch: (String) -> Unit = { url ->
    runCatching {
      context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }.onFailure {
      onShowMessage("No browser available to search offers.")
    }
  }

  val shareProductAction = {
    val lowestOffer = detailState.offers.minByOrNull { it.price }
    val shareBody = buildString {
      append(product?.name ?: "Product Barcode $barcode")
      product?.brand?.takeIf { it.isNotBlank() }?.let { append(" by $it") }
      append("\nBarcode: $barcode")
      lowestOffer?.let {
        append(
          "\nBest observed price: ${MarketConfig.formatMoney(it.price, it.currencyCode)} at ${it.storeName}",
        )
      }
      product?.attributionText?.takeIf { it.isNotBlank() }?.let { append("\nSource: $it") }
    }
    val intent = Intent(Intent.ACTION_SEND).apply {
      type = "text/plain"
      putExtra(Intent.EXTRA_SUBJECT, product?.name ?: "SCAN2MONEY Product $barcode")
      putExtra(Intent.EXTRA_TEXT, shareBody)
    }
    context.startActivity(Intent.createChooser(intent, "Share Product Details"))
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .atmosphericBackdrop(),
    contentAlignment = Alignment.TopCenter,
  ) {
    LazyColumn(
      state = listState,
      modifier = Modifier
        .widthIn(max = 860.dp)
        .fillMaxSize()
        .testTag("product_detail_screen"),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            AnimatedIconButton(onClick = onBack) {
              Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
            }
            Text("Product Details", style = MaterialTheme.typography.titleLarge)
          }
          AnimatedIconButton(onClick = shareProductAction) {
            Icon(Icons.Rounded.Share, contentDescription = "Share product details")
          }
        }
      }

      if (detailState.isLoading) {
        item {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(32.dp),
            contentAlignment = Alignment.Center,
          ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
          }
        }
      } else if (product == null) {
        if (detailState.networkError != null) {
          item {
            Surface(
              shape = RoundedCornerShape(14.dp),
              color = MaterialTheme.colorScheme.tertiaryContainer,
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f)),
            ) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                  Icon(Icons.Rounded.CloudOff, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                  Text("Network Lookup Unreachable", style = MaterialTheme.typography.titleMedium)
                }
                Text(detailState.networkError.orEmpty(), style = MaterialTheme.typography.bodySmall)
                AnimatedOutlinedButton(
                  onClick = { productDetailViewModel.load(barcode, market.countryCode) },
                  shape = RoundedCornerShape(10.dp),
                ) {
                  Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(Modifier.width(6.dp))
                  Text("Retry Lookup")
                }
              }
            }
          }
        }
        if (!isSignedIn) {
          item {
            GuestModeBanner(onSignInClick = onSignInRequested)
          }
        }
        item {
          Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
          ) {
            Column(
              modifier = Modifier.padding(16.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              Text("Unlisted Barcode · $barcode", style = MaterialTheme.typography.titleLarge)
              Text(
                "Compare online prices right away or add an unverified community entry.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
              OutlinedTextField(
                value = newName,
                onValueChange = { newName = it },
                label = { Text("Product Name *") },
                modifier = Modifier.fillMaxWidth(),
              )
              OutlinedTextField(
                value = newBrand,
                onValueChange = { newBrand = it },
                label = { Text("Brand") },
                modifier = Modifier.fillMaxWidth(),
              )
              OutlinedTextField(
                value = newCategory,
                onValueChange = { newCategory = it },
                label = { Text("Category") },
                modifier = Modifier.fillMaxWidth(),
              )
              OutlinedTextField(
                value = newDescription,
                onValueChange = { newDescription = it },
                label = { Text("Notes / Size") },
                modifier = Modifier.fillMaxWidth(),
              )
              AnimatedButton(
                onClick = triggerCompareOffers,
                modifier = Modifier
                  .fillMaxWidth()
                  .height(48.dp)
                  .testTag("compare_offers_button"),
                shape = RoundedCornerShape(10.dp),
              ) {
                Icon(Icons.Rounded.CompareArrows, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Compare Offers")
              }
              AnimatedOutlinedButton(
                onClick = {
                  if (!isSignedIn) {
                    onSignInRequested()
                  } else {
                    productDetailViewModel.submitUnverifiedProduct(
                      barcode = barcode,
                      name = newName,
                      brand = newBrand,
                      category = newCategory,
                      description = newDescription,
                    )
                  }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
              ) {
                Text(if (isSignedIn) "Save Community Product" else "Sign In to Contribute Product")
              }
            }
          }
        }
        item {
          AnimatedVisibility(
            visible = detailState.hasComparedOffers,
            enter = expandVertically(tween(if (mainUiState.preferences.reducedMotionEnabled) 0 else 220, easing = FastOutSlowInEasing)) +
              fadeIn(tween(if (mainUiState.preferences.reducedMotionEnabled) 0 else 180)),
            exit = shrinkVertically() + fadeOut(),
          ) {
            OfferComparisonResultsCard(
              barcode = barcode,
              productName = barcode,
              brand = "",
              detailState = detailState,
              onOpenOfferDetail = { offerId -> onOpenOfferDetail(barcode, offerId) },
              onOpenUrl = openWebPriceSearch,
              onRetryCompare = triggerCompareOffers,
            )
          }
        }
      } else {
        item {
          Surface(
            modifier = Modifier
              .fillMaxWidth()
              .sharedProductBounds("product_card_$barcode"),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
          ) {
            Column(modifier = Modifier.fillMaxWidth()) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                if (product.imageUrl.isNotBlank()) {
                  AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.name,
                    modifier = Modifier
                      .size(80.dp)
                      .clip(RoundedCornerShape(12.dp))
                      .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentScale = ContentScale.Crop,
                  )
                } else {
                  Box(
                    modifier = Modifier
                      .size(80.dp)
                      .clip(RoundedCornerShape(12.dp))
                      .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                  ) {
                    Icon(
                      Icons.Rounded.QrCode2,
                      contentDescription = null,
                      tint = MaterialTheme.colorScheme.primary,
                      modifier = Modifier.size(34.dp),
                    )
                  }
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                  VerificationLabel(statusRaw = product.verificationStatus)
                  Text(product.name, style = MaterialTheme.typography.headlineMedium)
                  Text(
                    listOfNotNull(
                      product.brand.takeIf { it.isNotBlank() },
                      product.category.takeIf { it.isNotBlank() },
                    ).joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }
              }

              HorizontalDivider(color = MaterialTheme.colorScheme.outline)

              Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 12.dp, vertical = 9.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Text(
                    text = "GTIN ${product.barcode}",
                    style = TabularDataStyle,
                    color = MaterialTheme.colorScheme.onSurface,
                  )
                  Text(
                    text = if (detailState.fromOfflineCache) "Offline Cache" else product.dataProvider,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                  )
                }

                if (product.description.isNotBlank()) {
                  Text(product.description, style = MaterialTheme.typography.bodyMedium)
                }

                val meta = detailState.apiMetadata
                val gradeBadges = buildList {
                  if (meta.quantity.isNotBlank()) add("Pack: ${meta.quantity}")
                  if (meta.nutriscoreGrade.isNotBlank()) add("Nutri-Score ${meta.nutriscoreGrade}")
                  if (meta.ecoscoreGrade.isNotBlank()) add("Eco-Score ${meta.ecoscoreGrade}")
                  if (product.countryOfOrigin.isNotBlank()) add("Origin: ${product.countryOfOrigin}")
                }
                if (gradeBadges.isNotEmpty()) {
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                  ) {
                    gradeBadges.forEach { badge ->
                      Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                      ) {
                        Text(
                          text = badge,
                          style = MaterialTheme.typography.labelMedium,
                          color = MaterialTheme.colorScheme.onSurfaceVariant,
                          modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        )
                      }
                    }
                  }
                }

                if (meta.stores.isNotEmpty()) {
                  Text(
                    text = "Listed retailers: ${meta.stores.take(5).joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }

                if (product.attributionText.isNotBlank()) {
                  Text(
                    text = product.attributionText,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }

                AnimatedButton(
                  onClick = triggerCompareOffers,
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("compare_offers_button"),
                  shape = RoundedCornerShape(10.dp),
                ) {
                  Icon(Icons.Rounded.CompareArrows, contentDescription = null, modifier = Modifier.size(18.dp))
                  Spacer(Modifier.width(8.dp))
                  Text(if (detailState.isComparingOffers) "Comparing Offers..." else "Compare Prices")
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                  AnimatedOutlinedButton(
                    onClick = shareProductAction,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                  ) {
                    Icon(Icons.Rounded.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Share Item")
                  }
                  AnimatedOutlinedButton(
                    onClick = {
                      if (isSignedIn) onOpenReceiptSubmit(null) else onSignInRequested()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                  ) {
                    Icon(Icons.AutoMirrored.Rounded.ReceiptLong, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(if (isSignedIn) "Log Receipt" else "Sign In")
                  }
                }
              }
            }
          }
        }

        item {
          AnimatedVisibility(
            visible = detailState.hasComparedOffers,
            enter = expandVertically(tween(if (mainUiState.preferences.reducedMotionEnabled) 0 else 220, easing = FastOutSlowInEasing)) +
              fadeIn(tween(if (mainUiState.preferences.reducedMotionEnabled) 0 else 180)),
            exit = shrinkVertically() + fadeOut(),
          ) {
            OfferComparisonResultsCard(
              barcode = barcode,
              productName = product.name,
              brand = product.brand,
              detailState = detailState,
              onOpenOfferDetail = { offerId -> onOpenOfferDetail(barcode, offerId) },
              onOpenUrl = openWebPriceSearch,
              onRetryCompare = triggerCompareOffers,
            )
          }
        }

        if (mainUiState.campaigns.isNotEmpty()) {
          item { SectionHeader("Campaign Eligibility (${market.countryCode})") }
          items(mainUiState.campaigns, key = { it.id }) { campaign ->
            val eligibility = CampaignEligibilityEngine.evaluate(
              campaign = campaign,
              userCountryCode = market.countryCode,
              product = product,
              userTransactions = mainUiState.transactions,
            )
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = MaterialTheme.colorScheme.surface,
              border = BorderStroke(
                1.dp,
                if (eligibility.eligible) MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline,
              ),
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
              ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                  Text(campaign.title, style = MaterialTheme.typography.titleMedium)
                  Text(
                    text = eligibility.reason,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (eligibility.eligible) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }
                if (eligibility.eligible) {
                  AnimatedButton(
                    onClick = { if (isSignedIn) onOpenReceiptSubmit(campaign.id) else onSignInRequested() },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                      containerColor = MaterialTheme.colorScheme.secondaryContainer,
                      contentColor = MaterialTheme.colorScheme.secondary,
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                  ) {
                    Text(if (isSignedIn) "Claim +${campaign.rewardPoints}" else "Sign In")
                  }
                }
              }
            }
          }
        }

        item {
          SectionHeader(
            title = "Regional Offers (${market.countryCode})",
            actionLabel = "Report Price",
            onAction = {
              if (isSignedIn) showAddOfferDialog = true else onSignInRequested()
            },
          )
        }

        if (detailState.offers.isEmpty()) {
          item {
            Surface(
              shape = RoundedCornerShape(14.dp),
              color = MaterialTheme.colorScheme.surface,
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            ) {
              Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                Text(
                  text = "No store prices reported for ${market.countryName} yet.",
                  style = MaterialTheme.typography.bodyMedium,
                )
                AnimatedOutlinedButton(
                  onClick = {
                    if (isSignedIn) showAddOfferDialog = true else onSignInRequested()
                  },
                  shape = RoundedCornerShape(10.dp),
                ) {
                  Icon(Icons.Rounded.AddCircleOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(Modifier.width(6.dp))
                  Text(if (isSignedIn) "Report Store Price" else "Sign In to Report Price")
                }
              }
            }
          }
        } else {
          items(detailState.offers, key = { it.id }) { offer ->
            OfferRowCard(
              offer = offer,
              onClick = { onOpenOfferDetail(barcode, offer.id) },
              onShare = {
                val shareText = "${product.name} ($barcode) at ${offer.storeName}: ${MarketConfig.formatMoney(offer.price, offer.currencyCode)}"
                val intent = Intent(Intent.ACTION_SEND).apply {
                  type = "text/plain"
                  putExtra(Intent.EXTRA_TEXT, shareText)
                }
                context.startActivity(Intent.createChooser(intent, "Share Store Offer"))
              },
            )
          }
        }
      }
    }
  }

  if (showAddOfferDialog) {
    AlertDialog(
      onDismissRequest = { showAddOfferDialog = false },
      title = { Text("Report Price Observation") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            "Community prices are labeled Unverified until matched by a verified merchant feed.",
            style = MaterialTheme.typography.bodySmall,
          )
          OutlinedTextField(
            value = storeInput,
            onValueChange = { storeInput = it },
            label = { Text("Store Name *") },
            modifier = Modifier.fillMaxWidth(),
          )
          OutlinedTextField(
            value = priceInput,
            onValueChange = { priceInput = it },
            label = { Text("Price (${market.currencyCode}) *") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
          )
          OutlinedTextField(
            value = offerUrlInput,
            onValueChange = { offerUrlInput = it },
            label = { Text("Offer Link (optional)") },
            modifier = Modifier.fillMaxWidth(),
          )
        }
      },
      confirmButton = {
        AnimatedButton(
          onClick = {
            val parsedPrice = priceInput.toDoubleOrNull() ?: 0.0
            showAddOfferDialog = false
            productDetailViewModel.submitUnverifiedOffer(
              barcode = barcode,
              storeName = storeInput,
              price = parsedPrice,
              currencyCode = market.currencyCode,
              countryCode = market.countryCode,
              offerUrl = offerUrlInput,
            )
          },
        ) {
          Text("Submit Price")
        }
      },
      dismissButton = {
        AnimatedTextButton(onClick = { showAddOfferDialog = false }) { Text("Cancel") }
      },
    )
  }
}

@Composable
private fun OfferRowCard(
  offer: ProductOffer,
  onClick: () -> Unit,
  onShare: () -> Unit,
) {
  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .bounceClickable(onClick = onClick),
    shape = RoundedCornerShape(12.dp),
    color = MaterialTheme.colorScheme.surface,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
  ) {
    Row(
      modifier = Modifier.padding(14.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(offer.storeName, style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
          Surface(
            color = if (offer.verified) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.tertiaryContainer,
            shape = RoundedCornerShape(6.dp),
          ) {
            Text(
              text = if (offer.verified) "Verified Store" else "Unverified",
              style = MaterialTheme.typography.labelSmall,
              color = if (offer.verified) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.tertiary,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            )
          }
          Text(
            text = OfferAvailability.fromString(offer.availabilityStatus).label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = MarketConfig.formatMoney(offer.price, offer.currencyCode),
          style = TabularDataStyle.copy(
            fontSize = MaterialTheme.typography.titleLarge.fontSize,
            fontWeight = FontWeight.Bold,
          ),
          color = MaterialTheme.colorScheme.secondary,
        )
        AnimatedIconButton(onClick = onShare) {
          Icon(Icons.Rounded.Share, contentDescription = "Share offer", modifier = Modifier.size(18.dp))
        }
      }
    }
  }
}

@Composable
fun OfferDetailScreen(
  barcode: String,
  offerId: String,
  productDetailViewModel: ProductDetailViewModel,
  countryCode: String,
  onBack: () -> Unit,
) {
  val context = LocalContext.current
  val detailState by productDetailViewModel.uiState.collectAsStateWithLifecycle()

  LaunchedEffect(barcode, countryCode) {
    if (detailState.barcode != barcode) {
      productDetailViewModel.load(barcode, countryCode)
    }
  }

  val offer = detailState.offers.firstOrNull { it.id == offerId }
  val comparedOffer = detailState.comparedOffers.firstOrNull {
    it.id == offerId || it.firestoreOfferId == offerId
  }
  val product = detailState.product

  val resolvedStoreName = offer?.storeName ?: comparedOffer?.storeName
  val resolvedPrice = offer?.price ?: comparedOffer?.price
  val resolvedCurrency = offer?.currencyCode ?: comparedOffer?.currencyCode ?: MarketConfig.findByCountry(countryCode).currencyCode
  val resolvedVerified = offer?.verified ?: comparedOffer?.isVerifiedMerchant ?: false
  val resolvedOfferUrl = offer?.offerUrl ?: comparedOffer?.offerUrl.orEmpty()
  val resolvedMetaLabel = when {
    offer != null -> if (offer.verified) "Verified Merchant Price" else "Community Price Report (Unverified)"
    comparedOffer != null -> "${comparedOffer.sourceLabel}${if (comparedOffer.locationLabel.isNotBlank()) " · ${comparedOffer.locationLabel}" else ""}"
    else -> ""
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .atmosphericBackdrop(),
    contentAlignment = Alignment.TopCenter,
  ) {
    Column(
      modifier = Modifier
        .widthIn(max = 860.dp)
        .fillMaxSize()
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          AnimatedIconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
          }
          Text("Store Offer", style = MaterialTheme.typography.titleLarge)
        }
        AnimatedIconButton(
          onClick = {
            val text = if (resolvedStoreName != null && resolvedPrice != null) {
              "${product?.name ?: barcode} at $resolvedStoreName: ${MarketConfig.formatMoney(resolvedPrice, resolvedCurrency)}"
            } else {
              "Check barcode $barcode on SCAN2MONEY"
            }
            val intent = Intent(Intent.ACTION_SEND).apply {
              type = "text/plain"
              putExtra(Intent.EXTRA_TEXT, text)
            }
            context.startActivity(Intent.createChooser(intent, "Share Store Offer"))
          },
        ) {
          Icon(Icons.Rounded.Share, contentDescription = "Share Offer")
        }
      }

      if (resolvedStoreName == null || resolvedPrice == null) {
        if (detailState.isLoading || detailState.isComparingOffers) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 24.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(10.dp))
            Text("Loading offer details...", style = MaterialTheme.typography.bodyMedium)
          }
        } else {
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.65f)),
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              Text("Offer No Longer Listed", style = MaterialTheme.typography.titleLarge)
              Text(
                text = "This store offer could not be loaded for barcode $barcode.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
              AnimatedOutlinedButton(onClick = onBack, shape = RoundedCornerShape(12.dp)) {
                Text("Back to Product")
              }
            }
          }
        }
      } else {
        Surface(
          shape = RoundedCornerShape(18.dp),
          color = MaterialTheme.colorScheme.surface,
          border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.65f)),
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              Icon(Icons.Rounded.LocalOffer, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
              Text(resolvedStoreName, style = MaterialTheme.typography.headlineMedium)
            }
            Text(
              text = MarketConfig.formatMoney(resolvedPrice, resolvedCurrency),
              style = MaterialTheme.typography.displayMedium,
              color = MaterialTheme.colorScheme.secondary,
            )
            Text(
              text = "Product: ${product?.name ?: barcode}",
              style = MaterialTheme.typography.bodyMedium,
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              Icon(
                Icons.Rounded.CheckCircle,
                contentDescription = null,
                tint = if (resolvedVerified) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(16.dp),
              )
              Text(
                text = resolvedMetaLabel,
                style = MaterialTheme.typography.bodySmall,
              )
            }
            if (resolvedOfferUrl.startsWith("http", ignoreCase = true)) {
              AnimatedButton(
                onClick = {
                  runCatching {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(resolvedOfferUrl)))
                  }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
              ) {
                Icon(Icons.AutoMirrored.Rounded.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Open Merchant Offer Page")
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun OfferComparisonResultsCard(
  barcode: String,
  productName: String,
  brand: String,
  detailState: com.example.ui.viewmodel.ProductDetailUiState,
  onOpenOfferDetail: (String) -> Unit,
  onOpenUrl: (String) -> Unit,
  onRetryCompare: () -> Unit,
) {
  val searchQuery = Uri.encode(listOf(brand, productName, barcode).filter { it.isNotBlank() }.joinToString(" "))
  val googleShoppingUrl = "https://www.google.com/search?tbm=shop&q=$searchQuery"
  val webPriceSearchUrl = "https://www.google.com/search?q=$searchQuery+price"
  val openPricesUrl = "https://prices.openfoodfacts.org/products/$barcode"

  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("offer_comparison_results_card"),
    shape = RoundedCornerShape(16.dp),
    color = MaterialTheme.colorScheme.surface,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text("Online Price Comparison", style = MaterialTheme.typography.titleLarge)
          Text(
            text = if (detailState.includesSimulatedRetailers) {
              "Simulated Retailer Quotes + Live Open Prices"
            } else {
              "Live Open Prices & Verified Store Offers"
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        AnimatedIconButton(onClick = onRetryCompare) {
          Icon(Icons.Rounded.Refresh, contentDescription = "Refresh price comparison", tint = MaterialTheme.colorScheme.primary)
        }
      }

      if (detailState.isComparingOffers) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
          horizontalArrangement = Arrangement.Center,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.primary)
          Spacer(Modifier.width(10.dp))
          Text("Checking online retailers for $barcode...", style = MaterialTheme.typography.bodySmall)
        }
      } else {
        if (detailState.compareNotice != null) {
          Surface(
            color = MaterialTheme.colorScheme.tertiaryContainer,
            shape = RoundedCornerShape(8.dp),
          ) {
            Text(
              text = detailState.compareNotice,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onTertiaryContainer,
              modifier = Modifier.padding(10.dp),
            )
          }
        }

        if (detailState.comparedOffers.isEmpty()) {
          Text(
            text = "No retailer prices found for $barcode yet. Use the web links below to compare live retailers.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        } else {
          val best = detailState.comparedOffers.first()
          Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.secondaryContainer,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.45f)),
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                  text = "Lowest Observed Price",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.secondary,
                )
                Text(
                  text = best.storeName,
                  style = MaterialTheme.typography.titleMedium,
                  color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
                if (best.deliveryNote.isNotBlank()) {
                  Text(
                    text = best.deliveryNote,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }
              }
              Text(
                text = MarketConfig.formatMoney(best.price, best.currencyCode),
                style = TabularDataStyle.copy(
                  fontSize = MaterialTheme.typography.titleLarge.fontSize,
                  fontWeight = FontWeight.Bold,
                ),
                color = MaterialTheme.colorScheme.secondary,
              )
            }
          }

          Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
          ) {
            Column {
              val visibleOffers = detailState.comparedOffers.take(8)
              visibleOffers.forEachIndexed { index, offer ->
                ComparedOfferItemRow(
                  offer = offer,
                  isBest = index == 0,
                  onClick = {
                    if (offer.firestoreOfferId != null) {
                      onOpenOfferDetail(offer.firestoreOfferId)
                    } else if (offer.offerUrl.startsWith("http", ignoreCase = true)) {
                      onOpenUrl(offer.offerUrl)
                    }
                  },
                )
                if (index < visibleOffers.lastIndex) {
                  HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                }
              }
            }
          }
        }

        Text(
          text = "External price search",
          style = MaterialTheme.typography.titleSmall,
        )
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          AnimatedOutlinedButton(
            onClick = { onOpenUrl(googleShoppingUrl) },
            modifier = Modifier
              .weight(1f)
              .testTag("search_google_shopping_button"),
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
          ) {
            Text("Shopping", maxLines = 1)
          }
          AnimatedOutlinedButton(
            onClick = { onOpenUrl(webPriceSearchUrl) },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
          ) {
            Text("Web Prices", maxLines = 1)
          }
          AnimatedOutlinedButton(
            onClick = { onOpenUrl(openPricesUrl) },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
          ) {
            Icon(Icons.Rounded.Storefront, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text("OpenPrices", maxLines = 1)
          }
        }
      }
    }
  }
}

@Composable
private fun ComparedOfferItemRow(
  offer: ComparedOffer,
  isBest: Boolean,
  onClick: () -> Unit,
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .bounceClickable(onClick = onClick)
      .padding(horizontal = 14.dp, vertical = 12.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(offer.storeName, style = MaterialTheme.typography.titleSmall)
        if (isBest) {
          Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(4.dp)) {
            Text(
              text = "Best",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.secondary,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            )
          }
        }
        if (offer.isDiscounted && offer.originalPrice != null && offer.originalPrice > offer.price) {
          val pct = (((offer.originalPrice - offer.price) / offer.originalPrice) * 100.0).toInt().coerceAtLeast(1)
          Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(4.dp)) {
            Text(
              text = "-$pct%",
              style = TabularDataStyle.copy(fontSize = MaterialTheme.typography.labelSmall.fontSize),
              color = MaterialTheme.colorScheme.primary,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            )
          }
        }
      }
      Text(
        text = listOfNotNull(
          offer.sourceLabel,
          offer.locationLabel.takeIf { it.isNotBlank() },
          offer.observedDate.takeIf { it.isNotBlank() },
        ).joinToString(" · "),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
    Column(horizontalAlignment = Alignment.End) {
      Text(
        text = MarketConfig.formatMoney(offer.price, offer.currencyCode),
        style = TabularDataStyle.copy(fontWeight = FontWeight.Bold),
        color = if (isBest) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface,
      )
      if (offer.isDiscounted && offer.originalPrice != null && offer.originalPrice > offer.price) {
        Text(
          text = MarketConfig.formatMoney(offer.originalPrice, offer.currencyCode),
          style = TabularDataStyle.copy(
            fontSize = MaterialTheme.typography.labelSmall.fontSize,
            textDecoration = TextDecoration.LineThrough,
          ),
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
  }
}
