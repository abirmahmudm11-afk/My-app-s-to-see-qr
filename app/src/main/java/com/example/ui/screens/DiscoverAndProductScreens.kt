package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.AddCircleOutline
import androidx.compose.material.icons.rounded.CheckCircle
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
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
import com.example.ui.components.GuestModeBanner
import com.example.ui.components.ProductSummaryCard
import com.example.ui.components.SectionHeader
import com.example.ui.components.VerificationLabel
import com.example.ui.theme.AmberNotice
import com.example.ui.theme.AmberSoftTint
import com.example.ui.theme.CobaltInfo
import com.example.ui.theme.CobaltSoftTint
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.CrimsonSoftTint
import com.example.ui.theme.DeepObsidian
import com.example.ui.theme.ElectricLime
import com.example.ui.theme.EmeraldSoftTint
import com.example.ui.theme.EmeraldTrust
import com.example.ui.theme.HeaderEmeraldCobaltBrush
import com.example.ui.theme.HeroObsidianEmeraldBrush
import com.example.ui.theme.ObsidianOnLime
import com.example.ui.theme.TangerineAccent
import com.example.ui.theme.TangerineSoftTint
import com.example.ui.theme.TealVivid
import com.example.ui.theme.VioletAccent
import com.example.ui.theme.VioletSoftTint
import com.example.ui.viewmodel.MainUiState
import com.example.ui.viewmodel.ProductDetailViewModel
import kotlinx.coroutines.launch

