package info.guardianproject.notepadbot.fragments

import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import info.guardianproject.notepadbot.R
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

class LockScreenFragment : Fragment(R.layout.lockscreen_fragment) {
    private val passwordText by lazy { requireView().findViewById<TextInputLayout>(R.id.password_input) }
    private val settingsButton by lazy { requireView().findViewById<Button>(R.id.settings_button) }
    private val unlockButton by lazy { requireView().findViewById<Button>(R.id.unlock_button) }
    private val biometricButton by lazy { requireView().findViewById<Button>(R.id.use_biometric_button) }

    private val biometricManager by lazy { BiometricManager.from(requireContext()) }
    private val biometricPrompt =
        BiometricPrompt(this, ContextCompat.getMainExecutor(requireContext()),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Snackbar.make(requireView(), errString, Snackbar.LENGTH_LONG)
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Snackbar.make(requireView(), "Authentication failed", Snackbar.LENGTH_LONG)
                }
            })
    private val biometricPromptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Authenticate")
        .setSubtitle("Authenticate to unlock notes")
        .setNegativeButtonText("Nope")
        .setAllowedAuthenticators(BIOMETRIC_STRONG)
        .build()

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

        unlockButton.setOnClickListener { unlock() }

        when (biometricManager.canAuthenticate(BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                biometricButton.setOnClickListener {
                    biometricPrompt.authenticate(biometricPromptInfo)
                }
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                biometricButton.isEnabled = false
                // TODO: prompt enroll?
            }
            else -> biometricButton.isEnabled = false
        }

    }

    private fun unlock(): Boolean {
        passwordText.error = "Invalid"

        return false
    }
}