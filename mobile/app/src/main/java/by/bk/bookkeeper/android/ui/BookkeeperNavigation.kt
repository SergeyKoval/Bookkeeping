package by.bk.bookkeeper.android.ui

/**
 *  Created by Evgenia Grinkevich on 31, January, 2020
 **/

interface BookkeeperNavigation {

    interface Navigator {
        fun showLoginActivity()
        fun showAccountsFragment()
        fun showAssociationsFragment()
        fun onBackPressed()
    }

    interface NavigatorProvider {
        fun getNavigator(): Navigator
    }
}