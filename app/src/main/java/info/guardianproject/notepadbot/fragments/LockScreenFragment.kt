package info.guardianproject.notepadbot.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.transition.MaterialSharedAxis
import info.guardianproject.notepadbot.MainActivity
import info.guardianproject.notepadbot.R
import info.guardianproject.notepadbot.databinding.LockscreenFragmentBinding

class LockScreenFragment : Fragment(R.layout.lockscreen_fragment) {
    private lateinit var _binding: LockscreenFragmentBinding
    private val binding get() = _binding
    private val authenticator by lazy { BiometricAuth(this) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = LockscreenFragmentBinding.inflate(inflater, container, false).also {
        _binding = it
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (activity as MainActivity).apply {
            binding.bottomAppBar.performHide()
            binding.fab.hide()

            this@LockScreenFragment.binding.continueWithoutEncryptionButton.setOnClickListener {
                lockScreenToSettings()
            }
        }

        binding.passwordInput.editText?.setOnEditorActionListener { _, actionId, _ ->
            when (actionId) {
                EditorInfo.IME_ACTION_DONE -> {
                    unlock()
                    true
                }
                else -> false
            }
        }

        binding.unlockButton.setOnClickListener { unlock() }
        binding.useBiometricButton.setOnClickListener { unlock(biometricOnly = true) }

        if (!authenticator.canUseBiometric()) {
            binding.useBiometricButton.isEnabled = false
            binding.useBiometricButton.visibility = View.VISIBLE
        }
    }

    private fun unlock(biometricOnly: Boolean = false) {
        binding.unlockButton.isEnabled = false
        binding.useBiometricButton.isEnabled = false

        authenticator.authenticate(
            if (biometricOnly) null else binding.passwordInput.editText?.text.toString(),
            onFail = {
                binding.passwordInput.error = "Wrong Password"
                binding.unlockButton.isEnabled = true
                binding.useBiometricButton.isEnabled = authenticator.canUseBiometric()
            })
    }
}