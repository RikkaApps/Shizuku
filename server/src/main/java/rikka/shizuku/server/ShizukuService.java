package rikka.shizuku.server;

import static rikka.shizuku.ShizukuApiConstants.ATTACH_APPLICATION_API_VERSION;
import static rikka.shizuku.ShizukuApiConstants.ATTACH_APPLICATION_PACKAGE_NAME;
import static rikka.shizuku.ShizukuApiConstants.BIND_APPLICATION_PERMISSION_GRANTED;
import static rikka.shizuku.ShizukuApiConstants.BIND_APPLICATION_SERVER_PATCH_VERSION;
import static rikka.shizuku.ShizukuApiConstants.BIND_APPLICATION_SERVER_SECONTEXT;
import static rikka.shizuku.ShizukuApiConstants.BIND_APPLICATION_SERVER_UID;
import static rikka.shizuku.ShizukuApiConstants.BIND_APPLICATION_SERVER_VERSION;
import static rikka.shizuku.ShizukuApiConstants.BIND_APPLICATION_SHOULD_SHOW_REQUEST_PERMISSION_RATIONALE;
import static rikka.shizuku.ShizukuApiConstants.REQUEST_PERMISSION_REPLY_ALLOWED;
import static rikka.shizuku.ShizukuApiConstants.REQUEST_PERMISSION_REPLY_IS_ONETIME;
import static rikka.shizuku.server.ServerConstants.MANAGER_APPLICATION_ID;
import static rikka.shizuku.server.ServerConstants.PERMISSION;

import android.content.Context;
import android.content.IContentProvider;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.UserInfo;
import android.ddm.DdmHandleAppName;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import kotlin.collections.ArraysKt;
import moe.shizuku.api.BinderContainer;
import moe.shizuku.common.util.BuildUtils;
import moe.shizuku.common.util.OsUtils;
import moe.shizuku.server.IShizukuApplication;
import rikka.hidden.compat.ActivityManagerApis;
import rikka.hidden.compat.DeviceIdleControllerApis;
import rikka.hidden.compat.PackageManagerApis;
import rikka.hidden.compat.PermissionManagerApis;
import rikka.hidden.compat.UserManagerApis;
import rikka.parcelablelist.ParcelableListSlice;
import rikka.rish.RishConfig;
import rikka.shizuku.ShizukuApiConstants;
import rikka.shizuku.server.api.IContentProviderUtils;
import rikka.shizuku.server.util.HandlerUtil;
import rikka.shizuku.server.util.UserHandleCompat;

public class ShizukuService extends Service<ShizukuUserServiceManager, ShizukuClientManager, ShizukuConfigManager> {

    public static void main(String[] args) {
        DdmHandleAppName.setAppName("shizuku_server", 0);
        RishConfig.setLibraryPath(System.getProperty("shizuku.library.path"));

        Looper.prepareMainLooper();
        new ShizukuService();
        Looper.loop();
    }

    private static void waitSystemService(String name) {
        while (ServiceManager.getService(name) == null) {
            try {
                LOGGER.i("service " + name + " is not started, wait 1s.");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                LOGGER.w(e.getMessage(), e);
            }
        }
    }

    public static ApplicationInfo getManagerApplicationInfo() {
        return PackageManagerApis.getApplicationInfoNoThrow(MANAGER_APPLICATION_ID, 0, 0);
    }

    @SuppressWarnings({"FieldCanBeLocal"})
    private final Handler mainHandler = new Handler(Looper.myLooper());
    //private final Context systemContext = HiddenApiBridge.getSystemContext();
    private final ShizukuClientManager clientManager;
    private final ShizukuConfigManager configManager;
    private final int managerAppId;

    public ShizukuService() {
        super();

        HandlerUtil.setMainHandler(mainHandler);

        LOGGER.i("starting server...");

        waitSystemService("package");
        waitSystemService(Context.ACTIVITY_SERVICE);
        waitSystemService(Context.USER_SERVICE);
        waitSystemService(Context.APP_OPS_SERVICE);

        ApplicationInfo ai = getManagerApplicationInfo();
        if (ai == null) {
            System.exit(ServerConstants.MANAGER_APP_NOT_FOUND);
        }

        assert ai != null;
        managerAppId = ai.uid;

        configManager = getConfigManager();
        clientManager = getClientManager();

        ApkChangedObservers.start(ai.sourceDir, () -> {
            if (getManagerApplicationInfo() == null) {
                LOGGER.w("manager app is uninstalled in user 0, exiting...");
                System.exit(ServerConstants.MANAGER_APP_NOT_FOUND);
            }
        });

        BinderSender.register(this);

        mainHandler.post(() -> {
            sendBinderToClient();
            sendBinderToManager();
        });
    }

