package by.bk.bookkeeper.android.sms.preferences

import android.content.Context
import android.content.SharedPreferences
import by.bk.bookkeeper.android.BookkeeperApp
import by.bk.bookkeeper.android.network.auth.SessionDataProvider
import by.bk.bookkeeper.android.network.dto.SourceType
import by.bk.bookkeeper.android.network.request.ProcessedMessage
import by.bk.bookkeeper.android.network.response.UnprocessedCountResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject


object SharedPreferencesProvider : IMessagesPreferenceProvider {

    private const val SMS_PREFERENCE_FILE_KEY = "by.bk.bookkeeper.data.sms"
    private const val KEY_PENDING_SMS = "sms_requests"
    private const val KEY_SERVER_UNPROCESSED_COUNT_RESPONSE = "sms_count_response"
    private const val KEY_ASSOCIATIONS = "associations"
    private const val KEY_SHOULD_PROCESS_SMS = "should_process_sms"
    private const val KEY_DEBUG_PUSH_NOTIFICATIONS = "debug_push_notifications"
    private const val KEY_PUSH_PROCESSING_DELAY_SECONDS = "push_processing_delay_seconds"
    private const val DEFAULT_PUSH_DELAY_SECONDS = 5

    private val gson: Gson by lazy { Gson() }
    private val messagesMapTypeToken = object : TypeToken<HashMap<String, ArrayList<ProcessedMessage>>>() {}.type
    private val associationsMapTypeToken = object : TypeToken<HashMap<String, List<AssociationInfo>>>() {}.type

    private val dataPrefSubject = BehaviorSubject.createDefault(getSMSPreferences())
    private val dataPrefChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
        dataPrefSubject.onNext(sharedPreferences)
    }

    init {
        getSMSPreferences().registerOnSharedPreferenceChangeListener(dataPrefChangeListener)
    }

    override fun getPendingMessagesObservable(): Observable<List<ProcessedMessage>> = dataPrefSubject.map {
        getPendingMessagesFromStorage()
    }

    override fun getPendingMessagesMapObservable(): Observable<Map<SourceType, List<ProcessedMessage>>> = dataPrefSubject.map {
        getPendingMessagesFromStorage().groupBy { it.deviceMessage.source }
    }

    override fun getUnprocessedResponseObservable(): Observable<UnprocessedCountResponse> = dataPrefSubject.map {
        getUnprocessedResponseFromStorage()
    }

    override fun getPendingMessagesFromStorage(): List<ProcessedMessage> =
        getSmsMapFromPreference()[SessionDataProvider.getCurrentUser()] ?: listOf()

    override fun saveMessagesToStorage(sms: List<ProcessedMessage>) {
        val user = SessionDataProvider.getCurrentUser() ?: return
        val smsMap = getSmsMapFromPreference().apply {
            val pendingSms = (this[user] ?: arrayListOf()).apply { addAll(sms) }
            put(user, pendingSms)
        }
        getSMSPreferences().edit().putString(KEY_PENDING_SMS, gson.toJson(smsMap, messagesMapTypeToken)).apply()
    }

    override fun deleteMessagesFromStorage(sms: List<ProcessedMessage>) {
        val user = SessionDataProvider.getCurrentUser() ?: return
        val smsMap = getSmsMapFromPreference().apply {
            val pendingSms = (this[user] ?: arrayListOf()).apply { removeAll(sms) }
            put(user, pendingSms)
        }
        getSMSPreferences().edit().putString(KEY_PENDING_SMS, gson.toJson(smsMap, messagesMapTypeToken)).apply()
    }

    override fun saveAssociationsToStorage(association: List<AssociationInfo>) {
        val user = SessionDataProvider.getCurrentUser() ?: return
        val associationsMap = getAssociationMapFromPreference().apply {
            put(user, association)
        }
        getSMSPreferences().edit().putString(KEY_ASSOCIATIONS, gson.toJson(associationsMap)).apply()
    }

    override fun deleteAssociationsFromStorage(association: List<AssociationInfo>) {
        val user = SessionDataProvider.getCurrentUser() ?: return
        val associationsMap = getAssociationMapFromPreference().apply {
            remove(user, association)
        }
        getSMSPreferences().edit().putString(KEY_ASSOCIATIONS, gson.toJson(associationsMap, associationsMapTypeToken)).apply()
    }

    override fun getAssociationsFromStorage(): List<AssociationInfo> =
        getAssociationMapFromPreference()[SessionDataProvider.getCurrentUser()] ?: listOf()

    override fun getShouldProcessReceivedMessages(): Boolean = getSMSPreferences().getBoolean(KEY_SHOULD_PROCESS_SMS, false)

    override fun setShouldProcessReceivedMessages(shouldProcess: Boolean) =
        getSMSPreferences().edit().putBoolean(KEY_SHOULD_PROCESS_SMS, shouldProcess).apply()

    fun getDebugPushNotifications(): Boolean =
        getSMSPreferences().getBoolean(KEY_DEBUG_PUSH_NOTIFICATIONS, false)

    fun setDebugPushNotifications(enabled: Boolean) =
        getSMSPreferences().edit().putBoolean(KEY_DEBUG_PUSH_NOTIFICATIONS, enabled).apply()

    fun getPushProcessingDelaySeconds(): Int =
        getSMSPreferences().getInt(KEY_PUSH_PROCESSING_DELAY_SECONDS, DEFAULT_PUSH_DELAY_SECONDS)

    fun setPushProcessingDelaySeconds(seconds: Int) =
        getSMSPreferences().edit().putInt(KEY_PUSH_PROCESSING_DELAY_SECONDS, seconds).apply()

    override fun saveUnprocessedResponseToStorage(response: UnprocessedCountResponse) {
        getSMSPreferences().edit().putString(KEY_SERVER_UNPROCESSED_COUNT_RESPONSE, gson.toJson(response)).apply()
    }

    override fun getUnprocessedResponseFromStorage(): UnprocessedCountResponse =
        getSMSPreferences().getString(KEY_SERVER_UNPROCESSED_COUNT_RESPONSE, null)?.let {
            gson.fromJson(it, UnprocessedCountResponse::class.java)
        } ?: UnprocessedCountResponse()

    private fun getSMSPreferences(): SharedPreferences =
        BookkeeperApp.getContext().getSharedPreferences(SMS_PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)

    private fun getSmsMapFromPreference(): HashMap<String, ArrayList<ProcessedMessage>> =
        gson.fromJson(getSMSPreferences().getString(KEY_PENDING_SMS, null), messagesMapTypeToken)
            ?: hashMapOf()

    private fun getAssociationMapFromPreference(): HashMap<String, List<AssociationInfo>> =
        gson.fromJson(getSMSPreferences().getString(KEY_ASSOCIATIONS, null), associationsMapTypeToken)
            ?: hashMapOf()
}