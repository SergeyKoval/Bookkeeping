package by.bk.bookkeeper.android.ui.home

import by.bk.bookkeeper.android.network.BookkeeperService
import by.bk.bookkeeper.android.network.auth.SessionDataProvider
import by.bk.bookkeeper.android.ui.BaseViewModel
import io.reactivex.Observable


/**
 *  Created by Evgenia Grinkevich on 31, January, 2020
 **/

class AccountingActivityViewModel(private val bkService: BookkeeperService) : BaseViewModel(),
    AccountingInteraction.Inputs, AccountingInteraction.Outputs {

    override fun isSessionValid(): Observable<Boolean> = SessionDataProvider.getCurrentSessionDataObservable().map {
        it.isSuccess
    }

    override fun logout() {
        //Todo add pending sms removal logic
        SessionDataProvider.clearSessionData()
    }

}