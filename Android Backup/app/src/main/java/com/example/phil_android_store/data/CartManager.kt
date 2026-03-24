package com.example.phil_android_store.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

/**
 * Manages shopping cart with persistence across app restarts.
 * Cart items are saved to SharedPreferences and restored on app startup.
 */
class CartManager(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "cart_prefs",
        Context.MODE_PRIVATE
    )
    private val cartItemsKey = "cart_items"
    
    private val _cartItems: MutableList<Product> by lazy {
        loadCartItems()
    }
    
    val cartItems: List<Product> get() = _cartItems.toList()
    
    val cartItemCount: Int get() = _cartItems.size
    
    fun addToCart(product: Product) {
        _cartItems.add(product)
        saveCartItems()
    }
    
    fun removeFromCart(product: Product) {
        _cartItems.remove(product)
        saveCartItems()
    }
    
    fun clearCart() {
        _cartItems.clear()
        saveCartItems()
    }
    
    fun getTotalPrice(): Double {
        return _cartItems.sumOf { it.price }
    }
    
    /**
     * Load cart items from SharedPreferences
     */
    private fun loadCartItems(): MutableList<Product> {
        val jsonString = prefs.getString(cartItemsKey, null)
        val items = mutableListOf<Product>()
        
        if (jsonString != null) {
            try {
                val jsonArray = JSONArray(jsonString)
                for (i in 0 until jsonArray.length()) {
                    val productJson = jsonArray.getJSONObject(i)
                    items.add(
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
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        return items
    }
    
    /**
     * Save cart items to SharedPreferences
     */
    private fun saveCartItems() {
        try {
            val jsonArray = JSONArray()
            _cartItems.forEach { product ->
                val productJson = JSONObject().apply {
                    put("id", product.id)
                    put("name", product.name)
                    put("price", product.price)
                    put("description", product.description)
                    put("category", product.category)
                    put("isVip", product.isVip)
                }
                jsonArray.put(productJson)
            }
            prefs.edit().putString(cartItemsKey, jsonArray.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
