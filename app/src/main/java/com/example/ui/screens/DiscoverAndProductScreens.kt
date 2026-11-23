package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.Campaign
import com.example.data.model.OfferAvailability
import com.example.data.model.ProductOffer
import com.example.domain.MarketCatalog
import com.example.ui.components.ProductSummaryCard
import com.example.ui.components.SectionHeader
import com.example.ui.components.VerificationBadge
import com.example.ui.components.formatTimestamp
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.BentoButter
import com.example.ui.theme.BentoCream
import com.example.ui.theme.BentoMint
import com.example.ui.theme.BentoOrchid
import com.example.ui.theme.BentoSky
import com.example.ui.theme.DarkBentoButter
import com.example.ui.theme.DarkBentoOrchid
import com.example.ui.theme.DarkBentoSky
import com.example.ui.theme.Emerald800
import com.example.ui.theme.InkBlack
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.Mint100
import com.example.ui.theme.OnBentoButter
import com.example.ui.theme.OnBentoMint
import com.example.ui.theme.OnBentoOrchid
import com.example.ui.theme.OnBentoSky
import com.example.ui.viewmodel.DiscoverViewModel
import com.example.ui.viewmodel.ProductDetailUiState
import com.example.ui.viewmodel.ProductDetailViewModel
import com.example.ui.viewmodel.UiState

private data class SpotlightProduct(
    val barcode: String,
    val title: String,
    val brand: String,
    val category: String
)

private val DiscoverSpotlights = listOf(
    SpotlightProduct("3017620422003", "Nutella Hazelnut Spread", "Ferrero • 400g", "Spreads"),
    SpotlightProduct("5449000000996", "Coca-Cola Original", "Coca-Cola • Classic", "Beverages"),
    SpotlightProduct("737628064502", "Kikkoman Soy Sauce", "Kikkoman • Pantry", "Pantry"),
    SpotlightProduct("3068320115160", "Evian Natural Water", "Evian • Mineral", "Beverages")
)

