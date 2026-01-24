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
import com.example.phil_android_store.data.BrazeContentManager
import com.example.phil_android_store.data.BrazeUserSync
import com.example.phil_android_store.data.rememberCachedBanner

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
    
    // Read banner from cache (pre-loaded on session start, updated after events)
    val cachedBanner = rememberCachedBanner(placementId)
    var banner by remember(placementId) { mutableStateOf<Any?>(cachedBanner) }
    var shouldRender by remember(placementId) { mutableStateOf(false) }
    
    // Update state only when cache changes (no direct fetch - cache is pre-loaded)
    LaunchedEffect(cachedBanner) {
        banner = cachedBanner
        onBannerUpdate?.invoke(cachedBanner)
        
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
                                // Check if the URL is a demostore deep link
                                if (url != null && url.startsWith("demostore://")) {
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
                            // Check if the URL is a demostore deep link
                            if (url != null && url.startsWith("demostore://")) {
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
                            if (url != null && url.startsWith("demostore://")) {
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
                    .wrapContentHeight()
            )
        }
    }
}
