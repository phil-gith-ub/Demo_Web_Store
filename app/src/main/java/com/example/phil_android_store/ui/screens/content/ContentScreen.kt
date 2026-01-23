package com.example.phil_android_store.ui.screens.content

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
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
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.background
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.ImageLoader
import androidx.compose.foundation.Image
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.ui.viewinterop.AndroidView
import com.braze.Braze
import com.example.phil_android_store.data.BrazeSettingsManager
import com.example.phil_android_store.data.BrazeUserSync
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Color

/**
 * Content screen for demonstrating Braze Content Cards and Banners.
 * This page can be used to manually install content cards and banners for client demonstrations.
 */
@Composable
fun ContentScreen() {
    val context = LocalContext.current
    val settingsManager = remember { BrazeSettingsManager(context) }
    var notificationMessage by remember { mutableStateOf<String?>(null) }
    
    // Braze SDK: Auto-refresh Content Cards and Banner when entering this screen
    LaunchedEffect(Unit) {
        val autoRefresh = settingsManager.getAutoRefreshContentCards()
        if (autoRefresh) {
            Log.d("ContentScreen", "Auto-refresh enabled: Requesting Content Cards refresh")
            BrazeUserSync.requestContentCardsRefresh(context)
        } else {
            Log.d("ContentScreen", "Auto-refresh disabled: Skipping Content Cards refresh")
        }
        
        // Always refresh banner when entering content screen
        Log.d("ContentScreen", "Requesting banner refresh for content_banner")
        BrazeUserSync.requestBannerRefresh(context, listOf("content_banner"))
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
            
            // Content Card: Tile 1 (2:1 banner layout)
            // Filters Braze Content Cards by key-value pair: location = tile_1
            // Adaptive layout: Square images display side-by-side, wide images display top-to-bottom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp)
            ) {
                // Tile1 card - fixed position
                Tile1ContentCard(
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Floating notification banner - overlays the top of Tile1
                notificationMessage?.let { message ->
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shadowElevation = 8.dp
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
            }
            
            // Content Cards: Tile 2 and Tile 3 (1:1 square layout, side-by-side)
            // Filters by location = tile_2 and tile_3 respectively
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Tile2ContentCard(
                    modifier = Modifier.weight(1f)
                )
                
                Tile3ContentCard(
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Banner: Content Banner (2:1 banner layout, non-collapsing)
            // Always displays container - shows placeholder when no banner is available
            ContentBanner(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            )
            
            // Additional space for future Content Cards or Banners
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                // Empty placeholder for future content
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
    
    // Filter key: Matches Content Cards with location = tile_1 in Braze dashboard
    val locationKey = "tile_1"
    
    // State: Holds the matched Content Card and whether content is available
    var contentCard by remember { mutableStateOf<Any?>(null) }
    var hasCard by remember { mutableStateOf(false) }
    
    // Subscribe to Braze Content Cards updates
    DisposableEffect(Unit) {
        // Request initial card refresh from Braze
        BrazeUserSync.requestContentCardsRefresh(context)
        
        // Subscribe to receive Content Cards when they update
        val subscription = BrazeUserSync.subscribeToContentCardsUpdates(context) { cards ->
            Log.d("Tile1ContentCard", "Received ${cards.size} Content Cards")
            
            // Filter cards by location key-value pair
            var foundCard: Any? = null
            for (card in cards) {
                // Skip control cards (used for A/B testing, not displayed)
                var isControl = false
                try {
                    val isControlMethod = card.javaClass.getMethod("isControlCard")
                    isControl = isControlMethod.invoke(card) as? Boolean ?: false
                } catch (e: NoSuchMethodException) {
                    try {
                        val isControlField = card.javaClass.getDeclaredField("isControl")
                        isControlField.isAccessible = true
                        isControl = isControlField.get(card) as? Boolean ?: false
                    } catch (e2: Exception) {
                        val className = card.javaClass.simpleName
                        isControl = className.contains("Control", ignoreCase = true)
                    }
                } catch (e: Exception) {
                    Log.d("Tile1ContentCard", "Could not check control status, proceeding: ${e.message}")
                }
                
                if (isControl) {
                    Log.d("Tile1ContentCard", "Skipping control card")
                    continue
                }
                
                // Extract key-value pairs (extras) from card
                try {
                    val getExtrasMethod = card.javaClass.getMethod("getExtras")
                    val extras = getExtrasMethod.invoke(card) as? Map<*, *>
                    
                    // Log card details for debugging
                    try {
                        val getIdMethod = card.javaClass.getMethod("getId")
                        val cardId = getIdMethod.invoke(card) as? String
                        val getTitleMethod = card.javaClass.getMethod("getTitle")
                        val title = getTitleMethod.invoke(card) as? String
                        
                        Log.d("Tile1ContentCard", "Card ID: $cardId, Title: $title")
                        Log.d("Tile1ContentCard", "Extras: ${extras?.keys?.joinToString(", ")}")
                    } catch (e: Exception) {
                        Log.d("Tile1ContentCard", "Could not get card details: ${e.message}")
                    }
                    
                    // Match card by location key-value pair (case-insensitive)
                    if (extras != null) {
                        var locationValue: Any? = null
                        var cardIdValue: Any? = null
                        
                        for ((key, value) in extras) {
                            val keyStr = key?.toString()?.lowercase()
                            when (keyStr) {
                                "location" -> locationValue = value
                                "card_id", "cardid" -> cardIdValue = value
                            }
                        }
                        
                        // Try direct access with different cases
                        if (locationValue == null) {
                            locationValue = extras["location"] ?: extras["Location"] ?: extras["LOCATION"]
                        }
                        if (cardIdValue == null) {
                            cardIdValue = extras["card_id"] ?: extras["cardId"] ?: extras["Card_Id"]
                        }
                        
                        // Match if location = tile_1 OR card_id matches (for web compatibility)
                        val matches = locationValue?.toString() == locationKey || 
                                     cardIdValue?.toString() == locationKey ||
                                     cardIdValue?.toString()?.contains("tile_1") == true
                        
                        if (matches) {
                            Log.d("Tile1ContentCard", "✓ Found matching card! location=$locationValue")
                            foundCard = card
                            break
                        }
                    }
                } catch (e: Exception) {
                    Log.e("Tile1ContentCard", "Error processing card (skipping): ${e.message}", e)
                    continue
                }
            }
            
            contentCard = foundCard
            hasCard = foundCard != null
        }
        
        // Cleanup: Unsubscribe when component is removed
        onDispose {
            BrazeUserSync.unsubscribeFromContentCardsUpdates(context, subscription)
        }
    }
    
    // Extract card properties (title, description, image, URL) using reflection
    // Handles different Braze card types (ShortNewsCard, CaptionedImage, etc.)
    val cardData = remember(contentCard) {
        if (contentCard != null) {
            try {
                val card = contentCard!!
                var title: String? = null
                var description: String? = null
                var imageUrl: String? = null
                var cardUrl: String? = null
                
                // Extract title
                try {
                    val getTitleMethod = card.javaClass.getMethod("getTitle")
                    title = getTitleMethod.invoke(card) as? String
                } catch (e: Exception) {}
                
                // Extract description (try multiple method names for different card types)
                try {
                    val getDescriptionMethod = card.javaClass.getMethod("getCardDescription")
                    description = getDescriptionMethod.invoke(card) as? String
                } catch (e: NoSuchMethodException) {
                    try {
                        val getDescriptionMethod = card.javaClass.getMethod("getDescription")
                        description = getDescriptionMethod.invoke(card) as? String
                    } catch (e2: Exception) {}
                }
                
                // Extract image URL (try multiple method names)
                try {
                    val getImageMethod = card.javaClass.getMethod("getImage")
                    imageUrl = getImageMethod.invoke(card) as? String
                } catch (e: NoSuchMethodException) {
                    try {
                        val getImageMethod = card.javaClass.getMethod("getImageUrl")
                        imageUrl = getImageMethod.invoke(card) as? String
                    } catch (e2: Exception) {}
                }
                
                // Extract card URL (try multiple method names)
                try {
                    val getUrlMethod = card.javaClass.getMethod("getUrlString")
                    cardUrl = getUrlMethod.invoke(card) as? String
                } catch (e: Exception) {
                    try {
                        val getUrlMethod = card.javaClass.getMethod("getUrl")
                        cardUrl = getUrlMethod.invoke(card) as? String
                    } catch (e2: Exception) {}
                }
                
                CardData(title, description, imageUrl, cardUrl)
            } catch (e: Exception) {
                Log.e("Tile1ContentCard", "Error extracting card data: ${e.message}", e)
                null
            }
        } else {
            null
        }
    }
    
    // Adaptive layout: Detect image aspect ratio to choose layout
    // Square images (≤ 1.3): Image left, text right | Wide images (> 1.3): Image top, text bottom
    var imageAspectRatio by remember(cardData?.imageUrl) { mutableStateOf<Float?>(null) }
    var useRowLayout by remember(imageAspectRatio) { 
        mutableStateOf(imageAspectRatio != null && imageAspectRatio!! <= 1.3f)
    }
    
    // Load image and calculate aspect ratio
    LaunchedEffect(cardData?.imageUrl) {
        val imageUrl = cardData?.imageUrl
        if (imageUrl != null) {
            try {
                val imageLoader = ImageLoader(context)
                val request = ImageRequest.Builder(context)
                    .data(imageUrl)
                    .build()
                val result = imageLoader.execute(request)
                val drawable = result.drawable
                if (drawable != null) {
                    val width = drawable.intrinsicWidth
                    val height = drawable.intrinsicHeight
                    if (width > 0 && height > 0) {
                        val aspectRatio = width.toFloat() / height.toFloat()
                        imageAspectRatio = aspectRatio
                        useRowLayout = aspectRatio <= 1.3f
                        Log.d("Tile1ContentCard", "Image: ${width}x${height}, ratio: $aspectRatio, layout: ${if (useRowLayout) "Row" else "Column"}")
                    }
                }
            } catch (e: Exception) {
                Log.d("Tile1ContentCard", "Could not load image dimensions, using default layout: ${e.message}")
                useRowLayout = false
            }
        } else {
            imageAspectRatio = null
            useRowLayout = false
        }
    }
    
    // Log impression to Braze analytics when card is displayed
    LaunchedEffect(contentCard) {
        if (contentCard != null) {
            try {
                val logImpressionMethod = contentCard!!.javaClass.getMethod("logImpression")
                logImpressionMethod.invoke(contentCard)
                Log.d("Tile1ContentCard", "Logged impression")
            } catch (e: Exception) {
                Log.e("Tile1ContentCard", "Error logging impression: ${e.message}", e)
            }
        }
    }
    
    // Card container: 2:1 aspect ratio banner layout
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(2f) // 2:1 banner (width:height)
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = hasCard && cardData?.cardUrl != null) {
                // Log click to Braze analytics
                if (contentCard != null) {
                    try {
                        val logClickMethod = contentCard!!.javaClass.getMethod("logClick")
                        logClickMethod.invoke(contentCard)
                        Log.d("Tile1ContentCard", "Logged click")
                    } catch (e: Exception) {
                        Log.e("Tile1ContentCard", "Error logging click: ${e.message}", e)
                    }
                }
                
                // Handle deep link navigation (philstore://) or external URL
                val cardUrl = cardData?.cardUrl as? String
                if (cardUrl != null) {
                    if (cardUrl.startsWith("philstore://")) {
                        try {
                            val uri = Uri.parse(cardUrl)
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            intent.setPackage(context.packageName)
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Log.e("Tile1ContentCard", "Error handling deep link: ${e.message}", e)
                        }
                    }
                }
            },
        shape = RoundedCornerShape(12.dp),
        color = Color.Transparent, // Transparent - grey placeholder background shows through
        border = if (!hasCard) {
            androidx.compose.foundation.BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.5f))
        } else null,
        shadowElevation = if (hasCard) 4.dp else 0.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Always show placeholder background (grey container)
            // This provides the background color even when content is displayed
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp)
            ) {
                // Placeholder text - only visible when no content card
                if (!hasCard || cardData == null) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
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
            
            // Content card displayed over the placeholder background
            if (hasCard && cardData != null) {
                // Adaptive layout based on image aspect ratio
                if (useRowLayout && cardData.imageUrl != null) {
                    // Row layout: Square images (image left, text right)
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(cardData.imageUrl),
                            contentDescription = cardData.title ?: "Content Card Image",
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        
                        // Text section - displays over grey placeholder background
                        if (cardData.title != null || cardData.description != null) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.Start
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
                                        maxLines = 3,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Column layout: Wide images (image top, text bottom)
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
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
                        
                        // Text section - displays over grey placeholder background
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
                }
            }
        }
    }
}

