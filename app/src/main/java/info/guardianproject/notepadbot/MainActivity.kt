package info.guardianproject.notepadbot

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI.setupActionBarWithNavController
import androidx.navigation.ui.onNavDestinationSelected
import info.guardianproject.notepadbot.cacheword.CacheWordActivityHandler
import info.guardianproject.notepadbot.cacheword.CacheWordSettings
import info.guardianproject.notepadbot.cacheword.ICacheWordSubscriber
import info.guardianproject.notepadbot.databinding.MainActivityBinding

class MainActivity : AppCompatActivity(), ICacheWordSubscriber {
    private var _binding: MainActivityBinding? = null
    private val binding get() = _binding!!

    private var _cacheWord: CacheWordActivityHandler? = null
    val cacheWord get() = _cacheWord!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = MainActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        _cacheWord = CacheWordActivityHandler(this, CacheWordSettings(applicationContext))

        // Apply the Google PRNG fixes to properly seed SecureRandom
        PRNGFixes.apply()

        setSupportActionBar(binding.bottomAppBar)
        val navController = findNavController()
        setupActionBarWithNavController(this, navController)
        // get around some weird behavior
        navController.navigate(R.id.lockScreenFragment)
        binding.bottomAppBar.visibility = View.GONE
        binding.fab.hide()

        WindowCompat.setDecorFitsSystemWindows(window, false)
        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomAppBar) { v, insets ->
            val systemBarInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(bottom = systemBarInsets.bottom)
            insets
        }

        navController.addOnDestinationChangedListener { controller, destination, _ ->
            when (destination.id) {
                R.id.lockScreenFragment, R.id.setupFragment -> {
                    binding.bottomAppBar.performHide()
                    if (!cacheWord.isLocked) cacheWord.manuallyLock()
                }
                else -> {
                    binding.bottomAppBar.visibility = View.VISIBLE
                    binding.bottomAppBar.performShow()
                }
            }
            when (destination.id) {
                R.id.notesFragment -> binding.fab.show()
                else -> binding.fab.hide()
            }
        }
    }

    private fun findNavController(): NavController {
        val navHostFragment: NavHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        return NavHostFragment.findNavController(navHostFragment)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return item.onNavDestinationSelected(findNavController()) || super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        return findNavController().navigateUp() || super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        if (findNavController().currentBackStackEntry?.destination?.id == R.id.lockScreenFragment) {
            finish()
        } else {
            super.onBackPressed()
        }
    }

    override fun onCacheWordUninitialized() {
        findNavController().navigate(R.id.setupFragment)
    }

    override fun onCacheWordLocked() {
        findNavController().navigate(R.id.lockScreenFragment)
    }

    override fun onCacheWordOpened() {
        findNavController().apply {
            popBackStack(R.id.notesFragment, false)
        }
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