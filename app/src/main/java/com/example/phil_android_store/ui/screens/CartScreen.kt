package com.example.phil_android_store.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.zIndex
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Clear
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.phil_android_store.data.BrazeUserSync
import com.example.phil_android_store.data.CartManager
import com.example.phil_android_store.data.Product
import com.example.phil_android_store.data.PurchaseManager
import com.example.phil_android_store.data.UserProfileManager
import com.example.phil_android_store.ui.components.NotificationBanner
import com.example.phil_android_store.ui.components.BrazeBanner

@Composable
fun CartScreen(
    onCartUpdated: () -> Unit = {},  // Callback when cart is updated
    cartManager: CartManager  // Shared cart manager instance
) {
    val context = LocalContext.current
    // Use state to track cart items so UI updates immediately when cart changes
    var cartItems by remember { mutableStateOf(cartManager.cartItems) }
    var totalPrice by remember { mutableStateOf(cartManager.getTotalPrice()) }
    var purchaseSuccessMessage by remember { mutableStateOf<String?>(null) }
    val purchaseManager = remember { PurchaseManager(context) }
    val profileManager = remember { UserProfileManager(context) }
    
    // Update cart items and total price whenever cart changes
    LaunchedEffect(cartManager.cartItemCount) {
        cartItems = cartManager.cartItems
        totalPrice = cartManager.getTotalPrice()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            if (cartItems.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Your cart is empty",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "Cart",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                items(cartItems) { product ->
                    CartItemCard(
                        product = product,
                        onRemoveClick = {
                            cartManager.removeFromCart(product)
                            // Update local state immediately
                            cartItems = cartManager.cartItems
                            totalPrice = cartManager.getTotalPrice()
                            onCartUpdated()
                        }
                    )
                }
            }

            // Checkout section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Total: $${String.format("%.2f", totalPrice)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Clear Cart button
                    Button(
                        onClick = {
                            cartManager.clearCart()
                            // Update local state immediately
                            cartItems = cartManager.cartItems
                            totalPrice = cartManager.getTotalPrice()
                            onCartUpdated()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("Clear Cart")
                    }

                    // Checkout button
                    Button(
                        onClick = {
                            // Record purchase if user is logged in
                            val currentUserId = profileManager.getCurrentUserId()
                            if (currentUserId != null && cartItems.isNotEmpty()) {
                                // Log purchases to Braze (logs each product with properties)
                                BrazeUserSync.logPurchases(context, cartItems)
                                
                                purchaseManager.recordPurchase(currentUserId, cartItems)
                                
                                // Check if user reached VIP status and sync to Braze
                                val isVip = purchaseManager.isVip(currentUserId)
                                BrazeUserSync.syncVipStatusToBraze(context, currentUserId, isVip)
                            }
                            
                            // Show success message and clear cart
                            purchaseSuccessMessage = "Purchase successful! Thank you for your order."
                            cartManager.clearCart()
                            // Update local state immediately
                            cartItems = cartManager.cartItems
                            totalPrice = cartManager.getTotalPrice()
                            onCartUpdated()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Checkout")
                    }
                }
            }
            }
        }
        
        // Floating notification banner at the top
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .zIndex(1f),
            contentAlignment = Alignment.TopCenter
        ) {
            NotificationBanner(
                message = purchaseSuccessMessage,
                onDismiss = { purchaseSuccessMessage = null },
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        
        // Floating Braze banner at the bottom, above main menu tabs
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .zIndex(1f)
                .padding(bottom = 80.dp), // Position above bottom navigation (typically ~56-64dp)
            contentAlignment = Alignment.BottomCenter
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .padding(horizontal = 8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                BrazeBanner(
                    placementId = "cart_banner",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun CartItemCard(
    product: Product,
    onRemoveClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = product.category,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Text(
                    text = "$${String.format("%.2f", product.price)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            IconButton(
                onClick = onRemoveClick,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove item",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