@Composable
fun DiscoverScreen(
  uiState: MainUiState,
  onSearch: (String) -> Unit,
  onOpenProduct: (String) -> Unit,
  onOpenScan: () -> Unit,
) {
  var query by remember { mutableStateOf("") }
  val displayList = if (query.trim().isNotEmpty()) uiState.searchResults else uiState.cachedCatalog

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    // Rich Gradient Discover Banner
    item {
      Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .background(HeaderEmeraldCobaltBrush)
            .padding(16.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
          Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Surface(color = ElectricLime, shape = RoundedCornerShape(8.dp)) {
              Text(
                "OPEN CATALOG ENGINE",
                style = MaterialTheme.typography.labelSmall,
                color = ObsidianOnLime,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
              )
            }
            Text(
              "Discover Catalog",
              style = MaterialTheme.typography.headlineMedium,
              color = Color.White,
            )
            Text(
              "Search Open Food Facts, Firestore verified merchants, or local offline cache.",
              style = MaterialTheme.typography.bodySmall,
              color = Color.White.copy(alpha = 0.86f),
            )
          }
          Image(
            painter = painterResource(id = R.drawable.img_discover_basket),
            contentDescription = "Discover grocery basket",
            modifier = Modifier
              .size(68.dp)
              .clip(RoundedCornerShape(16.dp)),
            contentScale = ContentScale.Crop,
          )
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
          onValueChange = { query = it },
          placeholder = { Text("Product name, brand, or GTIN barcode") },
          leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = EmeraldTrust) },
          singleLine = true,
          keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
          keyboardActions = KeyboardActions(onSearch = { onSearch(query) }),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier.weight(1f),
        )
        Button(
          onClick = { onSearch(query) },
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier.height(54.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = EmeraldTrust,
            contentColor = Color.White,
          ),
        ) {
          Text("Go", fontWeight = FontWeight.Bold)
        }
      }
    }

    item {
      val chipStyles = listOf(
        Triple("Organic Oats", EmeraldSoftTint, EmeraldTrust),
        Triple("Sparkling Water", CobaltSoftTint, CobaltInfo),
        Triple("3017620422003", VioletSoftTint, VioletAccent),
        Triple("5449000000996", TangerineSoftTint, TangerineAccent),
      )
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        chipStyles.forEach { (quick, bg, fg) ->
          Surface(
            modifier = Modifier
              .clip(RoundedCornerShape(10.dp))
              .clickable {
                query = quick
                onSearch(quick)
              },
            color = bg,
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, fg.copy(alpha = 0.28f)),
          ) {
            Text(
              quick,
              style = MaterialTheme.typography.labelSmall,
              color = fg,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            )
          }
        }
      }
    }

    if (uiState.searchError != null && query.isNotBlank()) {
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = AmberSoftTint),
          border = BorderStroke(1.dp, AmberNotice.copy(alpha = 0.45f)),
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Icon(Icons.Rounded.CloudOff, contentDescription = null, tint = AmberNotice)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
              Text("Catalog Search Notice", style = MaterialTheme.typography.titleSmall, color = AmberNotice)
              Text(uiState.searchError, style = MaterialTheme.typography.bodySmall, color = DeepObsidian)
            }
            TextButton(onClick = { onSearch(query) }) {
              Text("Retry", color = AmberNotice, fontWeight = FontWeight.Bold)
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
          CircularProgressIndicator(color = EmeraldTrust)
        }
      }
    } else if (displayList.isEmpty()) {
      item {
        val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
        Card(
          shape = RoundedCornerShape(22.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = BorderStroke(1.dp, CobaltInfo.copy(alpha = 0.3f)),
        ) {
          Column(modifier = Modifier.fillMaxWidth()) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .background(
                  Brush.horizontalGradient(
                    if (isDark) listOf(Color(0xFF13263F), Color(0xFF112E28)) else listOf(CobaltSoftTint, EmeraldSoftTint),
                  ),
                )
                .padding(16.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
              Image(
                painter = painterResource(id = R.drawable.img_discover_basket),
                contentDescription = "Browse products",
                modifier = Modifier
                  .size(76.dp)
                  .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop,
              )
              Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                  text = if (query.isBlank()) "Start exploring verified products" else "No matching products",
                  style = MaterialTheme.typography.titleMedium,
                )
                Text(
                  text = "Scan any retail barcode or tap a quick search tag above to load nutrition facts and compare prices.",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            }
            Row(
              modifier = Modifier.padding(14.dp),
              horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              Button(
                onClick = {
                  query = "3017620422003"
                  onSearch("3017620422003")
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CobaltInfo, contentColor = Color.White),
              ) {
                Text("Search Sample", fontWeight = FontWeight.Bold)
              }
              Button(
                onClick = onOpenScan,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ElectricLime, contentColor = ObsidianOnLime),
              ) {
                Icon(Icons.Rounded.QrCodeScanner, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Open Scanner", fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }
    } else {
      item {
        SectionHeader(
          title = if (query.isBlank()) "Cached Products (${displayList.size})" else "Results (${displayList.size})",
          accentColor = CobaltInfo,
        )
      }
      items(displayList, key = { it.barcode }) { product ->
        ProductSummaryCard(
          product = product,
          onClick = { onOpenProduct(product.barcode) },
        )
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

  LazyColumn(
    state = listState,
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .testTag("product_detail_screen"),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    item {
      Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .background(HeroObsidianEmeraldBrush)
            .padding(horizontal = 8.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White)
          }
          Text("Product Specs & Offers", style = MaterialTheme.typography.titleLarge, color = Color.White)
          IconButton(onClick = shareProductAction) {
            Icon(Icons.Rounded.Share, contentDescription = "Share product details", tint = ElectricLime)
          }
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
          CircularProgressIndicator(color = EmeraldTrust)
        }
      }
    } else if (product == null) {
      if (detailState.networkError != null) {
        item {
          Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = AmberSoftTint),
            border = BorderStroke(1.dp, AmberNotice.copy(alpha = 0.45f)),
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Rounded.CloudOff, contentDescription = null, tint = AmberNotice)
                Text("Network Lookup Unreachable", style = MaterialTheme.typography.titleMedium, color = AmberNotice)
              }
              Text(detailState.networkError.orEmpty(), style = MaterialTheme.typography.bodySmall, color = DeepObsidian)
              OutlinedButton(
                onClick = { productDetailViewModel.load(barcode, market.countryCode) },
                shape = RoundedCornerShape(12.dp),
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
        Card(
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        ) {
          Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Surface(color = VioletSoftTint, shape = RoundedCornerShape(8.dp)) {
              Text(
                "UNLISTED BARCODE • $barcode",
                style = MaterialTheme.typography.labelSmall,
                color = VioletAccent,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
              )
            }
            Text("Unlisted Barcode: $barcode", style = MaterialTheme.typography.titleLarge)
            Text(
              "Compare live offers online immediately or contribute an unverified community entry below.",
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
            Button(
              onClick = triggerCompareOffers,
              modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("compare_offers_button"),
              shape = RoundedCornerShape(14.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = ElectricLime,
                contentColor = ObsidianOnLime,
              ),
            ) {
              Icon(Icons.Rounded.CompareArrows, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(Modifier.width(8.dp))
              Text("Compare Offers", fontWeight = FontWeight.Bold)
            }
            Button(
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
              shape = RoundedCornerShape(14.dp),
            ) {
              Text(if (isSignedIn) "Save Community Product (Unverified)" else "Sign In to Contribute Product")
            }
          }
        }
      }
      if (detailState.hasComparedOffers) {
        item {
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
        val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
        Card(
          shape = RoundedCornerShape(22.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
          border = BorderStroke(1.dp, TealVivid.copy(alpha = 0.35f)),
        ) {
          Column(modifier = Modifier.fillMaxWidth()) {
            // Color-rich Hero Product Strip
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .background(
                  Brush.horizontalGradient(
                    if (isDark) listOf(Color(0xFF0E2B24), Color(0xFF142642)) else listOf(EmeraldSoftTint, CobaltSoftTint),
                  ),
                )
                .padding(16.dp),
              horizontalArrangement = Arrangement.spacedBy(14.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              if (product.imageUrl.isNotBlank()) {
                AsyncImage(
                  model = product.imageUrl,
                  contentDescription = product.name,
                  modifier = Modifier
                    .size(86.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White),
                  contentScale = ContentScale.Crop,
                )
              } else {
                Box(
                  modifier = Modifier
                    .size(86.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.linearGradient(listOf(EmeraldTrust, CobaltInfo))),
                  contentAlignment = Alignment.Center,
                ) {
                  Icon(Icons.Rounded.QrCode2, contentDescription = null, tint = Color.White, modifier = Modifier.size(36.dp))
                }
              }
              Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                VerificationLabel(statusRaw = product.verificationStatus)
                Text(product.name, style = MaterialTheme.typography.headlineMedium)
                Text(
                  listOfNotNull(
                    product.brand.takeIf { it.isNotBlank() },
                    product.category.takeIf { it.isNotBlank() },
                  ).joinToString(" • "),
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            }

            Column(
              modifier = Modifier.padding(18.dp),
              verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(10.dp),
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 9.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Text("GTIN / Code: ${product.barcode}", style = MaterialTheme.typography.labelMedium)
                  Surface(color = EmeraldTrust, shape = RoundedCornerShape(6.dp)) {
                    Text(
                      if (detailState.fromOfflineCache) "Offline Cache" else product.dataProvider,
                      style = MaterialTheme.typography.labelSmall,
                      color = Color.White,
                      modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                    )
                  }
                }
              }

              if (product.description.isNotBlank()) {
                Text(product.description, style = MaterialTheme.typography.bodyMedium)
              }

              val meta = detailState.apiMetadata
              val gradeBadges = buildList {
                if (meta.quantity.isNotBlank()) add(Triple("Pack: ${meta.quantity}", CobaltSoftTint, CobaltInfo))
                if (meta.nutriscoreGrade.isNotBlank()) {
                  val (bg, fg) = scoreBadgeColors(meta.nutriscoreGrade)
                  add(Triple("Nutri-Score: ${meta.nutriscoreGrade}", bg, fg))
                }
                if (meta.ecoscoreGrade.isNotBlank()) {
                  val (bg, fg) = scoreBadgeColors(meta.ecoscoreGrade)
                  add(Triple("Eco-Score: ${meta.ecoscoreGrade}", bg, fg))
                }
                if (product.countryOfOrigin.isNotBlank()) add(Triple("Origin: ${product.countryOfOrigin}", VioletSoftTint, VioletAccent))
              }
              if (gradeBadges.isNotEmpty()) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                  gradeBadges.forEach { (badge, bg, fg) ->
                    Surface(
                      color = bg,
                      shape = RoundedCornerShape(9.dp),
                      border = BorderStroke(1.dp, fg.copy(alpha = 0.35f)),
                    ) {
                      Text(
                        text = badge,
                        style = MaterialTheme.typography.labelSmall,
                        color = fg,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                      )
                    }
                  }
                }
              }

              if (meta.stores.isNotEmpty()) {
                Text(
                  text = "Known retailers (API): ${meta.stores.take(5).joinToString(", ")}",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }

              if (product.attributionText.isNotBlank()) {
                Text(
                  product.attributionText,
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }

              Button(
                onClick = triggerCompareOffers,
                modifier = Modifier
                  .fillMaxWidth()
                  .height(50.dp)
                  .testTag("compare_offers_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                  containerColor = ElectricLime,
                  contentColor = ObsidianOnLime,
                ),
              ) {
                Icon(Icons.Rounded.CompareArrows, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                  text = if (detailState.isComparingOffers) "Searching Offers..." else "Compare Offers",
                  fontWeight = FontWeight.Bold,
                )
              }

              Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                  onClick = shareProductAction,
                  modifier = Modifier.weight(1f),
                  shape = RoundedCornerShape(12.dp),
                ) {
                  Icon(Icons.Rounded.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(Modifier.width(6.dp))
                  Text("Share Item")
                }
                Button(
                  onClick = {
                    if (isSignedIn) onOpenReceiptSubmit(null) else onSignInRequested()
                  },
                  modifier = Modifier.weight(1f),
                  shape = RoundedCornerShape(12.dp),
                  colors = ButtonDefaults.buttonColors(containerColor = CobaltInfo, contentColor = Color.White),
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

      if (detailState.hasComparedOffers) {
        item {
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

      // Eligible Partner Campaigns
      if (mainUiState.campaigns.isNotEmpty()) {
        item { SectionHeader("Campaign Eligibility (${market.countryCode})", accentColor = TangerineAccent) }
        items(mainUiState.campaigns, key = { it.id }) { campaign ->
          val eligibility = CampaignEligibilityEngine.evaluate(
            campaign = campaign,
            userCountryCode = market.countryCode,
            product = product,
            userTransactions = mainUiState.transactions,
          )
          Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, if (eligibility.eligible) EmeraldTrust.copy(alpha = 0.45f) else MaterialTheme.colorScheme.outline),
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
                  eligibility.reason,
                  style = MaterialTheme.typography.bodySmall,
                  color = if (eligibility.eligible) EmeraldTrust else MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
              if (eligibility.eligible) {
                Button(
                  onClick = { if (isSignedIn) onOpenReceiptSubmit(campaign.id) else onSignInRequested() },
                  shape = RoundedCornerShape(10.dp),
                  colors = ButtonDefaults.buttonColors(containerColor = EmeraldSoftTint, contentColor = EmeraldTrust),
                  contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                ) {
                  Text(if (isSignedIn) "Claim +${campaign.rewardPoints}" else "Sign In", fontWeight = FontWeight.Bold)
                }
              }
            }
          }
        }
      }

      // Regional Store Offers
      item {
        SectionHeader(
          title = "Regional Offers (${market.countryCode})",
          actionLabel = "+ Report Price",
          onAction = {
            if (isSignedIn) showAddOfferDialog = true else onSignInRequested()
          },
          accentColor = EmeraldTrust,
        )
      }

      if (detailState.offers.isEmpty()) {
        item {
          Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
          ) {
            Column(
              modifier = Modifier.padding(16.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Text(
                "No store prices reported for ${market.countryName} yet.",
                style = MaterialTheme.typography.bodyMedium,
              )
              OutlinedButton(
                onClick = {
                  if (isSignedIn) showAddOfferDialog = true else onSignInRequested()
                },
                shape = RoundedCornerShape(12.dp),
              ) {
                Icon(Icons.Rounded.AddCircleOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(if (isSignedIn) "Report Unverified Store Price" else "Sign In to Report Price")
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

  if (showAddOfferDialog) {
    AlertDialog(
      onDismissRequest = { showAddOfferDialog = false },
      title = { Text("Report Price Observation") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            "Community price observations are labeled Unverified until matched by a verified merchant feed.",
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
        Button(
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
          Text("Submit")
        }
      },
      dismissButton = {
        TextButton(onClick = { showAddOfferDialog = false }) { Text("Cancel") }
      },
    )
  }
}

private fun scoreBadgeColors(grade: String): Pair<Color, Color> = when (grade.trim().uppercase()) {
  "A" -> EmeraldSoftTint to EmeraldTrust
  "B" -> Color(0xFFECFCCB) to Color(0xFF3F6212)
  "C" -> AmberSoftTint to AmberNotice
  "D" -> TangerineSoftTint to TangerineAccent
  "E" -> CrimsonSoftTint to CrimsonAlert
  else -> CobaltSoftTint to CobaltInfo
}

@Composable
private fun OfferRowCard(
  offer: ProductOffer,
  onClick: () -> Unit,
  onShare: () -> Unit,
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
  ) {
    Row(
      modifier = Modifier.padding(14.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(offer.storeName, style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
          Surface(
            color = if (offer.verified) EmeraldSoftTint else AmberSoftTint,
            shape = RoundedCornerShape(6.dp),
          ) {
            Text(
              text = if (offer.verified) "Verified Store" else "Unverified Report",
              style = MaterialTheme.typography.labelSmall,
              color = if (offer.verified) EmeraldTrust else AmberNotice,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            )
          }
          Text(
            OfferAvailability.fromString(offer.availabilityStatus).label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          MarketConfig.formatMoney(offer.price, offer.currencyCode),
          style = MaterialTheme.typography.titleLarge,
          color = EmeraldTrust,
          fontWeight = FontWeight.Bold,
        )
        IconButton(onClick = onShare) {
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
  val product = detailState.product

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      IconButton(onClick = onBack) {
        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
      }
      Text("Store Offer Detail", style = MaterialTheme.typography.titleLarge)
      IconButton(
        onClick = {
          val text = if (offer != null) {
            "${product?.name ?: barcode} at ${offer.storeName}: ${MarketConfig.formatMoney(offer.price, offer.currencyCode)} (${if (offer.verified) "Verified" else "Unverified"})"
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

    if (offer == null) {
      Text("Loading offer details...", style = MaterialTheme.typography.bodyMedium)
    } else {
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, EmeraldTrust.copy(alpha = 0.4f)),
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(18.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Rounded.LocalOffer, contentDescription = null, tint = EmeraldTrust)
            Text(offer.storeName, style = MaterialTheme.typography.headlineMedium)
          }
          Text(
            MarketConfig.formatMoney(offer.price, offer.currencyCode),
            style = MaterialTheme.typography.displayMedium,
            color = EmeraldTrust,
          )
          Text(
            text = "Product: ${product?.name ?: barcode}",
            style = MaterialTheme.typography.bodyMedium,
          )
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = if (offer.verified) EmeraldTrust else AmberNotice, modifier = Modifier.size(16.dp))
            Text(
              text = if (offer.verified) "Verified Merchant Price" else "Community Price Observation (Unverified)",
              style = MaterialTheme.typography.bodySmall,
            )
          }
          if (offer.offerUrl.startsWith("http", ignoreCase = true)) {
            Button(
              onClick = {
                runCatching {
                  context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(offer.offerUrl)))
                }
              },
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(14.dp),
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

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("offer_comparison_results_card"),
    shape = RoundedCornerShape(22.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    border = BorderStroke(1.5.dp, EmeraldTrust.copy(alpha = 0.55f)),
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
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Surface(color = EmeraldSoftTint, shape = RoundedCornerShape(10.dp)) {
            Icon(
              Icons.Rounded.CompareArrows,
              contentDescription = null,
              tint = EmeraldTrust,
              modifier = Modifier.padding(6.dp).size(18.dp),
            )
          }
          Column {
            Text("Price Comparison Search", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
              "Live Open Prices API & Store Offers",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
        IconButton(onClick = onRetryCompare) {
          Icon(Icons.Rounded.Refresh, contentDescription = "Refresh price comparison", tint = EmeraldTrust)
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
          CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = EmeraldTrust)
          Spacer(Modifier.width(10.dp))
          Text("Searching Open Prices & store offers...", style = MaterialTheme.typography.bodySmall)
        }
      } else {
        if (detailState.compareNotice != null) {
          Surface(
            color = AmberSoftTint,
            shape = RoundedCornerShape(10.dp),
          ) {
            Text(
              text = detailState.compareNotice,
              style = MaterialTheme.typography.bodySmall,
              color = AmberNotice,
              modifier = Modifier.padding(10.dp),
            )
          }
        }

        if (detailState.comparedOffers.isEmpty()) {
          Text(
            text = "No crowd-sourced Open Prices sightings found for barcode $barcode yet. Use the web price search buttons below to compare live retailers.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        } else {
          val best = detailState.comparedOffers.first()
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(14.dp))
              .background(Brush.horizontalGradient(listOf(EmeraldTrust, CobaltInfo)))
              .padding(14.dp),
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text("BEST OBSERVED PRICE", style = MaterialTheme.typography.labelSmall, color = ElectricLime, fontWeight = FontWeight.Bold)
                Text(best.storeName, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
              }
              Text(
                text = MarketConfig.formatMoney(best.price, best.currencyCode),
                style = MaterialTheme.typography.titleLarge,
                color = ElectricLime,
                fontWeight = FontWeight.Bold,
              )
            }
          }

          detailState.comparedOffers.take(8).forEachIndexed { index, offer ->
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
          }
        }

        Text(
          text = "Search Better Prices Online",
          style = MaterialTheme.typography.labelLarge,
        )
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Button(
            onClick = { onOpenUrl(googleShoppingUrl) },
            modifier = Modifier
              .weight(1f)
              .testTag("search_google_shopping_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CobaltInfo, contentColor = Color.White),
          ) {
            Icon(Icons.Rounded.Search, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text("Shopping", maxLines = 1)
          }
          Button(
            onClick = { onOpenUrl(webPriceSearchUrl) },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = VioletSoftTint, contentColor = VioletAccent),
          ) {
            Icon(Icons.AutoMirrored.Rounded.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text("Web Prices", maxLines = 1)
          }
          Button(
            onClick = { onOpenUrl(openPricesUrl) },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldSoftTint, contentColor = EmeraldTrust),
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
  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick),
    shape = RoundedCornerShape(12.dp),
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    border = BorderStroke(
      1.dp,
      if (isBest) EmeraldTrust.copy(alpha = 0.55f) else MaterialTheme.colorScheme.outline,
    ),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          Text(offer.storeName, style = MaterialTheme.typography.titleSmall)
          if (isBest) {
            Surface(color = EmeraldSoftTint, shape = RoundedCornerShape(6.dp)) {
              Text(
                "Lowest",
                style = MaterialTheme.typography.labelSmall,
                color = EmeraldTrust,
                fontWeight = FontWeight.Bold,
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
          ).joinToString(" • "),
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
      Column(horizontalAlignment = Alignment.End) {
        Text(
          text = MarketConfig.formatMoney(offer.price, offer.currencyCode),
          style = MaterialTheme.typography.titleMedium,
          color = EmeraldTrust,
          fontWeight = FontWeight.Bold,
        )
        if (offer.isDiscounted && offer.originalPrice != null && offer.originalPrice > offer.price) {
          Text(
            text = MarketConfig.formatMoney(offer.originalPrice, offer.currencyCode),
            style = MaterialTheme.typography.labelSmall.copy(textDecoration = TextDecoration.LineThrough),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
    }
  }
}
