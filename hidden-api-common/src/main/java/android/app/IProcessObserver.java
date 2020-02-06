package android.app;

import android.os.Binder;
import android.os.RemoteException;

public interface IProcessObserver {

    void onForegroundActivitiesChanged(int pid, int uid, boolean foregroundActivities) throws RemoteException;

    void onProcessDied(int pid, int uid) throws RemoteException;

    // no longer exists from API 26
    void onProcessStateChanged(int pid, int uid, int procState) throws RemoteException;

    // from Q beta 3
    void onForegroundServicesChanged(int pid, int uid, int serviceTypes) throws RemoteException;

    abstract class Stub extends Binder implements IProcessObserver {

    }
}
