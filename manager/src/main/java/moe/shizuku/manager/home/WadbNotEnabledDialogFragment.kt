package moe.shizuku.manager.home

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import moe.shizuku.manager.R
import moe.shizuku.manager.starter.StarterActivity
import java.net.InetAddress

class WadbNotEnabledDialogFragment :DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = requireContext()
        return AlertDialog.Builder(context)
                .setMessage(R.string.dialog_wireless_adb_not_enabled)
                .setPositiveButton(android.R.string.ok, null)
                .setNeutralButton(context.getString(R.string.dialog_adb_try, 5555)) { _, _ ->
                    val host = InetAddress.getLoopbackAddress().hostName
                    val intent = Intent(context, StarterActivity::class.java).apply {
                        putExtra(StarterActivity.EXTRA_IS_ROOT, false)
                        putExtra(StarterActivity.EXTRA_HOST, host)
                        putExtra(StarterActivity.EXTRA_PORT, 5555)
                    }
                    context.startActivity(intent)
                }
                .create()
    }

    fun show(fragmentManager: FragmentManager) {
        if (fragmentManager.isStateSaved) return
        show(fragmentManager, javaClass.simpleName)
    }
}