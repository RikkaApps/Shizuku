package hidden;

import android.annotation.NonNull;
import android.content.pm.IOnAppsChangedListener;
import android.content.pm.ParceledListSlice;
import android.os.Bundle;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.UserHandle;

public class OnAppsChangedListenerAdapter extends IOnAppsChangedListener.Stub {

    @Override
    public void onPackageRemoved(UserHandle user, String packageName) throws RemoteException {

    }

    @Override
    public void onPackageAdded(UserHandle user, String packageName) throws RemoteException {

    }

    @Override
    public void onPackageChanged(UserHandle user, String packageName) throws RemoteException {

    }

    @Override
    public void onPackagesAvailable(UserHandle user, String[] packageNames, boolean replacing) throws RemoteException {

    }

    @Override
    public void onPackagesUnavailable(UserHandle user, String[] packageNames, boolean replacing) throws RemoteException {

    }

    @Override
    public void onPackagesSuspended(UserHandle user, String[] packageNames, Bundle launcherExtras) throws RemoteException {

    }

    @Override
    public void onPackagesUnsuspended(UserHandle user, String[] packageNames) throws RemoteException {

    }

    @Override
    public void onShortcutChanged(UserHandle user, String packageName, ParceledListSlice shortcuts) throws RemoteException {

    }

    @Override
    public boolean onTransact(int code, @NonNull Parcel data, Parcel reply, int flags) throws RemoteException {
        try {
            return super.onTransact(code, data, reply, flags);
        } catch (Throwable tr) {
            return true;
        }
    }
}
