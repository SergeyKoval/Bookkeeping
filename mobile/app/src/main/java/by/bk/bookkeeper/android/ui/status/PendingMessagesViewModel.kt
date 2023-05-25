package by.bk.bookkeeper.android.ui.status

import PendingMessagesInteraction
import by.bk.bookkeeper.android.R
import by.bk.bookkeeper.android.network.BookkeeperService
import by.bk.bookkeeper.android.network.dto.SourceType
import by.bk.bookkeeper.android.network.request.ProcessedMessage
import by.bk.bookkeeper.android.network.response.BaseResponse
import by.bk.bookkeeper.android.network.response.UnprocessedCountResponse
import by.bk.bookkeeper.android.network.wrapper.DataStatus
import by.bk.bookkeeper.android.network.wrapper.FailureWrapper
import by.bk.bookkeeper.android.sms.preferences.SharedPreferencesProvider
import by.bk.bookkeeper.android.ui.BaseViewModel
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber

/**
 *  Created by Evgenia Grinkevich on 31, January, 2020
 **/
class PendingMessagesViewModel(private val bkService: BookkeeperService) : BaseViewModel(),
        PendingMessagesInteraction.Inputs, PendingMessagesInteraction.Outputs {

    private val messagesLoadingState: BehaviorSubject<DataStatus> = BehaviorSubject.createDefault(DataStatus.Loading)
    override fun messagesLoadingState(): Observable<DataStatus> = messagesLoadingState

    override fun pendingMessages(): Observable<List<ProcessedMessage>> = SharedPreferencesProvider.getPendingMessagesObservable()

    override fun pendingMessagesMap(): Observable<Map<SourceType, List<ProcessedMessage>>> = SharedPreferencesProvider.getPendingMessagesMapObservable()

    override fun serverUnprocessedCount(): Observable<UnprocessedCountResponse> = SharedPreferencesProvider.getUnprocessedResponseObservable()

    override fun sendMessagesToServer(type: SourceType) {
        val pendingSms = SharedPreferencesProvider.getPendingMessagesFromStorage()
        subscriptions.add(
            bkService.sendPushesObservable(pendingSms)
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { messagesLoadingState.onNext(DataStatus.Loading) }
                .subscribe({ response ->
                    if (response.status == BaseResponse.STATUS_SUCCESS) {
                        SharedPreferencesProvider.deleteMessagesFromStorage(pendingSms)
                    }
                    messagesLoadingState.onNext(if (response.status == BaseResponse.STATUS_SUCCESS) DataStatus.Success
                    else DataStatus.Error(FailureWrapper.Generic(R.string.err_unable_to_get_sms, null, response.message)))
                }, { error ->
                    messagesLoadingState.onNext(DataStatus.Error(FailureWrapper.getFailureType(error)))
                    Timber.e(error)
                })
        )
    }

    override fun getServerUnprocessedCount() {
        subscriptions.add(
                bkService.getUnprocessedSmsMessagesSingle()
                        .subscribeOn(Schedulers.io())
                        .doOnSubscribe { messagesLoadingState.onNext(DataStatus.Loading) }
                        .subscribe({ response ->
                            if (response.status == BaseResponse.STATUS_SUCCESS) {
                                SharedPreferencesProvider.saveUnprocessedResponseToStorage(response)
                            }
                            messagesLoadingState.onNext(if (response.status == BaseResponse.STATUS_SUCCESS) DataStatus.Success
                            else DataStatus.Error(FailureWrapper.Generic(R.string.err_unable_to_get_unprocessed_sms_count, null, response.status)))
                        }, { error ->
                            messagesLoadingState.onNext(DataStatus.Error(FailureWrapper.getFailureType(error)))
                            Timber.e(error)
                        })
        )
    }

}