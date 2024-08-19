package moe.shizuku.manager.receiver

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.topjohnwu.superuser.internal.UiThreadHandler.handler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import moe.shizuku.manager.ShizukuSettings
import moe.shizuku.manager.adb.AdbClient
import moe.shizuku.manager.adb.AdbKey
import moe.shizuku.manager.adb.AdbMdns
import moe.shizuku.manager.adb.PreferenceAdbKeyStore
import moe.shizuku.manager.starter.Starter

fun SecureSettingsAllowed(context: Context): Boolean {
    val packageInfo: PackageInfo = context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_PERMISSIONS)
    val list: List<String>  = packageInfo.requestedPermissions!!.filterIndexed { index, permission ->
        (packageInfo.requestedPermissionsFlags[index] and PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0
    }
    if (list.contains("android.permission.WRITE_SECURE_SETTINGS")) { return true } else {return false}
}

class LaunchActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GlobalScope.launch { delay(1000)
            Starter.writeSdcardFiles(applicationContext)
            val context = applicationContext
            if(SecureSettingsAllowed(context)) {
                Log.i(application.packageName, "SecureSettingsAllowed")
                val serviceIntent = Intent(context, StartService::class.java)
                context.startService(serviceIntent)
            }
        }
    }
}