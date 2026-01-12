package com.example.phil_android_store.data

import android.content.Context
import com.braze.Braze
import java.util.UUID

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
        }
    }
    
    /**
     * Reset Braze to a completely anonymous state on logout.
     * Changes to a new anonymous user ID and clears all user attributes.
     * 
     * Note: This only affects Braze's local data. Your internal UserProfileManager
     * uses SharedPreferences which is separate and unaffected by this operation.
     */
    fun onUserLogout(context: Context) {
        val brazeInstance = Braze.getInstance(context)
        
        // Change to a new anonymous user ID to create a clean slate
        // This effectively creates a new anonymous user session
        brazeInstance.changeUser(UUID.randomUUID().toString())
        
        // Clear all user attributes we've set
        brazeInstance.currentUser?.let { user ->
            // Clear standard attributes by setting them to null
            user.setFirstName(null)
            user.setLastName(null)
            user.setEmail(null)
            user.setPhoneNumber(null)
            
            // Clear custom attribute
            user.unsetCustomUserAttribute("favorite_product_category")
        }
        
        // Flush data to ensure changes are sent to Braze immediately
        brazeInstance.requestImmediateDataFlush()
    }
}
