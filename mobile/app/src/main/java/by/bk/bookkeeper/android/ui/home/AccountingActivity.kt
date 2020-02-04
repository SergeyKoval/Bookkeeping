package by.bk.bookkeeper.android.ui.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import by.bk.bookkeeper.android.R
import by.bk.bookkeeper.android.ui.BaseActivity
import by.bk.bookkeeper.android.ui.BookkeeperNavigation
import by.bk.bookkeeper.android.ui.BookkeeperNavigator
import by.bk.bookkeeper.android.ui.LogoutConfirmationDialog
import com.google.android.material.navigation.NavigationView
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_accounting.*

class AccountingActivity : BaseActivity<AccountingActivityViewModel>(),
        NavigationView.OnNavigationItemSelectedListener,
        BookkeeperNavigation.NavigatorProvider,
        LogoutConfirmationDialog.OnLogoutConfirmedListener {

    private var navMenuSelectedItemId: Int? = null
    private val navigator: BookkeeperNavigation.Navigator = BookkeeperNavigator(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_accounting)
        setSupportActionBar(toolbar)
        nav_view.setNavigationItemSelectedListener(this)
        drawer_layout.addDrawerListener(ActionBarDrawerToggle(
                this, drawer_layout, toolbar,
                R.string.content_description_navigation_drawer_open,
                R.string.content_description_navigation_drawer_close
        ).also {
            it.syncState()
        })
        savedInstanceState ?: onNavigationItemSelected(nav_view.menu.findItem(R.id.nav_accounts))
    }

    override fun onResume() {
        super.onResume()
        subscriptionsDisposable.addAll(
                viewModel.isSessionValid()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { sessionValid ->
                            if (!sessionValid) navigator.showLoginActivity()
                        }
        )
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_accounts -> {
                navigator.showAccountsFragment()
            }
            R.id.nav_status -> {
            }
            R.id.nav_logout -> {
                LogoutConfirmationDialog.show(this)
            }
        }
        navMenuSelectedItemId = item.itemId
        nav_view.setCheckedItem(item.itemId)
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onLogoutConfirmed() {
        viewModel.logout()
    }

    override fun getViewModelClass(): Class<AccountingActivityViewModel> = AccountingActivityViewModel::class.java

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        navMenuSelectedItemId?.let { outState.putInt(KEY_NAV_MENU_SELECTION, it) }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        navMenuSelectedItemId?.let { nav_view.setCheckedItem(it) }
    }

    override fun getNavigator(): BookkeeperNavigation.Navigator = navigator

    override fun onBackPressed() {
        when {
            supportFragmentManager.backStackEntryCount > 0 -> supportFragmentManager.popBackStack()
            drawer_layout.isDrawerOpen(GravityCompat.START) -> drawer_layout.closeDrawer(GravityCompat.START)
            else -> super.onBackPressed()
        }
    }

    companion object {

        private const val KEY_NAV_MENU_SELECTION = "key_nav_menu_selection"

        fun getStartIntent(fromPackageContext: Context): Intent =
                Intent(fromPackageContext, AccountingActivity::class.java).also {
                    it.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                }
    }

}