/**
 * Content Card: Tile 2 (1:1 Square Layout)
 * 
 * Custom Braze Content Card implementation:
 * - Filters Content Cards by key-value pair: location = tile_2
 * - Displays as 1:1 square (equal width and height)
 * - Automatically logs impressions and clicks to Braze analytics
 * - Displays placeholder when no content is available
 * 
 * To duplicate for another card:
 * 1. Copy this function and rename (e.g., Tile4ContentCard)
 * 2. Change locationKey to the new value (e.g., "tile_4")
 * 3. Add the component to ContentScreen layout
 */
@Composable
fun Tile2ContentCard(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme
    val scope = rememberCoroutineScope()
    
    // Filter key: Matches Content Cards with location = tile_2 in Braze dashboard
    val locationKey = "tile_2"
    
    // State: Holds the matched Content Card and whether content is available
    var contentCard by remember { mutableStateOf<Any?>(null) }
    var hasCard by remember { mutableStateOf(false) }
    
    // Subscribe to Braze Content Cards updates
    DisposableEffect(Unit) {
        // Request initial card refresh from Braze
        BrazeUserSync.requestContentCardsRefresh(context)
        
        // Subscribe to receive Content Cards when they update
        val subscription = BrazeUserSync.subscribeToContentCardsUpdates(context) { cards ->
            Log.d("Tile2ContentCard", "Received ${cards.size} Content Cards")
            
            // Filter cards by location key-value pair
            var foundCard: Any? = null
            for (card in cards) {
                // Skip control cards (used for A/B testing, not displayed)
                var isControl = false
                try {
                    val isControlMethod = card.javaClass.getMethod("isControlCard")
                    isControl = isControlMethod.invoke(card) as? Boolean ?: false
                } catch (e: NoSuchMethodException) {
                    try {
                        val isControlField = card.javaClass.getDeclaredField("isControl")
                        isControlField.isAccessible = true
                        isControl = isControlField.get(card) as? Boolean ?: false
                    } catch (e2: Exception) {
                        val className = card.javaClass.simpleName
                        isControl = className.contains("Control", ignoreCase = true)
                    }
                } catch (e: Exception) {
                    Log.d("Tile2ContentCard", "Could not check control status, proceeding: ${e.message}")
                }
                
                if (isControl) continue
                
                // Extract key-value pairs (extras) from card
                try {
                    val getExtrasMethod = card.javaClass.getMethod("getExtras")
                    val extras = getExtrasMethod.invoke(card) as? Map<*, *>
                    
                    // Match card by location key-value pair (case-insensitive)
                    if (extras != null) {
                        var locationValue: Any? = null
                        for ((key, value) in extras) {
                            val keyStr = key?.toString()?.lowercase()
                            if (keyStr == "location") {
                                locationValue = value
                                break
                            }
                        }
                        
                        if (locationValue == null) {
                            locationValue = extras["location"] ?: extras["Location"] ?: extras["LOCATION"]
                        }
                        
                        if (locationValue?.toString() == locationKey) {
                            Log.d("Tile2ContentCard", "✓ Found matching card! location=$locationValue")
                            foundCard = card
                            break
                        }
                    }
                } catch (e: Exception) {
                    Log.e("Tile2ContentCard", "Error processing card (skipping): ${e.message}")
                    continue
                }
            }
            
            contentCard = foundCard
            hasCard = foundCard != null
        }
        
        // Cleanup: Unsubscribe when component is removed
        onDispose {
            BrazeUserSync.unsubscribeFromContentCardsUpdates(context, subscription)
        }
    }
    
    // Extract card properties (title, description, image, URL) using reflection
    val cardData = remember(contentCard) {
        if (contentCard != null) {
            try {
                val card = contentCard!!
                var title: String? = null
                var description: String? = null
                var imageUrl: String? = null
                var cardUrl: String? = null
                
                try {
                    val getTitleMethod = card.javaClass.getMethod("getTitle")
                    title = getTitleMethod.invoke(card) as? String
                } catch (e: Exception) {}
                
                try {
                    val getDescriptionMethod = card.javaClass.getMethod("getCardDescription")
                    description = getDescriptionMethod.invoke(card) as? String
                } catch (e: NoSuchMethodException) {
                    try {
                        val getDescriptionMethod = card.javaClass.getMethod("getDescription")
                        description = getDescriptionMethod.invoke(card) as? String
                    } catch (e2: Exception) {}
                }
                
                try {
                    val getImageMethod = card.javaClass.getMethod("getImage")
                    imageUrl = getImageMethod.invoke(card) as? String
                } catch (e: NoSuchMethodException) {
                    try {
                        val getImageMethod = card.javaClass.getMethod("getImageUrl")
                        imageUrl = getImageMethod.invoke(card) as? String
                    } catch (e2: Exception) {}
                }
                
                try {
                    val getUrlMethod = card.javaClass.getMethod("getUrlString")
                    cardUrl = getUrlMethod.invoke(card) as? String
                } catch (e: Exception) {
                    try {
                        val getUrlMethod = card.javaClass.getMethod("getUrl")
                        cardUrl = getUrlMethod.invoke(card) as? String
                    } catch (e2: Exception) {}
                }
                
                CardData(title, description, imageUrl, cardUrl)
            } catch (e: Exception) {
                Log.e("Tile2ContentCard", "Error extracting card data: ${e.message}", e)
                null
            }
        } else {
            null
        }
    }
    
    // Log impression to Braze analytics when card is displayed
    LaunchedEffect(contentCard) {
        if (contentCard != null) {
            try {
                val logImpressionMethod = contentCard!!.javaClass.getMethod("logImpression")
                logImpressionMethod.invoke(contentCard)
                Log.d("Tile2ContentCard", "Logged impression")
            } catch (e: Exception) {
                Log.e("Tile2ContentCard", "Error logging impression: ${e.message}", e)
            }
        }
    }
    
    // Card container: 1:1 aspect ratio square layout
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f) // 1:1 square (width:height)
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = hasCard && cardData?.cardUrl != null) {
                // Log click to Braze analytics
                if (contentCard != null) {
                    try {
                        val logClickMethod = contentCard!!.javaClass.getMethod("logClick")
                        logClickMethod.invoke(contentCard)
                        Log.d("Tile2ContentCard", "Logged click")
                    } catch (e: Exception) {
                        Log.e("Tile2ContentCard", "Error logging click: ${e.message}", e)
                    }
                }
                
                // Handle deep link navigation (philstore://) or external URL
                val cardUrl = cardData?.cardUrl as? String
                if (cardUrl != null) {
                    if (cardUrl.startsWith("philstore://")) {
                        try {
                            val uri = Uri.parse(cardUrl)
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            intent.setPackage(context.packageName)
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Log.e("Tile2ContentCard", "Error handling deep link: ${e.message}", e)
                        }
                    }
                }
            },
        shape = RoundedCornerShape(12.dp),
        color = Color.Transparent, // Transparent - grey placeholder background shows through
        border = if (!hasCard) {
            androidx.compose.foundation.BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.5f))
        } else null,
        shadowElevation = if (hasCard) 4.dp else 0.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Always show placeholder background (grey container)
            // This provides the background color even when content is displayed
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp)
            ) {
                // Placeholder text - only visible when no content card
                if (!hasCard || cardData == null) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Content Card",
                            style = MaterialTheme.typography.titleSmall,
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
            
            // Content card displayed over the placeholder background
            if (hasCard && cardData != null) {
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
                    
                    // Text section - displays over grey placeholder background
                    if (cardData.title != null || cardData.description != null) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (cardData.title != null) {
                                Text(
                                    text = cardData.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = colorScheme.onSurface,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            
                            if (cardData.description != null) {
                                Text(
                                    text = cardData.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Content Card: Tile 3 (1:1 Square Layout)
 * 
 * Custom Braze Content Card implementation:
 * - Filters Content Cards by key-value pair: location = tile_3
 * - Displays as 1:1 square (equal width and height)
 * - Automatically logs impressions and clicks to Braze analytics
 * - Displays placeholder when no content is available
 * 
 * To duplicate for another card:
 * 1. Copy this function and rename (e.g., Tile4ContentCard)
 * 2. Change locationKey to the new value (e.g., "tile_4")
 * 3. Add the component to ContentScreen layout
 */
@Composable
fun Tile3ContentCard(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme
    val scope = rememberCoroutineScope()
    
    // Filter key: Matches Content Cards with location = tile_3 in Braze dashboard
    val locationKey = "tile_3"
    
    // State: Holds the matched Content Card and whether content is available
    var contentCard by remember { mutableStateOf<Any?>(null) }
    var hasCard by remember { mutableStateOf(false) }
    
    // Subscribe to Braze Content Cards updates
    DisposableEffect(Unit) {
        // Request initial card refresh from Braze
        BrazeUserSync.requestContentCardsRefresh(context)
        
        // Subscribe to receive Content Cards when they update
        val subscription = BrazeUserSync.subscribeToContentCardsUpdates(context) { cards ->
            Log.d("Tile3ContentCard", "Received ${cards.size} Content Cards")
            
            // Filter cards by location key-value pair
            var foundCard: Any? = null
            for (card in cards) {
                // Skip control cards (used for A/B testing, not displayed)
                var isControl = false
                try {
                    val isControlMethod = card.javaClass.getMethod("isControlCard")
                    isControl = isControlMethod.invoke(card) as? Boolean ?: false
                } catch (e: NoSuchMethodException) {
                    try {
                        val isControlField = card.javaClass.getDeclaredField("isControl")
                        isControlField.isAccessible = true
                        isControl = isControlField.get(card) as? Boolean ?: false
                    } catch (e2: Exception) {
                        val className = card.javaClass.simpleName
                        isControl = className.contains("Control", ignoreCase = true)
                    }
                } catch (e: Exception) {
                    Log.d("Tile3ContentCard", "Could not check control status, proceeding: ${e.message}")
                }
                
                if (isControl) continue
                
                // Extract key-value pairs (extras) from card
                try {
                    val getExtrasMethod = card.javaClass.getMethod("getExtras")
                    val extras = getExtrasMethod.invoke(card) as? Map<*, *>
                    
                    // Match card by location key-value pair (case-insensitive)
                    if (extras != null) {
                        var locationValue: Any? = null
                        for ((key, value) in extras) {
                            val keyStr = key?.toString()?.lowercase()
                            if (keyStr == "location") {
                                locationValue = value
                                break
                            }
                        }
                        
                        if (locationValue == null) {
                            locationValue = extras["location"] ?: extras["Location"] ?: extras["LOCATION"]
                        }
                        
                        if (locationValue?.toString() == locationKey) {
                            Log.d("Tile3ContentCard", "✓ Found matching card! location=$locationValue")
                            foundCard = card
                            break
                        }
                    }
                } catch (e: Exception) {
                    Log.e("Tile3ContentCard", "Error processing card (skipping): ${e.message}")
                    continue
                }
            }
            
            contentCard = foundCard
            hasCard = foundCard != null
        }
        
        // Cleanup: Unsubscribe when component is removed
        onDispose {
            BrazeUserSync.unsubscribeFromContentCardsUpdates(context, subscription)
        }
    }
    
    // Extract card properties (title, description, image, URL) using reflection
    val cardData = remember(contentCard) {
        if (contentCard != null) {
            try {
                val card = contentCard!!
                var title: String? = null
                var description: String? = null
                var imageUrl: String? = null
                var cardUrl: String? = null
                
                try {
                    val getTitleMethod = card.javaClass.getMethod("getTitle")
                    title = getTitleMethod.invoke(card) as? String
                } catch (e: Exception) {}
                
                try {
                    val getDescriptionMethod = card.javaClass.getMethod("getCardDescription")
                    description = getDescriptionMethod.invoke(card) as? String
                } catch (e: NoSuchMethodException) {
                    try {
                        val getDescriptionMethod = card.javaClass.getMethod("getDescription")
                        description = getDescriptionMethod.invoke(card) as? String
                    } catch (e2: Exception) {}
                }
                
                try {
                    val getImageMethod = card.javaClass.getMethod("getImage")
                    imageUrl = getImageMethod.invoke(card) as? String
                } catch (e: NoSuchMethodException) {
                    try {
                        val getImageMethod = card.javaClass.getMethod("getImageUrl")
                        imageUrl = getImageMethod.invoke(card) as? String
                    } catch (e2: Exception) {}
                }
                
                try {
                    val getUrlMethod = card.javaClass.getMethod("getUrlString")
                    cardUrl = getUrlMethod.invoke(card) as? String
                } catch (e: Exception) {
                    try {
                        val getUrlMethod = card.javaClass.getMethod("getUrl")
                        cardUrl = getUrlMethod.invoke(card) as? String
                    } catch (e2: Exception) {}
                }
                
                CardData(title, description, imageUrl, cardUrl)
            } catch (e: Exception) {
                Log.e("Tile3ContentCard", "Error extracting card data: ${e.message}", e)
                null
            }
        } else {
            null
        }
    }
    
    // Log impression to Braze analytics when card is displayed
    LaunchedEffect(contentCard) {
        if (contentCard != null) {
            try {
                val logImpressionMethod = contentCard!!.javaClass.getMethod("logImpression")
                logImpressionMethod.invoke(contentCard)
                Log.d("Tile3ContentCard", "Logged impression")
            } catch (e: Exception) {
                Log.e("Tile3ContentCard", "Error logging impression: ${e.message}", e)
            }
        }
    }
    
    // Card container: 1:1 aspect ratio square layout
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f) // 1:1 square (width:height)
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = hasCard && cardData?.cardUrl != null) {
                // Log click to Braze analytics
                if (contentCard != null) {
                    try {
                        val logClickMethod = contentCard!!.javaClass.getMethod("logClick")
                        logClickMethod.invoke(contentCard)
                        Log.d("Tile3ContentCard", "Logged click")
                    } catch (e: Exception) {
                        Log.e("Tile3ContentCard", "Error logging click: ${e.message}", e)
                    }
                }
                
                // Handle deep link navigation (philstore://) or external URL
                val cardUrl = cardData?.cardUrl as? String
                if (cardUrl != null) {
                    if (cardUrl.startsWith("philstore://")) {
                        try {
                            val uri = Uri.parse(cardUrl)
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            intent.setPackage(context.packageName)
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Log.e("Tile3ContentCard", "Error handling deep link: ${e.message}", e)
                        }
                    }
                }
            },
        shape = RoundedCornerShape(12.dp),
        color = Color.Transparent, // Transparent - grey placeholder background shows through
        border = if (!hasCard) {
            androidx.compose.foundation.BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.5f))
        } else null,
        shadowElevation = if (hasCard) 4.dp else 0.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Always show placeholder background (grey container)
            // This provides the background color even when content is displayed
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp)
            ) {
                // Placeholder text - only visible when no content card
                if (!hasCard || cardData == null) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Content Card",
                            style = MaterialTheme.typography.titleSmall,
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
            
            // Content card displayed over the placeholder background
            if (hasCard && cardData != null) {
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
                    
                    // Text section - displays over grey placeholder background
                    if (cardData.title != null || cardData.description != null) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (cardData.title != null) {
                                Text(
                                    text = cardData.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = colorScheme.onSurface,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            
                            if (cardData.description != null) {
                                Text(
                                    text = cardData.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Banner: Content Banner (2:1 Banner Layout, Non-Collapsing)
 * 
 * Custom Braze Banner implementation that always displays a container:
 * - Uses placement ID: content_banner
 * - Displays banner when available (same as store page banner)
 * - Shows placeholder when no banner is available (does not collapse)
 * - Placeholder displays "Banner" and "Placement ID: content_banner"
 */
@Composable
fun ContentBanner(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme
    val placementId = "content_banner"
    var banner by remember(placementId) { mutableStateOf<Any?>(null) }
    var shouldRender by remember(placementId) { mutableStateOf(false) }
    
    // Request banner refresh and get banner (same pattern as BrazeBanner)
    LaunchedEffect(placementId) {
        android.util.Log.d("ContentBanner", "=== Initializing ContentBanner for placement: $placementId ===")
        // Braze SDK: Request refresh for this placement
        BrazeUserSync.requestBannerRefresh(context, listOf(placementId))
        android.util.Log.d("ContentBanner", "Requested banner refresh")
        
        // Small delay to allow banner to be fetched
        kotlinx.coroutines.delay(500)
        
        // Braze SDK: Get the banner
        banner = BrazeUserSync.getBanner(context, placementId)
        android.util.Log.d("ContentBanner", "Retrieved banner: ${if (banner != null) "exists (${banner!!.javaClass.simpleName})" else "null"}")
        
        // Check if banner exists and is not a control variant
        if (banner != null) {
            try {
                val isControlMethod = banner!!.javaClass.getMethod("isControl")
                val isControl = isControlMethod.invoke(banner) as? Boolean ?: false
                shouldRender = !isControl
                android.util.Log.d("ContentBanner", "Banner isControl: $isControl, shouldRender: $shouldRender")
            } catch (e: Exception) {
                // If isControl method doesn't exist, assume we should render
                shouldRender = true
                android.util.Log.d("ContentBanner", "Could not check isControl, defaulting shouldRender to true: ${e.message}")
            }
        } else {
            shouldRender = false
            android.util.Log.d("ContentBanner", "No banner found, shouldRender: false")
        }
        android.util.Log.d("ContentBanner", "=== Banner initialization complete - shouldRender: $shouldRender, banner: ${if (banner != null) "exists" else "null"} ===")
    }
    
    // Always render container (2:1 aspect ratio banner layout)
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(2f) // 2:1 banner (width:height)
            .clip(RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        color = if (shouldRender && banner != null) colorScheme.surface else colorScheme.surfaceVariant,
        border = if (!shouldRender || banner == null) {
            androidx.compose.foundation.BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.5f))
        } else null,
        shadowElevation = if (shouldRender && banner != null) 4.dp else 0.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (shouldRender && banner != null) {
                android.util.Log.d("ContentBanner", "Rendering banner in WebView")
                // Display banner (same implementation as BrazeBanner)
                AndroidView(
                    factory = { ctx ->
                        android.webkit.WebView(ctx).apply {
                            setBackgroundColor(android.graphics.Color.TRANSPARENT)
                            
                            webViewClient = object : android.webkit.WebViewClient() {
                                override fun shouldOverrideUrlLoading(view: android.webkit.WebView?, url: String?): Boolean {
                                    android.util.Log.d("ContentBanner", "shouldOverrideUrlLoading called with URL: $url")
                                    if (url != null && url.startsWith("philstore://")) {
                                        android.util.Log.d("ContentBanner", "Deep link detected: $url")
                                        try {
                                            val uri = android.net.Uri.parse(url)
                                            android.util.Log.d("ContentBanner", "Parsed URI - scheme: ${uri.scheme}, host: ${uri.host}, path: ${uri.path}")
                                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, uri)
                                            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP or android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                            intent.setPackage(ctx.packageName)
                                            android.util.Log.d("ContentBanner", "Intent created - package: ${ctx.packageName}, action: ${intent.action}")
                                            val resolvedActivity = intent.resolveActivity(ctx.packageManager)
                                            android.util.Log.d("ContentBanner", "Resolved activity: $resolvedActivity")
                                            if (resolvedActivity != null) {
                                                ctx.startActivity(intent)
                                                android.util.Log.d("ContentBanner", "✓ Started activity with deep link: $url")
                                            } else {
                                                android.util.Log.w("ContentBanner", "✗ Could not resolve activity for deep link: $url")
                                            }
                                            return true
                                        } catch (e: Exception) {
                                            android.util.Log.e("ContentBanner", "Error handling deep link: ${e.message}", e)
                                            e.printStackTrace()
                                        }
                                    } else {
                                        android.util.Log.d("ContentBanner", "URL is not a deep link, allowing WebView to handle: $url")
                                    }
                                    return false
                                }
                            }
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.loadWithOverviewMode = true
                            settings.useWideViewPort = true
                            settings.layoutAlgorithm = android.webkit.WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING
                        }
                    },
                    update = { webView ->
                        // Helper function to handle deep links
                        fun handleDeepLink(url: String) {
                            android.util.Log.d("ContentBanner", "handleDeepLink called with URL: $url")
                            try {
                                val uri = android.net.Uri.parse(url)
                                android.util.Log.d("ContentBanner", "Parsed URI - scheme: ${uri.scheme}, host: ${uri.host}, path: ${uri.path}")
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, uri)
                                intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                intent.setPackage(context.packageName)
                                android.util.Log.d("ContentBanner", "Intent created - package: ${context.packageName}, action: ${intent.action}")
                                val resolvedActivity = intent.resolveActivity(context.packageManager)
                                android.util.Log.d("ContentBanner", "Resolved activity: $resolvedActivity")
                                if (resolvedActivity != null) {
                                    context.startActivity(intent)
                                    android.util.Log.d("ContentBanner", "✓ Started activity with deep link: $url")
                                } else {
                                    android.util.Log.w("ContentBanner", "✗ Could not resolve activity for deep link: $url")
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("ContentBanner", "Error handling deep link: ${e.message}", e)
                                e.printStackTrace()
                            }
                        }
                        
                        // JavaScript interface for deep links
                        class DeepLinkHandler(private val handler: (String) -> Unit) {
                            @android.webkit.JavascriptInterface
                            fun handleDeepLink(url: String) {
                                android.util.Log.d("ContentBanner", "JavaScript interface handleDeepLink called with URL: $url")
                                android.os.Handler(android.os.Looper.getMainLooper()).post {
                                    android.util.Log.d("ContentBanner", "Executing deep link handler on main thread")
                                    handler(url)
                                }
                            }
                        }
                        
                        webView.addJavascriptInterface(DeepLinkHandler(::handleDeepLink), "AndroidDeepLinkHandler")
                        android.util.Log.d("ContentBanner", "JavaScript interface 'AndroidDeepLinkHandler' added to WebView")
                        
                        val customWebViewClient = object : android.webkit.WebViewClient() {
                            override fun shouldOverrideUrlLoading(view: android.webkit.WebView?, url: String?): Boolean {
                                android.util.Log.d("ContentBanner", "[update] shouldOverrideUrlLoading called with URL: $url")
                                if (url != null && url.startsWith("philstore://")) {
                                    android.util.Log.d("ContentBanner", "[update] Deep link detected, calling handleDeepLink")
                                    handleDeepLink(url)
                                    return true
                                }
                                android.util.Log.d("ContentBanner", "[update] URL is not a deep link, returning false")
                                return false
                            }
                            
                            @android.annotation.SuppressLint("NewApi")
                            override fun shouldOverrideUrlLoading(view: android.webkit.WebView?, request: android.webkit.WebResourceRequest?): Boolean {
                                val url = request?.url?.toString()
                                android.util.Log.d("ContentBanner", "[update] shouldOverrideUrlLoading (new API) called with URL: $url")
                                if (url != null && url.startsWith("philstore://")) {
                                    android.util.Log.d("ContentBanner", "[update] Deep link detected (new API), calling handleDeepLink")
                                    handleDeepLink(url)
                                    return true
                                }
                                android.util.Log.d("ContentBanner", "[update] URL is not a deep link (new API), returning false")
                                return false
                            }
                        }
                        
                        webView.webViewClient = customWebViewClient
                        webView.setBackgroundColor(android.graphics.Color.TRANSPARENT)
                        android.util.Log.d("ContentBanner", "Custom WebViewClient set on WebView")
                        
                        // Braze SDK: Get the current banner (same as BrazeBanner)
                        val currentBanner = BrazeUserSync.getBanner(context, placementId)
                        android.util.Log.d("ContentBanner", "Current banner in update block: ${if (currentBanner != null) "exists (${currentBanner.javaClass.simpleName})" else "null"}")
                        if (currentBanner != null) {
                            try {
                                android.util.Log.d("ContentBanner", "Attempting to insert banner into WebView")
                                val brazeInstance = com.braze.Braze.getInstance(context)
                                val insertMethod = brazeInstance.javaClass.getMethod(
                                    "insertBanner",
                                    currentBanner.javaClass,
                                    android.view.View::class.java
                                )
                                insertMethod.invoke(brazeInstance, currentBanner, webView)
                                android.util.Log.d("ContentBanner", "✓ Banner inserted into WebView")
                                
                                webView.post {
                                    android.util.Log.d("ContentBanner", "Re-applying WebViewClient after Braze insertBanner")
                                    webView.setBackgroundColor(android.graphics.Color.TRANSPARENT)
                                    webView.webViewClient = customWebViewClient
                                    
                                    webView.postDelayed({
                                        try {
                                            android.util.Log.d("ContentBanner", "Injecting JavaScript deep link interceptor")
                                            val jsCode = """
                                                (function() {
                                                    console.log('ContentBanner: Deep link interceptor script loaded');
                                                    
                                                    function handleDeepLink(url) {
                                                        console.log('ContentBanner: handleDeepLink called with:', url);
                                                        if (url && url.startsWith('philstore://')) {
                                                            console.log('ContentBanner: Deep link detected:', url);
                                                            if (window.AndroidDeepLinkHandler) {
                                                                console.log('ContentBanner: Using AndroidDeepLinkHandler');
                                                                window.AndroidDeepLinkHandler.handleDeepLink(url);
                                                                return true;
                                                            } else {
                                                                console.warn('ContentBanner: AndroidDeepLinkHandler not available, using location.href');
                                                            }
                                                            window.location.href = url;
                                                            return true;
                                                        }
                                                        return false;
                                                    }
                                                    
                                                    document.addEventListener('click', function(e) {
                                                        var target = e.target;
                                                        while (target && target.tagName !== 'A') {
                                                            target = target.parentElement;
                                                        }
                                                        if (target && target.href) {
                                                            console.log('ContentBanner: Click detected on link:', target.href);
                                                            if (handleDeepLink(target.href)) {
                                                                e.preventDefault();
                                                                e.stopPropagation();
                                                                return false;
                                                            }
                                                        }
                                                    }, true);
                                                    
                                                    var links = document.querySelectorAll('a[href^="philstore://"]');
                                                    console.log('ContentBanner: Found', links.length, 'deep link(s) in document');
                                                    links.forEach(function(link) {
                                                        link.addEventListener('click', function(e) {
                                                            console.log('ContentBanner: Direct link click:', this.href);
                                                            if (handleDeepLink(this.href)) {
                                                                e.preventDefault();
                                                                e.stopPropagation();
                                                                return false;
                                                            }
                                                        }, true);
                                                    });
                                                    
                                                    var observer = new MutationObserver(function(mutations) {
                                                        mutations.forEach(function(mutation) {
                                                            mutation.addedNodes.forEach(function(node) {
                                                                if (node.nodeType === 1) {
                                                                    var newLinks = node.querySelectorAll ? node.querySelectorAll('a[href^="philstore://"]') : [];
                                                                    if (newLinks.length > 0) {
                                                                        console.log('ContentBanner: Found', newLinks.length, 'new deep link(s)');
                                                                    }
                                                                    newLinks.forEach(function(link) {
                                                                        link.addEventListener('click', function(e) {
                                                                            console.log('ContentBanner: Dynamic link click:', this.href);
                                                                            if (handleDeepLink(this.href)) {
                                                                                e.preventDefault();
                                                                                e.stopPropagation();
                                                                                return false;
                                                                            }
                                                                        }, true);
                                                                    });
                                                                }
                                                            });
                                                        });
                                                    });
                                                    observer.observe(document.body, { childList: true, subtree: true });
                                                    console.log('ContentBanner: MutationObserver set up for dynamic links');
                                                })();
                                            """.trimIndent()
                                            webView.evaluateJavascript(jsCode) { result ->
                                                android.util.Log.d("ContentBanner", "JavaScript injection result: $result")
                                            }
                                            android.util.Log.d("ContentBanner", "✓ JavaScript deep link interceptor injected")
                                        } catch (e: Exception) {
                                            android.util.Log.e("ContentBanner", "Error injecting JavaScript: ${e.message}", e)
                                            e.printStackTrace()
                                        }
                                    }, 500)
                                }
                            } catch (e: Exception) {
                                android.util.Log.w("ContentBanner", "insertBanner method failed, trying getHtml: ${e.message}")
                                try {
                                    val htmlMethod = currentBanner.javaClass.getMethod("getHtml")
                                    val htmlContent = htmlMethod.invoke(currentBanner) as? String
                                    android.util.Log.d("ContentBanner", "Retrieved HTML content: ${if (htmlContent != null) "${htmlContent.length} chars" else "null"}")
                                    if (htmlContent != null) {
                                        // Log if HTML contains deep links
                                        if (htmlContent.contains("philstore://")) {
                                            android.util.Log.d("ContentBanner", "✓ HTML contains philstore:// deep links")
                                        } else {
                                            android.util.Log.d("ContentBanner", "✗ HTML does not contain philstore:// deep links")
                                        }
                                        
                                        webView.webViewClient = customWebViewClient
                                        val wrappedHtml = """
                                            <!DOCTYPE html>
                                            <html>
                                            <head>
                                                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                                                <style>
                                                    body {
                                                        margin: 0;
                                                        padding: 0;
                                                        display: flex;
                                                        justify-content: center;
                                                        align-items: center;
                                                        min-height: 100%;
                                                        background-color: transparent;
                                                    }
                                                    html {
                                                        background-color: transparent;
                                                    }
                                                    * {
                                                        max-width: 100%;
                                                    }
                                                </style>
                                            </head>
                                            <body>
                                                $htmlContent
                                            </body>
                                            </html>
                                        """.trimIndent()
                                        webView.loadDataWithBaseURL(null, wrappedHtml, "text/html", "UTF-8", null)
                                        android.util.Log.d("ContentBanner", "✓ Loaded HTML content with custom WebViewClient")
                                        
                                        // Inject JavaScript after HTML loads (same as insertBanner path)
                                        webView.postDelayed({
                                            try {
                                                android.util.Log.d("ContentBanner", "Injecting JavaScript deep link interceptor (getHtml path)")
                                                val jsCode = """
                                                    (function() {
                                                        console.log('ContentBanner: Deep link interceptor script loaded (getHtml path)');
                                                        
                                                        function handleDeepLink(url) {
                                                            console.log('ContentBanner: handleDeepLink called with:', url);
                                                            if (url && url.startsWith('philstore://')) {
                                                                console.log('ContentBanner: Deep link detected:', url);
                                                                if (window.AndroidDeepLinkHandler) {
                                                                    console.log('ContentBanner: Using AndroidDeepLinkHandler');
                                                                    window.AndroidDeepLinkHandler.handleDeepLink(url);
                                                                    return true;
                                                                } else {
                                                                    console.warn('ContentBanner: AndroidDeepLinkHandler not available, using location.href');
                                                                }
                                                                window.location.href = url;
                                                                return true;
                                                            }
                                                            return false;
                                                        }
                                                        
                                                        document.addEventListener('click', function(e) {
                                                            var target = e.target;
                                                            while (target && target.tagName !== 'A') {
                                                                target = target.parentElement;
                                                            }
                                                            if (target && target.href) {
                                                                console.log('ContentBanner: Click detected on link:', target.href);
                                                                if (handleDeepLink(target.href)) {
                                                                    e.preventDefault();
                                                                    e.stopPropagation();
                                                                    return false;
                                                                }
                                                            }
                                                        }, true);
                                                        
                                                        var links = document.querySelectorAll('a[href^="philstore://"]');
                                                        console.log('ContentBanner: Found', links.length, 'deep link(s) in document');
                                                        links.forEach(function(link) {
                                                            link.addEventListener('click', function(e) {
                                                                console.log('ContentBanner: Direct link click:', this.href);
                                                                if (handleDeepLink(this.href)) {
                                                                    e.preventDefault();
                                                                    e.stopPropagation();
                                                                    return false;
                                                                }
                                                            }, true);
                                                        });
                                                        
                                                        var observer = new MutationObserver(function(mutations) {
                                                            mutations.forEach(function(mutation) {
                                                                mutation.addedNodes.forEach(function(node) {
                                                                    if (node.nodeType === 1) {
                                                                        var newLinks = node.querySelectorAll ? node.querySelectorAll('a[href^="philstore://"]') : [];
                                                                        if (newLinks.length > 0) {
                                                                            console.log('ContentBanner: Found', newLinks.length, 'new deep link(s)');
                                                                        }
                                                                        newLinks.forEach(function(link) {
                                                                            link.addEventListener('click', function(e) {
                                                                                console.log('ContentBanner: Dynamic link click:', this.href);
                                                                                if (handleDeepLink(this.href)) {
                                                                                    e.preventDefault();
                                                                                    e.stopPropagation();
                                                                                    return false;
                                                                                }
                                                                            }, true);
                                                                        });
                                                                    }
                                                                });
                                                            });
                                                        });
                                                        observer.observe(document.body, { childList: true, subtree: true });
                                                        console.log('ContentBanner: MutationObserver set up for dynamic links');
                                                    })();
                                                """.trimIndent()
                                                webView.evaluateJavascript(jsCode) { result ->
                                                    android.util.Log.d("ContentBanner", "JavaScript injection result (getHtml path): $result")
                                                }
                                                android.util.Log.d("ContentBanner", "✓ JavaScript deep link interceptor injected (getHtml path)")
                                            } catch (e: Exception) {
                                                android.util.Log.e("ContentBanner", "Error injecting JavaScript (getHtml path): ${e.message}", e)
                                                e.printStackTrace()
                                            }
                                        }, 500)
                                    } else {
                                        android.util.Log.w("ContentBanner", "✗ HTML content is null")
                                    }
                                } catch (e2: Exception) {
                                    android.util.Log.e("ContentBanner", "✗ Error loading banner HTML: ${e2.message}", e2)
                                    e2.printStackTrace()
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Placeholder: Displayed when no banner is available
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Banner",
                        style = MaterialTheme.typography.titleMedium,
                        color = colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Placement ID: $placementId",
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
 * Data class: Holds extracted Content Card properties
 * Used by all Content Card components to store card data
 */
data class CardData(
    val title: String?,
    val description: String?,
    val imageUrl: String?,
    val cardUrl: String?
)
