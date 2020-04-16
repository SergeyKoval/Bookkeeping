package by.bk.bookkeeper.android.sms.worker

import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit


/**
 *  Created by Evgenia Grinkevich on 16, April, 2020
 **/

object PeriodicSMSScheduler {

    private val smsRequest = createPeriodicSmsRequest()
    private val unprocessedSmsRequest = createPeriodicUnprocessedSmsRequest()

    fun schedule() {
        val workManager = WorkManager.getInstance()
        workManager.cancelAllWork()
        workManager.pruneWork()
        workManager.enqueue(listOf(smsRequest, unprocessedSmsRequest))
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