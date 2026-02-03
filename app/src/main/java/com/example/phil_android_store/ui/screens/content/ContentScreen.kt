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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.ui.viewinterop.AndroidView
import com.braze.Braze
import com.example.phil_android_store.data.BrazeContentManager
import com.example.phil_android_store.data.BrazeLogManager
import com.example.phil_android_store.data.BrazeSettingsManager
import com.example.phil_android_store.data.BrazeUserSync
import com.example.phil_android_store.data.UserProfileManager
import com.example.phil_android_store.data.rememberCachedBanner
import com.example.phil_android_store.data.rememberCachedContentCards
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Dialog

/** Standard custom events available in the app (for Send Custom Event dropdown). */
private val STANDARD_CUSTOM_EVENTS = listOf(
    "enable_push",
    "push_notification",
    "user_action_button",
    "added_item_to_cart",
    "viewed_vip_products",
    "enabled_dark_mode",
    "logged_in",
    "logged_out"
)

/** Snake case: only lowercase letters, numbers, underscores. */
private fun isValidSnakeCase(s: String): Boolean =
    s.isNotEmpty() && s.matches(Regex("^[a-z0-9_]+$"))

/**
 * Content screen for demonstrating Braze Content Cards and Banners.
 * This page can be used to manually install content cards and banners for client demonstrations.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentScreen() {
    val context = LocalContext.current
    val settingsManager = remember { BrazeSettingsManager(context) }
    val profileManager = remember { UserProfileManager(context) }
    var notificationMessage by remember { mutableStateOf<String?>(null) }
    var isRefreshing by remember { mutableStateOf(false) }
    var showCustomEventDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) {
        BrazeLogManager.logScreenEntered("Content Page")
    }
    
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            scope.launch {
                BrazeContentManager.forceRefreshAllContent(context) {
                    isRefreshing = false
                    notificationMessage = "Content refreshed"
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        val brazeInstance = Braze.getInstance(context)
                        brazeInstance.logCustomEvent("enable_push")
                        BrazeLogManager.logCustomEvent("enable_push")
                        brazeInstance.requestImmediateDataFlush()
                        notificationMessage = "Custom Event Sent"
                    },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp),
                    contentPadding = ButtonDefaults.ButtonWithIconContentPadding
                ) {
                    Text(
                        text = "Enable Push",
                        style = MaterialTheme.typography.labelMedium,
                        textAlign = TextAlign.Center,
                        maxLines = 2
                    )
                }
                
                Button(
                    onClick = {
                        val brazeInstance = Braze.getInstance(context)
                        brazeInstance.logCustomEvent("push_notification")
                        BrazeLogManager.logCustomEvent("push_notification")
                        brazeInstance.requestImmediateDataFlush()
                        notificationMessage = "Custom Event Sent"
                    },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp),
                    contentPadding = ButtonDefaults.ButtonWithIconContentPadding
                ) {
                    Text(
                        text = "Send Push",
                        style = MaterialTheme.typography.labelMedium,
                        textAlign = TextAlign.Center,
                        maxLines = 2
                    )
                }
                
                Button(
                    onClick = { showCustomEventDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp),
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
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp)
            ) {
                Tile1ContentCard(
                    modifier = Modifier.fillMaxWidth()
                )
                
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
            
            ContentBanner(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            )
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp),
                contentAlignment = Alignment.Center
            ) {
            }
        }
        }
    }
    
    if (showCustomEventDialog) {
        CustomEventDialog(
            onDismiss = { showCustomEventDialog = false },
            onSent = {
                notificationMessage = "Custom Event Sent"
                showCustomEventDialog = false
            },
            profileManager = profileManager,
            context = context
        )
    }
    
    LaunchedEffect(notificationMessage) {
        if (notificationMessage != null) {
            delay(2000L)
            notificationMessage = null
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomEventDialog(
    onDismiss: () -> Unit,
    onSent: () -> Unit,
    profileManager: UserProfileManager,
    context: android.content.Context
) {
    var selectedOption by remember { mutableStateOf<String?>(null) }
    var newEventName by remember { mutableStateOf("") }
    var dropdownExpanded by remember { mutableStateOf(false) }
    val currentProfile = profileManager.getCurrentProfile()
    val storedEvents = currentProfile?.customEvents?.filter { it !in STANDARD_CUSTOM_EVENTS } ?: emptyList()
    val allOptions = listOf("new") + STANDARD_CUSTOM_EVENTS + storedEvents
    val displayOptions = listOf("New Custom Event") + STANDARD_CUSTOM_EVENTS + storedEvents
    val dropdownDisplayText = when {
        selectedOption == null -> ""
        selectedOption == "new" -> "New Custom Event"
        else -> selectedOption!!
    }
    val isNewEvent = selectedOption == "new"
    val newEventNameTrimmed = newEventName.trim()
    val newEventValid = newEventNameTrimmed.isEmpty() || isValidSnakeCase(newEventNameTrimmed)
    val canSend = when {
        selectedOption == null -> false
        isNewEvent -> isValidSnakeCase(newEventNameTrimmed)
        else -> true
    }
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                ExposedDropdownMenuBox(
                    expanded = dropdownExpanded,
                    onExpandedChange = { dropdownExpanded = !dropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = dropdownDisplayText,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        placeholder = { Text("Select Custom Event", color = Color.Gray) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) }
                    )
                    ExposedDropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false }
                    ) {
                        displayOptions.forEachIndexed { index, label ->
                            val value = allOptions[index]
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    selectedOption = value
                                    if (value != "new") newEventName = ""
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }
                if (isNewEvent) {
                    OutlinedTextField(
                        value = newEventName,
                        onValueChange = { newEventName = it },
                        label = { Text("Create new custom event") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        singleLine = true,
                        isError = !newEventValid && newEventName.isNotEmpty()
                    )
                    if (!newEventValid && newEventName.isNotEmpty()) {
                        Text(
                            text = "Only snake_case is allowed. Example: example_event",
                            color = Color.Red,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            if (!canSend) return@Button
                            val eventName = if (isNewEvent) newEventNameTrimmed else selectedOption!!
                            Braze.getInstance(context).logCustomEvent(eventName)
                            BrazeLogManager.logCustomEvent(eventName)
                            Braze.getInstance(context).requestImmediateDataFlush()
                            if (isNewEvent && eventName !in STANDARD_CUSTOM_EVENTS && currentProfile != null) {
                                val existing = currentProfile.customEvents
                                if (eventName !in existing) {
                                    val profile = currentProfile
                                    profile.customEvents = existing + eventName
                                    profileManager.saveProfile(profile)
                                }
                            }
                            onSent()
                        },
                        enabled = canSend
                    ) {
                        Text("Send")
                    }
                }
            }
        }
    }
}

/**
 * Content Card: Tile 1
 * 
 * Displays Braze Content Cards filtered by location = tile_1
 * - 2:1 banner layout with adaptive image positioning
 * - Logs impressions and clicks to Braze analytics
 * - Shows placeholder when no content is available
 */
