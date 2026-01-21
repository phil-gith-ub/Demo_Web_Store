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
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.LaunchedEffect
import com.braze.Braze
import com.example.phil_android_store.data.BrazeUserSync
import kotlinx.coroutines.delay

/**
 * Content screen for demonstrating Braze Content Cards and Banners.
 * This page can be used to manually install content cards and banners for client demonstrations.
 */
@Composable
fun ContentScreen() {
    val context = LocalContext.current
    var notificationMessage by remember { mutableStateOf<String?>(null) }
    
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
            
            // Blank content area for manually installing content cards and banners
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                // Empty space for content cards and banners
                // This area can be used to manually install Braze Content Cards and Banners
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
