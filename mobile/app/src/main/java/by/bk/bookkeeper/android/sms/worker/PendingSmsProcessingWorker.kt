package by.bk.bookkeeper.android.sms.worker

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import by.bk.bookkeeper.android.Injection
import by.bk.bookkeeper.android.network.auth.SessionDataProvider
import by.bk.bookkeeper.android.network.response.BaseResponse
import by.bk.bookkeeper.android.sms.preferences.SmsPreferenceProvider
import timber.log.Timber

class PendingSmsProcessingWorker(appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {

    private val bkService = Injection.provideBookkeeperService()

    override fun doWork(): Result {
        Timber.d("Periodic pending sms checker started")
        SessionDataProvider.getCurrentSessionData()?.token ?: return Result.failure()
        val pendingSms = SmsPreferenceProvider.getPendingSmsFromStorage()
        if (pendingSms.isNotEmpty()) {
            val response = bkService.sendSmsToServer(pendingSms).execute()
            return if (response.isSuccessful && response.body()?.status == BaseResponse.STATUS_SUCCESS) {
                SmsPreferenceProvider.deleteSMSFromStorage(pendingSms)
                Timber.d("SMS pending request SUCCESS")
                Result.success()
            } else {
                Timber.d("SMS pending request FAILURE ${response.errorBody()}")
                Result.failure()
            }
        }
        return Result.success()
    }
}