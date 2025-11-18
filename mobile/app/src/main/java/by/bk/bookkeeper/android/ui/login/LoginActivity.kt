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
import by.bk.bookkeeper.android.databinding.ActivityLoginRootBinding
import by.bk.bookkeeper.android.network.wrapper.DataStatus
import by.bk.bookkeeper.android.network.wrapper.InputValidationError
import by.bk.bookkeeper.android.network.wrapper.InputValidationWrapper
import by.bk.bookkeeper.android.ui.BaseActivity
import by.bk.bookkeeper.android.ui.home.AccountingActivity
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.widget.textChanges
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.*
import java.util.concurrent.TimeUnit

/**
 *  Created by Evgenia Grinkevich on 29, January, 2020
 **/

class LoginActivity : BaseActivity<LoginViewModel>() {

    private lateinit var binding: ActivityLoginRootBinding
    private var shouldShowSplash: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginRootBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.textViewCopyright.text =
                String.format(getString(R.string.str_copyright), Calendar.getInstance().get(Calendar.YEAR))
        binding.textViewVersion.text = String.format(getString(R.string.str_version), getBuildConfigAppVersion())
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
                            binding.progressLogin.visibility = if (status is DataStatus.Loading) View.VISIBLE else View.GONE
                            binding.textViewError.visibility = if (status is DataStatus.Error) View.VISIBLE else View.GONE
                            binding.buttonLogin.isEnabled = status !is DataStatus.Loading
                            when (status) {
                                is DataStatus.Success -> {
                                    startActivity(AccountingActivity.getStartIntent(this@LoginActivity))
                                }
                                is DataStatus.Error -> {
                                    binding.textViewError.text = status.failure.serverErrorMessage
                                            ?: getString(status.failure.messageStringRes)
                                }
                                else -> {
                                    // Handle Empty and Loading states
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
                binding.buttonLogin.clicks().subscribe {
                    viewModel.login(binding.editTextEmail.text.toString(), binding.editTextPassword.text.toString())
                },
                binding.editTextEmail.textChanges()
                        .skipInitialValue()
                        .subscribe { binding.textInputEmail.error = null },
                binding.editTextPassword.textChanges()
                        .skipInitialValue()
                        .subscribe { binding.textInputPassword.error = null }
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
                binding.textInputEmail.error = errorMessage
                binding.editTextEmail.requestFocus()
            }
            is InputValidationError.EmptyPassword -> {
                binding.textInputPassword.error = errorMessage
                binding.editTextPassword.requestFocus()
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
        TransitionManager.beginDelayedTransition(binding.constraintLayoutRoot)
        constraintSetLoginFields.applyTo(binding.constraintLayoutRoot)
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