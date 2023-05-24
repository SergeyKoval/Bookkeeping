package by.bk.bookkeeper.android.sms.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import by.bk.bookkeeper.processor.ProcessingService
import timber.log.Timber


/**
 *  Created by Evgenia Grinkevich on 05, February, 2020
 **/

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == ACTION_BOOT_COMPLETED || intent?.action == ACTION_QUICKBOOT_POWERON) {
            Timber.d("Received boot event")
            context.startForegroundService(Intent(context, ProcessingService::class.java).apply {
                action = INTENT_ACTION_BOOT_RECEIVED
            })
        }
    }

    companion object {
        const val INTENT_ACTION_BOOT_RECEIVED = "boot_received"
        private const val ACTION_BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED"
        private const val ACTION_QUICKBOOT_POWERON = "android.intent.action.QUICKBOOT_POWERON"
    }

}