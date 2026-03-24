package com.example.phil_android_store.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.braze.Braze
import com.example.phil_android_store.data.BrazeSettingsManager

/**
 * Settings screen with Braze configuration options
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val settingsManager = remember { BrazeSettingsManager(context) }
    
    // Get SDK version - try multiple methods
    val sdkVersion = remember {
        try {
            // Try BuildConfig first (most reliable)
            try {
                val buildConfigClass = Class.forName("com.braze.BuildConfig")
                val versionField = buildConfigClass.getField("VERSION_NAME")
                versionField.get(null) as? String ?: "40.1.1" // Fallback to known version
            } catch (e: Exception) {
                // Braze SDK: Fallback: try Braze instance method
                try {
                    val brazeInstance = Braze.getInstance(context)
                    val versionMethod = brazeInstance.javaClass.getMethod("getSdkVersion")
                    versionMethod.invoke(brazeInstance) as? String ?: "40.1.1"
                } catch (e2: Exception) {
                    // Final fallback to known version from build.gradle
                    "40.1.1"
                }
            }
        } catch (e: Exception) {
            "40.1.1" // Fallback to known version
        }
    }
    
    // API Key state
    var apiKey by remember { mutableStateOf(settingsManager.getApiKey()) }
    var isApiKeyEditable by remember { mutableStateOf(false) }
    var apiKeyEditValue by remember { mutableStateOf(apiKey) }
    
    // Endpoint state
    var endpoint by remember { mutableStateOf(settingsManager.getEndpoint()) }
    var isEndpointEditable by remember { mutableStateOf(false) }
    var endpointEditValue by remember { mutableStateOf(endpoint) }
    
    // Push Sender ID state
    var pushSenderId by remember { mutableStateOf(settingsManager.getPushSenderId()) }
    var isPushSenderIdEditable by remember { mutableStateOf(false) }
    var pushSenderIdEditValue by remember { mutableStateOf(pushSenderId) }
    
    // Push Token dialog state
    var showPushTokenDialog by remember { mutableStateOf(false) }
    var pushToken by remember { mutableStateOf<String?>(null) }
    var isLoadingToken by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    // Available deep links
    val deepLinks = listOf(
        "Store" to "demostore://store",
        "Cart" to "demostore://cart",
        "Profile" to "demostore://profile",
        "VIP Products" to "demostore://vip",
        "Purchase History" to "demostore://purchase-history",
        "Purchase History (Alt)" to "demostore://history"
    )
    
    // Banner placement IDs
    val bannerPlacements = listOf(
        "Store Page Banner" to "store_page_banner",
        "Cart Banner" to "cart_banner",
        "Content Banner" to "content_banner",
        "Tile Banner" to "tile_banner"
    )
    
    // Content Card Key-Value Pairs
    val contentCardKVPs = listOf(
        "Tile 1" to "location = tile_1",
        "Tile 2" to "location = tile_2"
    )
    
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Status bar background to match banner color
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(statusBarPadding.calculateTopPadding()),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {}
        
        // Purple banner with "Demo Store" and X button (matches TopBanner style)
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                // Centered "Demo Store" text
                Text(
                    text = "Demo Store",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                // Close button aligned to the right
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
        
        // Scrollable content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // SDK Version
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "SDK Version",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = sdkVersion,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // API Key
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Braze API Key",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(
                            onClick = {
                                if (isApiKeyEditable) {
                                    // Save
                                    settingsManager.setApiKey(apiKeyEditValue)
                                    apiKey = apiKeyEditValue
                                }
                                isApiKeyEditable = !isApiKeyEditable
                                if (!isApiKeyEditable) {
                                    apiKeyEditValue = apiKey
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = if (isApiKeyEditable) "Save" else "Edit",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    OutlinedTextField(
                        value = if (isApiKeyEditable) apiKeyEditValue else apiKey,
                        onValueChange = { if (isApiKeyEditable) apiKeyEditValue = it },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = isApiKeyEditable,
                        readOnly = !isApiKeyEditable
                    )
                }
            }
            
            // SDK Endpoint
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Braze SDK Endpoint",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(
                            onClick = {
                                if (isEndpointEditable) {
                                    // Save
                                    settingsManager.setEndpoint(endpointEditValue)
                                    endpoint = endpointEditValue
                                }
                                isEndpointEditable = !isEndpointEditable
                                if (!isEndpointEditable) {
                                    endpointEditValue = endpoint
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = if (isEndpointEditable) "Save" else "Edit",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    OutlinedTextField(
                        value = if (isEndpointEditable) endpointEditValue else endpoint,
                        onValueChange = { if (isEndpointEditable) endpointEditValue = it },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = isEndpointEditable,
                        readOnly = !isEndpointEditable
                    )
                }
            }
            
            // Push Sender ID
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Push Sender ID",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Button(
                            onClick = {
                                isLoadingToken = true
                                showPushTokenDialog = true
                                pushToken = null
                                // Get FCM token
                                scope.launch {
                                    try {
                                        val token = FirebaseMessaging.getInstance().token.await()
                                        pushToken = token
                                    } catch (e: Exception) {
                                        pushToken = "Error: ${e.message}"
                                    } finally {
                                        isLoadingToken = false
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                            )
                        ) {
                            Text(
                                text = "Show Push Token",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        IconButton(
                            onClick = {
                                if (isPushSenderIdEditable) {
                                    // Save
                                    settingsManager.setPushSenderId(pushSenderIdEditValue)
                                    pushSenderId = pushSenderIdEditValue
                                }
                                isPushSenderIdEditable = !isPushSenderIdEditable
                                if (!isPushSenderIdEditable) {
                                    pushSenderIdEditValue = pushSenderId
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = if (isPushSenderIdEditable) "Save" else "Edit",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    OutlinedTextField(
                        value = if (isPushSenderIdEditable) pushSenderIdEditValue else pushSenderId,
                        onValueChange = { if (isPushSenderIdEditable) pushSenderIdEditValue = it },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = isPushSenderIdEditable,
                        readOnly = !isPushSenderIdEditable
                    )
                }
            }
            
            // Info Section - Banner Placement IDs and Deep Links
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Header with "Info" and "click to copy" hint
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Info",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "click to copy",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    // Banner Placement IDs
                    Text(
                        text = "Banner Placement IDs",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    bannerPlacements.forEach { (name, id) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = name,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = id,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.clickable {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Placement ID", id)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Placement ID copied to clipboard", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                    
                    // Content Card Key-Value Pairs
                    Text(
                        text = "Content Card Key-Value Pairs",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    
                    contentCardKVPs.forEach { (name, kvp) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = name,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = kvp,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.clickable {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Content Card KVP", kvp)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Content Card KVP copied to clipboard", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                    
                    // Deep Links
                    Text(
                        text = "Deeplinks",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    
                    deepLinks.forEach { (name, link) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = name,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = link,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.clickable {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Deep Link", link)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Deep link copied to clipboard", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
            }
        }
        
        // Push Token Dialog
        if (showPushTokenDialog) {
            AlertDialog(
                onDismissRequest = { showPushTokenDialog = false },
                title = {
                    Text("Push Token")
                },
                text = {
                    if (isLoadingToken) {
                        Text("Loading push token...")
                    } else {
                        Column {
                            Text(
                                text = pushToken ?: "No token available",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            if (pushToken != null && !pushToken!!.startsWith("Error")) {
                                Spacer(modifier = Modifier.height(8.dp))
                                TextButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("Push Token", pushToken)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "Push token copied to clipboard", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Text("Copy to Clipboard")
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { showPushTokenDialog = false }
                    ) {
                        Text("Close")
                    }
                }
            )
        }
    }
}
