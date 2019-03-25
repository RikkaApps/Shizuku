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

    /**
     * Call {@link IBinder#transact(int, Parcel, Parcel, int)} at remote service.
     *
     * <p>How to construct the data parcel:
     * <code><br>data.writeInterfaceToken(ShizukuApiConstants.BINDER_DESCRIPTOR);
     * <br>data.writeStrongBinder(\/* binder you want to use at remote *\/);
     * <br>data.writeInt(\/* transact code you want to use *\/);
     * <br>data.writeInterfaceToken(\/* interface name of that binder *\/);
     * <br>\/* write data of the binder call you want*\/</code>
     *
     * @see SystemServiceHelper#obtainParcel(String, String, String)
     * @see SystemServiceHelper#obtainParcel(String, String, String, String)
     */
    public static void transactRemote(Parcel data, Parcel reply, int flags) throws RemoteException {
        sService.asBinder().transact(ShizukuApiConstants.BINDER_TRANSACTION_transact, data, reply, flags);
    }

    /**
     * Start a new process at remote service, parameters are passed to {@link java.lang.Runtime#exec(String, String[], java.io.File)}.
     *
     * @return RemoteProcess holds the binder of remote process
     */
    public static RemoteProcess newProcess(String[] cmd, String[] env, String dir) throws RemoteException {
        return new RemoteProcess(sService.newProcess(cmd, env, dir));
    }

    /**
     * Returns uid of remote service.
     * @return uid
     */
    public static int getUid() throws RemoteException {
        return sService.getUid();
    }

    /**
     * Returns remote service version.
     * @return server version
     */
    public static int getVersion() throws RemoteException {
        return sService.getVersion();
    }

    /**
     * Check permission at remote service.
     *
     * @param permission permission name
     * @return PackageManager.PERMISSION_DENIED or PackageManager.PERMISSION_GRANTED
     */
    public static int checkPermission(String permission) throws RemoteException {
        return sService.checkPermission(permission);
    }

    /**
     * Set token of current process. Do not call this on API 23+.
     *
     * @param token token
     * @return is token correct
     *
     * @throws IllegalStateException call on API 23+
     */
    public static boolean setCurrentProcessTokenPre23(String token) throws RemoteException {
        return sService.setPidToken(token);
    }
}
