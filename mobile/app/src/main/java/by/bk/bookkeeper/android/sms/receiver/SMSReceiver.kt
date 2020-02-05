package by.bk.bookkeeper.android.sms.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import by.bk.bookkeeper.android.sms.SMSProcessingService
import timber.log.Timber

/**
 *  Created by Evgenia Grinkevich on 05, February, 2020
 **/
class SMSReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION && intent.extras != null) {
            Timber.d("SMS received")
            context.startForegroundService(Intent(context, SMSProcessingService::class.java).apply {
                action = INTENT_ACTION_SMS_RECEIVED
                putExtra(SMSProcessingService.INTENT_PDU_EXTRA, intent.extras?.get("pdus") as? Array<*>)
                putExtra(SMSProcessingService.INTENT_PDU_FORMAT, intent.getStringExtra("format"))
            })
        }
    }

    companion object {
        const val INTENT_ACTION_SMS_RECEIVED = "sms_received"
    }
}
