package com.example.phil_android_store.data

import android.content.Context
import android.content.SharedPreferences
import com.braze.Braze
import com.braze.configuration.BrazeConfig
import com.braze.models.outgoing.BrazeProperties
import org.json.JSONObject

/**
 * Utility class to sync user profile data to Braze attributes
 * Only sends deltas (changed attributes) to minimize data transfer
 */
object BrazeUserSync {
    private const val ANONYMOUS_USER_ID = "anonymous_user"
    private const val PREFS_NAME = "braze_sync_prefs"
    private const val LAST_SENT_PREFIX = "last_sent_"
    
    /**
     * Data class to track last sent values to Braze
     */
    private data class LastSentValues(
        val firstName: String? = null,
        val lastName: String? = null,
        val email: String? = null,
        val mobile: String? = null,
        val favoriteProductCategory: String? = null,
        val activeMember: Boolean? = null
    )
    
    /**
     * Get SharedPreferences for storing last sent values
     */
    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    /**
     * Load last sent values for a user
     */
    private fun loadLastSentValues(context: Context, userId: String): LastSentValues {
        val prefs = getPrefs(context)
        val jsonString = prefs.getString("${LAST_SENT_PREFIX}$userId", null)
        
        if (jsonString == null) {
            return LastSentValues()
        }
        
        return try {
            val json = JSONObject(jsonString)
            LastSentValues(
                firstName = if (json.has("firstName")) json.getString("firstName") else null,
                lastName = if (json.has("lastName")) json.getString("lastName") else null,
                email = if (json.has("email")) json.getString("email") else null,
                mobile = if (json.has("mobile")) json.getString("mobile") else null,
                favoriteProductCategory = if (json.has("favoriteProductCategory")) json.getString("favoriteProductCategory") else null,
                activeMember = if (json.has("activeMember")) json.getBoolean("activeMember") else null
            )
        } catch (e: Exception) {
            LastSentValues()
        }
    }
    
    /**
     * Save last sent values for a user
     */
    private fun saveLastSentValues(context: Context, userId: String, values: LastSentValues) {
        val prefs = getPrefs(context)
        val json = JSONObject()
        
        values.firstName?.let { json.put("firstName", it) }
        values.lastName?.let { json.put("lastName", it) }
        values.email?.let { json.put("email", it) }
        values.mobile?.let { json.put("mobile", it) }
        values.favoriteProductCategory?.let { json.put("favoriteProductCategory", it) }
        values.activeMember?.let { json.put("activeMember", it) }
        
        prefs.edit().putString("${LAST_SENT_PREFIX}$userId", json.toString()).apply()
    }
    /**
     * Sync user profile to Braze attributes and start a new session.
     * Only sends deltas (changed attributes) to minimize data transfer.
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
        
        // Load last sent values to compare
        val lastSent = loadLastSentValues(context, profile.userId)
        var hasChanges = false
        
        // Normalize current values (empty strings become null for comparison)
        val currentFirstName = if (profile.firstName.isNotBlank()) profile.firstName else null
        val currentLastName = if (profile.lastName.isNotBlank()) profile.lastName else null
        val currentEmail = if (profile.email.isNotBlank()) profile.email else null
        val currentMobile = if (profile.mobile.isNotBlank()) profile.mobile else null
        val currentCategory = if (profile.favoriteProductCategory.isNotBlank()) profile.favoriteProductCategory else null
        
        // Use a null-safe call with ?.let to execute code only if currentUser is not null
        brazeInstance.currentUser?.let { user ->
            // Only send firstName if it changed
            if (currentFirstName != lastSent.firstName) {
                user.setFirstName(currentFirstName ?: "")
                hasChanges = true
            }
            
            // Only send lastName if it changed
            if (currentLastName != lastSent.lastName) {
                user.setLastName(currentLastName ?: "")
                hasChanges = true
            }
            
            // Only send email if it changed
            if (currentEmail != lastSent.email) {
                user.setEmail(currentEmail ?: "")
                hasChanges = true
            }
            
            // Only send mobile if it changed
            if (currentMobile != lastSent.mobile) {
                user.setPhoneNumber(currentMobile ?: "")
                hasChanges = true
            }
            
            // Only send favoriteProductCategory if it changed
            if (currentCategory != lastSent.favoriteProductCategory) {
                if (currentCategory != null) {
                    user.setCustomUserAttribute("favorite_product_category", currentCategory)
                } else {
                    user.unsetCustomUserAttribute("favorite_product_category")
                }
                hasChanges = true
            }
            
            // Always check active_member - set to true for identified users
            if (lastSent.activeMember != true) {
                user.setCustomUserAttribute("active_member", true)
                hasChanges = true
            }
        }
        
        // Update last sent values if there were changes
        if (hasChanges) {
            saveLastSentValues(
                context, 
                profile.userId,
                LastSentValues(
                    firstName = currentFirstName,
                    lastName = currentLastName,
                    email = currentEmail,
                    mobile = currentMobile,
                    favoriteProductCategory = currentCategory,
                    activeMember = true
                )
            )
            
            // Flush data to ensure attributes are sent to Braze immediately
            brazeInstance.requestImmediateDataFlush()
        }
    }
    
    /**
     * Initialize anonymous user session.
     * Sets the user to anonymous_user and active_member to false.
     * Only sends if active_member has changed.
     * This should be called on app startup if no user is logged in.
     */
    fun initializeAnonymousSession(context: Context) {
        val brazeInstance = Braze.getInstance(context)
        
        // Change to anonymous user to start a session
        brazeInstance.changeUser(ANONYMOUS_USER_ID)
        
        // Check if active_member needs to be updated
        val lastSent = loadLastSentValues(context, ANONYMOUS_USER_ID)
        if (lastSent.activeMember != false) {
            brazeInstance.currentUser?.let { user ->
                user.setCustomUserAttribute("active_member", false)
            }
            
            // Update last sent values
            saveLastSentValues(
                context,
                ANONYMOUS_USER_ID,
                LastSentValues(activeMember = false)
            )
            
            // Flush data to ensure attributes are sent to Braze immediately
            brazeInstance.requestImmediateDataFlush()
        }
    }
    
