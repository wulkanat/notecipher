package info.guardianproject.notepadbot

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI.setupActionBarWithNavController
import androidx.navigation.ui.onNavDestinationSelected
import com.google.android.material.bottomappbar.BottomAppBar
import info.guardianproject.notepadbot.cacheword.CacheWordActivityHandler
import info.guardianproject.notepadbot.cacheword.CacheWordSettings
import info.guardianproject.notepadbot.cacheword.ICacheWordSubscriber

class MainActivity : AppCompatActivity(R.layout.main_activity), ICacheWordSubscriber {
    private val bottomAppBar by lazy { findViewById<BottomAppBar>(R.id.bottom_app_bar) }
    private val navHostFragment: NavHostFragment by lazy {
        supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
    }
    private val navController by lazy { NavHostFragment.findNavController(navHostFragment) }

    val cacheWordSettings by lazy { CacheWordSettings(applicationContext) }
    val cacheWord by lazy { CacheWordActivityHandler(this, cacheWordSettings) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Apply the Google PRNG fixes to properly seed SecureRandom
        PRNGFixes.apply()

        setSupportActionBar(bottomAppBar)
        setupActionBarWithNavController(this, navController)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        ViewCompat.setOnApplyWindowInsetsListener(bottomAppBar) { v, insets ->
            val systemBarInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(bottom = systemBarInsets.bottom)
            insets
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return item.onNavDestinationSelected(navController) || super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onCacheWordUninitialized() {
        // noop
    }

    override fun onCacheWordLocked() {
        // noop
    }

    override fun onCacheWordOpened() {
        // noop
    }

    override fun onPause() {
        super.onPause()
        cacheWord.onPause()
    }

    override fun onResume() {
        super.onResume()
        cacheWord.onResume()
    }
}