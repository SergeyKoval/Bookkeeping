package by.bk.bookkeeper.android.ui.home

import io.reactivex.Observable


/**
 *  Created by Evgenia Grinkevich on 29, January, 2020
 **/

internal interface AccountingInteraction {

    interface Inputs {
        fun logout()
    }

    interface Outputs {
        fun isSessionValid(): Observable<Boolean>
    }

}