package android.app;

import android.os.RemoteException;

/**
 * Created by Rikka on 2017/5/5.
 */

public interface IActivityManager {

    void registerTaskStackListener(ITaskStackListener listener) throws RemoteException;
}
