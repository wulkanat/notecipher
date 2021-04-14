package info.guardianproject.notepadbot.fragments

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import info.guardianproject.notepadbot.MainActivity
import java.nio.charset.Charset
import java.security.GeneralSecurityException
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

/**
 * Wrapper around the biometric authentication
 *
 * Also handles locking and unlocking of the notes section
 */
class BiometricAuth(private val fragment: Fragment) {
    private val masterKeyAlias = "MasterKey"
    private val androidKeyStore = "AndroidKeyStore"

    private val encryptedKeyStore = "encryptedKeyStore"

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
        KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, androidKeyStore).apply {
            init(keyGenParameterSpec)
            generateKey()
        }
    }

    fun canUseBiometric() = when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
        BiometricManager.BIOMETRIC_SUCCESS -> fragment.requireContext()
            .getSharedPreferences(encryptedKeyStore, MODE_PRIVATE)
            .getString(encryptedKeyStore, null) != null
        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> false // TODO: separate?
        else ->  false
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
            val encryptedPassword = fragment.requireContext()
                .getSharedPreferences(encryptedKeyStore, MODE_PRIVATE)
                .getString(encryptedKeyStore, null)
                ?: throw Exception("User tried to use Biometric auth without it being enabled")

            generateSecretKey(buildKey())
            // Exceptions are unhandled within this snippet.
            val cipher = getCipher()
            val secretKey = getSecretKey()
            cipher.init(Cipher.DECRYPT_MODE, secretKey)
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
                                encryptedPassword.toByteArray(Charset.defaultCharset())
                            ) ?: throw Exception("Decryption failed")

                            cacheWord.setPassphrase(CharArray(decryptedPassword.size) { decryptedPassword[it].toChar() } )
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
                    masterKeyAlias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
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
                            ).toString()
                            fragment.requireContext()
                                .getSharedPreferences(encryptedKeyStore, MODE_PRIVATE).edit {
                                    putString(encryptedKeyStore, encryptedPassword)
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
        masterKeyAlias,
        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
    ).setBlockModes(KeyProperties.BLOCK_MODE_CBC)
        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
        .setUserAuthenticationRequired(true)
        .setInvalidatedByBiometricEnrollment(true)
        .build()

    private fun getSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")

        keyStore.load(null)
        return keyStore.getKey(masterKeyAlias, null) as SecretKey
    }

    private fun getCipher() =
        Cipher.getInstance("${KeyProperties.KEY_ALGORITHM_AES}/${KeyProperties.BLOCK_MODE_CBC}/${KeyProperties.ENCRYPTION_PADDING_PKCS7}")

}