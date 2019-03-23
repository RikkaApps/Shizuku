package moe.shizuku.manager.authorization;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ParceledListSlice;
import android.os.Parcel;
import android.os.Process;

import java.util.ArrayList;
import java.util.List;

import moe.shizuku.api.ShizukuService;
import moe.shizuku.api.SystemServiceHelper;
import moe.shizuku.manager.Manifest;

@SuppressWarnings("SameParameterValue")
public class AuthorizationManagerImplV23 implements AuthorizationManagerImpl {

    private static List<PackageInfo> getInstalledPackages(int flags, int userId) {
        if (!ShizukuService.pingBinder()) {
            return new ArrayList<>();
        }

        Parcel data = SystemServiceHelper.obtainParcel("package", "android.content.pm.IPackageManager", "getInstalledPackages");
        Parcel reply = Parcel.obtain();
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
            return new ArrayList<>();
        } catch (Throwable tr) {
            throw new RuntimeException(tr.getMessage(), tr);
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    private static int checkPermission(String permName, String pkgName, int userId) throws RuntimeException {
        if (!ShizukuService.pingBinder()) {
            return PackageManager.PERMISSION_DENIED;
        }

        Parcel data = SystemServiceHelper.obtainParcel("package", "android.content.pm.IPackageManager", "checkPermission");
        Parcel reply = Parcel.obtain();
        data.writeString(permName);
        data.writeString(pkgName);
        data.writeInt(userId);

        try {
            ShizukuService.transactRemote(data, reply, 0);
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
        if (!ShizukuService.pingBinder()) {
            return;
        }

        Parcel data = SystemServiceHelper.obtainParcel("package", "android.content.pm.IPackageManager", "grantRuntimePermission");
        Parcel reply = Parcel.obtain();
        data.writeString(packageName);
        data.writeString(permissionName);
        data.writeInt(userId);

        try {
            ShizukuService.transactRemote(data, reply, 0);
            reply.readException();
        } catch (Throwable tr) {
            throw new RuntimeException(tr.getMessage(), tr);
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    private static void revokeRuntimePermission(String packageName, String permissionName, int userId) throws RuntimeException {
        if (!ShizukuService.pingBinder()) {
            return;
        }

        Parcel data = SystemServiceHelper.obtainParcel("package", "android.content.pm.IPackageManager", "revokeRuntimePermission");
        Parcel reply = Parcel.obtain();
        data.writeString(packageName);
        data.writeString(permissionName);
        data.writeInt(userId);

        try {
            ShizukuService.transactRemote(data, reply, 0);
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
