package moe.shizuku.manager.receiver

import android.Manifest.permission.WRITE_SECURE_SETTINGS
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.SystemClock
import android.util.Log
import androidx.annotation.RequiresApi
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
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

class BootCompleteReceiver : BroadcastReceiver() {

    companion object {
        private const val BOOT_ADB_START_TIMEOUT_MS = 9_000L
        private const val BOOT_ADB_DISCOVERY_TIMEOUT_MS = 5_000L
    }

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
        val deadline = SystemClock.elapsedRealtime() + BOOT_ADB_START_TIMEOUT_MS
        val settingsBefore = AdbWirelessDebugging.captureSettings(context)
        if (!AdbWirelessDebugging.enableForStart(context, settingsBefore)) return
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            val portChannel = Channel<Int>(Channel.CONFLATED)
            var serverStarted = false
            val adbMdns = AdbMdns(context, AdbMdns.TLS_CONNECT) { port ->
                if (port > 0) portChannel.trySend(port)
            }
            try {
                adbMdns.start()
                val discoveryTimeout = minOf(
                    BOOT_ADB_DISCOVERY_TIMEOUT_MS,
                    (deadline - SystemClock.elapsedRealtime()).coerceAtLeast(0L),
                )
                val port = if (discoveryTimeout > 0) {
                    withTimeoutOrNull(discoveryTimeout) { portChannel.receive() }
                } else {
                    null
                }
                if (port != null && SystemClock.elapsedRealtime() < deadline) {
                    // NsdManager calls observers on the main thread. Stop discovery here and do
                    // the blocking ADB handshake and Binder wait on this IO coroutine instead.
                    adbMdns.stop()
                    val key = AdbKey(PreferenceAdbKeyStore(ShizukuSettings.getPreferences()), "shizuku")
                    val remainingTimeout = (deadline - SystemClock.elapsedRealtime()).coerceAtLeast(0L)
                    if (remainingTimeout > 0) AdbClient(
                        "127.0.0.1",
                        port,
                        key,
                        timeoutMillis = remainingTimeout.toInt(),
                    ).use { client ->
                        client.connect()
                        client.shellCommand(Starter.internalCommand, null)
                    }
                    serverStarted = AdbWirelessDebugging.awaitServerReadyMillis(
                        (deadline - SystemClock.elapsedRealtime()).coerceAtLeast(0L),
                    )
                }
            } catch (e: Exception) {
                Log.w(AppConstants.TAG, "Failed to start Shizuku over wireless ADB", e)
            } finally {
                try {
                    adbMdns.stop()
                } finally {
                    portChannel.close()
                    try {
                        AdbWirelessDebugging.finishStart(
                            context,
                            settingsBefore,
                            serverStarted,
                            settingsChangedByManager = true,
                        )
                    } finally {
                        pending.finish()
                    }
                }
            }
        }
    }
}
