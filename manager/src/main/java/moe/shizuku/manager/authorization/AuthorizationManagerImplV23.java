package moe.shizuku.manager.authorization;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ParceledListSlice;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Process;
import android.os.ServiceManager;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import moe.shizuku.api.ShizukuApiConstants;
import moe.shizuku.api.ShizukuClientV3;
import moe.shizuku.manager.Manifest;

@SuppressWarnings("SameParameterValue")
public class AuthorizationManagerImplV23 implements AuthorizationManagerImpl {

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

    private static List<PackageInfo> getInstalledPackages(int flags, int userId) {
        if (ShizukuClientV3.getBinder() == null) {
            return new ArrayList<>();
        }

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
            ShizukuClientV3.getBinderThrow().transact(ShizukuApiConstants.BINDER_TRANSACTION_transactRemote, data, reply, 0);
            reply.readException();
            if (reply.readInt() != 0) {
                //noinspection unchecked
                ParceledListSlice<PackageInfo> listSlice = ParceledListSlice.CREATOR.createFromParcel(reply);
                return listSlice.getList();
            }
            return new ArrayList<>();
        } catch (Throwable tr) {
            throw new RuntimeException(tr.getMessage(), tr);
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    private static int checkPermission(String permName, String pkgName, int userId) throws RuntimeException {
        if (ShizukuClientV3.getBinder() == null) {
            return PackageManager.PERMISSION_DENIED;
        }

        int code = getTransactionCode("android.content.pm.IPackageManager", "checkPermission");

        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(ShizukuApiConstants.BINDER_DESCRIPTOR);
        data.writeStrongBinder(getSystemService("package"));
        data.writeInt(code);
        data.writeInterfaceToken("android.content.pm.IPackageManager");
        data.writeString(permName);
        data.writeString(pkgName);
        data.writeInt(userId);

        try {
            ShizukuClientV3.getBinderThrow().transact(ShizukuApiConstants.BINDER_TRANSACTION_transactRemote, data, reply, 0);
            reply.readException();
            return reply.readInt();
        } catch (Throwable tr) {
            throw new RuntimeException(tr.getMessage(), tr);
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    private static void grantRuntimePermission(String packageName, String permissionName, int userId) throws RuntimeException {
        if (ShizukuClientV3.getBinder() == null) {
            return;
        }

        int code = getTransactionCode("android.content.pm.IPackageManager", "grantRuntimePermission");

        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(ShizukuApiConstants.BINDER_DESCRIPTOR);
        data.writeStrongBinder(getSystemService("package"));
        data.writeInt(code);
        data.writeInterfaceToken("android.content.pm.IPackageManager");
        data.writeString(packageName);
        data.writeString(permissionName);
        data.writeInt(userId);

        try {
            ShizukuClientV3.getBinderThrow().transact(ShizukuApiConstants.BINDER_TRANSACTION_transactRemote, data, reply, 0);
            reply.readException();
        } catch (Throwable tr) {
            throw new RuntimeException(tr.getMessage(), tr);
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    private static void revokeRuntimePermission(String packageName, String permissionName, int userId) throws RuntimeException {
        if (ShizukuClientV3.getBinder() == null) {
            return;
        }

        int code = getTransactionCode("android.content.pm.IPackageManager", "revokeRuntimePermission");

        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(ShizukuApiConstants.BINDER_DESCRIPTOR);
        data.writeStrongBinder(getSystemService("package"));
        data.writeInt(code);
        data.writeInterfaceToken("android.content.pm.IPackageManager");
        data.writeString(packageName);
        data.writeString(permissionName);
        data.writeInt(userId);

        try {
            ShizukuClientV3.getBinderThrow().transact(ShizukuApiConstants.BINDER_TRANSACTION_transactRemote, data, reply, 0);
            reply.readException();
        } catch (Throwable tr) {
            throw new RuntimeException(tr.getMessage(), tr);
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override
    public List<String> getPackages() {
        List<String> packages = new ArrayList<>();

        for (PackageInfo pi : getInstalledPackages(PackageManager.GET_PERMISSIONS, Process.myUid() / 100000)) {
            if (pi.requestedPermissions == null)
                continue;

            for (String p : pi.requestedPermissions) {
                if (Manifest.permission.API_V23.equals(p)) {
                    packages.add(pi.packageName);
                    break;
                }
            }
        }
        return packages;
    }

    @Override
    public boolean granted(final String packageName) {
        return checkPermission(Manifest.permission.API_V23, packageName, Process.myUid() / 100000) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void grant(final String packageName) {
        grantRuntimePermission(packageName, Manifest.permission.API_V23, Process.myUid() / 100000);
    }

    @Override
    public void revoke(final String packageName) {
        revokeRuntimePermission(packageName, Manifest.permission.API_V23, Process.myUid() / 100000);
    }
}
