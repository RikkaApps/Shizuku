package moe.shizuku.api;

import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import moe.shizuku.server.IShizukuService;

public class ShizukuService {

    static IShizukuService sService;

    public static void setBinder(@Nullable IBinder binder) {
        sService = IShizukuService.Stub.asInterface(binder);
    }

    @NonNull
    private static IShizukuService requireService() {
        if (getService() == null) {
            throw new IllegalStateException("Binder haven't been received, check Shizuku and your code.");
        }
        return getService();
    }

    @Nullable
    private static IShizukuService getService() {
        return sService;
    }

    @Nullable
    public static IBinder getBinder() {
        IShizukuService service = getService();
        return service != null ? service.asBinder() : null;
    }

    public static boolean pingBinder() {
        if (getBinder() == null)
            return false;

        IBinder binder = getBinder();
        return binder != null && binder.pingBinder();
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
    public static void transactRemote(@NonNull Parcel data, @Nullable Parcel reply, int flags) throws RemoteException {
        requireService().asBinder().transact(ShizukuApiConstants.BINDER_TRANSACTION_transact, data, reply, flags);
    }

    /**
     * Start a new process at remote service, parameters are passed to {@link java.lang.Runtime#exec(String, String[], java.io.File)}.
     * <p>
     * Note, you may need to read/write streams from RemoteProcess in different threads.
     * </p>
     *
     * @return RemoteProcess holds the binder of remote process
     * @deprecated This method is super easy to be abused, it may be removed in the future.
     * Currently the only known use is install packages, but use binder in much more easier (see sample).
     */
    @Deprecated
    public static RemoteProcess newProcess(@NonNull String[] cmd, @Nullable String[] env, @Nullable String dir) throws RemoteException {
        return new RemoteProcess(requireService().newProcess(cmd, env, dir));
    }

    /**
     * Returns uid of remote service.
     *
     * @return uid
     */
    public static int getUid() throws RemoteException {
        return requireService().getUid();
    }

    /**
     * Returns remote service version.
     *
     * @return server version
     */
    public static int getVersion() throws RemoteException {
        return requireService().getVersion();
    }

    /**
     * Return latest service version when this library was released.
     *
     * @see ShizukuService#getVersion()
     * @return Latest service version
     */
    public static int getLatestServiceVersion() {
        return ShizukuApiConstants.SERVER_VERSION;
    }

    /**
     * Check permission at remote service.
     *
     * @param permission permission name
     * @return PackageManager.PERMISSION_DENIED or PackageManager.PERMISSION_GRANTED
     */
    public static int checkPermission(String permission) throws RemoteException {
        return requireService().checkPermission(permission);
    }

    /**
     * Returns SELinux context of Shizuku server process.
     *
     * <p>This API is only meaningful for root app using {@link ShizukuService#newProcess(String[], String[], String)}.</p>
     *
     * <p>For adb, context should always be <code>u:r:shell:s0</code>.
     * <br>For root, context depends on su the user uses. E.g., context of Magisk is <code>u:r:magisk:s0</code>.
     * If the user's su does not allow binder calls between su and app, Shizuku will switch to context <code>u:r:shell:s0</code>.
     * </p>
     *
     * @return SELinux context
     * @since added from version 6
     */
    public static String getSELinuxContext() throws RemoteException {
        return requireService().getSELinuxContext();
    }
}
