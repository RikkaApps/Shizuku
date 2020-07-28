package moe.shizuku.server.api

import android.annotation.SuppressLint
import android.app.IActivityManager
import android.content.pm.ILauncherApps
import android.content.pm.IPackageManager
import android.os.IBinder
import android.os.IInterface
import android.os.IUserManager
import android.os.ServiceManager
import android.permission.IPermissionManager
import android.util.ArrayMap
import com.android.internal.app.IAppOpsService
import hidden.HiddenApiBridgeV23
import moe.shizuku.server.utils.BuildUtils
import moe.shizuku.server.utils.Logger.LOGGER

@SuppressLint("NewApi")
object SystemServiceProvider {

    interface Listener {
        fun onSystemRestarted()
    }

    private val serviceCache: MutableMap<String, IInterface> = ArrayMap()

    @JvmStatic
    var listener: Listener? = null

    private fun getService(name: String): IBinder? {
        return ServiceManager.getService(name)
    }

    private fun <T : IInterface> getServiceInterface(name: String, converter: (binder: IBinder) -> T): T? {
        synchronized(serviceCache) {
            var service = serviceCache[name]
            if (service != null && !service.asBinder().pingBinder()) {
                LOGGER.w("cached service '$name' is dead? clear all cached services...")
                serviceCache.clear()
                service = null
                if (listener != null) {
                    listener!!.onSystemRestarted()
                }
            }
            if (service == null) {
                val binder = getService(name) ?: return null
                service = converter.invoke(binder)
                serviceCache[name] = service
            }
            @Suppress("UNCHECKED_CAST")
            return service as T
        }
    }

    private fun <T> unsafeLazy(initializer: () -> T): Lazy<T> = kotlin.lazy(LazyThreadSafetyMode.NONE, initializer)

    val activityManager by unsafeLazy {
        getServiceInterface<IActivityManager>("activity") {
            if (BuildUtils.atLeast26()) {
                IActivityManager.Stub.asInterface(it)
            } else {
                HiddenApiBridgeV23.ActivityManagerNative_asInterface(it)
            }
        }
    }

    val packageManager by unsafeLazy {
        getServiceInterface<IPackageManager>("package") {
            IPackageManager.Stub.asInterface(it)
        }
    }

    val userManager by unsafeLazy {
        getServiceInterface<IUserManager>("user") {
            IUserManager.Stub.asInterface(it)
        }
    }

    val appOpsService by unsafeLazy {
        getServiceInterface<IAppOpsService>("appops") {
            IAppOpsService.Stub.asInterface(it)
        }
    }

    val launcherApps by unsafeLazy {
        getServiceInterface<ILauncherApps>("launcherapps") {
            ILauncherApps.Stub.asInterface(it)
        }
    }

    val permissionManager by unsafeLazy {
        getServiceInterface<IPermissionManager>("permissionmgr") {
            IPermissionManager.Stub.asInterface(it)
        }
    }
}