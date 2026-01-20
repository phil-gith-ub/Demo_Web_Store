package com.example.phil_android_store.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONObject

/**
 * Manages user profiles with persistence across app restarts.
 * Supports multiple users and recalls profiles by User ID.
 */
class UserProfileManager(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "user_profiles_prefs",
        Context.MODE_PRIVATE
    )
    private val currentUserIdKey = "current_user_id"
    private val userProfilesKey = "user_profiles"
    
    private val userProfiles: MutableMap<String, UserProfile> by lazy {
        loadAllProfiles()
    }
    
    /**
     * Get current logged-in user ID
     */
    fun getCurrentUserId(): String? {
        return prefs.getString(currentUserIdKey, null)
    }
    
    /**
     * Set current logged-in user ID
     */
    fun setCurrentUserId(userId: String) {
        prefs.edit().putString(currentUserIdKey, userId).apply()
    }
    
    /**
     * Clear current user (logout)
     */
    fun clearCurrentUser() {
        prefs.edit().remove(currentUserIdKey).apply()
    }
    
    /**
     * Get profile for a user ID, or create a new one if it doesn't exist
     */
    fun getProfile(userId: String): UserProfile {
        return userProfiles.getOrPut(userId) {
            UserProfile(userId = userId)
        }
    }
    
    /**
     * Get current user's profile
     */
    fun getCurrentProfile(): UserProfile? {
        val userId = getCurrentUserId()
        return userId?.let { getProfile(it) }
    }
    
    /**
     * Save/update a user profile
     */
    fun saveProfile(profile: UserProfile) {
        userProfiles[profile.userId] = profile
        saveAllProfiles()
    }
    
    /**
     * Check if user is logged in
     */
    fun isLoggedIn(): Boolean {
        return getCurrentUserId() != null
    }
    
    /**
     * Load all profiles from SharedPreferences
     */
    private fun loadAllProfiles(): MutableMap<String, UserProfile> {
        val jsonString = prefs.getString(userProfilesKey, null)
        val profiles = mutableMapOf<String, UserProfile>()
        
        if (jsonString != null) {
            try {
                val jsonObject = JSONObject(jsonString)
                val keys = jsonObject.keys()
                while (keys.hasNext()) {
                    val userId = keys.next()
                    val profileJson = jsonObject.getJSONObject(userId)
                    profiles[userId] = UserProfile(
                        userId = profileJson.getString("userId"),
                        firstName = profileJson.optString("firstName", ""),
                        lastName = profileJson.optString("lastName", ""),
                        email = profileJson.optString("email", ""),
                        mobile = profileJson.optString("mobile", ""),
                        favoriteProductCategory = profileJson.optString("favoriteProductCategory", ""),
                        isDarkModeEnabled = profileJson.optBoolean("isDarkModeEnabled", false),
                        paidMembership = profileJson.optBoolean("paidMembership", false)
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        return profiles
    }
    
    /**
     * Save all profiles to SharedPreferences
     */
    private fun saveAllProfiles() {
        try {
            val jsonObject = JSONObject()
            userProfiles.forEach { (userId, profile) ->
                val profileJson = JSONObject().apply {
                    put("userId", profile.userId)
                    put("firstName", profile.firstName)
                    put("lastName", profile.lastName)
                    put("email", profile.email)
                    put("mobile", profile.mobile)
                    put("favoriteProductCategory", profile.favoriteProductCategory)
                    put("isDarkModeEnabled", profile.isDarkModeEnabled)
                    put("paidMembership", profile.paidMembership)
                }
                jsonObject.put(userId, profileJson)
            }
            prefs.edit().putString(userProfilesKey, jsonObject.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
