package moe.shizuku.manager.starter

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Process
import android.util.Log
import com.topjohnwu.superuser.Shell
import moe.shizuku.manager.AppConstants
import moe.shizuku.manager.ShizukuSettings
import moe.shizuku.manager.ShizukuSettings.LaunchMethod
import rikka.shizuku.Shizuku

class BootCompleteReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_LOCKED_BOOT_COMPLETED != intent.action
                && Intent.ACTION_BOOT_COMPLETED != intent.action) {
            return
        }
        if (Process.myUid() / 100000 > 0) return

        if (ShizukuSettings.getLastLaunchMode() == LaunchMethod.ROOT) {
            Log.i(AppConstants.TAG, "start on boot, action=" + intent.action)
            if (Shizuku.pingBinder()) {
                Log.i(AppConstants.TAG, "service is running")
                return
            }
            start(context)
        }
    }

    private fun start(context: Context) {
        if (!Shell.rootAccess()) {
            //NotificationHelper.notify(context, AppConstants.NOTIFICATION_ID_STATUS, AppConstants.NOTIFICATION_CHANNEL_STATUS, R.string.notification_service_start_no_root)
            return
        }

        Starter.writeDataFiles(context)
        Shell.su(Starter.dataCommand).exec().let {}
    }
}
