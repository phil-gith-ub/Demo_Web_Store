package com.example.phil_android_store

import com.braze.push.BrazeFirebaseMessagingService
import com.example.phil_android_store.data.BrazeLogManager
import com.google.firebase.messaging.RemoteMessage
import android.os.Handler
import android.os.Looper

/**
 * Custom Firebase Messaging Service that logs push notification payloads to Braze logs
 * before delegating to Braze's handler.
 */
class BrazePushMessagingService : BrazeFirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Build payload map for logs: data + notification title/body
        val payload = mutableMapOf<String, Any>()
        remoteMessage.data?.let { data ->
            payload["data"] = data
        }
        remoteMessage.notification?.let { notification ->
            notification.title?.let { payload["notification_title"] = it }
            notification.body?.let { payload["notification_body"] = it }
            notification.channelId?.let { payload["notification_channel_id"] = it }
        }
        if (payload.isEmpty()) {
            payload["message_id"] = remoteMessage.messageId ?: "unknown"
        }

        // Log on main thread so Braze logs UI updates correctly
        Handler(Looper.getMainLooper()).post {
            BrazeLogManager.logPushNotificationReceived(payload)
        }

        super.onMessageReceived(remoteMessage)
    }
}
