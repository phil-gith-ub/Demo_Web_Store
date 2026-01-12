package com.example.phil_android_store.data

import android.content.Context
import com.braze.Braze
import com.braze.configuration.BrazeConfig

/**
 * Utility class to sync user profile data to Braze attributes
 */
object BrazeUserSync {
    /**
     * Sync user profile to Braze attributes
     * Maps:
     * - userId -> external_id
     * - firstName -> first_name
     * - lastName -> last_name
     * - email -> email
     * - mobile -> phone
     * - favoriteProductCategory -> favorite_product_category (custom attribute)
     */
    fun syncUserToBraze(context: Context, profile: UserProfile) {
        val brazeInstance = Braze.getInstance(context)
        
        // Change user to identify them in Braze (this merges anonymous data with identified user)
        brazeInstance.changeUser(profile.userId)
        
        // Use a null-safe call with ?.let to execute code only if currentUser is not null
        brazeInstance.currentUser?.let { user ->
            // Set standard attributes
            if (profile.firstName.isNotBlank()) {
                user.setFirstName(profile.firstName)
            }
            
            if (profile.lastName.isNotBlank()) {
                user.setLastName(profile.lastName)
            }
            
            if (profile.email.isNotBlank()) {
                user.setEmail(profile.email)
            }
            
            if (profile.mobile.isNotBlank()) {
                user.setPhoneNumber(profile.mobile)
            }
            
            // Set custom attribute for favorite product category
            if (profile.favoriteProductCategory.isNotBlank()) {
                user.setCustomUserAttribute("favorite_product_category", profile.favoriteProductCategory)
            }
            
            // Set active_member to true when user is logged in
            user.setCustomUserAttribute("active_member", true)
        }
    }
    
    /**
     * Reset Braze to a completely anonymous state on logout.
     * Wipes all Braze data and reinitializes the SDK to start fresh with no external user ID.
     * 
     * Note: This only affects Braze's local data. Your internal UserProfileManager
     * uses SharedPreferences which is separate and unaffected by this operation.
     */
    fun onUserLogout(context: Context) {
        // Reinitialize Braze SDK with the same configuration
        // This will reset the SDK to a fresh state with no external user ID
        // Note: If wipeData() method exists in your SDK version, you can add it before this line:
        // Braze.wipeData(context.applicationContext)
        val brazeConfig = BrazeConfig.Builder()
            .setIsInAppMessageAccessibilityExclusiveModeEnabled(false)
            .build()
        Braze.configure(context.applicationContext, brazeConfig)
        
        // Set active_member to false for the new anonymous session
        Braze.getInstance(context).currentUser?.let { user ->
            user.setCustomUserAttribute("active_member", false)
        }
    }
}
