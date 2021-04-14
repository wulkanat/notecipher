package info.guardianproject.notepadbot.fragments

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputLayout
import info.guardianproject.notepadbot.R

class LockScreenFragment : Fragment(R.layout.lockscreen_fragment) {
    private val passwordText by lazy { requireView().findViewById<TextInputLayout>(R.id.password_input) }
    private val settingsButton by lazy { requireView().findViewById<Button>(R.id.continue_without_encryption_button) }
    private val unlockButton by lazy { requireView().findViewById<Button>(R.id.unlock_button) }
    private val biometricButton by lazy { requireView().findViewById<Button>(R.id.use_biometric_button) }
    private val biometricHint by lazy { requireView().findViewById<TextView>(R.id.biometric_hint) }
    private val authenticator by lazy { BiometricAuth(this) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        passwordText.editText?.setOnEditorActionListener { _, actionId, _ ->
            when (actionId) {
                EditorInfo.IME_ACTION_DONE -> {
                    unlock()
                    true
                }
                else -> false
            }
        }

        settingsButton.setOnClickListener {
            findNavController().navigate(R.id.settingsFragment)
        }

        unlockButton.setOnClickListener { unlock() }

        if (!authenticator.canUseBiometric()) {
            biometricButton.isEnabled = false
            biometricHint.visibility = View.VISIBLE
        }
    }

    private fun unlock() {
        unlockButton.isEnabled = false
        biometricButton.isEnabled = false

        authenticator.authenticate(passwordText.editText?.text.toString(),
            onFail = {
                passwordText.error = "Wrong Password"
                unlockButton.isEnabled = true
                biometricButton.isEnabled = authenticator.canUseBiometric()
            },
            onSuccess = { findNavController().navigateUp() })
    }
}