package by.bk.bookkeeper.android

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import by.bk.bookkeeper.android.network.BookkeeperService
import by.bk.bookkeeper.android.ui.accounts.AccountsViewModel
import by.bk.bookkeeper.android.ui.association.InboxSmsViewModel
import by.bk.bookkeeper.android.ui.home.AccountingActivityViewModel
import by.bk.bookkeeper.android.ui.login.LoginViewModel


/**
 *  Created by Evgenia Grinkevich on 30, January, 2020
 **/

@Suppress("UNCHECKED_CAST")
class ViewModelFactory(private val bkService: BookkeeperService) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(LoginViewModel::class.java) -> LoginViewModel(bkService) as T
        modelClass.isAssignableFrom(AccountingActivityViewModel::class.java) -> AccountingActivityViewModel(bkService) as T
        modelClass.isAssignableFrom(AccountsViewModel::class.java) -> AccountsViewModel(bkService) as T
        modelClass.isAssignableFrom(InboxSmsViewModel::class.java) -> InboxSmsViewModel() as T
        else -> throw IllegalArgumentException("Cannot instantiate ${modelClass.canonicalName}")
    }
}

inline fun <reified T : ViewModel> Fragment.fragmentScopeViewModel(): Lazy<T> = lazy {
    ViewModelProvider(this, Injection.provideViewModelFactory()).get(T::class.java)
}

inline fun <reified T : ViewModel> Fragment.activityScopeViewModel(): Lazy<T> = lazy {
    ViewModelProvider(activity!!, Injection.provideViewModelFactory()).get(T::class.java)
}

inline fun <reified T : ViewModel> AppCompatActivity.activityScopeViewModel(): Lazy<T> = lazy {
    ViewModelProvider(this, Injection.provideViewModelFactory()).get(T::class.java)
}
