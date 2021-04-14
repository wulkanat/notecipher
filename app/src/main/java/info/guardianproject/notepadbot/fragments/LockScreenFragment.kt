package info.guardianproject.notepadbot.fragments

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputLayout
import info.guardianproject.notepadbot.R

class LockScreenFragment : Fragment(R.layout.lockscreen_fragment) {
    private val passwordText: TextInputLayout by lazy { requireView().findViewById(R.id.password_input) }
    private val settingsButton: Button by lazy { requireView().findViewById(R.id.settings_button) }
    private val unlockButton: Button by lazy { requireView().findViewById(R.id.unlock_button) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        passwordText.editText?.setOnEditorActionListener { _, actionId, _ ->
            when (actionId) {
                EditorInfo.IME_ACTION_DONE -> unlock()
                else -> false
            }
        }

        settingsButton.setOnClickListener {
            findNavController().navigate(R.id.settingsFragment)
        }
    }

    private fun unlock(): Boolean {
        passwordText.error = "Invalid"

        return false
    }
}