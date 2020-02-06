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
        val pm = SystemServiceProvider.packageManager ?: throw RemoteException("can't get IPackageManger")
        return pm.checkUidPermission(permName, uid)
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
    fun getPackagesForUid(uid: Int): Array<String> {
        val pm = SystemServiceProvider.packageManager ?: throw RemoteException("can't get IPackageManger")
        return pm.getPackagesForUid(uid)
    }

    @Throws(RemoteException::class)
    fun getInstalledApplications(flags: Int, userId: Int): List<ApplicationInfo> {
        val pm = SystemServiceProvider.packageManager ?: throw RemoteException("can't get IPackageManger")
        @Suppress("UNCHECKED_CAST")
        val list: ParceledListSlice<ApplicationInfo>? = pm.getInstalledApplications(flags, userId) as ParceledListSlice<ApplicationInfo>?
        return if (list != null) list.list else ArrayList()
    }

    @JvmStatic
    @Throws(RemoteException::class)
    fun getInstalledPackages(flags: Int, userId: Int): List<PackageInfo> {
        val pm = SystemServiceProvider.packageManager ?: throw RemoteException("can't get IPackageManger")
        @Suppress("UNCHECKED_CAST")
        val list: ParceledListSlice<PackageInfo>? = pm.getInstalledPackages(flags, userId) as ParceledListSlice<PackageInfo>?
        return if (list != null) list.list else ArrayList()
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
    fun getUsers(excludeDying: Boolean): List<UserInfo> {
        val um = SystemServiceProvider.userManager ?: throw RemoteException("can't get IUserManger")
        return um.getUsers(excludeDying)
    }

    @JvmStatic
    fun getUsersNoThrow(): List<Int> {
        val users = ArrayList<Int>()
        try {
            for (ui in getUsers(true)) {
                users.add(ui.id)
            }
        } catch (tr: Throwable) {
            users.clear()
            users.add(0)
        }
        return users
    }
}