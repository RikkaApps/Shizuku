package android.content.pm;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;
import android.os.UserHandle;

public interface IOnAppsChangedListener extends IInterface {

    void onPackageRemoved(UserHandle user, String packageName)
            throws RemoteException;

    void onPackageAdded(UserHandle user, String packageName)
            throws RemoteException;

    void onPackageChanged(UserHandle user, String packageName)
            throws RemoteException;

    void onPackagesAvailable(UserHandle user, String[] packageNames, boolean replacing)
            throws RemoteException;

    void onPackagesUnavailable(UserHandle user, String[] packageNames, boolean replacing)
            throws RemoteException;

    void onPackagesSuspended(UserHandle user, String[] packageNames,
                             Bundle launcherExtras)
            throws RemoteException;

    void onPackagesUnsuspended(UserHandle user, String[] packageNames)
            throws RemoteException;

    void onShortcutChanged(UserHandle user, String packageName, ParceledListSlice shortcuts)
            throws RemoteException;

    abstract class Stub extends Binder implements IOnAppsChangedListener {

        public static IOnAppsChangedListener asInterface(IBinder obj) {
            throw new RuntimeException("STUB");
        }

        @Override
        public IBinder asBinder() {
            throw new RuntimeException("STUB");
        }
    }
}
