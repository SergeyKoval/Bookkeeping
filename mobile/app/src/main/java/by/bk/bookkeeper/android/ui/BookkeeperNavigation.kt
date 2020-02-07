package by.bk.bookkeeper.android.ui

import by.bk.bookkeeper.android.sms.Conversation
import by.bk.bookkeeper.android.ui.association.AccountInfoHolder

/**
 *  Created by Evgenia Grinkevich on 31, January, 2020
 **/

interface BookkeeperNavigation {

    interface Navigator {
        fun popBackStackToRoot()
        fun showLoginActivity()
        fun showAccountsFragment()
        fun showSmsStatusFragment()
        fun showConversationsFragment(accountInfoHolder: AccountInfoHolder)
        fun showSmsListFragment(conversation: Conversation, accountInfoHolder: AccountInfoHolder)
        fun onBackPressed()
    }

    interface NavigatorProvider {
        fun getNavigator(): Navigator
    }
}