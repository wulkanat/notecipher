package info.guardianproject.notepadbot

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.*
import androidx.navigation.ui.onNavDestinationSelected
import androidx.transition.Transition
import com.google.android.material.transition.MaterialSharedAxis
import info.guardianproject.notepadbot.cacheword.CacheWordActivityHandler
import info.guardianproject.notepadbot.cacheword.CacheWordSettings
import info.guardianproject.notepadbot.cacheword.ICacheWordSubscriber
import info.guardianproject.notepadbot.cacheword.PRNGFixes
import info.guardianproject.notepadbot.databinding.MainActivityBinding
import info.guardianproject.notepadbot.fragments.LockScreenFragment
import info.guardianproject.notepadbot.fragments.NotesFragment

class MainActivity : AppCompatActivity(), ICacheWordSubscriber {
    private lateinit var _binding: MainActivityBinding
    private val binding get() = _binding

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

        supportFragmentManager.commit {
            add<LockScreenFragment>(R.id.nav_host_fragment)
        }
        // setupActionBarWithNavController(this, navController)
        // get around some weird behavior
        // navController.navigate(R.id.lockScreenFragment)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomAppBar) { v, insets ->
            val systemBarInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(bottom = systemBarInsets.bottom)
            insets
        }

        /*navController.addOnDestinationChangedListener { controller, destination, _ ->
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
        }*/
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        binding.bottomAppBar.performHide()
        binding.fab.hide()
    }

    /*private fun findNavController(): NavController {
        val navHostFragment: NavHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        return NavHostFragment.findNavController(navHostFragment)
    }*/

    override fun onOptionsItemSelected(item: MenuItem) = when(item.itemId) {
        R.id.lockScreenFragment -> true.also { cacheWord.manuallyLock() }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        return /*findNavController().navigateUp() || */super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        // Yeah. I really hate that solution, but it seems to be pretty reliable
        /*when (findNavController().currentBackStackEntry?.destination?.id) {
            R.id.lockScreenFragment, R.id.setupFragment -> finish()
            else -> super.onBackPressed()
        }*/
        super.onBackPressed()
    }

    override fun onCacheWordUninitialized() {
        // findNavController().navigate(R.id.setupFragment)
    }

    override fun onCacheWordLocked() = lock()
    override fun onCacheWordOpened() = unlock()

    fun lock() {
        val previousFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
        if (previousFragment is LockScreenFragment) return
        supportFragmentManager.commit {
            val nextFragment = LockScreenFragment()

            previousFragment!!.exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
            nextFragment.enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)

            replace(R.id.nav_host_fragment, nextFragment)
        }
        binding.bottomAppBar.performHide()
        binding.fab.hide()
    }

    fun unlock() {
        supportFragmentManager.commit {
            val previousFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
            val nextFragment = NotesFragment()

            previousFragment!!.exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
            nextFragment.enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)

            replace(R.id.nav_host_fragment, nextFragment)
        }
        binding.bottomAppBar.performShow()
        binding.fab.show()
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