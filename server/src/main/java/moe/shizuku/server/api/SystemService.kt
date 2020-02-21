package moe.shizuku.server.api

import android.app.IProcessObserver
import android.app.IUidObserver
import android.content.IContentProvider
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.ParceledListSlice
import android.content.pm.UserInfo
import android.os.IBinder
import android.os.RemoteException
import hidden.HiddenApiBridgeV23
import moe.shizuku.server.utils.BuildUtils
import moe.shizuku.server.utils.Logger.LOGGER
import java.util.*

object SystemService {

    @JvmStatic
    @Throws(RemoteException::class)
    fun checkPermission(permission: String?, pid: Int, uid: Int): Int {
        val am = SystemServiceProvider.activityManager ?: throw RemoteException("can't get IActivityManager")
        return am.checkPermission(permission, pid, uid)
    }

    @JvmStatic
    @Throws(RemoteException::class)
    fun getPackageInfo(packageName: String?, flags: Int, userId: Int): PackageInfo? {
        val pm = SystemServiceProvider.packageManager ?: throw RemoteException("can't get IPackageManger")
        return pm.getPackageInfo(packageName, flags, userId)
    }

    @JvmStatic
    @Throws(RemoteException::class)
    fun getApplicationInfo(packageName: String?, flags: Int, userId: Int): ApplicationInfo? {
        val pm = SystemServiceProvider.packageManager ?: throw RemoteException("can't get IPackageManger")
        return pm.getApplicationInfo(packageName, flags, userId)
    }

    @JvmStatic
    @Throws(RemoteException::class)
    fun checkPermission(permName: String?, uid: Int): Int {
        return if (BuildUtils.atLeast30()) {
            val permmgr = SystemServiceProvider.permissionManager ?: throw RemoteException("can't get IPermission")
            permmgr.checkUidPermission(permName, uid)
        } else {
            val pm = SystemServiceProvider.packageManager ?: throw RemoteException("can't get IPackageManger")
            pm.checkUidPermission(permName, uid)
        }
    }

    @JvmStatic
    @Throws(RemoteException::class)
    fun registerProcessObserver(processObserver: IProcessObserver?) {
        val am = SystemServiceProvider.activityManager ?: throw RemoteException("can't get IActivityManager")
        am.registerProcessObserver(processObserver)
    }

    @JvmStatic
    @Throws(RemoteException::class)
    fun registerUidObserver(observer: IUidObserver?, which: Int, cutpoint: Int, callingPackage: String?) {
        val am = SystemServiceProvider.activityManager ?: throw RemoteException("can't get IActivityManager")
        am.registerUidObserver(observer, which, cutpoint, callingPackage)
    }

    @JvmStatic
    @Throws(RemoteException::class)
    fun getPackagesForUid(uid: Int): Array<String?>? {
        val pm = SystemServiceProvider.packageManager ?: throw RemoteException("can't get IPackageManger")
        return pm.getPackagesForUid(uid)
    }

    @JvmStatic
    @Throws(RemoteException::class)
    fun getInstalledApplications(flags: Int, userId: Int): ParceledListSlice<ApplicationInfo>? {
        val pm = SystemServiceProvider.packageManager ?: throw RemoteException("can't get IPackageManager")
        @Suppress("UNCHECKED_CAST")
        return pm.getInstalledApplications(flags, userId) as ParceledListSlice<ApplicationInfo>?
    }

    @JvmStatic
    @Throws(RemoteException::class)
    fun getInstalledPackages(flags: Int, userId: Int): ParceledListSlice<PackageInfo>? {
        val pm = SystemServiceProvider.packageManager ?: throw RemoteException("can't get IPackageManager")
        @Suppress("UNCHECKED_CAST")
        return pm.getInstalledPackages(flags, userId) as ParceledListSlice<PackageInfo>?
    }

    @JvmStatic
    @Throws(RemoteException::class)
    fun getContentProviderExternal(name: String?, userId: Int, token: IBinder?, tag: String?): IContentProvider? {
        val am = SystemServiceProvider.activityManager ?: throw RemoteException("can't get IActivityManager")
        return when {
            BuildUtils.atLeast29() -> {
                am.getContentProviderExternal(name, userId, token, tag)?.provider
            }
            BuildUtils.atLeast26() -> {
                am.getContentProviderExternal(name, userId, token)?.provider
            }
            else -> {
                HiddenApiBridgeV23.getContentProviderExternal_provider(am, name, userId, token)
            }
        }
    }

    @JvmStatic
    @Throws(RemoteException::class)
    fun removeContentProviderExternal(name: String?, token: IBinder?) {
        val am = SystemServiceProvider.activityManager ?: throw RemoteException("can't get IActivityManager")
        am.removeContentProviderExternal(name, token)
    }

    @JvmStatic
    @Throws(RemoteException::class)
    fun getUsers(): List<UserInfo> {
        val um = SystemServiceProvider.userManager ?: throw RemoteException("can't get IUserManger")
        return if (BuildUtils.atLeast30()) {
            um.getUsers(true, true, true)
        } else {
            um.getUsers(true)
        }
    }

    @JvmStatic
    fun getInstalledPackagesNoThrow(flags: Int, userId: Int): List<PackageInfo> {
        return try {
            getInstalledPackages(flags, userId)?.list ?: emptyList()
        } catch (tr: Throwable) {
            LOGGER.w(tr, "getInstalledPackages failed: flags=%d, user=%d", flags, userId)
            emptyList()
        }
    }

    @JvmStatic
    fun getInstalledApplicationsNoThrow(flags: Int, userId: Int): List<ApplicationInfo> {
        return try {
            getInstalledApplications(flags, userId)?.list ?: emptyList()
        } catch (tr: Throwable) {
            LOGGER.w(tr, "getInstalledApplications failed: flags=%d, user=%d", flags, userId)
            emptyList()
        }
    }

    @JvmStatic
    fun getPackageInfoNoThrow(packageName: String?, flags: Int, userId: Int): PackageInfo? {
        return try {
            getPackageInfo(packageName, flags, userId)
        } catch (tr: Throwable) {
            LOGGER.w(tr, "getPackageInfo failed: packageName=%s, flags=%d, user=%d", packageName, flags, userId)
            null
        }
    }

    @JvmStatic
    fun getApplicationInfoNoThrow(packageName: String?, flags: Int, userId: Int): ApplicationInfo? {
        return try {
            getApplicationInfo(packageName, flags, userId)
        } catch (tr: Throwable) {
            LOGGER.w(tr, "getApplicationInfo failed: packageName=%s, flags=%d, user=%d", packageName, flags, userId)
            null
        }
    }

    @JvmStatic
    fun getUsersNoThrow(): List<Int> {
        val users = ArrayList<Int>()
        try {
            for (ui in getUsers()) {
                users.add(ui.id)
            }
        } catch (tr: Throwable) {
            users.clear()
            users.add(0)
        }
        return users
    }

    @JvmStatic
    fun getPackagesForUidNoThrow(uid: Int): List<String> {
        val packages = ArrayList<String>()
        try {
            packages.addAll(getPackagesForUid(uid)?.filterNotNull().orEmpty())
        } catch (tr: Throwable) {
        }
        return packages
    }

    @JvmStatic
    fun forceStopPackageNoThrow(packageName: String?, userId: Int) {
        val am = SystemServiceProvider.activityManager ?: throw RemoteException("can't get IActivityManager")
        try {
            am.forceStopPackage(packageName, userId)
        } catch (e: Exception) {
        }
    }
}