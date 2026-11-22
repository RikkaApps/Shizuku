package moe.shizuku.common.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/** Reflection limited to the platform signatures used by app discovery. */
final class PlatformApiReflection {

    private PlatformApiReflection() {
    }

    static Method installedPackagesMethod(Class<?> packageManager, boolean longFlags)
            throws NoSuchMethodException {
        // Looking up by parameters avoids encoding the Android 17 return type in bytecode.
        return packageManager.getMethod("getInstalledPackages",
                longFlags ? long.class : int.class, int.class);
    }

    static Object installedPackages(Method method, Object packageManager, long flags, int userId)
            throws Throwable {
        if (method.getParameterTypes()[0] == long.class) {
            // Keep separate branches: a numeric ternary would promote the int to a long.
            return invoke(method, packageManager, Long.valueOf(flags), Integer.valueOf(userId));
        }
        if (flags < Integer.MIN_VALUE || flags > 0xffff_ffffL) {
            throw new IllegalArgumentException("Package flags do not fit the legacy 32-bit API: "
                    + Long.toHexString(flags));
        }
        return invoke(method, packageManager, Integer.valueOf((int) flags), Integer.valueOf(userId));
    }

    static Method usersMethod(Class<?> userManager) throws NoSuchMethodException {
        try {
            // Android 7-10 and later Android 16 / Android 17 releases use this signature.
            return userManager.getMethod("getUsers", boolean.class);
        } catch (NoSuchMethodException missingSingleBoolean) {
            // Android 11 through earlier Android 16 releases use three booleans.
            return userManager.getMethod("getUsers", boolean.class, boolean.class, boolean.class);
        }
    }

    static Object users(Method method, Object userManager, boolean excludeDying) throws Throwable {
        if (method.getParameterTypes().length == 1) {
            return invoke(method, userManager, Boolean.valueOf(excludeDying));
        }
        return invoke(method, userManager, Boolean.TRUE, Boolean.valueOf(excludeDying), Boolean.TRUE);
    }

    private static Object invoke(Method method, Object receiver, Object... args) throws Throwable {
        try {
            return method.invoke(receiver, args);
        } catch (InvocationTargetException exception) {
            // Keep SecurityException, RemoteException and linkage errors intact for the caller.
            Throwable cause = exception.getCause();
            throw cause == null ? exception : cause;
        }
    }
}
