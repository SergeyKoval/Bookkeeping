package by.bk.bookkeeper.android.sms.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.provider.Telephony
import android.util.SparseArray
import by.bk.bookkeeper.processor.ProcessingService
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 *  Created by Evgenia Grinkevich on 05, February, 2020
 **/
class SMSReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION && intent.extras != null) {
            Timber.d("SMS received")
            synchronized(activeWakeLocks) {
                val id = wakeLockNextId
                wakeLockNextId += 1
                if (wakeLockNextId <= 0) {
                    wakeLockNextId = 1
                }
                val powerManager: PowerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
                val wakeLock = powerManager.newWakeLock(
                        PowerManager.PARTIAL_WAKE_LOCK, "bookkeeper:wakelock:${intent.action}"
                )
                wakeLock.acquire(TimeUnit.SECONDS.toMillis(30))
                activeWakeLocks.put(id, wakeLock)
                context.startForegroundService(Intent(context, ProcessingService::class.java).apply {
                    action = INTENT_ACTION_SMS_RECEIVED
                    putExtra(ProcessingService.INTENT_PDU_EXTRA, intent.extras?.get("pdus") as? Array<*>?)
                    putExtra(ProcessingService.INTENT_PDU_FORMAT, intent.getStringExtra("format"))
                    putExtra(EXTRA_WAKE_LOCK_ID, id)
                })
                Timber.w("WakeLock acquired for ${intent.action}, ID = $id")
            }
        }
    }

    companion object {
        const val INTENT_ACTION_SMS_RECEIVED = "sms_received"

        private val activeWakeLocks = SparseArray<PowerManager.WakeLock>()
        private var wakeLockNextId = 1

        const val EXTRA_WAKE_LOCK_ID = "extra_wakelock_id"

        fun completeWakefulIntent(wakelockId: Int): Boolean {
            if (wakelockId == 0) return false
            synchronized(activeWakeLocks) {
                activeWakeLocks.get(wakelockId)?.let {
                    it.release()
                    Timber.d("released with ID $wakelockId")
                    activeWakeLocks.remove(wakelockId)
                    return true
                }
                return true
            }
        }
    }
}
