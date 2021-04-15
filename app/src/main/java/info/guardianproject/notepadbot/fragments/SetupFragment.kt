package info.guardianproject.notepadbot.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import info.guardianproject.notepadbot.R
import info.guardianproject.notepadbot.databinding.SetupFragmentBinding

class SetupFragment : Fragment(R.layout.setup_fragment) {
    private lateinit var _binding: SetupFragmentBinding
    private val binding get() = _binding
    private val authenticator by lazy { BiometricAuth(this) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = SetupFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.confirmPassword.editText?.setOnEditorActionListener { _, actionId, _ ->
            when (actionId) {
                EditorInfo.IME_ACTION_DONE -> {
                    createKey()
                    true
                }
                else -> false
            }
        }

        binding.continueWithoutEncryptionButton.setOnClickListener {
            TODO()
        }

        binding.createKey.setOnClickListener { createKey() }
        if (!this.authenticator.canSupportBiometric()) {
            binding.useBiometric.isEnabled = false
            binding.useBiometric.isChecked = false
        }
    }

    private fun createKey() {
        val createTextPassword = binding.createPassword.editText!!.text.toString()
        val confirmTextPassword = binding.confirmPassword.editText!!.text.toString()
        if (createTextPassword.length < 4) {
            binding.createPassword.error = "Choose a longer password"
            return
        } else if (createTextPassword != confirmTextPassword) {
            binding.createPassword.error = "Passwords do not match"
            return
        }

        binding.createKey.isEnabled = false
        binding.useBiometric.isEnabled = false

        authenticator.setPassword(
            createTextPassword,
            binding.useBiometric.isEnabled,
            onFail = {
                Toast.makeText(requireContext(), "Unknown error occurred", Toast.LENGTH_LONG)
                    .show()
                binding.createKey.isEnabled = true
                binding.useBiometric.isEnabled = authenticator.canUseBiometric()
            })
    }
}