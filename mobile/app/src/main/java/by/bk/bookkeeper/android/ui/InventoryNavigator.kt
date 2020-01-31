package by.bk.bookkeeper.android.ui

import androidx.appcompat.app.AppCompatActivity
import by.bk.bookkeeper.android.R
import by.bk.bookkeeper.android.ui.accounts.AccountsFragment
import by.bk.bookkeeper.android.ui.login.LoginActivity

/**
 *  Created by Evgenia Grinkevich on 29, January, 2020
 **/

class BookkeeperNavigator(private val activity: AppCompatActivity) : BookkeeperNavigation.Navigator {

    override fun showContentFragment(fragmentTag: String) {
        val fragment = activity.supportFragmentManager.findFragmentByTag(fragmentTag)
            ?: when (fragmentTag) {
                AccountsFragment.TAG -> AccountsFragment.newInstance()
                else -> throw IllegalArgumentException("Unknown Fragment")
            }
        replaceFragment(fragment as BaseFragment)
    }

    private fun replaceFragment(fragment: BaseFragment) {
        activity.supportFragmentManager.beginTransaction()
            .replace(R.id.section_fragment_container, fragment, fragment.getTAG())
            .commit()
    }

    override fun showLoginActivity() {
        activity.startActivity(LoginActivity.getStartIntent(activity))
    }

    override fun onBackPressed() {
        activity.onBackPressed()
    }
}