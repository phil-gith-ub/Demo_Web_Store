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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.phil_android_store.data.CartManager
import com.example.phil_android_store.data.FeatureFlags
import com.example.phil_android_store.data.MockData
import com.example.phil_android_store.data.Product
import com.example.phil_android_store.ui.components.NotificationBanner
import com.example.phil_android_store.ui.components.NotificationBanner

enum class ProductCategory {
    ALL,
    ELECTRONICS,
    VIP
}

@Composable
fun StoreScreen(
    bannerContent: String? = null,  // Can be set to null to collapse, or a string to display
    onShowBannerMessage: (String) -> Unit  // Callback to show banner message
) {
    val allProducts = MockData.products
    var selectedCategory by remember { mutableStateOf(ProductCategory.ALL) }
    var notificationMessage by remember { mutableStateOf<String?>(null) }
    
    // Determine which tabs to show based on feature flag
    val categories = if (FeatureFlags.isVipProductsEnabled) {
        listOf(ProductCategory.ALL, ProductCategory.ELECTRONICS, ProductCategory.VIP)
    } else {
        listOf(ProductCategory.ALL, ProductCategory.ELECTRONICS)
    }
    
    // Filter products based on selected category
    val filteredProducts = when (selectedCategory) {
        ProductCategory.ALL -> allProducts
        ProductCategory.ELECTRONICS -> allProducts.filter { it.category == "Electronics" }
        ProductCategory.VIP -> allProducts.filter { it.isVip }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Category Tabs
            ScrollableTabRow(
                selectedTabIndex = categories.indexOf(selectedCategory),
                modifier = Modifier.fillMaxWidth()
            ) {
                categories.forEach { category ->
                    Tab(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
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
                // Banner Container (collapses when bannerContent is null)
                if (bannerContent != null) {
                    item {
                        BannerCard(content = bannerContent)
                    }
                }
                
                items(filteredProducts) { product ->
                    ProductCard(
                        product = product,
                        onBuyClick = {
                            CartManager.addToCart(product)
                            notificationMessage = "${product.name} added to cart!"
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
