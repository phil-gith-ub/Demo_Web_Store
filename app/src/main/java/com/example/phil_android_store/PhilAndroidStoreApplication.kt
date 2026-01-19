package com.example.phil_android_store

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.braze.Braze
import com.braze.BrazeActivityLifecycleCallbackListener
import com.braze.configuration.BrazeConfig
import com.braze.deeplink.BrazeDeeplinkHandler
import com.braze.deeplink.IBrazeDeeplinkHandler
import com.braze.models.outgoing.BrazeProperties
import com.braze.models.outgoing.UriAction

class PhilAndroidStoreApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Configure Braze
        val brazeConfig = BrazeConfig.Builder()
            .setIsInAppMessageAccessibilityExclusiveModeEnabled(false)
            .build()
        
        // Initialize Braze
        Braze.configure(this, brazeConfig)
        
        // Set custom deep link handler for banners, content cards, and in-app messages
        BrazeDeeplinkHandler.setBrazeDeeplinkHandler(object : IBrazeDeeplinkHandler {
            override fun gotoUri(context: android.content.Context, uriAction: UriAction) {
                val uri = uriAction.uri
                val uriString = uri.toString()
                
                Log.d("BrazeDeeplinkHandler", "Handling deep link: $uriString")
                
                // Check if it's a philstore:// deep link
                if (uriString.startsWith("philstore://")) {
                    try {
                        // Create intent to open MainActivity with the deep link
                        val intent = Intent(Intent.ACTION_VIEW, uri)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        intent.setPackage(context.packageName)
                        
                        // Check if MainActivity can handle this intent
                        if (intent.resolveActivity(context.packageManager) != null) {
                            context.startActivity(intent)
                            Log.d("BrazeDeeplinkHandler", "Started MainActivity with deep link: $uriString")
                        } else {
                            Log.w("BrazeDeeplinkHandler", "Could not resolve activity for deep link: $uriString")
                        }
                    } catch (e: Exception) {
                        Log.e("BrazeDeeplinkHandler", "Error handling deep link: ${e.message}", e)
                    }
                } else {
                    // For non-philstore URLs, use default Braze behavior
                    uriAction.execute(context)
                }
            }
        })
        
        // Register activity lifecycle callback for in-app messages
        registerActivityLifecycleCallbacks(BrazeActivityLifecycleCallbackListener())
    }
}
