package com.example.phil_android_store.ui.components

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.braze.Braze
import com.example.phil_android_store.data.BrazeUserSync

/**
 * Composable that displays a Braze banner for a given placement ID.
 * The banner is collapsible - it only renders when a banner is available.
 * 
 * @param placementId The Braze banner placement ID
 * @param modifier Modifier for the banner container
 * @param onBannerUpdate Callback when banner is updated (receives banner or null)
 */
@Composable
fun BrazeBanner(
    placementId: String,
    modifier: Modifier = Modifier,
    onBannerUpdate: ((Any?) -> Unit)? = null
) {
    val context = LocalContext.current
    var banner by remember(placementId) { mutableStateOf<Any?>(null) }
    var shouldRender by remember(placementId) { mutableStateOf(false) }
    
    // Request banner refresh when this composable is first displayed
    LaunchedEffect(placementId) {
        // Request refresh for this placement
        BrazeUserSync.requestBannerRefresh(context, listOf(placementId))
        
        // Small delay to allow banner to be fetched
        kotlinx.coroutines.delay(500)
        
        // Get the banner
        banner = BrazeUserSync.getBanner(context, placementId)
        
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
        
        onBannerUpdate?.invoke(banner)
    }
    
    // Only render if banner is available and not a control variant
    if (shouldRender && banner != null) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
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
                                        
                                        // Create an intent to handle the deep link
                                        val intent = Intent(Intent.ACTION_VIEW, uri)
                                        // Use SINGLE_TOP to reuse existing activity and trigger onNewIntent
                                        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                        
                                        // Start the activity - this will trigger onNewIntent if activity exists
                                        ctx.startActivity(intent)
                                        
                                        Log.d("BrazeBanner", "Started activity with deep link intent")
                                        
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
                    // Always set our custom WebViewClient to handle deep links
                    // This must be set after Braze inserts the banner, as Braze may override it
                    val customWebViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                            // Check if the URL is a philstore deep link
                            if (url != null && url.startsWith("philstore://")) {
                                try {
                                    Log.d("BrazeBanner", "Intercepted deep link: $url")
                                    
                                    // Parse the URI (handles URL encoding automatically)
                                    val uri = Uri.parse(url)
                                    Log.d("BrazeBanner", "Parsed URI - scheme: ${uri.scheme}, host: ${uri.host}")
                                    
                                    // Create an intent to handle the deep link
                                    val intent = Intent(Intent.ACTION_VIEW, uri)
                                    // Use SINGLE_TOP to reuse existing activity and trigger onNewIntent
                                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                    
                                    // Start the activity - this will trigger onNewIntent if activity exists
                                    context.startActivity(intent)
                                    
                                    Log.d("BrazeBanner", "Started activity with deep link intent")
                                    
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
                    
                    // Get the current banner
                    val currentBanner = BrazeUserSync.getBanner(context, placementId)
                    if (currentBanner != null) {
                        try {
                            // Try to use insertBanner method (Braze SDK method)
                            val brazeInstance = Braze.getInstance(context)
                            val insertMethod = brazeInstance.javaClass.getMethod(
                                "insertBanner", 
                                currentBanner.javaClass,
                                android.view.View::class.java
                            )
                            insertMethod.invoke(brazeInstance, currentBanner, webView)
                            
                            // IMPORTANT: Set our WebViewClient AFTER Braze inserts the banner
                            // This ensures our deep link handler is active even if Braze set its own
                            webView.webViewClient = customWebViewClient
                            Log.d("BrazeBanner", "Set custom WebViewClient after Braze insertBanner")
                        } catch (e: Exception) {
                            // If insertBanner doesn't work, try to get HTML content
                            try {
                                val htmlMethod = currentBanner.javaClass.getMethod("getHtml")
                                val htmlContent = htmlMethod.invoke(currentBanner) as? String
                                if (htmlContent != null) {
                                    // Set WebViewClient before loading HTML
                                    webView.webViewClient = customWebViewClient
                                    
                                    // Wrap HTML to center content and allow dynamic height
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
                                    webView.loadDataWithBaseURL(
                                        null,
                                        wrappedHtml,
                                        "text/html",
                                        "UTF-8",
                                        null
                                    )
                                    Log.d("BrazeBanner", "Loaded HTML content with custom WebViewClient")
                                }
                            } catch (e2: Exception) {
                                // Both methods failed - banner might not be renderable
                                Log.e("BrazeBanner", "Error loading banner: ${e2.message}", e2)
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            )
        }
    }
}
