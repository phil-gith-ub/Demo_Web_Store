package com.example.phil_android_store.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    var contentCard by remember(positionId) { mutableStateOf<Any?>(null) }
    var shouldRender by remember(positionId) { mutableStateOf(false) }
    
    // Request Content Cards refresh when this composable is first displayed
    LaunchedEffect(positionId) {
        // Request refresh for Content Cards
        BrazeUserSync.requestContentCardsRefresh(context)
        
        // Small delay to allow cards to be fetched
        kotlinx.coroutines.delay(500)
        
        // Get the Content Card by position_id
        contentCard = BrazeUserSync.getContentCardByPositionId(context, positionId)
        
        // Check if card exists and is not a control variant
        if (contentCard != null) {
            try {
                val isControlMethod = contentCard!!.javaClass.getMethod("isControlCard")
                val isControl = isControlMethod.invoke(contentCard) as? Boolean ?: false
                shouldRender = !isControl
            } catch (e: Exception) {
                // If isControlCard method doesn't exist, assume we should render
                shouldRender = true
            }
        } else {
            shouldRender = false
        }
        
        onCardUpdate?.invoke(contentCard)
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
