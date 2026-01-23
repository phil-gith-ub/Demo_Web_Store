package com.example.phil_android_store.ui.screens.content

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.rememberAsyncImagePainter
import androidx.compose.foundation.Image
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.braze.Braze
import com.example.phil_android_store.data.BrazeSettingsManager
import com.example.phil_android_store.data.BrazeUserSync
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Content screen for demonstrating Braze Content Cards and Banners.
 * This page can be used to manually install content cards and banners for client demonstrations.
 */
@Composable
fun ContentScreen() {
    val context = LocalContext.current
    val settingsManager = remember { BrazeSettingsManager(context) }
    var notificationMessage by remember { mutableStateOf<String?>(null) }
    
    // Braze SDK: Auto-refresh Content Cards when entering this screen (if enabled in settings)
    LaunchedEffect(Unit) {
        val autoRefresh = settingsManager.getAutoRefreshContentCards()
        if (autoRefresh) {
            Log.d("ContentScreen", "Auto-refresh enabled: Requesting Content Cards refresh")
            BrazeUserSync.requestContentCardsRefresh(context)
        } else {
            Log.d("ContentScreen", "Auto-refresh disabled: Skipping Content Cards refresh")
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top row with buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Enable Push button on the left
                Button(
                    onClick = {
                        // Braze SDK: Log custom event to Braze
                        val brazeInstance = Braze.getInstance(context)
                        brazeInstance.logCustomEvent("enable_push")
                        brazeInstance.requestImmediateDataFlush()
                        notificationMessage = "Custom Event Sent"
                    },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp), // Fixed minimum height for consistent button size
                    contentPadding = ButtonDefaults.ButtonWithIconContentPadding
                ) {
                    Text(
                        text = "Enable Push",
                        style = MaterialTheme.typography.labelMedium,
                        textAlign = TextAlign.Center,
                        maxLines = 2
                    )
                }
                
                // Push Notification button in the middle
                Button(
                    onClick = {
                        // Braze SDK: Log custom event to Braze
                        val brazeInstance = Braze.getInstance(context)
                        brazeInstance.logCustomEvent("push_notification")
                        brazeInstance.requestImmediateDataFlush()
                        notificationMessage = "Custom Event Sent"
                    },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp), // Fixed minimum height for consistent button size
                    contentPadding = ButtonDefaults.ButtonWithIconContentPadding
                ) {
                    Text(
                        text = "Push Notification",
                        style = MaterialTheme.typography.labelMedium,
                        textAlign = TextAlign.Center,
                        maxLines = 2
                    )
                }
                
                // User Action button on the right
                Button(
                    onClick = {
                        // Braze SDK: Log custom event to Braze
                        val brazeInstance = Braze.getInstance(context)
                        brazeInstance.logCustomEvent("user_action_button")
                        brazeInstance.requestImmediateDataFlush()
                        notificationMessage = "Custom Event Sent"
                    },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp), // Fixed minimum height for consistent button size
                    contentPadding = ButtonDefaults.ButtonWithIconContentPadding
                ) {
                    Text(
                        text = "User Action",
                        style = MaterialTheme.typography.labelMedium,
                        textAlign = TextAlign.Center,
                        maxLines = 2
                    )
                }
            }
            
            // Notification banner positioned underneath the buttons
            AnimatedVisibility(
                visible = notificationMessage != null,
                enter = fadeIn(animationSpec = tween(300)),
                exit = fadeOut(animationSpec = tween(300)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                notificationMessage?.let { message ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 0.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shadowElevation = 4.dp
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = message,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
            
            // Content Card: Tile 1 (1x2 banner style)
            // Braze SDK: Custom Content Card filtered by location = tile_1
            // This demonstrates how to create a custom Content Card fragment with key-value pair filtering
            Tile1ContentCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp) // Gap for notification banner
            )
            
            // Blank content area for additional content cards and banners
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                // Empty space for additional content cards and banners
                // This area can be used to manually install more Braze Content Cards and Banners
            }
        }
    }
    
    // Auto-dismiss notification after duration
    LaunchedEffect(notificationMessage) {
        if (notificationMessage != null) {
            delay(2000L)
            notificationMessage = null
        }
    }
}

