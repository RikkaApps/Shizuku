package moe.shizuku.manager.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.topjohnwu.superuser.Shell
import moe.shizuku.manager.AppConstants
import moe.shizuku.manager.ShizukuSettings
import moe.shizuku.manager.ShizukuSettings.LaunchMethod
import moe.shizuku.manager.adb.AdbAutostart
import moe.shizuku.manager.adb.AdbAutostartScheduler
import moe.shizuku.manager.starter.Starter
import moe.shizuku.manager.utils.UserHandleCompat
import rikka.shizuku.Shizuku

class BootCompleteReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action != Intent.ACTION_LOCKED_BOOT_COMPLETED
            && action != Intent.ACTION_BOOT_COMPLETED
            && action != Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            return
        }

        if (UserHandleCompat.myUserId() > 0) return

        Log.i(AppConstants.TAG, "BootCompleteReceiver: received $action")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            AdbAutostartScheduler.schedule(context)
        }

        if (Shizuku.pingBinder()) return

        if (ShizukuSettings.getLastLaunchMode() == LaunchMethod.ROOT) {
            rootStart(context)
        } else if (AdbAutostart.canAutostart(context)) {
            val initialDelay = if (action == Intent.ACTION_MY_PACKAGE_REPLACED) 0L else 3000L
            val pending = goAsync()
            AdbAutostart.start(context, initialDelayMs = initialDelay) {
                pending.finish()
            }
        } else {
            Log.w(AppConstants.TAG, "BootCompleteReceiver: no eligible launch method")
        }
    }

    private fun rootStart(context: Context) {
        if (!Shell.getShell().isRoot) {
            Shell.getCachedShell()?.close()
            return
        }

        Shell.cmd(Starter.internalCommand).exec()
    }
}
