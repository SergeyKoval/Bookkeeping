package by.bk.bookkeeper.android.ui

import androidx.appcompat.app.AppCompatActivity
import by.bk.bookkeeper.android.R
import by.bk.bookkeeper.android.ui.accounts.AccountsFragment
import by.bk.bookkeeper.android.ui.association.AccountInfoHolder
import by.bk.bookkeeper.android.ui.association.AssociationsFragment
import by.bk.bookkeeper.android.ui.association.SMSListFragment
import by.bk.bookkeeper.android.ui.login.LoginActivity

/**
 *  Created by Evgenia Grinkevich on 29, January, 2020
 **/

class BookkeeperNavigator(private val activity: AppCompatActivity) : BookkeeperNavigation.Navigator {

    override fun showAccountsFragment() {
        val fragment = activity.supportFragmentManager.findFragmentByTag(AccountsFragment.TAG)
                ?: AccountsFragment.newInstance()
        replaceFragment(fragment as BaseFragment)
    }

    override fun showAssociationsFragment(accountInfoHolder: AccountInfoHolder) {
        val fragment = activity.supportFragmentManager.findFragmentByTag(AssociationsFragment.TAG)
                ?: AssociationsFragment.newInstance(accountInfoHolder)
        replaceFragment(fragment as BaseFragment, true)
    }

    override fun showSmsListFragment(threadId: Long, accountInfoHolder: AccountInfoHolder) {
        val fragment = activity.supportFragmentManager.findFragmentByTag(SMSListFragment.TAG)
                ?: SMSListFragment.newInstance(threadId, accountInfoHolder)
        replaceFragment(fragment as BaseFragment, true)
    }

    private fun replaceFragment(fragment: BaseFragment, addToBackStack: Boolean = false) {
        activity.supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment, fragment.getTAG())
                .also { if (addToBackStack) it.addToBackStack(fragment.getTAG()) }
                .commit()
    }

    override fun showLoginActivity() {
        activity.startActivity(LoginActivity.getStartIntent(activity))
    }

    override fun onBackPressed() {
        activity.onBackPressed()
    }
}