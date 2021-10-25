package moe.shizuku.manager.home

import android.app.Dialog
import android.app.NotificationManager
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.getSystemService
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import moe.shizuku.manager.R
import moe.shizuku.manager.ktx.toHtml
import rikka.html.text.HtmlCompat

@RequiresApi(VERSION_CODES.R)
class AdbPairTutorialDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = requireContext()
        val enabled = context.getSystemService<NotificationManager>()?.areNotificationsEnabled()

        val builder = AlertDialog.Builder(context).apply {
            setTitle(R.string.adb_pairing_tutorial_title)
            setMessage(
                ((if (enabled == false) "<b>" + getString(R.string.adb_pairing_tutorial_content_enable_notification) + "</b><p>" else "") +
                        getString(R.string.adb_pairing_tutorial_content_start_service) + "<p>" +
                        getString(R.string.adb_pairing_tutorial_content_input_pairing_code) + "<p>" +
                        "<small>" + getString(R.string.adb_pairing_tutorial_content_bad_system) + "</small>").toHtml(
                    HtmlCompat.FROM_HTML_OPTION_TRIM_WHITESPACE
                )
            )
            setPositiveButton(android.R.string.ok, null)
            setNeutralButton(R.string.development_settings, null)
        }
        val dialog = builder.create()
        dialog.setCanceledOnTouchOutside(false)
        dialog.setOnShowListener { onDialogShow(dialog) }
        return dialog
    }

    private fun onDialogShow(dialog: AlertDialog) {
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener {
            val intent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            try {
                it.context.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
            }
        }
    }


    fun show(fragmentManager: FragmentManager) {
        if (fragmentManager.isStateSaved) return
        show(fragmentManager, javaClass.simpleName)
    }

    override fun getDialog(): AlertDialog? {
        return super.getDialog() as AlertDialog?
    }
}
