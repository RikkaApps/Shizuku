package moe.shizuku.manager.watchdog

import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.IBinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import moe.shizuku.manager.ShizukuSettings
import rikka.shizuku.Shizuku

/**
 * 轻量后台服务，仅用于持有 binder death 监听与 Wi-Fi 可用监听。
 *
 * 不是前台服务，不显示通知。系统回收时监听随之失效，这是设计上接受的代价。
 */
class WatchdogService : Service() {

    private var watchJob: Job? = null

    private val binderDeadListener = Shizuku.OnBinderDeadListener {
        if (!ShizukuSettings.isWatchdogEnabled()) return@OnBinderDeadListener
        if (ShizukuSettings.isWatchdogUserStopped()) return@OnBinderDeadListener
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return@OnBinderDeadListener

        CoroutineScope(Dispatchers.IO).launch {
            WatchdogCore.ensureServer(this@WatchdogService)
        }
    }

    /**
     * Wi-Fi 可用时再问一次 server 死没死。
     *
     * server 由 adb shell 启动，Wi-Fi 断开会让 adbd 清理掉它；而 server 刚死的那一刻
     * 没有网络，mDNS 探测不到无线调试端口，只能放弃。等 Wi-Fi 回来必须有人再触发一次，
     * 否则会一直停着。这是纯系统事件回调，不轮询、不唤醒。
     */
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            if (!ShizukuSettings.isWatchdogEnabled()) return
            if (ShizukuSettings.isWatchdogUserStopped()) return
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return

            watchAfterNetworkBack()
        }
    }

    /**
     * Wi-Fi 恢复后观察一段时间，而不是只问一次。
     *
     * adbd 清理 server 有延迟：Wi-Fi 刚回来时 server 往往还活着，只问一次会直接返回，
     * 等它随后真正死掉时已经没有触发源了。这里按指数退避问四次（10s / 20s / 40s / 80s），
     * 每次只是一次 pingBinder，开销可以忽略。
     */
    private fun watchAfterNetworkBack() {
        watchJob?.cancel()
        watchJob = CoroutineScope(Dispatchers.IO).launch {
            var interval = 10_000L
            repeat(4) {
                delay(interval)
                if (!ShizukuSettings.isWatchdogEnabled()) return@launch
                if (ShizukuSettings.isWatchdogUserStopped()) return@launch
                WatchdogCore.ensureServer(this@WatchdogService)
                interval *= 2
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        Shizuku.addBinderDeadListener(binderDeadListener)

        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()
        try {
            getSystemService(ConnectivityManager::class.java)
                ?.registerNetworkCallback(request, networkCallback)
        } catch (_: Throwable) {
            // 注册失败只影响这一条触发源，binder death 仍然有效
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!ShizukuSettings.isWatchdogEnabled()) {
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    override fun onDestroy() {
        watchJob?.cancel()
        Shizuku.removeBinderDeadListener(binderDeadListener)
        try {
            getSystemService(ConnectivityManager::class.java)
                ?.unregisterNetworkCallback(networkCallback)
        } catch (_: Throwable) {
        }
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
