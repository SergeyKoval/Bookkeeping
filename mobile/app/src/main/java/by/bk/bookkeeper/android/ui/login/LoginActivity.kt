package by.bk.bookkeeper.android.ui.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.transition.TransitionManager
import android.view.View
import androidx.constraintlayout.widget.ConstraintSet
import by.bk.bookkeeper.android.BuildConfig
import by.bk.bookkeeper.android.R
import by.bk.bookkeeper.android.network.wrapper.DataStatus
import by.bk.bookkeeper.android.network.wrapper.InputValidationError
import by.bk.bookkeeper.android.network.wrapper.InputValidationWrapper
import by.bk.bookkeeper.android.ui.BaseActivity
import by.bk.bookkeeper.android.ui.home.AccountingActivity
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.widget.textChanges
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_login_root.*
import java.util.*
import java.util.concurrent.TimeUnit

/**
 *  Created by Evgenia Grinkevich on 29, January, 2020
 **/

class LoginActivity : BaseActivity<LoginViewModel>() {

    private var shouldShowSplash: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_root)
        text_view_copyright.text =
                String.format(getString(R.string.str_copyright), Calendar.getInstance().get(Calendar.YEAR))
        text_view_version.text = String.format(getString(R.string.str_version), getBuildConfigAppVersion())
        savedInstanceState?.getBoolean(KEY_SHOW_SPLASH_VIEW)?.let { showSplash ->
            if (!showSplash) performLoginViewsTransition()
        }
    }

    override fun onStart() {
        super.onStart()
        subscriptionsDisposable.addAll(
                viewModel.isAuthorized()
                        .observeOn(AndroidSchedulers.mainThread())
                        .take(1)
                        .subscribe { isAlreadyLoggedIn ->
                            shouldShowSplash = false
                            performScreensTransition(isAlreadyLoggedIn)
                        },
                viewModel.authRequestState()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { status ->
                            progress_login.visibility = if (status is DataStatus.Loading) View.VISIBLE else View.GONE
                            text_view_error.visibility = if (status is DataStatus.Error) View.VISIBLE else View.GONE
                            button_login.isEnabled = status !is DataStatus.Loading
                            when (status) {
                                is DataStatus.Success -> {
                                    startActivity(AccountingActivity.getStartIntent(this@LoginActivity))
                                }
                                is DataStatus.Error -> {
                                    text_view_error.text = status.failure.serverErrorMessage
                                            ?: getString(status.failure.messageStringRes)
                                }
                            }
                        },
                viewModel.inputValidation()
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { validationWrapper ->
                            (validationWrapper as? InputValidationWrapper.Invalid)?.let {
                                handleValidationErrors(it.validationError)
                            }
                        },
                button_login.clicks().subscribe {
                    viewModel.login(edit_text_email.text.toString(), edit_text_password.text.toString())
                },
                edit_text_email.textChanges()
                        .skipInitialValue()
                        .subscribe { text_input_email.error = null },
                edit_text_password.textChanges()
                        .skipInitialValue()
                        .subscribe { text_input_password.error = null }
        )
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_SHOW_SPLASH_VIEW, shouldShowSplash)
    }

    override fun getViewModelClass(): Class<LoginViewModel> = LoginViewModel::class.java

    private fun handleValidationErrors(validationError: InputValidationError) {
        val errorMessage: String = getString(validationError.messageStringRes)
        when (validationError) {
            is InputValidationError.InvalidEmail,
            is InputValidationError.EmptyEmail -> {
                text_input_email.error = errorMessage
                edit_text_email.requestFocus()
            }
            is InputValidationError.EmptyPassword -> {
                text_input_password.error = errorMessage
                edit_text_password.requestFocus()
            }
        }
    }

    private fun performScreensTransition(alreadyLoggedIn: Boolean) {
        Handler().postDelayed({
            if (alreadyLoggedIn) startActivity(AccountingActivity.getStartIntent(this@LoginActivity))
            else performLoginViewsTransition()
        }, TimeUnit.SECONDS.toMillis(1))
    }

    private fun performLoginViewsTransition() {
        val constraintSetLoginFields = ConstraintSet().apply {
            clone(this@LoginActivity, R.layout.activity_login_alternative)
        }
        TransitionManager.beginDelayedTransition(constraint_layout_root)
        constraintSetLoginFields.applyTo(constraint_layout_root)
    }

    private fun getBuildConfigAppVersion(): String = "${BuildConfig.VERSION_NAME}.${BuildConfig.VERSION_CODE}"

    companion object {

        private const val KEY_SHOW_SPLASH_VIEW = "should_show_splash"

        fun getStartIntent(fromPackageContext: Context): Intent =
                Intent(fromPackageContext, LoginActivity::class.java).also {
                    it.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                }
    }
}