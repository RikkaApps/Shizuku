package android.app;

import android.os.Binder;
import android.os.RemoteException;

public interface IUidObserver {

    void onUidGone(int uid, boolean disabled) throws RemoteException;

    void onUidActive(int uid) throws RemoteException;

    void onUidIdle(int uid, boolean disabled) throws RemoteException;

    void onUidStateChanged(int uid, int procState, long procStateSeq) throws RemoteException;

    /**
     * Report when the cached state of a uid has changed.
     * If true, a uid has become cached -- that is, it has some active processes that are
     * all in the cached state.  It should be doing as little as possible at this point.
     * If false, that a uid is no longer cached.  This will only be called after
     * onUidCached() has been reported true.  It will happen when either one of its actively
     * running processes is no longer cached, or it no longer has any actively running processes.
     */
    void onUidCachedChanged(int uid, boolean cached) throws RemoteException;

    abstract class Stub extends Binder implements IUidObserver {

    }
}
