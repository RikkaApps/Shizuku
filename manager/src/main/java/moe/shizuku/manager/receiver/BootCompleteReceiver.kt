package moe.shizuku.manager.receiver

import android.Manifest.permission.WRITE_SECURE_SETTINGS
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import moe.shizuku.manager.AppConstants
import moe.shizuku.manager.ShizukuSettings
import moe.shizuku.manager.ShizukuSettings.LaunchMethod
import moe.shizuku.manager.adb.AdbClient
import moe.shizuku.manager.adb.AdbKey
import moe.shizuku.manager.adb.AdbMdns
import moe.shizuku.manager.adb.AdbWirelessDebugging
import moe.shizuku.manager.adb.PreferenceAdbKeyStore
import moe.shizuku.manager.starter.Starter
import moe.shizuku.manager.utils.UserHandleCompat
import rikka.shizuku.Shizuku
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class BootCompleteReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_LOCKED_BOOT_COMPLETED != intent.action
            && Intent.ACTION_BOOT_COMPLETED != intent.action) {
            return
        }

        if (UserHandleCompat.myUserId() > 0 || Shizuku.pingBinder()) return

        if (ShizukuSettings.getLastLaunchMode() == LaunchMethod.ROOT) {
            rootStart(context)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU // https://r.android.com/2128832
            && context.checkSelfPermission(WRITE_SECURE_SETTINGS) == PackageManager.PERMISSION_GRANTED
            && ShizukuSettings.getLastLaunchMode() == LaunchMethod.ADB) {
            adbStart(context)
        } else {
            Log.w(AppConstants.TAG, "No support start on boot")
        }
    }

    private fun rootStart(context: Context) {
        if (!Shell.getShell().isRoot) {
            //NotificationHelper.notify(context, AppConstants.NOTIFICATION_ID_STATUS, AppConstants.NOTIFICATION_CHANNEL_STATUS, R.string.notification_service_start_no_root)
            Shell.getCachedShell()?.close()
            return
        }

        Shell.cmd(Starter.internalCommand).exec()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun adbStart(context: Context) {
        val settingsBefore = AdbWirelessDebugging.captureSettings(context)
        if (!AdbWirelessDebugging.enableForStart(context)) return
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            val latch = CountDownLatch(1)
            val startRequested = AtomicBoolean(false)
            var serverStarted = false
            val adbMdns = AdbMdns(context, AdbMdns.TLS_CONNECT) { port ->
                if (port <= 0 || !startRequested.compareAndSet(false, true)) return@AdbMdns
                try {
                    val keystore = PreferenceAdbKeyStore(ShizukuSettings.getPreferences())
                    val key = AdbKey(keystore, "shizuku")
                    val client = AdbClient("127.0.0.1", port, key)
                    client.connect()
                    client.shellCommand(Starter.internalCommand, null)
                    client.close()
                    serverStarted = AdbWirelessDebugging.awaitServerReady(6)
                } catch (_: Exception) {
                    Log.w(AppConstants.TAG, "Failed to start Shizuku over wireless ADB")
                } finally {
                    latch.countDown()
                }
            }
            try {
                adbMdns.start()
                latch.await(8, TimeUnit.SECONDS)
            } finally {
                adbMdns.stop()
                AdbWirelessDebugging.finishStart(context, settingsBefore, serverStarted, settingsChangedByManager = true)
                pending.finish()
            }
        }
    }
}
