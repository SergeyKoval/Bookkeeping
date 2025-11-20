package by.bk.bookkeeper.android.push

import android.app.Notification
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class PushListenerService : NotificationListenerService() {

    private val handler = Handler(Looper.getMainLooper())
    private val recentNotifications = mutableSetOf<String>()
    private val cleanupThreshold = 100 // Clean up after this many notifications

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)

        val text = sbn.notification.extras?.getString(Notification.EXTRA_TEXT) ?: ""

        // Skip notifications with empty text (Android sometimes posts updates/removals with empty content)
        if (text.isBlank()) {
            return
        }

        // Create a unique key for this notification (package + text + timestamp)
        val notificationKey = "${sbn.packageName}|${text}|${sbn.postTime}"

        // Skip if we've already processed this exact notification
        if (recentNotifications.contains(notificationKey)) {
            return
        }

        // Add to recent notifications set
        recentNotifications.add(notificationKey)

        // Clean up old entries if set gets too large
        if (recentNotifications.size > cleanupThreshold) {
            recentNotifications.clear()
        }

        val pushMessage = PushMessage(
            sbn.packageName ?: "",
            text,
            sbn.postTime
        )

        // Send debug broadcast if enabled
        if (by.bk.bookkeeper.android.sms.preferences.SharedPreferencesProvider.getDebugPushNotifications()) {
            sendBroadcast(Intent(ACTION_DEBUG_NOTIFICATION_POSTED).apply {
                setPackage(packageName)
                putExtra(PUSH_MESSAGE, pushMessage)
            })
        }

        // Send normal broadcast (delayed)
        val delayMs = by.bk.bookkeeper.android.sms.preferences.SharedPreferencesProvider
            .getPushProcessingDelaySeconds() * 1000L
        handler.postDelayed({
            sendBroadcast(Intent(ACTION_ON_NOTIFICATION_POSTED).apply {
                setPackage(packageName)
                putExtra(PUSH_MESSAGE, pushMessage)
            })
        }, delayMs)
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null) // Clean up pending callbacks
        super.onDestroy()
    }

    companion object {
        const val PUSH_MESSAGE = "PUSH_MESSAGE"
        const val ACTION_ON_NOTIFICATION_POSTED = "on_notification_posted"
        const val ACTION_DEBUG_NOTIFICATION_POSTED = "debug_notification_posted"
    }

}
