package by.bk.bookkeeper.android.network.auth

import io.reactivex.Observable

/**
 *  Created by Evgenia Grinkevich on 29, January, 2020
 **/

interface ISessionDataProvider {

    fun getCurrentSessionData(): SessionData?

    fun getDeviceId(): String

    fun getCurrentSessionDataObservable(): Observable<Result<SessionData>>

    fun saveSessionData(sessionData: SessionData)

    fun clearSessionData()

    fun setCurrentUser(email: String?)

    fun getCurrentUser(): String?

}