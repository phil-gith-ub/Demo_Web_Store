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
    val type: LogType = LogType.INFO,
    val payload: Map<String, Any>? = null // Raw payload data for events
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
            event = "→ $screenName",
            type = BrazeLogEntry.LogType.INFO
        ))
    }
    
    /**
     * Log banner refresh request
     */
    fun logBannerRefreshRequested(placementIds: List<String>) {
        val idsStr = placementIds.joinToString(", ") { "'$it'" }
        addLog(BrazeLogEntry(
            timestamp = System.currentTimeMillis(),
            event = "requestBannerRefresh([$idsStr])",
            type = BrazeLogEntry.LogType.REQUEST
        ))
    }
    
    /**
     * Log banner received
     */
    fun logBannerReceived(placementId: String, isControl: Boolean = false) {
        val status = if (isControl) " (control)" else ""
        addLog(BrazeLogEntry(
            timestamp = System.currentTimeMillis(),
            event = "getBanner('$placementId') → Banner$status",
            type = BrazeLogEntry.LogType.RESPONSE
        ))
    }
    
    /**
     * Log banner not found
     */
    fun logBannerNotFound(placementId: String) {
        addLog(BrazeLogEntry(
            timestamp = System.currentTimeMillis(),
            event = "getBanner('$placementId') → null",
            type = BrazeLogEntry.LogType.RESPONSE
        ))
    }
    
    /**
     * Log content cards refresh request
     */
    fun logContentCardsRefreshRequested() {
        addLog(BrazeLogEntry(
            timestamp = System.currentTimeMillis(),
            event = "requestContentCardsRefresh()",
            type = BrazeLogEntry.LogType.REQUEST
        ))
    }
    
    /**
     * Log content cards received
     */
    fun logContentCardsReceived(count: Int, cards: List<Any>? = null) {
        // Build payload with card data if available
        val payload = cards?.mapIndexed { index, card ->
            try {
                val getIdMethod = card.javaClass.getMethod("getId")
                val cardId = getIdMethod.invoke(card) as? String ?: "unknown"
                val getExtrasMethod = card.javaClass.getMethod("getExtras")
                val extras = getExtrasMethod.invoke(card) as? Map<*, *>
                "card_$index" to mapOf(
                    "id" to cardId,
                    "extras" to (extras?.mapKeys { it.key?.toString() ?: "unknown" } ?: emptyMap())
                )
            } catch (e: Exception) {
                "card_$index" to mapOf("error" to e.message ?: "unknown")
            }
        }?.associate { it.first to it.second }
        
        addLog(BrazeLogEntry(
            timestamp = System.currentTimeMillis(),
            event = "getContentCards() → [$count cards]",
            type = BrazeLogEntry.LogType.RESPONSE,
            payload = payload
        ))
    }
    
    /**
     * Log content card matched
     */
    fun logContentCardMatched(locationKey: String, cardId: String? = null) {
        // Store card ID in payload instead of inline
        val payload = cardId?.let { mapOf("card_id" to it) }
        addLog(BrazeLogEntry(
            timestamp = System.currentTimeMillis(),
            event = "matchCard(location='$locationKey')",
            type = BrazeLogEntry.LogType.RESPONSE,
            payload = payload
        ))
    }
    
    /**
     * Log custom event
     */
    fun logCustomEvent(eventName: String, properties: Map<String, Any>? = null) {
        // Format as raw code style: logCustomEvent('eventName') or logPurchaseEvent('purchase')
        val functionName = if (eventName == "purchase") "logPurchaseEvent" else "logCustomEvent"
        addLog(BrazeLogEntry(
            timestamp = System.currentTimeMillis(),
            event = "$functionName('$eventName')",
            details = if (properties != null && properties.isNotEmpty()) "click to view payload" else null,
            type = BrazeLogEntry.LogType.EVENT,
            payload = properties
        ))
    }
    
    /**
     * Log user identification
     */
    fun logUserIdentified(userId: String, isAnonymous: Boolean = false) {
        val method = if (isAnonymous) "changeUser" else "changeUser"
        addLog(BrazeLogEntry(
            timestamp = System.currentTimeMillis(),
            event = "$method('$userId')",
            type = BrazeLogEntry.LogType.INFO
        ))
    }
    
    /**
     * Log user attribute update
     */
    fun logUserAttributeUpdated(attribute: String, value: Any?) {
        addLog(BrazeLogEntry(
            timestamp = System.currentTimeMillis(),
            event = "setCustomUserAttribute('$attribute', $value)",
            type = BrazeLogEntry.LogType.EVENT
        ))
    }
    
    /**
     * Log impression
     */
    fun logImpression(type: String, id: String? = null) {
        val idStr = id?.let { "('$it')" } ?: "()"
        addLog(BrazeLogEntry(
            timestamp = System.currentTimeMillis(),
            event = "logImpression$idStr",
            details = type,
            type = BrazeLogEntry.LogType.EVENT
        ))
    }
    
    /**
     * Log click
     */
    fun logClick(type: String, id: String? = null) {
        val idStr = id?.let { "('$it')" } ?: "()"
        addLog(BrazeLogEntry(
            timestamp = System.currentTimeMillis(),
            event = "logClick$idStr",
            details = type,
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
            event = "ERROR: $details",
            type = BrazeLogEntry.LogType.ERROR
        ))
    }
}
