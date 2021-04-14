package info.guardianproject.notepadbot

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import androidx.fragment.app.commit
import androidx.preference.PreferenceManager
import info.guardianproject.notepadbot.cacheword.ICacheWordSubscriber
import info.guardianproject.notepadbot.fragments.SettingsFragment

class Settings : AppCompatActivity(), ICacheWordSubscriber {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager.commit {
            replace(android.R.id.content, SettingsFragment())
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            NavUtils.navigateUpTo(this, Intent(this, NoteCipher::class.java))
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    public override fun onDestroy() {
        super.onDestroy()
    }

    override fun onCacheWordUninitialized() {
        Log.d(NConstants.TAG, "onCacheWordUninitialized")
        System.gc()
        showLockScreen()
    }

    override fun onCacheWordLocked() {
        Log.d(NConstants.TAG, "onCacheWordLocked")
        System.gc()
        showLockScreen()
    }

    override fun onCacheWordOpened() {
        Log.d(NConstants.TAG, "onCacheWordOpened")
    }

    private fun showLockScreen() {
        val intent = Intent(this, LockScreenActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        intent.putExtra("originalIntent", getIntent())
        startActivity(intent)
        finish()
    }

    companion object {
        const val LANG_SEL_KEY = "langSelected"
        fun getNoteLinesOption(context: Context): Boolean {
            val defValue = context.resources.getBoolean(R.bool.notecipher_uselines_default)
            return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(NConstants.SHARED_PREFS_NOTE_LINES, defValue)
        }
    }
}