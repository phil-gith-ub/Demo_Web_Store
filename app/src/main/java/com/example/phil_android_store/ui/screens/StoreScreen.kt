package com.example.phil_android_store.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import com.example.phil_android_store.data.BrazeUserSync
import com.example.phil_android_store.data.CartManager
import com.example.phil_android_store.data.MockData
import com.example.phil_android_store.data.Product
import com.example.phil_android_store.data.PurchaseManager
import com.example.phil_android_store.data.UserProfileManager
import com.example.phil_android_store.ui.components.NotificationBanner
import com.example.phil_android_store.ui.components.BrazeBanner
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay

enum class ProductCategory {
    ALL,
    ELECTRONICS,
    VIP
}

@Composable
fun StoreScreen(
    bannerContent: String? = null,  // Can be set to null to collapse, or a string to display
    onShowBannerMessage: (String) -> Unit,  // Callback to show banner message
    initialCategory: String? = null,  // Optional initial category to select (e.g., "VIP")
    refreshKey: Int = 0,  // Key that changes to trigger refresh
    onCartUpdated: () -> Unit = {},  // Callback when cart is updated
    cartManager: CartManager  // Shared cart manager instance
) {
    val context = LocalContext.current
    val allProducts = MockData.products
    val purchaseManager = remember { PurchaseManager(context) }
    val profileManager = remember { UserProfileManager(context) }
    
    // Check Braze feature flag for VIP products tab visibility
    var isVipFeatureEnabled by remember { mutableStateOf(false) }
    
    // Check if user is VIP based on total purchases > $1000
    var isVipUser by remember { mutableStateOf(false) }
    
    // Refresh feature flag on screen load
    LaunchedEffect(Unit) {
        isVipFeatureEnabled = BrazeUserSync.isVipProductsEnabled(context)
    }
    
    // Refresh feature flag and VIP status when refreshKey changes (login/logout)
    LaunchedEffect(refreshKey) {
        // Small delay to allow Braze actions (changeUser, events) to complete
        delay(500)
        isVipFeatureEnabled = BrazeUserSync.isVipProductsEnabled(context)
        
        val currentUserId = profileManager.getCurrentUserId()
        isVipUser = if (currentUserId != null) {
            purchaseManager.isVip(currentUserId)
        } else {
            false
        }
    }
    
    // Refresh VIP status on screen load
    LaunchedEffect(Unit) {
        val currentUserId = profileManager.getCurrentUserId()
        isVipUser = if (currentUserId != null) {
            purchaseManager.isVip(currentUserId)
        } else {
            false
        }
    }
    
    var selectedCategory by remember { 
        mutableStateOf(ProductCategory.ALL)
    }
    var notificationMessage by remember { mutableStateOf<String?>(null) }
    
    // Determine which tabs to show based on feature flag (not user VIP status)
    val categories = if (isVipFeatureEnabled) {
        listOf(ProductCategory.ALL, ProductCategory.ELECTRONICS, ProductCategory.VIP)
    } else {
        listOf(ProductCategory.ALL, ProductCategory.ELECTRONICS)
    }
    
    // Update selectedCategory based on initialCategory and feature flag availability
    // IMPORTANT: Only set VIP category if feature flag is enabled AND VIP is in categories list
    LaunchedEffect(initialCategory, isVipFeatureEnabled, categories) {
        when (initialCategory) {
            "VIP" -> {
                // Only set to VIP if feature flag is enabled AND VIP category exists in list
                if (isVipFeatureEnabled && categories.contains(ProductCategory.VIP)) {
                    selectedCategory = ProductCategory.VIP
                } else {
                    // Fallback to ALL if VIP tab is not available
                    selectedCategory = ProductCategory.ALL
                }
            }
            "ELECTRONICS" -> {
                if (categories.contains(ProductCategory.ELECTRONICS)) {
                    selectedCategory = ProductCategory.ELECTRONICS
                } else {
                    selectedCategory = ProductCategory.ALL
                }
            }
            else -> {
                // Set to ALL if no initial category or if initial category is null
                if (initialCategory == null) {
                    selectedCategory = ProductCategory.ALL
                }
            }
        }
    }
    
    // Filter products based on selected category
    // When user is not VIP, filter out VIP products from ALL products list
    val filteredProducts = when (selectedCategory) {
        ProductCategory.ALL -> {
            if (isVipUser) {
                allProducts
            } else {
                // Hide VIP products when user is not VIP
                allProducts.filter { !it.isVip }
            }
        }
        ProductCategory.ELECTRONICS -> {
            if (isVipUser) {
                allProducts.filter { it.category == "Electronics" }
            } else {
                // Hide VIP products when user is not VIP
                allProducts.filter { it.category == "Electronics" && !it.isVip }
            }
        }
        ProductCategory.VIP -> if (isVipUser) {
            allProducts.filter { it.isVip }
        } else {
            emptyList() // Hide products if user is not VIP (will show message instead)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Category Tabs
            ScrollableTabRow(
                selectedTabIndex = categories.indexOf(selectedCategory).coerceIn(0, categories.size - 1),
                modifier = Modifier.fillMaxWidth()
            ) {
                categories.forEach { category ->
                    Tab(
                        selected = selectedCategory == category,
                        onClick = { 
                            selectedCategory = category
                            // Log event every time VIP products tab is clicked
                            if (category == ProductCategory.VIP) {
                                BrazeUserSync.logViewedVipProducts(context)
                            }
                        },
                        text = {
                            Text(
                                text = when (category) {
                                    ProductCategory.ALL -> "All Products"
                                    ProductCategory.ELECTRONICS -> "Electronics"
                                    ProductCategory.VIP -> "VIP Products"
                                }
                            )
                        }
                    )
                }
            }
            
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Braze Banner Container (collapses when no banner is available)
                item {
                    BrazeBanner(
                        placementId = "store_page_banner",
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                
                // Show message if user is viewing VIP tab but is not VIP
                if (selectedCategory == ProductCategory.VIP && !isVipUser) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "You have not reached VIP member status",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
                
                items(filteredProducts) { product ->
                    ProductCard(
                        product = product,
                        onBuyClick = {
                            cartManager.addToCart(product)
                            // Log event when item is added to cart
                            BrazeUserSync.logAddedItemToCart(context, product)
                            notificationMessage = "${product.name} added to cart!"
                            // Notify parent that cart was updated
                            onCartUpdated()
                        }
                    )
                }
            }
        }
        
        // Floating notification banner above product list (underneath tabs, floats above content)
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .zIndex(1f)
        ) {
            NotificationBanner(
                message = notificationMessage,
                onDismiss = { notificationMessage = null },
                modifier = Modifier.padding(top = 56.dp) // Position below tabs (tabs are typically ~48dp)
            )
        }
    }
}

@Composable
fun BannerCard(content: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = content,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun ProductCard(
    product: Product,
    onBuyClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Title and Price on same line, with VIP tag to the left of price
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // VIP Badge to the left of price
                    if (product.isVip) {
                        Surface(
                            color = MaterialTheme.colorScheme.tertiary,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "VIP",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    
                    Text(
                        text = "$${String.format("%.2f", product.price)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Text(
                text = product.category,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 4.dp)
            )
            
            // Description and Buy button on same line
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = product.description,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f).padding(end = 16.dp)
                )
                
                Button(
                    onClick = onBuyClick
                ) {
                    Text("Buy")
                }
            }
        }
    }
}
