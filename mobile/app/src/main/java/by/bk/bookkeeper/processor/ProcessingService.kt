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
import by.bk.bookkeeper.android.network.request.ProcessedMessage
import by.bk.bookkeeper.android.network.response.BaseResponse
import by.bk.bookkeeper.android.push.PushListenerService
import by.bk.bookkeeper.android.push.PushMessage
import by.bk.bookkeeper.android.sms.preferences.SharedPreferencesProvider
import by.bk.bookkeeper.android.sms.receiver.SMSReceiver
import by.bk.bookkeeper.android.sms.worker.PeriodicMessagesScheduler
import by.bk.bookkeeper.android.ui.home.AccountingActivity
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.atomic.AtomicInteger

/**
 *  Created by Evgenia Grinkevich on 05, February, 2020
 **/

class ProcessingService : Service() {

    private lateinit var pushReceiver: PushBroadcastReceiver
    private lateinit var debugReceiver: DebugBroadcastReceiver
    // CRITICAL: Use lazy initialization to avoid blocking onCreate().
    // The 5-second foreground service deadline starts when startForegroundService() is called.
    // If property initializers (especially network stack creation) take too long,
    // onCreate() won't be reached in time to call startForeground().
    private val repository by lazy { Injection.provideMessagesRepository() }
    private val smsProcessor by lazy { Injection.provideSmsProcessor() }
    private val pushProcessor by lazy { Injection.providePushProcessor() }
    private val disposables = CompositeDisposable()
    private val gson by lazy { com.google.gson.Gson() }

    // State for notification content (updated from any thread, read only from main thread)
    @Volatile private var pendingSmsCount: Int = 0
    @Volatile private var unprocessedCount: Int? = null

    // Track active work to stop service when idle (Android 14+ dataSync timeout fix)
    private val activeWorkCount = AtomicInteger(0)
    private val mainHandler = Handler(Looper.getMainLooper())
    private val stopWhenIdleRunnable = Runnable {
        if (activeWorkCount.get() == 0) {
            Timber.d("No active work, stopping service to conserve dataSync quota")
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
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
        pushReceiver = PushBroadcastReceiver()
        val filter = IntentFilter().apply {
            addAction(PushListenerService.ACTION_ON_NOTIFICATION_POSTED)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(pushReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(pushReceiver, filter)
        }

        // Register debug broadcast receiver
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

        when (intent?.action) {
            SMSReceiver.INTENT_ACTION_SMS_RECEIVED -> {
                handleSms(
                    wakelockId = intent.extras?.getInt(SMSReceiver.EXTRA_WAKE_LOCK_ID) ?: 0,
                    pdus = intent.extras?.get(INTENT_PDU_EXTRA) as? Array<*>,
                    format = intent.extras?.getString(INTENT_PDU_FORMAT)
                )
            }
            PushListenerService.ACTION_PUSH_RECEIVED -> {
                val pushMessage = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(PushListenerService.PUSH_MESSAGE, PushMessage::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(PushListenerService.PUSH_MESSAGE)
                }
                pushMessage?.let { handlePush(it) } ?: scheduleStopWhenIdle()
            }
            else -> {
                // No specific work to do, schedule stop after delay
                scheduleStopWhenIdle()
            }
        }

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

    private fun onWorkStarted() {
        activeWorkCount.incrementAndGet()
        mainHandler.removeCallbacks(stopWhenIdleRunnable)
    }

    private fun onWorkCompleted() {
        val remaining = activeWorkCount.decrementAndGet()
        Timber.d("Work completed, $remaining tasks remaining")
        if (remaining <= 0) {
            scheduleStopWhenIdle()
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun handleSms(wakelockId: Int, pdus: Array<*>?, format: String?) {
        val matchedSms = smsProcessor.process(pdus?.toList() as? List<Any>?, format)
        if (matchedSms.isNotEmpty()) {
            onWorkStarted()
            disposables.add(repository.sendProcessedSms(matchedSms)
                .subscribeOn(Schedulers.io())
                .doFinally {
                    SMSReceiver.completeWakefulIntent(wakelockId)
                    onWorkCompleted()
                }
                .subscribeWith(processedMessagesRequestObserver(matchedSms))
            )
        } else {
            Timber.d("No sms matches found")
            SMSReceiver.completeWakefulIntent(wakelockId)
            scheduleStopWhenIdle()
        }
    }

    private fun handlePush(pushMessage: PushMessage) {
        val matchedPush = pushProcessor.process(listOf(pushMessage))
        if (matchedPush.isNotEmpty()) {
            onWorkStarted()
            disposables.add(
                repository.sendProcessedPushes(matchedPush)
                    .subscribeOn(Schedulers.io())
                    .doFinally { onWorkCompleted() }
                    .subscribeWith(processedMessagesRequestObserver(matchedPush))
            )
        } else {
            Timber.d("No push matches found")
            scheduleStopWhenIdle()
        }
    }

    private fun processedMessagesRequestObserver(messages: List<ProcessedMessage>): DisposableSingleObserver<BaseResponse> {
        return object : DisposableSingleObserver<BaseResponse>() {
            override fun onSuccess(t: BaseResponse) {
                Timber.d("Messages successfully sent")
            }

            override fun onError(e: Throwable) {
                SharedPreferencesProvider.saveMessagesToStorage(messages)
                Timber.d("Messages sending failed with $e. Writing messages to storage")
            }
        }
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
        unregisterReceiver(pushReceiver)
        unregisterReceiver(debugReceiver)
        disposables.clear()
    }

    override fun onBind(intent: Intent): IBinder? = null

    inner class PushBroadcastReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == PushListenerService.ACTION_ON_NOTIFICATION_POSTED) {
                val pushMessage = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(PushListenerService.PUSH_MESSAGE, PushMessage::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(PushListenerService.PUSH_MESSAGE)
                }
                pushMessage?.let { push ->
                    handlePush(push)
                }
            }
        }
    }

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
        private const val NOTIFICATION_CHANNEL_NAME = "Bookkeeper sms processing service"
        const val SERVICE_REQUEST_CODE = 1689
        const val INTENT_PDU_EXTRA = "pdu_extra"
        const val INTENT_PDU_FORMAT = "pdu_format"

        // Delay before stopping service when idle (allows handling rapid successive messages)
        private const val IDLE_STOP_DELAY_MS = 5000L
    }
}