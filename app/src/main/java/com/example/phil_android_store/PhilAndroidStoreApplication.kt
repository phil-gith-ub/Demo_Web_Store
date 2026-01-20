package com.example.phil_android_store

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.braze.Braze
import com.braze.BrazeActivityLifecycleCallbackListener
import com.braze.configuration.BrazeConfig
import com.example.phil_android_store.data.BrazeSettingsManager

class PhilAndroidStoreApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Braze SDK installation: Get settings manager to read Sender ID
        val settingsManager = BrazeSettingsManager(this)
        val senderId = settingsManager.getPushSenderId()
        
        // Braze SDK installation: Configure Braze SDK
        val brazeConfigBuilder = BrazeConfig.Builder()
            .setIsInAppMessageAccessibilityExclusiveModeEnabled(false)
        
        // Braze SDK: Set Firebase Cloud Messaging Sender ID if available
        // This ensures the Sender ID from Settings (SharedPreferences) is used if set
        if (senderId.isNotEmpty()) {
            try {
                // Use reflection to set the Sender ID via BrazeConfig
                val setSenderIdMethod = brazeConfigBuilder.javaClass.getMethod(
                    "setFirebaseCloudMessagingSenderId",
                    String::class.java
                )
                setSenderIdMethod.invoke(brazeConfigBuilder, senderId)
                Log.d("BrazeConfig", "Set FCM Sender ID from settings: $senderId")
            } catch (e: Exception) {
                Log.w("BrazeConfig", "Could not set FCM Sender ID via BrazeConfig (may not be available in this SDK version): ${e.message}")
                // Fallback: The Sender ID in braze.xml will be used
            }
        }
        
        val brazeConfig = brazeConfigBuilder.build()
        
        // Braze SDK installation: Initialize Braze SDK
        Braze.configure(this, brazeConfig)
        
        // Braze SDK: Set custom deep link handler using reflection (in case classes aren't available)
        try {
            Log.d("BrazeDeeplinkHandler", "Attempting to register custom deep link handler...")
            val handlerClass = Class.forName("com.braze.deeplink.IBrazeDeeplinkHandler")
            Log.d("BrazeDeeplinkHandler", "Found IBrazeDeeplinkHandler class")
            
            val handlerSetMethod = Class.forName("com.braze.deeplink.BrazeDeeplinkHandler")
                .getMethod("setBrazeDeeplinkHandler", handlerClass)
            Log.d("BrazeDeeplinkHandler", "Found setBrazeDeeplinkHandler method")
            
            val handlerInstance = java.lang.reflect.Proxy.newProxyInstance(
                handlerClass.classLoader,
                arrayOf(handlerClass)
            ) { _, method, args ->
                if (method.name == "gotoUri" && args != null && args.size >= 2) {
                    try {
                        val context = args[0] as android.content.Context
                        val uriAction = args[1]
                        
                        // Get URI from UriAction using reflection
                        val getUriMethod = uriAction.javaClass.getMethod("getUri")
                        val uri = getUriMethod.invoke(uriAction) as? Uri
                        val uriString = uri?.toString() ?: ""
                        
                        Log.d("BrazeDeeplinkHandler", "Handling deep link: $uriString")
                        
                        // Check if it's a philstore:// deep link
                        if (uriString.startsWith("philstore://")) {
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
                        } else {
                            // For non-philstore URLs, use default Braze behavior
                            val executeMethod = uriAction.javaClass.getMethod("execute", android.content.Context::class.java)
                            executeMethod.invoke(uriAction, context)
                        }
                    } catch (e: Exception) {
                        Log.e("BrazeDeeplinkHandler", "Error handling deep link: ${e.message}", e)
                        e.printStackTrace()
                    }
                }
                null
            }
            
            handlerSetMethod.invoke(null, handlerInstance)
            Log.d("BrazeDeeplinkHandler", "Custom deep link handler registered successfully via reflection")
        } catch (e: ClassNotFoundException) {
            Log.w("BrazeDeeplinkHandler", "Deep link handler classes not found in SDK - using WebViewClient approach instead: ${e.message}")
        } catch (e: Exception) {
            Log.e("BrazeDeeplinkHandler", "Error registering custom deep link handler: ${e.message}", e)
            e.printStackTrace()
            // Deep links will be handled by WebViewClient in BrazeBanner instead
        }
        
        // Braze SDK: Register activity lifecycle callback for in-app messages
        registerActivityLifecycleCallbacks(BrazeActivityLifecycleCallbackListener())
    }
}
