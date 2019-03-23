package moe.shizuku.api;

import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;

import moe.shizuku.server.IShizukuService;

public class ShizukuService {

    private static IShizukuService sService;

    public static IBinder getBinder() {
        return sService != null ? sService.asBinder() : null;
    }

    public static void setBinder(IBinder binder) {
        sService = IShizukuService.Stub.asInterface(binder);
    }

    public static boolean pingBinder() {
        if (sService == null)
            return false;

        return sService.asBinder().pingBinder();
    }

    public static void transactRemote(Parcel data, Parcel reply, int flags) throws RemoteException {
        sService.asBinder().transact(ShizukuApiConstants.BINDER_TRANSACTION_transact, data, reply, flags);
    }

    public static RemoteProcess newProcess(String[] cmd, String[] env, String dir) throws RemoteException {
        return new RemoteProcess(sService.newProcess(cmd, env, dir));
    }

    public static int getUid() throws RemoteException {
        return sService.getUid();
    }

    public static int getVersion() throws RemoteException {
        return sService.getVersion();
    }

    public static int checkPermission(String permission) throws RemoteException {
        return sService.checkPermission(permission);
    }
}
