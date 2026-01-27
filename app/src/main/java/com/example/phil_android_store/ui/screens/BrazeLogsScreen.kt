package com.example.phil_android_store.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import android.util.Log
import com.example.phil_android_store.data.BrazeLogEntry
import com.example.phil_android_store.data.BrazeLogManager
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.json.JSONArray

/**
 * Braze SDK Logs screen - displays all SDK interactions in readable format
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrazeLogsScreen(
    onClose: () -> Unit
) {
    var logs by remember { mutableStateOf(BrazeLogManager.getLogs()) }
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // Update logs periodically to show new entries
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(500)
            logs = BrazeLogManager.getLogs()
        }
    }
    var showClearDialog by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    var expandedEntries by remember { mutableStateOf(setOf<String>()) }
    
    // Auto-scroll to top when logs update
    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }
    
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues()
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Status bar background to match banner color
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(statusBarPadding.calculateTopPadding()),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {}
        
        // Purple banner with "Braze SDK Logs" and X button
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
                // Centered "Braze SDK Logs" text
                Text(
                    text = "Braze SDK Logs",
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
        
        // Logs content
        if (logs.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "No logs yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "SDK interactions will appear here",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            // Logs list with buttons
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Copy All and Clear buttons at top
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Copy All button on the left
                    Button(
                        onClick = {
                            val allLogsText = logs.joinToString("\n") { log ->
                                val typeAbbr = log.type.name.take(3).lowercase()
                                val baseLine = "${log.formattedTime} $typeAbbr ${log.event}"
                                if (log.payload != null && log.payload.isNotEmpty()) {
                                    val payloadJson = mapToJsonString(log.payload) ?: log.payload.toString()
                                    "$baseLine\n$payloadJson"
                                } else {
                                    baseLine
                                }
                            }
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Braze SDK Logs", allLogsText)
                            clipboard.setPrimaryClip(clip)
                            scope.launch {
                                snackbarHostState.showSnackbar("All logs copied to clipboard")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy All",
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Text("Copy All")
                    }
                    
                    // Clear button on the right
                    Button(
                        onClick = { showClearDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Clear",
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Text("Clear Logs")
                    }
                }
                
                // Logs list
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    itemsIndexed(
                        items = logs
                    ) { index, logEntry ->
                        val logKey = "${logEntry.timestamp}_${logEntry.event}_${logEntry.type}_$index"
                        val isExpanded = expandedEntries.contains(logKey)
                        
                        LogEntryCard(
                            logEntry = logEntry,
                            onCopyLine = { lineText ->
                                try {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Log Entry", lineText)
                                    clipboard.setPrimaryClip(clip)
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Log line copied")
                                    }
                                } catch (e: Exception) {
                                    Log.e("BrazeLogsScreen", "Error copying line: ${e.message}", e)
                                }
                            },
                            isExpanded = isExpanded,
                            onToggleExpand = {
                                try {
                                    expandedEntries = if (expandedEntries.contains(logKey)) {
                                        expandedEntries - logKey
                                    } else {
                                        expandedEntries + logKey
                                    }
                                } catch (e: Exception) {
                                    Log.e("BrazeLogsScreen", "Error toggling expand: ${e.message}", e)
                                }
                            }
                        )
                    }
                }
            }
        }
        
        // Snackbar for copy feedback
        SnackbarHost(hostState = snackbarHostState)
    }
    
    // Clear confirmation dialog
    if (showClearDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = {
                Text("Clear All Logs?")
            },
            text = {
                Text("This will remove all logged SDK interactions. This action cannot be undone.")
            },
                confirmButton = {
                TextButton(
                    onClick = {
                        BrazeLogManager.clearLogs()
                        logs = BrazeLogManager.getLogs()
                        showClearDialog = false
                    }
                ) {
                    Text("Clear", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showClearDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Helper function to safely convert Map to JSON string
 */
private fun mapToJsonString(payload: Map<String, Any>?): String? {
    if (payload == null || payload.isEmpty()) return null
    return try {
        val jsonObject = JSONObject()
        payload.forEach { (key, value) ->
            try {
                when (value) {
                    is Map<*, *> -> {
                        @Suppress("UNCHECKED_CAST")
                        jsonObject.put(key, mapToJsonObject(value as Map<String, Any>, maxDepth = 10))
                    }
                    is List<*> -> {
                        jsonObject.put(key, listToJsonArray(value, maxDepth = 10))
                    }
                    is String -> jsonObject.put(key, value)
                    is Number -> jsonObject.put(key, value)
                    is Boolean -> jsonObject.put(key, value)
                    null -> jsonObject.put(key, JSONObject.NULL)
                    else -> jsonObject.put(key, value.toString())
                }
            } catch (e: Exception) {
                // Skip problematic entries
                jsonObject.put(key, "[Error: ${e.message}]")
            }
        }
        jsonObject.toString(2)
    } catch (e: Exception) {
        // Fallback to simple string representation
        payload.toString()
    }
}

private fun mapToJsonObject(map: Map<String, Any>, maxDepth: Int = 10): JSONObject {
    if (maxDepth <= 0) {
        return JSONObject().apply { put("error", "Max depth exceeded") }
    }
    val jsonObject = JSONObject()
    map.forEach { (key, value) ->
        try {
            when (value) {
                is Map<*, *> -> {
                    @Suppress("UNCHECKED_CAST")
                    jsonObject.put(key, mapToJsonObject(value as Map<String, Any>, maxDepth - 1))
                }
                is List<*> -> {
                    jsonObject.put(key, listToJsonArray(value, maxDepth - 1))
                }
                is String -> jsonObject.put(key, value)
                is Number -> jsonObject.put(key, value)
                is Boolean -> jsonObject.put(key, value)
                null -> jsonObject.put(key, JSONObject.NULL)
                else -> jsonObject.put(key, value.toString())
            }
        } catch (e: Exception) {
            jsonObject.put(key, "[Error: ${e.message}]")
        }
    }
    return jsonObject
}

