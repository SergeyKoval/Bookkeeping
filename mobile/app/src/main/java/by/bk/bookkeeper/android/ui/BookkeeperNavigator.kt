package by.bk.bookkeeper.android.ui

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import by.bk.bookkeeper.android.R
import by.bk.bookkeeper.android.sms.Conversation
import by.bk.bookkeeper.android.ui.accounts.AccountsFragment
import by.bk.bookkeeper.android.ui.association.AccountInfoHolder
import by.bk.bookkeeper.android.ui.association.ConversationsFragment
import by.bk.bookkeeper.android.ui.association.SMSListFragment
import by.bk.bookkeeper.android.ui.login.LoginActivity
import by.bk.bookkeeper.android.ui.status.PendingSMSFragment

/**
 *  Created by Evgenia Grinkevich on 29, January, 2020
 **/

class BookkeeperNavigator(private val activity: AppCompatActivity) : BookkeeperNavigation.Navigator {

    override fun showAccountsFragment() {
        val fragment = activity.supportFragmentManager.findFragmentByTag(AccountsFragment.TAG)
                ?: AccountsFragment.newInstance()
        replaceFragment(fragment as BaseFragment)
    }

    override fun showSmsStatusFragment() {
        val fragment = activity.supportFragmentManager.findFragmentByTag(PendingSMSFragment.TAG)
                ?: PendingSMSFragment.newInstance()
        replaceFragment(fragment as BaseFragment)
    }

    override fun showConversationsFragment(accountInfoHolder: AccountInfoHolder) {
        replaceFragment(ConversationsFragment.newInstance(accountInfoHolder) as BaseFragment, true)
    }

    override fun showSmsListFragment(conversation: Conversation, accountInfoHolder: AccountInfoHolder) {
        replaceFragment(SMSListFragment.newInstance(conversation, accountInfoHolder) as BaseFragment, true)
    }

    private fun replaceFragment(fragment: BaseFragment, addToBackStack: Boolean = false) {
        activity.supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment, fragment.getTAG())
                .also { if (addToBackStack) it.addToBackStack(fragment.getTAG()) }
                .commit()
    }

    override fun popBackStackToRoot() {
        activity.supportFragmentManager.popBackStack(activity.supportFragmentManager.getBackStackEntryAt(0).name, FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }

    override fun showLoginActivity() {
        activity.startActivity(LoginActivity.getStartIntent(activity))
    }

    override fun onBackPressed() {
        activity.onBackPressed()
    }
}