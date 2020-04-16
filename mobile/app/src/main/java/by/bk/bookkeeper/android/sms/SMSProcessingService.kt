package by.bk.bookkeeper.android.sms

import android.Manifest.permission
import android.app.*
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import android.telephony.SmsMessage
import by.bk.bookkeeper.android.Injection
import by.bk.bookkeeper.android.R
import by.bk.bookkeeper.android.network.BookkeeperService
import by.bk.bookkeeper.android.network.request.MatchedSms
import by.bk.bookkeeper.android.network.response.BaseResponse
import by.bk.bookkeeper.android.sms.ReceivedSms.Companion.createFromPdu
import by.bk.bookkeeper.android.sms.preferences.AssociationInfo
import by.bk.bookkeeper.android.sms.preferences.SmsPreferenceProvider
import by.bk.bookkeeper.android.sms.receiver.SMSReceiver
import by.bk.bookkeeper.android.sms.worker.PeriodicSMSScheduler
import by.bk.bookkeeper.android.ui.home.AccountingActivity
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

/**
 *  Created by Evgenia Grinkevich on 05, February, 2020
 **/

class SMSProcessingService : Service() {

    private val bkService: BookkeeperService = Injection.provideBookkeeperService()
    private val disposables = CompositeDisposable()
    private val notificationBuilder by lazy { createNotificationBuilder() }

    override fun onCreate() {
        super.onCreate()
        Timber.d("Service on create invoked")
        createNotificationChannel()
        PeriodicSMSScheduler.schedule()
        observePendingSms()
        observeUnprocessedSms()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.d("On start command invoked with action ${intent?.action}")
        if (checkSelfPermission(permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED
                || intent?.action == AccountingActivity.INTENT_ACTION_USER_LOGGED_OUT) {
            stopForeground(true)
            stopSelf()
        }
        if (intent?.action == SMSReceiver.INTENT_ACTION_SMS_RECEIVED) {
            processSms(intent.extras?.get(INTENT_PDU_EXTRA) as? Array<*>, intent.extras?.getString(INTENT_PDU_FORMAT))
        }
        startForeground(SERVICE_NOTIFICATION_ID, notificationBuilder.build())
        return START_STICKY
    }

    private fun observePendingSms() {
        disposables.add(SmsPreferenceProvider.getPendingSmsObservable()
                .subscribeOn(Schedulers.io())
                .subscribe { pendingSms ->
                    updateNotification(notificationBuilder
                            .setContentText(
                                    if (pendingSms.isNotEmpty()) applicationContext.getString(R.string.msg_service_pending_sms, pendingSms.size)
                                    else applicationContext.getString(R.string.msg_service_notification_waiting_for_sms)
                            )
                            .setContentIntent(createPendingIntent(
                                    if (pendingSms.isNotEmpty()) AccountingActivity.ACTION_EXTERNAL_SHOW_SMS_STATUS
                                    else AccountingActivity.ACTION_EXTERNAL_HOME))
                            .build()
                    )
                }
        )
    }

    private fun observeUnprocessedSms() {
        disposables.add(SmsPreferenceProvider.getUnprocessedResponseObservable()
                .subscribeOn(Schedulers.io())
                .subscribe { response ->
                    val count = response.count
                    updateNotification(notificationBuilder
                            .setContentTitle(if (count != null && count > 0)
                                applicationContext.getString(R.string.msg_sms_status_server_unprocessed_count, response.count) else null)
                            .setContentIntent(createPendingIntent(
                                    if (count != null && count > 0) AccountingActivity.ACTION_EXTERNAL_SHOW_SMS_STATUS
                                    else AccountingActivity.ACTION_EXTERNAL_HOME))
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

    private fun createPendingIntent(targetAction: String): PendingIntent = PendingIntent.getActivity(this, SERVICE_REQUEST_CODE,
            Intent(this, AccountingActivity::class.java).apply {
                action = targetAction
            }, 0)

    private fun processSms(pdus: Array<*>?, format: String?) {
        val associationInfo: List<AssociationInfo> = SmsPreferenceProvider.getAssociationsFromStorage()
        val receivedSms: HashMap<String, ReceivedSms> = hashMapOf()
        val matchedSms: ArrayList<MatchedSms> = arrayListOf()
        pdus?.forEach { pdu ->
            val sms = createFromPdu(pdu as ByteArray, format) ?: return
            val originatingAddress = sms.originatingAddress
            if (!receivedSms.containsKey(originatingAddress)) {
                receivedSms[originatingAddress] = sms
            } else {
                receivedSms[originatingAddress]?.apply { body += sms.body }
            }
        }
        receivedSms.forEach { entry ->
            entry.value.run {
                Timber.d("SMS processing from $senderName")
                associationInfo.forEach { association ->
                    if (association.sms.senderName.equals(senderName, ignoreCase = true)) {
                        val sms = SMS(senderName = senderName, body = body, dateReceived = dateReceived)
                        association.sms.bodyTemplate?.let {
                            if (body.contains(association.sms.bodyTemplate, ignoreCase = true)) {
                                matchedSms.add(MatchedSms(association.accountName, association.subAccountName, sms))
                            }
                        }
                                ?: matchedSms.add(MatchedSms(association.accountName, association.subAccountName, sms))
                    }
                }
            }
        }
        if (matchedSms.isNotEmpty()) {
            sendSMSRequest(matchedSms)
        } else {
            Timber.d("No matches found")
        }
    }

    private fun sendSMSRequest(matchedSms: List<MatchedSms>) {
        disposables.add(bkService.sendSmsToServerSingle(matchedSms)
                .subscribeOn(Schedulers.io())
                .subscribeWith(smsRequestSingleDisposableObserver(matchedSms))
        )
    }

    private fun smsRequestSingleDisposableObserver(matchedSms: List<MatchedSms>): DisposableSingleObserver<BaseResponse> {
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


data class ReceivedSms(val originatingAddress: String,
                       val senderName: String,
                       var body: String,
                       val dateReceived: Long) {

    companion object {

        fun createFromPdu(pdu: ByteArray?, format: String?): ReceivedSms? {
            val receivedSMS: SmsMessage = SmsMessage.createFromPdu(pdu, format) ?: return null
            val originatingAddress = receivedSMS.originatingAddress ?: ""
            val sender = receivedSMS.displayOriginatingAddress
            val message = receivedSMS.displayMessageBody
            val dateReceived = receivedSMS.timestampMillis
            return ReceivedSms(originatingAddress = originatingAddress, senderName = sender, body = message, dateReceived = dateReceived)
        }
    }
}