package com.example.phil_android_store.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Manager for Braze settings (API key, endpoint, and push sender ID)
 * Stores values in SharedPreferences for runtime modification
 */
class BrazeSettingsManager(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("braze_settings", Context.MODE_PRIVATE)
    
    private val KEY_API_KEY = "braze_api_key"
    private val KEY_ENDPOINT = "braze_endpoint"
    private val KEY_PUSH_SENDER_ID = "braze_push_sender_id"
    private val KEY_AUTO_REFRESH_CONTENT_CARDS = "auto_refresh_content_cards"
    
    /**
     * Get the Braze API key.
     * First checks SharedPreferences, then falls back to braze.xml resource
     */
    fun getApiKey(): String {
        val savedKey = prefs.getString(KEY_API_KEY, null)
        if (savedKey != null) {
            return savedKey
        }
        
        // Fall back to resource value
        return try {
            val resourceId = context.resources.getIdentifier(
                "com_braze_api_key",
                "string",
                context.packageName
            )
            if (resourceId != 0) {
                context.getString(resourceId)
            } else {
                ""
            }
        } catch (e: Exception) {
            ""
        }
    }
    
    /**
     * Set the Braze API key (saves to SharedPreferences)
     */
    fun setApiKey(apiKey: String) {
        prefs.edit().putString(KEY_API_KEY, apiKey).apply()
    }
    
    /**
     * Get the Braze SDK endpoint.
     * First checks SharedPreferences, then falls back to braze.xml resource
     */
    fun getEndpoint(): String {
        val savedEndpoint = prefs.getString(KEY_ENDPOINT, null)
        if (savedEndpoint != null) {
            return savedEndpoint
        }
        
        // Fall back to resource value
        return try {
            val resourceId = context.resources.getIdentifier(
                "com_braze_custom_endpoint",
                "string",
                context.packageName
            )
            if (resourceId != 0) {
                context.getString(resourceId)
            } else {
                ""
            }
        } catch (e: Exception) {
            ""
        }
    }
    
    /**
     * Set the Braze SDK endpoint (saves to SharedPreferences)
     */
    fun setEndpoint(endpoint: String) {
        prefs.edit().putString(KEY_ENDPOINT, endpoint).apply()
    }
    
    /**
     * Get the Push Sender ID.
     * First checks SharedPreferences, then falls back to braze.xml resource
     */
    fun getPushSenderId(): String {
        val savedSenderId = prefs.getString(KEY_PUSH_SENDER_ID, null)
        if (savedSenderId != null) {
            return savedSenderId
        }
        
        // Fall back to resource value
        return try {
            val resourceId = context.resources.getIdentifier(
                "com_braze_firebase_cloud_messaging_sender_id",
                "string",
                context.packageName
            )
            if (resourceId != 0) {
                context.getString(resourceId)
            } else {
                ""
            }
        } catch (e: Exception) {
            ""
        }
    }
    
    /**
     * Set the Push Sender ID (saves to SharedPreferences)
     */
    fun setPushSenderId(senderId: String) {
        prefs.edit().putString(KEY_PUSH_SENDER_ID, senderId).apply()
    }
    
    /**
     * Get the auto-refresh Content Cards setting.
     * Defaults to true (enabled) if not set.
     */
    fun getAutoRefreshContentCards(): Boolean {
        return prefs.getBoolean(KEY_AUTO_REFRESH_CONTENT_CARDS, true) // Default to true (enabled)
    }
    
    /**
     * Set the auto-refresh Content Cards setting (saves to SharedPreferences)
     */
    fun setAutoRefreshContentCards(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_REFRESH_CONTENT_CARDS, enabled).apply()
    }
}
