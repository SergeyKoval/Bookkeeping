package by.bk.bookkeeper.android.sms.preferences

import by.bk.bookkeeper.android.network.dto.SourceType
import by.bk.bookkeeper.android.network.request.ProcessedMessage
import by.bk.bookkeeper.android.network.response.UnprocessedCountResponse
import io.reactivex.Observable

/**
 *  Created by Evgenia Grinkevich on 29, January, 2020
 **/

interface IMessagesPreferenceProvider {

    fun getPendingMessagesFromStorage(): List<ProcessedMessage>

    fun saveMessagesToStorage(sms: List<ProcessedMessage>)

    fun saveAssociationsToStorage(association: List<AssociationInfo>)

    fun deleteAssociationsFromStorage(association: List<AssociationInfo>)

    fun getAssociationsFromStorage(): List<AssociationInfo>

    fun deleteMessagesFromStorage(sms: List<ProcessedMessage>)

    fun setShouldProcessReceivedMessages(shouldProcess: Boolean)

    fun getPendingMessagesObservable(): Observable<List<ProcessedMessage>>

    fun getPendingMessagesMapObservable(): Observable<Map<SourceType, List<ProcessedMessage>>>

    /** Indicates whether app should process receiving sms.
     *  In case of user manual logout, this value should return false
     */
    fun getShouldProcessReceivedMessages(): Boolean

    fun saveUnprocessedResponseToStorage(response: UnprocessedCountResponse)

    fun getUnprocessedResponseFromStorage(): UnprocessedCountResponse

    fun getUnprocessedResponseObservable(): Observable<UnprocessedCountResponse>
}