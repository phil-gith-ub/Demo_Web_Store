package com.example.phil_android_store.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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

@Composable
fun CartScreen(
    onCartUpdated: () -> Unit = {}  // Callback when cart is updated
) {
    val context = LocalContext.current
    val cartItems = CartManager.cartItems
    val totalPrice = CartManager.getTotalPrice()
    var showPurchaseDialog by remember { mutableStateOf(false) }
    val purchaseManager = remember { PurchaseManager(context) }
    val profileManager = remember { UserProfileManager(context) }

    if (showPurchaseDialog) {
        PurchaseSuccessDialog(
            onDismiss = {
                showPurchaseDialog = false
                CartManager.clearCart()
                // Notify parent that cart was updated
                onCartUpdated()
            }
        )
    }

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
                    CartItemCard(product = product)
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

                    Button(
                        onClick = {
                            // Record purchase if user is logged in
                            val currentUserId = profileManager.getCurrentUserId()
                            if (currentUserId != null && cartItems.isNotEmpty()) {
                                purchaseManager.recordPurchase(currentUserId, cartItems)
                                
                                // Check if user reached VIP status and sync to Braze
                                val isVip = purchaseManager.isVip(currentUserId)
                                BrazeUserSync.syncVipStatusToBraze(context, currentUserId, isVip)
                            }
                            showPurchaseDialog = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Checkout")
                    }
                }
            }
        }
    }
}

@Composable
fun CartItemCard(product: Product) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
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
    }
}

@Composable
fun PurchaseSuccessDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Purchase Successful!",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text("Thank you for your purchase. Your order has been processed successfully.")
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}
