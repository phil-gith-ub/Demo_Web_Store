package com.example.phil_android_store.data

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Data class representing a single Braze SDK log entry
 */
data class BrazeLogEntry(
    val timestamp: Long,
    val event: String,
    val details: String? = null,
    val type: LogType = LogType.INFO
) {
    val formattedTime: String
        get() = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(timestamp))
    
    enum class LogType {
        INFO,      // General information (e.g., "Entered Store Page")
        REQUEST,   // SDK requests (e.g., "SDK Fetched Banners")
        RESPONSE,  // SDK responses (e.g., "store_banner received")
        EVENT,     // Custom events (e.g., "SDK sent custom event added_to_cart")
        ERROR      // Errors
    }
}

/**
 * Centralized Braze SDK logging manager
 * Tracks all SDK interactions in a readable format
 */
object BrazeLogManager {
    private val logs: SnapshotStateList<BrazeLogEntry> = mutableStateListOf()
    private const val MAX_LOGS = 500 // Keep last 500 logs
    
    /**
     * Get all logs (read-only)
     */
    fun getLogs(): List<BrazeLogEntry> = logs.toList()
    
    /**
     * Clear all logs
     */
    fun clearLogs() {
        logs.clear()
    }
    
    /**
     * Add a log entry
     */
    private fun addLog(entry: BrazeLogEntry) {
        logs.add(0, entry) // Add to beginning for newest first
        if (logs.size > MAX_LOGS) {
            logs.removeAt(logs.size - 1) // Remove oldest
        }
    }
    
    /**
     * Log screen navigation
     */
    fun logScreenEntered(screenName: String) {
        addLog(BrazeLogEntry(
            timestamp = System.currentTimeMillis(),
            event = "Entered $screenName",
            type = BrazeLogEntry.LogType.INFO
        ))
    }
    
    /**
     * Log banner refresh request
     */
    fun logBannerRefreshRequested(placementIds: List<String>) {
        addLog(BrazeLogEntry(
            timestamp = System.currentTimeMillis(),
            event = "SDK Requested Banner Refresh",
            details = placementIds.joinToString(", "),
            type = BrazeLogEntry.LogType.REQUEST
        ))
    }
    
    /**
     * Log banner received
     */
    fun logBannerReceived(placementId: String, isControl: Boolean = false) {
        val status = if (isControl) " (control variant)" else ""
        addLog(BrazeLogEntry(
            timestamp = System.currentTimeMillis(),
            event = "SDK Received Banner",
            details = "$placementId$status",
            type = BrazeLogEntry.LogType.RESPONSE
        ))
    }
    
    /**
     * Log banner not found
     */
    fun logBannerNotFound(placementId: String) {
        addLog(BrazeLogEntry(
            timestamp = System.currentTimeMillis(),
            event = "SDK Banner Not Found",
            details = placementId,
            type = BrazeLogEntry.LogType.RESPONSE
        ))
    }
    
    /**
     * Log content cards refresh request
     */
    fun logContentCardsRefreshRequested() {
        addLog(BrazeLogEntry(
            timestamp = System.currentTimeMillis(),
            event = "SDK Requested Content Cards Refresh",
            type = BrazeLogEntry.LogType.REQUEST
        ))
    }
    
    /**
     * Log content cards received
     */
    fun logContentCardsReceived(count: Int) {
        addLog(BrazeLogEntry(
            timestamp = System.currentTimeMillis(),
            event = "SDK Received Content Cards",
            details = "$count card(s)",
            type = BrazeLogEntry.LogType.RESPONSE
        ))
    }
    
    /**
     * Log content card matched
     */
    fun logContentCardMatched(locationKey: String, cardId: String? = null) {
        val details = cardId?.let { "ID: $it" } ?: locationKey
        addLog(BrazeLogEntry(
            timestamp = System.currentTimeMillis(),
            event = "SDK Content Card Matched",
            details = "$locationKey - $details",
            type = BrazeLogEntry.LogType.RESPONSE
        ))
    }
    
    /**
     * Log custom event
     */
    fun logCustomEvent(eventName: String, properties: Map<String, Any>? = null) {
        val details = properties?.entries?.joinToString(", ") { "${it.key}=${it.value}" }
        addLog(BrazeLogEntry(
            timestamp = System.currentTimeMillis(),
            event = "SDK Sent Custom Event",
            details = "$eventName${if (details != null) " ($details)" else ""}",
            type = BrazeLogEntry.LogType.EVENT
        ))
    }
    
    /**
     * Log user identification
     */
    fun logUserIdentified(userId: String, isAnonymous: Boolean = false) {
        val type = if (isAnonymous) "Anonymous" else "User"
        addLog(BrazeLogEntry(
            timestamp = System.currentTimeMillis(),
            event = "SDK User Identified",
            details = "$type: $userId",
            type = BrazeLogEntry.LogType.INFO
        ))
    }
    
    /**
     * Log user attribute update
     */
    fun logUserAttributeUpdated(attribute: String, value: Any?) {
        addLog(BrazeLogEntry(
            timestamp = System.currentTimeMillis(),
            event = "SDK User Attribute Updated",
            details = "$attribute = $value",
            type = BrazeLogEntry.LogType.EVENT
        ))
    }
    
    /**
     * Log impression
     */
    fun logImpression(type: String, id: String? = null) {
        val details = id ?: type
        addLog(BrazeLogEntry(
            timestamp = System.currentTimeMillis(),
            event = "SDK Logged Impression",
            details = "$type - $details",
            type = BrazeLogEntry.LogType.EVENT
        ))
    }
    
    /**
     * Log click
     */
    fun logClick(type: String, id: String? = null) {
        val details = id ?: type
        addLog(BrazeLogEntry(
            timestamp = System.currentTimeMillis(),
            event = "SDK Logged Click",
            details = "$type - $details",
            type = BrazeLogEntry.LogType.EVENT
        ))
    }
    
    /**
     * Log error
     */
    fun logError(message: String, error: Throwable? = null) {
        val details = error?.message ?: message
        addLog(BrazeLogEntry(
            timestamp = System.currentTimeMillis(),
            event = "SDK Error",
            details = details,
            type = BrazeLogEntry.LogType.ERROR
        ))
    }
}
