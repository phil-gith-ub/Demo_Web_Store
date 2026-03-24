package com.example.phil_android_store.data

data class Product(
    val id: String,
    val name: String,
    val price: Double,
    val description: String,
    val category: String,
    val isVip: Boolean = false
)
