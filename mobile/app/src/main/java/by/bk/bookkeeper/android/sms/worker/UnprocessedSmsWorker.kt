package by.bk.bookkeeper.android.sms.worker

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import by.bk.bookkeeper.android.Injection
import by.bk.bookkeeper.android.network.auth.SessionDataProvider
import by.bk.bookkeeper.android.network.response.BaseResponse
import by.bk.bookkeeper.android.sms.preferences.SmsPreferenceProvider
import timber.log.Timber

/**
 *  Created by Evgenia Grinkevich on 15, April, 2020
 **/

class UnprocessedSmsWorker(appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {

    private val bkService = Injection.provideBookkeeperService()

    override fun doWork(): Result {
        Timber.d("Unprocessed sms checker started")
        SessionDataProvider.getCurrentSessionData()?.token ?: return Result.failure()
        val response = bkService.getUnprocessedSmsCount().execute()
        return if (response.isSuccessful && response.body()?.status == BaseResponse.STATUS_SUCCESS) {
            response.body()?.let { SmsPreferenceProvider.saveUnprocessedResponseToStorage(it) }
            Timber.d("Unprocessed SMS pending request SUCCESS")
            Result.success()
        } else {
            Timber.d("Unprocessed SMS pending request FAILURE ${response.errorBody()}")
            Result.failure()
        }
    }

    companion object {
        const val WORK_NAME = "Unprocessed SMS count worker"
    }
}
