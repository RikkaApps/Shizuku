package android.content.pm;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;

public interface IPackageManager extends IInterface {

    ParceledListSlice<PackageInfo> getInstalledPackages(int flags, int userId) throws RemoteException;

    abstract class Stub extends Binder implements IPackageManager {

        public static IPackageManager asInterface(IBinder obj) {
            throw new UnsupportedOperationException();
        }
    }
}
