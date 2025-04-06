package moe.shizuku.manager.starter

import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import moe.shizuku.manager.BuildConfig
import moe.shizuku.manager.ShizukuSettings
import moe.shizuku.manager.adb.AdbClient
import moe.shizuku.manager.adb.AdbKey
import moe.shizuku.manager.adb.AdbKeyException
import moe.shizuku.manager.adb.AdbMdns
import moe.shizuku.manager.adb.PreferenceAdbKeyStore
import moe.shizuku.manager.utils.EnvironmentUtils
import rikka.lifecycle.Resource

class SelfStarterService : Service(), LifecycleOwner {

    private val sb = StringBuilder()
    private lateinit var adbMdns: AdbMdns
    private val port = MutableLiveData<Int>()
    private val lifecycleOwner = LifecycleRegistry(this)
    private val _output = MutableLiveData<Resource<StringBuilder>>()
    val output = _output as LiveData<Resource<StringBuilder>>

    override fun onCreate() {
        super.onCreate()

        val host = "127.0.0.1"
        val startOnBootWirelessIsEnabled = ShizukuSettings.getPreferences().getBoolean(ShizukuSettings.KEEP_START_ON_BOOT_WIRELESS, false)
        if (startOnBootWirelessIsEnabled && Settings.Global.getInt(this.contentResolver, "adb_wifi_enabled") == 1) {
            Starter.writeSdcardFiles(applicationContext)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                adbMdns = AdbMdns(this, AdbMdns.TLS_CONNECT, port)
                adbMdns.start()

                // Observe changes in the port
                port.observeForever { it ->
                    if (it > 65535 || it < 1)
                        return@observeForever
                    try {
                        startAdb(host, it)
                        adbMdns.stop()
                    } catch (e: Exception) {
                        adbMdns.stop()
                        e.printStackTrace()
                    }
                }
                Toast.makeText(this, "Shizuku service has been started!", Toast.LENGTH_SHORT).show()
                return
            } else {
                val port = EnvironmentUtils.getAdbTcpPort()
                if (port > 0) {
                    startAdb(host, port)
                }
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }


    override fun getLifecycle(): Lifecycle {
        return lifecycleOwner
    }

    private fun postResult(throwable: Throwable? = null) {
        if (throwable == null)
            _output.postValue(Resource.success(sb))
        else
            _output.postValue(Resource.error(throwable, sb))
    }

    private fun startAdb(host: String, port: Int) {
        sb.append("Starting with wireless adb...").append('\n').append('\n')
        postResult()

        GlobalScope.launch(Dispatchers.IO) {
            val key = try {
                AdbKey(PreferenceAdbKeyStore(ShizukuSettings.getPreferences()), "shizuku")
            } catch (e: Throwable) {
                e.printStackTrace()
                sb.append('\n').append(Log.getStackTraceString(e))

                postResult(AdbKeyException(e))
                return@launch
            }

            AdbClient(host, port, key).runCatching {
                connect()

                shellCommand(Starter.sdcardCommand) {
                    sb.append(String(it))
                    postResult()
                }
                close()
            }.onFailure {
                it.printStackTrace()

                sb.append('\n').append(Log.getStackTraceString(it))
                postResult(it)
            }

            /* Adb on MIUI Android 11 has no permission to access Android/data.
               Before MIUI Android 12, we can temporarily use /data/user_de.
               After that, is better to implement "adb push" and push files directly to /data/local/tmp.
             */
            if (sb.contains("/Android/data/${BuildConfig.APPLICATION_ID}/start.sh: Permission denied")) {
                sb.append('\n')
                        .appendLine("adb have no permission to access Android/data, how could this possible ?!")
                        .appendLine("try /data/user_de instead...")
                        .appendLine()
                postResult()

                Starter.writeDataFiles(moe.shizuku.manager.application, true)

                AdbClient(host, port, key).runCatching {
                    connect()
                    shellCommand(Starter.dataCommand) {
                        sb.append(String(it))
                        postResult()
                    }
                    close()
                }.onFailure {
                    it.printStackTrace()

                    sb.append('\n').append(Log.getStackTraceString(it))
                    postResult(it)
                }
            }
        }
    }
}
