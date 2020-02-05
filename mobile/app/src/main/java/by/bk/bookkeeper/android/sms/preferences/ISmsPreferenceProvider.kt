package by.bk.bookkeeper.android.sms.preferences

import by.bk.bookkeeper.android.sms.SMS

/**
 *  Created by Evgenia Grinkevich on 29, January, 2020
 **/

interface ISmsPreferenceProvider {

    fun getPendingSmsFromStorage(): List<SMS>

    fun saveSMSToStorage(vararg sms: SMS)

    fun deleteSMSFromStorage(vararg sms: SMS)

    fun setShouldProcessReceivedSms(shouldProcess: Boolean)

    /** Indicates whether app should process receiving sms.
     *  In case of user manual logout, this value should return false
     */
    fun getShouldProcessReceivedSms(): Boolean

}