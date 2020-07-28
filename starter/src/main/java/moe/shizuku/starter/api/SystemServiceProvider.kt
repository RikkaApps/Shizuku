package moe.shizuku.starter.api

import android.annotation.SuppressLint
import android.app.IActivityManager
import android.content.pm.ILauncherApps
import android.content.pm.IPackageManager
import android.os.*
import android.permission.IPermissionManager
import android.util.ArrayMap
import com.android.internal.app.IAppOpsService
import hidden.HiddenApiBridgeV23

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
            if (Build.VERSION.SDK_INT >= 26) {
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
}