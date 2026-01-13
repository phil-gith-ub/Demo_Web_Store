package com.example.phil_android_store.data

import java.util.Date

/**
 * Represents a single purchase order containing multiple products
 */
data class PurchaseOrder(
    val orderId: String,
    val userId: String,
    val purchaseDate: Date,
    val products: List<Product>,
    val totalAmount: Double
) {
    init {
        // Validate that totalAmount matches sum of product prices
        val calculatedTotal = products.sumOf { it.price }
        require(kotlin.math.abs(totalAmount - calculatedTotal) < 0.01) {
            "Total amount must match sum of product prices"
        }
    }
}

/**
 * Represents a single purchased product (for display in purchase history)
 */
data class Purchase(
    val product: Product,
    val purchaseDate: Date,
    val orderId: String
)
