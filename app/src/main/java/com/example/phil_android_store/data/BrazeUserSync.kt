package com.example.phil_android_store.data

import android.content.Context
import com.braze.Braze
import com.braze.configuration.BrazeConfig

/**
 * Utility class to sync user profile data to Braze attributes
 */
object BrazeUserSync {
    private const val ANONYMOUS_USER_ID = "anonymous_user"
    /**
     * Sync user profile to Braze attributes and start a new session.
     * Identifies the user and sets attributes.
     * Maps:
     * - userId -> external_id
     * - firstName -> first_name
     * - lastName -> last_name
     * - email -> email
     * - mobile -> phone
     * - favoriteProductCategory -> favorite_product_category (custom attribute)
     * - active_member -> true
     */
    fun syncUserToBraze(context: Context, profile: UserProfile) {
        val brazeInstance = Braze.getInstance(context)
        
        // Change user to identify them in Braze (this starts a new session for this user)
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
     * Initialize anonymous user session.
     * Sets the user to anonymous_user and active_member to false.
     * This should be called on app startup if no user is logged in.
     */
    fun initializeAnonymousSession(context: Context) {
        val brazeInstance = Braze.getInstance(context)
        
        // Change to anonymous user to start a session
        brazeInstance.changeUser(ANONYMOUS_USER_ID)
        
        // Set active_member to false for anonymous session
        brazeInstance.currentUser?.let { user ->
            user.setCustomUserAttribute("active_member", false)
        }
    }
    
    /**
     * Reset Braze to a completely anonymous state on logout.
     * Changes to anonymous_user and sets active_member to false.
     * 
     * Note: This only affects Braze's local data. Your internal UserProfileManager
     * uses SharedPreferences which is separate and unaffected by this operation.
     */
    fun onUserLogout(context: Context) {
        val brazeInstance = Braze.getInstance(context)
        
        // Change to anonymous user to start a fresh anonymous session
        brazeInstance.changeUser(ANONYMOUS_USER_ID)
        
        // Set active_member to false for the anonymous session
        brazeInstance.currentUser?.let { user ->
            user.setCustomUserAttribute("active_member", false)
        }
    }
}
