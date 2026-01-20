package com.example.phil_android_store.data

data class UserProfile(
    val userId: String,
    var firstName: String = "",
    var lastName: String = "",
    var email: String = "",
    var mobile: String = "",
    var favoriteProductCategory: String = "",
    var isDarkModeEnabled: Boolean = false,
    var paidMembership: Boolean = false
)
