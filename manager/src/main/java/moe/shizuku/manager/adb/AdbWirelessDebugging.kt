package moe.shizuku.manager.adb

import android.Manifest.permission.WRITE_SECURE_SETTINGS
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import moe.shizuku.manager.ShizukuSettings
import rikka.shizuku.Shizuku
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/** Owns the temporary ADB settings changed while starting Shizuku with wireless ADB. */
object AdbWirelessDebugging {

    const val EXTRA_SETTINGS_BEFORE_START = "moe.shizuku.manager.extra.ADB_SETTINGS_BEFORE_START"
    const val EXTRA_SETTINGS_CHANGED_BY_MANAGER = "moe.shizuku.manager.extra.ADB_SETTINGS_CHANGED_BY_MANAGER"

    private const val TAG = "AdbWirelessDebugging"
    private const val ADB_WIFI_ENABLED = "adb_wifi_enabled"
    private const val ADB_ALLOWED_CONNECTION_TIME = "adb_allowed_connection_time"
    private const val KEY_WIFI_ENABLED = "wifi_enabled"
    private const val KEY_USB_ENABLED = "usb_enabled"
    private const val KEY_ALLOWED_CONNECTION_TIME = "allowed_connection_time"

    fun captureSettings(context: Context): Bundle? {
        return try {
            val resolver = context.contentResolver
            Bundle().apply {
                putInt(KEY_WIFI_ENABLED, Settings.Global.getInt(resolver, ADB_WIFI_ENABLED, 0))
                putInt(KEY_USB_ENABLED, Settings.Global.getInt(resolver, Settings.Global.ADB_ENABLED, 0))
                putString(KEY_ALLOWED_CONNECTION_TIME, Settings.Global.getString(resolver, ADB_ALLOWED_CONNECTION_TIME))
            }
        } catch (e: SecurityException) {
            Log.w(TAG, "Cannot capture ADB settings", e)
            null
        }
    }

    fun enableForStart(context: Context, before: Bundle?): Boolean {
        if (before == null) {
            Log.w(TAG, "Cannot enable ADB settings without a saved settings snapshot")
            return false
        }

        return try {
            val resolver = context.contentResolver
            val settingsEnabled = Settings.Global.putInt(resolver, ADB_WIFI_ENABLED, 1)
                    && Settings.Global.putInt(resolver, Settings.Global.ADB_ENABLED, 1)
                    && Settings.Global.putLong(resolver, ADB_ALLOWED_CONNECTION_TIME, 0L)
            if (!settingsEnabled) {
                Log.w(TAG, "ADB settings update was rejected")
                finishStart(context, before, serverStarted = false, settingsChangedByManager = true)
                return false
            }
            true
        } catch (e: SecurityException) {
            Log.w(TAG, "Cannot enable ADB settings", e)
            // A write earlier in this sequence may have succeeded before a later one failed.
            finishStart(context, before, serverStarted = false, settingsChangedByManager = true)
            false
        }
    }

    /** Restore temporary USB/auth settings and apply the user's post-start Wi-Fi preference. */
    fun finishStart(
        context: Context,
        before: Bundle?,
        serverStarted: Boolean,
        settingsChangedByManager: Boolean
    ) {
        if (before == null) return
        if (context.checkSelfPermission(WRITE_SECURE_SETTINGS) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "Cannot restore ADB settings without WRITE_SECURE_SETTINGS")
            return
        }

        val resolver = context.contentResolver
        try {
            val currentWifi = Settings.Global.getInt(resolver, ADB_WIFI_ENABLED, 0)
            val wifiWasEnabled = before.getInt(KEY_WIFI_ENABLED, 0) == 1
            if (AdbWirelessDebuggingPolicy.shouldDisableWirelessDebugging(
                    wasEnabled = wifiWasEnabled,
                    isEnabled = currentWifi == 1,
                    serverStarted = serverStarted,
                    disableAfterStart = ShizukuSettings.shouldDisableAdbWifiAfterStart(),
                    settingsChangedByManager = settingsChangedByManager,
                )) {
                Settings.Global.putInt(resolver, ADB_WIFI_ENABLED, 0)
            }

            if (settingsChangedByManager) {
                val currentUsb = Settings.Global.getInt(resolver, Settings.Global.ADB_ENABLED, 0)
                if (currentUsb == 1 && before.getInt(KEY_USB_ENABLED, 0) == 0) {
                    Settings.Global.putInt(resolver, Settings.Global.ADB_ENABLED, 0)
                }

                val currentConnectionTime = Settings.Global.getString(resolver, ADB_ALLOWED_CONNECTION_TIME)
                val previousConnectionTime = before.getString(KEY_ALLOWED_CONNECTION_TIME)
                if (currentConnectionTime == "0" && previousConnectionTime != "0") {
                    Settings.Global.putString(resolver, ADB_ALLOWED_CONNECTION_TIME, previousConnectionTime)
                }
            }
        } catch (e: SecurityException) {
            Log.w(TAG, "Failed to restore ADB settings", e)
        }
    }

    fun awaitServerReady(timeoutSeconds: Long): Boolean {
        return awaitServerReadyMillis(TimeUnit.SECONDS.toMillis(timeoutSeconds))
    }

    fun awaitServerReadyMillis(timeoutMillis: Long): Boolean {
        if (timeoutMillis <= 0) return false

        val latch = CountDownLatch(1)
        val listener = Shizuku.OnBinderReceivedListener { latch.countDown() }
        // Use the sticky readiness event instead of a synchronous pingBinder IPC so the wait
        // remains bounded by timeoutMillis.
        Shizuku.addBinderReceivedListenerSticky(listener)
        return try {
            latch.await(timeoutMillis, TimeUnit.MILLISECONDS)
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            false
        } finally {
            Shizuku.removeBinderReceivedListener(listener)
        }
    }
}