private fun listToJsonArray(list: List<*>, maxDepth: Int = 10): JSONArray {
    if (maxDepth <= 0) {
        return JSONArray().apply { put("[Max depth exceeded]") }
    }
    val jsonArray = JSONArray()
    list.forEach { item ->
        try {
            when (item) {
                is Map<*, *> -> {
                    @Suppress("UNCHECKED_CAST")
                    jsonArray.put(mapToJsonObject(item as Map<String, Any>, maxDepth - 1))
                }
                is List<*> -> jsonArray.put(listToJsonArray(item, maxDepth - 1))
                is String -> jsonArray.put(item)
                is Number -> jsonArray.put(item)
                is Boolean -> jsonArray.put(item)
                null -> jsonArray.put(JSONObject.NULL)
                else -> jsonArray.put(item?.toString() ?: "null")
            }
        } catch (e: Exception) {
            jsonArray.put("[Error: ${e.message}]")
        }
    }
    return jsonArray
}

/**
 * Individual log entry card - raw code style format
 */
@Composable
private fun LogEntryCard(
    logEntry: BrazeLogEntry,
    onCopyLine: (String) -> Unit,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit
) {
    val context = LocalContext.current
    val typeColor = when (logEntry.type) {
        BrazeLogEntry.LogType.INFO -> MaterialTheme.colorScheme.primary
        BrazeLogEntry.LogType.REQUEST -> MaterialTheme.colorScheme.secondary
        BrazeLogEntry.LogType.RESPONSE -> MaterialTheme.colorScheme.tertiary
        BrazeLogEntry.LogType.EVENT -> MaterialTheme.colorScheme.primaryContainer
        BrazeLogEntry.LogType.ERROR -> MaterialTheme.colorScheme.error
    }
    
    val hasPayload = logEntry.payload != null && logEntry.payload.isNotEmpty()
    
    val eventText = logEntry.event
    
    // Format payload as JSON string - safely compute during composition
    // Use remember to cache the result and prevent recomputation issues
    val payloadJson = remember(logEntry.payload) {
        try {
            if (hasPayload && logEntry.payload != null) {
                mapToJsonString(logEntry.payload) ?: logEntry.payload.toString()
            } else null
        } catch (e: Throwable) {
            // Fallback to string representation if JSON conversion fails
            // Catch Throwable to catch all possible exceptions including OutOfMemoryError, StackOverflowError, etc.
            try {
                logEntry.payload?.toString() ?: null
            } catch (e2: Throwable) {
                null // If even toString fails, just return null
            }
        }
    }
    
    
    // Build full log line text for copying (always includes payload if present)
    val fullLogLine = if (hasPayload && payloadJson != null) {
        "${logEntry.formattedTime} ${logEntry.type.name.take(3).lowercase()} ${logEntry.event}\n$payloadJson"
    } else {
        "${logEntry.formattedTime} ${logEntry.type.name.take(3).lowercase()} ${logEntry.event}"
    }
    
    // Calculate available width for payload display (approximate)
    // We'll use a simple truncation approach
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = if (isExpanded) 8.dp else 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = if (isExpanded) 0.dp else 20.dp, max = if (isExpanded) Int.MAX_VALUE.dp else 20.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = if (isExpanded) Alignment.Top else Alignment.CenterVertically
            ) {
                // Time - clickable to copy
                Text(
                    text = logEntry.formattedTime,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                    modifier = Modifier
                        .width(70.dp)
                        .clickable { 
                            try {
                                onCopyLine(fullLogLine) 
                            } catch (e: Exception) {
                                Log.e("LogEntryCard", "Error copying line: ${e.message}", e)
                            }
                        },
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    textDecoration = TextDecoration.Underline
                )
                
                // Type badge (no gap - spacing handled by Row)
                Surface(
                    color = typeColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(3.dp)
                ) {
                    Text(
                        text = logEntry.type.name.take(3).lowercase(),
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = typeColor,
                        fontWeight = FontWeight.Bold,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }
                
                // Event and payload (main content) - with reserved space for arrow
                // All rows are expandable - show full text when expanded, truncated when collapsed
                Box(
                    modifier = Modifier.weight(1f)
                ) {
                    val displayText = if (isExpanded) {
                        // When expanded, show event + payload if available
                        if (hasPayload && payloadJson != null) {
                            "$eventText\n$payloadJson"
                        } else if (hasPayload) {
                            "$eventText\n${logEntry.payload?.toString() ?: ""}"
                        } else {
                            eventText
                        }
                    } else {
                        // When collapsed, show event (truncated) + payload preview if available
                        if (hasPayload && payloadJson != null) {
                            "$eventText $payloadJson"
                        } else if (hasPayload) {
                            "$eventText ${logEntry.payload?.toString() ?: ""}"
                        } else {
                            eventText
                        }
                    }
                    
                    Text(
                        text = displayText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.fillMaxWidth(),
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        overflow = if (isExpanded) TextOverflow.Visible else TextOverflow.Ellipsis,
                        maxLines = if (isExpanded) Int.MAX_VALUE else 1
                    )
                }
                
                // Expand/collapse arrow - always visible for all rows
                IconButton(
                    onClick = { 
                        try {
                            onToggleExpand() 
                        } catch (e: Exception) {
                            Log.e("LogEntryCard", "Error toggling expand: ${e.message}", e)
                        }
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
