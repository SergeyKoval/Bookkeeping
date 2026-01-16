package by.bk.bookkeeper.processor

import android.Manifest.permission
import android.app.ForegroundServiceStartNotAllowedException
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import by.bk.bookkeeper.android.Injection
import by.bk.bookkeeper.android.R
import by.bk.bookkeeper.android.push.PushListenerService
import by.bk.bookkeeper.android.push.PushMessage
import by.bk.bookkeeper.android.sms.preferences.SharedPreferencesProvider
import by.bk.bookkeeper.android.sms.worker.PeriodicMessagesScheduler
import by.bk.bookkeeper.android.ui.home.AccountingActivity
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

/**
 *  Created by Evgenia Grinkevich on 05, February, 2020
 **/

/**
 * Service for showing notification with pending message count and scheduling periodic workers.
 *
 * Note: SMS and push message processing have been moved to save-first architecture
 * (SMSReceiver and PushListenerService handle messages directly via WorkManager)
 * to avoid ForegroundServiceDidNotStartInTimeException on cold start.
 *
 * This service now only handles:
 * - Scheduling PeriodicMessagesScheduler on boot/app open
 * - Displaying notification with pending/unprocessed message count
 * - Debug logging for push notifications
 */
class ProcessingService : Service() {

    private lateinit var debugReceiver: DebugBroadcastReceiver
    // CRITICAL: Use lazy initialization to avoid blocking onCreate().
    // The 5-second foreground service deadline starts when startForegroundService() is called.
    // If property initializers (especially network stack creation) take too long,
    // onCreate() won't be reached in time to call startForeground().
    private val repository by lazy { Injection.provideMessagesRepository() }
    private val disposables = CompositeDisposable()
    private val gson by lazy { com.google.gson.Gson() }

    // State for notification content (updated from any thread, read only from main thread)
    @Volatile private var pendingSmsCount: Int = 0
    @Volatile private var unprocessedCount: Int? = null

