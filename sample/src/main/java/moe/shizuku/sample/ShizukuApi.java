package moe.shizuku.sample;

import android.app.ITaskStackListener;
import android.os.Build;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import moe.shizuku.api.ShizukuApiConstants;
import moe.shizuku.api.ShizukuClientV3;

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

    public static void registerTaskStackListener(ITaskStackListener taskStackListener) {
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
                ShizukuClientV3.getBinderThrow().transact(ShizukuApiConstants.BINDER_TRANSACTION_transactRemote, data, reply, 0);
                reply.readException();

                Log.i("ShizukuSample", "registerTaskStackListener");
            } catch (RemoteException e) {
                Log.e("ShizukuSample", "registerTaskStackListener", e);
            } finally {
                data.recycle();
                reply.recycle();
            }
        }
    }

    public static void unregisterTaskStackListener(ITaskStackListener taskStackListener) {
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
                ShizukuClientV3.getBinderThrow().transact(ShizukuApiConstants.BINDER_TRANSACTION_transactRemote, data, reply, 0);
                reply.readException();

                Log.i("ShizukuSample", "unregisterTaskStackListener");
            } catch (RemoteException e) {
                Log.e("ShizukuSample", "unregisterTaskStackListener", e);
            } finally {
                data.recycle();
                reply.recycle();
            }
        }
    }
}
