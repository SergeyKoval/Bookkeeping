package by.bk.bookkeeper.android.push

import android.app.Notification
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import by.bk.bookkeeper.android.Injection
import by.bk.bookkeeper.android.sms.preferences.SharedPreferencesProvider
import by.bk.bookkeeper.android.sms.worker.PendingPushWorker
import timber.log.Timber

/**
 * NotificationListenerService that captures push notifications from banking apps.
 *
 * Architecture: "Save-First" approach to avoid ForegroundServiceDidNotStartInTimeException.
 *
 * Previous approach used startForegroundService() which has a 5-10 second deadline.
 * On cold start, the deadline could expire before ProcessingService.onCreate() runs.
 *
 * New approach:
 * 1. Process push immediately using PushProcessor (fast, no network)
 * 2. Save matched messages to SharedPreferences (fast)
 * 3. Schedule immediate WorkManager job to send to server
 *
 * This eliminates the foreground service timing constraint entirely.
 */
class PushListenerService : NotificationListenerService() {

    private val handler = Handler(Looper.getMainLooper())
    private val recentNotifications = mutableSetOf<String>()
    private val cleanupThreshold = 100 // Clean up after this many notifications
    private val pushProcessor by lazy { Injection.providePushProcessor() }

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
        if (SharedPreferencesProvider.getDebugPushNotifications()) {
            sendBroadcast(Intent(ACTION_DEBUG_NOTIFICATION_POSTED).apply {
                setPackage(packageName)
                putExtra(PUSH_MESSAGE, pushMessage)
            })
        }

        // Process push with configured delay to allow SMS priority
        val delayMs = SharedPreferencesProvider.getPushProcessingDelaySeconds() * 1000L
        handler.postDelayed({
            processPushWithSaveFirst(pushMessage)
        }, delayMs)
    }

    /**
     * Processes push notification using save-first approach.
     * This avoids foreground service timing issues on cold start.
     */
    private fun processPushWithSaveFirst(pushMessage: PushMessage) {
        Timber.d("Push received - using save-first approach for ${pushMessage.packageName}")

        // Step 1: Process push immediately (fast, CPU-only operation)
        val matchedPush = pushProcessor.process(listOf(pushMessage), null)

        if (matchedPush.isEmpty()) {
            Timber.d("No push matches found for configured associations")
            return
        }

        // Step 2: Save to storage FIRST - guarantees no message loss
        SharedPreferencesProvider.saveMessagesToStorage(matchedPush)
        Timber.d("Saved ${matchedPush.size} push notification(s) to storage for processing")

        // Step 3: Schedule immediate WorkManager job to send to server
        scheduleImmediateProcessing()
    }

    private fun scheduleImmediateProcessing() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<PendingPushWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueue(workRequest)
        Timber.d("Scheduled immediate push processing via WorkManager")
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null) // Clean up pending callbacks
        super.onDestroy()
    }

    companion object {
        const val PUSH_MESSAGE = "PUSH_MESSAGE"
        const val ACTION_DEBUG_NOTIFICATION_POSTED = "debug_notification_posted"
    }

}