@Composable
fun Tile1ContentCard(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme
    val scope = rememberCoroutineScope()
    
    val locationKey = "tile_1"
    val cachedCards = rememberCachedContentCards()
    
    var contentCard by remember(locationKey) { 
        mutableStateOf<Any?>(
            BrazeContentManager.getCachedContentCardByPositionId(locationKey)
        )
    }
    var hasCard by remember(locationKey) { 
        mutableStateOf(
            BrazeContentManager.getCachedContentCardByPositionId(locationKey) != null
        )
    }
    
    // Process cached cards when cache updates
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
            
            try {
                val getExtrasMethod = card.javaClass.getMethod("getExtras")
                val extras = getExtrasMethod.invoke(card) as? Map<*, *>
                
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
                    
                    if (locationValue == null) {
                        locationValue = extras["location"] ?: extras["Location"] ?: extras["LOCATION"]
                    }
                    if (cardIdValue == null) {
                        cardIdValue = extras["card_id"] ?: extras["cardId"] ?: extras["Card_Id"]
                    }
                    
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
                
                try {
                    val getExtrasMethod = card.javaClass.getMethod("getExtras")
                    val extras = getExtrasMethod.invoke(card) as? Map<*, *>
                    
                    if (extras != null) {
                        var locationValue: Any? = null
                        var cardIdValue: Any? = null
                        
                        for ((key, value) in extras) {
                            val keyStr = key?.toString()?.lowercase()
                            if (keyStr == "location") {
                                locationValue = value
                            }
                            if (keyStr == "card_id" || keyStr == "cardid") {
                                cardIdValue = value
                            }
                        }
                        
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
            
        onDispose {
            BrazeUserSync.unsubscribeFromContentCardsUpdates(context, subscription)
        }
    }
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
    
    var imageAspectRatio by remember(cardData?.imageUrl) { mutableStateOf<Float?>(null) }
    var useRowLayout by remember(imageAspectRatio) { 
        mutableStateOf(imageAspectRatio != null && imageAspectRatio!! <= 1.3f)
    }
    
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
    
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(2f)
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = hasCard && cardData?.cardUrl != null) {
                if (contentCard != null) {
                    try {
                        val logClickMethod = contentCard!!.javaClass.getMethod("logClick")
                        logClickMethod.invoke(contentCard)
                        BrazeLogManager.logClick("Content Card", "tile_1")
                    } catch (e: Exception) {
                    }
                }
                val cardUrl = cardData?.cardUrl as? String
                if (cardUrl != null) {
                    if (cardUrl.startsWith("demostore://")) {
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
            
            if (hasCard && cardData != null) {
                if (useRowLayout && cardData.imageUrl != null) {
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
 * Content Card: Tile 2
 * 
 * Displays Braze Content Cards filtered by location = tile_2
 * - 1:1 square layout
 * - Logs impressions and clicks to Braze analytics
 * - Shows placeholder when no content is available
 */
@Composable
fun Tile2ContentCard(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme
    val scope = rememberCoroutineScope()
    
    val locationKey = "tile_2"
    val cachedCards = rememberCachedContentCards()
    
    var contentCard by remember(locationKey) { 
        mutableStateOf<Any?>(
            BrazeContentManager.getCachedContentCardByPositionId(locationKey)
        )
    }
    var hasCard by remember(locationKey) { 
        mutableStateOf(
            BrazeContentManager.getCachedContentCardByPositionId(locationKey) != null
        )
    }
    
    // Process cached cards when cache updates
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
            
            try {
                val getExtrasMethod = card.javaClass.getMethod("getExtras")
                val extras = getExtrasMethod.invoke(card) as? Map<*, *>
                
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
                
                try {
                    val getExtrasMethod = card.javaClass.getMethod("getExtras")
                    val extras = getExtrasMethod.invoke(card) as? Map<*, *>
                    
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
        
        onDispose {
            BrazeUserSync.unsubscribeFromContentCardsUpdates(context, subscription)
        }
    }
    
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
    
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = hasCard && cardData?.cardUrl != null) {
                if (contentCard != null) {
                    try {
                        val logClickMethod = contentCard!!.javaClass.getMethod("logClick")
                        logClickMethod.invoke(contentCard)
                        BrazeLogManager.logClick("Content Card", "tile_2")
                    } catch (e: Exception) {
                    }
                }
                val cardUrl = cardData?.cardUrl as? String
                if (cardUrl != null) {
                    if (cardUrl.startsWith("demostore://")) {
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
            
            if (hasCard && cardData != null) {
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
 * Banner: Content Banner
 * 
 * Displays Braze Banner with placement ID: content_banner
 * - 2:1 banner layout, non-collapsing container
 * - Shows placeholder when no banner is available
 */
@Composable
fun ContentBanner(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme
    val placementId = "content_banner"
    
    val cachedBanner = rememberCachedBanner(placementId)
    
    var banner by remember(placementId) { 
        mutableStateOf<Any?>(cachedBanner) 
    }
    var shouldRender by remember(placementId) { 
        mutableStateOf(
            if (cachedBanner != null) {
                try {
                    val isControlMethod = cachedBanner.javaClass.getMethod("isControl")
                    val isControl = isControlMethod.invoke(cachedBanner) as? Boolean ?: false
                    !isControl
                } catch (e: Exception) {
                    true // If isControl method doesn't exist, assume we should render
                }
            } else {
                false
            }
        )
    }
    
    // Update state only when cache changes (no direct fetch)
    LaunchedEffect(cachedBanner) {
        banner = cachedBanner
        
        // Check if banner exists and is not a control variant
        if (cachedBanner != null) {
            try {
                val isControlMethod = cachedBanner.javaClass.getMethod("isControl")
                val isControl = isControlMethod.invoke(cachedBanner) as? Boolean ?: false
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
                            
                            webViewClient = object : WebViewClient() {
                                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                                    // Check if the URL is a demostore deep link
                                    if (url != null && url.startsWith("demostore://")) {
                                        try {
                                            val uri = Uri.parse(url)
                                            val intent = Intent(Intent.ACTION_VIEW, uri)
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
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
                        
                        fun handleDeepLink(url: String) {
                            try {
                                val uri = Uri.parse(url)
                                val intent = Intent(Intent.ACTION_VIEW, uri)
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                intent.setPackage(context.packageName)
                                
                                if (intent.resolveActivity(context.packageManager) != null) {
                                    context.startActivity(intent)
                                }
                            } catch (e: Exception) {
                            }
                        }
                        
                        class DeepLinkHandler(private val handler: (String) -> Unit) {
                            @JavascriptInterface
                            fun handleDeepLink(url: String) {
                                android.os.Handler(android.os.Looper.getMainLooper()).post {
                                    handler(url)
                                }
                            }
                        }
                        
                        webView.addJavascriptInterface(DeepLinkHandler(::handleDeepLink), "AndroidDeepLinkHandler")
                        
                        val customWebViewClient = object : WebViewClient() {
                            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                                if (url != null && url.startsWith("demostore://")) {
                                    handleDeepLink(url)
                                    return true
                                }
                                return false
                            }
                            
                            @android.annotation.SuppressLint("NewApi")
                            override fun shouldOverrideUrlLoading(view: WebView?, request: android.webkit.WebResourceRequest?): Boolean {
                                val url = request?.url?.toString()
                                if (url != null && url.startsWith("demostore://")) {
                                    handleDeepLink(url)
                                    return true
                                }
                                return false
                            }
                            
                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                
                                if (url == null || url == "about:blank" || url.startsWith("data:")) {
                                    return
                                }
                                
                                view?.postDelayed({
                                    try {
                                        val jsCode = """
                                            (function() {
                                                function handleDeepLink(url) {
                                                    if (url && url.startsWith('demostore://')) {
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
                                                    if (url && url.startsWith('demostore://')) {
                                                        if (handleDeepLink(url)) return null;
                                                    }
                                                    return originalOpen.apply(window, arguments);
                                                };
                                                
                                                window.location.assign = function(url) {
                                                    if (url && url.startsWith('demostore://')) {
                                                        if (handleDeepLink(url)) return;
                                                    }
                                                    return window._originalAssign?.apply(window.location, arguments);
                                                };
                                                
                                                window.location.replace = function(url) {
                                                    if (url && url.startsWith('demostore://')) {
                                                        if (handleDeepLink(url)) return;
                                                    }
                                                    return window._originalReplace?.apply(window.location, arguments);
                                                };
                                                
                                                function setupClickInterceptors() {
                                                    document.addEventListener('click', function(e) {
                                                        var target = e.target;
                                                        var link = null;
                                                        
                                                        // Check if clicked element is an image or inside a link
                                                        while (target && target !== document.body) {
                                                            if (target.tagName === 'A' && target.href) {
                                                                link = target;
                                                                break;
                                                            }
                                                            // Also check if image is inside a clickable element
                                                            if (target.tagName === 'IMG') {
                                                                var parent = target.parentElement;
                                                                while (parent && parent !== document.body) {
                                                                    if (parent.tagName === 'A' || parent.onclick || parent.getAttribute('onclick')) {
                                                                        link = parent;
                                                                        break;
                                                                    }
                                                                    parent = parent.parentElement;
                                                                }
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
                                                            
                                                            if (onclickAttr && onclickAttr.includes('demostore://')) {
                                                                var match = onclickAttr.match(/demostore:\/\/[^"'\s)]+/);
                                                                if (match) deepLink = match[0];
                                                            } else if (dataHref && dataHref.includes('demostore://')) {
                                                                deepLink = dataHref;
                                                            } else if (href && href.includes('demostore://')) {
                                                                deepLink = href;
                                                            } else {
                                                                // For images and other elements, check parent elements more thoroughly
                                                                var parent = clicked.parentElement;
                                                                var depth = 0;
                                                                while (parent && depth < 10) {
                                                                    var parentOnclick = parent.getAttribute('onclick');
                                                                    var parentDataHref = parent.getAttribute('data-href');
                                                                    var parentHref = parent.href;
                                                                    
                                                                    if (parentOnclick && parentOnclick.includes('demostore://')) {
                                                                        var match = parentOnclick.match(/demostore:\/\/[^"'\s)]+/);
                                                                        if (match) {
                                                                            deepLink = match[0];
                                                                            break;
                                                                        }
                                                                    } else if (parentDataHref && parentDataHref.includes('demostore://')) {
                                                                        deepLink = parentDataHref;
                                                                        break;
                                                                    } else if (parentHref && parentHref.includes('demostore://')) {
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
                                                        var regex = /demostore:\/\/[^"'\s<>)]+/g;
                                                        var matches = htmlContent.match(regex);
                                                        if (matches && matches.length > 0) {
                                                            window._bannerDeepLink = matches[0];
                                                        }
                                                        
                                                        // Find all clickable elements including images with onclick
                                                        var clickableElements = document.querySelectorAll('[onclick*="demostore://"], button, [data-href*="demostore://"], [onclick], img[onclick], img[onclick*="demostore://"]');
                                                        clickableElements.forEach(function(el) {
                                                            el.addEventListener('click', function(e) {
                                                                var storedDeepLink = window._bannerDeepLink;
                                                                if (!storedDeepLink) {
                                                                    storedDeepLink = htmlContent.match(regex)?.[0];
                                                                    window._bannerDeepLink = storedDeepLink;
                                                                }
                                                                
                                                                // Check element's own onclick first
                                                                var elementOnclick = el.getAttribute('onclick');
                                                                var elementDataHref = el.getAttribute('data-href');
                                                                var elementHref = el.href;
                                                                var foundDeepLink = null;
                                                                
                                                                if (elementOnclick && elementOnclick.includes('demostore://')) {
                                                                    var match = elementOnclick.match(/demostore:\/\/[^"'\s)]+/);
                                                                    if (match) foundDeepLink = match[0];
                                                                } else if (elementDataHref && elementDataHref.includes('demostore://')) {
                                                                    foundDeepLink = elementDataHref;
                                                                } else if (elementHref && elementHref.includes('demostore://')) {
                                                                    foundDeepLink = elementHref;
                                                                } else if (storedDeepLink) {
                                                                    foundDeepLink = storedDeepLink;
                                                                }
                                                                
                                                                if (foundDeepLink) {
                                                                    if (handleDeepLink(foundDeepLink)) {
                                                                        e.preventDefault();
                                                                        e.stopPropagation();
                                                                        e.stopImmediatePropagation();
                                                                        return false;
                                                                    }
                                                                }
                                                            }, true);
                                                        });
                                                        
                                                        // Also attach to all images to check for parent clickable elements
                                                        var images = document.querySelectorAll('img');
                                                        images.forEach(function(img) {
                                                            img.addEventListener('click', function(e) {
                                                                var parent = img.parentElement;
                                                                var depth = 0;
                                                                while (parent && depth < 10) {
                                                                    var parentOnclick = parent.getAttribute('onclick');
                                                                    var parentDataHref = parent.getAttribute('data-href');
                                                                    var parentHref = parent.href;
                                                                    
                                                                    if (parentOnclick && parentOnclick.includes('demostore://')) {
                                                                        var match = parentOnclick.match(/demostore:\/\/[^"'\s)]+/);
                                                                        if (match) {
                                                                            if (handleDeepLink(match[0])) {
                                                                                e.preventDefault();
                                                                                e.stopPropagation();
                                                                                e.stopImmediatePropagation();
                                                                                return false;
                                                                            }
                                                                        }
                                                                    } else if (parentDataHref && parentDataHref.includes('demostore://')) {
                                                                        if (handleDeepLink(parentDataHref)) {
                                                                            e.preventDefault();
                                                                            e.stopPropagation();
                                                                            e.stopImmediatePropagation();
                                                                            return false;
                                                                        }
                                                                    } else if (parentHref && parentHref.includes('demostore://')) {
                                                                        if (handleDeepLink(parentHref)) {
                                                                            e.preventDefault();
                                                                            e.stopPropagation();
                                                                            e.stopImmediatePropagation();
                                                                            return false;
                                                                        }
                                                                    }
                                                                    
                                                                    parent = parent.parentElement;
                                                                    depth++;
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
                                    }
                                }, 100)
                            }
                        }
                        
                        webView.webViewClient = customWebViewClient
                        webView.setBackgroundColor(android.graphics.Color.TRANSPARENT)
                        
                        try {
                            val brazeInstance = Braze.getInstance(context)
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
                                    val preloadJs = """
                                        (function() {
                                            function handleDeepLink(url) {
                                                if (url && url.startsWith('demostore://')) {
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
                                                if (url && url.startsWith('demostore://')) {
                                                    if (handleDeepLink(url)) return null;
                                                }
                                                return window._originalOpen.apply(window, arguments);
                                            };
                                            
                                            window.location.assign = function(url) {
                                                if (url && url.startsWith('demostore://')) {
                                                    if (handleDeepLink(url)) return;
                                                }
                                                return window._originalAssign?.apply(window.location, arguments);
                                            };
                                            
                                            window.location.replace = function(url) {
                                                if (url && url.startsWith('demostore://')) {
                                                    if (handleDeepLink(url)) return;
                                                }
                                                return window._originalReplace?.apply(window.location, arguments);
                                            };
                                            
                                            document.addEventListener('DOMContentLoaded', function() {
                                                function findDeepLinkInDocument() {
                                                    var htmlContent = document.documentElement.innerHTML || '';
                                                    var regex = /demostore:\/\/[^"'\s<>)]+/g;
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
                                    
                                    webView.webViewClient = customWebViewClient
                                    webView.loadDataWithBaseURL(null, htmlWithInterceptors, "text/html", "UTF-8", null)
                                    
                                    webView.postDelayed({
                                        try {
                                            val jsCode = """
                                                (function() {
                                                    function handleDeepLink(url) {
                                                        if (url && url.startsWith('demostore://')) {
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
                                                                
                                                                if (onclickAttr && onclickAttr.includes('demostore://')) {
                                                                    var match = onclickAttr.match(/demostore:\/\/[^"'\s)]+/);
                                                                    if (match) deepLink = match[0];
                                                                } else if (dataHref && dataHref.includes('demostore://')) {
                                                                    deepLink = dataHref;
                                                                } else if (href && href.includes('demostore://')) {
                                                                    deepLink = href;
                                                                } else {
                                                                    var parent = clicked.parentElement;
                                                                    var depth = 0;
                                                                    while (parent && depth < 5) {
                                                                        var parentOnclick = parent.getAttribute('onclick');
                                                                        var parentDataHref = parent.getAttribute('data-href');
                                                                        var parentHref = parent.href;
                                                                        
                                                                        if (parentOnclick && parentOnclick.includes('demostore://')) {
                                                                            var match = parentOnclick.match(/demostore:\/\/[^"'\s)]+/);
                                                                            if (match) {
                                                                                deepLink = match[0];
                                                                                break;
                                                                            }
                                                                        } else if (parentDataHref && parentDataHref.includes('demostore://')) {
                                                                            deepLink = parentDataHref;
                                                                            break;
                                                                        } else if (parentHref && parentHref.includes('demostore://')) {
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
                                                            var regex = /demostore:\/\/[^"'\s<>)]+/g;
                                                            var matches = htmlContent.match(regex);
                                                            if (matches && matches.length > 0) {
                                                                window._bannerDeepLink = matches[0];
                                                            }
                                                            
                                                            var clickableElements = document.querySelectorAll('[onclick*="demostore://"], button, [data-href*="demostore://"], [onclick]');
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
                                        }
                                    }, 500)
                                }
                            } catch (e2: Exception) {
                            }
                        }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxSize()
            )
            } else {
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
 * Banner: Tile Banner
 * 
 * Displays Braze Banner with placement ID: tile_banner
 * - 1:1 square layout, non-collapsing container
 * - Shows placeholder when no banner is available
 */
@Composable
fun TileBanner(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme
    val placementId = "tile_banner"
    
    val cachedBanner = rememberCachedBanner(placementId)
    
    var banner by remember(placementId) { 
        mutableStateOf<Any?>(cachedBanner) 
    }
    var shouldRender by remember(placementId) { 
        mutableStateOf(
            if (cachedBanner != null) {
                try {
                    val isControlMethod = cachedBanner.javaClass.getMethod("isControl")
                    val isControl = isControlMethod.invoke(cachedBanner) as? Boolean ?: false
                    !isControl
                } catch (e: Exception) {
                    true // If isControl method doesn't exist, assume we should render
                }
            } else {
                false
            }
        )
    }
    var bannerClickUrl by remember(placementId) { 
        mutableStateOf<String?>(
            if (cachedBanner != null) {
                try {
                    val getClickUrlMethod = cachedBanner.javaClass.getMethod("getClickUrl")
                    getClickUrlMethod.invoke(cachedBanner) as? String
                } catch (e: Exception) {
                    try {
                        val getUrlMethod = cachedBanner.javaClass.getMethod("getUrl")
                        getUrlMethod.invoke(cachedBanner) as? String
                    } catch (e2: Exception) {
                        null
                    }
                }
            } else {
                null
            }
        )
    }
    
    // Update state only when cache changes (no direct fetch)
    LaunchedEffect(cachedBanner) {
        banner = cachedBanner
        
        if (cachedBanner != null) {
            try {
                val isControlMethod = cachedBanner.javaClass.getMethod("isControl")
                val isControl = isControlMethod.invoke(cachedBanner) as? Boolean ?: false
                shouldRender = !isControl
                
                try {
                    val getClickUrlMethod = cachedBanner.javaClass.getMethod("getClickUrl")
                    val clickUrl = getClickUrlMethod.invoke(cachedBanner) as? String
                    bannerClickUrl = clickUrl
                } catch (e: Exception) {
                    try {
                        val getUrlMethod = cachedBanner.javaClass.getMethod("getUrl")
                        val url = getUrlMethod.invoke(cachedBanner) as? String
                        bannerClickUrl = url
                    } catch (e2: Exception) {
                        bannerClickUrl = null
                    }
                }
            } catch (e: Exception) {
                shouldRender = true
            }
        } else {
            shouldRender = false
            bannerClickUrl = null
        }
    }
    
    fun handleBannerClick() {
        if (bannerClickUrl != null && banner != null) {
            try {
                val logClickMethod = banner!!.javaClass.getMethod("logClick")
                logClickMethod.invoke(banner)
                BrazeLogManager.logClick("Banner", placementId)
            } catch (e: Exception) {
            }
            
            if (bannerClickUrl!!.startsWith("demostore://")) {
                try {
                    val uri = Uri.parse(bannerClickUrl!!)
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    intent.setPackage(context.packageName)
                    if (intent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(intent)
                    }
                } catch (e: Exception) {
                }
            }
        }
    }
    
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
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
                                    // Check if the URL is a demostore deep link
                                    if (url != null && url.startsWith("demostore://")) {
                                        try {
                                            // Parse the URI (handles URL encoding automatically)
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
                                
                                @android.annotation.SuppressLint("NewApi")
                                override fun shouldOverrideUrlLoading(view: android.webkit.WebView?, request: android.webkit.WebResourceRequest?): Boolean {
                                    val url = request?.url?.toString()
                                    if (url != null && url.startsWith("demostore://")) {
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
                                if (url != null && url.startsWith("demostore://")) {
                                    handleDeepLink(url)
                                    return true
                                }
                                return false
                            }
                            
                            @android.annotation.SuppressLint("NewApi")
                            override fun shouldOverrideUrlLoading(view: android.webkit.WebView?, request: android.webkit.WebResourceRequest?): Boolean {
                                val url = request?.url?.toString()
                                if (url != null && url.startsWith("demostore://")) {
                                    handleDeepLink(url)
                                    return true
                                }
                                return false
                            }
                            
                            override fun onPageFinished(view: android.webkit.WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                
                                if (url == null || url == "about:blank" || url.startsWith("data:")) {
                                    return
                                }
                                
                                view?.postDelayed({
                                    try {
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
                                                    if (url && url.startsWith('demostore://')) {
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
                                                    if (url && url.startsWith('demostore://')) {
                                                        if (handleDeepLink(url)) return null;
                                                    }
                                                    return originalOpen.apply(window, arguments);
                                                };
                                                
                                                window.location.assign = function(url) {
                                                    if (url && url.startsWith('demostore://')) {
                                                        if (handleDeepLink(url)) return;
                                                    }
                                                    return window._originalAssign?.apply(window.location, arguments);
                                                };
                                                
                                                window.location.replace = function(url) {
                                                    if (url && url.startsWith('demostore://')) {
                                                        if (handleDeepLink(url)) return;
                                                    }
                                                    return window._originalReplace?.apply(window.location, arguments);
                                                };
                                                
                                                function setupClickInterceptors() {
                                                    document.addEventListener('click', function(e) {
                                                        var target = e.target;
                                                        var link = null;
                                                        
                                                        // Check if clicked element is an image or inside a link
                                                        while (target && target !== document.body) {
                                                            if (target.tagName === 'A' && target.href) {
                                                                link = target;
                                                                break;
                                                            }
                                                            // Also check if image is inside a clickable element
                                                            if (target.tagName === 'IMG') {
                                                                var parent = target.parentElement;
                                                                while (parent && parent !== document.body) {
                                                                    if (parent.tagName === 'A' || parent.onclick || parent.getAttribute('onclick')) {
                                                                        link = parent;
                                                                        break;
                                                                    }
                                                                    parent = parent.parentElement;
                                                                }
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
                                                            
                                                            if (onclickAttr && onclickAttr.includes('demostore://')) {
                                                                var match = onclickAttr.match(/demostore:\/\/[^"'\s)]+/);
                                                                if (match) deepLink = match[0];
                                                            } else if (dataHref && dataHref.includes('demostore://')) {
                                                                deepLink = dataHref;
                                                            } else if (href && href.includes('demostore://')) {
                                                                deepLink = href;
                                                            } else {
                                                                // For images and other elements, check parent elements more thoroughly
                                                                var parent = clicked.parentElement;
                                                                var depth = 0;
                                                                while (parent && depth < 10) {
                                                                    var parentOnclick = parent.getAttribute('onclick');
                                                                    var parentDataHref = parent.getAttribute('data-href');
                                                                    var parentHref = parent.href;
                                                                    
                                                                    if (parentOnclick && parentOnclick.includes('demostore://')) {
                                                                        var match = parentOnclick.match(/demostore:\/\/[^"'\s)]+/);
                                                                        if (match) {
                                                                            deepLink = match[0];
                                                                            break;
                                                                        }
                                                                    } else if (parentDataHref && parentDataHref.includes('demostore://')) {
                                                                        deepLink = parentDataHref;
                                                                        break;
                                                                    } else if (parentHref && parentHref.includes('demostore://')) {
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
                                                        var regex = /demostore:\/\/[^"'\s<>)]+/g;
                                                        var matches = htmlContent.match(regex);
                                                        if (matches && matches.length > 0) {
                                                            window._bannerDeepLink = matches[0];
                                                        }
                                                        
                                                        // Find all clickable elements including images with onclick
                                                        var clickableElements = document.querySelectorAll('[onclick*="demostore://"], button, [data-href*="demostore://"], [onclick], img[onclick], img[onclick*="demostore://"]');
                                                        clickableElements.forEach(function(el) {
                                                            el.addEventListener('click', function(e) {
                                                                var storedDeepLink = window._bannerDeepLink;
                                                                if (!storedDeepLink) {
                                                                    storedDeepLink = htmlContent.match(regex)?.[0];
                                                                    window._bannerDeepLink = storedDeepLink;
                                                                }
                                                                
                                                                // Check element's own onclick first
                                                                var elementOnclick = el.getAttribute('onclick');
                                                                var elementDataHref = el.getAttribute('data-href');
                                                                var elementHref = el.href;
                                                                var foundDeepLink = null;
                                                                
                                                                if (elementOnclick && elementOnclick.includes('demostore://')) {
                                                                    var match = elementOnclick.match(/demostore:\/\/[^"'\s)]+/);
                                                                    if (match) foundDeepLink = match[0];
                                                                } else if (elementDataHref && elementDataHref.includes('demostore://')) {
                                                                    foundDeepLink = elementDataHref;
                                                                } else if (elementHref && elementHref.includes('demostore://')) {
                                                                    foundDeepLink = elementHref;
                                                                } else if (storedDeepLink) {
                                                                    foundDeepLink = storedDeepLink;
                                                                }
                                                                
                                                                if (foundDeepLink) {
                                                                    if (handleDeepLink(foundDeepLink)) {
                                                                        e.preventDefault();
                                                                        e.stopPropagation();
                                                                        e.stopImmediatePropagation();
                                                                        return false;
                                                                    }
                                                                }
                                                            }, true);
                                                        });
                                                        
                                                        // Also attach to all images to check for parent clickable elements
                                                        var images = document.querySelectorAll('img');
                                                        images.forEach(function(img) {
                                                            img.addEventListener('click', function(e) {
                                                                var parent = img.parentElement;
                                                                var depth = 0;
                                                                while (parent && depth < 10) {
                                                                    var parentOnclick = parent.getAttribute('onclick');
                                                                    var parentDataHref = parent.getAttribute('data-href');
                                                                    var parentHref = parent.href;
                                                                    
                                                                    if (parentOnclick && parentOnclick.includes('demostore://')) {
                                                                        var match = parentOnclick.match(/demostore:\/\/[^"'\s)]+/);
                                                                        if (match) {
                                                                            if (handleDeepLink(match[0])) {
                                                                                e.preventDefault();
                                                                                e.stopPropagation();
                                                                                e.stopImmediatePropagation();
                                                                                return false;
                                                                            }
                                                                        }
                                                                    } else if (parentDataHref && parentDataHref.includes('demostore://')) {
                                                                        if (handleDeepLink(parentDataHref)) {
                                                                            e.preventDefault();
                                                                            e.stopPropagation();
                                                                            e.stopImmediatePropagation();
                                                                            return false;
                                                                        }
                                                                    } else if (parentHref && parentHref.includes('demostore://')) {
                                                                        if (handleDeepLink(parentHref)) {
                                                                            e.preventDefault();
                                                                            e.stopPropagation();
                                                                            e.stopImmediatePropagation();
                                                                            return false;
                                                                        }
                                                                    }
                                                                    
                                                                    parent = parent.parentElement;
                                                                    depth++;
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
                                    }
                                }, 100)
                            }
                        }
                        
                        webView.webViewClient = customWebViewClient
                        webView.setBackgroundColor(android.graphics.Color.TRANSPARENT)
                        
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
                                                    if (url && url.startsWith('demostore://')) {
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
                                                    if (url && url.startsWith('demostore://')) {
                                                        if (handleDeepLink(url)) return null;
                                                    }
                                                    return window._originalOpen.apply(window, arguments);
                                                };
                                                
                                                window.location.assign = function(url) {
                                                    if (url && url.startsWith('demostore://')) {
                                                        if (handleDeepLink(url)) return;
                                                    }
                                                    return window._originalAssign?.apply(window.location, arguments);
                                                };
                                                
                                                window.location.replace = function(url) {
                                                    if (url && url.startsWith('demostore://')) {
                                                        if (handleDeepLink(url)) return;
                                                    }
                                                    return window._originalReplace?.apply(window.location, arguments);
                                                };
                                                
                                                document.addEventListener('DOMContentLoaded', function() {
                                                    function findDeepLinkInDocument() {
                                                        var htmlContent = document.documentElement.innerHTML || '';
                                                        var regex = /demostore:\/\/[^"'\s<>)]+/g;
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
                                                        
                                                        if (!isClickableElement && bannerClickUrl) {
                                                            if (handleDeepLink(bannerClickUrl)) {
                                                                e.preventDefault();
                                                                e.stopPropagation();
                                                                e.stopImmediatePropagation();
                                                                return false;
                                                            }
                                                        }
                                                        
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
                                        
                                        webView.webViewClient = customWebViewClient
                                        webView.loadDataWithBaseURL(null, htmlWithInterceptors, "text/html", "UTF-8", null)
                                        
                                        webView.postDelayed({
                                            try {
                                                val jsCode = """
                                                    (function() {
                                                        function handleDeepLink(url) {
                                                            if (url && url.startsWith('demostore://')) {
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
                                                                    
                                                                    if (onclickAttr && onclickAttr.includes('demostore://')) {
                                                                        var match = onclickAttr.match(/demostore:\/\/[^"'\s)]+/);
                                                                        if (match) deepLink = match[0];
                                                                    } else if (dataHref && dataHref.includes('demostore://')) {
                                                                        deepLink = dataHref;
                                                                    } else if (href && href.includes('demostore://')) {
                                                                        deepLink = href;
                                                                    } else {
                                                                        var parent = clicked.parentElement;
                                                                        var depth = 0;
                                                                        while (parent && depth < 5) {
                                                                            var parentOnclick = parent.getAttribute('onclick');
                                                                            var parentDataHref = parent.getAttribute('data-href');
                                                                            var parentHref = parent.href;
                                                                            
                                                                            if (parentOnclick && parentOnclick.includes('demostore://')) {
                                                                                var match = parentOnclick.match(/demostore:\/\/[^"'\s)]+/);
                                                                                if (match) {
                                                                                    deepLink = match[0];
                                                                                    break;
                                                                                }
                                                                            } else if (parentDataHref && parentDataHref.includes('demostore://')) {
                                                                                deepLink = parentDataHref;
                                                                                break;
                                                                            } else if (parentHref && parentHref.includes('demostore://')) {
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
                                                                var regex = /demostore:\/\/[^"'\s<>)]+/g;
                                                                var matches = htmlContent.match(regex);
                                                                if (matches && matches.length > 0) {
                                                                    window._bannerDeepLink = matches[0];
                                                                }
                                                                
                                                                var clickableElements = document.querySelectorAll('[onclick*="demostore://"], button, [data-href*="demostore://"], [onclick]');
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
