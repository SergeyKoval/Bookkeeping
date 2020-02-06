package by.bk.bookkeeper.android.sms.preferences

import android.content.Context
import android.content.SharedPreferences
import by.bk.bookkeeper.android.BookkeeperApp
import by.bk.bookkeeper.android.network.auth.SessionDataProvider
import by.bk.bookkeeper.android.network.response.Association
import by.bk.bookkeeper.android.sms.SMS
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject


object SmsPreferenceProvider : ISmsPreferenceProvider {

    private const val SMS_PREFERENCE_FILE_KEY = "by.bk.bookkeeper.data.sms"
    private const val KEY_PENDING_SMS = "sms_requests"
    private const val KEY_ASSOCIATIONS = "associations"
    private const val KEY_SHOULD_PROCESS_SMS = "should_process_sms"

    private val gson: Gson by lazy { Gson() }
    private val smsMapTypeToken = object : TypeToken<HashMap<String, ArrayList<SMS>>>() {}.type
    private val associationsMapTypeToken = object : TypeToken<HashMap<String, List<Association>>>() {}.type

    private val dataPrefSubject = BehaviorSubject.createDefault(getSMSPreferences())
    private val dataPrefChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
        when (key) {
            KEY_PENDING_SMS -> dataPrefSubject.onNext(sharedPreferences)
        }
    }

    init {
        getSMSPreferences().registerOnSharedPreferenceChangeListener(dataPrefChangeListener)
    }

    override fun getPendingSmsObservable(): Observable<List<SMS>> = dataPrefSubject.map {
        getPendingSmsFromStorage()
    }

    override fun getPendingSmsFromStorage(): List<SMS> =
            getSmsMapFromPreference()[SessionDataProvider.getCurrentUser()] ?: listOf()

    override fun saveSMSToStorage(sms: List<SMS>) {
        val user = SessionDataProvider.getCurrentUser() ?: return
        val smsMap = getSmsMapFromPreference().apply {
            val pendingSms = (this[user] ?: arrayListOf()).apply { addAll(sms) }
            put(user, pendingSms)
        }
        getSMSPreferences().edit().putString(KEY_PENDING_SMS, gson.toJson(smsMap, smsMapTypeToken)).apply()
    }

    override fun deleteSMSFromStorage(sms: List<SMS>) {
        val user = SessionDataProvider.getCurrentUser() ?: return
        val smsMap = getSmsMapFromPreference().apply {
            val pendingSms = (this[user] ?: arrayListOf()).apply { removeAll(sms) }
            put(user, pendingSms)
        }
        getSMSPreferences().edit().putString(KEY_PENDING_SMS, gson.toJson(smsMap, smsMapTypeToken)).apply()
    }

    override fun saveAssociationsToStorage(association: List<Association>) {
        val user = SessionDataProvider.getCurrentUser() ?: return
        val associationsMap = getAssociationMapFromPreference().apply {
            put(user, association)
        }
        getSMSPreferences().edit().putString(KEY_ASSOCIATIONS, gson.toJson(associationsMap)).apply()
    }

    override fun deleteAssociationsFromStorage(association: List<Association>) {
        val user = SessionDataProvider.getCurrentUser() ?: return
        val associationsMap = getAssociationMapFromPreference().apply {
            remove(user, association)
        }
        getSMSPreferences().edit().putString(KEY_ASSOCIATIONS, gson.toJson(associationsMap, associationsMapTypeToken)).apply()
    }

    override fun getAssociationsFromStorage(): List<Association> =
            getAssociationMapFromPreference()[SessionDataProvider.getCurrentUser()] ?: listOf()

    override fun getShouldProcessReceivedSms(): Boolean = getSMSPreferences().getBoolean(KEY_SHOULD_PROCESS_SMS, false)

    override fun setShouldProcessReceivedSms(shouldProcess: Boolean) =
            getSMSPreferences().edit().putBoolean(KEY_SHOULD_PROCESS_SMS, shouldProcess).apply()

    private fun getSMSPreferences(): SharedPreferences =
            BookkeeperApp.getContext().getSharedPreferences(SMS_PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)

    private fun getSmsMapFromPreference(): HashMap<String, ArrayList<SMS>> =
            gson.fromJson(getSMSPreferences().getString(KEY_PENDING_SMS, null), smsMapTypeToken) ?: hashMapOf()

    private fun getAssociationMapFromPreference(): HashMap<String, List<Association>> =
            gson.fromJson(getSMSPreferences().getString(KEY_ASSOCIATIONS, null), associationsMapTypeToken)
                    ?: hashMapOf()
}