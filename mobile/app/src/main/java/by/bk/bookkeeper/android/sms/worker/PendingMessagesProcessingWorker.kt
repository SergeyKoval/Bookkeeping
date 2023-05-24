package by.bk.bookkeeper.android.sms.worker

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import by.bk.bookkeeper.android.Injection
import by.bk.bookkeeper.android.network.auth.SessionDataProvider
import by.bk.bookkeeper.android.network.request.ProcessedMessage
import by.bk.bookkeeper.android.network.response.BaseResponse
import by.bk.bookkeeper.android.sms.preferences.SharedPreferencesProvider
import retrofit2.Call
import timber.log.Timber

abstract class PendingMessagesProcessingWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : Worker(appContext, workerParams) {

    protected val bkService = Injection.provideBookkeeperService()

    override fun doWork(): Result {
        Timber.d("Pending messages checker started")
        SessionDataProvider.getCurrentSessionData()?.token ?: return Result.failure()
        val pendingMessages = getPendingMessages()
        if (pendingMessages.isNotEmpty()) {
            val response = getRequest(pendingMessages).execute()
            return if (response.isSuccessful && response.body()?.status == BaseResponse.STATUS_SUCCESS) {
                SharedPreferencesProvider.deleteMessagesFromStorage(pendingMessages)
                Timber.d("Message pending request SUCCESS")
                Result.success()
            } else {
                Timber.d("Message pending request FAILURE ${response.errorBody()}")
                Result.failure()
            }
        }
        return Result.success()
    }

    abstract fun getPendingMessages(): List<ProcessedMessage>

    abstract fun getRequest(pendingMessages: List<ProcessedMessage>): Call<BaseResponse>

    abstract fun getWorkName(): String

}