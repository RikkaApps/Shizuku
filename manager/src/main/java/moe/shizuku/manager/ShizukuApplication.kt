package moe.shizuku.manager

import android.app.Application
import android.content.Context
import com.topjohnwu.superuser.Shell
import me.weishu.reflection.Reflection
import moe.shizuku.manager.adb.AdbPairingClient.Companion.available
import moe.shizuku.manager.authorization.AuthorizationManager
import rikka.core.util.BuildUtils.atLeast29
import rikka.core.util.BuildUtils.atLeast30
import rikka.material.app.DayNightDelegate
import rikka.material.app.LocaleDelegate

class ShizukuApplication : Application() {

    companion object {

        init {
            Shell.Config.setFlags(Shell.FLAG_REDIRECT_STDERR)
            if (BuildConfig.DEBUG && atLeast29) {
                available()
            }
            if (atLeast30) {
                System.loadLibrary("bypass")
            }
        }
    }

    private fun init(context: Context?) {
        ShizukuSettings.initialize(context)
        LocaleDelegate.defaultLocale = ShizukuSettings.getLocale()
        DayNightDelegate.setApplicationContext(context)
        DayNightDelegate.setDefaultNightMode(ShizukuSettings.getNightMode())
        AuthorizationManager.init(context)
    }

    override fun onCreate() {
        super.onCreate()
        init(this)
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        if (!atLeast30) {
            Reflection.unseal(base)
        }
    }
}