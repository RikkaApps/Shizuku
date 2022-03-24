package moe.shizuku.manager.home

import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.SystemProperties
import android.provider.Settings
import android.view.LayoutInflater
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.MutableLiveData
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import moe.shizuku.manager.R
import moe.shizuku.manager.adb.AdbMdns
import moe.shizuku.manager.databinding.AdbDialogBinding
import moe.shizuku.manager.starter.StarterActivity
import java.net.InetAddress

@RequiresApi(Build.VERSION_CODES.R)
class AdbDialogFragment : DialogFragment() {

    private lateinit var binding: AdbDialogBinding
    private lateinit var adbMdns: AdbMdns
    private val port = MutableLiveData<Int>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = requireContext()
        binding = AdbDialogBinding.inflate(LayoutInflater.from(context))
        adbMdns = AdbMdns(context, AdbMdns.TLS_CONNECT, port)

        var port = SystemProperties.getInt("service.adb.tcp.port", -1)
        if (port == -1) port = SystemProperties.getInt("persist.adb.tcp.port", -1)

        val builder = MaterialAlertDialogBuilder(context).apply {
            setTitle(R.string.dialog_adb_discovery)
            setView(binding.root)
            setNegativeButton(android.R.string.cancel, null)
            setPositiveButton(R.string.development_settings, null)

            if (port != -1) {
                setNeutralButton("$port", null)
            }
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

        dialog.getButton(AlertDialog.BUTTON_NEUTRAL)?.setOnClickListener {
            var port = SystemProperties.getInt("service.adb.tcp.port", -1)
            if (port == -1) port = SystemProperties.getInt("persist.adb.tcp.port", -1)
            startAndDismiss(port)
        }

        port.observe(this) {
            if (it > 65535 || it < 1) return@observe
            startAndDismiss(it)
        }
    }

    private fun startAndDismiss(port: Int) {
        val host = "127.0.0.1"
        val intent = Intent(context, StarterActivity::class.java).apply {
            putExtra(StarterActivity.EXTRA_IS_ROOT, false)
            putExtra(StarterActivity.EXTRA_HOST, host)
            putExtra(StarterActivity.EXTRA_PORT, port)
        }
        requireContext().startActivity(intent)

        dismissAllowingStateLoss()
    }

    fun show(fragmentManager: FragmentManager) {
        if (fragmentManager.isStateSaved) return
        show(fragmentManager, javaClass.simpleName)
    }
}