    /**
     * Reset Braze to a completely anonymous state on logout.
     * Wipes all cached data in the SDK without calling changeUser.
     * 
     * Note: The logged_out event should be logged BEFORE calling this method.
     * This method will flush the event to Braze servers, then wipe all local cache.
     * 
     * Note: This only affects Braze's local data. Your internal UserProfileManager
     * uses SharedPreferences which is separate and unaffected by this operation.
     */
    fun onUserLogout(context: Context) {
        val brazeInstance = Braze.getInstance(context)
        
        // Flush data to ensure logged_out event and any pending data is sent to Braze servers
        brazeInstance.requestImmediateDataFlush()
        
        // Wipe all cached data in the Braze SDK (does not call changeUser)
        Braze.wipeData(context)
    }
    
    /**
     * Log custom event: added_item_to_cart
     * Includes product properties: product_name (lowercase with underscores), product_category, product_price
     */
    fun logAddedItemToCart(context: Context, product: Product) {
        val brazeInstance = Braze.getInstance(context)
        
        // Convert product name to lowercase with underscores (e.g., "Wireless Headphones" -> "wireless_headphones")
        val productNameFormatted = product.name
            .lowercase()
            .replace(" ", "_")
            .replace("-", "_")
            .replace("'", "")
            .replace(".", "")
            .replace(",", "")
        
        val properties = BrazeProperties().apply {
            addProperty("product_name", productNameFormatted)
            addProperty("product_category", product.category)
            addProperty("product_price", product.price)
        }
        
        brazeInstance.logCustomEvent("added_item_to_cart", properties)
    }
    
    /**
     * Log custom event: viewed_vip_products
     * Triggered when user clicks on VIP products tab
     */
    fun logViewedVipProducts(context: Context) {
        val brazeInstance = Braze.getInstance(context)
        brazeInstance.logCustomEvent("viewed_vip_products")
    }
    
    /**
     * Log custom event: logged_in
     * Triggered when user clicks login button (only if userId is populated)
     * IMPORTANT: This should be called AFTER changeUser() is called in syncUserToBraze()
     * to ensure the event is associated with the correct user profile.
     * The event is flushed immediately to ensure in-app messages can be triggered.
     */
    fun logLoggedIn(context: Context, userId: String) {
        if (userId.isNotBlank()) {
            val brazeInstance = Braze.getInstance(context)
            brazeInstance.logCustomEvent("logged_in")
            // Flush immediately to ensure event is sent to Braze servers right away
            // This allows in-app messages triggered by logged_in to display promptly
            brazeInstance.requestImmediateDataFlush()
        }
    }
    
    /**
     * Log custom event: logged_out
     * Triggered when user clicks logout button
     * Should be called BEFORE switching to anonymous user
     */
    fun logLoggedOut(context: Context) {
        val brazeInstance = Braze.getInstance(context)
        brazeInstance.logCustomEvent("logged_out")
    }
}
