package by.bk.bookkeeper.android.sms.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit


/**
 *  Created by Evgenia Grinkevich on 16, April, 2020
 **/

object PeriodicMessagesScheduler {

    private val workers = listOf(PendingSmsWorker::class.java, PendingPushWorker::class.java, UnprocessedSmsWorker::class.java)

    fun schedule(context: Context) {
        workers.forEach { clazz ->
            val request = PeriodicWorkRequest.Builder(
                clazz,
                PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS, TimeUnit.MILLISECONDS,
                PeriodicWorkRequest.MIN_PERIODIC_FLEX_MILLIS, TimeUnit.MILLISECONDS
            ).setConstraints(createNetworkConstraint()).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                clazz.simpleName,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }

    private fun createNetworkConstraint(): Constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()
}