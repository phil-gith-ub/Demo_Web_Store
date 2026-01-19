package com.example.phil_android_store.ui.components

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.braze.Braze
import com.example.phil_android_store.data.BrazeUserSync

/**
 * Composable that displays a Braze Content Card filtered by position_id.
 * The card is collapsible - it only renders when a card is available.
 * 
 * @param positionId The position_id value to filter Content Cards by (from extras)
 * @param modifier Modifier for the card container
 * @param onCardUpdate Callback when card is updated (receives card or null)
 */
@Composable
fun BrazeContentCard(
    positionId: String,
    modifier: Modifier = Modifier,
    onCardUpdate: ((Any?) -> Unit)? = null
) {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme
    val backgroundColor = colorScheme.surface
    var contentCard by remember(positionId) { mutableStateOf<Any?>(null) }
    var shouldRender by remember(positionId) { mutableStateOf(false) }
    
    // Request Content Cards refresh when this composable is first displayed
    LaunchedEffect(positionId) {
        // Request refresh for Content Cards
        BrazeUserSync.requestContentCardsRefresh(context)
        
        // Small delay to allow cards to be fetched
        kotlinx.coroutines.delay(500)
        
        // Get the Content Card by position_id
        contentCard = BrazeUserSync.getContentCardByPositionId(context, positionId)
        
        // Check if card exists and is not a control variant
        if (contentCard != null) {
            try {
                val isControlMethod = contentCard!!.javaClass.getMethod("isControlCard")
                val isControl = isControlMethod.invoke(contentCard) as? Boolean ?: false
                shouldRender = !isControl
            } catch (e: Exception) {
                // If isControlCard method doesn't exist, assume we should render
                shouldRender = true
            }
        } else {
            shouldRender = false
        }
        
        onCardUpdate?.invoke(contentCard)
    }
    
    // Only render if card is available and not a control variant
    if (shouldRender && contentCard != null) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        // Set background to transparent to prevent white flash
                        setBackgroundColor(android.graphics.Color.TRANSPARENT)
                        
                        // Custom WebViewClient to intercept deep links
                        webViewClient = object : WebViewClient() {
                            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                                // Check if the URL is a philstore deep link
                                if (url != null && url.startsWith("philstore://")) {
                                    try {
                                        Log.d("BrazeContentCard", "Intercepted deep link: $url")
                                        
                                        val uri = Uri.parse(url)
                                        val intent = Intent(Intent.ACTION_VIEW, uri)
                                        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                        intent.setPackage(context.packageName)
                                        
                                        context.startActivity(intent)
                                        Log.d("BrazeContentCard", "Started activity with deep link intent")
                                        
                                        return true
                                    } catch (e: Exception) {
                                        Log.e("BrazeContentCard", "Error handling deep link: ${e.message}", e)
                                        return false
                                    }
                                }
                                return false
                            }
                            
                            @android.annotation.SuppressLint("NewApi")
                            override fun shouldOverrideUrlLoading(view: WebView?, request: android.webkit.WebResourceRequest?): Boolean {
                                val url = request?.url?.toString()
                                if (url != null && url.startsWith("philstore://")) {
                                    try {
                                        Log.d("BrazeContentCard", "Intercepted deep link (new API): $url")
                                        
                                        val intent = Intent(Intent.ACTION_VIEW, request.url)
                                        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                        intent.setPackage(context.packageName)
                                        
                                        context.startActivity(intent)
                                        return true
                                    } catch (e: Exception) {
                                        Log.e("BrazeContentCard", "Error handling deep link (new API): ${e.message}", e)
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
                    // Get the current card
                    val currentCard = BrazeUserSync.getContentCardByPositionId(context, positionId)
                    if (currentCard != null) {
                        try {
                            // Try to get HTML content from the card
                            val getHtmlMethod = currentCard.javaClass.getMethod("getHtml")
                            val htmlContent = getHtmlMethod.invoke(currentCard) as? String
                            
                            if (htmlContent != null) {
                                // Set background color to match theme
                                val bgColorHex = String.format("#%08X", backgroundColor.toArgb())
                                
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
                                                background-color: $bgColorHex;
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
                                Log.d("BrazeContentCard", "Loaded Content Card HTML content")
                            } else {
                                // Try to get image URL and URL for image-only cards
                                try {
                                    val getImageMethod = currentCard.javaClass.getMethod("getImage")
                                    val imageUrl = getImageMethod.invoke(currentCard) as? String
                                    val getUrlMethod = currentCard.javaClass.getMethod("getUrlString")
                                    val cardUrl = getUrlMethod.invoke(currentCard) as? String
                                    
                                    if (imageUrl != null) {
                                        val bgColorHex = String.format("#%08X", backgroundColor.toArgb())
                                        val imageHtml = if (cardUrl != null) {
                                            """
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
                                                        background-color: $bgColorHex;
                                                    }
                                                    img {
                                                        max-width: 100%;
                                                        height: auto;
                                                    }
                                                </style>
                                            </head>
                                            <body>
                                                <a href="$cardUrl"><img src="$imageUrl" alt="Content Card" /></a>
                                            </body>
                                            </html>
                                            """.trimIndent()
                                        } else {
                                            """
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
                                                        background-color: $bgColorHex;
                                                    }
                                                    img {
                                                        max-width: 100%;
                                                        height: auto;
                                                    }
                                                </style>
                                            </head>
                                            <body>
                                                <img src="$imageUrl" alt="Content Card" />
                                            </body>
                                            </html>
                                            """.trimIndent()
                                        }
                                        
                                        webView.loadDataWithBaseURL(
                                            null,
                                            imageHtml,
                                            "text/html",
                                            "UTF-8",
                                            null
                                        )
                                        Log.d("BrazeContentCard", "Loaded Content Card image")
                                    }
                                } catch (e: Exception) {
                                    Log.e("BrazeContentCard", "Error loading Content Card: ${e.message}", e)
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("BrazeContentCard", "Error getting Content Card content: ${e.message}", e)
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
