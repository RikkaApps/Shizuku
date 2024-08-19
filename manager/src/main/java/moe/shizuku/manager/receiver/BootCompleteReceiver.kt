package moe.shizuku.manager.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Looper
import android.os.Process
import android.provider.Settings.Global
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.topjohnwu.superuser.Shell
import com.topjohnwu.superuser.internal.UiThreadHandler.handler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import moe.shizuku.manager.AppConstants
import moe.shizuku.manager.ShizukuSettings
import moe.shizuku.manager.ShizukuSettings.LaunchMethod
import moe.shizuku.manager.adb.AdbClient
import moe.shizuku.manager.adb.AdbKey
import moe.shizuku.manager.adb.AdbMdns
import moe.shizuku.manager.adb.PreferenceAdbKeyStore
import moe.shizuku.manager.starter.Starter
import rikka.shizuku.Shizuku

@RequiresApi(Build.VERSION_CODES.R)
class BootCompleteReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.i("shizuku", "start shizukuuuuu")


        // TODO Record if receiver is called
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
        Log.i("shizuku", "start shizukuuuuu")
        if (!Shell.rootAccess()) {
            Log.i("shizuku", "start non-root sihzuku")
            val intents = Intent(context, LaunchActivity::class.java)
            intents.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intents)
        } else {
            Starter.writeDataFiles(context)
            Shell.su(Starter.dataCommand).exec()
        }
    }
}
