package moe.shizuku.manager.utils

import android.content.pm.PackageInfo
import android.os.RemoteException
import hidden.HiddenApiBridge
import rikka.core.ktx.unsafeLazy
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuBinderWrapper
import rikka.shizuku.SystemServiceHelper
import java.util.*

object ShizukuSystemApis {

    private val packageManager by unsafeLazy {
        // If manager depends on hideen-api-common, kotlin & R8 use classes from it. Use HiddenApiBridge to avoid this problem.
        HiddenApiBridge.IPackageManager_Stub_asInterface(ShizukuBinderWrapper(SystemServiceHelper.getSystemService("package")))
    }

    fun getInstalledPackages(flags: Int, userId: Int): List<PackageInfo> {
        return if (!Shizuku.pingBinder()) {
            ArrayList()
        } else try {
            HiddenApiBridge.IPackageManager_getInstalledPackages(packageManager, flags, userId)
        } catch (tr: RemoteException) {
            throw RuntimeException(tr.message, tr)
        }
    }
}