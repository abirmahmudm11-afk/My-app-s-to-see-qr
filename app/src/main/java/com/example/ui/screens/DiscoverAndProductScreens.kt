package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.AddCircleOutline
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.LocalOffer
import androidx.compose.material.icons.rounded.QrCode2
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.model.OfferAvailability
import com.example.data.model.ProductOffer
import com.example.domain.CampaignEligibilityEngine
import com.example.domain.MarketConfig
import com.example.ui.components.ProductSummaryCard
import com.example.ui.components.SectionHeader
import com.example.ui.components.VerificationLabel
import com.example.ui.theme.AmberNotice
import com.example.ui.theme.AmberSoftTint
import com.example.ui.theme.EmeraldSoftTint
import com.example.ui.theme.EmeraldTrust
import com.example.ui.viewmodel.MainUiState
import com.example.ui.viewmodel.ProductDetailViewModel

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
    item {
      Text("Discover Catalog", style = MaterialTheme.typography.headlineMedium)
      Text(
        "Search Open Food Facts, Firestore verified merchants, or local offline cache.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
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
          leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
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
        ) {
          Text("Go")
        }
      }
    }

    item {
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf("Organic Oats", "Sparkling Water", "3017620422003").forEach { quick ->
          Surface(
            modifier = Modifier.clickable {
              query = quick
              onSearch(quick)
            },
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(10.dp),
          ) {
            Text(
              quick,
              style = MaterialTheme.typography.labelSmall,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            )
          }
        }
      }
    }

    if (uiState.isSearching) {
      item {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp),
          horizontalArrangement = Arrangement.Center,
        ) {
          CircularProgressIndicator()
        }
      }
    } else if (displayList.isEmpty()) {
      item {
        Card(
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Text(
              text = if (query.isBlank()) "Local cache is empty" else "No matching products",
              style = MaterialTheme.typography.titleMedium,
            )
            Text(
              text = "Scan a barcode or search by product name / 13-digit EAN.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedButton(onClick = onOpenScan, shape = RoundedCornerShape(12.dp)) {
              Text("Open Scanner")
            }
          }
        }
      }
    } else {
      item {
        SectionHeader(
          title = if (query.isBlank()) "Cached Products (${displayList.size})" else "Results (${displayList.size})",
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
  onShowMessage: (String) -> Unit,
) {
  val context = LocalContext.current
  val detailState by productDetailViewModel.uiState.collectAsStateWithLifecycle()
  val market = MarketConfig.findByCountry(mainUiState.preferences.countryCode)

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
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        IconButton(onClick = onBack) {
          Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
        }
        Text("Product Specs & Offers", style = MaterialTheme.typography.titleLarge)
        IconButton(onClick = shareProductAction) {
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
          CircularProgressIndicator()
        }
      }
    } else if (product == null) {
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
            Text("Unlisted Barcode: $barcode", style = MaterialTheme.typography.titleLarge)
            Text(
              "No catalog entry was found. Signed-in users can contribute an unverified community entry below.",
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
              onClick = {
                productDetailViewModel.submitUnverifiedProduct(
                  barcode = barcode,
                  name = newName,
                  brand = newBrand,
                  category = newCategory,
                  description = newDescription,
                )
              },
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(14.dp),
            ) {
              Text("Save Community Product (Unverified)")
            }
          }
        }
      }
    } else {
      item {
        Card(
          shape = RoundedCornerShape(22.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        ) {
          Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(14.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              if (product.imageUrl.isNotBlank()) {
                AsyncImage(
                  model = product.imageUrl,
                  contentDescription = product.name,
                  modifier = Modifier
                    .size(82.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                  contentScale = ContentScale.Crop,
                )
              } else {
                Box(
                  modifier = Modifier
                    .size(82.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                  contentAlignment = Alignment.Center,
                ) {
                  Icon(Icons.Rounded.QrCode2, contentDescription = null, modifier = Modifier.size(34.dp))
                }
              }
              Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
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

            Surface(
              color = MaterialTheme.colorScheme.surfaceVariant,
              shape = RoundedCornerShape(10.dp),
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
              ) {
                Text("GTIN / Code: ${product.barcode}", style = MaterialTheme.typography.labelMedium)
                Text(
                  if (detailState.fromOfflineCache) "Offline Cache" else product.dataProvider,
                  style = MaterialTheme.typography.labelSmall,
                  color = EmeraldTrust,
                )
              }
            }

            if (product.description.isNotBlank()) {
              Text(product.description, style = MaterialTheme.typography.bodyMedium)
            }
            if (product.attributionText.isNotBlank()) {
              Text(
                product.attributionText,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                onClick = { onOpenReceiptSubmit(null) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
              ) {
                Icon(Icons.Rounded.ReceiptLong, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Log Receipt")
              }
            }
          }
        }
      }

      // Eligible Partner Campaigns
      if (mainUiState.campaigns.isNotEmpty()) {
        item { SectionHeader("Campaign Eligibility (${market.countryCode})") }
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
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
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
                TextButton(onClick = { onOpenReceiptSubmit(campaign.id) }) {
                  Text("Claim +${campaign.rewardPoints}")
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
          onAction = { showAddOfferDialog = true },
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
              OutlinedButton(onClick = { showAddOfferDialog = true }, shape = RoundedCornerShape(12.dp)) {
                Icon(Icons.Rounded.AddCircleOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Report Unverified Store Price")
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
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
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
