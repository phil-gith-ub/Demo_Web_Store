package com.example.phil_android_store.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.phil_android_store.data.BrazeUserSync
import com.example.phil_android_store.data.rememberCachedContentCards

/**
 * Composable that displays a Braze Content Card filtered by position_id.
 * Uses custom rendering for text and image with theme support.
 * The card is collapsible - it only renders when a card is available.
 * 
 * @param positionId The position_id value to filter Content Cards by (from extras)
 * @param modifier Modifier for the card container
 * @param onCardUpdate Callback when card is updated (receives card or null)
 */
@Composable
fun BrazeContentCard(
    positionId: String,
    modifier: Modifier = Modifier,
    onCardUpdate: ((Any?) -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Read content cards from cache (pre-loaded on session start, updated after events)
    val cachedCards = rememberCachedContentCards()
    
    // Find card by position_id from cache
    var contentCard by remember(positionId) { mutableStateOf<Any?>(null) }
    var shouldRender by remember(positionId) { mutableStateOf(false) }
    
    // Process cached cards on initial load and when cache updates
    LaunchedEffect(cachedCards, positionId) {
        val foundCard = com.example.phil_android_store.data.BrazeContentManager.getCachedContentCardByPositionId(positionId)
        contentCard = foundCard
        
        // Check if card exists and is not a control variant
        if (foundCard != null) {
            try {
                val isControlMethod = foundCard.javaClass.getMethod("isControlCard")
                val isControl = isControlMethod.invoke(foundCard) as? Boolean ?: false
                shouldRender = !isControl
            } catch (e: Exception) {
                shouldRender = true
            }
        } else {
            shouldRender = false
        }
        
        onCardUpdate?.invoke(foundCard)
    }
    
    // Braze SDK: Subscribe to Content Cards updates (for real-time updates when cache refreshes)
    DisposableEffect(positionId) {
        // Braze SDK: Subscribe to Content Cards updates - this will notify us when cards change
        val subscription = BrazeUserSync.subscribeToContentCardsUpdates(context) { cards ->
            // This callback is called whenever Content Cards are updated
            // Braze SDK: Re-fetch the card by position_id from the updated cards
            val updatedCard = BrazeUserSync.getContentCardByPositionId(context, positionId)
            contentCard = updatedCard
            
            // Check if card exists and is not a control variant
            if (updatedCard != null) {
                try {
                    val isControlMethod = updatedCard.javaClass.getMethod("isControlCard")
                    val isControl = isControlMethod.invoke(updatedCard) as? Boolean ?: false
                    shouldRender = !isControl
                } catch (e: Exception) {
                    shouldRender = true
                }
            } else {
                shouldRender = false
            }
            
            onCardUpdate?.invoke(updatedCard)
        }
        
        // Braze SDK: Unsubscribe when composable is disposed
        onDispose {
            if (subscription != null) {
                BrazeUserSync.unsubscribeFromContentCardsUpdates(context, subscription)
            }
        }
    }
    
    // Only render if card is available and not a control variant
    if (shouldRender && contentCard != null) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            contentAlignment = Alignment.Center
        ) {
            // Use custom renderer instead of WebView
            CustomContentCard(
                positionId = positionId,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
