package moe.shizuku.manager

import android.app.Application
import android.content.Context
import com.topjohnwu.superuser.Shell
import me.weishu.reflection.Reflection
import moe.shizuku.manager.ktx.logd
import rikka.core.util.BuildUtils.atLeast30
import rikka.material.app.DayNightDelegate
import rikka.material.app.LocaleDelegate

lateinit var application: ShizukuApplication

class ShizukuApplication : Application() {

    companion object {

        init {
            logd("ShizukuApplication", "init")

            Shell.setDefaultBuilder(Shell.Builder.create().setFlags(Shell.FLAG_REDIRECT_STDERR))
            if (atLeast30) {
                System.loadLibrary("adb")
            }
        }
    }

    private fun init(context: Context?) {
        ShizukuSettings.initialize(context)
        LocaleDelegate.defaultLocale = ShizukuSettings.getLocale()
        DayNightDelegate.setApplicationContext(context)
        DayNightDelegate.setDefaultNightMode(ShizukuSettings.getNightMode())
    }

    override fun onCreate() {
        super.onCreate()
        application = this
        init(this)
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        if (!atLeast30) {
            Reflection.unseal(base)
        }
    }
}