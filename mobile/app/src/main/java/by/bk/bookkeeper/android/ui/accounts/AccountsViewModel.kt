package by.bk.bookkeeper.android.ui.accounts

import AccountsInteraction
import by.bk.bookkeeper.android.R
import by.bk.bookkeeper.android.network.BookkeeperService
import by.bk.bookkeeper.android.network.request.AssociationRequest
import by.bk.bookkeeper.android.network.request.DissociationRequest
import by.bk.bookkeeper.android.network.response.Account
import by.bk.bookkeeper.android.network.response.BaseResponse
import by.bk.bookkeeper.android.network.wrapper.DataStatus
import by.bk.bookkeeper.android.network.wrapper.FailureWrapper
import by.bk.bookkeeper.android.sms.preferences.SmsPreferenceProvider
import by.bk.bookkeeper.android.ui.BaseViewModel
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import timber.log.Timber

/**
 *  Created by Evgenia Grinkevich on 31, January, 2020
 **/
class AccountsViewModel(private val bkService: BookkeeperService) : BaseViewModel(),
        AccountsInteraction.Inputs, AccountsInteraction.Outputs {

    private val accounts: BehaviorSubject<List<Account>> = BehaviorSubject.create()
    override fun accounts(): Observable<List<Account>> = accounts

    private val accountsRequestState: BehaviorSubject<DataStatus> = BehaviorSubject.createDefault(DataStatus.Loading)
    override fun accountsRequestState(): Observable<DataStatus> = accountsRequestState

    private val associationRequestState: PublishSubject<AssociationRequestLoadingState> = PublishSubject.create()
    override fun associationRequestState(): Observable<AssociationRequestLoadingState> = associationRequestState

    init {
        getAccounts()
    }

    private fun getAccounts() {
        subscriptions.add(
                bkService.getAccounts()
                        .subscribeOn(Schedulers.io())
                        .doOnSubscribe {
                            accountsRequestState.onNext(DataStatus.Loading)
                        }
                        .subscribe({ responseList ->
                            accounts.onNext(responseList)
                            val associations = responseList.flatMap { acc ->
                                acc.subAccounts.mapNotNull { it.association }
                            }
                            SmsPreferenceProvider.saveAssociationsToStorage(associations)
                            accountsRequestState.onNext(if (responseList.isNotEmpty()) DataStatus.Success else DataStatus.Empty)
                        }, { error ->
                            Timber.e(error)
                            accountsRequestState.onNext(DataStatus.Error(FailureWrapper.getFailureType(error)))
                        })
        )
    }

    override fun addAssociation(associationRequest: AssociationRequest) {
        subscriptions.add(
                bkService.associateWithAccount(associationRequest)
                        .subscribeOn(Schedulers.io())
                        .doOnSubscribe {
                            associationRequestState.onNext(AssociationRequestLoadingState.AddAssociation(DataStatus.Loading))
                        }
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

    override fun removeAssociation(dissociationRequest: DissociationRequest) {
        subscriptions.add(
                bkService.dissociateFromAccount(dissociationRequest)
                        .subscribeOn(Schedulers.io())
                        .doOnSubscribe {
                            associationRequestState.onNext(AssociationRequestLoadingState.RemoveAssociation(DataStatus.Loading))
                        }
                        .subscribe({ response ->
                            associationRequestState.onNext(
                                    if (BaseResponse.STATUS_SUCCESS == response.status)
                                        AssociationRequestLoadingState.RemoveAssociation(DataStatus.Success)
                                    else AssociationRequestLoadingState.RemoveAssociation(DataStatus.Error(
                                            FailureWrapper.Generic(R.string.err_association_removal_failed, null, response.message)))
                            )
                        }, { error ->
                            Timber.e(error)
                            associationRequestState.onNext(
                                    AssociationRequestLoadingState.RemoveAssociation(DataStatus.Error(FailureWrapper.getFailureType(error)))
                            )
                        })
        )

    }

    override fun refreshAccounts() = getAccounts()

}