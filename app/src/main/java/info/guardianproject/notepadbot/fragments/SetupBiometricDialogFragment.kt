package info.guardianproject.notepadbot.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.textfield.TextInputLayout
import info.guardianproject.notepadbot.R
import info.guardianproject.notepadbot.databinding.SetupBiometricDialogFragmentBinding

class SetupBiometricDialogFragment : DialogFragment() {
    private val authenticator by lazy { BiometricAuth(this) }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater

            val view = inflater.inflate(R.layout.setup_biometric_dialog_fragment, null)
            val input = view.findViewById<TextInputLayout>(R.id.password_input)

            builder.setView(view).apply {
                setTitle("Input Current Password")
                setPositiveButton("OK", null)
                setNegativeButton("Cancel") { dialog: DialogInterface, _: Int -> dialog.cancel() }
            }.create().also { dialog ->
                dialog.setOnShowListener {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        authenticator.setPassword(input.editText?.text.toString(), true,
                        onSuccess = {
                            dialog.dismiss()
                        },
                        onFail = {
                            input.error = "Invalid Password"
                        })
                    }
                }
            }
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}