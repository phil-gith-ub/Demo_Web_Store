package com.example.phil_android_store.data

/**
 * Feature flags that can be easily mapped to Braze SDK remote configuration.
 * When a feature is disabled, the app should display in a different state.
 */
object FeatureFlags {
    /**
     * VIP Products feature flag.
     * When false: Only show 2 category tabs (All Products, Electronics)
     * When true: Show 3 category tabs (All Products, Electronics, VIP Products)
     * 
     * TODO: Map this to Braze SDK remote configuration
     */
    var isVipProductsEnabled: Boolean = true
        private set
    
    /**
     * Dark Mode Toggle feature flag.
     * When false: Hide the dark mode toggle in profile
     * When true: Show the dark mode toggle in profile
     * 
     * TODO: Map this to Braze SDK remote configuration
     */
    var isDarkModeToggleEnabled: Boolean = true
        private set
    
    /**
     * Method to update VIP feature flag (will be called by Braze SDK integration)
     */
    fun setVipProductsEnabled(enabled: Boolean) {
        isVipProductsEnabled = enabled
    }
    
    /**
     * Method to update dark mode toggle feature flag (will be called by Braze SDK integration)
     */
    fun setDarkModeToggleEnabled(enabled: Boolean) {
        isDarkModeToggleEnabled = enabled
    }
}
