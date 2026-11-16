package moe.shizuku.manager.watchdog

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import moe.shizuku.manager.ShizukuSettings
import rikka.shizuku.Shizuku

/**
 * 轻量后台服务，仅用于持有 binder death 监听。
 *
 * 不是前台服务，不显示通知。网络触发由 [WatchdogBootstrap] 负责，
 * 因为 App 被广播在后台拉起时系统不允许 startService，Service 起不来。
 */
class WatchdogService : Service() {

    private val binderDeadListener = Shizuku.OnBinderDeadListener {
        if (!ShizukuSettings.isWatchdogEnabled()) return@OnBinderDeadListener
        if (ShizukuSettings.isWatchdogUserStopped()) return@OnBinderDeadListener
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return@OnBinderDeadListener

        CoroutineScope(Dispatchers.IO).launch {
            WatchdogCore.ensureServer(this@WatchdogService)
        }
    }

    override fun onCreate() {
        super.onCreate()
        Shizuku.addBinderDeadListener(binderDeadListener)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!ShizukuSettings.isWatchdogEnabled()) {
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    override fun onDestroy() {
        Shizuku.removeBinderDeadListener(binderDeadListener)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        fun start(context: Context) {
            try {
                context.startService(Intent(context, WatchdogService::class.java))
            } catch (_: Throwable) {
            }
        }

        fun stop(context: Context) {
            try {
                context.stopService(Intent(context, WatchdogService::class.java))
            } catch (_: Throwable) {
            }
        }
    }
}
