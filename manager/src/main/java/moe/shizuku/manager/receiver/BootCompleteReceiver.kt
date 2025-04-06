package moe.shizuku.manager.receiver

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Process
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.topjohnwu.superuser.Shell
import moe.shizuku.manager.AppConstants
import moe.shizuku.manager.ShizukuSettings
import moe.shizuku.manager.ShizukuSettings.KEEP_START_ON_BOOT_WIRELESS
import moe.shizuku.manager.ShizukuSettings.LaunchMethod
import moe.shizuku.manager.ShizukuSettings.getPreferences
import moe.shizuku.manager.adb.WirelessADBHelper.validateThenEnableWirelessAdb
import moe.shizuku.manager.starter.Starter
import moe.shizuku.manager.starter.SelfStarterService
import rikka.shizuku.Shizuku

class BootCompleteReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_LOCKED_BOOT_COMPLETED != intent.action
                && Intent.ACTION_BOOT_COMPLETED != intent.action) {
            return
        }

        if (Process.myUid() / 100000 > 0) return

        // TODO Record if receiver is called
        if (ShizukuSettings.getLastLaunchMode() == LaunchMethod.ROOT) {
            Log.i(AppConstants.TAG, "start on boot, action=" + intent.action)
            if (Shizuku.pingBinder()) {
                Log.i(AppConstants.TAG, "service is running")
                return
            }
            start(context, false)
        } else if (ShizukuSettings.getLastLaunchMode() == LaunchMethod.ADB) {
            Log.i(AppConstants.TAG, "start on boot, action=" + intent.action)
            if (Shizuku.pingBinder()) {
                Log.i(AppConstants.TAG, "service is running")
                return
            }
            if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
                val startOnBootWirelessIsEnabled = getPreferences().getBoolean(KEEP_START_ON_BOOT_WIRELESS, false)
                start(context, startOnBootWirelessIsEnabled)
            } else return
        }
    }

    private fun start(context: Context, startOnBootWirelessIsEnabled: Boolean) {

        if (!Shell.rootAccess()) {
            if (startOnBootWirelessIsEnabled && ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_SECURE_SETTINGS) == PackageManager.PERMISSION_GRANTED) {
                Log.i(AppConstants.TAG, "WRITE_SECURE_SETTINGS is enabled and user has Start on boot is enabled for wireless ADB")
                try {
                    val wirelessAdbStatus = validateThenEnableWirelessAdb(context.contentResolver, context)
                    if (wirelessAdbStatus) {
                        val intentService = Intent(context, SelfStarterService::class.java)
                        context.startService(intentService)
                    }
                } catch (e: SecurityException) {
                    e.printStackTrace()
                    Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            } else
            //NotificationHelper.notify(context, AppConstants.NOTIFICATION_ID_STATUS, AppConstants.NOTIFICATION_CHANNEL_STATUS, R.string.notification_service_start_no_root)
                return
        }

        Starter.writeDataFiles(context)
        Shell.su(Starter.dataCommand).exec()
    }
}
