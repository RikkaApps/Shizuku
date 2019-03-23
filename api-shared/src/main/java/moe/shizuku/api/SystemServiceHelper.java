package moe.shizuku.api;

import android.os.IBinder;
import android.os.Parcel;
import android.os.ServiceManager;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SystemServiceHelper {

    private static Map<String, IBinder> systemServiceCache = new HashMap<>();
    private static Map<String, Integer> transactCodeCache = new HashMap<>();

    public static IBinder getSystemService(String name) {
        IBinder binder = systemServiceCache.get(name);
        if (binder == null) {
            binder = ServiceManager.getService(name);
            systemServiceCache.put(name, binder);
        }
        return binder;
    }

    public static Integer getTransactionCode(String className, String methodName) {
        final String fieldName = "TRANSACTION_" + methodName;
        final String key = className + "." + fieldName;

        Integer value = transactCodeCache.get(key);
        if (value != null) return value;

        try {
            final Class<?> cls = Class.forName(className);
            final Field declaredField = cls.getDeclaredField(fieldName);
            declaredField.setAccessible(true);
            value = declaredField.getInt(cls);

            transactCodeCache.put(key, value);
            return value;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Parcel obtainParcel(String serviceName, String interfaceName, String methodName) {
        return obtainParcel(serviceName, interfaceName, interfaceName + "$Stub", methodName);
    }

    /**
     * +
     *
     * @throws NullPointerException Can't get system service or transaction code
     */
    public static Parcel obtainParcel(final String serviceName, final String interfaceName, final String className, final String methodName) {
        IBinder binder = getSystemService(serviceName);
        Integer code = getTransactionCode(className, methodName);

        Objects.requireNonNull(binder, "can't find system service " + serviceName);
        Objects.requireNonNull(code, "can't find transaction code of " + methodName + " in " + className);

        Parcel data = Parcel.obtain();
        data.writeInterfaceToken(ShizukuApiConstants.BINDER_DESCRIPTOR);
        data.writeStrongBinder(binder);
        data.writeInt(code);
        data.writeInterfaceToken(interfaceName);
        return data;
    }
}
