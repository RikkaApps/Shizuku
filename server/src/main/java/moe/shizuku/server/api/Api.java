package moe.shizuku.server.api;

import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.app.IProcessObserver;
import android.app.IUidObserver;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageInfo;
import android.os.IBinder;
import android.os.IUserManager;
import android.os.RemoteException;
import android.os.ServiceManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import moe.shizuku.server.utils.BinderSingleton;

import static moe.shizuku.server.utils.Logger.LOGGER;

public class Api {

    public static final BinderSingleton<IActivityManager> ACTIVITY_MANAGER_SINGLETON = new BinderSingleton<IActivityManager>() {

        @Override
        protected IActivityManager create() {
            //noinspection deprecation
            return ActivityManagerNative.getDefault();
        }
    };

    public static final BinderSingleton<IPackageManager> PACKAGE_MANAGER_SINGLETON = new BinderSingleton<IPackageManager>() {

        @Override
        protected IPackageManager create() {
            return IPackageManager.Stub.asInterface(ServiceManager.getService("package"));
        }
    };

    public static final BinderSingleton<IUserManager> USER_MANAGER_SINGLETON = new BinderSingleton<IUserManager>() {

        @Override
        protected IUserManager create() {
            return IUserManager.Stub.asInterface(ServiceManager.getService("user"));
        }
    };

    private static Method method_IActivityManager_getContentProviderExternal;
    private static int method_IActivityManager_getContentProviderExternal_paramCount;

    static {
        try {
            for (Method method : IActivityManager.class.getDeclaredMethods()) {
                if ("getContentProviderExternal".equals(method.getName())) {
                    method_IActivityManager_getContentProviderExternal = method;
                    method_IActivityManager_getContentProviderExternal_paramCount = method.getParameterTypes().length;
                }
            }
        } catch (Throwable tr) {
            LOGGER.e(tr, "reflection failed");
        }
    }

    public static int checkPermission(String permission, int pid, int uid) throws RemoteException {
        IActivityManager am = ACTIVITY_MANAGER_SINGLETON.get();
        if (am == null) {
            throw new RemoteException("can't get IActivityManager");
        }
        return am.checkPermission(permission, pid, uid);
    }

    public static PackageInfo getPackageInfo(String packageName, int flags, int userId) throws RemoteException {
        IPackageManager pm = PACKAGE_MANAGER_SINGLETON.get();
        if (pm == null) {
            throw new RemoteException("can't get IPackageManager");
        }
        return pm.getPackageInfo(packageName, flags, userId);
    }

    public static ApplicationInfo getApplicationInfo(String packageName, int flags, int userId) throws RemoteException {
        IPackageManager pm = PACKAGE_MANAGER_SINGLETON.get();
        if (pm == null) {
            throw new RemoteException("can't get IPackageManager");
        }
        return pm.getApplicationInfo(packageName, flags, userId);
    }

    public static int checkPermission(String permName, int uid) throws RemoteException {
        IPackageManager pm = PACKAGE_MANAGER_SINGLETON.get();
        if (pm == null) {
            throw new RemoteException("can't get IPackageManager");
        }
        return pm.checkUidPermission(permName, uid);
    }

    public static void registerProcessObserver(IProcessObserver processObserver) throws RemoteException {
        IActivityManager am = ACTIVITY_MANAGER_SINGLETON.get();
        if (am == null) {
            throw new RemoteException("can't get IActivityManager");
        }
        am.registerProcessObserver(processObserver);
    }

    public static void registerUidObserver(IUidObserver observer, int which, int cutpoint, String callingPackage) throws RemoteException {
        IActivityManager am = ACTIVITY_MANAGER_SINGLETON.get();
        if (am == null) {
            throw new RemoteException("can't get IActivityManager");
        }
        am.registerUidObserver(observer, which, cutpoint, callingPackage);
    }

    public static String[] getPackagesForUid(int uid) throws RemoteException {
        IPackageManager pm = PACKAGE_MANAGER_SINGLETON.get();
        if (pm == null) {
            throw new RemoteException("can't get IPackageManager");
        }
        return pm.getPackagesForUid(uid);
    }

    public static Object getContentProviderExternal(String name, int userId, IBinder token) throws RemoteException {
        IActivityManager am = ACTIVITY_MANAGER_SINGLETON.get();
        if (am == null) {
            throw new RemoteException("can't get IActivityManager");
        }

        try {
            if (method_IActivityManager_getContentProviderExternal_paramCount == 4) {
                // Android Q
                return method_IActivityManager_getContentProviderExternal.invoke(am, name, userId, token, name /* tag */);
            } else if (method_IActivityManager_getContentProviderExternal_paramCount == 3) {
                return method_IActivityManager_getContentProviderExternal.invoke(am, name, userId, token);
            } else {
                throw new NoSuchMethodError("android.app.IActivityManager.getContentProviderExternal");
            }
        } catch (IllegalAccessException e) {
            // should never happened
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getTargetException());
        }
    }

    public static void removeContentProviderExternal(String name, IBinder token) throws RemoteException {
        IActivityManager am = ACTIVITY_MANAGER_SINGLETON.get();
        if (am == null) {
            throw new RemoteException("can't get IActivityManager");
        }
        am.removeContentProviderExternal(name, token);
    }

}
