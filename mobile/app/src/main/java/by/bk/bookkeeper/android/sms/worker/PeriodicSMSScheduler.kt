package by.bk.bookkeeper.android.sms.worker

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit


/**
 *  Created by Evgenia Grinkevich on 16, April, 2020
 **/

object PeriodicSMSScheduler {

    private val smsRequest = createPeriodicSmsRequest()
    private val unprocessedSmsRequest = createPeriodicUnprocessedSmsRequest()

    fun schedule(context: Context) {
        val workManager = WorkManager.getInstance(context)
        workManager.enqueueUniquePeriodicWork(
                PendingSmsProcessingWorker.WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                smsRequest
        )
        workManager.enqueueUniquePeriodicWork(
                UnprocessedSmsWorker.WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                unprocessedSmsRequest
        )
    }

    private fun createPeriodicSmsRequest(): PeriodicWorkRequest = PeriodicWorkRequest.Builder(
            PendingSmsProcessingWorker::class.java,
            PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS, TimeUnit.MILLISECONDS,
            PeriodicWorkRequest.MIN_PERIODIC_FLEX_MILLIS, TimeUnit.MILLISECONDS)
            .setConstraints(createNetworkConstraint())
            .build()

    private fun createPeriodicUnprocessedSmsRequest(): PeriodicWorkRequest = PeriodicWorkRequest.Builder(
            UnprocessedSmsWorker::class.java,
            PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS, TimeUnit.MILLISECONDS,
            PeriodicWorkRequest.MIN_PERIODIC_FLEX_MILLIS, TimeUnit.MILLISECONDS)
            .setConstraints(createNetworkConstraint())
            .build()

    private fun createNetworkConstraint(): Constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
}