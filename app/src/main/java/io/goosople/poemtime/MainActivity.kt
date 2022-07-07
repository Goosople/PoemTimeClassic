package io.goosople.poemtime

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuItemCompat.getActionView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import com.google.android.material.navigation.NavigationView
import io.goosople.poemtime.databinding.ActivityMainBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

const val EXTRA_FRAGMENT = "io.goosople.poemtime.EXTRA_FRAGMENT"

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.poemFragment
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        if (navController.currentDestination?.id == R.id.nav_home || navController.currentDestination?.id == R.id.poemFragment) {
            menuInflater.inflate(R.menu.main, menu)
            if (sharedPreferences.getBoolean("online_service", true))
                binding.appBarMain.toolbar.menu.removeItem(R.id.app_bar_search)
            else {
                val searchView: SearchView =
                    getActionView(menu.findItem(R.id.app_bar_search)) as SearchView
                searchView.imeOptions = EditorInfo.IME_ACTION_SEARCH
                searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(s: String): Boolean {
                        Toast.makeText(
                            this@MainActivity,
                            "${getString(R.string.search)} ${getString(R.string.start)}",
                            Toast.LENGTH_SHORT
                        ).show()
                        GlobalScope.launch {
                            val num = PoemTimeUtils.search(s, resources)
                            if (num != -1) {
                                with(
                                    PreferenceManager.getDefaultSharedPreferences(this@MainActivity)
                                        .edit()
                                ) {
                                    putInt("poemNum", num)
                                    commit()
                                }
                            }
                        }
                        Toast.makeText(
                            this@MainActivity,
                            "${getString(R.string.search)} ${getString(R.string.end)}",
                            Toast.LENGTH_SHORT
                        ).show()
                        return false
                    }

                    override fun onQueryTextChange(s: String): Boolean {
                        return false
                    }
                })
            }
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.fullscreen -> {
            val navController = findNavController(R.id.nav_host_fragment_content_main)
            val intent = Intent(this, FullscreenActivity::class.java).apply {
                navController.currentDestination?.let { putExtra(EXTRA_FRAGMENT, it.id) }
            }
            startActivity(intent)
            true
        }

        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}