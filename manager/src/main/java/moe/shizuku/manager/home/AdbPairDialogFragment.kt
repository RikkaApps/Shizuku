package moe.shizuku.manager.home

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import moe.shizuku.manager.R
import moe.shizuku.manager.databinding.AdbPairDialogBinding

class AdbPairDialogFragment : DialogFragment() {

    private lateinit var binding: AdbPairDialogBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = requireContext()
        binding = AdbPairDialogBinding.inflate(LayoutInflater.from(context))

        val builder = AlertDialog.Builder(context).apply {
            setTitle(R.string.dialog_adb_pairing_title)
            setView(binding.root)
            setNegativeButton(android.R.string.cancel, null)
            setPositiveButton(android.R.string.ok, null)
        }
        val dialog = builder.create()
        dialog.setOnShowListener { onDialogShow(dialog) }

        return dialog
    }

    private fun onDialogShow(dialog: AlertDialog) {
        binding.port.editText!!.doAfterTextChanged {
            binding.port.error = null
        }

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val context = it.context
            val port = binding.port.editText!!.text.toString().toInt()
            if (port > 65535) {
                binding.port.error = context.getString(R.string.dialog_adb_invalid_port)
                return@setOnClickListener
            }
        }
    }

    fun show(fragmentManager: FragmentManager) {
        if (fragmentManager.isStateSaved) return
        show(fragmentManager, javaClass.simpleName)
    }
}