package hidden;

import android.app.IProcessObserver;
import android.os.RemoteException;

public class ProcessObserverAdapter extends IProcessObserver.Stub {

    public void onForegroundActivitiesChanged(int pid, int uid, boolean foregroundActivities) throws RemoteException {
    }

    public void onProcessDied(int pid, int uid) throws RemoteException {
    }

    public void onProcessStateChanged(int pid, int uid, int procState) throws RemoteException {
        // no longer exists from API 26
    }

    public void onForegroundServicesChanged(int pid, int uid, int serviceTypes) throws RemoteException {
        // from Q beta 3
    }
}
