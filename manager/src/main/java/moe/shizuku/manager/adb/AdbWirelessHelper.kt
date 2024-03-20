package moe.shizuku.manager.adb

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import moe.shizuku.manager.AppConstants
import moe.shizuku.manager.starter.StarterActivity

object WirelessADBHelper {

    fun validateThenEnableWirelessAdb(contentResolver: ContentResolver, context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if (networkCapabilities != null && networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            enableWirelessADB(contentResolver, context)
            return true
        }
        return false
    }

    private fun enableWirelessADB(contentResolver: ContentResolver, context: Context) {
        // Enable wireless ADB
        Settings.Global.putInt(
                contentResolver,
                "adb_wifi_enabled",
                1
        )

        Log.i(AppConstants.TAG, "Wireless Debugging enabled")
        Toast.makeText(context, "Wireless Debugging enabled", Toast.LENGTH_SHORT).show()
    }

    fun callStartAdb(context: Context, host: String, port: Int) {
        val intent = Intent(context, StarterActivity::class.java).apply {
            putExtra(StarterActivity.EXTRA_IS_ROOT, false)
            putExtra(StarterActivity.EXTRA_HOST, host)
            putExtra(StarterActivity.EXTRA_PORT, port)
        }
        context.startActivity(intent)
    }
}
