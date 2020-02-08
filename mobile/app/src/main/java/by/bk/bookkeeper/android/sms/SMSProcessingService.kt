package by.bk.bookkeeper.android.sms

import android.Manifest.permission
import android.app.*
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import android.telephony.SmsMessage
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import by.bk.bookkeeper.android.Injection
import by.bk.bookkeeper.android.R
import by.bk.bookkeeper.android.network.BookkeeperService
import by.bk.bookkeeper.android.network.response.Association
import by.bk.bookkeeper.android.network.response.BaseResponse
import by.bk.bookkeeper.android.sms.preferences.SmsPreferenceProvider
import by.bk.bookkeeper.android.sms.receiver.SMSReceiver
import by.bk.bookkeeper.android.ui.home.AccountingActivity
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 *  Created by Evgenia Grinkevich on 05, February, 2020
 **/

class SMSProcessingService : Service() {

    private val periodicPendingSmsRequest = createPeriodicSmsRequest()
    private val bkService: BookkeeperService = Injection.provideBookkeeperService()
    private val disposables = CompositeDisposable()
    private val notificationBuilder by lazy { createNotificationBuilder() }

    override fun onCreate() {
        super.onCreate()
        Timber.d("Service on create invoked")
        createNotificationChannel()
        WorkManager.getInstance().enqueue(periodicPendingSmsRequest)
        observePendingSms()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Timber.d("On start command invoked with action ${intent.action}")
        if (checkSelfPermission(permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED
                || intent.action == AccountingActivity.INTENT_ACTION_USER_LOGGED_OUT) {
            stopForeground(true)
            stopSelf()
        }
        if (intent.action == SMSReceiver.INTENT_ACTION_SMS_RECEIVED) {
            processSms(intent.extras?.get(INTENT_PDU_EXTRA) as? Array<*>, intent.extras?.getString(INTENT_PDU_FORMAT))
        }
        startForeground(SERVICE_NOTIFICATION_ID, notificationBuilder.build())
        return START_REDELIVER_INTENT
    }

    private fun observePendingSms() {
        disposables.add(SmsPreferenceProvider.getPendingSmsObservable()
                .subscribeOn(Schedulers.io())
                .subscribe { pendingSms ->
                    updateNotification(notificationBuilder
                            .setContentTitle(if (pendingSms.isNotEmpty()) applicationContext.getString(R.string.msg_service_pending_sms, pendingSms.size) else null)
                            .setContentIntent(createPendingIntent(
                                    if (pendingSms.isNotEmpty()) AccountingActivity.ACTION_EXTERNAL_SHOW_SMS_STATUS
                                    else AccountingActivity.ACTION_EXTERNAL_HOME))
                            .build()
                    )
                }
        )
    }

    private fun createNotificationBuilder(): Notification.Builder = Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentIntent(createPendingIntent(AccountingActivity.ACTION_EXTERNAL_HOME))
            .setContentText(applicationContext.getString(R.string.msg_service_notification_waiting_for_sms))
            .setSmallIcon(R.drawable.ic_running_service)

    private fun updateNotification(notification: Notification) =
            getSystemService(NotificationManager::class.java)?.notify(SERVICE_NOTIFICATION_ID, notification)

    private fun createPendingIntent(targetAction: String): PendingIntent = PendingIntent.getActivity(this, SERVICE_REQUEST_CODE,
            Intent(this, AccountingActivity::class.java).apply {
                action = targetAction
            }, 0)

    private fun processSms(pdus: Array<*>?, format: String?) {
        val associations: List<Association> = SmsPreferenceProvider.getAssociationsFromStorage()
        val matchedSms: ArrayList<SMS> = arrayListOf()
        pdus?.forEach { pdu ->
            val receivedSMS = SmsMessage.createFromPdu(pdu as ByteArray, format)
            val sender = receivedSMS.displayOriginatingAddress
            val message = receivedSMS.displayMessageBody
            val dateReceived = receivedSMS.timestampMillis
            Timber.d("SMS processing from $sender")
            associations.forEach { association ->
                if (association.sender.equals(sender, ignoreCase = true)) {
                    val sms = SMS(senderName = sender, body = message, dateReceived = dateReceived)
                    association.smsBodyTemplate?.let {
                        if (message.contains(association.smsBodyTemplate, ignoreCase = true)) {
                            matchedSms.add(sms)
                        }
                    } ?: matchedSms.add(sms)
                }
            }
        }
        if (matchedSms.isNotEmpty()) {
            sendSMSRequest(matchedSms)
        } else {
            Timber.d("No matches found")
        }
    }

    private fun sendSMSRequest(matchedSms: List<SMS>) {
        disposables.add(bkService.sendSmsToServerSingle(matchedSms)
                .subscribeOn(Schedulers.io())
                .subscribeWith(smsRequestSingleDisposableObserver(matchedSms))
        )
    }

    private fun smsRequestSingleDisposableObserver(matchedSms: List<SMS>): DisposableSingleObserver<BaseResponse> {
        return object : DisposableSingleObserver<BaseResponse>() {
            override fun onSuccess(t: BaseResponse) {
                Timber.d("Sms successfully sent")
            }

            override fun onError(e: Throwable) {
                SmsPreferenceProvider.saveSMSToStorage(matchedSms)
                Timber.d("Sms sending failed with $e. Writing sms to storage")
            }
        }
    }

    private fun createNotificationChannel() {
        getSystemService(NotificationManager::class.java)?.createNotificationChannel(NotificationChannel(NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW))
    }

    private fun createPeriodicSmsRequest(): PeriodicWorkRequest = PeriodicWorkRequest.Builder(
            PendingSmsProcessingWorker::class.java,
            PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS, TimeUnit.MILLISECONDS,
            PeriodicWorkRequest.MIN_PERIODIC_FLEX_MILLIS, TimeUnit.MILLISECONDS)
            .setConstraints(createNetworkConstraint())
            .build()

    private fun createNetworkConstraint(): Constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
    }

    override fun onBind(intent: Intent): IBinder? = null

    companion object {
        private const val SERVICE_NOTIFICATION_ID = 1209
        private val NOTIFICATION_CHANNEL_ID = SMSProcessingService::class.java.canonicalName
        private const val NOTIFICATION_CHANNEL_NAME = "Bookkeeper sms processing service"
        const val SERVICE_REQUEST_CODE = 1689
        const val INTENT_PDU_EXTRA = "pdu_extra"
        const val INTENT_PDU_FORMAT = "pdu_format"
    }
}