package hidden;

import android.content.pm.IPackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.ParceledListSlice;
import android.content.pm.UserInfo;
import android.os.IBinder;
import android.os.IUserManager;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.permission.IPermissionManager;

import androidx.annotation.RequiresApi;

import com.android.org.conscrypt.Conscrypt;

import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSocket;

public class HiddenApiBridge {

    public static Object IUserManager_Stub_asInterface(IBinder binder) {
        return IUserManager.Stub.asInterface(binder);
    }

    public static List<UserInfoCompat> IUserManager_getUsers(Object um) throws RemoteException {
        List<UserInfo> list;
        try {
            list = ((IUserManager) um).getUsers(true, true, true);
        } catch (NoSuchMethodError e) {
            list = ((IUserManager) um).getUsers(true);
        }
        List<UserInfoCompat> users = new ArrayList<>();
        for (UserInfo ui : list) {
            users.add(new UserInfoCompat(ui.id, ui.name));
        }
        return users;
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
}
