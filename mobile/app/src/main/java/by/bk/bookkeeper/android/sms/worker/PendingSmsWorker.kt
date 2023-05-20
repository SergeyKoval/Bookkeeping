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

class PendingSmsWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : PendingMessagesProcessingWorker(appContext, workerParams) {

    override fun getPendingMessages(): List<ProcessedMessage> = SharedPreferencesProvider.getPendingMessagesFromStorage().filter {
        it.deviceMessage.source == SourceType.SMS
    }

    override fun getRequest(pendingMessages: List<ProcessedMessage>): Call<BaseResponse> = bkService.sendSms(pendingMessages)

    override fun getWorkName(): String = "PENDING_SMS"
}