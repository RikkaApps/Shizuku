package android.content.pm;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;

public interface IPackageManager extends IInterface {

    ParceledListSlice<PackageInfo> getInstalledPackages(int flags, int userId) throws RemoteException;

    int checkPermission(String permName, String pkgName, int userId) throws RemoteException;

    void grantRuntimePermission(String packageName, String permissionName, int userId) throws RemoteException;

    void revokeRuntimePermission(String packageName, String permissionName, int userId) throws RemoteException;

    abstract class Stub extends Binder implements IPackageManager {

        public static IPackageManager asInterface(IBinder obj) {
            throw new UnsupportedOperationException();
        }
    }
}
