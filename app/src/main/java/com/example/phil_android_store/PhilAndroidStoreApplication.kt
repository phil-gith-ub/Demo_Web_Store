package com.example.phil_android_store

import android.app.Application
import com.braze.Braze
import com.braze.BrazeActivityLifecycleCallbackListener
import com.braze.configuration.BrazeConfig

class PhilAndroidStoreApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Configure Braze
        val brazeConfig = BrazeConfig.Builder()
            .setIsInAppMessageAccessibilityExclusiveModeEnabled(false)
            .setIsInAppMessagePushAccelerationEnabled(true)
            .build()
        
        // Initialize Braze
        Braze.configure(this, brazeConfig)
        
        // Register activity lifecycle callback for in-app messages
        registerActivityLifecycleCallbacks(BrazeActivityLifecycleCallbackListener())
    }
}
