package info.guardianproject.notepadbot

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.NumberPicker
import android.widget.Toast
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import info.guardianproject.notepadbot.cacheword.CacheWordActivityHandler
import info.guardianproject.notepadbot.cacheword.Constants
import info.guardianproject.notepadbot.cacheword.PassphraseSecrets
import java.io.IOException

class SettingsFragment : PreferenceFragmentCompat() {
    private lateinit var mCacheWord: CacheWordActivityHandler
    private lateinit var activity: Activity

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)

        findPreference<Preference>(Constants.SHARED_PREFS_TIMEOUT_SECONDS)!!
            .onPreferenceClickListener = changeLockTimeoutListener
        findPreference<Preference>(Constants.SHARED_PREFS_VIBRATE)!!
            .onPreferenceChangeListener = vibrateChangeListener
        findPreference<Preference>(Constants.SHARED_PREFS_SECRETS)!!
            .onPreferenceChangeListener = passphraseChangeListener

        activity = requireActivity().also {
            mCacheWord = CacheWordActivityHandler(it, (it.application as App).cWSettings)
        }
    }

    private fun changeTimeoutPrompt() {
        if (mCacheWord.isLocked) {
            return
        }
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(R.string.change_timeout_prompt_title)
        builder.setMessage(R.string.change_timeout_prompt)
        val input = NumberPicker(activity)
        input.minValue = 1
        input.maxValue = 60
        input.value = mCacheWord.timeoutSeconds
        builder.setView(input)
        builder.setPositiveButton(
            "OK"
        ) { dialog: DialogInterface, _: Int ->
            val timeout = input.value
            mCacheWord.timeoutSeconds = timeout
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel") { dialog: DialogInterface, _: Int -> dialog.cancel() }
        builder.show()
    }

    private val changeLockTimeoutListener = Preference.OnPreferenceClickListener {
        changeTimeoutPrompt()
        true
    }

    private val vibrateChangeListener =
        Preference.OnPreferenceChangeListener { _, newValue -> // save option internally in cacheword as well
            mCacheWord.vibrateSetting = (newValue as Boolean)
            true
        }
    private val passphraseChangeListener =
        Preference.OnPreferenceChangeListener { _, newValue -> // save option internally in cacheword as well
            try {
                val pass = (newValue as String).toCharArray()
                if (NConstants.validatePassword(pass)) {
                    mCacheWord.changePassphrase(
                        mCacheWord.cachedSecrets as PassphraseSecrets,
                        pass
                    )
                } else {
                    Toast.makeText(activity, R.string.pass_err_length, Toast.LENGTH_SHORT).show()
                }
            } catch (e: IOException) {
                Toast.makeText(activity, R.string.pass_err, Toast.LENGTH_SHORT).show()
            }
            false
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