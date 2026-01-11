package com.example.phil_android_store.data

object CartManager {
    private val _cartItems = mutableListOf<Product>()
    val cartItems: List<Product> get() = _cartItems.toList()
    
    val cartItemCount: Int get() = _cartItems.size
    
    fun addToCart(product: Product) {
        _cartItems.add(product)
    }
    
    fun removeFromCart(product: Product) {
        _cartItems.remove(product)
    }
    
    fun clearCart() {
        _cartItems.clear()
    }
    
    fun getTotalPrice(): Double {
        return _cartItems.sumOf { it.price }
    }
}
