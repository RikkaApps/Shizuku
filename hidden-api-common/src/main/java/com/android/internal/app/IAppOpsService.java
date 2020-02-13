package com.android.internal.app;

import android.app.AppOpsManager;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;

import androidx.annotation.RequiresApi;

import java.util.List;

public interface IAppOpsService extends IInterface {

    List<AppOpsManager.PackageOps> getOpsForPackage(int uid, String packageName, int[] ops)
            throws RemoteException;

    @RequiresApi(26)
    List<AppOpsManager.PackageOps> getUidOps(int uid, int[] ops)
            throws RemoteException;

    void setMode(int code, int uid, String packageName, int mode)
            throws RemoteException;

    void setUidMode(int code, int uid, int mode)
            throws RemoteException;

    abstract class Stub implements IAppOpsService {

        public static IAppOpsService asInterface(IBinder obj) {
            throw new RuntimeException("STUB");
        }
    }
}
