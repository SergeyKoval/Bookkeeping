package by.bk.bookkeeper.android.ui.accounts

import AccountsInteraction
import by.bk.bookkeeper.android.network.BookkeeperService
import by.bk.bookkeeper.android.network.response.Account
import by.bk.bookkeeper.android.network.wrapper.DataStatus
import by.bk.bookkeeper.android.network.wrapper.FailureWrapper
import by.bk.bookkeeper.android.ui.BaseViewModel
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
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

    init {
        getAccounts()
    }

    private fun getAccounts() {
        subscriptions.add(
                bkService.getAccounts()
                        .subscribeOn(Schedulers.io())
                        .subscribe({ responseList ->
                            accounts.onNext(responseList)
                            accountsRequestState.onNext(if (responseList.isNotEmpty()) DataStatus.Success else DataStatus.Empty)
                        }, { error ->
                            Timber.e(error)
                            accountsRequestState.onNext(DataStatus.Error(FailureWrapper.getFailureType(error)))
                        })
        )
    }

    override fun retryLoading() = getAccounts()

}