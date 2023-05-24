package by.bk.bookkeeper.android.ui.login

import android.util.Patterns
import by.bk.bookkeeper.android.network.BookkeeperService
import by.bk.bookkeeper.android.network.auth.SessionData
import by.bk.bookkeeper.android.network.auth.SessionDataProvider
import by.bk.bookkeeper.android.network.request.AuthRequest
import by.bk.bookkeeper.android.network.wrapper.DataStatus
import by.bk.bookkeeper.android.network.wrapper.FailureWrapper
import by.bk.bookkeeper.android.network.wrapper.InputValidationError
import by.bk.bookkeeper.android.network.wrapper.InputValidationWrapper
import by.bk.bookkeeper.android.sms.preferences.SharedPreferencesProvider
import by.bk.bookkeeper.android.ui.BaseViewModel
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import timber.log.Timber

/**
 *  Created by Evgenia Grinkevich on 29, January, 2020
 **/

class LoginViewModel(private val bkService: BookkeeperService) : BaseViewModel(),
    LoginInteraction.Inputs, LoginInteraction.Outputs {

    private val deviceId = SessionDataProvider.getDeviceId()

    private val authRequestState: PublishSubject<DataStatus> = PublishSubject.create()
    override fun authRequestState(): Observable<DataStatus> = authRequestState

    private val validation: BehaviorSubject<InputValidationWrapper> = BehaviorSubject.create()
    override fun inputValidation(): Observable<InputValidationWrapper> = validation

    override fun isAuthorized(): Observable<Boolean> = SessionDataProvider.getCurrentSessionDataObservable().map {
        it.isSuccess
    }

    override fun login(email: String?, password: String?) {
        if (!isValidCredentials(email, password)) return
        subscriptions.add(
            bkService.login(AuthRequest(deviceId, email!!, password!!))
                .subscribeOn(Schedulers.io())
                .doOnSubscribe {
                    authRequestState.onNext(DataStatus.Loading)
                }
                .subscribe({ tokenResponse ->
                    SessionDataProvider.saveSessionData(SessionData(token = tokenResponse.token))
                    SessionDataProvider.setCurrentUser(email)
                    SharedPreferencesProvider.setShouldProcessReceivedMessages(true)
                    authRequestState.onNext(DataStatus.Success)
                }, { error ->
                    authRequestState.onNext(DataStatus.Error(FailureWrapper.getFailureType(error)))
                    Timber.e(error)
                })
        )
    }


    private fun isValidCredentials(email: String?, password: String?): Boolean {
        if (email.isNullOrEmpty()) {
            validation.onNext(InputValidationWrapper.Invalid(InputValidationError.EmptyEmail()))
            return false
        }
        if (password.isNullOrEmpty()) {
            validation.onNext(InputValidationWrapper.Invalid(InputValidationError.EmptyPassword()))
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            validation.onNext(InputValidationWrapper.Invalid(InputValidationError.InvalidEmail()))
            return false
        }
        return true
    }

}