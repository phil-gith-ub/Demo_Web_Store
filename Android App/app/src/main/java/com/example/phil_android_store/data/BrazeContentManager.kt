package com.example.phil_android_store.data

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Centralized manager for Braze banners and content cards.
 * Pre-loads content on session start and caches it to minimize refreshes.
 * Only refreshes after events or changeUser, and only updates UI if content actually changed.
 */
object BrazeContentManager {
    // Cache for banners by placement ID
    private val bannerCache = mutableMapOf<String, BannerCacheEntry>()
    
    // Cache for content cards (all cards, filtered by location KVP in components)
    private var contentCardsCache: List<Any> = emptyList()
    private var contentCardsCacheTimestamp: Long = 0
    
    // State to notify composables of changes
    private val cacheUpdateTrigger = mutableStateOf(0)
    
    // All banner placement IDs used in the app
    private val allBannerPlacementIds = listOf(
        "store_page_banner",
        "cart_banner",
        "content_banner",
        "tile_banner"
    )
    
    // Coroutine scope for async operations
    private val scope = CoroutineScope(Dispatchers.Main)
    
    /**
     * Cache entry for a banner
     */
    private data class BannerCacheEntry(
        val banner: Any?,
        val cachedBannerId: String?,
        val timestamp: Long
    ) {
        fun getBannerId(): String? {
            return cachedBannerId ?: try {
                banner?.javaClass?.getMethod("getId")?.invoke(banner)?.toString()
            } catch (e: Exception) {
                null
            }
        }
    }
    
    /**
     * Pre-load all banners and content cards on session start.
     * Should be called once when the app starts or after changeUser.
     * Caches immediately, then refreshes with multiple retries to catch content as soon as available.
     */
    fun preloadAllContent(context: Context) {
        // Request refresh immediately
        BrazeUserSync.requestBannerRefresh(context, allBannerPlacementIds)
        BrazeUserSync.requestContentCardsRefresh(context)
        
        // Cache immediately (may be empty initially, but components will observe updates)
        cacheAllBanners(context)
        cacheAllContentCards(context)
        cacheUpdateTrigger.value++
        
        // Retry multiple times with increasing delays to catch content as soon as it's available
        scope.launch {
            // First retry after 300ms
            delay(300)
            cacheAllBanners(context)
            cacheAllContentCards(context)
            cacheUpdateTrigger.value++
            
            // Second retry after 600ms
            delay(300)
            cacheAllBanners(context)
            cacheAllContentCards(context)
            cacheUpdateTrigger.value++
            
            // Final retry after 1200ms total
            delay(600)
            cacheAllBanners(context)
            cacheAllContentCards(context)
            cacheUpdateTrigger.value++
        }
    }
    
    /**
     * Refresh all content after an event or changeUser.
     * Always updates cache (even if content is null) to remove old content.
     * Makes ONE refresh request, then ONE retry for latency - no more to avoid hitting Braze limits.
     */
    fun refreshAllContent(context: Context, onContentChanged: (() -> Unit)? = null) {
        scope.launch {
            // Request refresh from Braze (ONCE)
            BrazeUserSync.requestBannerRefresh(context, allBannerPlacementIds)
            BrazeUserSync.requestContentCardsRefresh(context)
            
            // First attempt after 500ms
            delay(500)
            if (updateCacheFromBraze(context)) {
                cacheUpdateTrigger.value++
            }
            
            // ONE retry after 1500ms total (additional 1000ms delay) for latency
            delay(1000)
            if (updateCacheFromBraze(context)) {
                cacheUpdateTrigger.value++
            }
            
            // Notify that refresh completed
            onContentChanged?.invoke()
        }
    }
    
    /**
     * Force refresh all content with data flush (for manual pull-to-refresh).
     * Flushes all Braze data, then refreshes banners and content cards.
     */
    fun forceRefreshAllContent(context: Context, onContentChanged: (() -> Unit)? = null) {
        scope.launch {
            // Flush all Braze data first (including IAM)
            val brazeInstance = com.braze.Braze.getInstance(context)
            brazeInstance.requestImmediateDataFlush()
            
            // Request refresh from Braze
            BrazeUserSync.requestBannerRefresh(context, allBannerPlacementIds)
            BrazeUserSync.requestContentCardsRefresh(context)
            
            // First attempt after 500ms
            delay(500)
            if (updateCacheFromBraze(context)) {
                cacheUpdateTrigger.value++
            }
            
            // ONE retry after 1500ms total (additional 1000ms delay) for latency
            delay(1000)
            if (updateCacheFromBraze(context)) {
                cacheUpdateTrigger.value++
            }
            
            // Notify that refresh completed
            onContentChanged?.invoke()
        }
    }
    
