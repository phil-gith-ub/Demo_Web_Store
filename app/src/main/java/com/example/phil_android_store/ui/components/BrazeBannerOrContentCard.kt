package com.example.phil_android_store.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.braze.Braze
import com.example.phil_android_store.data.BrazeUserSync
import com.example.phil_android_store.data.rememberCachedBanner
import com.example.phil_android_store.data.rememberCachedContentCards
import kotlinx.coroutines.launch

/**
 * Shared container that displays either a Braze Banner or a Content Card.
 * Priority: Banner first, then Content Card if no banner is available.
 * 
 * @param bannerPlacementId The banner placement ID (e.g., "cart_banner")
 * @param contentCardPositionId The Content Card position_id from extras (e.g., "cart_content_card")
 * @param modifier Modifier for the container
 */
@Composable
fun BrazeBannerOrContentCard(
    bannerPlacementId: String,
    contentCardPositionId: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // Read from cache (pre-loaded on session start, updated after events)
    val cachedBanner = rememberCachedBanner(bannerPlacementId)
    val cachedCards = rememberCachedContentCards()
    
    var hasBanner by remember(bannerPlacementId) { mutableStateOf(false) }
    var hasContentCard by remember(contentCardPositionId) { mutableStateOf(false) }
    
    // Check cached banner
    LaunchedEffect(cachedBanner) {
        if (cachedBanner != null) {
            try {
                val isControlMethod = cachedBanner.javaClass.getMethod("isControl")
                val isControl = isControlMethod.invoke(cachedBanner) as? Boolean ?: false
                hasBanner = !isControl
            } catch (e: Exception) {
                hasBanner = true
            }
        } else {
            hasBanner = false
        }
    }
    
    // Check cached content cards
    LaunchedEffect(cachedCards, contentCardPositionId) {
        val foundCard = com.example.phil_android_store.data.BrazeContentManager.getCachedContentCardByPositionId(contentCardPositionId)
        if (foundCard != null) {
            try {
                val isControlMethod = foundCard.javaClass.getMethod("isControlCard")
                val isControl = isControlMethod.invoke(foundCard) as? Boolean ?: false
                hasContentCard = !isControl
            } catch (e: Exception) {
                hasContentCard = true
            }
        } else {
            hasContentCard = false
        }
    }
    
    // Braze SDK: Subscribe to Content Cards updates (for real-time updates when cache refreshes)
    DisposableEffect(bannerPlacementId, contentCardPositionId) {
        // Braze SDK: Subscribe to Content Cards updates (recommended approach per Braze docs)
        // This will notify us whenever Content Cards are updated
        val subscription = BrazeUserSync.subscribeToContentCardsUpdates(context) { cards ->
            // Braze SDK: Re-check for content card when cards are updated
            val updatedCard = BrazeUserSync.getContentCardByPositionId(context, contentCardPositionId)
            if (updatedCard != null) {
                try {
                    val isControlMethod = updatedCard.javaClass.getMethod("isControlCard")
                    val isControl = isControlMethod.invoke(updatedCard) as? Boolean ?: false
                    hasContentCard = !isControl
                } catch (e: Exception) {
                    hasContentCard = true
                }
            } else {
                hasContentCard = false
            }
        }
        
        // Braze SDK: Unsubscribe when composable is disposed
        onDispose {
            if (subscription != null) {
                BrazeUserSync.unsubscribeFromContentCardsUpdates(context, subscription)
            }
        }
    }
    
    // Show banner if available, otherwise show content card
    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        contentAlignment = Alignment.Center
    ) {
        if (hasBanner) {
            BrazeBanner(
                placementId = bannerPlacementId,
                modifier = Modifier.fillMaxWidth()
            )
        } else if (hasContentCard) {
            BrazeContentCard(
                positionId = contentCardPositionId,
                modifier = Modifier.fillMaxWidth()
            )
        }
        // If neither is available, nothing is rendered (container collapses)
    }
}
