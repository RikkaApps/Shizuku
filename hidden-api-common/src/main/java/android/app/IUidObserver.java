package android.app;

import android.os.Binder;
import android.os.RemoteException;

public interface IUidObserver {

    void onUidGone(int uid, boolean disabled) throws RemoteException;

    void onUidActive(int uid) throws RemoteException;

    void onUidIdle(int uid, boolean disabled) throws RemoteException;

    void onUidStateChanged(int uid, int procState, long procStateSeq) throws RemoteException;

    void onUidCachedChanged(int uid, boolean cached) throws RemoteException;

    abstract class Stub extends Binder implements IUidObserver {

    }
}
