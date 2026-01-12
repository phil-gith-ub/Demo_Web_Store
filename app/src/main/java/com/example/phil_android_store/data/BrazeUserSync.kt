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
     * Switch to anonymous user (logout)
     * Note: Once a user is identified in Braze, you cannot revert to true anonymous.
     * This creates a new anonymous user session.
     */
    fun switchToAnonymousUser(context: Context) {
        val brazeInstance = Braze.getInstance(context)
        // Generate a unique anonymous user ID for this session
        val anonymousUserId = "anonymous_${UUID.randomUUID()}"
        brazeInstance.changeUser(anonymousUserId)
    }
}
