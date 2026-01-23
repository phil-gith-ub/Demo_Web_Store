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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.example.phil_android_store.data.BrazeLogEntry
import com.example.phil_android_store.data.BrazeLogManager
import kotlinx.coroutines.launch
import org.json.JSONObject

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
                                "${log.formattedTime} $typeAbbr ${log.event}"
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
                    items(logs) { logEntry ->
                        LogEntryCard(
                            logEntry = logEntry,
                            onCopyLine = { lineText ->
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Log Entry", lineText)
                                clipboard.setPrimaryClip(clip)
                                scope.launch {
                                    snackbarHostState.showSnackbar("Log line copied")
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
 * Individual log entry card - raw code style format
 */
@Composable
private fun LogEntryCard(
    logEntry: BrazeLogEntry,
    onCopyLine: (String) -> Unit
) {
    val context = LocalContext.current
    var showPayloadDialog by remember { mutableStateOf(false) }
    val typeColor = when (logEntry.type) {
        BrazeLogEntry.LogType.INFO -> MaterialTheme.colorScheme.primary
        BrazeLogEntry.LogType.REQUEST -> MaterialTheme.colorScheme.secondary
        BrazeLogEntry.LogType.RESPONSE -> MaterialTheme.colorScheme.tertiary
        BrazeLogEntry.LogType.EVENT -> MaterialTheme.colorScheme.primaryContainer
        BrazeLogEntry.LogType.ERROR -> MaterialTheme.colorScheme.error
    }
    
    val hasPayload = logEntry.payload != null && logEntry.payload.isNotEmpty()
    val eventText = logEntry.event
    
    // Parse different clickable patterns:
    // 1. logPurchaseEvent('purchase') - clickable event name
    // 2. matchCard(location='tile_2') - clickable location
    // 3. getContentCards() → [2 cards] - clickable card count
    
    val clickableEventName = if (hasPayload && eventText.contains("('") && eventText.contains("')")) {
        val startIdx = eventText.indexOf("('") + 2
        val endIdx = eventText.indexOf("')", startIdx)
        if (startIdx > 1 && endIdx > startIdx) {
            eventText.substring(startIdx, endIdx)
        } else null
    } else null
    
    // Check for matchCard(location='tile_X') pattern
    val matchCardLocation = if (hasPayload && eventText.startsWith("matchCard(location='") && eventText.contains("')")) {
        val startIdx = eventText.indexOf("location='") + 10
        val endIdx = eventText.indexOf("')", startIdx)
        if (startIdx > 9 && endIdx > startIdx) {
            eventText.substring(startIdx, endIdx)
        } else null
    } else null
    
    // Check for getContentCards() → [X cards] pattern
    val cardCountText = if (hasPayload && eventText.contains(" → [") && eventText.contains(" cards]")) {
        val startIdx = eventText.indexOf(" → [") + 4
        val endIdx = eventText.indexOf(" cards]", startIdx)
        if (startIdx > 3 && endIdx > startIdx) {
            eventText.substring(startIdx, endIdx)
        } else null
    } else null
    
    // Build annotated string with appropriate clickable parts
    val annotatedText = when {
        clickableEventName != null -> {
            buildAnnotatedString {
                val clickablePattern = "('$clickableEventName')"
                val patternIdx = eventText.indexOf(clickablePattern)
                if (patternIdx >= 0) {
                    val beforeClickable = eventText.substring(0, patternIdx + 1)
                    val clickablePart = "'$clickableEventName'"
                    val afterClickable = eventText.substring(patternIdx + clickablePattern.length)
                    
                    append(beforeClickable)
                    pushStringAnnotation(tag = "clickable", annotation = clickablePart)
                    withStyle(
                        style = SpanStyle(
                            color = MaterialTheme.colorScheme.primary,
                            textDecoration = TextDecoration.Underline
                        )
                    ) {
                        append(clickablePart)
                    }
                    pop()
                    append(afterClickable)
                } else {
                    append(eventText)
                }
            }
        }
        matchCardLocation != null -> {
            buildAnnotatedString {
                val clickablePattern = "location='$matchCardLocation'"
                val patternIdx = eventText.indexOf(clickablePattern)
                if (patternIdx >= 0) {
                    val beforeClickable = eventText.substring(0, patternIdx + 9) // "location='"
                    val clickablePart = matchCardLocation
                    val afterClickable = eventText.substring(patternIdx + clickablePattern.length)
                    
                    append(beforeClickable)
                    pushStringAnnotation(tag = "clickable", annotation = clickablePart)
                    withStyle(
                        style = SpanStyle(
                            color = MaterialTheme.colorScheme.primary,
                            textDecoration = TextDecoration.Underline
                        )
                    ) {
                        append(clickablePart)
                    }
                    pop()
                    append(afterClickable)
                } else {
                    append(eventText)
                }
            }
        }
        cardCountText != null -> {
            buildAnnotatedString {
                val clickablePattern = "[$cardCountText cards]"
                val patternIdx = eventText.indexOf(clickablePattern)
                if (patternIdx >= 0) {
                    val beforeClickable = eventText.substring(0, patternIdx + 1) // Include "["
                    val clickablePart = "$cardCountText cards]"
                    val afterClickable = eventText.substring(patternIdx + clickablePattern.length)
                    
                    append(beforeClickable)
                    pushStringAnnotation(tag = "clickable", annotation = clickablePart)
                    withStyle(
                        style = SpanStyle(
                            color = MaterialTheme.colorScheme.primary,
                            textDecoration = TextDecoration.Underline
                        )
                    ) {
                        append(clickablePart)
                    }
                    pop()
                    append(afterClickable)
                } else {
                    append(eventText)
                }
            }
        }
        else -> {
            buildAnnotatedString {
                append(eventText)
            }
        }
    }
    
    val hasClickable = clickableEventName != null || matchCardLocation != null || cardCountText != null
    
    // Build full log line text for copying
    val fullLogLine = "${logEntry.formattedTime} ${logEntry.type.name.take(3).lowercase()} ${logEntry.event}"
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        onCopyLine(fullLogLine)
                    }
                )
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Time
            Text(
                text = logEntry.formattedTime,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.width(70.dp),
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
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
            
            // Event (main content) - with clickable parts if payload exists
            if (hasClickable) {
                Text(
                    annotatedText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showPayloadDialog = true },
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
            } else {
                Text(
                    text = logEntry.event,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
            }
        }
    }
    
    // Payload dialog
    if (showPayloadDialog && logEntry.payload != null) {
        val context = LocalContext.current
        val payloadJson = try {
            JSONObject(logEntry.payload).toString(2) // Pretty print with 2-space indent
        } catch (e: Exception) {
            logEntry.payload.toString()
        }
        
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showPayloadDialog = false },
            title = {
                Text("Payload", style = MaterialTheme.typography.titleMedium)
            },
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onLongPress = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Payload", payloadJson)
                                    clipboard.setPrimaryClip(clip)
                                }
                            )
                        }
                ) {
                    Text(
                        text = payloadJson,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showPayloadDialog = false }
                ) {
                    Text("Close")
                }
            }
        )
    }
}
