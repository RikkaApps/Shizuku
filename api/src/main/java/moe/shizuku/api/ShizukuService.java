package moe.shizuku.api;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;

import java.util.Objects;

import moe.shizuku.server.IShizukuApplication;
import moe.shizuku.server.IShizukuService;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP_PREFIX;
import static moe.shizuku.api.ShizukuApiConstants.ATTACH_REPLY_PERMISSION_GRANTED;
import static moe.shizuku.api.ShizukuApiConstants.ATTACH_REPLY_SERVER_SECONTEXT;
import static moe.shizuku.api.ShizukuApiConstants.ATTACH_REPLY_SERVER_UID;
import static moe.shizuku.api.ShizukuApiConstants.ATTACH_REPLY_SERVER_VERSION;
import static moe.shizuku.api.ShizukuApiConstants.ATTACH_REPLY_SHOULD_SHOW_REQUEST_PERMISSION_RATIONALE;
import static moe.shizuku.api.ShizukuApiConstants.REQUEST_PERMISSION_REPLY_ALLOWED;

public class ShizukuService {

    private static IBinder binder;
    private static IShizukuService service;

    private static int serverUid = -1;
    private static int serverVersion = -1;
    private static String serverContext = null;
    private static boolean permissionGranted = false;
    private static boolean shouldShowRequestPermissionRationale = false;

    private static final IShizukuApplication SHIZUKU_APPLICATION = new IShizukuApplication.Stub() {

        @Override
        public void bindApplication(Bundle data) {
            serverUid = data.getInt(ATTACH_REPLY_SERVER_UID, -1);
            serverVersion = data.getInt(ATTACH_REPLY_SERVER_VERSION, -1);
            serverContext = data.getString(ATTACH_REPLY_SERVER_SECONTEXT);
            permissionGranted = data.getBoolean(ATTACH_REPLY_PERMISSION_GRANTED, false);
            shouldShowRequestPermissionRationale = data.getBoolean(ATTACH_REPLY_SHOULD_SHOW_REQUEST_PERMISSION_RATIONALE, false);
        }

        @Override
        public void dispatchRequestPermissionResult(int requestCode, Bundle data) {
            boolean allowed = data.getBoolean(REQUEST_PERMISSION_REPLY_ALLOWED, false);

            ShizukuProvider.postRequestPermissionResultListener(requestCode, allowed ? PackageManager.PERMISSION_GRANTED : PackageManager.PERMISSION_DENIED);
        }
    };

