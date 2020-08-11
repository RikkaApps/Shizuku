package hidden;

import android.app.ActivityManager;
import android.app.ActivityThread;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.pm.IPackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ParceledListSlice;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.permission.IPermissionManager;

import androidx.annotation.RequiresApi;

import com.android.internal.app.IAppOpsService;
import com.android.org.conscrypt.Conscrypt;

import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSocket;

public class HiddenApiBridge {

    public static int getActivityManager_UID_OBSERVER_ACTIVE() {
        return ActivityManager.UID_OBSERVER_ACTIVE;
    }

    public static int getActivityManager_PROCESS_STATE_UNKNOWN() {
        return ActivityManager.PROCESS_STATE_UNKNOWN;
    }

    public static UserHandle createUserHandle(int userId) {
        return new UserHandle(userId);
    }

    public static Context getSystemContext() {
        return ActivityThread.systemMain().getSystemContext();
    }

    public static int UserHandle_getIdentifier(UserHandle userHandle) {
        return userHandle.getIdentifier();
    }

    public static List<?> getOpsForPackage(IAppOpsService appOpsService, int uid, String packageName, int[] ops) throws RemoteException {
        return appOpsService.getOpsForPackage(uid, packageName, ops);
    }

    @RequiresApi(26)
    public static List<?> getUidOps(IAppOpsService appOpsService, int uid, int[] ops) throws RemoteException {
        return appOpsService.getUidOps(uid, ops);
    }

    public static int permissionToOpCode(String permission) {
        return AppOpsManager.permissionToOpCode(permission);
    }

    public static List<?> PackageOps_getOps(Object _packageOps) {
        AppOpsManager.PackageOps packageOps = (AppOpsManager.PackageOps) _packageOps;
        return packageOps.getOps();
    }

    public static int OpEntry_getMode(Object _opEntry) {
        AppOpsManager.OpEntry opEntry = (AppOpsManager.OpEntry) _opEntry;
        return opEntry.getMode();
    }

    public static int ActivityManager_RunningAppProcessInfo_procStateToImportance(int procState) {
        return ActivityManager.RunningAppProcessInfo.procStateToImportance(procState);
    }

    public static String PackageInfo_overlayTarget(PackageInfo packageInfo) {
        return packageInfo.overlayTarget;
    }

    public static Object IPackageManager_Stub_asInterface(IBinder binder) {
        return IPackageManager.Stub.asInterface(binder);
    }

    @RequiresApi(30)
    public static Object IPermissionManager_Stub_asInterface(IBinder binder) {
        return IPermissionManager.Stub.asInterface(binder);
    }

    public static List<PackageInfo> IPackageManager_getInstalledPackages(Object pm, int flags, int userId) throws RemoteException {
        //noinspection unchecked
        ParceledListSlice<PackageInfo> listSlice = ((IPackageManager) pm).getInstalledPackages(flags, userId);
        if (listSlice != null) {
            return listSlice.getList();
        }
        return new ArrayList<>();
    }

    public static int IPackageManager_checkPermission(Object pm, String permName, String pkgName, int userId) throws RemoteException {
        return ((IPackageManager) pm).checkPermission(permName, pkgName, userId);
    }

    public static void IPackageManager_grantRuntimePermission(Object pm, String packageName, String permissionName, int userId) throws RemoteException {
        ((IPackageManager) pm).grantRuntimePermission(packageName, permissionName, userId);
    }

    public static void IPackageManager_revokeRuntimePermission(Object pm, String packageName, String permissionName, int userId) throws RemoteException {
        ((IPackageManager) pm).revokeRuntimePermission(packageName, permissionName, userId);
    }

    @RequiresApi(30)
    public static int IPermissionManager_checkPermission(Object permissionmgr, String permName, String pkgName, int userId) throws RemoteException {
        return ((IPermissionManager) permissionmgr).checkPermission(permName, pkgName, userId);
    }

    @RequiresApi(30)
    public static void IPermissionManager_grantRuntimePermission(Object permissionmgr, String packageName, String permissionName, int userId) throws RemoteException {
        ((IPermissionManager) permissionmgr).grantRuntimePermission(packageName, permissionName, userId);
    }

    @RequiresApi(30)
    public static void IPermissionManager_revokeRuntimePermission(Object permissionmgr, String packageName, String permissionName, int userId, String reason) throws RemoteException {
        try {
            ((IPermissionManager) permissionmgr).revokeRuntimePermission(packageName, permissionName, userId, reason);
        } catch (NoSuchMethodError e) {
            ((IPermissionManager) permissionmgr).revokeRuntimePermission(packageName, permissionName, userId);
        }
    }

    @RequiresApi(29)
    public static byte[] Conscrypt_exportKeyingMaterial(SSLSocket socket, String label, byte[] context, int length) throws SSLException {
        return Conscrypt.exportKeyingMaterial(socket, label, context, length);
    }

    public static int SystemProperties_getInt(String key, int defaultValue) {
        return SystemProperties.getInt(key, defaultValue);
    }

    public static Context Context_createPackageContextAsUser(Context context, String packageName, int flags, UserHandle user)
            throws PackageManager.NameNotFoundException {
        return context.createPackageContextAsUser(packageName, flags, user);
    }
}
