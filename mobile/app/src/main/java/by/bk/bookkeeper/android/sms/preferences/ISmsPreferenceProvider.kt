package by.bk.bookkeeper.android.sms.preferences

import by.bk.bookkeeper.android.network.response.Association
import by.bk.bookkeeper.android.sms.SMS

/**
 *  Created by Evgenia Grinkevich on 29, January, 2020
 **/

interface ISmsPreferenceProvider {

    fun getPendingSmsFromStorage(): List<SMS>

    fun saveSMSToStorage(sms: List<SMS>)

    fun saveAssociationsToStorage(association: List<Association>)

    fun deleteAssociationsFromStorage(association: List<Association>)

    fun getAssociationsFromStorage(): List<Association>

    fun deleteSMSFromStorage(sms: List<SMS>)

    fun setShouldProcessReceivedSms(shouldProcess: Boolean)

    /** Indicates whether app should process receiving sms.
     *  In case of user manual logout, this value should return false
     */
    fun getShouldProcessReceivedSms(): Boolean

}