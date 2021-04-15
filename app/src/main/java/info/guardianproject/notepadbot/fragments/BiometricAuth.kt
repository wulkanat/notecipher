package info.guardianproject.notepadbot.fragments

import android.content.Context.MODE_PRIVATE
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import info.guardianproject.notepadbot.MainActivity
import java.nio.charset.Charset
import java.nio.charset.CharsetEncoder
import java.security.GeneralSecurityException
import java.security.KeyStore
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

/**
 * Wrapper around the biometric authentication
 *
 * Also handles locking and unlocking of the notes section
 */
class BiometricAuth(private val fragment: Fragment) {
    companion object {
        private const val MASTER_KEY_ALIAS = "MasterKey"
        private const val ANDROID_KEY_STORE = "AndroidKeyStore"

        private const val ENCRYPTED_KEY_STORE = "encryptedKeyStore"
        private const val ENCRYPTION_IV_STORE = "encryptionIvStore"
    }

    private val biometricManager by lazy { BiometricManager.from(fragment.requireContext()) }
    private val biometricPromptInfo by lazy {
        BiometricPrompt.PromptInfo.Builder()
            .setTitle("Authenticate")
            .setSubtitle("Authenticate to unlock notes")
            .setNegativeButtonText("Nope")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()
    }
    private val cacheWord by lazy { (fragment.activity as MainActivity).cacheWord }

    private fun generateSecretKey(keyGenParameterSpec: KeyGenParameterSpec) {
        KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE).apply {
            init(keyGenParameterSpec)
            generateKey()
        }
    }

    fun canUseBiometric() = canSupportBiometric() && fragment.requireContext()
        .getSharedPreferences(ENCRYPTED_KEY_STORE, MODE_PRIVATE)
        .getString(ENCRYPTED_KEY_STORE, null) != null

    fun canSupportBiometric() =
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> false // TODO: separate?
            else -> false
        }

    /**
     * Authenticate
     *
     * @param password leave blank to prompt biometric auth
     */
    fun authenticate(
        password: String?,
        onSuccess: () -> Unit = {},
        onFail: () -> Unit = {}
    ) {
        password ?: run {
            val sharedPreferences = fragment.requireContext()
                .getSharedPreferences(ENCRYPTED_KEY_STORE, MODE_PRIVATE)
            val encryptedPassword = sharedPreferences.getString(ENCRYPTED_KEY_STORE, null)
                ?: throw Exception("User tried to use Biometric auth without it being enabled")
            val encryptionIv = sharedPreferences.getString(ENCRYPTION_IV_STORE, null)?.let {
                Base64.getDecoder().decode(it)
            } ?: throw Exception("User tried to use Biometric auth without it being enabled")

            generateSecretKey(buildKey())
            // Exceptions are unhandled within this snippet.
            val cipher = getCipher()
            val secretKey = getSecretKey()
            cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(encryptionIv))
            val prompt =
                BiometricPrompt(fragment, ContextCompat.getMainExecutor(fragment.requireContext()),
                    object : BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationError(
                            errorCode: Int,
                            errString: CharSequence
                        ) {
                            super.onAuthenticationError(errorCode, errString)
                            onFail()
                        }

                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                            super.onAuthenticationSucceeded(result)

                            val decryptedPassword = result.cryptoObject?.cipher?.doFinal(
                                Base64.getDecoder().decode(encryptedPassword)
                            ) ?: throw Exception("Decryption failed")

                            cacheWord.setPassphrase(decryptedPassword.decodeToString().toCharArray())
                            onSuccess()
                        }

                        override fun onAuthenticationFailed() {
                            super.onAuthenticationFailed()
                            onFail()
                        }
                    })
            prompt.authenticate(
                biometricPromptInfo,
                BiometricPrompt.CryptoObject(cipher)
            )
            return
        }
        try {
            cacheWord.setPassphrase(password.toCharArray())
            onSuccess()
        } catch (e: GeneralSecurityException) {
            onFail()
        }
    }

    fun setupBiometricAuthentication(
        onSuccess: () -> Unit = {},
        onFail: () -> Unit = {}
    ) {
        /*AlertDialog.Builder(fragment.requireActivity()).apply {
            setTitle("Enter current password")
            val input = TextInputLayout(fragment.requireContext()).apply {
                editText?.apply {
                    inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD or InputType.TYPE_CLASS_TEXT
                }
                hint = "Current password"
            }
            setView(input)
            setPositiveButton("OK") { dialog: DialogInterface, _: Int ->
                setPassword(input.editText?.text.toString(), true, onSuccess, onFail)
            }
            setNegativeButton("Cancel") { dialog: DialogInterface, _: Int -> dialog.cancel() }
        }.create().show()*/
        SetupBiometricDialogFragment()
            .show(fragment.requireActivity().supportFragmentManager, "AddBioDialog")
    }

    fun deleteBiometricAuthentication() {
        fragment.requireContext()
            .getSharedPreferences(ENCRYPTED_KEY_STORE, MODE_PRIVATE).edit {
                remove(ENCRYPTED_KEY_STORE)
            }
    }

    /**
     * Set a new password
     */
    fun setPassword(
        password: String,
        userBiometric: Boolean = false,
        onSuccess: () -> Unit = {},
        onFail: () -> Unit = {}
    ) {
        if (userBiometric) {
            // https://developer.android.com/training/sign-in/biometric-auth#kotlin
            generateSecretKey(
                KeyGenParameterSpec.Builder(
                    MASTER_KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
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
            val iv = cipher.parameters.getParameterSpec(IvParameterSpec::class.java).iv

            val prompt =
                BiometricPrompt(fragment, ContextCompat.getMainExecutor(fragment.requireContext()),
                    object : BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationError(
                            errorCode: Int,
                            errString: CharSequence
                        ) {
                            super.onAuthenticationError(errorCode, errString)
                            onFail()
                        }

                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                            super.onAuthenticationSucceeded(result)

                            val encryptedPassword = result.cryptoObject?.cipher?.doFinal(
                                password.toByteArray(Charset.defaultCharset())
                            )
                            fragment.requireContext()
                                .getSharedPreferences(ENCRYPTED_KEY_STORE, MODE_PRIVATE).edit {
                                    putString(ENCRYPTED_KEY_STORE, Base64.getEncoder().encodeToString(encryptedPassword))
                                    putString(ENCRYPTION_IV_STORE, Base64.getEncoder().encodeToString(iv))
                                }

                            cacheWord.setPassphrase(password.toCharArray())
                            onSuccess()
                        }

                        override fun onAuthenticationFailed() {
                            super.onAuthenticationFailed()
                            onFail()
                        }
                    })

            prompt.authenticate(biometricPromptInfo, BiometricPrompt.CryptoObject(cipher))
        } else {
            cacheWord.setPassphrase(password.toCharArray())
        }
    }

    private fun buildKey() = KeyGenParameterSpec.Builder(
        MASTER_KEY_ALIAS,
        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
    ).setBlockModes(KeyProperties.BLOCK_MODE_CBC)
        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
        .setUserAuthenticationRequired(true)
        .setInvalidatedByBiometricEnrollment(true)
        .build()

    private fun getSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")

        keyStore.load(null)
        return keyStore.getKey(MASTER_KEY_ALIAS, null) as SecretKey
    }

    private fun getCipher() =
        Cipher.getInstance("${KeyProperties.KEY_ALGORITHM_AES}/${KeyProperties.BLOCK_MODE_CBC}/${KeyProperties.ENCRYPTION_PADDING_PKCS7}")
}