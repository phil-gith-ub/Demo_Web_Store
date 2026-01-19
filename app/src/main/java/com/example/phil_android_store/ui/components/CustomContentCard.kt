package com.example.phil_android_store.ui.components

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.phil_android_store.data.BrazeUserSync

/**
 * Custom Content Card renderer that displays text and image with theme support.
 * Extracts title, description, and image from the Content Card.
 * 
 * @param positionId The position_id value to filter Content Cards by (from extras)
 * @param modifier Modifier for the card container
 */
@Composable
fun CustomContentCard(
    positionId: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme
    
    // Braze SDK: Get the Content Card and refresh when positionId changes
    var contentCard by remember(positionId) { 
        mutableStateOf<Any?>(BrazeUserSync.getContentCardByPositionId(context, positionId))
    }
    
    // Braze SDK: Refresh card when positionId changes
    LaunchedEffect(positionId) {
        kotlinx.coroutines.delay(100)
        contentCard = BrazeUserSync.getContentCardByPositionId(context, positionId)
    }
    
    // Extract card properties using reflection (outside of composable)
    val cardData = remember(contentCard) {
        if (contentCard != null) {
            try {
                val card = contentCard!!
                val getTitleMethod = card.javaClass.getMethod("getTitle")
                val title = getTitleMethod.invoke(card) as? String
                
                val getDescriptionMethod = card.javaClass.getMethod("getCardDescription")
                val description = getDescriptionMethod.invoke(card) as? String
                
                val getImageMethod = card.javaClass.getMethod("getImage")
                val imageUrl = getImageMethod.invoke(card) as? String
                
                val getUrlMethod = card.javaClass.getMethod("getUrlString")
                val cardUrl = getUrlMethod.invoke(card) as? String
                
                CardData(title, description, imageUrl, cardUrl)
            } catch (e: Exception) {
                Log.e("CustomContentCard", "Error extracting card data: ${e.message}", e)
                null
            }
        } else {
            null
        }
    }
    
    if (cardData != null) {
        val title = cardData.title
        val description = cardData.description
        val imageUrl = cardData.imageUrl
        val cardUrl = cardData.cardUrl
            
        // Only render if we have at least title or image
        if (title != null || imageUrl != null) {
            // Braze SDK: Log impression when card is displayed
            LaunchedEffect(contentCard) {
                if (contentCard != null) {
                    try {
                        val logImpressionMethod = contentCard!!.javaClass.getMethod("logImpression")
                        logImpressionMethod.invoke(contentCard)
                        Log.d("CustomContentCard", "Logged Content Card impression")
                    } catch (e: Exception) {
                        Log.e("CustomContentCard", "Error logging impression: ${e.message}", e)
                    }
                }
            }
            
            Card(
                modifier = modifier
                    .fillMaxWidth()
                    .clickable(enabled = cardUrl != null) {
                        // Braze SDK: Log click analytics
                        if (contentCard != null) {
                            try {
                                val logClickMethod = contentCard!!.javaClass.getMethod("logClick")
                                logClickMethod.invoke(contentCard)
                                Log.d("CustomContentCard", "Logged Content Card click")
                            } catch (e: Exception) {
                                Log.e("CustomContentCard", "Error logging click: ${e.message}", e)
                            }
                        }
                        
                        // Handle click - check if it's a deep link
                        if (cardUrl != null) {
                            if (cardUrl.startsWith("philstore://")) {
                                try {
                                    Log.d("CustomContentCard", "Handling deep link: $cardUrl")
                                    val uri = Uri.parse(cardUrl)
                                    val intent = Intent(Intent.ACTION_VIEW, uri)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                    intent.setPackage(context.packageName)
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Log.e("CustomContentCard", "Error handling deep link: ${e.message}", e)
                                }
                            } else {
                                // Handle regular URL if needed
                                Log.d("CustomContentCard", "Card URL: $cardUrl")
                            }
                        }
                    },
                    colors = CardDefaults.cardColors(
                        containerColor = colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Image on the left (if available)
                        if (imageUrl != null) {
                            Box(
                                modifier = Modifier
                                    .width(100.dp)
                                    .height(100.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            ) {
                                Image(
                                    painter = rememberAsyncImagePainter(imageUrl),
                                    contentDescription = title ?: "Content Card Image",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                        
                        // Text content on the right
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (title != null) {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = colorScheme.onSurface,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            
                            if (description != null) {
                                Text(
                                    text = description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colorScheme.onSurfaceVariant,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
}

/**
 * Data class to hold extracted Content Card properties
 */
private data class CardData(
    val title: String?,
    val description: String?,
    val imageUrl: String?,
    val cardUrl: String?
)
