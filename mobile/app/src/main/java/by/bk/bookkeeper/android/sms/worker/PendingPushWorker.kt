package by.bk.bookkeeper.android.sms.worker

import android.content.Context
import androidx.work.WorkerParameters
import by.bk.bookkeeper.android.network.dto.SourceType
import by.bk.bookkeeper.android.network.request.ProcessedMessage
import by.bk.bookkeeper.android.network.response.BaseResponse
import by.bk.bookkeeper.android.sms.preferences.SharedPreferencesProvider
import retrofit2.Call

/**
 *  Created by Evgenia Grinkevich on 20, May, 2023
 **/

class PendingPushWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : PendingMessagesProcessingWorker(appContext, workerParams) {

    override fun doWork(): Result {
        // Add configured delay before processing push notifications
        // This ensures SMS messages are processed first during retry
        val delayMs = SharedPreferencesProvider.getPushProcessingDelaySeconds() * 1000L

        if (delayMs > 0) {
            try {
                Thread.sleep(delayMs)
            } catch (e: InterruptedException) {
                // If interrupted, proceed with processing anyway
                Thread.currentThread().interrupt()
            }
        }

        // Call parent's doWork implementation
        return super.doWork()
    }

    override fun getPendingMessages(): List<ProcessedMessage> = SharedPreferencesProvider.getPendingMessagesFromStorage().filter {
        it.deviceMessage.source == SourceType.PUSH
    }

    override fun getRequest(pendingMessages: List<ProcessedMessage>): Call<BaseResponse> = bkService.sendPushes(pendingMessages)

    override fun getWorkName(): String = "PENDING_PUSHES"
}