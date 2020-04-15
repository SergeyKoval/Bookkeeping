package by.bk.bookkeeper.android.ui.status

import PendingSmsInteraction
import by.bk.bookkeeper.android.R
import by.bk.bookkeeper.android.network.BookkeeperService
import by.bk.bookkeeper.android.network.request.MatchedSms
import by.bk.bookkeeper.android.network.response.BaseResponse
import by.bk.bookkeeper.android.network.response.UnprocessedCountResponse
import by.bk.bookkeeper.android.network.wrapper.DataStatus
import by.bk.bookkeeper.android.network.wrapper.FailureWrapper
import by.bk.bookkeeper.android.sms.preferences.SmsPreferenceProvider
import by.bk.bookkeeper.android.ui.BaseViewModel
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber

/**
 *  Created by Evgenia Grinkevich on 31, January, 2020
 **/
class PendingSmsViewModel(private val bkService: BookkeeperService) : BaseViewModel(),
        PendingSmsInteraction.Inputs, PendingSmsInteraction.Outputs {

    private val smsLoadingState: BehaviorSubject<DataStatus> = BehaviorSubject.createDefault(DataStatus.Loading)
    override fun smsLoadingState(): Observable<DataStatus> = smsLoadingState

    override fun pendingSms(): Observable<List<MatchedSms>> = SmsPreferenceProvider.getPendingSmsObservable()

    override fun serverUnprocessedCount(): Observable<UnprocessedCountResponse> = SmsPreferenceProvider.getUnprocessedResponseObservable()

    override fun sendSmsToServer() = sendSendToServer()

    private fun sendSendToServer() {
        val pendingSms = SmsPreferenceProvider.getPendingSmsFromStorage()
        subscriptions.add(
                bkService.sendSmsToServerSingle(pendingSms)
                        .subscribeOn(Schedulers.io())
                        .doOnSubscribe { smsLoadingState.onNext(DataStatus.Loading) }
                        .subscribe({ response ->
                            if (response.status == BaseResponse.STATUS_SUCCESS) {
                                SmsPreferenceProvider.deleteSMSFromStorage(pendingSms)
                            }
                            smsLoadingState.onNext(if (response.status == BaseResponse.STATUS_SUCCESS) DataStatus.Success
                            else DataStatus.Error(FailureWrapper.Generic(R.string.err_unable_to_get_sms, null, response.message)))
                        }, { error ->
                            smsLoadingState.onNext(DataStatus.Error(FailureWrapper.getFailureType(error)))
                            Timber.e(error)
                        })
        )
    }

    override fun getServerUnprocessedCount() {
        subscriptions.add(
                bkService.getUnprocessedSmsCountSingle()
                        .subscribeOn(Schedulers.io())
                        .doOnSubscribe { smsLoadingState.onNext(DataStatus.Loading) }
                        .subscribe({ response ->
                            if (response.status == BaseResponse.STATUS_SUCCESS) {
                                SmsPreferenceProvider.saveUnprocessedResponseToStorage(response)
                            }
                            smsLoadingState.onNext(if (response.status == BaseResponse.STATUS_SUCCESS) DataStatus.Success
                            else DataStatus.Error(FailureWrapper.Generic(R.string.err_unable_to_get_unprocessed_sms_count, null, response.status)))
                        }, { error ->
                            smsLoadingState.onNext(DataStatus.Error(FailureWrapper.getFailureType(error)))
                            Timber.e(error)
                        })
        )
    }

}