    private val mainHandler = Handler(Looper.getMainLooper())
    // Stop service when idle to conserve Android 14+ dataSync quota
    private val stopWhenIdleRunnable = Runnable {
        Timber.d("Stopping service to conserve dataSync quota")
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onCreate() {
        super.onCreate()
        Timber.d("Service on create invoked")
        createNotificationChannel()
        // CRITICAL: Call startForeground() immediately after channel creation.
        // The 5-second window starts when startForegroundService() is called by the caller,
        // NOT when onStartCommand() runs. If onCreate() takes too long (WorkManager init,
        // process cold-start, memory pressure), onStartCommand() may never be reached in time.
        // This ensures the foreground contract is satisfied ASAP. onStartCommand() will
        // call startForegroundSafely() again to update the notification with correct content.
        startForegroundSafely()
        PeriodicMessagesScheduler.schedule(context = this)
        observePendingMessages()
        observeUnprocessedSms()

        // Register debug broadcast receiver for push notification logging
        debugReceiver = DebugBroadcastReceiver()
        val debugFilter = IntentFilter().apply {
            addAction(PushListenerService.ACTION_DEBUG_NOTIFICATION_POSTED)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(debugReceiver, debugFilter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(debugReceiver, debugFilter)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.d("On start command invoked with action ${intent?.action}")

        // CRITICAL: Must call startForeground() immediately when started via startForegroundService().
        // Android requires this within 5 seconds or throws ForegroundServiceDidNotStartInTimeException.
        // This must happen BEFORE any permission checks or early returns.
        startForegroundSafely()

        // Cancel any pending stop - new work is arriving
        mainHandler.removeCallbacks(stopWhenIdleRunnable)

        // Now that foreground contract is satisfied, check if we should stop immediately
        if (checkSelfPermission(permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED
                || intent?.action == AccountingActivity.INTENT_ACTION_USER_LOGGED_OUT) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }

        // Note: SMS and push messages are now handled directly by SMSReceiver and
        // PushListenerService using save-first approach to avoid
        // ForegroundServiceDidNotStartInTimeException on cold start.
        // This service now only shows notifications and schedules periodic workers.
        scheduleStopWhenIdle()

        // Don't auto-restart - receivers will start us when needed
        return START_NOT_STICKY
    }

    private fun observePendingMessages() {
        disposables.add(
            SharedPreferencesProvider.getPendingMessagesObservable()
                .subscribeOn(Schedulers.io())
                .subscribe(
                    { pendingSms ->
                        pendingSmsCount = pendingSms.size
                        updateNotificationOnMainThread()
                    },
                    { e -> Timber.e(e, "Error observing pending messages") }
                )
        )
    }

    private fun observeUnprocessedSms() {
        disposables.add(SharedPreferencesProvider.getUnprocessedResponseObservable()
            .subscribeOn(Schedulers.io())
            .subscribe(
                { response ->
                    unprocessedCount = response.count
                    updateNotificationOnMainThread()
                },
                { e -> Timber.e(e, "Error observing unprocessed SMS") }
            )
        )
    }

    /**
     * Ensures startForeground() is called, catching all possible exceptions.
     * Uses progressively simpler notifications if building fails.
     * This method GUARANTEES that either startForeground() is called or quota is exhausted.
     */
    private fun startForegroundSafely() {
        // Try with full-featured notification
        try {
            startForeground(SERVICE_NOTIFICATION_ID, buildNotificationSafely())
            return
        } catch (e: ForegroundServiceStartNotAllowedException) {
            // On Android 14+, dataSync services have a daily time limit (~6 hours).
            // If quota is exhausted, continue without foreground status.
            Timber.w(e, "Foreground service quota exhausted, continuing without foreground status")
            return
        } catch (e: Exception) {
            Timber.w(e, "Failed to build notification, trying fallback")
        }

        // Try with minimal fallback notification
        try {
            startForeground(SERVICE_NOTIFICATION_ID, buildFallbackNotification())
            return
        } catch (e: ForegroundServiceStartNotAllowedException) {
            Timber.w(e, "Foreground service quota exhausted, continuing without foreground status")
            return
        } catch (e: Exception) {
            Timber.w(e, "Fallback notification also failed, trying minimal inline notification")
        }

        // Last resort: inline minimal notification with no external dependencies
        try {
            val minimalNotification = Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)  // System icon, always available
                .setContentTitle("Bookkeeper")
                .setContentText("Processing")
                .build()
            startForeground(SERVICE_NOTIFICATION_ID, minimalNotification)
        } catch (e: ForegroundServiceStartNotAllowedException) {
            Timber.w(e, "Foreground service quota exhausted, continuing without foreground status")
        } catch (e: Exception) {
            // If even this fails, there's nothing more we can do
            Timber.e(e, "All attempts to start foreground service failed")
        }
    }

    /**
     * Builds a notification with current state. Creates a fresh builder each time
     * to avoid thread-safety issues with shared Notification.Builder instances.
     * Must be called from main thread only.
     */
    private fun buildNotificationSafely(): Notification {
        val hasPending = pendingSmsCount > 0
        val hasUnprocessed = (unprocessedCount ?: 0) > 0

        val contentText = if (hasPending) {
            applicationContext.getString(R.string.msg_service_pending_sms, pendingSmsCount)
        } else {
            applicationContext.getString(R.string.msg_service_notification_waiting_for_messages)
        }

        val contentTitle = if (hasUnprocessed) {
            applicationContext.getString(R.string.msg_sms_status_server_unprocessed_count, unprocessedCount)
        } else {
            getString(R.string.app_name)
        }

        val targetAction = if (hasPending || hasUnprocessed) {
            AccountingActivity.ACTION_EXTERNAL_SHOW_SMS_STATUS
        } else {
            AccountingActivity.ACTION_EXTERNAL_HOME
        }

        return Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentIntent(createPendingIntent(targetAction))
            .setSmallIcon(R.drawable.ic_running_service)
            .setContentTitle(contentTitle)
            .setContentText(contentText)
            .build()
    }

    /**
     * Builds a minimal fallback notification when the regular notification building fails.
     */
    private fun buildFallbackNotification(): Notification {
        return Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_running_service)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.notification_service_running))
            .build()
    }

    /**
     * Updates the notification on the main thread to avoid concurrent modification issues.
     */
    private fun updateNotificationOnMainThread() {
        mainHandler.post {
            try {
                val notification = buildNotificationSafely()
                getSystemService(NotificationManager::class.java)?.notify(SERVICE_NOTIFICATION_ID, notification)
            } catch (e: Exception) {
                Timber.e(e, "Failed to update notification")
            }
        }
    }


    private fun createPendingIntent(targetAction: String): PendingIntent = PendingIntent.getActivity(
        this, SERVICE_REQUEST_CODE,
        Intent(this, AccountingActivity::class.java).apply {
            action = targetAction
        }, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT
    )

    private fun scheduleStopWhenIdle() {
        mainHandler.removeCallbacks(stopWhenIdleRunnable)
        mainHandler.postDelayed(stopWhenIdleRunnable, IDLE_STOP_DELAY_MS)
    }

    private fun createNotificationChannel() {
        getSystemService(NotificationManager::class.java)?.createNotificationChannel(
            NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW
            )
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        mainHandler.removeCallbacks(stopWhenIdleRunnable)
        unregisterReceiver(debugReceiver)
        disposables.clear()
    }

    override fun onBind(intent: Intent): IBinder? = null

    inner class DebugBroadcastReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == PushListenerService.ACTION_DEBUG_NOTIFICATION_POSTED) {
                val pushMessage = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(PushListenerService.PUSH_MESSAGE, PushMessage::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(PushListenerService.PUSH_MESSAGE)
                }

                pushMessage?.let { push ->
                    val formattedTimestamp = java.text.SimpleDateFormat(
                        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                        java.util.Locale.US
                    ).apply {
                        timeZone = java.util.TimeZone.getTimeZone("UTC")
                    }.format(java.util.Date(push.timestamp))

                    val debugData = mapOf(
                        "type" to "push_notification",
                        "timestamp" to formattedTimestamp,
                        "packageName" to push.packageName,
                        "text" to push.text,
                        "postTime" to push.timestamp
                    )

                    val debugJson = gson.toJson(debugData)

                    disposables.add(
                        repository.sendLog(debugJson)
                            .subscribeOn(Schedulers.io())
                            .subscribe(
                                { Timber.d("Debug log sent successfully") },
                                { e -> Timber.e(e, "Failed to send debug log") }
                            )
                    )
                }
            }
        }
    }

    companion object {
        private const val SERVICE_NOTIFICATION_ID = 1209
        private val NOTIFICATION_CHANNEL_ID = ProcessingService::class.java.canonicalName
        private const val NOTIFICATION_CHANNEL_NAME = "Bookkeeper processing service"
        const val SERVICE_REQUEST_CODE = 1689

        // Delay before stopping service when idle (allows handling rapid successive messages)
        private const val IDLE_STOP_DELAY_MS = 5000L
    }
}