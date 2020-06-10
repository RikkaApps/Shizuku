package moe.shizuku.manager.utils

import android.annotation.SuppressLint
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.RemoteException
import hidden.HiddenApiBridge
import moe.shizuku.api.ShizukuBinderWrapper
import moe.shizuku.api.ShizukuService
import moe.shizuku.api.SystemServiceHelper
import rikka.core.ktx.unsafeLazy
import rikka.core.util.BuildUtils
import java.util.*

object ShizukuSystemApis {

    private val packageManager by unsafeLazy {
        // If manager depends on hideen-api-common, kotlin & R8 use classes from it. Use HiddenApiBridge to avoid this problem.
        HiddenApiBridge.IPackageManager_Stub_asInterface(ShizukuBinderWrapper(SystemServiceHelper.getSystemService("package")))
    }

    @delegate:SuppressLint("NewApi")
    private val permissionManager by unsafeLazy {
        // If manager depends on hideen-api-common, kotlin & R8 use classes from it. Use HiddenApiBridge to avoid this problem.
        HiddenApiBridge.IPermissionManager_Stub_asInterface(ShizukuBinderWrapper(SystemServiceHelper.getSystemService("permissionmgr")))
    }

    fun getInstalledPackages(flags: Int, userId: Int): List<PackageInfo> {
        return if (!ShizukuService.pingBinder()) {
            ArrayList()
        } else try {
            HiddenApiBridge.IPackageManager_getInstalledPackages(packageManager, flags, userId)
        } catch (tr: RemoteException) {
            throw RuntimeException(tr.message, tr)
        }
    }

    fun checkPermission(permName: String, pkgName: String, userId: Int): Int {
        return if (!ShizukuService.pingBinder()) {
            PackageManager.PERMISSION_DENIED
        } else try {
            if (BuildUtils.atLeast30) {
                HiddenApiBridge.IPermissionManager_checkPermission(permissionManager, permName, pkgName, userId)
            } else {
                HiddenApiBridge.IPackageManager_checkPermission(packageManager, permName, pkgName, userId)
            }
        } catch (tr: RemoteException) {
            throw RuntimeException(tr.message, tr)
        }
    }

    fun grantRuntimePermission(packageName: String, permissionName: String, userId: Int) {
        if (!ShizukuService.pingBinder()) {
            return
        }
        try {
            if (BuildUtils.atLeast30) {
                HiddenApiBridge.IPermissionManager_grantRuntimePermission(permissionManager, packageName, permissionName, userId)
            } else {
                HiddenApiBridge.IPackageManager_grantRuntimePermission(packageManager, packageName, permissionName, userId)
            }
        } catch (tr: RemoteException) {
            throw RuntimeException(tr.message, tr)
        }
    }

    fun revokeRuntimePermission(packageName: String, permissionName: String, userId: Int) {
        if (!ShizukuService.pingBinder()) {
            return
        }
        try {
            if (BuildUtils.atLeast30) {
                HiddenApiBridge.IPermissionManager_revokeRuntimePermission(permissionManager, packageName, permissionName, userId)
            } else {
                HiddenApiBridge.IPackageManager_revokeRuntimePermission(packageManager, packageName, permissionName, userId)
            }
        } catch (tr: RemoteException) {
            throw RuntimeException(tr.message, tr)
        }
    }
}