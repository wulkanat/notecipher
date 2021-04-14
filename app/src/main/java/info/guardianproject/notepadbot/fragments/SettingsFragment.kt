package info.guardianproject.notepadbot.fragments

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.NumberPicker
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import info.guardianproject.notepadbot.App
import info.guardianproject.notepadbot.NConstants
import info.guardianproject.notepadbot.R
import info.guardianproject.notepadbot.cacheword.CacheWordActivityHandler
import info.guardianproject.notepadbot.cacheword.Constants
import info.guardianproject.notepadbot.cacheword.PassphraseSecrets
import java.io.IOException

class SettingsFragment : PreferenceFragmentCompat() {
    private val activity: Activity by lazy { requireActivity() }
    private val mCacheWord by lazy {
        CacheWordActivityHandler(activity, (activity.application as App).cWSettings)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)

        findPreference<Preference>(Constants.SHARED_PREFS_TIMEOUT_SECONDS)!!
            .setOnPreferenceClickListener {
                changeTimeoutPrompt()
                true
            }
        findPreference<Preference>(Constants.SHARED_PREFS_VIBRATE)!!
            .setOnPreferenceChangeListener { _, newValue ->
                mCacheWord.vibrateSetting = (newValue as Boolean)
                true
            }
        findPreference<Preference>(Constants.SHARED_PREFS_SECRETS)!!
            .setOnPreferenceChangeListener { _, newValue ->
                try {
                    val pass = (newValue as String).toCharArray()
                    if (NConstants.validatePassword(pass)) {
                        mCacheWord.changePassphrase(
                            mCacheWord.cachedSecrets as PassphraseSecrets,
                            pass
                        )
                    } else {
                        Toast.makeText(activity, R.string.pass_err_length, Toast.LENGTH_SHORT)
                            .show()
                    }
                } catch (e: IOException) {
                    Toast.makeText(activity, R.string.pass_err, Toast.LENGTH_SHORT).show()
                }
                false
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = super.onCreateView(inflater, container, savedInstanceState)?.apply {
        ViewCompat.setOnApplyWindowInsetsListener(this@apply) { v, insets ->
            val systemBarInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBarInsets.left, systemBarInsets.top,
                         systemBarInsets.right, systemBarInsets.bottom)

            insets
        }
    }

    private fun changeTimeoutPrompt() {
        if (mCacheWord.isLocked) return

        val input = NumberPicker(activity).apply {
            minValue = 1
            maxValue = 60
            value = mCacheWord.timeoutSeconds
        }
        AlertDialog.Builder(activity)
            .setTitle(R.string.change_timeout_prompt_title)
            .setMessage(R.string.change_timeout_prompt)
            .setView(input)
            .setPositiveButton("OK") { dialog: DialogInterface, _: Int ->
                val timeout = input.value
                mCacheWord.timeoutSeconds = timeout
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog: DialogInterface, _: Int -> dialog.cancel() }
            .show()
    }

    override fun onPause() {
        super.onPause()
        mCacheWord.onPause()
    }

    override fun onResume() {
        super.onResume()
        mCacheWord.onResume()
    }
}