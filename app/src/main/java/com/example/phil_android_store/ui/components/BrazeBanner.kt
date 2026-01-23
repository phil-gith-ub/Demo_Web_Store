package com.example.phil_android_store.ui.components

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.webkit.JavascriptInterface
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
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
    val colorScheme = MaterialTheme.colorScheme
    val backgroundColor = colorScheme.surface
    var banner by remember(placementId) { mutableStateOf<Any?>(null) }
    var shouldRender by remember(placementId) { mutableStateOf(false) }
    
    // Braze SDK: Request banner refresh when this composable is first displayed
    LaunchedEffect(placementId) {
        // Braze SDK: Request refresh for this placement
        BrazeUserSync.requestBannerRefresh(context, listOf(placementId))
        
        // Try to get banner with one retry if needed
        var retryCount = 0
        val maxRetries = 1
        while (retryCount <= maxRetries && banner == null) {
            // Delay: 500ms for first attempt, 1000ms for retry
            kotlinx.coroutines.delay(500L * (retryCount + 1))
            
            // Braze SDK: Get the banner
            banner = BrazeUserSync.getBanner(context, placementId)
            
            if (banner != null) {
                break
            }
            retryCount++
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
                    if (currentBanner == null || webView.tag == currentBanner) {
                        return@AndroidView // Already loaded or no banner
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
                                
                                // Inject JavaScript to intercept clicks and use our handler
                                webView.postDelayed({
                                    try {
                                        val jsCode = """
                                            (function() {
                                                // Function to handle deep links
                                                function handleDeepLink(url) {
                                                    if (url && url.startsWith('philstore://')) {
                                                        // Try JavaScript interface first
                                                        if (window.AndroidDeepLinkHandler) {
                                                            window.AndroidDeepLinkHandler.handleDeepLink(url);
                                                            return true;
                                                        }
                                                        // Fallback to location change (will be caught by WebViewClient)
                                                        window.location.href = url;
                                                        return true;
                                                    }
                                                    return false;
                                                }
                                                
                                                // Intercept all clicks
                                                document.addEventListener('click', function(e) {
                                                    var target = e.target;
                                                    while (target && target.tagName !== 'A') {
                                                        target = target.parentElement;
                                                    }
                                                    if (target && target.href) {
                                                        if (handleDeepLink(target.href)) {
                                                            e.preventDefault();
                                                            e.stopPropagation();
                                                            return false;
                                                        }
                                                    }
                                                }, true);
                                                
                                                // Also intercept all existing links
                                                var links = document.querySelectorAll('a[href^="philstore://"]');
                                                links.forEach(function(link) {
                                                    link.addEventListener('click', function(e) {
                                                        if (handleDeepLink(this.href)) {
                                                            e.preventDefault();
                                                            e.stopPropagation();
                                                            return false;
                                                        }
                                                    }, true);
                                                });
                                                
                                                // Monitor for dynamically added links
                                                var observer = new MutationObserver(function(mutations) {
                                                    mutations.forEach(function(mutation) {
                                                        mutation.addedNodes.forEach(function(node) {
                                                            if (node.nodeType === 1) {
                                                                var newLinks = node.querySelectorAll ? node.querySelectorAll('a[href^="philstore://"]') : [];
                                                                newLinks.forEach(function(link) {
                                                                    link.addEventListener('click', function(e) {
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
                                            })();
                                        """.trimIndent()
                                        webView.evaluateJavascript(jsCode, null)
                                        Log.d("BrazeBanner", "Injected JavaScript click interceptor with deep link handler")
                                    } catch (e: Exception) {
                                        Log.e("BrazeBanner", "Error injecting JavaScript: ${e.message}", e)
                                    }
                                }, 500)
                            }
                        } catch (e: Exception) {
                            // If insertBanner doesn't work, try to get HTML content
                            try {
                                val htmlMethod = currentBanner.javaClass.getMethod("getHtml")
                                val htmlContent = htmlMethod.invoke(currentBanner) as? String
                                if (htmlContent != null) {
                                    // Set WebViewClient before loading HTML
                                    webView.webViewClient = customWebViewClient
                                    
                                    // Keep background transparent - let the HTML content handle its own background
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
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            )
        }
    }
}
