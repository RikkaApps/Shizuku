package com.android.internal.app;

import android.os.IBinder;
import android.os.RemoteException;

import java.util.List;

/**
 * Created by Rikka on 2017/5/11.
 */

public interface IAppOpsService {

    List getPackagesForOps(int[] ops) throws RemoteException;

    List getOpsForPackage(int uid, String packageName, int[] ops) throws RemoteException;

    void setMode(int code, int uid, String packageName, int mode) throws RemoteException;

    void resetAllModes(int reqUserId, String reqPackageName) throws RemoteException;

    class Stub {

        public static IAppOpsService asInterface(IBinder binder) {
            throw new UnsupportedOperationException();
        }
    }
}