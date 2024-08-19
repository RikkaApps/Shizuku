package moe.shizuku.manager.receiver

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.provider.Settings.Global
import android.util.Log
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

private fun Launch(port: Int) {
    Log.i("AAAA", "LAUNCHING SHIZUKU")
    GlobalScope.launch(Dispatchers.IO) {
        val host = "127.0.0.1"

        val key = try {
            AdbKey(PreferenceAdbKeyStore(ShizukuSettings.getPreferences()), "shizuku")
        } catch (e: Throwable) {
            e.printStackTrace()
            return@launch
        }
        AdbClient(host, port, key).runCatching {
            connect()
            shellCommand(Starter.sdcardCommand) {}
            close()
        }.onFailure {
            it.printStackTrace()
        }
    }

}

@RequiresApi(Build.VERSION_CODES.R)
fun startupNonRoot(context: Context) {
    val adbPort = MutableLiveData<Int>()
    val adbConnect = AdbMdns(context, AdbMdns.TLS_CONNECT, adbPort)
    val ConnectObserver = Observer<Int> { port ->
        if (port in 0..65535) {
            Log.i(context.packageName, "port: " + port)
            adbConnect.stop()
            Launch(port)
        }
    }
    if (Looper.myLooper() == Looper.getMainLooper()) {
        adbPort.observeForever(ConnectObserver)
    } else {
        handler.post { adbPort.observeForever(ConnectObserver) }
    }
    adbConnect.start()
}


class StartService() : Service() {
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(application.packageName, "startservice")
        GlobalScope.launch {
            delay(500)
            val context = getApplicationContext();
            Global.putLong(context.getContentResolver(), "adb_allowed_connection_time", 0L)
            Global.putInt(context.getContentResolver(), "adb_wifi_enabled", 1);
            Log.i(application.packageName, "adb_wifi_enabled")
            startupNonRoot(context)
        }
        return START_STICKY  // Consider using START_STICKY to restart the service if killed
    }
    override fun onBind(intent: Intent?): IBinder? {
        return null  // We don't intend to bind to this service from other components
    }
}