/**
 * Content Card Fragment: Tile 1
 * 
 * Braze SDK: Custom Content Card implementation
 * - Filters Content Cards by key-value pair: location = tile_1
 * - Displays as 1x2 banner style (full width, 2:1 aspect ratio)
 * - Logs analytics (impressions and clicks) to Braze dashboard
 * - Shows placeholder when no content is available
 * 
 * Debugging: Check Logcat for "Tile1ContentCard" tag to see:
 * - How many cards are received
 * - All extras for each card
 * - Which card (if any) matches the filter
 * 
 * Common issues:
 * - Card not received: Check if Content Cards are enabled in Braze dashboard
 * - Card filtered out: Verify the extras key is exactly "location" (case-sensitive in Braze dashboard)
 * - Card value mismatch: Verify the value in extras is exactly "tile_1" (no spaces, correct case)
 * 
 * To duplicate this for another card:
 * 1. Copy this function and rename (e.g., Tile2ContentCard)
 * 2. Change the locationKey value (e.g., "tile_2")
 * 3. Update the placeholder text accordingly
 * 4. Add the new component to ContentScreen below this one
 */
@Composable
fun Tile1ContentCard(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme
    val scope = rememberCoroutineScope()
    
    // Braze SDK: Key-value pair filter - location = tile_1
    val locationKey = "tile_1"
    
    // Braze SDK: State to hold the filtered Content Card
    var contentCard by remember { mutableStateOf<Any?>(null) }
    var hasCard by remember { mutableStateOf(false) }
    
    // Braze SDK: Subscribe to Content Cards updates
    DisposableEffect(Unit) {
        // Braze SDK: Request initial refresh
        BrazeUserSync.requestContentCardsRefresh(context)
        
        // Braze SDK: Subscribe to Content Cards updates - this will notify us when cards change
        val subscription = BrazeUserSync.subscribeToContentCardsUpdates(context) { cards ->
            // Braze SDK: Debug logging - log all received cards and their extras
            Log.d("Tile1ContentCard", "Received ${cards.size} Content Cards")
            
            // Braze SDK: Filter cards by location = tile_1 from extras
            var foundCard: Any? = null
            for (card in cards) {
                // Check if card is a control card (try multiple methods/approaches)
                var isControl = false
                try {
                    // Try isControlCard() method
                    val isControlMethod = card.javaClass.getMethod("isControlCard")
                    isControl = isControlMethod.invoke(card) as? Boolean ?: false
                } catch (e: NoSuchMethodException) {
                    // Method doesn't exist, try checking class name or other methods
                    try {
                        // Try isControl property (Kotlin style)
                        val isControlField = card.javaClass.getDeclaredField("isControl")
                        isControlField.isAccessible = true
                        isControl = isControlField.get(card) as? Boolean ?: false
                    } catch (e2: Exception) {
                        // Check class name for ControlCard
                        val className = card.javaClass.simpleName
                        isControl = className.contains("Control", ignoreCase = true)
                        if (isControl) {
                            Log.d("Tile1ContentCard", "Detected control card by class name: $className")
                        }
                    }
                } catch (e: Exception) {
                    // If we can't determine, assume it's not a control card and proceed
                    Log.d("Tile1ContentCard", "Could not check control status, proceeding: ${e.message}")
                }
                
                if (isControl) {
                    Log.d("Tile1ContentCard", "Skipping control card")
                    continue
                }
                
                // Try to get extras and process the card
                try {
                    // Braze SDK: Get extras (key-value pairs) from card
                    val getExtrasMethod = card.javaClass.getMethod("getExtras")
                    val extras = getExtrasMethod.invoke(card) as? Map<*, *>
                    
                    // Debug: Log all extras for this card and card ID
                    try {
                        val getIdMethod = card.javaClass.getMethod("getId")
                        val cardId = getIdMethod.invoke(card) as? String
                        
                        // Get title and description for better logging
                        val getTitleMethod = card.javaClass.getMethod("getTitle")
                        val title = getTitleMethod.invoke(card) as? String
                        
                        Log.d("Tile1ContentCard", "=== Card Details ===")
                        Log.d("Tile1ContentCard", "Card ID: $cardId")
                        Log.d("Tile1ContentCard", "Title: $title")
                        Log.d("Tile1ContentCard", "Card class: ${card.javaClass.simpleName}")
                        Log.d("Tile1ContentCard", "Extras keys: ${extras?.keys?.joinToString(", ")}")
                        Log.d("Tile1ContentCard", "Extras full: $extras")
                        
                        // Log each extra key-value pair individually
                        if (extras != null) {
                            for ((key, value) in extras) {
                                Log.d("Tile1ContentCard", "  Extra: '$key' = '$value' (type: ${value?.javaClass?.simpleName})")
                            }
                        }
                    } catch (e: Exception) {
                        Log.d("Tile1ContentCard", "Could not get card details: ${e.message}, but extras: $extras")
                    }
                    Log.d("Tile1ContentCard", "Looking for location = $locationKey")
                    
                    // Filter by location = tile_1
                    // Match web code approach: Check multiple possible keys (location, card_id, etc.)
                    if (extras != null) {
                        // Check all keys in extras (case-insensitive) for "location"
                        var locationValue: Any? = null
                        var cardIdValue: Any? = null
                        
                        for ((key, value) in extras) {
                            val keyStr = key?.toString()?.lowercase()
                            when (keyStr) {
                                "location" -> locationValue = value
                                "card_id", "cardid" -> cardIdValue = value
                            }
                        }
                        
                        // Also try direct access with different cases
                        if (locationValue == null) {
                            locationValue = extras["location"] ?: extras["Location"] ?: extras["LOCATION"]
                        }
                        if (cardIdValue == null) {
                            cardIdValue = extras["card_id"] ?: extras["cardId"] ?: extras["Card_Id"]
                        }
                        
                        Log.d("Tile1ContentCard", "Card location value: $locationValue")
                        Log.d("Tile1ContentCard", "Card card_id value: $cardIdValue")
                        
                        // Match if location = tile_1 OR if card_id matches (for web compatibility)
                        val matches = locationValue?.toString() == locationKey || 
                                     cardIdValue?.toString() == locationKey ||
                                     cardIdValue?.toString()?.contains("tile_1") == true
                        
                        if (matches) {
                            Log.d("Tile1ContentCard", "✓ Found matching card! location=$locationValue, card_id=$cardIdValue")
                            foundCard = card
                            break
                        } else {
                            Log.d("Tile1ContentCard", "✗ Card does not match - location='$locationValue', card_id='$cardIdValue' (looking for '$locationKey')")
                        }
                    } else {
                        Log.d("Tile1ContentCard", "Card has no extras")
                    }
                } catch (e: Exception) {
                    // Skip cards that we can't process
                    Log.e("Tile1ContentCard", "Error processing card (skipping): ${e.message}", e)
                    continue
                }
            }
            
            contentCard = foundCard
            hasCard = foundCard != null
            Log.d("Tile1ContentCard", "Final result: hasCard = $hasCard")
        }
        
        // Braze SDK: Initial check - subscription callback will handle card updates
        // No need for separate initial check since subscription will fire when cards are available
        Log.d("Tile1ContentCard", "Subscribed to Content Cards updates - waiting for cards...")
        
        // Braze SDK: Cleanup - unsubscribe when component is removed
        onDispose {
            BrazeUserSync.unsubscribeFromContentCardsUpdates(context, subscription)
        }
    }
    
    // Braze SDK: Extract card properties (title, description, image, URL)
    // Handle different card types (ShortNewsCard, CaptionedImage, etc.)
    val cardData = remember(contentCard) {
        if (contentCard != null) {
            try {
                val card = contentCard!!
                var title: String? = null
                var description: String? = null
                var imageUrl: String? = null
                var cardUrl: String? = null
                
                // Get title (most cards have this)
                try {
                    val getTitleMethod = card.javaClass.getMethod("getTitle")
                    title = getTitleMethod.invoke(card) as? String
                } catch (e: Exception) {
                    Log.d("Tile1ContentCard", "Card does not have getTitle method")
                }
                
                // Get description - try multiple method names for different card types
                try {
                    val getDescriptionMethod = card.javaClass.getMethod("getCardDescription")
                    description = getDescriptionMethod.invoke(card) as? String
                } catch (e: NoSuchMethodException) {
                    // Try alternative method names
                    try {
                        val getDescriptionMethod = card.javaClass.getMethod("getDescription")
                        description = getDescriptionMethod.invoke(card) as? String
                    } catch (e2: Exception) {
                        Log.d("Tile1ContentCard", "Card does not have description method")
                    }
                }
                
                // Get image URL - try multiple method names
                try {
                    val getImageMethod = card.javaClass.getMethod("getImage")
                    imageUrl = getImageMethod.invoke(card) as? String
                } catch (e: NoSuchMethodException) {
                    try {
                        val getImageMethod = card.javaClass.getMethod("getImageUrl")
                        imageUrl = getImageMethod.invoke(card) as? String
                    } catch (e2: Exception) {
                        Log.d("Tile1ContentCard", "Card does not have image method")
                    }
                }
                
                // Get URL
                try {
                    val getUrlMethod = card.javaClass.getMethod("getUrlString")
                    cardUrl = getUrlMethod.invoke(card) as? String
                } catch (e: Exception) {
                    try {
                        val getUrlMethod = card.javaClass.getMethod("getUrl")
                        cardUrl = getUrlMethod.invoke(card) as? String
                    } catch (e2: Exception) {
                        Log.d("Tile1ContentCard", "Card does not have URL method")
                    }
                }
                
                // Debug: Log extracted card data
                Log.d("Tile1ContentCard", "=== Extracted Card Data ===")
                Log.d("Tile1ContentCard", "Title: $title")
                Log.d("Tile1ContentCard", "Description: $description")
                Log.d("Tile1ContentCard", "Image URL: $imageUrl")
                Log.d("Tile1ContentCard", "Card URL: $cardUrl")
                
                CardData(title, description, imageUrl, cardUrl)
            } catch (e: Exception) {
                Log.e("Tile1ContentCard", "Error extracting card data: ${e.message}", e)
                e.printStackTrace()
                null
            }
        } else {
            null
        }
    }
    
    // Braze SDK: Log impression when card is displayed
    LaunchedEffect(contentCard) {
        if (contentCard != null) {
            try {
                val logImpressionMethod = contentCard!!.javaClass.getMethod("logImpression")
                logImpressionMethod.invoke(contentCard)
                Log.d("Tile1ContentCard", "Logged Content Card impression")
            } catch (e: Exception) {
                Log.e("Tile1ContentCard", "Error logging impression: ${e.message}", e)
            }
        }
    }
    
    // Display card or placeholder
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(2f) // 1x2 banner style (width:height = 2:1)
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = hasCard && cardData?.cardUrl != null) {
                // Braze SDK: Log click analytics
                if (contentCard != null) {
                    try {
                        val logClickMethod = contentCard!!.javaClass.getMethod("logClick")
                        logClickMethod.invoke(contentCard)
                        Log.d("Tile1ContentCard", "Logged Content Card click")
                    } catch (e: Exception) {
                        Log.e("Tile1ContentCard", "Error logging click: ${e.message}", e)
                    }
                }
                
                // Handle click - check if it's a deep link
                val cardUrl = cardData?.cardUrl
                if (cardUrl != null) {
                    if (cardUrl.startsWith("philstore://")) {
                        try {
                            Log.d("Tile1ContentCard", "Handling deep link: $cardUrl")
                            val uri = Uri.parse(cardUrl)
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            intent.setPackage(context.packageName)
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Log.e("Tile1ContentCard", "Error handling deep link: ${e.message}", e)
                        }
                    } else {
                        Log.d("Tile1ContentCard", "Card URL: $cardUrl")
                    }
                }
            },
        shape = RoundedCornerShape(12.dp),
        color = if (hasCard) colorScheme.surface else colorScheme.surfaceVariant,
        border = if (!hasCard) {
            androidx.compose.foundation.BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.5f))
        } else null,
        shadowElevation = if (hasCard) 4.dp else 0.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (hasCard && cardData != null) {
                // Braze SDK: Display Content Card with image and/or text
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Image (if available) - fills most of the space
                    if (cardData.imageUrl != null) {
                        Image(
                            painter = rememberAsyncImagePainter(cardData.imageUrl),
                            contentDescription = cardData.title ?: "Content Card Image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentScale = ContentScale.Crop
                        )
                    }
                    
                    // Text content (if available)
                    if (cardData.title != null || cardData.description != null) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (cardData.title != null) {
                                Text(
                                    text = cardData.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = colorScheme.onSurface,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            
                            if (cardData.description != null) {
                                Text(
                                    text = cardData.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            } else {
                // Placeholder: Show when no content is available
                // Displays a bordered rectangle with placeholder text
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Content Card",
                        style = MaterialTheme.typography.titleMedium,
                        color = colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "location = $locationKey",
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
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
