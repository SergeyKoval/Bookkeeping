package by.bk.bookkeeper.android.sms.preferences

import by.bk.bookkeeper.android.network.request.MatchedSms
import by.bk.bookkeeper.android.network.response.UnprocessedCountResponse
import io.reactivex.Observable

/**
 *  Created by Evgenia Grinkevich on 29, January, 2020
 **/

interface ISmsPreferenceProvider {

    fun getPendingSmsFromStorage(): List<MatchedSms>

    fun saveSMSToStorage(sms: List<MatchedSms>)

    fun saveAssociationsToStorage(association: List<AssociationInfo>)

    fun deleteAssociationsFromStorage(association: List<AssociationInfo>)

    fun getAssociationsFromStorage(): List<AssociationInfo>

    fun deleteSMSFromStorage(sms: List<MatchedSms>)

    fun setShouldProcessReceivedSms(shouldProcess: Boolean)

    fun getPendingSmsObservable(): Observable<List<MatchedSms>>

    /** Indicates whether app should process receiving sms.
     *  In case of user manual logout, this value should return false
     */
    fun getShouldProcessReceivedSms(): Boolean

    fun saveUnprocessedResponseToStorage(response: UnprocessedCountResponse)

    fun getUnprocessedResponseFromStorage(): UnprocessedCountResponse

    fun getUnprocessedResponseObservable(): Observable<UnprocessedCountResponse>
}