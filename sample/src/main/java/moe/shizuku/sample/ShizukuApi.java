package moe.shizuku.sample;

import android.app.ITaskStackListener;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.ParceledListSlice;
import android.content.pm.UserInfo;
import android.os.Build;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import moe.shizuku.api.ShizukuApiConstants;
import moe.shizuku.api.ShizukuService;

public class ShizukuApi {

    private static Map<String, IBinder> systemServiceCache = new HashMap<>();
    private static Map<String, Integer> transactCodeCache = new HashMap<>();

    private static IBinder getSystemService(final String name) {
        IBinder binder = systemServiceCache.get(name);
        if (binder == null) {
            binder = ServiceManager.getService(name);
            systemServiceCache.put(name, binder);
        }
        return binder;
    }

    private static Integer getTransactionCode(final String className, final String methodName) {
        final String stubName = className + "$Stub";
        final String fieldName = "TRANSACTION_" + methodName;
        final String key = stubName + "." + fieldName;

        Integer value = transactCodeCache.get(key);
        if (value != null) return value;

        try {
            final Class<?> cls = Class.forName(stubName);
            final Field declaredField = cls.getDeclaredField(fieldName);
            declaredField.setAccessible(true);
            value = declaredField.getInt(cls);

            transactCodeCache.put(key, value);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return value;
    }

    public static void ActivityManager_registerTaskStackListener(ITaskStackListener taskStackListener) {
        if (Build.VERSION.SDK_INT >= 26) {
            int code = getTransactionCode("android.app.IActivityManager", "registerTaskStackListener");

            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            data.writeInterfaceToken(ShizukuApiConstants.BINDER_DESCRIPTOR);
            data.writeStrongBinder(getSystemService("activity"));
            data.writeInt(code);
            data.writeInterfaceToken("android.app.IActivityManager");
            data.writeStrongBinder(taskStackListener.asBinder());
            try {
                ShizukuService.transactRemote(data, reply, 0);
                reply.readException();

                Log.i("ShizukuSample", "ActivityManager#registerTaskStackListener");
            } catch (RemoteException e) {
                Log.e("ShizukuSample", "ActivityManager#registerTaskStackListener", e);
            } finally {
                data.recycle();
                reply.recycle();
            }
        }
    }

    public static void ActivityManager_unregisterTaskStackListener(ITaskStackListener taskStackListener) {
        if (Build.VERSION.SDK_INT >= 26) {
            int code = getTransactionCode("android.app.IActivityManager", "unregisterTaskStackListener");

            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            data.writeInterfaceToken(ShizukuApiConstants.BINDER_DESCRIPTOR);
            data.writeStrongBinder(getSystemService("activity"));
            data.writeInt(code);
            data.writeInterfaceToken("android.app.IActivityManager");
            data.writeStrongBinder(taskStackListener.asBinder());
            try {
                ShizukuService.transactRemote(data, reply, 0);
                reply.readException();

                Log.i("ShizukuSample", "ActivityManager#unregisterTaskStackListener");
            } catch (RemoteException e) {
                Log.e("ShizukuSample", "ActivityManager#unregisterTaskStackListener", e);
            } finally {
                data.recycle();
                reply.recycle();
            }
        }
    }

    public static List<UserInfo> UserManager_getUsers(boolean excludeDying) {
        int code = getTransactionCode("android.os.IUserManager", "getUsers");

        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(ShizukuApiConstants.BINDER_DESCRIPTOR);
        data.writeStrongBinder(getSystemService(Context.USER_SERVICE));
        data.writeInt(code);
        data.writeInterfaceToken("android.os.IUserManager");
        data.writeInt(excludeDying ? 1 : 0);

        List<UserInfo> res = null;
        try {
            ShizukuService.transactRemote(data, reply, 0);
            reply.readException();
            res = reply.createTypedArrayList(UserInfo.CREATOR);

            Log.i("ShizukuSample", "UserManager#getUsers");
        } catch (RemoteException e) {
            Log.e("ShizukuSample", "UserManager#getUsers", e);
        } finally {
            data.recycle();
            reply.recycle();
        }
        return res;
    }

    public static List<PackageInfo> PackageManager_getInstalledPackages(int flags, int userId) {
        int code = getTransactionCode("android.content.pm.IPackageManager", "getInstalledPackages");

        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(ShizukuApiConstants.BINDER_DESCRIPTOR);
        data.writeStrongBinder(getSystemService("package"));
        data.writeInt(code);
        data.writeInterfaceToken("android.content.pm.IPackageManager");
        data.writeInt(flags);
        data.writeInt(userId);

        try {
            ShizukuService.transactRemote(data, reply, 0);
            reply.readException();
            if (reply.readInt() != 0) {
                //noinspection unchecked
                ParceledListSlice<PackageInfo> listSlice = ParceledListSlice.CREATOR.createFromParcel(reply);
                return listSlice.getList();
            }
            return null;
        } catch (RemoteException tr) {
            Log.e("ShizukuSample", "PackageManager#getInstalledPackages", tr);
        } finally {
            data.recycle();
            reply.recycle();
        }
        return null;
    }
}
