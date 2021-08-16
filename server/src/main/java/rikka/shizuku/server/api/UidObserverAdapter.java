package rikka.shizuku.server.api;

import android.app.IUidObserver;
import android.os.Parcel;
import android.os.RemoteException;

import androidx.annotation.NonNull;

public class UidObserverAdapter extends IUidObserver.Stub {

    /**
     * Report that there are no longer any processes running for a uid.
     */
    @Override
    public void onUidGone(int uid, boolean disabled) throws RemoteException {

    }

    /**
     * Report that a uid is now active (no longer idle).
     */
    @Override
    public void onUidActive(int uid) throws RemoteException {

    }

    /**
     * Report that a uid is idle -- it has either been running in the background for
     * a sufficient period of time, or all of its processes have gone away.
     */
    @Override
    public void onUidIdle(int uid, boolean disabled) throws RemoteException {

    }

    /**
     * General report of a state change of an uid.
     *
     * @param uid          The uid for which the state change is being reported.
     * @param procState    The updated process state for the uid.
     * @param procStateSeq The sequence no. associated with process state change of the uid,
     *                     see UidRecord.procStateSeq for details.
     */
    @Override
    public void onUidStateChanged(int uid, int procState, long procStateSeq) throws RemoteException {

    }

    /**
     * Report when the cached state of a uid has changed.
     * If true, a uid has become cached -- that is, it has some active processes that are
     * all in the cached state.  It should be doing as little as possible at this point.
     * If false, that a uid is no longer cached.  This will only be called after
     * onUidCached() has been reported true.  It will happen when either one of its actively
     * running processes is no longer cached, or it no longer has any actively running processes.
     */
    @Override
    public void onUidCachedChanged(int uid, boolean cached) throws RemoteException {

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
