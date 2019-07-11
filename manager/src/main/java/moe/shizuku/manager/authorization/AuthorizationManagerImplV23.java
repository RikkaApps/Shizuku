package moe.shizuku.manager.authorization;

import android.content.pm.IPackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ParceledListSlice;
import android.os.Process;
import android.os.RemoteException;

import java.util.ArrayList;
import java.util.List;

import moe.shizuku.api.ShizukuBinderWrapper;
import moe.shizuku.api.ShizukuService;
import moe.shizuku.api.SystemServiceHelper;
import moe.shizuku.manager.Manifest;

@SuppressWarnings("SameParameterValue")
public class AuthorizationManagerImplV23 implements AuthorizationManagerImpl {

    private static final IPackageManager PACKAGE_MANAGER = IPackageManager.Stub.asInterface(new ShizukuBinderWrapper(SystemServiceHelper.getSystemService("package")));

    private static IPackageManager getPackageManager() {
        return PACKAGE_MANAGER;
    }

    private static List<PackageInfo> getInstalledPackages(int flags, int userId) {
        if (!ShizukuService.pingBinder()) {
            return new ArrayList<>();
        }

        try {
            ParceledListSlice<PackageInfo> listSlice = getPackageManager().getInstalledPackages(flags, userId);
            if (listSlice != null) {
                return listSlice.getList();
            }
            return new ArrayList<>();
        } catch (RemoteException tr) {
            throw new RuntimeException(tr.getMessage(), tr);
        }
    }

    private static int checkPermission(String permName, String pkgName, int userId) throws RuntimeException {
        if (!ShizukuService.pingBinder()) {
            return PackageManager.PERMISSION_DENIED;
        }

        try {
            return getPackageManager().checkPermission(permName, pkgName, userId);
        } catch (RemoteException tr) {
            throw new RuntimeException(tr.getMessage(), tr);
        }
    }

    private static void grantRuntimePermission(String packageName, String permissionName, int userId) throws RuntimeException {
        if (!ShizukuService.pingBinder()) {
            return;
        }

        try {
            getPackageManager().grantRuntimePermission(packageName, permissionName, userId);
        } catch (RemoteException tr) {
            throw new RuntimeException(tr.getMessage(), tr);
        }
    }

    private static void revokeRuntimePermission(String packageName, String permissionName, int userId) throws RuntimeException {
        if (!ShizukuService.pingBinder()) {
            return;
        }

        try {
            getPackageManager().revokeRuntimePermission(packageName, permissionName, userId);
        } catch (RemoteException tr) {
            throw new RuntimeException(tr.getMessage(), tr);
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
