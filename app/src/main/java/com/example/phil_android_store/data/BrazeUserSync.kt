package com.example.phil_android_store.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.braze.Braze
import com.example.phil_android_store.data.BrazeLogManager
import com.braze.configuration.BrazeConfig
import com.braze.models.outgoing.BrazeProperties
import com.braze.models.FeatureFlag
import org.json.JSONObject
import java.math.BigDecimal

/**
 * Braze SDK: Utility class to sync user profile data to Braze attributes
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
        val activeMember: Boolean? = null,
        val vipMember: Boolean? = null,
        val paidMembership: Boolean? = null
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
                activeMember = if (json.has("activeMember")) json.getBoolean("activeMember") else null,
                vipMember = if (json.has("vipMember")) json.getBoolean("vipMember") else null,
                paidMembership = if (json.has("paidMembership")) json.getBoolean("paidMembership") else null
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
        values.vipMember?.let { json.put("vipMember", it) }
        values.paidMembership?.let { json.put("paidMembership", it) }
        
        prefs.edit().putString("${LAST_SENT_PREFIX}$userId", json.toString()).apply()
    }
    /**
     * Login user to Braze.
     * Calls changeUser to identify the user, sets active_member=true, and logs logged_in event.
     * Note: VIP status should be synced separately via syncVipStatusToBraze() after login.
     * Does NOT send any other profile attributes (those are only sent when Save Profile is clicked).
     * IMPORTANT: active_member is ALWAYS sent on login (not delta tracked) to signal to Braze
     * that the user is logged in for the next session.
     */
    fun loginUserToBraze(context: Context, userId: String) {
        // Braze SDK: Get Braze instance
        val brazeInstance = Braze.getInstance(context)
        
        // Braze SDK: Change user to identify them in Braze (this starts a new session for this user)
        brazeInstance.changeUser(userId)
        BrazeLogManager.logUserIdentified(userId, isAnonymous = false)
        
        // Load last sent values to preserve vipMember
        val lastSent = loadLastSentValues(context, userId)
        
        // Braze SDK: ALWAYS set active_member to true on login (not delta tracked)
        // This signals to Braze that the user is logged in for the next session
        brazeInstance.currentUser?.let { user ->
            // Braze SDK: Set custom user attribute
            user.setCustomUserAttribute("active_member", true)
            BrazeLogManager.logUserAttributeUpdated("active_member", true)
            
            // Update last sent values (preserve vipMember)
            saveLastSentValues(
                context,
                userId,
                lastSent.copy(activeMember = true)
            )
            
            // Braze SDK: Flush data to ensure attribute is sent to Braze immediately
            brazeInstance.requestImmediateDataFlush()
        }
    }
    
    /**
     * Sync user profile to Braze attributes.
     * Only sends deltas (changed attributes) to minimize data transfer.
     * Does NOT call changeUser (user should already be identified).
     * Maps:
     * - firstName -> first_name
     * - lastName -> last_name
     * - email -> email
     * - mobile -> phone
     * - favoriteProductCategory -> favorite_product_category (custom attribute)
     * This should only be called when "Save Profile" is clicked and changes have been made.
     */
    fun syncUserToBraze(context: Context, profile: UserProfile) {
        // Braze SDK: Get Braze instance
        val brazeInstance = Braze.getInstance(context)
        
        // Load last sent values to compare
        val lastSent = loadLastSentValues(context, profile.userId)
        var hasChanges = false
        
        // Normalize current values (empty strings become null for comparison)
        val currentFirstName = if (profile.firstName.isNotBlank()) profile.firstName else null
        val currentLastName = if (profile.lastName.isNotBlank()) profile.lastName else null
        val currentEmail = if (profile.email.isNotBlank()) profile.email else null
        val currentMobile = if (profile.mobile.isNotBlank()) profile.mobile else null
        val currentCategory = if (profile.favoriteProductCategory.isNotBlank()) profile.favoriteProductCategory else null
        
        // Braze SDK: Use a null-safe call with ?.let to execute code only if currentUser is not null
        brazeInstance.currentUser?.let { user ->
            // Braze SDK: Only send firstName if it changed
            if (currentFirstName != lastSent.firstName) {
                user.setFirstName(currentFirstName ?: "")
                hasChanges = true
            }
            
            // Braze SDK: Only send lastName if it changed
            if (currentLastName != lastSent.lastName) {
                user.setLastName(currentLastName ?: "")
                hasChanges = true
            }
            
            // Braze SDK: Only send email if it changed
            if (currentEmail != lastSent.email) {
                user.setEmail(currentEmail ?: "")
                hasChanges = true
            }
            
            // Braze SDK: Only send mobile if it changed
            if (currentMobile != lastSent.mobile) {
                user.setPhoneNumber(currentMobile ?: "")
                hasChanges = true
            }
            
            // Braze SDK: Only send favoriteProductCategory if it changed
            if (currentCategory != lastSent.favoriteProductCategory) {
                if (currentCategory != null) {
                    // Braze SDK: Set custom user attribute
                    user.setCustomUserAttribute("favorite_product_category", currentCategory)
                } else {
                    // Braze SDK: Unset custom user attribute
                    user.unsetCustomUserAttribute("favorite_product_category")
                }
                hasChanges = true
            }
            
            // Braze SDK: Only send paid_membership if it changed (delta tracking)
            if (profile.paidMembership != lastSent.paidMembership) {
                // Braze SDK: Set custom user attribute
                user.setCustomUserAttribute("paid_membership", profile.paidMembership)
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
                    activeMember = lastSent.activeMember, // Preserve activeMember value
                    vipMember = lastSent.vipMember, // Preserve vipMember value
                    paidMembership = if (profile.paidMembership != lastSent.paidMembership) profile.paidMembership else lastSent.paidMembership // Update if changed, preserve otherwise
                )
            )
            
            // Braze SDK: Flush data to ensure attributes are sent to Braze immediately
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
        // Braze SDK: Get Braze instance
        val brazeInstance = Braze.getInstance(context)
        
        // Braze SDK: Change to anonymous user to start a session
        brazeInstance.changeUser(ANONYMOUS_USER_ID)
        BrazeLogManager.logUserIdentified(ANONYMOUS_USER_ID, isAnonymous = true)
        
        // Check if active_member needs to be updated
        val lastSent = loadLastSentValues(context, ANONYMOUS_USER_ID)
        if (lastSent.activeMember != false) {
            brazeInstance.currentUser?.let { user ->
                // Braze SDK: Set custom user attribute
                user.setCustomUserAttribute("active_member", false)
            }
            
            // Update last sent values
            saveLastSentValues(
                context,
                ANONYMOUS_USER_ID,
                LastSentValues(activeMember = false)
            )
            
            // Braze SDK: Flush data to ensure attributes are sent to Braze immediately
            brazeInstance.requestImmediateDataFlush()
        }
    }
    
    /**
     * Handle user logout.
     * Sets active_member to false and logs logged_out event.
     * Does NOT call changeUser or wipeData - user remains identified in Braze.
     */
    fun onUserLogout(context: Context, userId: String) {
        // Braze SDK: Get Braze instance
        val brazeInstance = Braze.getInstance(context)
        
        // Load last sent values to check if active_member needs to be updated
        val lastSent = loadLastSentValues(context, userId)
        
        // Braze SDK: Set active_member to false (only if it changed)
        if (lastSent.activeMember != false) {
            brazeInstance.currentUser?.let { user ->
                // Braze SDK: Set custom user attribute
                user.setCustomUserAttribute("active_member", false)
                
                // Update last sent values
                saveLastSentValues(
                    context,
                    userId,
                    LastSentValues(activeMember = false)
                )
            }
        }
        
        // Braze SDK: Log logged_out event
        brazeInstance.logCustomEvent("logged_out")
        BrazeLogManager.logCustomEvent("logged_out")
        
        // Braze SDK: Flush data to ensure attributes and event are sent to Braze immediately
        brazeInstance.requestImmediateDataFlush()
    }
    
    /**
     * Log custom event: added_item_to_cart
     * Includes product properties: product_name (lowercase with underscores), product_category, product_price
     */
    fun logAddedItemToCart(context: Context, product: Product) {
        // Braze SDK: Get Braze instance
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
        
        // Braze SDK: Log custom event
        brazeInstance.logCustomEvent("added_item_to_cart", properties)
        BrazeLogManager.logCustomEvent("added_item_to_cart", mapOf(
            "product_name" to productNameFormatted,
            "product_category" to product.category,
            "product_price" to product.price
        ))
        // Braze SDK: Flush data to ensure event is sent to Braze immediately
        brazeInstance.requestImmediateDataFlush()
    }
    
    /**
     * Log custom event: viewed_vip_products
     * Triggered when user clicks on VIP products tab
     */
    fun logViewedVipProducts(context: Context) {
        // Braze SDK: Get Braze instance and log custom event
        val brazeInstance = Braze.getInstance(context)
        brazeInstance.logCustomEvent("viewed_vip_products")
        BrazeLogManager.logCustomEvent("viewed_vip_products")
    }
    
    /**
     * Log custom event: logged_in
     * Triggered when user clicks login button (only if userId is populated)
     * IMPORTANT: This should be called AFTER changeUser() is called in loginUserToBraze()
     * to ensure the event is associated with the correct user profile.
     * The event is flushed immediately to ensure in-app messages can be triggered.
     */
    fun logLoggedIn(context: Context, userId: String) {
        if (userId.isNotBlank()) {
            // Braze SDK: Get Braze instance
            val brazeInstance = Braze.getInstance(context)
            // Braze SDK: Log custom event
            brazeInstance.logCustomEvent("logged_in")
            BrazeLogManager.logCustomEvent("logged_in")
            // Braze SDK: Flush immediately to ensure event is sent to Braze servers right away
            // This allows in-app messages triggered by logged_in to display promptly
            brazeInstance.requestImmediateDataFlush()
        }
    }
    
    /**
     * Sync VIP member status to Braze.
     * Only sends if the value has changed (delta tracking).
     * Sets vip_member=true if user is VIP, false otherwise.
     * This should be called:
     * - After a purchase is made (to update if user reaches VIP status)
     * - On login (to sync current VIP status)
     * - On logout (to set to false)
     */
    fun syncVipStatusToBraze(context: Context, userId: String, isVip: Boolean) {
        // Braze SDK: Get Braze instance
        val brazeInstance = Braze.getInstance(context)
        
        // Load last sent values to check if vip_member needs to be updated
        val lastSent = loadLastSentValues(context, userId)
        
        // Braze SDK: Only send if value has changed
        if (lastSent.vipMember != isVip) {
            brazeInstance.currentUser?.let { user ->
                // Braze SDK: Set custom user attribute
                user.setCustomUserAttribute("vip_member", isVip)
                BrazeLogManager.logUserAttributeUpdated("vip_member", isVip)
                
                // Update last sent values
                val updatedValues = lastSent.copy(vipMember = isVip)
                saveLastSentValues(context, userId, updatedValues)
                
                // Braze SDK: Flush data to ensure attribute is sent to Braze immediately
                brazeInstance.requestImmediateDataFlush()
            }
        }
    }
    
    /**
     * Log a purchase event to Braze.
     * Logs a single product purchase with properties.
     * According to Braze documentation: https://www.braze.com/docs/developer_guide/analytics/logging_purchases/?tab=android#logging-purchases-and-revenue
     * 
     * @param context Android context
     * @param product The product being purchased
     * @param quantity The quantity purchased (defaults to 1)
     * @param currencyCode The currency code (defaults to "USD")
     */
    fun logPurchase(context: Context, product: Product, quantity: Int = 1, currencyCode: String = "USD") {
        // Braze SDK: Get Braze instance
        val brazeInstance = Braze.getInstance(context)
        
        // Convert product name to lowercase with underscores for product_id (similar to added_item_to_cart event)
        val productId = product.name
            .lowercase()
            .replace(" ", "_")
            .replace("-", "_")
            .replace("'", "")
            .replace(".", "")
            .replace(",", "")
        
        // Create purchase properties with product details
        val purchaseProperties = BrazeProperties().apply {
            addProperty("product_name", product.name)
            addProperty("product_category", product.category)
            addProperty("product_description", product.description)
            if (product.isVip) {
                addProperty("is_vip", true)
            }
        }
        
        // Braze SDK: Log purchase to Braze
        // Method signature: logPurchase(productId: String, currencyCode: String, price: BigDecimal, quantity: Int, purchaseProperties: BrazeProperties?)
        brazeInstance.logPurchase(
            productId,
            currencyCode,
            BigDecimal.valueOf(product.price),
            quantity,
            purchaseProperties
        )
        BrazeLogManager.logCustomEvent("purchase", mapOf(
            "product_id" to productId,
            "product_name" to product.name,
            "quantity" to quantity,
            "price" to product.price,
            "currency" to currencyCode
        ))
        
        // Braze SDK: Flush data to ensure purchase is sent to Braze immediately
        brazeInstance.requestImmediateDataFlush()
    }
    
    /**
     * Log multiple purchases (for checkout with multiple items).
     * Logs each product in the cart as a separate purchase event.
     * 
     * @param context Android context
     * @param products List of products being purchased
     * @param currencyCode The currency code (defaults to "USD")
     */
    fun logPurchases(context: Context, products: List<Product>, currencyCode: String = "USD") {
        // Group products by ID and count quantities
        val productQuantities = products.groupingBy { it.id }.eachCount()
        
        // Log each unique product with its quantity
        productQuantities.forEach { (productId, quantity) ->
            val product = products.first { it.id == productId }
            logPurchase(context, product, quantity, currencyCode)
        }
    }
    
    /**
     * Check if VIP products feature flag is enabled.
     * Logs feature flag impression for analytics.
     * Returns false by default if feature flag is not found.
     */
    fun isVipProductsEnabled(context: Context): Boolean {
        // Braze SDK: Get Braze instance
        val brazeInstance = Braze.getInstance(context)
        // Braze SDK: Get feature flag
        val featureFlag = brazeInstance.getFeatureFlag("enable_vip_products")
        
        // Braze SDK: Log feature flag impression for analytics
        brazeInstance.logFeatureFlagImpression("enable_vip_products")
        
        return featureFlag?.enabled == true
    }
    
    /**
     * Request banner refresh for the specified placement IDs.
     * Should be called when the app starts or when navigating to screens with banners.
     * 
     * @param context Android context
     * @param placementIds List of banner placement IDs to refresh
     */
    fun requestBannerRefresh(context: Context, placementIds: List<String>) {
        // Braze SDK: Get Braze instance
        val brazeInstance = Braze.getInstance(context)
        // Braze SDK: Request banner refresh
        brazeInstance.requestBannersRefresh(placementIds)
        // Log the request
        BrazeLogManager.logBannerRefreshRequested(placementIds)
    }
    
    /**
     * Get a banner for a specific placement ID.
     * Returns null if no banner is available for the user.
     * 
     * @param context Android context
     * @param placementId The banner placement ID
     * @return Banner instance or null if not available
     */
    fun getBanner(context: Context, placementId: String): Any? {
        // Braze SDK: Get Braze instance
        val brazeInstance = Braze.getInstance(context)
        // Braze SDK: Get banner by placement ID
        val banner = brazeInstance.getBanner(placementId)
        // Log the result
        if (banner != null) {
            try {
                val isControlMethod = banner.javaClass.getMethod("isControl")
                val isControl = isControlMethod.invoke(banner) as? Boolean ?: false
                BrazeLogManager.logBannerReceived(placementId, isControl)
            } catch (e: Exception) {
                BrazeLogManager.logBannerReceived(placementId, false)
            }
        } else {
            BrazeLogManager.logBannerNotFound(placementId)
        }
        return banner
    }
    
    /**
     * Request Content Cards refresh from Braze.
     * Should be called when the app starts or when navigating to screens with content cards.
     * 
     * @param context Android context
     */
    fun requestContentCardsRefresh(context: Context) {
        // Braze SDK: Get Braze instance
        val brazeInstance = Braze.getInstance(context)
        try {
            // Braze SDK: Use reflection to call requestContentCardsRefresh
            val method = brazeInstance.javaClass.getMethod("requestContentCardsRefresh")
            method.invoke(brazeInstance)
            // Log the request
            BrazeLogManager.logContentCardsRefreshRequested()
        } catch (e: Exception) {
            Log.e("BrazeUserSync", "Error requesting Content Cards refresh: ${e.message}", e)
            BrazeLogManager.logError("Error requesting Content Cards refresh: ${e.message}", e)
        }
    }
    
    /**
     * Get all Content Cards from Braze using reflection.
     * Tries multiple methods to get Content Cards.
     * 
     * @param context Android context
     * @return List of Content Card instances
     */
    fun getContentCards(context: Context): List<Any> {
        // Braze SDK: Get Braze instance
        val brazeInstance = Braze.getInstance(context)
        return try {
            // Try multiple possible method names
            val possibleMethods = listOf(
                "getContentCards",
                "getCachedContentCards",
                "getAllContentCards"
            )
            
            var cards: Any? = null
            var methodFound = false
            
            for (methodName in possibleMethods) {
                try {
                    val method = brazeInstance.javaClass.getMethod(methodName)
                    cards = method.invoke(brazeInstance)
                    methodFound = true
                    Log.d("BrazeUserSync", "Successfully called $methodName")
                    break
                } catch (e: NoSuchMethodException) {
                    // Try next method
                    continue
                }
            }
            
            if (!methodFound) {
                // Try accessing through ContentCardsManager if it exists
                try {
                    val contentCardsManagerClass = Class.forName("com.braze.ui.contentcards.BrazeContentCardsManager")
                    val getInstanceMethod = contentCardsManagerClass.getMethod("getInstance", Context::class.java)
                    val manager = getInstanceMethod.invoke(null, context)
                    val getCardsMethod = manager.javaClass.getMethod("getContentCards")
                    cards = getCardsMethod.invoke(manager)
                    Log.d("BrazeUserSync", "Successfully got cards through ContentCardsManager")
                } catch (e: Exception) {
                    Log.w("BrazeUserSync", "Could not get cards through ContentCardsManager: ${e.message}")
                }
            }
            
            // Process the result
            when (cards) {
                is List<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    cards.filterNotNull() as List<Any>
                }
                null -> {
                    Log.w("BrazeUserSync", "Content Cards method returned null")
                    emptyList()
                }
                else -> {
                    Log.w("BrazeUserSync", "Content Cards method returned unexpected type: ${cards.javaClass}")
                    emptyList()
                }
            }
        } catch (e: Exception) {
            Log.e("BrazeUserSync", "Error getting Content Cards: ${e.message}", e)
            e.printStackTrace()
            emptyList()
        }
    }
    
    /**
     * Get a Content Card by position_id from extras.
     * 
     * @param context Android context
     * @param positionId The position_id value to search for in card extras
     * @return Content Card instance or null if not found
     */
    fun getContentCardByPositionId(context: Context, positionId: String): Any? {
        val cards = getContentCards(context)
        for (card in cards) {
            try {
                // Check if card is a control card (should not be displayed)
                val isControlMethod = card.javaClass.getMethod("isControlCard")
                val isControl = isControlMethod.invoke(card) as? Boolean ?: false
                if (isControl) continue
                
                // Get extras from card
                val getExtrasMethod = card.javaClass.getMethod("getExtras")
                val extras = getExtrasMethod.invoke(card) as? Map<*, *>
                
                // Check if position_id matches
                if (extras != null && extras["position_id"] == positionId) {
                    return card
                }
            } catch (e: Exception) {
                // Skip cards that don't have the expected methods
                continue
            }
        }
        return null
    }
    
    /**
     * Subscribe to Content Cards updates from Braze using the proper event-based pattern.
     * This follows the Braze documentation pattern using IEventSubscriber<ContentCardsUpdatedEvent>.
     * 
     * @param context Android context
     * @param callback Function to call when Content Cards are updated (receives list of cards)
     * @return The subscriber object that can be used to unsubscribe, or null if subscription fails
     */
    fun subscribeToContentCardsUpdates(
        context: Context,
        callback: (List<Any>) -> Unit
    ): Any? {
        // Braze SDK: Get Braze instance
        val brazeInstance = Braze.getInstance(context)
        return try {
            // Braze SDK: Get the ContentCardsUpdatedEvent class
            val eventClass = Class.forName("com.braze.events.ContentCardsUpdatedEvent")
            
            // Braze SDK: Get the IEventSubscriber interface
            val subscriberInterface = Class.forName("com.braze.events.IEventSubscriber")
            
            // Braze SDK: Create a proxy implementation of IEventSubscriber
            val subscriber = java.lang.reflect.Proxy.newProxyInstance(
                subscriberInterface.classLoader,
                arrayOf(subscriberInterface)
            ) { proxy, method, args ->
                when (method.name) {
                    "trigger" -> {
                        if (args != null && args.isNotEmpty()) {
                            // The event object is passed as the first argument
                            val event = args[0]
                            
                            // Braze SDK: Call getAllCards() on the event
                            val getAllCardsMethod = eventClass.getMethod("getAllCards")
                            val cards = getAllCardsMethod.invoke(event)
                            
                            if (cards is List<*>) {
                                @Suppress("UNCHECKED_CAST")
                                val cardList = cards.filterNotNull() as List<Any>
                                BrazeLogManager.logContentCardsReceived(cardList.size, cardList)
                                callback(cardList)
                            }
                        }
                        null
                    }
                    "equals" -> {
                        // Properly implement equals for the proxy
                        val other = args?.getOrNull(0)
                        other === proxy
                    }
                    "hashCode" -> {
                        // Return a consistent hash code
                        System.identityHashCode(proxy)
                    }
                    "toString" -> {
                        "IEventSubscriber@${Integer.toHexString(System.identityHashCode(proxy))}"
                    }
                    else -> {
                        // For other methods, return default value based on return type
                        val returnType = method.returnType
                        when {
                            returnType == Boolean::class.javaPrimitiveType || returnType == Boolean::class.java -> false
                            returnType == Int::class.javaPrimitiveType || returnType == Int::class.java -> 0
                            returnType == Long::class.javaPrimitiveType || returnType == Long::class.java -> 0L
                            returnType == Void.TYPE -> null
                            else -> null
                        }
                    }
                }
            }
            
            // Braze SDK: Subscribe using subscribeToContentCardsUpdates
            val subscribeMethod = brazeInstance.javaClass.getMethod(
                "subscribeToContentCardsUpdates",
                subscriberInterface
            )
            subscribeMethod.invoke(brazeInstance, subscriber)
            
            Log.d("BrazeUserSync", "Successfully subscribed to Content Cards updates")
            
            // Return the subscriber for potential unsubscription
            subscriber
        } catch (e: Exception) {
            Log.e("BrazeUserSync", "Error subscribing to Content Cards updates: ${e.message}", e)
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Unsubscribe from Content Cards updates.
     * 
     * @param context Android context
     * @param subscriber The subscriber object returned from subscribeToContentCardsUpdates
     */
    fun unsubscribeFromContentCardsUpdates(context: Context, subscriber: Any?) {
        if (subscriber == null) return
        
        // Braze SDK: Get Braze instance
        val brazeInstance = Braze.getInstance(context)
        try {
            // Braze SDK: Get the ContentCardsUpdatedEvent class
            val eventClass = Class.forName("com.braze.events.ContentCardsUpdatedEvent")
            
            // Braze SDK: Call removeSingleSubscription
            val removeMethod = brazeInstance.javaClass.getMethod(
                "removeSingleSubscription",
                Class.forName("com.braze.events.IEventSubscriber"),
                Class::class.java
            )
            removeMethod.invoke(brazeInstance, subscriber, eventClass)
            
            Log.d("BrazeUserSync", "Successfully unsubscribed from Content Cards updates")
        } catch (e: Exception) {
            Log.e("BrazeUserSync", "Error unsubscribing from Content Cards updates: ${e.message}", e)
        }
    }
}
