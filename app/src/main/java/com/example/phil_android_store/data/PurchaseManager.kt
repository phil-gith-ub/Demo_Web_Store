package com.example.phil_android_store.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import java.util.Date
import java.util.UUID

/**
 * Manages purchase history with persistence across app restarts.
 * Tracks purchases per user ID.
 */
class PurchaseManager(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "purchase_history_prefs",
        Context.MODE_PRIVATE
    )
    private val purchaseHistoryKey = "purchase_history"
    
    private val purchaseHistory: MutableMap<String, MutableList<PurchaseOrder>> by lazy {
        loadAllPurchases()
    }
    
    /**
     * Record a purchase for a user
     */
    fun recordPurchase(userId: String, products: List<Product>): PurchaseOrder {
        val orderId = UUID.randomUUID().toString()
        val purchaseDate = Date()
        val totalAmount = products.sumOf { it.price }
        
        val order = PurchaseOrder(
            orderId = orderId,
            userId = userId,
            purchaseDate = purchaseDate,
            products = products.toList(), // Create a copy
            totalAmount = totalAmount
        )
        
        val userPurchases = purchaseHistory.getOrPut(userId) { mutableListOf() }
        userPurchases.add(order)
        saveAllPurchases()
        
        return order
    }
    
    /**
     * Get all purchase orders for a user
     */
    fun getPurchaseHistory(userId: String): List<PurchaseOrder> {
        return purchaseHistory[userId]?.toList() ?: emptyList()
    }
    
    /**
     * Get total amount spent by a user across all purchases
     */
    fun getTotalSpent(userId: String): Double {
        return purchaseHistory[userId]?.sumOf { it.totalAmount } ?: 0.0
    }
    
    /**
     * Check if user is VIP (total purchases > $2000)
     */
    fun isVip(userId: String): Boolean {
        return getTotalSpent(userId) > 2000.0
    }
    
    /**
     * Get all purchases grouped by date for a user
     * Returns a map where key is the date (as a formatted string) and value is list of orders
     */
    fun getPurchasesGroupedByDate(userId: String): Map<String, List<PurchaseOrder>> {
        val orders = getPurchaseHistory(userId)
        return orders.groupBy { order ->
            // Format date as "YYYY-MM-DD" for grouping
            val date = order.purchaseDate
            val year = date.year + 1900 // Date.year is years since 1900
            val month = date.month + 1 // Date.month is 0-based
            val day = date.date
            String.format("%04d-%02d-%02d", year, month, day)
        }
    }
    
    /**
     * Load all purchases from SharedPreferences
     */
    private fun loadAllPurchases(): MutableMap<String, MutableList<PurchaseOrder>> {
        val jsonString = prefs.getString(purchaseHistoryKey, null)
        val purchases = mutableMapOf<String, MutableList<PurchaseOrder>>()
        
        if (jsonString != null) {
            try {
                val jsonObject = JSONObject(jsonString)
                val userIds = jsonObject.keys()
                
                while (userIds.hasNext()) {
                    val userId = userIds.next()
                    val ordersArray = jsonObject.getJSONArray(userId)
                    val userOrders = mutableListOf<PurchaseOrder>()
                    
                    for (i in 0 until ordersArray.length()) {
                        val orderJson = ordersArray.getJSONObject(i)
                        val productsArray = orderJson.getJSONArray("products")
                        val products = mutableListOf<Product>()
                        
                        for (j in 0 until productsArray.length()) {
                            val productJson = productsArray.getJSONObject(j)
                            products.add(
                                Product(
                                    id = productJson.getString("id"),
                                    name = productJson.getString("name"),
                                    price = productJson.getDouble("price"),
                                    description = productJson.getString("description"),
                                    category = productJson.getString("category"),
                                    isVip = productJson.optBoolean("isVip", false)
                                )
                            )
                        }
                        
                        val purchaseDate = Date(orderJson.getLong("purchaseDate"))
                        
                        userOrders.add(
                            PurchaseOrder(
                                orderId = orderJson.getString("orderId"),
                                userId = orderJson.getString("userId"),
                                purchaseDate = purchaseDate,
                                products = products,
                                totalAmount = orderJson.getDouble("totalAmount")
                            )
                        )
                    }
                    
                    purchases[userId] = userOrders
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        return purchases
    }
    
    /**
     * Save all purchases to SharedPreferences
     */
    private fun saveAllPurchases() {
        try {
            val jsonObject = JSONObject()
            
        purchaseHistory.forEach { (userId, orders) ->
                val ordersArray = JSONArray()
                
                orders.forEach { order ->
                    val orderJson = JSONObject().apply {
                        put("orderId", order.orderId)
                        put("userId", order.userId)
                        put("purchaseDate", order.purchaseDate.time)
                        put("totalAmount", order.totalAmount)
                        
                        val productsArray = JSONArray()
                        order.products.forEach { product ->
                            val productJson = JSONObject().apply {
                                put("id", product.id)
                                put("name", product.name)
                                put("price", product.price)
                                put("description", product.description)
                                put("category", product.category)
                                put("isVip", product.isVip)
                            }
                            productsArray.put(productJson)
                        }
                        put("products", productsArray)
                    }
                    ordersArray.put(orderJson)
                }
                
                jsonObject.put(userId, ordersArray)
            }
            
            prefs.edit().putString(purchaseHistoryKey, jsonObject.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
