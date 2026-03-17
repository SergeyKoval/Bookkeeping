package by.bk.bookkeeper.android.push

import android.app.Notification
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
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
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
    private val repository by lazy { Injection.provideMessagesRepository() }
    private val gson by lazy { com.google.gson.Gson() }
    private val disposables = CompositeDisposable()

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

        // Send debug log directly to server if enabled
        if (SharedPreferencesProvider.getDebugPushNotifications()) {
            sendDebugLog(pushMessage)
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
        disposables.clear()
        super.onDestroy()
    }

    private fun sendDebugLog(pushMessage: PushMessage) {
        val formattedTimestamp = java.text.SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            java.util.Locale.US
        ).apply {
            timeZone = java.util.TimeZone.getTimeZone("UTC")
        }.format(java.util.Date(pushMessage.timestamp))

        val debugData = mapOf(
            "type" to "push_notification",
            "timestamp" to formattedTimestamp,
            "packageName" to pushMessage.packageName,
            "text" to pushMessage.text,
            "postTime" to pushMessage.timestamp
        )

        val debugJson = gson.toJson(debugData)

        disposables.add(
            repository.sendLog(debugJson)
                .subscribeOn(Schedulers.io())
                .subscribe(
                    { Timber.d("Debug push log sent successfully") },
                    { e -> Timber.e(e, "Failed to send debug push log") }
                )
        )
    }

}
