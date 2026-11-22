package moe.shizuku.common.util;

import android.content.pm.IPackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.ParceledListSlice;
import android.content.pm.UserInfo;
import android.os.Build;
import android.os.IUserManager;
import android.util.ArraySet;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import rikka.hidden.compat.util.SystemServiceBinder;

/** Compatibility helpers for platform app and user enumeration. */
public final class DiscoveryCompat {

    public static final String TAG = "ShizukuDiscovery";

    // SystemServiceBinder also applies the manager's ShizukuBinderWrapper listener.
    private static final SystemServiceBinder<IPackageManager> PACKAGE_MANAGER =
            new SystemServiceBinder<>("package", IPackageManager.Stub::asInterface);
    private static final SystemServiceBinder<IUserManager> USER_MANAGER =
            new SystemServiceBinder<>("user", IUserManager.Stub::asInterface);

    private DiscoveryCompat() {
    }

    @SuppressWarnings("unchecked")
    public static List<PackageInfo> getInstalledPackages(long flags, int userId) throws Throwable {
        IPackageManager packageManager = PACKAGE_MANAGER.get();
        if (packageManager == null) {
            throw new IllegalStateException("Package manager service is unavailable");
        }
        Method method = PlatformApiReflection.installedPackagesMethod(
                IPackageManager.class, Build.VERSION.SDK_INT >= 33);
        Object value = PlatformApiReflection.installedPackages(method, packageManager, flags, userId);
        // Android 17's PackageInfoList extends ParceledListSlice. Do not compile against
        // the new return class: the actual interface method is resolved on the device.
        if (!(value instanceof ParceledListSlice<?>)) {
            throw new IllegalStateException("getInstalledPackages returned "
                    + (value == null ? "null" : value.getClass().getName()));
        }
        List<PackageInfo> packages = ((ParceledListSlice<PackageInfo>) value).getList();
        if (packages == null) {
            throw new IllegalStateException("getInstalledPackages returned a null package list");
        }
        return packages;
    }

    /** Keep hidden-compat's best-effort result, but retain the cause in the system log. */
    public static List<PackageInfo> getInstalledPackagesNoThrow(long flags, int userId) {
        try {
            return getInstalledPackages(flags, userId);
        } catch (Throwable failure) {
            Log.w(TAG, "getInstalledPackages failed userId=" + userId + " flags=0x"
                    + Long.toHexString(flags), failure);
            return Collections.emptyList();
        }
    }

    @SuppressWarnings("unchecked")
    public static List<UserInfo> getUsers(boolean excludeDying) throws Throwable {
        IUserManager userManager = USER_MANAGER.get();
        if (userManager == null) {
            throw new IllegalStateException("User manager service is unavailable");
        }
        Method method = PlatformApiReflection.usersMethod(IUserManager.class);
        Object value = PlatformApiReflection.users(method, userManager, excludeDying);
        if (!(value instanceof List<?>)) {
            throw new IllegalStateException("getUsers returned "
                    + (value == null ? "null" : value.getClass().getName()));
        }
        return (List<UserInfo>) value;
    }

    /** Preserve hidden-compat's fallback to user 0 only when enumeration fails. */
    public static Collection<Integer> getUserIdsNoThrow() {
        Set<Integer> result = new ArraySet<>();
        try {
            for (UserInfo user : getUsers(true)) {
                result.add(user.id);
            }
        } catch (Throwable failure) {
            Log.w(TAG, "getUsers failed; adding fallback userId=0", failure);
            result.add(0);
        }
        return result;
    }
}
