package by.bk.bookkeeper.android.ui.login

import by.bk.bookkeeper.android.network.wrapper.DataStatus
import by.bk.bookkeeper.android.network.wrapper.InputValidationWrapper
import io.reactivex.Observable

/**
 *  Created by Evgenia Grinkevich on 29, January, 2020
 **/

internal interface LoginInteraction {

    interface Inputs {
        fun login(email: String?, password: String?)
    }

    interface Outputs {
        fun authRequestState(): Observable<DataStatus>
        fun inputValidation(): Observable<InputValidationWrapper>
        fun isAuthorized(): Observable<Boolean>
    }
}