    protected static void setBinder(@Nullable IBinder binder, String packageName) {
        if (ShizukuService.binder == binder) return;

        if (binder == null) {
            ShizukuService.binder = null;
            ShizukuService.service = null;
            serverUid = -1;
            serverVersion = -1;
            serverContext = null;
        } else {
            ShizukuService.binder = binder;
            ShizukuService.service = IShizukuService.Stub.asInterface(binder);

            try {
                ShizukuService.binder.linkToDeath(ShizukuProvider.DEATH_RECIPIENT, 0);
            } catch (Throwable ignored) {
            }
            try {
                service.attachApplication(SHIZUKU_APPLICATION, packageName);
                Log.i("ShizukuClient", "attachApplication");
            } catch (Throwable e) {
                Log.w("ShizukuClient", Log.getStackTraceString(e));
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

    private static RuntimeException rethrowAsRuntimeException(RemoteException e) {
        return new RuntimeException(e);
    }

    /**
     * Used by manager only
     */
    @RestrictTo(LIBRARY_GROUP_PREFIX)
    public static void exit() {
        try {
            requireService().exit();
        } catch (RemoteException e) {
            throw rethrowAsRuntimeException(e);
        }
    }

    /**
     * Used by manager only
     */
    @RestrictTo(LIBRARY_GROUP_PREFIX)
    public static void sendUserService(@NonNull IBinder binder, @NonNull Bundle options) {
        try {
            requireService().sendUserService(binder, options);
        } catch (RemoteException e) {
            throw rethrowAsRuntimeException(e);
        }
    }

    /**
     * Used by manager only
     *
     * @since added from version 11
     */
    @RestrictTo(LIBRARY_GROUP_PREFIX)
    public static void dispatchPermissionConfirmationResult(int requestUid, int requestPid, int requestCode, @NonNull Bundle data) {
        try {
            requireService().dispatchPermissionConfirmationResult(requestUid, requestPid, requestCode, data);
        } catch (RemoteException e) {
            throw rethrowAsRuntimeException(e);
        }
    }

    /**
     * Used by manager only
     *
     * @since added from version 11
     */
    @RestrictTo(LIBRARY_GROUP_PREFIX)
    public static int getFlagsForUid(int uid, int mask) {
        try {
            return requireService().getFlagsForUid(uid, mask);
        } catch (RemoteException e) {
            throw rethrowAsRuntimeException(e);
        }
    }

    /**
     * Used by manager only
     *
     * @since added from version 11
     */
    @RestrictTo(LIBRARY_GROUP_PREFIX)
    public static void updateFlagsForUid(int uid, int mask, int value) {
        try {
            requireService().updateFlagsForUid(uid, mask, value);
        } catch (RemoteException e) {
            throw rethrowAsRuntimeException(e);
        }
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
    public static void transactRemote(@NonNull Parcel data, @Nullable Parcel reply, int flags) {
        try {
            requireService().asBinder().transact(ShizukuApiConstants.BINDER_TRANSACTION_transact, data, reply, flags);
        } catch (RemoteException e) {
            throw rethrowAsRuntimeException(e);
        }
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
    public static RemoteProcess newProcess(@NonNull String[] cmd, @Nullable String[] env, @Nullable String dir) {
        try {
            return new RemoteProcess(requireService().newProcess(cmd, env, dir));
        } catch (RemoteException e) {
            throw rethrowAsRuntimeException(e);
        }
    }

    /**
     * Returns uid of remote service.
     *
     * @return uid
     */
    public static int getUid() {
        if (serverUid != -1) return serverUid;
        try {
            serverUid = requireService().getUid();
        } catch (RemoteException e) {
            throw rethrowAsRuntimeException(e);
        }
        return serverUid;
    }

    /**
     * Returns remote service version.
     *
     * @return server version
     */
    public static int getVersion() {
        if (serverVersion != -1) return serverVersion;
        try {
            serverVersion = requireService().getVersion();
        } catch (RemoteException e) {
            throw rethrowAsRuntimeException(e);
        }
        return serverVersion;
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
    public static int checkPermission(String permission) {
        if (serverUid == 0) return PackageManager.PERMISSION_GRANTED;
        try {
            return requireService().checkPermission(permission);
        } catch (RemoteException e) {
            throw rethrowAsRuntimeException(e);
        }
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
    public static String getSELinuxContext() {
        if (serverContext != null) return serverContext;
        try {
            serverContext = requireService().getSELinuxContext();
        } catch (RemoteException e) {
            throw rethrowAsRuntimeException(e);
        }
        return serverContext;
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
    public static void bindUserService(@NonNull UserServiceArgs args, @NonNull ServiceConnection conn) {
        ShizukuServiceConnection connection = ShizukuServiceConnection.getOrCreate(args);
        connection.addConnection(conn);
        try {
            requireService().addUserService(connection, args.forAdd());
        } catch (RemoteException e) {
            throw rethrowAsRuntimeException(e);
        }
    }

    /**
     * Remove user service.
     *
     * @param remove Remove (kill) the remote user service.
     */
    public static void unbindUserService(@NonNull UserServiceArgs args, @NonNull ServiceConnection conn, boolean remove) {
        ShizukuServiceConnection connection = ShizukuServiceConnection.get(args);
        if (connection != null) {
            connection.removeConnection(conn);
        }
        if (remove) {
            try {
                requireService().removeUserService(null /* (unused) */, args.forRemove());
            } catch (RemoteException e) {
                throw rethrowAsRuntimeException(e);
            }
        }
    }

    /**
     * Request permission.
     *
     * @since added from version 11, use runtime permission APIs for old versions
     */
    public static void requestPermission(int requestCode) {
        try {
            requireService().requestPermission(requestCode);
        } catch (RemoteException e) {
            throw rethrowAsRuntimeException(e);
        }
    }

    /**
     * Check if self has permission.
     *
     * @since added from version 11, use runtime permission APIs for old versions
     */
    public static int checkSelfPermission() {
        if (permissionGranted) return PackageManager.PERMISSION_GRANTED;
        try {
            permissionGranted = requireService().checkSelfPermission();
        } catch (RemoteException e) {
            throw rethrowAsRuntimeException(e);
        }
        return permissionGranted ? PackageManager.PERMISSION_GRANTED : PackageManager.PERMISSION_DENIED;
    }

    /**
     * Should show UI with rationale before requesting the permission.
     *
     * @since added from version 11, use runtime permission APIs for old versions
     */
    public static boolean shouldShowRequestPermissionRationale() {
        if (permissionGranted) return false;
        if (shouldShowRequestPermissionRationale) return true;
        try {
            shouldShowRequestPermissionRationale = requireService().shouldShowRequestPermissionRationale();
        } catch (RemoteException e) {
            throw rethrowAsRuntimeException(e);
        }
        return shouldShowRequestPermissionRationale;
    }
}
