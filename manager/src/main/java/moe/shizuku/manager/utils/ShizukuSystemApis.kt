package moe.shizuku.manager.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.RemoteException
import hidden.HiddenApiBridge
import hidden.UserInfoCompat
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

    @delegate:SuppressLint("NewApi")
    private val permissionManager by unsafeLazy {
        // If manager depends on hideen-api-common, kotlin & R8 use classes from it. Use HiddenApiBridge to avoid this problem.
        HiddenApiBridge.IPermissionManager_Stub_asInterface(ShizukuBinderWrapper(SystemServiceHelper.getSystemService("permissionmgr")))
    }

    private val userManager by unsafeLazy {
        HiddenApiBridge.IUserManager_Stub_asInterface(ShizukuBinderWrapper(SystemServiceHelper.getSystemService(Context.USER_SERVICE)))
    }

    private val users = arrayListOf<UserInfoCompat>()

    private fun getUsers(): List<UserInfoCompat> {
        return if (!Shizuku.pingBinder()) {
            arrayListOf(UserInfoCompat(UserHandleCompat.myUserId(), "Owner"))
        } else try {
            HiddenApiBridge.IUserManager_getUsers(userManager)
        } catch (tr: Throwable) {
            arrayListOf(UserInfoCompat(UserHandleCompat.myUserId(), "Owner"))
        }
    }

    fun getUsers(useCache: Boolean = true): List<UserInfoCompat> {
        synchronized(users) {
            if (!useCache || users.isEmpty()) {
                users.clear()
                users.addAll(getUsers())
            }
            return users
        }
    }

    fun getUserInfo(userId: Int): UserInfoCompat {
        return getUsers(useCache = true).firstOrNull { it.id == userId } ?: UserInfoCompat(UserHandleCompat.myUserId(), "Unknown")
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

    fun checkPermission(permName: String, pkgName: String, userId: Int): Int {
        return if (!Shizuku.pingBinder()) {
            PackageManager.PERMISSION_DENIED
        } else try {
            if (Build.VERSION.SDK_INT >= 31 || Build.VERSION.SDK_INT == 30 && Build.VERSION.PREVIEW_SDK_INT > 0) {
                HiddenApiBridge.IPackageManager_checkPermission(packageManager, permName, pkgName, userId)
            } else if (Build.VERSION.SDK_INT >= 30) {
                HiddenApiBridge.IPermissionManager_checkPermission(permissionManager, permName, pkgName, userId)
            } else {
                HiddenApiBridge.IPackageManager_checkPermission(packageManager, permName, pkgName, userId)
            }
        } catch (tr: RemoteException) {
            throw RuntimeException(tr.message, tr)
        }
    }

    fun grantRuntimePermission(packageName: String, permissionName: String, userId: Int) {
        if (!Shizuku.pingBinder()) {
            return
        }
        try {
            if (Build.VERSION.SDK_INT >= 30) {
                HiddenApiBridge.IPermissionManager_grantRuntimePermission(permissionManager, packageName, permissionName, userId)
            } else {
                HiddenApiBridge.IPackageManager_grantRuntimePermission(packageManager, packageName, permissionName, userId)
            }
        } catch (tr: RemoteException) {
            throw RuntimeException(tr.message, tr)
        }
    }

    fun revokeRuntimePermission(packageName: String, permissionName: String, userId: Int) {
        if (!Shizuku.pingBinder()) {
            return
        }
        try {
            if (Build.VERSION.SDK_INT >= 30) {
                HiddenApiBridge.IPermissionManager_revokeRuntimePermission(permissionManager, packageName, permissionName, userId, null)
            } else {
                HiddenApiBridge.IPackageManager_revokeRuntimePermission(packageManager, packageName, permissionName, userId)
            }
        } catch (tr: RemoteException) {
            throw RuntimeException(tr.message, tr)
        }
    }
}