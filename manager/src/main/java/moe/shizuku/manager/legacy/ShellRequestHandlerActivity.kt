package moe.shizuku.manager.legacy

import android.os.Bundle
import android.os.Process
import moe.shizuku.manager.app.AppActivity
import moe.shizuku.manager.shell.ShellBinderRequestHandler

class ShellRequestHandlerActivity : AppActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Same protection as ShizukuReceiver: the REQUEST_BINDER flow is only used by the
        // shell tooling (rish) running from adb. Allow shell/root, and the system process
        // (used by the Android 8.x chooser fallback), so only normal apps are blocked.
        val uid = callingUid
        if (uid == Process.SHELL_UID || uid == Process.ROOT_UID || uid == Process.SYSTEM_UID || uid == Process.myUid()) {
            ShellBinderRequestHandler.handleRequest(this, intent)
        }
        finish()
    }
}
