package com.example.phil_android_store.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.phil_android_store.data.BrazeLogEntry
import com.example.phil_android_store.data.BrazeLogManager

/**
 * Braze SDK Logs screen - displays all SDK interactions in readable format
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrazeLogsScreen(
    onClose: () -> Unit
) {
    var logs by remember { mutableStateOf(BrazeLogManager.getLogs()) }
    
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
            // Logs list with clear button
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Clear button at top
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
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
                        LogEntryCard(logEntry = logEntry)
                    }
                }
            }
        }
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
 * Individual log entry card - condensed console-style format
 */
@Composable
private fun LogEntryCard(logEntry: BrazeLogEntry) {
    var showPayloadDialog by remember { mutableStateOf(false) }
    val typeColor = when (logEntry.type) {
        BrazeLogEntry.LogType.INFO -> MaterialTheme.colorScheme.primary
        BrazeLogEntry.LogType.REQUEST -> MaterialTheme.colorScheme.secondary
        BrazeLogEntry.LogType.RESPONSE -> MaterialTheme.colorScheme.tertiary
        BrazeLogEntry.LogType.EVENT -> MaterialTheme.colorScheme.primaryContainer
        BrazeLogEntry.LogType.ERROR -> MaterialTheme.colorScheme.error
    }
    
    val hasPayload = logEntry.payload != null && logEntry.payload.isNotEmpty()
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (hasPayload) Modifier.clickable { showPayloadDialog = true } else Modifier),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Time
            Text(
                text = logEntry.formattedTime,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.width(70.dp)
            )
            
            // Type badge
            Surface(
                color = typeColor.copy(alpha = 0.15f),
                shape = RoundedCornerShape(3.dp)
            ) {
                Text(
                    text = logEntry.type.name.take(3),
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = typeColor,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Event (main content)
            Text(
                text = logEntry.event,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            )
            
            // Clickable indicator if payload exists
            if (hasPayload) {
                Text(
                    text = "👁",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }
    
    // Payload dialog
    if (showPayloadDialog && logEntry.payload != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showPayloadDialog = false },
            title = {
                Text("Payload", style = MaterialTheme.typography.titleMedium)
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    logEntry.payload.forEach { (key, value) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "$key:",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )
                            Text(
                                text = value.toString(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
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