    /**
     * Internal method to update cache from Braze (always updates, even if null)
     * Returns true if cache was updated, false otherwise
     */
    private fun updateCacheFromBraze(context: Context): Boolean {
        var updated = false
        
        // Always update banners (even if null - removes old content)
        for (placementId in allBannerPlacementIds) {
            val newBanner = BrazeUserSync.getBanner(context, placementId)
            val newBannerId = getBannerId(newBanner)
            val cachedEntry = bannerCache[placementId]
            
            // Always update cache (even if null) to remove old banners
            // Only mark as updated if banner actually changed
            if (newBannerId != cachedEntry?.getBannerId()) {
                bannerCache[placementId] = BannerCacheEntry(
                    banner = newBanner,
                    cachedBannerId = newBannerId,
                    timestamp = System.currentTimeMillis()
                )
                updated = true
            } else if (newBanner == null && cachedEntry?.banner != null) {
                // Banner was removed (became null) - always update
                bannerCache[placementId] = BannerCacheEntry(
                    banner = null,
                    cachedBannerId = null,
                    timestamp = System.currentTimeMillis()
                )
                updated = true
            }
        }
        
        // Always update content cards (even if empty - removes old content)
        val newCards = BrazeUserSync.getContentCards(context)
        val newCardsIds = newCards.mapNotNull { getCardId(it) }.sorted()
        val cachedCardsIds = contentCardsCache.mapNotNull { getCardId(it) }.sorted()
        
        if (newCardsIds != cachedCardsIds) {
            contentCardsCache = newCards
            contentCardsCacheTimestamp = System.currentTimeMillis()
            updated = true
        }
        
        return updated
    }
    
    /**
     * Cache all banners (internal method)
     */
    private fun cacheAllBanners(context: Context) {
        for (placementId in allBannerPlacementIds) {
            val banner = BrazeUserSync.getBanner(context, placementId)
            val bannerId = getBannerId(banner)
            bannerCache[placementId] = BannerCacheEntry(
                banner = banner,
                cachedBannerId = bannerId,
                timestamp = System.currentTimeMillis()
            )
        }
    }
    
    /**
     * Cache all content cards (internal method)
     */
    private fun cacheAllContentCards(context: Context) {
        contentCardsCache = BrazeUserSync.getContentCards(context)
        contentCardsCacheTimestamp = System.currentTimeMillis()
    }
    
    /**
     * Get cached banner for a placement ID
     */
    fun getCachedBanner(placementId: String): Any? {
        return bannerCache[placementId]?.banner
    }
    
    /**
     * Get cached content cards
     */
    fun getCachedContentCards(): List<Any> {
        return contentCardsCache
    }
    
    /**
     * Get content card by position_id from cache
     */
    fun getCachedContentCardByPositionId(positionId: String): Any? {
        for (card in contentCardsCache) {
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
     * Get banner ID for change detection
     */
    private fun getBannerId(banner: Any?): String? {
        if (banner == null) return null
        return try {
            banner.javaClass.getMethod("getId")?.invoke(banner)?.toString()
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Get card ID for change detection
     */
    private fun getCardId(card: Any?): String? {
        if (card == null) return null
        return try {
            card.javaClass.getMethod("getId")?.invoke(card)?.toString()
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Clear cache (useful for testing or logout)
     */
    fun clearCache() {
        bannerCache.clear()
        contentCardsCache = emptyList()
        contentCardsCacheTimestamp = 0
        cacheUpdateTrigger.value++
    }
    
    /**
     * Get update trigger state for composables to observe changes
     */
    fun getUpdateTriggerState() = cacheUpdateTrigger
}

/**
 * Composable helper to remember cached banner state and observe updates
 */
@Composable
fun rememberCachedBanner(placementId: String): Any? {
    val updateTriggerState = BrazeContentManager.getUpdateTriggerState()
    // Observe state changes using 'by' to trigger recomposition
    val updateTrigger by updateTriggerState
    return remember(placementId, updateTrigger) {
        BrazeContentManager.getCachedBanner(placementId)
    }
}

/**
 * Composable helper to remember cached content cards and observe updates
 */
@Composable
fun rememberCachedContentCards(): List<Any> {
    val updateTriggerState = BrazeContentManager.getUpdateTriggerState()
    // Observe state changes using 'by' to trigger recomposition
    val updateTrigger by updateTriggerState
    return remember(updateTrigger) {
        BrazeContentManager.getCachedContentCards()
    }
}
