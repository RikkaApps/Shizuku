package moe.shizuku.manager.home

import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.observe
import moe.shizuku.manager.R
import moe.shizuku.manager.adb.AdbMdns
import moe.shizuku.manager.databinding.AdbDialogBinding
import moe.shizuku.manager.starter.StarterActivity
import java.net.InetAddress

class AdbDialogFragment : DialogFragment() {

    private lateinit var binding: AdbDialogBinding
    private lateinit var adbMdns: AdbMdns
    private val port = MutableLiveData<Int>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = requireContext()
        binding = AdbDialogBinding.inflate(LayoutInflater.from(context))
        adbMdns = AdbMdns(context, AdbMdns.TLS_CONNECT, port)

        val builder = AlertDialog.Builder(context).apply {
            setTitle(R.string.dialog_adb_discovery)
            setView(binding.root)
            setNegativeButton(android.R.string.cancel, null)
            setPositiveButton(R.string.development_settings, null)
        }
        val dialog = builder.create()
        dialog.setCanceledOnTouchOutside(false)
        dialog.setOnShowListener { onDialogShow(dialog) }
        return dialog
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        adbMdns.stop()
    }

    private fun onDialogShow(dialog: AlertDialog) {
        adbMdns.start()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val intent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            try {
                it.context.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
            }
        }

        port.observe(this) {
            if (it > 65535 || it < 1) return@observe
            val host = InetAddress.getLoopbackAddress().hostName
            val intent = Intent(context, StarterActivity::class.java).apply {
                putExtra(StarterActivity.EXTRA_IS_ROOT, false)
                putExtra(StarterActivity.EXTRA_HOST, host)
                putExtra(StarterActivity.EXTRA_PORT, it)
            }
            requireContext().startActivity(intent)

            dismissAllowingStateLoss()
        }
    }

    fun show(fragmentManager: FragmentManager) {
        if (fragmentManager.isStateSaved) return
        show(fragmentManager, javaClass.simpleName)
    }
}