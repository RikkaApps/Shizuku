package moe.shizuku.manager.shell

import android.os.Bundle
import android.os.IBinder
import android.os.Parcel
import moe.shizuku.manager.app.AppActivity
import rikka.shizuku.Shizuku

class ShellRequestHandlerActivity : AppActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent.action != "rikka.shizuku.intent.action.REQUEST_BINDER") {
            finish()
            return
        }

        val binder = intent.getBundleExtra("data")?.getBinder("binder")
        if (binder == null) {
            finish()
            return
        }

        val data = Parcel.obtain()
        try {
            data.writeStrongBinder(Shizuku.getBinder())
            data.writeString(applicationInfo.sourceDir)
            binder.transact(1, data, null, IBinder.FLAG_ONEWAY)
        } catch (e: Throwable) {
            e.printStackTrace()
        } finally {
            data.recycle()
        }

        finish()
    }
}
