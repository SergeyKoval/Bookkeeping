package by.bk.bookkeeper.android.ui

import by.bk.bookkeeper.android.ui.association.AccountInfoHolder

/**
 *  Created by Evgenia Grinkevich on 31, January, 2020
 **/

interface BookkeeperNavigation {

    interface Navigator {
        fun showLoginActivity()
        fun showAccountsFragment()
        fun showAssociationsFragment(accountInfoHolder: AccountInfoHolder)
        fun showSmsListFragment(threadId: Long, accountInfoHolder: AccountInfoHolder)
        fun onBackPressed()
    }

    interface NavigatorProvider {
        fun getNavigator(): Navigator
    }
}