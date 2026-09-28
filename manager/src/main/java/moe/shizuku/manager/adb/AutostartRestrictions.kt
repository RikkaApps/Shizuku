package moe.shizuku.manager.adb

import android.Manifest.permission.WRITE_SECURE_SETTINGS
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import moe.shizuku.manager.AppConstants
import moe.shizuku.manager.ShizukuSettings
import moe.shizuku.manager.ShizukuSettings.LaunchMethod

object AutostartRestrictions {

    private const val PREF_DISMISSED = "autostart_hint_dismissed"

    fun isXiaomi(): Boolean {
        val m = Build.MANUFACTURER?.lowercase().orEmpty()
        val b = Build.BRAND?.lowercase().orEmpty()
        return m == "xiaomi" || b == "xiaomi" || b == "redmi" || b == "poco"
    }

    fun isBatteryOptimizationIgnored(context: Context): Boolean {
        val pm = context.getSystemService(PowerManager::class.java) ?: return true
        return pm.isIgnoringBatteryOptimizations(context.packageName)
    }

    fun isDismissed(): Boolean {
        return ShizukuSettings.getPreferences().getBoolean(PREF_DISMISSED, false)
    }

    fun setDismissed(value: Boolean) {
        ShizukuSettings.getPreferences().edit().putBoolean(PREF_DISMISSED, value).apply()
    }

    fun isAutostartConfigured(context: Context): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && context.checkSelfPermission(WRITE_SECURE_SETTINGS) == PackageManager.PERMISSION_GRANTED
                && ShizukuSettings.getLastLaunchMode() == LaunchMethod.ADB
    }

    fun shouldShowHint(context: Context): Boolean {
        if (!isAutostartConfigured(context)) return false
        if (isDismissed()) return false
        return !isBatteryOptimizationIgnored(context) || isXiaomi()
    }

    @SuppressLint("BatteryLife")
    fun requestIgnoreBatteryOptimizations(context: Context): Boolean {
        return try {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Log.w(AppConstants.TAG, "requestIgnoreBatteryOptimizations: failed, falling back", e)
            openBatteryOptimizationList(context)
        }
    }

    private fun openBatteryOptimizationList(context: Context): Boolean {
        return try {
            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Log.w(AppConstants.TAG, "openBatteryOptimizationList: failed", e)
            false
        }
    }

    fun openMiuiAutostartSettings(context: Context): Boolean {
        val intent = Intent().apply {
            component = ComponentName(
                "com.miui.securitycenter",
                "com.miui.permcenter.autostart.AutoStartManagementActivity"
            )
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
            return true
        } catch (e: Exception) {
            Log.w(AppConstants.TAG, "openMiuiAutostartSettings: deep-link unavailable, falling back", e)
        }
        return openAppDetails(context)
    }

    private fun openAppDetails(context: Context): Boolean {
        return try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Log.w(AppConstants.TAG, "openAppDetails: failed", e)
            false
        }
    }
}