@Composable
fun DiscoverScreen(
    viewModel: DiscoverViewModel,
    countryCode: String,
    onOpenProduct: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val query by viewModel.searchQuery.collectAsStateWithLifecycle()
    val results by viewModel.searchResults.collectAsStateWithLifecycle()
    val isSearching by viewModel.isSearching.collectAsStateWithLifecycle()
    val cachedCatalog by viewModel.cachedCatalog.collectAsStateWithLifecycle()
    val campaignsState by viewModel.activeCampaigns.collectAsStateWithLifecycle()

    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Beverages", "Spreads", "Pantry", "Cached")

    val isDark = isSystemInDarkTheme()
    val tilePalettes = if (isDark) {
        listOf(
            DarkBentoButter to Color(0xFFFFE58F),
            DarkBentoSky to Color(0xFFB3D6FF),
            DarkBentoOrchid to Color(0xFFF6C2EE),
            Emerald800 to Color.White
        )
    } else {
        listOf(
            BentoButter to OnBentoButter,
            BentoSky to OnBentoSky,
            BentoOrchid to OnBentoOrchid,
            BentoMint to OnBentoMint
        )
    }

    val filteredSpotlights = DiscoverSpotlights.filter {
        selectedCategory == "All" || it.category == selectedCategory
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Cobalt Featured Live Search Banner
        item {
            Surface(
                shape = RoundedCornerShape(26.dp),
                shadowElevation = 6.dp,
                color = com.example.ui.theme.BentoIndigo,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            androidx.compose.ui.graphics.Brush.linearGradient(
                                listOf(com.example.ui.theme.BentoIndigo, com.example.ui.theme.BentoCobalt)
                            )
                        )
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Surface(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "OPEN FOOD FACTS • $countryCode",
                                fontFamily = JetBrainsMonoFontFamily,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Catalog Explorer",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Lookup global GTINs, ingredients & verified offers.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = BentoCream,
                        shadowElevation = 4.dp,
                        modifier = Modifier.size(width = 96.dp, height = 84.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_discover_basket),
                            contentDescription = "Catalog storefront",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }

        // Elevated Search Field
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { viewModel.updateQuery(it) },
                    label = { Text("Search products, brands, or GTIN") },
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.weight(1f).testTag("discover_search_input")
                )
                Button(
                    onClick = { viewModel.executeSearch() },
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.height(56.dp).testTag("discover_search_button")
                ) {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                }
            }
        }

        // Category Filter Pills
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { category ->
                    val selected = selectedCategory == category
                    Surface(
                        color = if (selected) InkBlack else MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(20.dp),
                        shadowElevation = if (selected) 2.dp else 0.dp,
                        border = if (selected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { selectedCategory = category }
                    ) {
                        Text(
                            text = category,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (selected) com.example.ui.theme.BentoLime else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }

        if (isSearching) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }

        if (results.isNotEmpty()) {
            item {
                SectionHeader(title = "Search Results (${results.size})")
            }
            items(results, key = { "search_${it.barcode}" }) { product ->
                ProductSummaryCard(product = product, onClick = { onOpenProduct(product.barcode) })
            }
        }

        // 2-Column Elevated White Storefront Cards with Color Header Swatches
        if (selectedCategory != "Cached" && filteredSpotlights.isNotEmpty()) {
            item {
                SectionHeader(title = "Verified GTIN Directory")
            }
            val rows = filteredSpotlights.chunked(2)
            items(rows.size) { rowIdx ->
                val pair = rows[rowIdx]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    pair.forEachIndexed { colIdx, item ->
                        val paletteIdx = rowIdx * 2 + colIdx
                        val (bg, ink) = tilePalettes[paletteIdx % tilePalettes.size]
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(22.dp),
                            shadowElevation = 3.dp,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier
                                .weight(1f)
                                .height(186.dp)
                                .clip(RoundedCornerShape(22.dp))
                                .clickable { onOpenProduct(item.barcode) }
                        ) {
                            Column(modifier = Modifier.fillMaxSize()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(68.dp)
                                        .background(bg)
                                        .padding(12.dp)
                                ) {
                                    Surface(
                                        color = Color.White.copy(alpha = 0.85f),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.align(Alignment.TopStart)
                                    ) {
                                        Text(
                                            text = item.category.uppercase(),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = InkBlack,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                        )
                                    }
                                    Surface(
                                        color = InkBlack,
                                        shape = androidx.compose.foundation.shape.CircleShape,
                                        modifier = Modifier
                                            .size(28.dp)
                                            .align(Alignment.BottomEnd)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                                contentDescription = null,
                                                tint = com.example.ui.theme.BentoLime,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }

                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = item.title,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 2
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = item.brand,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = item.barcode,
                                        fontFamily = JetBrainsMonoFontFamily,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = com.example.ui.theme.Emerald600,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                    if (pair.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // Horizontal Campaign Carousel
        if (campaignsState is UiState.Success<List<Campaign>>) {
            val camps = (campaignsState as UiState.Success<List<Campaign>>).data
            if (camps.isNotEmpty()) {
                item {
                    SectionHeader(title = "Partner Campaigns")
                }
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(camps.size) { index ->
                            val camp = camps[index]
                            val (bg, ink) = tilePalettes[index % tilePalettes.size]
                            Surface(
                                color = bg,
                                shape = RoundedCornerShape(22.dp),
                                shadowElevation = 2.dp,
                                modifier = Modifier.width(250.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Campaign, contentDescription = null, tint = ink, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(camp.partnerName, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = ink)
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(camp.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ink)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(camp.terms, style = MaterialTheme.typography.bodySmall, color = ink.copy(alpha = 0.8f), maxLines = 2)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Surface(
                                        color = InkBlack,
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text(
                                            text = "+${camp.rewardPoints} PTS • ${camp.eligibleCountries.joinToString()}",
                                            fontFamily = JetBrainsMonoFontFamily,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            SectionHeader(title = "Offline Cached Products (${cachedCatalog.size})")
        }
        if (cachedCatalog.isEmpty()) {
            item {
                Text(
                    text = "Tap any verified GTIN tile above or scan a barcode to cache product specifications offline.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(cachedCatalog, key = { "cached_${it.barcode}" }) { product ->
                ProductSummaryCard(product = product, onClick = { onOpenProduct(product.barcode) })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    viewModel: ProductDetailViewModel,
    isGuest: Boolean,
    countryCode: String,
    currencyCode: String,
    onOpenOffer: (ProductOffer) -> Unit,
    onSubmitReceiptForCampaign: (String) -> Unit,
    onRequestSignIn: () -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val actionMessage by viewModel.actionMessage.collectAsStateWithLifecycle()

    var showAddProductDialog by remember { mutableStateOf(false) }
    var showAddOfferDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Product & Offers") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        when (val state = uiState) {
            is ProductDetailUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is ProductDetailUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                }
            }
            is ProductDetailUiState.Loaded -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    if (actionMessage != null) {
                        item {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = MaterialTheme.shapes.medium,
                                modifier = Modifier.fillMaxWidth().clickable { viewModel.clearActionMessage() }
                            ) {
                                Text(
                                    text = actionMessage.orEmpty(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }

                    val product = state.product
                    if (product == null) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("No Catalog Record Found", style = MaterialTheme.typography.titleLarge)
                                    Text(
                                        text = "GTIN / Barcode: ${state.barcode}. We never fabricate missing product details.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Button(
                                        onClick = {
                                            if (isGuest) onRequestSignIn() else showAddProductDialog = true
                                        },
                                        modifier = Modifier.testTag("submit_missing_product_button")
                                    ) {
                                        Text(if (isGuest) "Sign in to Submit Product Info" else "Submit Unverified Product Info")
                                    }
                                }
                            }
                        }
                    } else {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(84.dp)
                                                .clip(MaterialTheme.shapes.medium)
                                                .background(MaterialTheme.colorScheme.surfaceVariant),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (product.imageUrl.isNotBlank()) {
                                                AsyncImage(
                                                    model = product.imageUrl,
                                                    contentDescription = product.name,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.size(84.dp)
                                                )
                                            } else {
                                                Icon(Icons.Default.QrCode2, contentDescription = null, modifier = Modifier.size(36.dp))
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(14.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            VerificationBadge(statusRaw = product.verificationStatus)
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(product.name, style = MaterialTheme.typography.titleLarge)
                                            Text(
                                                text = product.brand.ifBlank { "Brand: Not provided by source" },
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    HorizontalDivider()
                                    AttributionLine("Barcode / GTIN", product.barcode)
                                    AttributionLine("Category", product.category.ifBlank { "Not provided by source" })
                                    AttributionLine("Manufacturer", product.manufacturer.ifBlank { "Not provided by source" })
                                    AttributionLine("Origin", product.countryOfOrigin.ifBlank { "Not provided by source" })
                                    AttributionLine("Data Provider", product.dataProvider)
                                    AttributionLine("Attribution", product.attributionText)
                                    if (product.description.isNotBlank()) {
                                        AttributionLine("Details", product.description)
                                    }
                                }
                            }
                        }
                    }

                    // Campaign Eligibility Section
                    if (state.eligibleCampaigns.isNotEmpty()) {
                        item {
                            SectionHeader(title = "Campaign Eligibility ($countryCode)")
                        }
                        items(state.eligibleCampaigns, key = { it.campaign.id }) { res ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(res.campaign.title, style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        text = res.reason,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (res.eligible) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (res.eligible) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        OutlinedButton(onClick = { onSubmitReceiptForCampaign(res.campaign.id) }) {
                                            Text("Attach to Receipt")
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Price Comparison Offers
                    item {
                        SectionHeader(
                            title = "Price Comparison ($countryCode)",
                            actionLabel = if (isGuest) null else "Report Price",
                            onActionClick = { showAddOfferDialog = true }
                        )
                    }

                    if (state.offers.isEmpty()) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = "No verified merchant price feeds available for this product in $countryCode.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (!isGuest) {
                                        OutlinedButton(onClick = { showAddOfferDialog = true }) {
                                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Report Unverified Shelf Price")
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        items(state.offers, key = { it.id }) { offer ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier.fillMaxWidth().clickable { onOpenOffer(offer) }
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.LocalOffer, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(offer.storeName, style = MaterialTheme.typography.titleMedium)
                                        }
                                        Text(
                                            text = if (offer.verified) "Verified • ${formatTimestamp(offer.lastVerifiedAt)}"
                                            else "Unverified observation — not a guaranteed price",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (offer.verified) MaterialTheme.colorScheme.onSurfaceVariant else AmberWarning
                                        )
                                    }
                                    Text(
                                        text = MarketCatalog.formatMoney(offer.price, offer.currencyCode),
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }
                            }
                        }
                    }
                }

                if (showAddProductDialog) {
                    SubmitProductDialog(
                        barcode = state.barcode,
                        onDismiss = { showAddProductDialog = false },
                        onSubmit = { name, brand, cat, desc ->
                            showAddProductDialog = false
                            viewModel.submitMissingProduct(name, brand, cat, desc)
                        }
                    )
                }

                if (showAddOfferDialog) {
                    SubmitOfferDialog(
                        currencyCode = currencyCode,
                        onDismiss = { showAddOfferDialog = false },
                        onSubmit = { store, price, url ->
                            showAddOfferDialog = false
                            viewModel.submitPriceObservation(store, price, currencyCode, url)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AttributionLine(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.width(12.dp))
        Text(value, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun SubmitProductDialog(
    barcode: String,
    onDismiss: () -> Unit,
    onSubmit: (String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Submit Unverified Product") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Barcode: $barcode (will be tagged USER_SUBMITTED_UNVERIFIED)", style = MaterialTheme.typography.bodySmall)
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Product Name*") }, singleLine = true)
                OutlinedTextField(value = brand, onValueChange = { brand = it }, label = { Text("Brand") }, singleLine = true)
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category") }, singleLine = true)
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") })
            }
        },
        confirmButton = {
            Button(onClick = { onSubmit(name, brand, category, desc) }, enabled = name.isNotBlank()) {
                Text("Submit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun SubmitOfferDialog(
    currencyCode: String,
    onDismiss: () -> Unit,
    onSubmit: (String, String, String) -> Unit
) {
    var store by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Report Unverified Price") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Marked UNVERIFIED until confirmed by a verified merchant feed.", style = MaterialTheme.typography.bodySmall)
                OutlinedTextField(value = store, onValueChange = { store = it }, label = { Text("Store Name*") }, singleLine = true)
                OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Observed Price ($currencyCode)*") }, singleLine = true)
                OutlinedTextField(value = url, onValueChange = { url = it }, label = { Text("Offer Link (optional)") }, singleLine = true)
            }
        },
        confirmButton = {
            Button(onClick = { onSubmit(store, price, url) }, enabled = store.isNotBlank() && price.isNotBlank()) {
                Text("Submit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfferDetailScreen(
    barcode: String,
    storeName: String,
    price: Double,
    currencyCode: String,
    countryCode: String,
    availabilityStatus: String,
    verified: Boolean,
    offerUrl: String,
    lastVerifiedText: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val availability = OfferAvailability.safeValueOf(availabilityStatus)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Offer Detail") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (!verified) {
                Surface(
                    color = AmberContainer,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = AmberWarning)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Unverified or stale price observation. Never rely on this as a guaranteed current shelf price.",
                            style = MaterialTheme.typography.bodySmall,
                            color = AmberWarning
                        )
                    }
                }
            } else {
                Surface(color = Mint100, shape = MaterialTheme.shapes.medium, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Verified Merchant Feed Offer",
                        style = MaterialTheme.typography.labelLarge,
                        color = Emerald800,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(storeName, style = MaterialTheme.typography.headlineMedium)
                    Text(
                        text = MarketCatalog.formatMoney(price, currencyCode),
                        fontFamily = JetBrainsMonoFontFamily,
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    HorizontalDivider()
                    Text("Product GTIN: $barcode", fontFamily = JetBrainsMonoFontFamily, style = MaterialTheme.typography.bodySmall)
                    Text("Country: $countryCode • Currency: $currencyCode", style = MaterialTheme.typography.bodySmall)
                    Text("Availability: ${availability.label}", style = MaterialTheme.typography.bodySmall)
                    Text("Last Verified: $lastVerifiedText", style = MaterialTheme.typography.bodySmall)
                }
            }

            if (offerUrl.startsWith("https://") || offerUrl.startsWith("http://")) {
                Button(
                    onClick = {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(offerUrl)))
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Open Store Offer Link")
                }
            }
        }
    }
}
