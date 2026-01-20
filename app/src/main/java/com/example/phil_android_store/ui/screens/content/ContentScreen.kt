package com.example.phil_android_store.ui.screens.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.braze.Braze
import com.example.phil_android_store.data.BrazeUserSync

/**
 * Content screen for demonstrating Braze Content Cards and Banners.
 * This page can be used to manually install content cards and banners for client demonstrations.
 */
@Composable
fun ContentScreen() {
    val context = LocalContext.current
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
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
                },
                modifier = Modifier.weight(1f),
                contentPadding = ButtonDefaults.ButtonWithIconContentPadding
            ) {
                Text(
                    text = "Enable Push",
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center
                )
            }
            
            // Push Notification button in the middle
            Button(
                onClick = {
                    // Braze SDK: Log custom event to Braze
                    val brazeInstance = Braze.getInstance(context)
                    brazeInstance.logCustomEvent("push_notification")
                    brazeInstance.requestImmediateDataFlush()
                },
                modifier = Modifier.weight(1f),
                contentPadding = ButtonDefaults.ButtonWithIconContentPadding
            ) {
                Text(
                    text = "Push Notification",
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center
                )
            }
            
            // User Action button on the right
            Button(
                onClick = {
                    // Braze SDK: Log custom event to Braze
                    val brazeInstance = Braze.getInstance(context)
                    brazeInstance.logCustomEvent("user_action_button")
                    brazeInstance.requestImmediateDataFlush()
                },
                modifier = Modifier.weight(1f),
                contentPadding = ButtonDefaults.ButtonWithIconContentPadding
            ) {
                Text(
                    text = "User Action",
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center
                )
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
