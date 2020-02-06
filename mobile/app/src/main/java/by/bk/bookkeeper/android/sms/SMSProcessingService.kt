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
import by.bk.bookkeeper.android.network.auth.SessionDataProvider
import by.bk.bookkeeper.android.network.response.Association
import by.bk.bookkeeper.android.network.response.BaseResponse
import by.bk.bookkeeper.android.sms.preferences.SmsPreferenceProvider
import by.bk.bookkeeper.android.sms.receiver.BootReceiver
import by.bk.bookkeeper.android.sms.receiver.SMSReceiver
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

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Timber.d("On start command invoked with action ${intent.action}")
        if (checkSelfPermission(permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED
                || intent.action == AccountingActivity.INTENT_ACTION_USER_LOGGED_OUT) {
            stopForeground(true)
            stopSelf()
        }
        val notificationMessage: String
        when (intent.action) {
            BootReceiver.INTENT_ACTION_BOOT_RECEIVED,
            AccountingActivity.INTENT_ACTION_ROOT_ACTIVITY_LAUNCHED -> {
                notificationMessage = applicationContext.getString(R.string.msg_service_notification_waiting_for_sms)
            }
            SMSReceiver.INTENT_ACTION_SMS_RECEIVED -> {
                notificationMessage = applicationContext.getString(R.string.msg_service_notification_sms_processing)
                processSms(intent.extras?.get(INTENT_PDU_EXTRA)as? Array<*>, intent.extras?.getString(INTENT_PDU_FORMAT))
            }
            else -> notificationMessage = applicationContext.getString(R.string.msg_service_notification_running)
        }
        createNotificationChannel()
        startForeground(SERVICE_NOTIFICATION_ID, createNotification(notificationMessage))
        return START_REDELIVER_INTENT
    }

    private fun createNotification(message: String): Notification {
        val contentPendingIntent = PendingIntent.getActivity(this, 0,
                Intent(this, AccountingActivity::class.java), 0)
        return Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
                // .setContentTitle(NOTIFICATION_TITLE)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_send)
                .setContentIntent(contentPendingIntent)
                .build()
    }

    private fun updateNotification(message: String) =
            getSystemService(NotificationManager::class.java).notify(SERVICE_NOTIFICATION_ID, createNotification(message))

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
        disposables.add(bkService.sendSmsToServer(matchedSms)
                .subscribeOn(Schedulers.io())
                .doFinally { updateNotification(applicationContext.getString(R.string.msg_service_notification_waiting_for_sms)) }
                .subscribeWith(smsRequestSingleDisposableObserver(matchedSms))
        )
    }

    private fun smsRequestSingleDisposableObserver(matchedSms: List<SMS>): DisposableSingleObserver<BaseResponse> {
        return object : DisposableSingleObserver<BaseResponse>() {
            override fun onSuccess(t: BaseResponse) {
                Timber.d("Sms successfully sended")
            }

            override fun onError(e: Throwable) {
                SmsPreferenceProvider.saveSMSToStorage(matchedSms)
                Timber.d("Sms sending failed with $e. Writing sms to storage")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
    }

    override fun onBind(intent: Intent): IBinder? = null

    private fun createNotificationChannel() {
        getSystemService(NotificationManager::class.java)
                .createNotificationChannel(NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW))
    }

    companion object {
        private const val SERVICE_NOTIFICATION_ID = 1209
        private val NOTIFICATION_CHANNEL_ID = SMSProcessingService::class.java.canonicalName
        private const val NOTIFICATION_CHANNEL_NAME = "Bookkeeper sms processing service"
        private const val NOTIFICATION_TITLE = "Bookkeeper SMS Service"
        const val INTENT_PDU_EXTRA = "pdu_extra"
        const val INTENT_PDU_FORMAT = "pdu_format"
    }
}