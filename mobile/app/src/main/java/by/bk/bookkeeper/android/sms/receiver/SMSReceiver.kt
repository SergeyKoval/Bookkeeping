package by.bk.bookkeeper.android.sms.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import by.bk.bookkeeper.android.Injection
import by.bk.bookkeeper.android.sms.preferences.SharedPreferencesProvider
import by.bk.bookkeeper.android.sms.worker.PendingSmsWorker
import timber.log.Timber

/**
 *  Created by Evgenia Grinkevich on 05, February, 2020
 *
 *  Architecture: "Save-First" approach to avoid ForegroundServiceDidNotStartInTimeException.
 *
 *  Previous approach used startForegroundService() which has a 5-10 second deadline.
 *  On cold start (app process not running), the deadline would expire before
 *  ProcessingService.onCreate() could call startForeground(), causing crashes.
 *
 *  New approach:
 *  1. Parse SMS immediately in BroadcastReceiver (fast, no network)
 *  2. Save matched messages to SharedPreferences (fast)
 *  3. Schedule immediate WorkManager job to send to server
 *
 *  This eliminates the foreground service timing constraint entirely.
 *  Messages are guaranteed saved before any potential issues, and WorkManager
 *  handles network availability, retries, and battery optimization.
 **/
class SMSReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION && intent.extras != null) {
            Timber.d("SMS received - using save-first approach")

            val pdus = intent.extras?.get("pdus") as? Array<*>
            val format = intent.getStringExtra("format")

            if (pdus == null || pdus.isEmpty()) {
                Timber.w("SMS received but no PDUs found")
                return
            }

            // Step 1: Parse SMS immediately (fast, CPU-only operation)
            val smsProcessor = Injection.provideSmsProcessor()
            @Suppress("UNCHECKED_CAST")
            val matchedSms = smsProcessor.process(pdus.toList() as List<Any>, format)

            if (matchedSms.isEmpty()) {
                Timber.d("No SMS matches found for configured associations")
                return
            }

            // Step 2: Save to storage FIRST - guarantees no message loss
            SharedPreferencesProvider.saveMessagesToStorage(matchedSms)
            Timber.d("Saved ${matchedSms.size} SMS to storage for processing")

            // Step 3: Schedule immediate WorkManager job to send to server
            // WorkManager handles network availability, retries, and battery optimization
            scheduleImmediateProcessing(context)
        }
    }

    private fun scheduleImmediateProcessing(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<PendingSmsWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
        Timber.d("Scheduled immediate SMS processing via WorkManager")
    }

    companion object {
        const val INTENT_ACTION_SMS_RECEIVED = "sms_received"
    }
}