    @Override
    public ShizukuUserServiceManager onCreateUserServiceManager() {
        return new ShizukuUserServiceManager();
    }

    @Override
    public ShizukuClientManager onCreateClientManager() {
        return new ShizukuClientManager(getConfigManager());
    }

    @Override
    public ShizukuConfigManager onCreateConfigManager() {
        return new ShizukuConfigManager();
    }

    @Override
    public boolean checkCallerManagerPermission(String func, int callingUid, int callingPid) {
        return UserHandleCompat.getAppId(callingUid) == managerAppId;
    }

    private int checkCallingPermission() {
        try {
            return ActivityManagerApis.checkPermission(ServerConstants.PERMISSION,
                    Binder.getCallingPid(),
                    Binder.getCallingUid());
        } catch (Throwable tr) {
            LOGGER.w(tr, "checkCallingPermission");
            return PackageManager.PERMISSION_DENIED;
        }
    }

    @Override
    public boolean checkCallerPermission(String func, int callingUid, int callingPid, @Nullable ClientRecord clientRecord) {
        if (UserHandleCompat.getAppId(callingUid) == managerAppId) {
            return true;
        }
        if (clientRecord == null && checkCallingPermission() == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

    @Override
    public void exit() {
        enforceManagerPermission("exit");
        LOGGER.i("exit");
        System.exit(0);
    }

    @Override
    public void attachUserService(IBinder binder, Bundle options) {
        enforceManagerPermission("func");

        super.attachUserService(binder, options);
    }

    @Override
    public void attachApplication(IShizukuApplication application, Bundle args) {
        if (application == null || args == null) {
            return;
        }

        String requestPackageName = args.getString(ATTACH_APPLICATION_PACKAGE_NAME);
        if (requestPackageName == null) {
            return;
        }
        int apiVersion = args.getInt(ATTACH_APPLICATION_API_VERSION, -1);

        int callingPid = Binder.getCallingPid();
        int callingUid = Binder.getCallingUid();
        boolean isManager;
        ClientRecord clientRecord = null;

        List<String> packages = PackageManagerApis.getPackagesForUidNoThrow(callingUid);
        if (!packages.contains(requestPackageName)) {
            LOGGER.w("Request package " + requestPackageName + "does not belong to uid " + callingUid);
            throw new SecurityException("Request package " + requestPackageName + "does not belong to uid " + callingUid);
        }

        isManager = MANAGER_APPLICATION_ID.equals(requestPackageName);

        if (clientManager.findClient(callingUid, callingPid) == null) {
            synchronized (this) {
                clientRecord = clientManager.addClient(callingUid, callingPid, application, requestPackageName, apiVersion);
            }
            if (clientRecord == null) {
                LOGGER.w("Add client failed");
                return;
            }
        }

        LOGGER.d("attachApplication: %s %d %d", requestPackageName, callingUid, callingPid);

        int replyServerVersion = ShizukuApiConstants.SERVER_VERSION;
        if (apiVersion == -1) {
            // ShizukuBinderWrapper has adapted API v13 in dev.rikka.shizuku:api 12.2.0, however
            // attachApplication in 12.2.0 is still old, so that server treat the client as pre 13.
            // This finally cause transactRemote fails.
            // So we can pass 12 here to pretend we are v12 server.
            replyServerVersion = 12;
        }

        Bundle reply = new Bundle();
        reply.putInt(BIND_APPLICATION_SERVER_UID, OsUtils.getUid());
        reply.putInt(BIND_APPLICATION_SERVER_VERSION, replyServerVersion);
        reply.putString(BIND_APPLICATION_SERVER_SECONTEXT, OsUtils.getSELinuxContext());
        reply.putInt(BIND_APPLICATION_SERVER_PATCH_VERSION, ShizukuApiConstants.SERVER_PATCH_VERSION);
        if (!isManager) {
            reply.putBoolean(BIND_APPLICATION_PERMISSION_GRANTED, Objects.requireNonNull(clientRecord).allowed);
            reply.putBoolean(BIND_APPLICATION_SHOULD_SHOW_REQUEST_PERMISSION_RATIONALE, false);
        }
        try {
            application.bindApplication(reply);
        } catch (Throwable e) {
            LOGGER.w(e, "attachApplication");
        }
    }

    @Override
    public void showPermissionConfirmation(int requestCode, @NonNull ClientRecord clientRecord, int callingUid, int callingPid, int userId) {
        ApplicationInfo ai = PackageManagerApis.getApplicationInfoNoThrow(clientRecord.packageName, 0, userId);
        if (ai == null) {
            return;
        }

        PackageInfo pi = PackageManagerApis.getPackageInfoNoThrow(MANAGER_APPLICATION_ID, 0, userId);
        UserInfo userInfo = UserManagerApis.getUserInfo(userId);
        boolean isWorkProfileUser = BuildUtils.atLeast30() ?
                "android.os.usertype.profile.MANAGED".equals(userInfo.userType) :
                (userInfo.flags & UserInfo.FLAG_MANAGED_PROFILE) != 0;
        if (pi == null && !isWorkProfileUser) {
            LOGGER.w("Manager not found in non work profile user %d. Revoke permission", userId);
            clientRecord.dispatchRequestPermissionResult(requestCode, false);
            return;
        }

        Intent intent = new Intent(ServerConstants.REQUEST_PERMISSION_ACTION)
                .setPackage(MANAGER_APPLICATION_ID)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
                .putExtra("uid", callingUid)
                .putExtra("pid", callingPid)
                .putExtra("requestCode", requestCode)
                .putExtra("applicationInfo", ai);
        ActivityManagerApis.startActivityNoThrow(intent, null, isWorkProfileUser ? 0 : userId);
    }

    @Override
    public void dispatchPermissionConfirmationResult(int requestUid, int requestPid, int requestCode, Bundle data) throws RemoteException {
        if (UserHandleCompat.getAppId(Binder.getCallingUid()) != managerAppId) {
            LOGGER.w("dispatchPermissionConfirmationResult called not from the manager package");
            return;
        }

        if (data == null) {
            return;
        }

        boolean allowed = data.getBoolean(REQUEST_PERMISSION_REPLY_ALLOWED);
        boolean onetime = data.getBoolean(REQUEST_PERMISSION_REPLY_IS_ONETIME);

        LOGGER.i("dispatchPermissionConfirmationResult: uid=%d, pid=%d, requestCode=%d, allowed=%s, onetime=%s",
                requestUid, requestPid, requestCode, Boolean.toString(allowed), Boolean.toString(onetime));

        List<ClientRecord> records = clientManager.findClients(requestUid);
        List<String> packages = new ArrayList<>();
        if (records.isEmpty()) {
            LOGGER.w("dispatchPermissionConfirmationResult: no client for uid %d was found", requestUid);
        } else {
            for (ClientRecord record : records) {
                packages.add(record.packageName);
                record.allowed = allowed;
                if (record.pid == requestPid) {
                    record.dispatchRequestPermissionResult(requestCode, allowed);
                }
            }
        }

        if (!onetime) {
            configManager.update(requestUid, packages, ConfigManager.MASK_PERMISSION, allowed ? ConfigManager.FLAG_ALLOWED : ConfigManager.FLAG_DENIED);
        }

        if (!onetime && allowed) {
            int userId = UserHandleCompat.getUserId(requestUid);

            for (String packageName : PackageManagerApis.getPackagesForUidNoThrow(requestUid)) {
                PackageInfo pi = PackageManagerApis.getPackageInfoNoThrow(packageName, PackageManager.GET_PERMISSIONS, userId);
                if (pi == null || pi.requestedPermissions == null || !ArraysKt.contains(pi.requestedPermissions, PERMISSION)) {
                    continue;
                }

                int deviceId = 0;//Context.DEVICE_ID_DEFAULT
                if (allowed) {
                    PermissionManagerApis.grantRuntimePermission(packageName, PERMISSION, userId);
                } else {
                    PermissionManagerApis.revokeRuntimePermission(packageName, PERMISSION, userId);
                }
            }
        }
    }

    private int  getFlagsForUidInternal(int uid, int mask, boolean allowRuntimePermission) {
        ShizukuConfig.PackageEntry entry = configManager.find(uid);
        if (entry != null) {
            return entry.flags & mask;
        }

        if (allowRuntimePermission && (mask & ConfigManager.MASK_PERMISSION) != 0) {
            int userId = UserHandleCompat.getUserId(uid);
            for (String packageName : PackageManagerApis.getPackagesForUidNoThrow(uid)) {
                PackageInfo pi = PackageManagerApis.getPackageInfoNoThrow(packageName, PackageManager.GET_PERMISSIONS, userId);
                if (pi == null || pi.requestedPermissions == null || !ArraysKt.contains(pi.requestedPermissions, PERMISSION)) {
                    continue;
                }

                try {
                    if (PermissionManagerApis.checkPermission(PERMISSION, uid) == PackageManager.PERMISSION_GRANTED) {
                        return ConfigManager.FLAG_ALLOWED;
                    }
                } catch (Throwable e) {
                    LOGGER.w("getFlagsForUid");
                }
            }
        }
        return 0;
    }

    @Override
    public int getFlagsForUid(int uid, int mask) {
        if (UserHandleCompat.getAppId(Binder.getCallingUid()) != managerAppId) {
            LOGGER.w("updateFlagsForUid is allowed to be called only from the manager");
            return 0;
        }
        return getFlagsForUidInternal(uid, mask, true);
    }

    @Override
    public void updateFlagsForUid(int uid, int mask, int value) throws RemoteException {
        if (UserHandleCompat.getAppId(Binder.getCallingUid()) != managerAppId) {
            LOGGER.w("updateFlagsForUid is allowed to be called only from the manager");
            return;
        }

        int userId = UserHandleCompat.getUserId(uid);

        if ((mask & ConfigManager.MASK_PERMISSION) != 0) {
            boolean allowed = (value & ConfigManager.FLAG_ALLOWED) != 0;
            boolean denied = (value & ConfigManager.FLAG_DENIED) != 0;

            List<ClientRecord> records = clientManager.findClients(uid);
            for (ClientRecord record : records) {
                if (allowed) {
                    record.allowed = true;
                } else {
                    record.allowed = false;
                    ActivityManagerApis.forceStopPackageNoThrow(record.packageName, UserHandleCompat.getUserId(record.uid));
                    onPermissionRevoked(record.packageName);
                }
            }

            for (String packageName : PackageManagerApis.getPackagesForUidNoThrow(uid)) {
                PackageInfo pi = PackageManagerApis.getPackageInfoNoThrow(packageName, PackageManager.GET_PERMISSIONS, userId);
                if (pi == null || pi.requestedPermissions == null || !ArraysKt.contains(pi.requestedPermissions, PERMISSION)) {
                    continue;
                }

                int deviceId = 0;//Context.DEVICE_ID_DEFAULT
                if (allowed) {
                    PermissionManagerApis.grantRuntimePermission(packageName, PERMISSION, userId);
                } else {
                    PermissionManagerApis.revokeRuntimePermission(packageName, PERMISSION, userId);
                }

                // TODO kill user service using
            }
        }

        configManager.update(uid, null, mask, value);
    }

    private void onPermissionRevoked(String packageName) {
        // TODO add runtime permission listener
        getUserServiceManager().removeUserServicesForPackage(packageName);
    }

    private ParcelableListSlice<PackageInfo> getApplications(int userId) {
        List<PackageInfo> list = new ArrayList<>();
        List<Integer> users = new ArrayList<>();
        if (userId == -1) {
            users.addAll(UserManagerApis.getUserIdsNoThrow());
        } else {
            users.add(userId);
        }

        for (int user : users) {
            for (PackageInfo pi : PackageManagerApis.getInstalledPackagesNoThrow(PackageManager.GET_META_DATA | PackageManager.GET_PERMISSIONS, user)) {
                if (Objects.equals(MANAGER_APPLICATION_ID, pi.packageName)) continue;
                if (pi.applicationInfo == null) continue;

                int uid = pi.applicationInfo.uid;
                int flags = 0;
                ShizukuConfig.PackageEntry entry = configManager.find(uid);
                if (entry != null) {
                    if (entry.packages != null && !entry.packages.contains(pi.packageName))
                        continue;
                    flags = entry.flags & ConfigManager.MASK_PERMISSION;
                }

                if (flags != 0) {
                    list.add(pi);
                } else if (pi.applicationInfo.metaData != null
                        && pi.applicationInfo.metaData.getBoolean("moe.shizuku.client.V3_SUPPORT", false)
                        && pi.requestedPermissions != null
                        && ArraysKt.contains(pi.requestedPermissions, PERMISSION)) {
                    list.add(pi);
                }
            }

        }
        return new ParcelableListSlice<>(list);
    }

    @Override
    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        //LOGGER.d("transact: code=%d, calling uid=%d", code, Binder.getCallingUid());
        if (code == ServerConstants.BINDER_TRANSACTION_getApplications) {
            data.enforceInterface(ShizukuApiConstants.BINDER_DESCRIPTOR);
            int userId = data.readInt();
            ParcelableListSlice<PackageInfo> result = getApplications(userId);
            reply.writeNoException();
            result.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
            return true;
        }
        return super.onTransact(code, data, reply, flags);
    }

    void sendBinderToClient() {
        for (int userId : UserManagerApis.getUserIdsNoThrow()) {
            sendBinderToClient(this, userId);
        }
    }

    private static void sendBinderToClient(Binder binder, int userId) {
        try {
            for (PackageInfo pi : PackageManagerApis.getInstalledPackagesNoThrow(PackageManager.GET_PERMISSIONS, userId)) {
                if (pi == null || pi.requestedPermissions == null)
                    continue;

                if (ArraysKt.contains(pi.requestedPermissions, PERMISSION)) {
                    sendBinderToUserApp(binder, pi.packageName, userId);
                }
            }
        } catch (Throwable tr) {
            LOGGER.e("exception when call getInstalledPackages", tr);
        }
    }

    void sendBinderToManager() {
        sendBinderToManger(this);
    }

    private static void sendBinderToManger(Binder binder) {
        for (int userId : UserManagerApis.getUserIdsNoThrow()) {
            sendBinderToManger(binder, userId);
        }
    }

    static void sendBinderToManger(Binder binder, int userId) {
        sendBinderToUserApp(binder, MANAGER_APPLICATION_ID, userId);
    }

    static void sendBinderToUserApp(Binder binder, String packageName, int userId) {
        sendBinderToUserApp(binder, packageName, userId, true);
    }

    static void sendBinderToUserApp(Binder binder, String packageName, int userId, boolean retry) {
        try {
            DeviceIdleControllerApis.addPowerSaveTempWhitelistApp(packageName, 30 * 1000, userId,
                    316/* PowerExemptionManager#REASON_SHELL */, "shell");
            LOGGER.v("Add %d:%s to power save temp whitelist for 30s", userId, packageName);
        } catch (Throwable tr) {
            LOGGER.e(tr, "Failed to add %d:%s to power save temp whitelist", userId, packageName);
        }

        String name = packageName + ".shizuku";
        IContentProvider provider = null;

        /*
         When we pass IBinder through binder (and really crossed process), the receive side (here is system_server process)
         will always get a new instance of android.os.BinderProxy.

         In the implementation of getContentProviderExternal and removeContentProviderExternal, received
         IBinder is used as the key of a HashMap. But hashCode() is not implemented by BinderProxy, so
         removeContentProviderExternal will never work.

         Luckily, we can pass null. When token is token, count will be used.
         */
        IBinder token = null;

        try {
            provider = ActivityManagerApis.getContentProviderExternal(name, userId, token, name);
            if (provider == null) {
                LOGGER.e("provider is null %s %d", name, userId);
                return;
            }
            if (!provider.asBinder().pingBinder()) {
                LOGGER.e("provider is dead %s %d", name, userId);

                if (retry) {
                    // For unknown reason, sometimes this could happens
                    // Kill Shizuku app and try again could work
                    ActivityManagerApis.forceStopPackageNoThrow(packageName, userId);
                    LOGGER.e("kill %s in user %d and try again", packageName, userId);
                    Thread.sleep(1000);
                    sendBinderToUserApp(binder, packageName, userId, false);
                }
                return;
            }

            if (!retry) {
                LOGGER.e("retry works");
            }

            Bundle extra = new Bundle();
            extra.putParcelable("moe.shizuku.privileged.api.intent.extra.BINDER", new BinderContainer(binder));

            Bundle reply = IContentProviderUtils.callCompat(provider, null, name, "sendBinder", null, extra);
            if (reply != null) {
                LOGGER.i("send binder to user app %s in user %d", packageName, userId);
            } else {
                LOGGER.w("failed to send binder to user app %s in user %d", packageName, userId);
            }
        } catch (Throwable tr) {
            LOGGER.e(tr, "failed send binder to user app %s in user %d", packageName, userId);
        } finally {
            if (provider != null) {
                try {
                    ActivityManagerApis.removeContentProviderExternal(name, token);
                } catch (Throwable tr) {
                    LOGGER.w(tr, "removeContentProviderExternal");
                }
            }
        }
    }

    // ------ Sui only ------

    @Override
    public void dispatchPackageChanged(Intent intent) throws RemoteException {

    }

    @Override
    public boolean isHidden(int uid) throws RemoteException {
        return false;
    }
}
