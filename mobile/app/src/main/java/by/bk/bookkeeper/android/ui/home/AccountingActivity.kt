package by.bk.bookkeeper.android.ui.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import by.bk.bookkeeper.android.R
import by.bk.bookkeeper.android.network.auth.SessionDataProvider
import by.bk.bookkeeper.android.ui.login.LoginActivity
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_accounting.*

class AccountingActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

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
    }

    override fun onBackPressed() {
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_accounting_toolbar_menu, menu)
        return true
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_logout -> {
                SessionDataProvider.clearSessionData()
                startActivity(LoginActivity.getStartIntent(this@AccountingActivity))
            }
        }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    companion object {

        fun getStartIntent(fromPackageContext: Context): Intent =
            Intent(fromPackageContext, AccountingActivity::class.java).also {
                it.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            }
    }

}
