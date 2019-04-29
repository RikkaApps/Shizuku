package moe.shizuku.api;

import android.os.IBinder;
import android.os.Parcel;
import android.os.ServiceManager;

import androidx.annotation.NonNull;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SystemServiceHelper {

    private static Map<String, IBinder> systemServiceCache = new HashMap<>();
    private static Map<String, Integer> transactCodeCache = new HashMap<>();

    /**
     * Returns a reference to a service with the given name.
     *
     * @param name the name of the service to get such as "package" for android.content.pm.IPackageManager
     * @return a reference to the service, or <code>null</code> if the service doesn't exist
     */
    public static IBinder getSystemService(@NonNull String name) {
        IBinder binder = systemServiceCache.get(name);
        if (binder == null) {
            binder = ServiceManager.getService(name);
            systemServiceCache.put(name, binder);
        }
        return binder;
    }

    /**
     * Returns transaction code from given class and method name.
     *
     * @param className class name such as "android.content.pm.IPackageManager$Stub"
     * @param methodName method name such as "getInstalledPackages"
     * @return transaction code, or <code>null</code> if the class or the method doesn't exist
     */
    public static Integer getTransactionCode(@NonNull String className, @NonNull String methodName) {
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

    /**
     * Obtain a new data parcel for {@link ShizukuService#transactRemote(Parcel, Parcel, int)}.
     *
     * @param serviceName system service name
     * @param interfaceName class name for reflection
     * @param methodName method name for reflection
     * @return data parcel
     *
     * @throws NullPointerException can't get system service or transaction code
     */
    public static Parcel obtainParcel(@NonNull String serviceName, @NonNull String interfaceName, @NonNull String methodName) {
        return obtainParcel(serviceName, interfaceName, interfaceName + "$Stub", methodName);
    }

    /**
     * Obtain a new data parcel for {@link ShizukuService#transactRemote(Parcel, Parcel, int)}.
     *
     * @param serviceName system service name
     * @param interfaceName interface name
     * @param className class name for reflection
     * @param methodName method name for reflection
     * @return data parcel
     *
     * @throws NullPointerException can't get system service or transaction code
     */
    public static Parcel obtainParcel(@NonNull  String serviceName, @NonNull String interfaceName, @NonNull String className, @NonNull String methodName) {
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
