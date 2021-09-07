package moe.shizuku.manager.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.*
import android.os.Build
import android.os.IUserManager
import android.os.RemoteException
import android.permission.IPermissionManager
import rikka.core.ktx.unsafeLazy
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuBinderWrapper
import rikka.shizuku.SystemServiceHelper
import java.util.*

object ShizukuSystemApis {

    private val packageManager by unsafeLazy {
        IPackageManager.Stub.asInterface(ShizukuBinderWrapper(SystemServiceHelper.getSystemService("package")))
    }

    @delegate:SuppressLint("NewApi")
    private val permissionManager by unsafeLazy {
       IPermissionManager.Stub.asInterface(ShizukuBinderWrapper(SystemServiceHelper.getSystemService("permissionmgr")))
    }

    private val userManager by unsafeLazy {
        IUserManager.Stub.asInterface(ShizukuBinderWrapper(SystemServiceHelper.getSystemService(Context.USER_SERVICE)))
    }

    private val users = arrayListOf<UserInfoCompat>()

    private fun getUsers(): List<UserInfoCompat> {
        return if (!Shizuku.pingBinder()) {
            arrayListOf(UserInfoCompat(UserHandleCompat.myUserId(), "Owner"))
        } else try {
            val list = try {
                userManager.getUsers(true, true, true)
            } catch (e: NoSuchMethodError) {
                userManager.getUsers(true)
            }
            val users: MutableList<UserInfoCompat> = ArrayList<UserInfoCompat>()
            for (ui in list) {
                users.add(UserInfoCompat(ui.id, ui.name))
            }
            return users
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
            val listSlice: ParceledListSlice<PackageInfo>? = packageManager.getInstalledPackages(flags, userId) as ParceledListSlice<PackageInfo>?
            return if (listSlice != null) {
                listSlice.list
            } else ArrayList()
        } catch (tr: RemoteException) {
            throw RuntimeException(tr.message, tr)
        }
    }

    fun checkPermission(permName: String, pkgName: String, userId: Int): Int {
        return if (!Shizuku.pingBinder()) {
            PackageManager.PERMISSION_DENIED
        } else try {
            if (Build.VERSION.SDK_INT >= 31 || Build.VERSION.SDK_INT == 30 && Build.VERSION.PREVIEW_SDK_INT > 0) {
                packageManager.checkPermission(permName, pkgName, userId)
            } else if (Build.VERSION.SDK_INT >= 30) {
                permissionManager.checkPermission(permName, pkgName, userId)
            } else {
                packageManager.checkPermission(permName, pkgName, userId)
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
                permissionManager.grantRuntimePermission(packageName, permissionName, userId)
            } else {
                packageManager.grantRuntimePermission( packageName, permissionName, userId)
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
                permissionManager.revokeRuntimePermission(packageName, permissionName, userId, null)
            } else {
                packageManager.revokeRuntimePermission(packageName, permissionName, userId)
            }
        } catch (tr: RemoteException) {
            throw RuntimeException(tr.message, tr)
        }
    }
}
