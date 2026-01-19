package com.example.phil_android_store.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
    var contentCard by remember(positionId) { mutableStateOf<Any?>(null) }
    var shouldRender by remember(positionId) { mutableStateOf(false) }
    
    // Subscribe to Content Cards updates using proper event-based pattern
    DisposableEffect(positionId) {
        // Request initial refresh
        BrazeUserSync.requestContentCardsRefresh(context)
        
        // Subscribe to Content Cards updates - this will notify us when cards change
        val subscription = BrazeUserSync.subscribeToContentCardsUpdates(context) { cards ->
            // This callback is called whenever Content Cards are updated
            // Re-fetch the card by position_id from the updated cards
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
        
        // Get initial Content Card after a short delay
        scope.launch {
            kotlinx.coroutines.delay(500)
            val initialCard = BrazeUserSync.getContentCardByPositionId(context, positionId)
            contentCard = initialCard
            
            // Check if card exists and is not a control variant
            if (initialCard != null) {
                try {
                    val isControlMethod = initialCard.javaClass.getMethod("isControlCard")
                    val isControl = isControlMethod.invoke(initialCard) as? Boolean ?: false
                    shouldRender = !isControl
                } catch (e: Exception) {
                    // If isControlCard method doesn't exist, assume we should render
                    shouldRender = true
                }
            } else {
                shouldRender = false
            }
            
            onCardUpdate?.invoke(initialCard)
        }
        
        // Unsubscribe when composable is disposed
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
