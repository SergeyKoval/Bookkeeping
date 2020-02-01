package by.bk.bookkeeper.android.ui.association

import AssociationInteraction
import by.bk.bookkeeper.android.R
import by.bk.bookkeeper.android.network.BookkeeperService
import by.bk.bookkeeper.android.network.request.AssociationRequest
import by.bk.bookkeeper.android.network.response.BaseResponse
import by.bk.bookkeeper.android.network.wrapper.DataStatus
import by.bk.bookkeeper.android.network.wrapper.FailureWrapper
import by.bk.bookkeeper.android.sms.SMS
import by.bk.bookkeeper.android.sms.SMSHandler
import by.bk.bookkeeper.android.ui.BaseViewModel
import by.bk.bookkeeper.android.ui.accounts.AssociationRequestLoadingState
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import timber.log.Timber

/**
 *  Created by Evgenia Grinkevich on 31, January, 2020
 **/
class AssociationViewModel(private val bkService: BookkeeperService) : BaseViewModel(),
        AssociationInteraction.Inputs, AssociationInteraction.Outputs {

    private val sms: BehaviorSubject<Map<String, List<SMS>>> = BehaviorSubject.create()
    override fun sms(): Observable<Map<String, List<SMS>>> = sms

    private val smsLoadingState: BehaviorSubject<DataStatus> = BehaviorSubject.createDefault(DataStatus.Loading)
    override fun storedSmsLoadingState(): Observable<DataStatus> = smsLoadingState

    private val associationRequestState: PublishSubject<AssociationRequestLoadingState> = PublishSubject.create()
    override fun associationRequestState(): Observable<AssociationRequestLoadingState> = associationRequestState

    init {
        loadAllSms()
    }

    private fun loadAllSms() {
        subscriptions.add(
                SMSHandler.allSmsObservable()
                        .subscribeOn(Schedulers.io())
                        .subscribe({
                            sms.onNext(it)
                            smsLoadingState.onNext(DataStatus.Success)
                        }, { error ->
                            smsLoadingState.onNext(DataStatus.Error(FailureWrapper.Generic(R.string.err_unable_to_get_sms, error, null)))
                            Timber.e(error)
                        })

        )
    }

    override fun addAssociation(associationRequest: AssociationRequest) {
        subscriptions.add(
                bkService.associateWithAccount(associationRequest)
                        .subscribeOn(Schedulers.io())
                        .subscribe({ response ->
                            associationRequestState.onNext(
                                    if (BaseResponse.STATUS_SUCCESS == response.status)
                                        AssociationRequestLoadingState.AddAssociation(DataStatus.Success)
                                    else AssociationRequestLoadingState.AddAssociation(DataStatus.Error(
                                            FailureWrapper.Generic(R.string.err_association_adding_failed, null, response.message)))
                            )
                        }, { error ->
                            Timber.e(error)
                            associationRequestState.onNext(
                                    AssociationRequestLoadingState.AddAssociation(DataStatus.Error(FailureWrapper.getFailureType(error)))
                            )
                        })
        )
    }
}