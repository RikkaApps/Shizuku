package rikka.shizuku.server;

import android.content.pm.PackageInfo;
import android.os.IBinder;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import rikka.hidden.compat.PackageManagerApis;

/**
 * Android 17 (API 37) changed IPackageManager.getInstalledPackages(long, int) to return
 * android.content.pm.PackageInfoList instead of ParceledListSlice. hidden-compat 4.4.0 still calls the
 * old signature, fails, swallows the error and returns an empty list: Shizuku then lists 0 apps and
 * never sends its binder to clients at startup.
 * <p>
 * This calls the method by reflection and reads getList() from whatever type comes back.
 */
public final class InstalledPackagesCompat {

    private static volatile Method sMethod;
    private static volatile Object sPm;

    private InstalledPackagesCompat() {
    }

    @SuppressWarnings("unchecked")
    public static List<PackageInfo> get(long flags, int userId) {
        try {
            if (sMethod == null || sPm == null) {
                Class<?> sm = Class.forName("android.os.ServiceManager");
                IBinder binder = (IBinder) sm.getMethod("getService", String.class).invoke(null, "package");
                Class<?> stub = Class.forName("android.content.pm.IPackageManager$Stub");
                sPm = stub.getMethod("asInterface", IBinder.class).invoke(null, binder);
                Class<?> ipm = Class.forName("android.content.pm.IPackageManager");
                Method m;
                try {
                    m = ipm.getMethod("getInstalledPackages", long.class, int.class);
                } catch (NoSuchMethodException e) {
                    m = ipm.getMethod("getInstalledPackages", int.class, int.class);
                }
                sMethod = m;
            }
            Object result = sMethod.getParameterTypes()[0] == long.class
                    ? sMethod.invoke(sPm, flags, userId)
                    : sMethod.invoke(sPm, (int) flags, userId);
            if (result != null) {
                Object list = result.getClass().getMethod("getList").invoke(result);
                if (list != null) return (List<PackageInfo>) list;
            }
        } catch (Throwable ignored) {
            // fall through to the hidden-compat path used on older Android versions
        }
        List<PackageInfo> legacy = PackageManagerApis.getInstalledPackagesNoThrow(flags, userId);
        return legacy != null ? legacy : Collections.emptyList();
    }
}
