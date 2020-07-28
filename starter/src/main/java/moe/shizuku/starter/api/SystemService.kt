package moe.shizuku.starter.api

import android.content.IContentProvider
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.os.Build
import android.os.IBinder
import android.os.RemoteException
import hidden.HiddenApiBridgeV23

object SystemService {

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
    fun getContentProviderExternal(name: String?, userId: Int, token: IBinder?, tag: String?): IContentProvider? {
        val am = SystemServiceProvider.activityManager ?: throw RemoteException("can't get IActivityManager")
        return when {
            Build.VERSION.SDK_INT >= 29 -> {
                am.getContentProviderExternal(name, userId, token, tag)?.provider
            }
            Build.VERSION.SDK_INT >= 26 -> {
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
    fun getPackageInfoNoThrow(packageName: String?, flags: Int, userId: Int): PackageInfo? {
        return try {
            getPackageInfo(packageName, flags, userId)
        } catch (tr: Throwable) {
            null
        }
    }

    @JvmStatic
    fun getApplicationInfoNoThrow(packageName: String?, flags: Int, userId: Int): ApplicationInfo? {
        return try {
            getApplicationInfo(packageName, flags, userId)
        } catch (tr: Throwable) {
            null
        }
    }
}