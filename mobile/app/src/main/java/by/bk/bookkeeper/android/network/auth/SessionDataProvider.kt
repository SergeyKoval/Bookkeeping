package by.bk.bookkeeper.android.network.auth

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.provider.Settings
import by.bk.bookkeeper.android.BookkeeperApp
import com.google.gson.Gson
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import java.util.*


/**
 *  Created by Evgenia Grinkevich on 29, January, 2020
 **/

object SessionDataProvider : ISessionDataProvider {

    private const val SESSION_DATA_PREFERENCE_FILE_KEY = "by.bk.bookkeeper.data"
    private const val KEY_SESSION_DATA = "session_data"
    private const val KEY_DEVICE_ID = "device_id"

    private val dataPrefSubject = BehaviorSubject.createDefault(getSessionPreferences())
    private val dataPrefChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
        when (key) {
            KEY_SESSION_DATA -> dataPrefSubject.onNext(sharedPreferences)
        }
    }

    init {
        getSessionPreferences().registerOnSharedPreferenceChangeListener(dataPrefChangeListener)
    }

    override fun getCurrentSessionDataObservable(): Observable<Result<SessionData>> = dataPrefSubject.map {
        getCurrentSessionData().let { data ->
            if (data != null) Result.success(data) else Result.failure(Throwable("Session data does not exist"))
        }
    }

    override fun getCurrentSessionData(): SessionData? =
            Gson().fromJson(getSessionPreferences().getString(KEY_SESSION_DATA, null), SessionData::class.java)

    @SuppressLint("HardwareIds")
    override fun getDeviceId(): String {
        var deviceId = getSessionPreferences().getString(KEY_DEVICE_ID, null)
        if (deviceId == null) {
            deviceId = Settings.Secure.getString(BookkeeperApp.getContext().contentResolver,
                    Settings.Secure.ANDROID_ID).also { ssuid ->
                Timber.d("Retrieving SSUID: $ssuid ")
            } ?: UUID.randomUUID().toString().also {
                Timber.d("Unable to retrieve SSUID, generating random UUID: $it ")
            }
            getSessionPreferences().edit().putString(KEY_DEVICE_ID, deviceId).apply()
        }
        return deviceId
    }

    override fun saveSessionData(sessionData: SessionData) =
            getSessionPreferences().edit().putString(KEY_SESSION_DATA, Gson().toJson(sessionData)).apply()

    override fun clearSessionData() = getSessionPreferences().edit().remove(KEY_SESSION_DATA).apply()

    private fun getSessionPreferences(): SharedPreferences =
            BookkeeperApp.getContext().getSharedPreferences(SESSION_DATA_PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)

}