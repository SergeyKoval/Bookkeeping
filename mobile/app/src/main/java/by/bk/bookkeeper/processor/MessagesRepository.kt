package by.bk.bookkeeper.processor

import by.bk.bookkeeper.android.network.BookkeeperService
import by.bk.bookkeeper.android.network.request.LogRequest
import by.bk.bookkeeper.android.network.request.ProcessedMessage
import by.bk.bookkeeper.android.network.response.BaseResponse
import io.reactivex.Single

/**
 *  Created by Evgenia Grinkevich on 20, May, 2023
 **/
class MessagesRepository(private val bkService: BookkeeperService) {

    fun sendProcessedSms(sms: List<ProcessedMessage>): Single<BaseResponse> = bkService.sendSmsObservable(sms)
    fun sendProcessedPushes(push: List<ProcessedMessage>): Single<BaseResponse> = bkService.sendPushesObservable(push)
    fun sendLog(log: String): Single<BaseResponse> = bkService.sendLog(LogRequest(log))
}