package moe.shizuku.manager.home

import android.app.Dialog
import android.app.NotificationManager
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import moe.shizuku.manager.R
import moe.shizuku.manager.adb.AdbPairingService
import moe.shizuku.manager.databinding.AdbPairTutorialDialogBinding

@RequiresApi(VERSION_CODES.R)
class AdbPairTutorialDialogFragment : DialogFragment() {

    private lateinit var binding: AdbPairTutorialDialogBinding

    private var notificationEnabled: Boolean = false

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = requireContext()

        binding = AdbPairTutorialDialogBinding.inflate(LayoutInflater.from(context))

        notificationEnabled = isNotificationEnabled()

        if (notificationEnabled) {
            context.startForegroundService(AdbPairingService.startIntent(context))
        }
        updateDialogView()

        val builder = AlertDialog.Builder(context).apply {
            setTitle(R.string.adb_pairing_tutorial_title)
            setView(binding.root)
            setPositiveButton(getButtonText(), null)
            setNegativeButton(android.R.string.cancel, null)
        }
        val dialog = builder.create()
        dialog.setCanceledOnTouchOutside(false)
        dialog.setOnShowListener { onDialogShow(dialog) }
        return dialog
    }

    private fun onDialogShow(dialog: AlertDialog) {
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val context = it.context

            if (notificationEnabled) {
                val intent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                try {
                    context.startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                }
                dismiss()
            } else {
                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                try {
                    context.startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                }
            }
        }
    }

    private fun getButtonText(): Int {
        return if (notificationEnabled) R.string.development_settings else R.string.notification_settings
    }

    private fun updateDialogView() {
        dialog?.getButton(AlertDialog.BUTTON_POSITIVE)?.setText(getButtonText())
        binding.text1.isVisible = notificationEnabled
        binding.text2.isGone = notificationEnabled
    }

    private fun isNotificationEnabled(): Boolean {
        val context = requireContext()

        val nm = context.getSystemService(NotificationManager::class.java)
        val channel = nm.getNotificationChannel(AdbPairingService.notificationChannel)
        return nm.areNotificationsEnabled() &&
                (channel == null || channel.importance != NotificationManager.IMPORTANCE_NONE)
    }

    override fun onResume() {
        super.onResume()

        val newNotificationEnabled = isNotificationEnabled()
        if (newNotificationEnabled != notificationEnabled) {
            notificationEnabled = newNotificationEnabled
            updateDialogView()

            if (newNotificationEnabled) {
                requireContext().startForegroundService(AdbPairingService.startIntent(requireContext()))
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
