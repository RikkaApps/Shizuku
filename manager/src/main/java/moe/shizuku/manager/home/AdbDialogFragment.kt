package moe.shizuku.manager.home

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import moe.shizuku.manager.R
import moe.shizuku.manager.databinding.AdbDialogBinding
import moe.shizuku.manager.starter.StarterActivity
import rikka.core.util.BuildUtils
import java.net.Inet4Address

class AdbDialogFragment : DialogFragment() {

    private lateinit var binding: AdbDialogBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = requireContext()
        binding = AdbDialogBinding.inflate(LayoutInflater.from(context))

        val builder = AlertDialog.Builder(context).apply {
            setTitle(R.string.dialog_adb_title)
            setView(binding.root)
            setNegativeButton(android.R.string.cancel, null)
            setPositiveButton(android.R.string.ok, null)
            if (BuildUtils.atLeast30) {
                setNeutralButton(R.string.adb_pairing, null)
            }
        }
        val dialog = builder.create()
        dialog.setCanceledOnTouchOutside(false)
        dialog.setOnShowListener { onDialogShow(dialog) }
        return dialog
    }

    private fun onDialogShow(dialog: AlertDialog) {
        binding.port.editText!!.doAfterTextChanged {
            binding.port.error = null
        }

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val context = it.context
            val port = try {
                binding.port.editText!!.text.toString().toInt()
            } catch (e: Exception) {
                -1
            }
            if (port > 65535 || port < 1) {
                binding.port.error = context.getString(R.string.dialog_adb_invalid_port)
                return@setOnClickListener
            }

            var host: String? = null
            runBlocking {
                GlobalScope.launch(Dispatchers.IO) {
                    host = Inet4Address.getLoopbackAddress().hostName
                }.join()
            }

            val intent = Intent(context, StarterActivity::class.java).apply {
                putExtra(StarterActivity.EXTRA_IS_ROOT, false)
                putExtra(StarterActivity.EXTRA_HOST, host)
                putExtra(StarterActivity.EXTRA_PORT, port)
            }
            context.startActivity(intent)

            dismiss()
        }

        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener {
            AdbPairDialogFragment().show(childFragmentManager)
        }
    }

    fun show(fragmentManager: FragmentManager) {
        if (fragmentManager.isStateSaved) return
        show(fragmentManager, javaClass.simpleName)
    }
}