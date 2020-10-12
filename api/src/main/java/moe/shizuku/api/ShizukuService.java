package moe.shizuku.api;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;

import java.util.Objects;

import moe.shizuku.server.IShizukuService;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP_PREFIX;

public class ShizukuService {

    private static IBinder binder;
    private static IShizukuService service;

    protected static void setBinder(@Nullable IBinder binder) {
        if (ShizukuService.binder == binder) return;

        if (binder == null) {
            ShizukuService.binder = null;
            ShizukuService.service = null;
        } else {
            ShizukuService.binder = binder;
            ShizukuService.service = IShizukuService.Stub.asInterface(binder);

            try {
                ShizukuService.binder.linkToDeath(ShizukuProvider.DEATH_RECIPIENT, 0);
            } catch (Throwable ignored) {
            }
        }
    }

    @NonNull
    protected static IShizukuService requireService() {
        if (service == null) {
            throw new IllegalStateException("binder haven't been received");
        }
        return service;
    }

    @Nullable
    public static IBinder getBinder() {
        return binder;
    }

    public static boolean pingBinder() {
        return binder != null && binder.pingBinder();
    }

    /**
     * Used by manager only
     */
    @RestrictTo(LIBRARY_GROUP_PREFIX)
    public static void exit() throws RemoteException {
        requireService().exit();
    }

    /**
     * Used by manager only
     */
    @RestrictTo(LIBRARY_GROUP_PREFIX)
    public static void sendUserService(@NonNull IBinder binder, @NonNull Bundle options) throws RemoteException {
        requireService().sendUserService(binder, options);
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
     * @deprecated If transactRemote is not enough for you, use UserService.
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
     * @return Latest service version
     * @see ShizukuService#getVersion()
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

    public static class UserServiceArgs {

        final ComponentName componentName;
        int versionCode = 1;
        boolean standalone = true;
        String processName;
        String tag;
        boolean debuggable = false;

        public UserServiceArgs(@NonNull ComponentName componentName) {
            this.componentName = componentName;
        }

        public UserServiceArgs tag(@NonNull String tag) {
            this.tag = tag;
            return this;
        }

        public UserServiceArgs version(int versionCode) {
            this.versionCode = versionCode;
            return this;
        }

        /*public UserServiceArgs useShizukuServerProcess() {
            this.standalone = false;
            this.debuggable = false;
            this.processName = null;
            return this;
        }*/

        public UserServiceArgs useStandaloneProcess(String processNameSuffix, boolean debuggable) {
            this.standalone = true;
            this.debuggable = debuggable;
            this.processName = processNameSuffix;
            return this;
        }

        private Bundle forAdd() {
            Bundle options = new Bundle();
            options.putParcelable(ShizukuApiConstants.USER_SERVICE_ARG_COMPONENT, componentName);
            options.putBoolean(ShizukuApiConstants.USER_SERVICE_ARG_DEBUGGABLE, debuggable);
            options.putInt(ShizukuApiConstants.USER_SERVICE_ARG_VERSION_CODE, versionCode);
            if (standalone) {
                options.putString(ShizukuApiConstants.USER_SERVICE_ARG_PROCESS_NAME,
                        Objects.requireNonNull(processName, "process name suffix must not be null when using standalone process mode"));
            }
            if (tag != null) {
                options.putString(ShizukuApiConstants.USER_SERVICE_ARG_TAG, tag);
            }
            return options;
        }

        private Bundle forRemove() {
            Bundle options = new Bundle();
            options.putParcelable(ShizukuApiConstants.USER_SERVICE_ARG_COMPONENT, componentName);
            if (tag != null) {
                options.putString(ShizukuApiConstants.USER_SERVICE_ARG_TAG, tag);
            }
            return options;
        }
    }

    /**
     * Run service class from the apk of current app.
     *
     * @since added from version 10
     */
    public static void bindUserService(@NonNull UserServiceArgs args, @NonNull ServiceConnection conn) throws RemoteException {
        ShizukuServiceConnection connection = ShizukuServiceConnection.getOrCreate(args);
        connection.addConnection(conn);
        requireService().addUserService(connection, args.forAdd());
    }

    /**
     * Remove user service.
     *
     * @param remove Remove (kill) the remote user service.
     */
    public static void unbindUserService(@NonNull UserServiceArgs args, @NonNull ServiceConnection conn, boolean remove) throws RemoteException {
        ShizukuServiceConnection connection = ShizukuServiceConnection.get(args);
        if (connection != null) {
            connection.removeConnection(conn);
        }
        if (remove) {
            requireService().removeUserService(null /* (unused) */, args.forRemove());
        }
    }
}
