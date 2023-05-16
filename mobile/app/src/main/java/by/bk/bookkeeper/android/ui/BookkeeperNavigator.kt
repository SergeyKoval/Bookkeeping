package by.bk.bookkeeper.android.ui

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import by.bk.bookkeeper.android.R
import by.bk.bookkeeper.android.sms.Conversation
import by.bk.bookkeeper.android.ui.accounts.AccountsFragment
import by.bk.bookkeeper.android.ui.association.AccountInfoHolder
import by.bk.bookkeeper.android.ui.association.AssociationType
import by.bk.bookkeeper.android.ui.association.AssociationTypeFragment
import by.bk.bookkeeper.android.ui.association.PushAssociationFragment
import by.bk.bookkeeper.android.ui.association.SMSAssociationFragment
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

    override fun showAssociationTypeFragment(accountInfoHolder: AccountInfoHolder) {
        replaceFragment(AssociationTypeFragment.newInstance(accountInfoHolder) as BaseFragment, true)
    }

    override fun showAssociationFragment(type: AssociationType, accountInfoHolder: AccountInfoHolder) {
        val fragment = when (type) {
            AssociationType.SMS -> SMSAssociationFragment.newInstance(accountInfoHolder)
            AssociationType.PUSH -> PushAssociationFragment.newInstance(accountInfoHolder)
        }
        replaceFragment(fragment, true)
    }

    override fun showSmsListFragment(conversation: Conversation, accountInfoHolder: AccountInfoHolder) {
        replaceFragment(SMSListFragment.newInstance(conversation, accountInfoHolder) as BaseFragment, true)
    }

    private fun replaceFragment(fragment: BaseFragment, addToBackStack: Boolean = false) {
        activity.supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment, fragment.getFragmentTag())
                .also { if (addToBackStack) it.addToBackStack(fragment.getFragmentTag()) }
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