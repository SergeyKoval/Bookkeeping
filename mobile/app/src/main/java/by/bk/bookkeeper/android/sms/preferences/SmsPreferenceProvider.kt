package by.bk.bookkeeper.android.sms.preferences

import android.content.Context
import android.content.SharedPreferences
import by.bk.bookkeeper.android.BookkeeperApp
import by.bk.bookkeeper.android.sms.SMS
import com.google.gson.Gson


object SmsPreferenceProvider : ISmsPreferenceProvider {

    private const val SMS_PREFERENCE_FILE_KEY = "by.bk.bookkeeper.data.sms"
    private const val KEY_PENDING_SMS = "sms_requests"
    private const val KEY_SHOULD_PROCESS_SMS = "should_process_sms"

    private val gson: Gson by lazy { Gson() }

    override fun getPendingSmsFromStorage(): List<SMS> = getSmsListFromPreference()

    override fun saveSMSToStorage(vararg sms: SMS) =
            getSMSPreferences().edit().putString(KEY_PENDING_SMS, gson.toJson(getSmsListFromPreference().also { it.addAll(sms) })).apply()


    override fun deleteSMSFromStorage(vararg sms: SMS) {
        getSMSPreferences().edit().putString(KEY_PENDING_SMS, gson.toJson(getSmsListFromPreference().also { it.removeAll(sms) })).apply()
    }

    override fun getShouldProcessReceivedSms(): Boolean = getSMSPreferences().getBoolean(KEY_SHOULD_PROCESS_SMS, false)

    override fun setShouldProcessReceivedSms(shouldProcess: Boolean) =
            getSMSPreferences().edit().putBoolean(KEY_SHOULD_PROCESS_SMS, shouldProcess).apply()

    private fun getSMSPreferences(): SharedPreferences =
            BookkeeperApp.getContext().getSharedPreferences(SMS_PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)

    private fun getSmsListFromPreference(): ArrayList<SMS> =
            gson.fromJson(getSMSPreferences().getString(KEY_PENDING_SMS, null), Array<SMS>::class.java)?.toCollection(ArrayList())
                    ?: arrayListOf()
}