package moe.shizuku.manager.watchdog

import android.Manifest.permission.WRITE_SECURE_SETTINGS
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.Observer
import moe.shizuku.manager.ShizukuSettings
import moe.shizuku.manager.ShizukuSettings.LaunchMethod
import moe.shizuku.manager.adb.AdbClient
import moe.shizuku.manager.adb.AdbKey
import moe.shizuku.manager.adb.AdbMdns
import moe.shizuku.manager.adb.PreferenceAdbKeyStore
import moe.shizuku.manager.starter.Starter
import rikka.shizuku.Shizuku
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/**
 * 看门狗核心逻辑。
 *
 * 只做一件事：检查 Shizuku server 是否存活，不存活时尝试通过本机无线调试端口重新拉起。
 *
 * 设计约束：
 * - 幂等：任意触发源都可以调用，server 存活时直接返回
 * - 不重试：探测不到端口就放弃，不做轮询，避免引入常驻开销
 * - 静默：失败不通知、不弹窗、不写任何用户可见状态
 */
object WatchdogCore {

    private const val TAG = "WatchdogCore"

    /**
     * mDNS 探测窗口。
     *
     * 开机后 Wi-Fi 与 adbd 的无线调试监听需要时间才能就绪，5 秒过短；
     * 这里放宽到 30 秒，仍低于后台广播 goAsync 的 60 秒上限。
     */
    private const val MDNS_TIMEOUT_MS = 30_000L

    /** 防止开机广播与 binder death 回调同时触发，导致重复拉起 */
    private val starting = AtomicBoolean(false)

    /**
     * @param ignoreSwitch true 表示忽略看门狗开关，用于保留官方原有的开机启动行为
     */
    @RequiresApi(Build.VERSION_CODES.R)
    fun ensureServer(context: Context, ignoreSwitch: Boolean = false) {
        val appContext = context.applicationContext

        if (!ignoreSwitch && !ShizukuSettings.isWatchdogEnabled()) return
        if (ShizukuSettings.isWatchdogUserStopped()) return
        if (!starting.compareAndSet(false, true)) return

        try {
            if (ShizukuSettings.getLastLaunchMode() != LaunchMethod.ADB) return
            if (isServerAlive()) return

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && appContext.checkSelfPermission(WRITE_SECURE_SETTINGS) == PackageManager.PERMISSION_GRANTED
            ) {
                enableWirelessAdb(appContext)
            }

            val port = discoverPort(appContext)
            if (port <= 0) {
                Log.d(TAG, "adb tls-connect port not found, give up")
                return
            }

            startServer(port)
            Log.i(TAG, "restart requested on port $port")
        } catch (t: Throwable) {
            Log.w(TAG, "ensureServer failed", t)
        } finally {
            starting.set(false)
        }
    }

    /**
     * 判断 server 是否存活。
     *
     * pingBinder() 返回 false 有两种可能：server 真的没了，或本进程还没拿到 binder。
     * 这里多等一次，避免把后者误判成前者，导致把活着的 server 杀掉重启。
     */
    private fun isServerAlive(): Boolean {
        if (Shizuku.pingBinder()) return true
        try {
            Thread.sleep(300)
        } catch (_: InterruptedException) {
            Thread.currentThread().interrupt()
        }
        return Shizuku.pingBinder()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun enableWirelessAdb(context: Context) {
        val cr = context.contentResolver
        Settings.Global.putInt(cr, "adb_wifi_enabled", 1)
        Settings.Global.putInt(cr, Settings.Global.ADB_ENABLED, 1)
        Settings.Global.putLong(cr, "adb_allowed_connection_time", 0L)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun discoverPort(context: Context): Int {
        val result = AtomicInteger(-1)
        val latch = CountDownLatch(1)
        val mdns = AdbMdns(context, AdbMdns.TLS_CONNECT, Observer<Int> { port ->
            if (port > 0 && result.compareAndSet(-1, port)) {
                latch.countDown()
            }
        })
        mdns.start()
        latch.await(MDNS_TIMEOUT_MS, TimeUnit.MILLISECONDS)
        mdns.stop()
        return result.get()
    }

    private fun startServer(port: Int) {
        val keystore = PreferenceAdbKeyStore(ShizukuSettings.getPreferences())
        val key = AdbKey(keystore, "shizuku")
        AdbClient("127.0.0.1", port, key).use { client ->
            client.connect()
            client.shellCommand(Starter.internalCommand, null)
        }
    }
}
