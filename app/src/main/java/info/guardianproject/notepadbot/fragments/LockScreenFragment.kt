package info.guardianproject.notepadbot.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import info.guardianproject.notepadbot.R
import info.guardianproject.notepadbot.databinding.LockscreenFragmentBinding

class LockScreenFragment : Fragment(R.layout.lockscreen_fragment) {
    private var _binding: LockscreenFragmentBinding? = null
    private val binding get() = _binding!!
    private val authenticator by lazy { BiometricAuth(this) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = LockscreenFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.passwordInput.editText?.setOnEditorActionListener { _, actionId, _ ->
            when (actionId) {
                EditorInfo.IME_ACTION_DONE -> {
                    unlock()
                    true
                }
                else -> false
            }
        }

        binding.continueWithoutEncryptionButton.setOnClickListener {
            LockScreenFragmentDirections.actionLockScreenFragmentToSettingsFragment().let {
                findNavController().navigate(it)
            }
        }

        binding.unlockButton.setOnClickListener { unlock() }

        if (!authenticator.canUseBiometric()) {
            binding.useBiometricButton.isEnabled = false
            binding.useBiometricButton.visibility = View.VISIBLE
        }
    }

    private fun unlock() {
        binding.unlockButton.isEnabled = false
        binding.useBiometricButton.isEnabled = false

        authenticator.authenticate(binding.passwordInput.editText?.text.toString(),
        onFail = {
            binding.passwordInput.error = "Wrong Password"
            binding.unlockButton.isEnabled = true
            binding.useBiometricButton.isEnabled = authenticator.canUseBiometric()
        })
    }
}