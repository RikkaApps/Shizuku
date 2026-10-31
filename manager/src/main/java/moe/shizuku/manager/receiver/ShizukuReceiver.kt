package moe.shizuku.manager.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Process
import moe.shizuku.manager.shell.ShellBinderRequestHandler

class ShizukuReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if ("rikka.shizuku.intent.action.REQUEST_BINDER" != intent.action) {
            return
        }

        // The REQUEST_BINDER flow is only used by the shell tooling (rish) running from adb.
        // Reject requests from normal apps so they cannot obtain the Shizuku binder or the
        // manager APK path.
        val uid = getSendingUid()
        if (uid != Process.SHELL_UID && uid != Process.ROOT_UID && uid != Process.SYSTEM_UID && uid != Process.myUid()) {
            return
        }

        ShellBinderRequestHandler.handleRequest(context, intent)
    }
}
