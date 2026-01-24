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
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.ui.viewinterop.AndroidView
import com.braze.Braze
import com.example.phil_android_store.data.BrazeContentManager
import com.example.phil_android_store.data.BrazeLogManager
import com.example.phil_android_store.data.BrazeSettingsManager
import com.example.phil_android_store.data.BrazeUserSync
import com.example.phil_android_store.data.rememberCachedBanner
import com.example.phil_android_store.data.rememberCachedContentCards
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
    
    // Braze SDK: Log screen entry (content is pre-loaded, no refresh needed)
    LaunchedEffect(Unit) {
        BrazeLogManager.logScreenEntered("Content Page")
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
                        BrazeLogManager.logCustomEvent("enable_push")
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
                        BrazeLogManager.logCustomEvent("push_notification")
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
                        BrazeLogManager.logCustomEvent("user_action_button")
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
                
                TileBanner(
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
    
    // Read content cards from cache (pre-loaded on session start, updated after events)
    val cachedCards = rememberCachedContentCards()
    
    // Process cached cards on initial load and when cache updates
    LaunchedEffect(cachedCards) {
        // Filter cards by location key-value pair from cache
        var foundCard: Any? = null
        for (card in cachedCards) {
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
            }
            
            if (isControl) {
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
                } catch (e: Exception) {
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
                        foundCard = card
                        try {
                            val getIdMethod = card.javaClass.getMethod("getId")
                            val cardId = getIdMethod.invoke(card) as? String
                            BrazeLogManager.logContentCardMatched(locationKey, cardId)
                        } catch (e: Exception) {
                            BrazeLogManager.logContentCardMatched(locationKey, null)
                        }
                        break
                    }
                }
            } catch (e: Exception) {
                continue
            }
        }
        
        contentCard = foundCard
        hasCard = foundCard != null
    }
    
    // Subscribe to Braze Content Cards updates (for real-time updates when cache refreshes)
    DisposableEffect(Unit) {
        // Subscribe to receive Content Cards when they update
        val subscription = BrazeUserSync.subscribeToContentCardsUpdates(context) { cards ->
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
                }
                
                if (isControl) {
                    continue
                }
                
                // Extract key-value pairs (extras) from card
                try {
                    val getExtrasMethod = card.javaClass.getMethod("getExtras")
                    val extras = getExtrasMethod.invoke(card) as? Map<*, *>
                    
                    // Match card by location key-value pair (case-insensitive)
                    if (extras != null) {
                        var locationValue: Any? = null
                        var cardIdValue: Any? = null
                        
                        // Check for location in extras (case-insensitive key matching)
                        for ((key, value) in extras) {
                            val keyStr = key?.toString()?.lowercase()
                            if (keyStr == "location") {
                                locationValue = value
                            }
                            if (keyStr == "card_id" || keyStr == "cardid") {
                                cardIdValue = value
                            }
                        }
                        
                        // Match if location = tile_1 OR card_id matches (for web compatibility)
                        val matches = locationValue?.toString() == locationKey || 
                                     cardIdValue?.toString() == locationKey ||
                                     cardIdValue?.toString()?.contains("tile_1") == true
                        
                        if (matches) {
                            foundCard = card
                            try {
                                val getIdMethod = card.javaClass.getMethod("getId")
                                val cardId = getIdMethod.invoke(card) as? String
                                BrazeLogManager.logContentCardMatched(locationKey, cardId)
                            } catch (e: Exception) {
                                BrazeLogManager.logContentCardMatched(locationKey, null)
                            }
                            break
                        }
                    }
                } catch (e: Exception) {
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
                    }
                }
            } catch (e: Exception) {
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
                BrazeLogManager.logImpression("Content Card", "tile_1")
            } catch (e: Exception) {
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
                        BrazeLogManager.logClick("Content Card", "tile_1")
                    } catch (e: Exception) {
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
    
    // Read content cards from cache (pre-loaded on session start, updated after events)
    val cachedCards = rememberCachedContentCards()
    
    // Process cached cards on initial load and when cache updates
    LaunchedEffect(cachedCards) {
        // Filter cards by location key-value pair from cache
        var foundCard: Any? = null
        for (card in cachedCards) {
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
                        foundCard = card
                        try {
                            val getIdMethod = card.javaClass.getMethod("getId")
                            val cardId = getIdMethod.invoke(card) as? String
                            BrazeLogManager.logContentCardMatched(locationKey, cardId)
                        } catch (e: Exception) {
                            BrazeLogManager.logContentCardMatched(locationKey, null)
                        }
                        break
                    }
                }
            } catch (e: Exception) {
                continue
            }
        }
        
        contentCard = foundCard
        hasCard = foundCard != null
    }
    
    // Subscribe to Braze Content Cards updates (for real-time updates when cache refreshes)
    DisposableEffect(Unit) {
        // Subscribe to receive Content Cards when they update
        val subscription = BrazeUserSync.subscribeToContentCardsUpdates(context) { cards ->
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
                            foundCard = card
                            try {
                                val getIdMethod = card.javaClass.getMethod("getId")
                                val cardId = getIdMethod.invoke(card) as? String
                                BrazeLogManager.logContentCardMatched(locationKey, cardId)
                            } catch (e: Exception) {
                                BrazeLogManager.logContentCardMatched(locationKey, null)
                            }
                            break
                        }
                    }
                } catch (e: Exception) {
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
                BrazeLogManager.logImpression("Content Card", "tile_2")
            } catch (e: Exception) {
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
                        BrazeLogManager.logClick("Content Card", "tile_2")
                    } catch (e: Exception) {
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
    
    // Read banner from cache (pre-loaded on session start, updated after events)
    val cachedBanner = rememberCachedBanner(placementId)
    var banner by remember(placementId) { mutableStateOf<Any?>(cachedBanner) }
    var shouldRender by remember(placementId) { mutableStateOf(false) }
    
    // Update banner when cache changes, or fetch directly if cache is empty
    LaunchedEffect(cachedBanner) {
        if (cachedBanner != null) {
            // Use cached banner
            banner = cachedBanner
        } else {
            // Cache is empty - fetch directly from Braze as fallback
            banner = BrazeUserSync.getBanner(context, placementId)
        }
        
        // Check if banner exists and is not a control variant
        if (banner != null) {
            try {
                val isControlMethod = banner!!.javaClass.getMethod("isControl")
                val isControl = isControlMethod.invoke(banner) as? Boolean ?: false
                shouldRender = !isControl
            } catch (e: Exception) {
                // If isControl method doesn't exist, assume we should render
                shouldRender = true
            }
        } else {
            shouldRender = false
        }
    }
    
    // Always render container (2:1 aspect ratio banner layout, non-collapsible)
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(2f) // 2:1 banner (width:height)
            .clip(RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        color = if (shouldRender && banner != null) Color.Transparent else colorScheme.surfaceVariant,
        border = if (!shouldRender || banner == null) {
            androidx.compose.foundation.BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.5f))
        } else null,
        shadowElevation = 0.dp // No shadow - banner provides its own styling
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (shouldRender && banner != null) {
                // Use exact same AndroidView implementation as BrazeBanner
                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            // Set background to transparent to prevent any color flash
                            setBackgroundColor(android.graphics.Color.TRANSPARENT)
                            
                            // Custom WebViewClient to intercept deep links
                            webViewClient = object : WebViewClient() {
                                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                                    // Check if the URL is a philstore deep link
                                    if (url != null && url.startsWith("philstore://")) {
                                        try {
                                            Log.d("BrazeBanner", "Intercepted deep link: $url")
                                            
                                            // Parse the URI (handles URL encoding automatically)
                                            val uri = Uri.parse(url)
                                            Log.d("BrazeBanner", "Parsed URI - scheme: ${uri.scheme}, host: ${uri.host}")
                                            
                                            // Create an intent to handle the deep link (matching in-app message handler)
                                            val intent = Intent(Intent.ACTION_VIEW, uri)
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                            intent.setPackage(ctx.packageName)
                                            
                                            // Check if MainActivity can handle this intent
                                            if (intent.resolveActivity(ctx.packageManager) != null) {
                                                ctx.startActivity(intent)
                                                Log.d("BrazeBanner", "Started MainActivity with deep link: $url")
                                            } else {
                                                Log.w("BrazeBanner", "Could not resolve activity for deep link: $url")
                                            }
                                            
                                            // Return true to indicate we handled the URL
                                            return true
                                        } catch (e: Exception) {
                                            Log.e("BrazeBanner", "Error handling deep link: ${e.message}", e)
                                            // If something goes wrong, let WebView handle it
                                            return false
                                        }
                                    }
                                    // For other URLs, let WebView handle them normally
                                    return false
                                }
                            }
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.loadWithOverviewMode = true
                            settings.useWideViewPort = true
                            // Center content horizontally
                            settings.layoutAlgorithm = android.webkit.WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING
                        }
                    },
                    update = { webView ->
                        // Only update if banner state has changed (prevent repeated updates)
                        val currentBanner = banner
                        if (currentBanner == null) {
                            return@AndroidView // No banner to display
                        }
                        // Only skip if this exact banner is already loaded
                        if (webView.tag == currentBanner) {
                            return@AndroidView // Already loaded this banner
                        }
                        webView.tag = currentBanner // Mark as loaded
                        
                        // Helper function to handle deep links (matching in-app message handler)
                        fun handleDeepLink(url: String) {
                            try {
                                Log.d("BrazeBanner", "Handling deep link: $url")
                                
                                // Parse the URI (handles URL encoding automatically)
                                val uri = Uri.parse(url)
                                Log.d("BrazeBanner", "Parsed URI - scheme: ${uri.scheme}, host: ${uri.host}")
                                
                                // Create an intent to handle the deep link (matching in-app message handler)
                                val intent = Intent(Intent.ACTION_VIEW, uri)
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                intent.setPackage(context.packageName)
                                
                                // Check if MainActivity can handle this intent
                                if (intent.resolveActivity(context.packageManager) != null) {
                                    context.startActivity(intent)
                                    Log.d("BrazeBanner", "Started MainActivity with deep link: $url")
                                } else {
                                    Log.w("BrazeBanner", "Could not resolve activity for deep link: $url")
                                }
                            } catch (e: Exception) {
                                Log.e("BrazeBanner", "Error handling deep link: ${e.message}", e)
                            }
                        }
                        
                        // JavaScript interface to handle deep links directly from JavaScript
                        class DeepLinkHandler(private val handler: (String) -> Unit) {
                            @JavascriptInterface
                            fun handleDeepLink(url: String) {
                                android.os.Handler(android.os.Looper.getMainLooper()).post {
                                    handler(url)
                                }
                            }
                        }
                        
                        // Add JavaScript interface BEFORE setting WebViewClient
                        webView.addJavascriptInterface(DeepLinkHandler(::handleDeepLink), "AndroidDeepLinkHandler")
                        
                        // Create custom WebViewClient to handle deep links (matching in-app message handler)
                        val customWebViewClient = object : WebViewClient() {
                            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                                // Check if the URL is a philstore deep link
                                if (url != null && url.startsWith("philstore://")) {
                                    handleDeepLink(url)
                                    // Return true to indicate we handled the URL
                                    return true
                                }
                                // For other URLs, let WebView handle them normally
                                return false
                            }
                            
                            // Also override the newer API method (Android 24+)
                            @android.annotation.SuppressLint("NewApi")
                            override fun shouldOverrideUrlLoading(view: WebView?, request: android.webkit.WebResourceRequest?): Boolean {
                                val url = request?.url?.toString()
                                if (url != null && url.startsWith("philstore://")) {
                                    handleDeepLink(url)
                                    return true
                                }
                                return false
                            }
                            
                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                
                                // Only inject JavaScript if page is actually loaded (not about:blank)
                                if (url == null || url == "about:blank" || url.startsWith("data:")) {
                                    return
                                }
                                
                                // Inject JavaScript after page is fully loaded
                                view?.postDelayed({
                                    try {
                                        val jsCode = """
                                            (function() {
                                                function handleDeepLink(url) {
                                                    if (url && url.startsWith('philstore://')) {
                                                        if (window.AndroidDeepLinkHandler) {
                                                            window.AndroidDeepLinkHandler.handleDeepLink(url);
                                                            return true;
                                                        }
                                                        window.location.href = url;
                                                        return true;
                                                    }
                                                    return false;
                                                }
                                                
                                                var originalOpen = window.open;
                                                window._originalAssign = window.location.assign;
                                                window._originalReplace = window.location.replace;
                                                
                                                window.open = function(url, target, features) {
                                                    if (url && url.startsWith('philstore://')) {
                                                        if (handleDeepLink(url)) return null;
                                                    }
                                                    return originalOpen.apply(window, arguments);
                                                };
                                                
                                                window.location.assign = function(url) {
                                                    if (url && url.startsWith('philstore://')) {
                                                        if (handleDeepLink(url)) return;
                                                    }
                                                    return window._originalAssign?.apply(window.location, arguments);
                                                };
                                                
                                                window.location.replace = function(url) {
                                                    if (url && url.startsWith('philstore://')) {
                                                        if (handleDeepLink(url)) return;
                                                    }
                                                    return window._originalReplace?.apply(window.location, arguments);
                                                };
                                                
                                                function setupClickInterceptors() {
                                                    document.addEventListener('click', function(e) {
                                                        var target = e.target;
                                                        var link = null;
                                                        
                                                        while (target && target !== document.body) {
                                                            if (target.tagName === 'A' && target.href) {
                                                                link = target;
                                                                break;
                                                            }
                                                            target = target.parentElement;
                                                        }
                                                        
                                                        if (link && link.href) {
                                                            if (handleDeepLink(link.href)) {
                                                                e.preventDefault();
                                                                e.stopPropagation();
                                                                e.stopImmediatePropagation();
                                                                return false;
                                                            }
                                                        } else {
                                                            var clicked = e.target;
                                                            var deepLink = null;
                                                            var onclickAttr = clicked.getAttribute('onclick');
                                                            var dataHref = clicked.getAttribute('data-href');
                                                            var href = clicked.href;
                                                            
                                                            if (onclickAttr && onclickAttr.includes('philstore://')) {
                                                                var match = onclickAttr.match(/philstore:\/\/[^"'\s)]+/);
                                                                if (match) deepLink = match[0];
                                                            } else if (dataHref && dataHref.includes('philstore://')) {
                                                                deepLink = dataHref;
                                                            } else if (href && href.includes('philstore://')) {
                                                                deepLink = href;
                                                            } else {
                                                                var parent = clicked.parentElement;
                                                                var depth = 0;
                                                                while (parent && depth < 5) {
                                                                    var parentOnclick = parent.getAttribute('onclick');
                                                                    var parentDataHref = parent.getAttribute('data-href');
                                                                    var parentHref = parent.href;
                                                                    
                                                                    if (parentOnclick && parentOnclick.includes('philstore://')) {
                                                                        var match = parentOnclick.match(/philstore:\/\/[^"'\s)]+/);
                                                                        if (match) {
                                                                            deepLink = match[0];
                                                                            break;
                                                                        }
                                                                    } else if (parentDataHref && parentDataHref.includes('philstore://')) {
                                                                        deepLink = parentDataHref;
                                                                        break;
                                                                    } else if (parentHref && parentHref.includes('philstore://')) {
                                                                        deepLink = parentHref;
                                                                        break;
                                                                    }
                                                                    parent = parent.parentElement;
                                                                    depth++;
                                                                }
                                                            }
                                                            
                                                            if (deepLink) {
                                                                if (handleDeepLink(deepLink)) {
                                                                    e.preventDefault();
                                                                    e.stopPropagation();
                                                                    e.stopImmediatePropagation();
                                                                    return false;
                                                                }
                                                            }
                                                        }
                                                    }, true);
                                                    
                                                    function attachToLinks() {
                                                        var htmlContent = document.documentElement.innerHTML || '';
                                                        var regex = /philstore:\/\/[^"'\s<>)]+/g;
                                                        var matches = htmlContent.match(regex);
                                                        if (matches && matches.length > 0) {
                                                            window._bannerDeepLink = matches[0];
                                                        }
                                                        
                                                        var clickableElements = document.querySelectorAll('[onclick*="philstore://"], button, [data-href*="philstore://"], [onclick]');
                                                        clickableElements.forEach(function(el) {
                                                            el.addEventListener('click', function(e) {
                                                                var storedDeepLink = window._bannerDeepLink;
                                                                if (!storedDeepLink) {
                                                                    storedDeepLink = htmlContent.match(regex)?.[0];
                                                                    window._bannerDeepLink = storedDeepLink;
                                                                }
                                                                
                                                                if (storedDeepLink) {
                                                                    if (handleDeepLink(storedDeepLink)) {
                                                                        e.preventDefault();
                                                                        e.stopPropagation();
                                                                        e.stopImmediatePropagation();
                                                                        return false;
                                                                    }
                                                                }
                                                            }, true);
                                                        });
                                                    }
                                                    
                                                    if (document.readyState === 'loading') {
                                                        document.addEventListener('DOMContentLoaded', attachToLinks);
                                                    } else {
                                                        attachToLinks();
                                                    }
                                                    
                                                    setTimeout(attachToLinks, 500);
                                                    setTimeout(attachToLinks, 1000);
                                                    setTimeout(attachToLinks, 2000);
                                                }
                                                
                                                if (document.readyState === 'loading') {
                                                    document.addEventListener('DOMContentLoaded', setupClickInterceptors);
                                                } else {
                                                    setupClickInterceptors();
                                                }
                                            })();
                                        """.trimIndent()
                                        view?.evaluateJavascript(jsCode, null)
                                    } catch (e: Exception) {
                                        // Error injecting JavaScript
                                    }
                                }, 100)
                            }
                        }
                        
                        // Set WebViewClient BEFORE calling insertBanner to ensure it's in place
                        webView.webViewClient = customWebViewClient
                        webView.setBackgroundColor(android.graphics.Color.TRANSPARENT)
                        
                        try {
                            // Braze SDK: Try to use insertBanner method (Braze SDK method)
                            val brazeInstance = Braze.getInstance(context)
                            val insertMethod = brazeInstance.javaClass.getMethod(
                                "insertBanner", 
                                currentBanner.javaClass,
                                android.view.View::class.java
                            )
                            insertMethod.invoke(brazeInstance, currentBanner, webView)
                            
                            // Re-apply WebViewClient after insertBanner (in case Braze replaced it)
                            webView.post {
                                    webView.setBackgroundColor(android.graphics.Color.TRANSPARENT)
                                    webView.webViewClient = customWebViewClient
                                    Log.d("BrazeBanner", "Re-applied custom WebViewClient after Braze insertBanner")
                                }
                        } catch (e: Exception) {
                            // If insertBanner doesn't work, try to get HTML content
                            try {
                                val htmlMethod = currentBanner.javaClass.getMethod("getHtml")
                                val htmlContent = htmlMethod.invoke(currentBanner) as? String
                                if (htmlContent != null) {
                                    val preloadJs = """
                                        (function() {
                                            function handleDeepLink(url) {
                                                if (url && url.startsWith('philstore://')) {
                                                    if (window.AndroidDeepLinkHandler) {
                                                        window.AndroidDeepLinkHandler.handleDeepLink(url);
                                                        return true;
                                                    }
                                                    return false;
                                                }
                                                return false;
                                            }
                                            
                                            window._originalOpen = window.open;
                                            window.open = function(url, target, features) {
                                                if (url && url.startsWith('philstore://')) {
                                                    if (handleDeepLink(url)) return null;
                                                }
                                                return window._originalOpen.apply(window, arguments);
                                            };
                                            
                                            window.location.assign = function(url) {
                                                if (url && url.startsWith('philstore://')) {
                                                    if (handleDeepLink(url)) return;
                                                }
                                                return window._originalAssign?.apply(window.location, arguments);
                                            };
                                            
                                            window.location.replace = function(url) {
                                                if (url && url.startsWith('philstore://')) {
                                                    if (handleDeepLink(url)) return;
                                                }
                                                return window._originalReplace?.apply(window.location, arguments);
                                            };
                                            
                                            document.addEventListener('DOMContentLoaded', function() {
                                                function findDeepLinkInDocument() {
                                                    var htmlContent = document.documentElement.innerHTML || '';
                                                    var regex = /philstore:\/\/[^"'\s<>)]+/g;
                                                    var matches = htmlContent.match(regex);
                                                    if (matches && matches.length > 0) {
                                                        return matches[0];
                                                    }
                                                    return null;
                                                }
                                                
                                                var deepLinkUrl = findDeepLinkInDocument();
                                                window._bannerDeepLink = deepLinkUrl;
                                                
                                                document.addEventListener('click', function(e) {
                                                    var storedDeepLink = window._bannerDeepLink;
                                                    if (!storedDeepLink) {
                                                        storedDeepLink = findDeepLinkInDocument();
                                                        window._bannerDeepLink = storedDeepLink;
                                                    }
                                                    
                                                    if (storedDeepLink) {
                                                        if (handleDeepLink(storedDeepLink)) {
                                                            e.preventDefault();
                                                            e.stopPropagation();
                                                            e.stopImmediatePropagation();
                                                            return false;
                                                        }
                                                    }
                                                }, true);
                                            });
                                        })();
                                    """.trimIndent()
                                    
                                    val htmlWithInterceptors = """
                                        <!DOCTYPE html>
                                        <html>
                                        <head>
                                            <meta name="viewport" content="width=device-width, initial-scale=1.0">
                                            <script>
                                                $preloadJs
                                            </script>
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
                                    
                                    // Set WebViewClient before loading HTML
                                    webView.webViewClient = customWebViewClient
                                    webView.loadDataWithBaseURL(null, htmlWithInterceptors, "text/html", "UTF-8", null)
                                    
                                    webView.postDelayed({
                                        try {
                                            val jsCode = """
                                                (function() {
                                                    function handleDeepLink(url) {
                                                        if (url && url.startsWith('philstore://')) {
                                                            if (window.AndroidDeepLinkHandler) {
                                                                window.AndroidDeepLinkHandler.handleDeepLink(url);
                                                                return true;
                                                            }
                                                            window.location.href = url;
                                                            return true;
                                                        }
                                                        return false;
                                                    }
                                                    
                                                    function setupClickInterceptors() {
                                                        document.addEventListener('click', function(e) {
                                                            var target = e.target;
                                                            var link = null;
                                                            
                                                            while (target && target !== document.body) {
                                                                if (target.tagName === 'A' && target.href) {
                                                                    link = target;
                                                                    break;
                                                                }
                                                                target = target.parentElement;
                                                            }
                                                            
                                                            if (link && link.href) {
                                                                if (handleDeepLink(link.href)) {
                                                                    e.preventDefault();
                                                                    e.stopPropagation();
                                                                    e.stopImmediatePropagation();
                                                                    return false;
                                                                }
                                                            } else {
                                                                var clicked = e.target;
                                                                var deepLink = null;
                                                                var onclickAttr = clicked.getAttribute('onclick');
                                                                var dataHref = clicked.getAttribute('data-href');
                                                                var href = clicked.href;
                                                                
                                                                if (onclickAttr && onclickAttr.includes('philstore://')) {
                                                                    var match = onclickAttr.match(/philstore:\/\/[^"'\s)]+/);
                                                                    if (match) deepLink = match[0];
                                                                } else if (dataHref && dataHref.includes('philstore://')) {
                                                                    deepLink = dataHref;
                                                                } else if (href && href.includes('philstore://')) {
                                                                    deepLink = href;
                                                                } else {
                                                                    var parent = clicked.parentElement;
                                                                    var depth = 0;
                                                                    while (parent && depth < 5) {
                                                                        var parentOnclick = parent.getAttribute('onclick');
                                                                        var parentDataHref = parent.getAttribute('data-href');
                                                                        var parentHref = parent.href;
                                                                        
                                                                        if (parentOnclick && parentOnclick.includes('philstore://')) {
                                                                            var match = parentOnclick.match(/philstore:\/\/[^"'\s)]+/);
                                                                            if (match) {
                                                                                deepLink = match[0];
                                                                                break;
                                                                            }
                                                                        } else if (parentDataHref && parentDataHref.includes('philstore://')) {
                                                                            deepLink = parentDataHref;
                                                                            break;
                                                                        } else if (parentHref && parentHref.includes('philstore://')) {
                                                                            deepLink = parentHref;
                                                                            break;
                                                                        }
                                                                        parent = parent.parentElement;
                                                                        depth++;
                                                                    }
                                                                }
                                                                
                                                                if (deepLink) {
                                                                    if (handleDeepLink(deepLink)) {
                                                                        e.preventDefault();
                                                                        e.stopPropagation();
                                                                        e.stopImmediatePropagation();
                                                                        return false;
                                                                    }
                                                                }
                                                            }
                                                        }, true);
                                                        
                                                        function attachToLinks() {
                                                            var htmlContent = document.documentElement.innerHTML || '';
                                                            var regex = /philstore:\/\/[^"'\s<>)]+/g;
                                                            var matches = htmlContent.match(regex);
                                                            if (matches && matches.length > 0) {
                                                                window._bannerDeepLink = matches[0];
                                                            }
                                                            
                                                            var clickableElements = document.querySelectorAll('[onclick*="philstore://"], button, [data-href*="philstore://"], [onclick]');
                                                            clickableElements.forEach(function(el) {
                                                                el.addEventListener('click', function(e) {
                                                                    var storedDeepLink = window._bannerDeepLink;
                                                                    if (!storedDeepLink) {
                                                                        storedDeepLink = htmlContent.match(regex)?.[0];
                                                                        window._bannerDeepLink = storedDeepLink;
                                                                    }
                                                                    
                                                                    if (storedDeepLink) {
                                                                        if (handleDeepLink(storedDeepLink)) {
                                                                            e.preventDefault();
                                                                            e.stopPropagation();
                                                                            e.stopImmediatePropagation();
                                                                            return false;
                                                                        }
                                                                    }
                                                                }, true);
                                                            });
                                                        }
                                                        
                                                        if (document.readyState === 'loading') {
                                                            document.addEventListener('DOMContentLoaded', attachToLinks);
                                                        } else {
                                                            attachToLinks();
                                                        }
                                                        
                                                        setTimeout(attachToLinks, 500);
                                                        setTimeout(attachToLinks, 1000);
                                                        setTimeout(attachToLinks, 2000);
                                                    }
                                                    
                                                    if (document.readyState === 'loading') {
                                                        document.addEventListener('DOMContentLoaded', setupClickInterceptors);
                                                    } else {
                                                        setupClickInterceptors();
                                                    }
                                                })();
                                            """.trimIndent()
                                            webView.evaluateJavascript(jsCode, null)
                                        } catch (e: Exception) {
                                            // Error injecting JavaScript
                                        }
                                    }, 500)
                                    
                                    Log.d("BrazeBanner", "Loaded HTML content with custom WebViewClient")
                                }
                            } catch (e2: Exception) {
                                // Both methods failed - banner might not be renderable
                                Log.e("BrazeBanner", "Error loading banner: ${e2.message}", e2)
                            }
                        }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxSize()
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
 * Banner: Tile Banner (1:1 Square Banner Layout, Non-Collapsing)
 * 
 * Custom Braze Banner implementation that always displays a container:
 * - Uses placement ID: tile_banner
 * - Displays banner when available (1:1 square layout)
 * - Shows placeholder when no banner is available (does not collapse)
 * - Placeholder displays "Banner" and "Placement ID: tile_banner"
 */
@Composable
fun TileBanner(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme
    val placementId = "tile_banner"
    
    // Read banner from cache (pre-loaded on session start, updated after events)
    val cachedBanner = rememberCachedBanner(placementId)
    var banner by remember(placementId) { mutableStateOf<Any?>(cachedBanner) }
    var shouldRender by remember(placementId) { mutableStateOf(false) }
    var bannerClickUrl by remember(placementId) { mutableStateOf<String?>(null) }
    
    // Update banner when cache changes, or fetch directly if cache is empty
    LaunchedEffect(cachedBanner) {
        if (cachedBanner != null) {
            // Use cached banner
            banner = cachedBanner
        } else {
            // Cache is empty - fetch directly from Braze as fallback
            banner = BrazeUserSync.getBanner(context, placementId)
        }
        
        // Check if banner exists and is not a control variant
        if (banner != null) {
            try {
                val isControlMethod = banner!!.javaClass.getMethod("isControl")
                val isControl = isControlMethod.invoke(banner) as? Boolean ?: false
                shouldRender = !isControl
                
                // Get banner click URL if available
                try {
                    val getClickUrlMethod = banner!!.javaClass.getMethod("getClickUrl")
                    val clickUrl = getClickUrlMethod.invoke(banner) as? String
                    bannerClickUrl = clickUrl
                } catch (e: Exception) {
                    // Banner doesn't have getClickUrl method, try getUrl
                    try {
                        val getUrlMethod = banner!!.javaClass.getMethod("getUrl")
                        val url = getUrlMethod.invoke(banner) as? String
                        bannerClickUrl = url
                    } catch (e2: Exception) {
                        bannerClickUrl = null
                    }
                }
            } catch (e: Exception) {
                // If isControl method doesn't exist, assume we should render
                shouldRender = true
            }
        } else {
            shouldRender = false
            bannerClickUrl = null
        }
    }
    
    // Helper function to handle banner click
    fun handleBannerClick() {
        if (bannerClickUrl != null && banner != null) {
            // Log click to Braze analytics
            try {
                val logClickMethod = banner!!.javaClass.getMethod("logClick")
                logClickMethod.invoke(banner)
                BrazeLogManager.logClick("Banner", placementId)
            } catch (e: Exception) {
                // Banner doesn't have logClick method
            }
            
            // Handle deep link navigation (philstore://) or external URL
            if (bannerClickUrl!!.startsWith("philstore://")) {
                try {
                    val uri = Uri.parse(bannerClickUrl!!)
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    intent.setPackage(context.packageName)
                    if (intent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(intent)
                    }
                } catch (e: Exception) {
                    // Error handling deep link
                }
            }
        }
    }
    
    // Always render container (1:1 aspect ratio square layout)
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f) // 1:1 square (width:height)
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = shouldRender && banner != null && bannerClickUrl != null) {
                handleBannerClick()
            },
        shape = RoundedCornerShape(12.dp),
        color = if (shouldRender && banner != null) Color.Transparent else colorScheme.surfaceVariant,
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
                // Display banner (same implementation as ContentBanner)
                AndroidView(
                    factory = { ctx ->
                        android.webkit.WebView(ctx).apply {
                            setBackgroundColor(android.graphics.Color.TRANSPARENT)
                            
                            webViewClient = object : android.webkit.WebViewClient() {
                                override fun shouldOverrideUrlLoading(view: android.webkit.WebView?, url: String?): Boolean {
                                    // Check if the URL is a philstore deep link
                                    if (url != null && url.startsWith("philstore://")) {
                                        try {
                                            // Parse the URI (handles URL encoding automatically)
                                            val uri = android.net.Uri.parse(url)
                                            
                                            // Create an intent to handle the deep link (matching in-app message handler)
                                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, uri)
                                            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                            intent.setPackage(ctx.packageName)
                                            
                                            // Check if MainActivity can handle this intent
                                            if (intent.resolveActivity(ctx.packageManager) != null) {
                                                ctx.startActivity(intent)
                                            }
                                            
                                            // Return true to indicate we handled the URL
                                            return true
                                        } catch (e: Exception) {
                                            // If something goes wrong, let WebView handle it
                                            return false
                                        }
                                    }
                                    // For other URLs, let WebView handle them normally
                                    return false
                                }
                                
                                // Also override the newer API method (Android 24+)
                                @android.annotation.SuppressLint("NewApi")
                                override fun shouldOverrideUrlLoading(view: android.webkit.WebView?, request: android.webkit.WebResourceRequest?): Boolean {
                                    val url = request?.url?.toString()
                                    if (url != null && url.startsWith("philstore://")) {
                                        try {
                                            val uri = android.net.Uri.parse(url)
                                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, uri)
                                            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                            intent.setPackage(ctx.packageName)
                                            if (intent.resolveActivity(ctx.packageManager) != null) {
                                                ctx.startActivity(intent)
                                            }
                                            return true
                                        } catch (e: Exception) {
                                            return false
                                        }
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
                        // Only update if banner state has changed (prevent repeated updates)
                        val currentBanner = banner
                        if (currentBanner == null) {
                            return@AndroidView // No banner to display
                        }
                        // Only skip if this exact banner is already loaded
                        if (webView.tag == currentBanner) {
                            return@AndroidView // Already loaded this banner
                        }
                        webView.tag = currentBanner // Mark as loaded
                        
                        // Helper function to handle deep links
                        fun handleDeepLink(url: String) {
                            try {
                                val uri = android.net.Uri.parse(url)
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, uri)
                                intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                intent.setPackage(context.packageName)
                                val resolvedActivity = intent.resolveActivity(context.packageManager)
                                if (resolvedActivity != null) {
                                    context.startActivity(intent)
                                }
                            } catch (e: Exception) {
                                // Error handling deep link
                            }
                        }
                        
                        // JavaScript interface for deep links
                        class DeepLinkHandler(private val handler: (String) -> Unit) {
                            @android.webkit.JavascriptInterface
                            fun handleDeepLink(url: String) {
                                android.os.Handler(android.os.Looper.getMainLooper()).post {
                                    handler(url)
                                }
                            }
                        }
                        
                        webView.addJavascriptInterface(DeepLinkHandler(::handleDeepLink), "AndroidDeepLinkHandler")
                        
                        val customWebViewClient = object : android.webkit.WebViewClient() {
                            override fun shouldOverrideUrlLoading(view: android.webkit.WebView?, url: String?): Boolean {
                                if (url != null && url.startsWith("philstore://")) {
                                    handleDeepLink(url)
                                    return true
                                }
                                return false
                            }
                            
                            @android.annotation.SuppressLint("NewApi")
                            override fun shouldOverrideUrlLoading(view: android.webkit.WebView?, request: android.webkit.WebResourceRequest?): Boolean {
                                val url = request?.url?.toString()
                                if (url != null && url.startsWith("philstore://")) {
                                    handleDeepLink(url)
                                    return true
                                }
                                return false
                            }
                            
                            override fun onPageFinished(view: android.webkit.WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                
                                // Only inject JavaScript if page is actually loaded (not about:blank)
                                if (url == null || url == "about:blank" || url.startsWith("data:")) {
                                    return
                                }
                                
                                // Inject JavaScript after page is fully loaded
                                view?.postDelayed({
                                    try {
                                        // Get banner click URL for full-banner click handling
                                        val bannerClickUrl = try {
                                            val getClickUrlMethod = currentBanner?.javaClass?.getMethod("getClickUrl")
                                            getClickUrlMethod?.invoke(currentBanner) as? String
                                        } catch (e: Exception) {
                                            try {
                                                val getUrlMethod = currentBanner?.javaClass?.getMethod("getUrl")
                                                getUrlMethod?.invoke(currentBanner) as? String
                                            } catch (e2: Exception) {
                                                null
                                            }
                                        }
                                        
                                        val jsCode = """
                                            (function() {
                                                var bannerClickUrl = ${if (bannerClickUrl != null) "'$bannerClickUrl'" else "null"};
                                                
                                                function handleDeepLink(url) {
                                                    if (url && url.startsWith('philstore://')) {
                                                        if (window.AndroidDeepLinkHandler) {
                                                            window.AndroidDeepLinkHandler.handleDeepLink(url);
                                                            return true;
                                                        }
                                                        window.location.href = url;
                                                        return true;
                                                    }
                                                    return false;
                                                }
                                                
                                                // Make entire banner clickable if there's a click URL
                                                if (bannerClickUrl) {
                                                    // Add click handler to document body
                                                    document.body.style.cursor = 'pointer';
                                                    document.body.addEventListener('click', function(e) {
                                                        // Only handle if no other clickable element was clicked
                                                        var target = e.target;
                                                        var isClickableElement = false;
                                                        
                                                        // Check if target or parent is a clickable element
                                                        while (target && target !== document.body) {
                                                            if (target.tagName === 'A' || target.tagName === 'BUTTON' || 
                                                                target.onclick || target.getAttribute('onclick') ||
                                                                target.href || target.getAttribute('href') ||
                                                                target.getAttribute('data-href')) {
                                                                isClickableElement = true;
                                                                break;
                                                            }
                                                            target = target.parentElement;
                                                        }
                                                        
                                                        // If no clickable element, handle banner click
                                                        if (!isClickableElement) {
                                                            if (handleDeepLink(bannerClickUrl)) {
                                                                e.preventDefault();
                                                                e.stopPropagation();
                                                                e.stopImmediatePropagation();
                                                                return false;
                                                            }
                                                        }
                                                    }, true);
                                                }
                                                
                                                var originalOpen = window.open;
                                                window._originalAssign = window.location.assign;
                                                window._originalReplace = window.location.replace;
                                                
                                                window.open = function(url, target, features) {
                                                    if (url && url.startsWith('philstore://')) {
                                                        if (handleDeepLink(url)) return null;
                                                    }
                                                    return originalOpen.apply(window, arguments);
                                                };
                                                
                                                window.location.assign = function(url) {
                                                    if (url && url.startsWith('philstore://')) {
                                                        if (handleDeepLink(url)) return;
                                                    }
                                                    return window._originalAssign?.apply(window.location, arguments);
                                                };
                                                
                                                window.location.replace = function(url) {
                                                    if (url && url.startsWith('philstore://')) {
                                                        if (handleDeepLink(url)) return;
                                                    }
                                                    return window._originalReplace?.apply(window.location, arguments);
                                                };
                                                
                                                function setupClickInterceptors() {
                                                    document.addEventListener('click', function(e) {
                                                        var target = e.target;
                                                        var link = null;
                                                        
                                                        while (target && target !== document.body) {
                                                            if (target.tagName === 'A' && target.href) {
                                                                link = target;
                                                                break;
                                                            }
                                                            target = target.parentElement;
                                                        }
                                                        
                                                        if (link && link.href) {
                                                            if (handleDeepLink(link.href)) {
                                                                e.preventDefault();
                                                                e.stopPropagation();
                                                                e.stopImmediatePropagation();
                                                                return false;
                                                            }
                                                        } else {
                                                            var clicked = e.target;
                                                            var deepLink = null;
                                                            var onclickAttr = clicked.getAttribute('onclick');
                                                            var dataHref = clicked.getAttribute('data-href');
                                                            var href = clicked.href;
                                                            
                                                            if (onclickAttr && onclickAttr.includes('philstore://')) {
                                                                var match = onclickAttr.match(/philstore:\/\/[^"'\s)]+/);
                                                                if (match) deepLink = match[0];
                                                            } else if (dataHref && dataHref.includes('philstore://')) {
                                                                deepLink = dataHref;
                                                            } else if (href && href.includes('philstore://')) {
                                                                deepLink = href;
                                                            } else {
                                                                var parent = clicked.parentElement;
                                                                var depth = 0;
                                                                while (parent && depth < 5) {
                                                                    var parentOnclick = parent.getAttribute('onclick');
                                                                    var parentDataHref = parent.getAttribute('data-href');
                                                                    var parentHref = parent.href;
                                                                    
                                                                    if (parentOnclick && parentOnclick.includes('philstore://')) {
                                                                        var match = parentOnclick.match(/philstore:\/\/[^"'\s)]+/);
                                                                        if (match) {
                                                                            deepLink = match[0];
                                                                            break;
                                                                        }
                                                                    } else if (parentDataHref && parentDataHref.includes('philstore://')) {
                                                                        deepLink = parentDataHref;
                                                                        break;
                                                                    } else if (parentHref && parentHref.includes('philstore://')) {
                                                                        deepLink = parentHref;
                                                                        break;
                                                                    }
                                                                    parent = parent.parentElement;
                                                                    depth++;
                                                                }
                                                            }
                                                            
                                                            if (deepLink) {
                                                                if (handleDeepLink(deepLink)) {
                                                                    e.preventDefault();
                                                                    e.stopPropagation();
                                                                    e.stopImmediatePropagation();
                                                                    return false;
                                                                }
                                                            }
                                                        }
                                                    }, true);
                                                    
                                                    function attachToLinks() {
                                                        var htmlContent = document.documentElement.innerHTML || '';
                                                        var regex = /philstore:\/\/[^"'\s<>)]+/g;
                                                        var matches = htmlContent.match(regex);
                                                        if (matches && matches.length > 0) {
                                                            window._bannerDeepLink = matches[0];
                                                        }
                                                        
                                                        var clickableElements = document.querySelectorAll('[onclick*="philstore://"], button, [data-href*="philstore://"], [onclick]');
                                                        clickableElements.forEach(function(el) {
                                                            el.addEventListener('click', function(e) {
                                                                var storedDeepLink = window._bannerDeepLink;
                                                                if (!storedDeepLink) {
                                                                    storedDeepLink = htmlContent.match(regex)?.[0];
                                                                    window._bannerDeepLink = storedDeepLink;
                                                                }
                                                                
                                                                if (storedDeepLink) {
                                                                    if (handleDeepLink(storedDeepLink)) {
                                                                        e.preventDefault();
                                                                        e.stopPropagation();
                                                                        e.stopImmediatePropagation();
                                                                        return false;
                                                                    }
                                                                }
                                                            }, true);
                                                        });
                                                    }
                                                    
                                                    if (document.readyState === 'loading') {
                                                        document.addEventListener('DOMContentLoaded', attachToLinks);
                                                    } else {
                                                        attachToLinks();
                                                    }
                                                    
                                                    setTimeout(attachToLinks, 500);
                                                    setTimeout(attachToLinks, 1000);
                                                    setTimeout(attachToLinks, 2000);
                                                }
                                                
                                                if (document.readyState === 'loading') {
                                                    document.addEventListener('DOMContentLoaded', setupClickInterceptors);
                                                } else {
                                                    setupClickInterceptors();
                                                }
                                            })();
                                        """.trimIndent()
                                        view?.evaluateJavascript(jsCode, null)
                                    } catch (e: Exception) {
                                        // Error injecting JavaScript
                                    }
                                }, 100)
                            }
                        }
                        
                        webView.webViewClient = customWebViewClient
                        webView.setBackgroundColor(android.graphics.Color.TRANSPARENT)
                        
                        // Braze SDK: Use the banner from state (already fetched in LaunchedEffect)
                        if (currentBanner != null) {
                            try {
                                val brazeInstance = com.braze.Braze.getInstance(context)
                                val insertMethod = brazeInstance.javaClass.getMethod(
                                    "insertBanner",
                                    currentBanner.javaClass,
                                    android.view.View::class.java
                                )
                                insertMethod.invoke(brazeInstance, currentBanner, webView)
                                
                                webView.post {
                                    webView.setBackgroundColor(android.graphics.Color.TRANSPARENT)
                                    webView.webViewClient = customWebViewClient
                                }
                            } catch (e: Exception) {
                                try {
                                    val htmlMethod = currentBanner.javaClass.getMethod("getHtml")
                                    val htmlContent = htmlMethod.invoke(currentBanner) as? String
                                    if (htmlContent != null) {
                                        // Get banner click URL for full-banner click handling
                                        val bannerClickUrl = try {
                                            val getClickUrlMethod = currentBanner.javaClass.getMethod("getClickUrl")
                                            getClickUrlMethod.invoke(currentBanner) as? String
                                        } catch (e: Exception) {
                                            try {
                                                val getUrlMethod = currentBanner.javaClass.getMethod("getUrl")
                                                getUrlMethod.invoke(currentBanner) as? String
                                            } catch (e2: Exception) {
                                                null
                                            }
                                        }
                                        
                                        val preloadJs = """
                                            (function() {
                                                var bannerClickUrl = ${if (bannerClickUrl != null) "'$bannerClickUrl'" else "null"};
                                                
                                                function handleDeepLink(url) {
                                                    if (url && url.startsWith('philstore://')) {
                                                        if (window.AndroidDeepLinkHandler) {
                                                            window.AndroidDeepLinkHandler.handleDeepLink(url);
                                                            return true;
                                                        }
                                                        return false;
                                                    }
                                                    return false;
                                                }
                                                
                                                // Make entire banner clickable if there's a click URL
                                                if (bannerClickUrl) {
                                                    document.body.style.cursor = 'pointer';
                                                }
                                                
                                                window._originalOpen = window.open;
                                                window._originalAssign = window.location.assign;
                                                window._originalReplace = window.location.replace;
                                                
                                                window.open = function(url, target, features) {
                                                    if (url && url.startsWith('philstore://')) {
                                                        if (handleDeepLink(url)) return null;
                                                    }
                                                    return window._originalOpen.apply(window, arguments);
                                                };
                                                
                                                window.location.assign = function(url) {
                                                    if (url && url.startsWith('philstore://')) {
                                                        if (handleDeepLink(url)) return;
                                                    }
                                                    return window._originalAssign?.apply(window.location, arguments);
                                                };
                                                
                                                window.location.replace = function(url) {
                                                    if (url && url.startsWith('philstore://')) {
                                                        if (handleDeepLink(url)) return;
                                                    }
                                                    return window._originalReplace?.apply(window.location, arguments);
                                                };
                                                
                                                document.addEventListener('DOMContentLoaded', function() {
                                                    function findDeepLinkInDocument() {
                                                        var htmlContent = document.documentElement.innerHTML || '';
                                                        var regex = /philstore:\/\/[^"'\s<>)]+/g;
                                                        var matches = htmlContent.match(regex);
                                                        if (matches && matches.length > 0) {
                                                            return matches[0];
                                                        }
                                                        return null;
                                                    }
                                                    
                                                    var deepLinkUrl = findDeepLinkInDocument();
                                                    window._bannerDeepLink = deepLinkUrl;
                                                    
                                                    document.addEventListener('click', function(e) {
                                                        var target = e.target;
                                                        var isClickableElement = false;
                                                        
                                                        // Check if target or parent is a clickable element
                                                        while (target && target !== document.body) {
                                                            if (target.tagName === 'A' || target.tagName === 'BUTTON' || 
                                                                target.onclick || target.getAttribute('onclick') ||
                                                                target.href || target.getAttribute('href') ||
                                                                target.getAttribute('data-href')) {
                                                                isClickableElement = true;
                                                                break;
                                                            }
                                                            target = target.parentElement;
                                                        }
                                                        
                                                        // If no clickable element and banner has click URL, handle banner click
                                                        if (!isClickableElement && bannerClickUrl) {
                                                            if (handleDeepLink(bannerClickUrl)) {
                                                                e.preventDefault();
                                                                e.stopPropagation();
                                                                e.stopImmediatePropagation();
                                                                return false;
                                                            }
                                                        }
                                                        
                                                        // Otherwise, try stored deep link from HTML
                                                        var storedDeepLink = window._bannerDeepLink;
                                                        if (!storedDeepLink) {
                                                            storedDeepLink = findDeepLinkInDocument();
                                                            window._bannerDeepLink = storedDeepLink;
                                                        }
                                                        
                                                        if (storedDeepLink) {
                                                            if (handleDeepLink(storedDeepLink)) {
                                                                e.preventDefault();
                                                                e.stopPropagation();
                                                                e.stopImmediatePropagation();
                                                                return false;
                                                            }
                                                        }
                                                    }, true);
                                                });
                                            })();
                                        """.trimIndent()
                                        
                                        val htmlWithInterceptors = """
                                            <!DOCTYPE html>
                                            <html>
                                            <head>
                                                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                                                <script>
                                                    $preloadJs
                                                </script>
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
                                        
                                        // Set WebViewClient before loading HTML
                                        webView.webViewClient = customWebViewClient
                                        webView.loadDataWithBaseURL(null, htmlWithInterceptors, "text/html", "UTF-8", null)
                                        
                                        webView.postDelayed({
                                            try {
                                                val jsCode = """
                                                    (function() {
                                                        function handleDeepLink(url) {
                                                            if (url && url.startsWith('philstore://')) {
                                                                if (window.AndroidDeepLinkHandler) {
                                                                    window.AndroidDeepLinkHandler.handleDeepLink(url);
                                                                    return true;
                                                                }
                                                                window.location.href = url;
                                                                return true;
                                                            }
                                                            return false;
                                                        }
                                                        
                                                        function setupClickInterceptors() {
                                                            document.addEventListener('click', function(e) {
                                                                var target = e.target;
                                                                var link = null;
                                                                
                                                                while (target && target !== document.body) {
                                                                    if (target.tagName === 'A' && target.href) {
                                                                        link = target;
                                                                        break;
                                                                    }
                                                                    target = target.parentElement;
                                                                }
                                                                
                                                                if (link && link.href) {
                                                                    if (handleDeepLink(link.href)) {
                                                                        e.preventDefault();
                                                                        e.stopPropagation();
                                                                        e.stopImmediatePropagation();
                                                                        return false;
                                                                    }
                                                                } else {
                                                                    var clicked = e.target;
                                                                    var deepLink = null;
                                                                    var onclickAttr = clicked.getAttribute('onclick');
                                                                    var dataHref = clicked.getAttribute('data-href');
                                                                    var href = clicked.href;
                                                                    
                                                                    if (onclickAttr && onclickAttr.includes('philstore://')) {
                                                                        var match = onclickAttr.match(/philstore:\/\/[^"'\s)]+/);
                                                                        if (match) deepLink = match[0];
                                                                    } else if (dataHref && dataHref.includes('philstore://')) {
                                                                        deepLink = dataHref;
                                                                    } else if (href && href.includes('philstore://')) {
                                                                        deepLink = href;
                                                                    } else {
                                                                        var parent = clicked.parentElement;
                                                                        var depth = 0;
                                                                        while (parent && depth < 5) {
                                                                            var parentOnclick = parent.getAttribute('onclick');
                                                                            var parentDataHref = parent.getAttribute('data-href');
                                                                            var parentHref = parent.href;
                                                                            
                                                                            if (parentOnclick && parentOnclick.includes('philstore://')) {
                                                                                var match = parentOnclick.match(/philstore:\/\/[^"'\s)]+/);
                                                                                if (match) {
                                                                                    deepLink = match[0];
                                                                                    break;
                                                                                }
                                                                            } else if (parentDataHref && parentDataHref.includes('philstore://')) {
                                                                                deepLink = parentDataHref;
                                                                                break;
                                                                            } else if (parentHref && parentHref.includes('philstore://')) {
                                                                                deepLink = parentHref;
                                                                                break;
                                                                            }
                                                                            parent = parent.parentElement;
                                                                            depth++;
                                                                        }
                                                                    }
                                                                    
                                                                    if (deepLink) {
                                                                        if (handleDeepLink(deepLink)) {
                                                                            e.preventDefault();
                                                                            e.stopPropagation();
                                                                            e.stopImmediatePropagation();
                                                                            return false;
                                                                        }
                                                                    }
                                                                }
                                                            }, true);
                                                            
                                                            function attachToLinks() {
                                                                var htmlContent = document.documentElement.innerHTML || '';
                                                                var regex = /philstore:\/\/[^"'\s<>)]+/g;
                                                                var matches = htmlContent.match(regex);
                                                                if (matches && matches.length > 0) {
                                                                    window._bannerDeepLink = matches[0];
                                                                }
                                                                
                                                                var clickableElements = document.querySelectorAll('[onclick*="philstore://"], button, [data-href*="philstore://"], [onclick]');
                                                                clickableElements.forEach(function(el) {
                                                                    el.addEventListener('click', function(e) {
                                                                        var storedDeepLink = window._bannerDeepLink;
                                                                        if (!storedDeepLink) {
                                                                            storedDeepLink = htmlContent.match(regex)?.[0];
                                                                            window._bannerDeepLink = storedDeepLink;
                                                                        }
                                                                        
                                                                        if (storedDeepLink) {
                                                                            if (handleDeepLink(storedDeepLink)) {
                                                                                e.preventDefault();
                                                                                e.stopPropagation();
                                                                                e.stopImmediatePropagation();
                                                                                return false;
                                                                            }
                                                                        }
                                                                    }, true);
                                                                });
                                                            }
                                                            
                                                            if (document.readyState === 'loading') {
                                                                document.addEventListener('DOMContentLoaded', attachToLinks);
                                                            } else {
                                                                attachToLinks();
                                                            }
                                                            
                                                            setTimeout(attachToLinks, 500);
                                                            setTimeout(attachToLinks, 1000);
                                                            setTimeout(attachToLinks, 2000);
                                                        }
                                                        
                                                        if (document.readyState === 'loading') {
                                                            document.addEventListener('DOMContentLoaded', setupClickInterceptors);
                                                        } else {
                                                            setupClickInterceptors();
                                                        }
                                                    })();
                                                """.trimIndent()
                                                webView.evaluateJavascript(jsCode, null)
                                            } catch (e: Exception) {
                                                // Error injecting JavaScript
                                            }
                                        }, 500)
                                    }
                                } catch (e2: Exception) {
                                    // Error loading banner HTML
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
