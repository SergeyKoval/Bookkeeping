package by.bk.bookkeeper.processor

import android.Manifest.permission
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
import android.os.IBinder
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

/**
 *  Created by Evgenia Grinkevich on 05, February, 2020
 **/

class ProcessingService : Service() {

    private lateinit var pushReceiver: PushBroadcastReceiver
    private val repository = Injection.provideMessagesRepository()
    private val smsProcessor = Injection.provideSmsProcessor()
    private val pushProcessor = Injection.providePushProcessor()
    private val disposables = CompositeDisposable()
    private val notificationBuilder by lazy { createNotificationBuilder() }

    override fun onCreate() {
        super.onCreate()
        Timber.d("Service on create invoked")
        createNotificationChannel()
        PeriodicMessagesScheduler.schedule(context = this)
        observePendingMessages()
        observeUnprocessedSms()
        pushReceiver = PushBroadcastReceiver()
        registerReceiver(pushReceiver, IntentFilter().apply {
            addAction(PushListenerService.ACTION_ON_NOTIFICATION_POSTED)
        })
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.d("On start command invoked with action ${intent?.action}")
        if (checkSelfPermission(permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED
            || intent?.action == AccountingActivity.INTENT_ACTION_USER_LOGGED_OUT
        ) {
            stopForeground(true)
            stopSelf()
        }
        if (intent?.action == SMSReceiver.INTENT_ACTION_SMS_RECEIVED) {
            handleSms(
                wakelockId = intent.extras?.getInt(SMSReceiver.EXTRA_WAKE_LOCK_ID) ?: 0,
                pdus = intent.extras?.get(INTENT_PDU_EXTRA) as? Array<*>,
                format = intent.extras?.getString(INTENT_PDU_FORMAT)
            )
        }
        startForeground(SERVICE_NOTIFICATION_ID, notificationBuilder.build())
        return START_STICKY
    }

    private fun observePendingMessages() {
        disposables.add(
            SharedPreferencesProvider.getPendingMessagesObservable()
                .subscribeOn(Schedulers.io())
                .subscribe { pendingSms ->
                    updateNotification(
                        notificationBuilder
                            .setContentText(
                                if (pendingSms.isNotEmpty()) applicationContext.getString(R.string.msg_service_pending_sms, pendingSms.size)
                                else applicationContext.getString(R.string.msg_service_notification_waiting_for_messages)
                            )
                            .setContentIntent(
                                createPendingIntent(
                                    if (pendingSms.isNotEmpty()) AccountingActivity.ACTION_EXTERNAL_SHOW_SMS_STATUS
                                    else AccountingActivity.ACTION_EXTERNAL_HOME
                                )
                            )
                            .build()
                    )
                }
        )
    }

    private fun observeUnprocessedSms() {
        disposables.add(SharedPreferencesProvider.getUnprocessedResponseObservable()
            .subscribeOn(Schedulers.io())
            .subscribe { response ->
                val count = response.count
                updateNotification(
                    notificationBuilder
                        .setContentTitle(
                            if (count != null && count > 0)
                                applicationContext.getString(R.string.msg_sms_status_server_unprocessed_count, response.count) else null
                        )
                        .setContentIntent(
                            createPendingIntent(
                                if (count != null && count > 0) AccountingActivity.ACTION_EXTERNAL_SHOW_SMS_STATUS
                                else AccountingActivity.ACTION_EXTERNAL_HOME
                            )
                        )
                        .build()
                )
            }
        )
    }

    private fun createNotificationBuilder(): Notification.Builder = Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
        .setContentIntent(createPendingIntent(AccountingActivity.ACTION_EXTERNAL_HOME))
        .setSmallIcon(R.drawable.ic_running_service)

    private fun updateNotification(notification: Notification) =
        getSystemService(NotificationManager::class.java)?.notify(SERVICE_NOTIFICATION_ID, notification)

    private fun createPendingIntent(targetAction: String): PendingIntent = PendingIntent.getActivity(
        this, SERVICE_REQUEST_CODE,
        Intent(this, AccountingActivity::class.java).apply {
            action = targetAction
        }, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT
    )

    @Suppress("UNCHECKED_CAST")
    private fun handleSms(wakelockId: Int, pdus: Array<*>?, format: String?) {
        val matchedSms = smsProcessor.process(pdus?.toList() as? List<Any>?, format)
        if (matchedSms.isNotEmpty()) {
            disposables.add(repository.sendProcessedSms(matchedSms)
                .subscribeOn(Schedulers.io())
                .doFinally { SMSReceiver.completeWakefulIntent(wakelockId) }
                .subscribeWith(processedMessagesRequestObserver(matchedSms))
            )
        } else {
            Timber.d("No sms matches found")
            SMSReceiver.completeWakefulIntent(wakelockId)
        }
    }

    private fun handlePush(pushMessage: PushMessage) {
        disposables.add(
            repository.sendLog("[Push] ${pushMessage.packageName} | ${pushMessage.text} | ${pushMessage.timestamp}")
                .subscribeOn(Schedulers.io())
                .subscribe()
        )
        val matchedPush = pushProcessor.process(listOf(pushMessage))
        if (matchedPush.isNotEmpty()) {
            disposables.add(
                repository.sendProcessedPushes(matchedPush)
                    .subscribeOn(Schedulers.io())
                    .subscribeWith(processedMessagesRequestObserver(matchedPush))
            )
        } else {
            Timber.d("No push matches found")
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
        unregisterReceiver(pushReceiver)
        disposables.clear()
    }

    override fun onBind(intent: Intent): IBinder? = null

    inner class PushBroadcastReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == PushListenerService.ACTION_ON_NOTIFICATION_POSTED) {
                intent.getParcelableExtra<PushMessage>(PushListenerService.PUSH_MESSAGE)?.let { push ->
                    handlePush(push)
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
    }
}