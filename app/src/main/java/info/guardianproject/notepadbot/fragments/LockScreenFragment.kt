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

    private fun generateSecretKey(keyGenParameterSpec: KeyGenParameterSpec) {
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore"
        )
        keyGenerator.init(keyGenParameterSpec)
        keyGenerator.generateKey()
    }

    private fun test() {
        generateSecretKey(
            KeyGenParameterSpec.Builder(
                "MasterKey",
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .setUserAuthenticationRequired(true)
                .setInvalidatedByBiometricEnrollment(true)
                .build()
        )

        val cipher = getCipher()
        val secretKey = getSecretKey()
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        biometricPrompt.authenticate(biometricPromptInfo, BiometricPrompt.CryptoObject(cipher))
    }

    private fun getSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")

        keyStore.load(null)
        return keyStore.getKey("MasterKey", null) as SecretKey
    }

    private fun getCipher() =
        Cipher.getInstance("${KeyProperties.KEY_ALGORITHM_AES}/${KeyProperties.BLOCK_MODE_CBC}/${KeyProperties.ENCRYPTION_PADDING_PKCS7}")

    private fun unlock(): Boolean {
        passwordText.error = "Invalid"

        return false
    }
}