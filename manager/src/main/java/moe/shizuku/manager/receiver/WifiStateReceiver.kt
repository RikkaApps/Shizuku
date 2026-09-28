package moe.shizuku.manager.receiver

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import moe.shizuku.manager.AppConstants
import moe.shizuku.manager.adb.AdbAutostart

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
object WifiStateReceiver {

    private var registered = false

    fun register(context: Context) {
        if (registered) return
        registered = true

        val cm = context.getSystemService(ConnectivityManager::class.java) ?: return
        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        cm.registerNetworkCallback(request, object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                if (AdbAutostart.canAutostart(context)) {
                    Log.i(AppConstants.TAG, "WifiStateReceiver: WiFi connected, triggering adb autostart")
                    AdbAutostart.start(context, initialDelayMs = 1000L)
                }
            }
        })

        Log.i(AppConstants.TAG, "WifiStateReceiver: registered WiFi network callback")
    }
}
