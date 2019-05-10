package moe.shizuku.server.api;

import android.app.IProcessObserver;
import android.os.Parcel;
import android.os.RemoteException;

import static moe.shizuku.server.utils.Logger.LOGGER;

public class ProcessObserver extends IProcessObserver.Stub {

    @Override
    public void onForegroundActivitiesChanged(int pid, int uid, boolean foregroundActivities) throws RemoteException {
    }

    @Override
    public void onProcessDied(int pid, int uid) throws RemoteException {
    }

    public void onProcessStateChanged(int pid, int uid, int procState) throws RemoteException {
        // no longer exists from API 26
    }

    public void onForegroundServicesChanged(int pid, int uid, int serviceTypes) throws RemoteException {
        // from Q beta 3
    }

    @Override
    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        try {
            return super.onTransact(code, data, reply, flags);
        } catch (Throwable tr) {
            LOGGER.e(tr, "ProcessObserver onTransact");
            return false;
        }
    }
}
