package com.example.phil_android_store.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.braze.Braze
import com.example.phil_android_store.data.BrazeUserSync

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
    var hasBanner by remember(bannerPlacementId) { mutableStateOf(false) }
    var hasContentCard by remember(contentCardPositionId) { mutableStateOf(false) }
    
    // Check for banner and content card availability
    LaunchedEffect(bannerPlacementId, contentCardPositionId) {
        // Request refresh for both
        BrazeUserSync.requestBannerRefresh(context, listOf(bannerPlacementId))
        BrazeUserSync.requestContentCardsRefresh(context)
        
        // Small delay to allow content to be fetched
        kotlinx.coroutines.delay(500)
        
        // Check if banner exists and is not control
        val banner = BrazeUserSync.getBanner(context, bannerPlacementId)
        if (banner != null) {
            try {
                val isControlMethod = banner.javaClass.getMethod("isControl")
                val isControl = isControlMethod.invoke(banner) as? Boolean ?: false
                hasBanner = !isControl
            } catch (e: Exception) {
                hasBanner = true
            }
        } else {
            hasBanner = false
        }
        
        // Check if content card exists and is not control
        val contentCard = BrazeUserSync.getContentCardByPositionId(context, contentCardPositionId)
        if (contentCard != null) {
            try {
                val isControlMethod = contentCard.javaClass.getMethod("isControlCard")
                val isControl = isControlMethod.invoke(contentCard) as? Boolean ?: false
                hasContentCard = !isControl
            } catch (e: Exception) {
                hasContentCard = true
            }
        } else {
            hasContentCard = false
        }
    }
    
    // Subscribe to Content Cards updates (if available)
    LaunchedEffect(contentCardPositionId) {
        try {
            val brazeInstance = Braze.getInstance(context)
            val subscribeMethod = brazeInstance.javaClass.getMethod(
                "subscribeToContentCardsUpdates",
                java.util.function.Consumer::class.java
            )
            subscribeMethod.invoke(
                brazeInstance,
                java.util.function.Consumer<Any> {
                    // Re-check for content card when cards are updated
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
            )
        } catch (e: Exception) {
            // Subscription method might not be available
            android.util.Log.d("BrazeBannerOrContentCard", "Could not subscribe to Content Cards updates: ${e.message}")
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
