package moe.shizuku.server;

import android.content.ComponentName;
import android.content.Context;
import android.content.IContentProvider;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.ddm.DdmHandleAppName;
import android.os.Binder;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Parcel;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.SELinux;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.system.Os;
import android.util.ArrayMap;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import dalvik.system.PathClassLoader;
import kotlin.collections.ArraysKt;
import moe.shizuku.api.BinderContainer;
import moe.shizuku.server.api.RemoteProcessHolder;
import moe.shizuku.server.api.SystemService;
import moe.shizuku.server.config.Config;
import moe.shizuku.server.config.ConfigManager;
import moe.shizuku.server.ktx.IContentProviderKt;
import moe.shizuku.server.utils.OsUtils;
import moe.shizuku.server.utils.UserHandleCompat;
import moe.shizuku.starter.ServiceStarter;
import rikka.parcelablelist.ParcelableListSlice;
import rikka.shizuku.ShizukuApiConstants;

import static moe.shizuku.server.ServerConstants.MANAGER_APPLICATION_ID;
import static moe.shizuku.server.ServerConstants.PERMISSION;
import static moe.shizuku.server.utils.Logger.LOGGER;
import static rikka.shizuku.ShizukuApiConstants.ATTACH_REPLY_PERMISSION_GRANTED;
import static rikka.shizuku.ShizukuApiConstants.ATTACH_REPLY_SERVER_PATCH_VERSION;
import static rikka.shizuku.ShizukuApiConstants.ATTACH_REPLY_SERVER_SECONTEXT;
import static rikka.shizuku.ShizukuApiConstants.ATTACH_REPLY_SERVER_UID;
import static rikka.shizuku.ShizukuApiConstants.ATTACH_REPLY_SERVER_VERSION;
import static rikka.shizuku.ShizukuApiConstants.ATTACH_REPLY_SHOULD_SHOW_REQUEST_PERMISSION_RATIONALE;
import static rikka.shizuku.ShizukuApiConstants.REQUEST_PERMISSION_REPLY_ALLOWED;
import static rikka.shizuku.ShizukuApiConstants.REQUEST_PERMISSION_REPLY_IS_ONETIME;
import static rikka.shizuku.ShizukuApiConstants.USER_SERVICE_ARG_COMPONENT;
import static rikka.shizuku.ShizukuApiConstants.USER_SERVICE_ARG_DEBUGGABLE;
import static rikka.shizuku.ShizukuApiConstants.USER_SERVICE_ARG_PROCESS_NAME;
import static rikka.shizuku.ShizukuApiConstants.USER_SERVICE_ARG_TAG;
import static rikka.shizuku.ShizukuApiConstants.USER_SERVICE_ARG_VERSION_CODE;
import static rikka.shizuku.ShizukuApiConstants.USER_SERVICE_TRANSACTION_destroy;

public class ShizukuService extends IShizukuService.Stub {

    public static void main(String[] args) {
        DdmHandleAppName.setAppName("shizuku_server", 0);

        Looper.prepare();
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

    private static ApplicationInfo getManagerApplicationInfo() {
        return SystemService.getApplicationInfoNoThrow(MANAGER_APPLICATION_ID, 0, 0);
    }

    @SuppressWarnings({"FieldCanBeLocal"})
    private final Handler mainHandler = new Handler(Looper.myLooper());
    //private final Context systemContext = HiddenApiBridge.getSystemContext();
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Map<String, UserServiceRecord> userServiceRecords = Collections.synchronizedMap(new ArrayMap<>());
    private final ClientManager clientManager;
    private final ConfigManager configManager;
    private final int managerAppId;

    public ShizukuService() {
        LOGGER.i("starting server...");

        waitSystemService("package");
        waitSystemService("activity");
        waitSystemService(Context.USER_SERVICE);
        waitSystemService(Context.APP_OPS_SERVICE);

        ApplicationInfo ai = getManagerApplicationInfo();
        if (ai == null) {
            System.exit(ServerConstants.MANAGER_APP_NOT_FOUND);
        }

        managerAppId = ai.uid;

        configManager = ConfigManager.getInstance();
        clientManager = ClientManager.getInstance();

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

    private int checkCallingPermission(String permission) {
        try {
            return SystemService.checkPermission(permission,
                    Binder.getCallingPid(),
                    Binder.getCallingUid());
        } catch (Throwable tr) {
            LOGGER.w(tr, "checkCallingPermission");
            return PackageManager.PERMISSION_DENIED;
        }
    }

    private void enforceManager(String func) {
        int callingUid = Binder.getCallingUid();
        int callingPid = Binder.getCallingPid();

        if (callingPid == Os.getpid() || UserHandleCompat.getAppId(callingUid) == managerAppId) {
            return;
        }

        String msg = "Permission Denial: " + func + " from pid="
                + Binder.getCallingPid()
                + " is not manager ";
        LOGGER.w(msg);
        throw new SecurityException(msg);
    }

    private void enforceCallingPermission(String func) {
        int callingUid = Binder.getCallingUid();
        int callingPid = Binder.getCallingPid();

        if (callingUid == OsUtils.getUid() || UserHandleCompat.getAppId(callingUid) == managerAppId) {
            return;
        }

        ClientRecord clientRecord = clientManager.findClient(callingUid, callingPid);
        if (clientRecord != null && clientRecord.allowed) {
            return;
        }

        if (clientRecord == null && checkCallingPermission(PERMISSION) == PackageManager.PERMISSION_GRANTED)
            return;

        String msg = "Permission Denial: " + func + " from pid="
                + Binder.getCallingPid()
                + " requires " + PERMISSION;
        LOGGER.w(msg);
        throw new SecurityException(msg);
    }

    private ClientRecord requireClient(int callingUid, int callingPid) {
        ClientRecord clientRecord = clientManager.findClient(callingUid, callingPid);
        if (clientRecord == null) {
            LOGGER.w("Caller (uid %d, pid %d) is not an attached client", callingUid, callingPid);
            throw new IllegalStateException("Not an attached client");
        }
        return clientRecord;
    }

    private void transactRemote(Parcel data, Parcel reply, int flags) throws RemoteException {
        enforceCallingPermission("transactRemote");

        IBinder targetBinder = data.readStrongBinder();
        int targetCode = data.readInt();

        LOGGER.d("transact: uid=%d, descriptor=%s, code=%d", Binder.getCallingUid(), targetBinder.getInterfaceDescriptor(), targetCode);
        Parcel newData = Parcel.obtain();
        try {
            newData.appendFrom(data, data.dataPosition(), data.dataAvail());
        } catch (Throwable tr) {
            LOGGER.w(tr, "appendFrom");
            return;
        }
        try {
            long id = Binder.clearCallingIdentity();
            targetBinder.transact(targetCode, newData, reply, flags);
            Binder.restoreCallingIdentity(id);
        } finally {
            newData.recycle();
        }
    }

    @Override
    public void exit() {
        enforceManager("exit");
        LOGGER.i("exit");
        System.exit(0);
    }

    @Override
    public int getVersion() {
        enforceCallingPermission("getVersion");
        return ShizukuApiConstants.SERVER_VERSION;
    }

    @Override
    public int getUid() {
        enforceCallingPermission("getUid");
        return Os.getuid();
    }

    @Override
    public int checkPermission(String permission) throws RemoteException {
        enforceCallingPermission("checkPermission");
        return SystemService.checkPermission(permission, Os.getuid());
    }

    @Override
    public IRemoteProcess newProcess(String[] cmd, String[] env, String dir) throws RemoteException {
        enforceCallingPermission("newProcess");

        LOGGER.d("newProcess: uid=%d, cmd=%s, env=%s, dir=%s", Binder.getCallingUid(), Arrays.toString(cmd), Arrays.toString(env), dir);

        java.lang.Process process;
        try {
            process = Runtime.getRuntime().exec(cmd, env, dir != null ? new File(dir) : null);
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage());
        }

        ClientRecord clientRecord = clientManager.findClient(Binder.getCallingUid(), Binder.getCallingPid());
        IBinder token = clientRecord != null ? clientRecord.client.asBinder() : null;

        return new RemoteProcessHolder(process, token);
    }

    @Override
    public String getSELinuxContext() throws RemoteException {
        enforceCallingPermission("getSELinuxContext");

        try {
            return SELinux.getContext();
        } catch (Throwable tr) {
            throw new IllegalStateException(tr.getMessage());
        }
    }

    @Override
    public String getSystemProperty(String name, String defaultValue) throws RemoteException {
        enforceCallingPermission("getSystemProperty");

        try {
            return SystemProperties.get(name, defaultValue);
        } catch (Throwable tr) {
            throw new IllegalStateException(tr.getMessage());
        }
    }

    @Override
    public void setSystemProperty(String name, String value) throws RemoteException {
        enforceCallingPermission("setSystemProperty");

        try {
            SystemProperties.set(name, value);
        } catch (Throwable tr) {
            throw new IllegalStateException(tr.getMessage());
        }
    }

    private class UserServiceRecord {

        private final DeathRecipient deathRecipient;
        public final boolean standalone;
        public final int versionCode;
        public String token;
        public IBinder service;
        public final ApkChangedObserver apkChangedObserver;
        public final RemoteCallbackList<IShizukuServiceConnection> callbacks = new RemoteCallbackList<>();

        public UserServiceRecord(boolean standalone, int versionCode, String apkPath) {
            this.standalone = standalone;
            this.versionCode = versionCode;
            this.token = UUID.randomUUID().toString() + "-" + System.currentTimeMillis();
            this.apkChangedObserver = ApkChangedObservers.start(apkPath, () -> {
                LOGGER.v("remove record %s because apk changed", token);
                removeSelf();
            });
            this.deathRecipient = () -> {
                LOGGER.v("binder in service record %s is dead", token);
                removeSelf();
            };
        }

        public void setBinder(IBinder binder) {
            LOGGER.v("binder received for service record %s", token);

            service = binder;

            if (standalone) {
                try {
                    binder.linkToDeath(deathRecipient, 0);
                } catch (Throwable tr) {
                    LOGGER.w(tr, "linkToDeath " + token);
                }
            }

            broadcastBinderReceived();
        }

        public void broadcastBinderReceived() {
            LOGGER.v("broadcast received for service record %s", token);

            int count = callbacks.beginBroadcast();
            for (int i = 0; i < count; i++) {
                try {
                    callbacks.getBroadcastItem(i).connected(service);
                } catch (Throwable e) {
                    LOGGER.w("failed to call connected");
                }
            }
            callbacks.finishBroadcast();
        }

        public void broadcastBinderDead() {
            LOGGER.v("broadcast dead for service record %s", token);

            int count = callbacks.beginBroadcast();
            for (int i = 0; i < count; i++) {
                try {
                    callbacks.getBroadcastItem(i).dead();
                } catch (Throwable e) {
                    LOGGER.w("failed to call connected");
                }
            }
            callbacks.finishBroadcast();
        }

        private void removeSelf() {
            synchronized (ShizukuService.this) {
                removeUserServiceLocked(UserServiceRecord.this);
            }
        }

        public void destroy() {
            if (standalone) {
                service.unlinkToDeath(deathRecipient, 0);
            } else {
                broadcastBinderDead();
            }

            ApkChangedObservers.stop(apkChangedObserver);

            if (service != null && service.pingBinder()) {
                Parcel data = Parcel.obtain();
                Parcel reply = Parcel.obtain();
                try {
                    data.writeInterfaceToken(service.getInterfaceDescriptor());
                    service.transact(USER_SERVICE_TRANSACTION_destroy, data, reply, Binder.FLAG_ONEWAY);
                } catch (Throwable e) {
                    LOGGER.w(e, "failed to destroy");
                } finally {
                    data.recycle();
                    reply.recycle();
                }
            }

            callbacks.kill();
        }
    }

    private PackageInfo ensureCallingPackageForUserService(String packageName, int appId, int userId) {
        PackageInfo packageInfo = SystemService.getPackageInfoNoThrow(packageName, 0x00002000 /*PackageManager.MATCH_UNINSTALLED_PACKAGES*/, userId);
        if (packageInfo == null || packageInfo.applicationInfo == null) {
            throw new SecurityException("unable to find package " + packageName);
        }
        if (UserHandleCompat.getAppId(packageInfo.applicationInfo.uid) != appId) {
            throw new SecurityException("package " + packageName + " is not owned by " + appId);
        }
        return packageInfo;
    }

    @Override
    public int removeUserService(IShizukuServiceConnection conn, Bundle options) {
        enforceCallingPermission("removeUserService");

        ComponentName componentName = Objects.requireNonNull(options.getParcelable(USER_SERVICE_ARG_COMPONENT), "component is null");

        int uid = Binder.getCallingUid();
        int appId = UserHandleCompat.getAppId(uid);
        int userId = UserHandleCompat.getUserId(uid);

        String packageName = componentName.getPackageName();
        ensureCallingPackageForUserService(packageName, appId, userId);

        String className = Objects.requireNonNull(componentName.getClassName(), "class is null");
        String tag = options.getString(USER_SERVICE_ARG_TAG);
        String key = packageName + ":" + (tag != null ? tag : className);

        synchronized (this) {
            UserServiceRecord record = getUserServiceRecordLocked(key);
            if (record == null) return 1;
            removeUserServiceLocked(record);
        }
        return 0;
    }

    private void removeUserServiceLocked(UserServiceRecord record) {
        if (userServiceRecords.values().remove(record)) {
            record.destroy();
        }
    }

    @Override
    public int addUserService(IShizukuServiceConnection conn, Bundle options) {
        enforceCallingPermission("addUserService");

        Objects.requireNonNull(conn, "connection is null");
        Objects.requireNonNull(options, "options is null");

        int uid = Binder.getCallingUid();
        int appId = UserHandleCompat.getAppId(uid);
        int userId = UserHandleCompat.getUserId(uid);

        ComponentName componentName = Objects.requireNonNull(options.getParcelable(USER_SERVICE_ARG_COMPONENT), "component is null");
        String packageName = Objects.requireNonNull(componentName.getPackageName(), "package is null");
        PackageInfo packageInfo = ensureCallingPackageForUserService(packageName, appId, userId);

        String className = Objects.requireNonNull(componentName.getClassName(), "class is null");
        String sourceDir = Objects.requireNonNull(packageInfo.applicationInfo.sourceDir, "apk path is null");
        String nativeLibraryDir = packageInfo.applicationInfo.nativeLibraryDir;

        int versionCode = options.getInt(USER_SERVICE_ARG_VERSION_CODE, 1);
        String tag = options.getString(USER_SERVICE_ARG_TAG);
        String processNameSuffix = options.getString(USER_SERVICE_ARG_PROCESS_NAME);
        boolean debug = options.getBoolean(USER_SERVICE_ARG_DEBUGGABLE, false);
        boolean standalone = processNameSuffix != null;
        String key = packageName + ":" + (tag != null ? tag : className);

        synchronized (this) {
            UserServiceRecord record = getOrCreateUserServiceRecordLocked(key, versionCode, standalone, sourceDir);
            record.callbacks.register(conn);

            if (record.service != null && record.service.pingBinder()) {
                record.broadcastBinderReceived();
            } else {
                Runnable runnable;
                if (standalone) {
                    runnable = () -> startUserServiceNewProcess(key, record.token, packageName, className, processNameSuffix, uid, debug);
                } else {
                    runnable = () -> {
                        /*CancellationSignal cancellationSignal = new CancellationSignal();
                        cancellationSignal.setOnCancelListener(() -> {
                            synchronized (ShizukuService.this) {
                                UserServiceRecord r = getUserServiceRecordLocked(key);
                                if (r != null) {
                                    removeUserServiceLocked(r);
                                    LOGGER.v("remove %s by user", key);
                                }
                            }
                        });

                        startUserServiceLocalProcess(key, record.token, packageName, className, sourceDir, cancellationSignal);*/
                    };
                }
                executor.execute(runnable);
            }
            return 0;
        }
    }

    private UserServiceRecord getUserServiceRecordLocked(String key) {
        return userServiceRecords.get(key);
    }

    private UserServiceRecord getOrCreateUserServiceRecordLocked(String key, int versionCode, boolean standalone, String apkPath) {
        UserServiceRecord record = getUserServiceRecordLocked(key);
        if (record != null) {
            if (record.versionCode != versionCode) {
                LOGGER.v("remove service record %s (%s) because version code not matched (old=%d, new=%d)", key, record.token, record.versionCode, versionCode);
            } else if (record.standalone != standalone) {
                LOGGER.v("remove service record %s (%s) because standalone not matched (old=%s, new=%s)", key, record.token, Boolean.toString(record.standalone), Boolean.toString(standalone));
            } else if (record.service == null || !record.service.pingBinder()) {
                LOGGER.v("service in record %s (%s) has dead", key, record.token);
            } else {
                LOGGER.i("found existing service record %s (%s)", key, record.token);
                return record;
            }

            removeUserServiceLocked(record);
        }

        record = new UserServiceRecord(standalone, versionCode, apkPath);
        userServiceRecords.put(key, record);
        LOGGER.i("new service record %s (%s): version=%d, standalone=%s, apk=%s", key, record.token, versionCode, Boolean.toString(standalone), apkPath);
        return record;
    }

    private void startUserServiceLocalProcess(String key, String token, String packageName, String className, String sourceDir, CancellationSignal cancellationSignal) {
        UserServiceRecord record = userServiceRecords.get(key);
        if (record == null || !Objects.equals(token, record.token)) {
            LOGGER.w("unable to find service record %s (%s)", key, token);
            return;
        }

        IBinder service;
        try {
            ClassLoader classLoader = new PathClassLoader(sourceDir, null, ClassLoader.getSystemClassLoader());
            Class<?> serviceClass = Objects.requireNonNull(classLoader.loadClass(className));
            Constructor<?> constructor;

            try {
                constructor = serviceClass.getConstructor(CancellationSignal.class);
                service = (IBinder) constructor.newInstance(cancellationSignal);
            } catch (Throwable e) {
                LOGGER.w("constructor with CancellationSignal not found");
                constructor = serviceClass.getConstructor();
                service = (IBinder) constructor.newInstance();
            }
        } catch (Throwable tr) {
            LOGGER.w(tr, "unable to create service %s/%s", packageName, className);
            return;
        }

        record.setBinder(service);
    }

    private void startUserServiceNewProcess(String key, String token, String packageName, String classname, String processNameSuffix, int callingUid, boolean debug) {
        LOGGER.v("starting process for service record %s (%s)...", key, token);

        String cmd = ServiceStarter.commandForUserService(getManagerApplicationInfo().sourceDir, token, packageName, classname, processNameSuffix, callingUid, debug);
        int exitCode;
        try {
            java.lang.Process process = Runtime.getRuntime().exec("sh");
            OutputStream os = process.getOutputStream();
            os.write(cmd.getBytes());
            os.flush();
            os.close();

            exitCode = process.waitFor();
        } catch (Throwable e) {
            throw new IllegalStateException(e.getMessage());
        }
        if (exitCode != 0) {
            throw new IllegalStateException("sh exited with " + exitCode);
        }
    }

    @Override
    public void attachUserService(IBinder binder, Bundle options) {
        enforceManager("attachUserService");

        Objects.requireNonNull(binder, "binder is null");
        String token = Objects.requireNonNull(options.getString(ShizukuApiConstants.USER_SERVICE_ARG_TOKEN), "token is null");

        synchronized (this) {
            sendUserServiceLocked(binder, token);
        }
    }

    @Override
    public void attachApplication(IShizukuApplication application, String requestPackageName) {
        if (application == null || requestPackageName == null) {
            return;
        }

        int callingPid = Binder.getCallingPid();
        int callingUid = Binder.getCallingUid();
        boolean isManager;
        ClientRecord clientRecord = null;

        List<String> packages = SystemService.getPackagesForUidNoThrow(callingUid);
        if (!packages.contains(requestPackageName)) {
            LOGGER.w("Request package " + requestPackageName + "does not belong to uid " + callingUid);
            throw new SecurityException("Request package " + requestPackageName + "does not belong to uid " + callingUid);
        }

        isManager = MANAGER_APPLICATION_ID.equals(requestPackageName);

        if (!isManager && clientManager.findClient(callingUid, callingPid) == null) {
            synchronized (this) {
                clientRecord = clientManager.addClient(callingUid, callingPid, application, requestPackageName);
            }
            if (clientRecord == null) {
                LOGGER.w("Add client failed");
                return;
            }
        }

        LOGGER.d("attachApplication: %s %d %d", requestPackageName, callingUid, callingPid);

        Bundle reply = new Bundle();
        reply.putInt(ATTACH_REPLY_SERVER_UID, OsUtils.getUid());
        reply.putInt(ATTACH_REPLY_SERVER_VERSION, ShizukuApiConstants.SERVER_VERSION);
        reply.putString(ATTACH_REPLY_SERVER_SECONTEXT, OsUtils.getSELinuxContext());
        if (!isManager) {
            reply.putBoolean(ATTACH_REPLY_PERMISSION_GRANTED, clientRecord.allowed);
            reply.putBoolean(ATTACH_REPLY_SHOULD_SHOW_REQUEST_PERMISSION_RATIONALE, false);
        } else {
            reply.putInt(ATTACH_REPLY_SERVER_PATCH_VERSION, ServerConstants.PATCH_VERSION);
        }
        try {
            application.bindApplication(reply);
        } catch (Throwable e) {
            LOGGER.w(e, "attachApplication");
        }
    }

    @Override
    public void requestPermission(int requestCode) {
        int callingUid = Binder.getCallingUid();
        int callingPid = Binder.getCallingPid();
        int userId = UserHandleCompat.getUserId(callingUid);

        if (callingUid == OsUtils.getUid() || callingPid == OsUtils.getPid()) {
            return;
        }

        ClientRecord clientRecord = requireClient(callingUid, callingPid);

        if (clientRecord.allowed) {
            clientRecord.dispatchRequestPermissionResult(requestCode, true);
            return;
        }

        Config.PackageEntry entry = configManager.find(callingUid);
        if (entry != null && entry.isDenied()) {
            clientRecord.dispatchRequestPermissionResult(requestCode, false);
            return;
        }

        ApplicationInfo ai = SystemService.getApplicationInfoNoThrow(clientRecord.packageName, 0, userId);
        if (ai == null) {
            return;
        }

        Intent intent = new Intent(ServerConstants.REQUEST_PERMISSION_ACTION)
                .setPackage(MANAGER_APPLICATION_ID)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
                .putExtra("uid", callingUid)
                .putExtra("pid", callingPid)
                .putExtra("requestCode", requestCode)
                .putExtra("applicationInfo", ai);
        SystemService.startActivityNoThrow(intent, null, 0);
    }

    @Override
    public boolean checkSelfPermission() {
        int callingUid = Binder.getCallingUid();
        int callingPid = Binder.getCallingPid();

        if (callingUid == OsUtils.getUid() || callingPid == OsUtils.getPid()) {
            return true;
        }

        return requireClient(callingUid, callingPid).allowed;
    }

    @Override
    public boolean shouldShowRequestPermissionRationale() {
        int callingUid = Binder.getCallingUid();
        int callingPid = Binder.getCallingPid();

        if (callingUid == OsUtils.getUid() || callingPid == OsUtils.getPid()) {
            return true;
        }

        requireClient(callingUid, callingPid);

        Config.PackageEntry entry = configManager.find(callingUid);
        return entry != null && entry.isDenied();
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
        if (records.isEmpty()) {
            LOGGER.w("dispatchPermissionConfirmationResult: no client for uid %d was found", requestUid);
        } else {
            for (ClientRecord record : records) {
                record.allowed = allowed;
                if (record.pid == requestPid) {
                    record.dispatchRequestPermissionResult(requestCode, allowed);
                }
            }
        }

        if (!onetime) {
            configManager.update(requestUid, Config.MASK_PERMISSION, allowed ? Config.FLAG_ALLOWED : Config.FLAG_DENIED);
        }

        if (!onetime && allowed) {
            int userId = UserHandleCompat.getUserId(requestUid);

            for (String packageName : SystemService.getPackagesForUidNoThrow(requestUid)) {
                PackageInfo pi = SystemService.getPackageInfoNoThrow(packageName, PackageManager.GET_PERMISSIONS, userId);
                if (pi == null || pi.requestedPermissions == null || !ArraysKt.contains(pi.requestedPermissions, PERMISSION)) {
                    continue;
                }

                if (allowed) {
                    SystemService.grantRuntimePermission(packageName, PERMISSION, userId);
                } else {
                    SystemService.revokeRuntimePermission(packageName, PERMISSION, userId);
                }
            }
        }
    }

    private int getFlagsForUidInternal(int uid, int mask, boolean allowRuntimePermission) {
        Config.PackageEntry entry = configManager.find(uid);
        if (entry != null) {
            return entry.flags & mask;
        }

        if (allowRuntimePermission && (mask & Config.MASK_PERMISSION) != 0) {
            int userId = UserHandleCompat.getUserId(uid);
            for (String packageName : SystemService.getPackagesForUidNoThrow(uid)) {
                PackageInfo pi = SystemService.getPackageInfoNoThrow(packageName, PackageManager.GET_PERMISSIONS, userId);
                if (pi == null || pi.requestedPermissions == null || !ArraysKt.contains(pi.requestedPermissions, PERMISSION)) {
                    continue;
                }

                try {
                    if (SystemService.checkPermission(PERMISSION, uid) == PackageManager.PERMISSION_GRANTED) {
                        return Config.FLAG_ALLOWED;
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

        if ((mask & Config.MASK_PERMISSION) != 0) {
            boolean allowed = (value & Config.FLAG_ALLOWED) != 0;
            boolean denied = (value & Config.FLAG_DENIED) != 0;

            List<ClientRecord> records = clientManager.findClients(uid);
            for (ClientRecord record : records) {
                if (allowed) {
                    record.allowed = true;
                } else {
                    record.allowed = false;
                    SystemService.forceStopPackageNoThrow(record.packageName, UserHandleCompat.getUserId(record.uid));
                }
            }

            for (String packageName : SystemService.getPackagesForUidNoThrow(uid)) {
                PackageInfo pi = SystemService.getPackageInfoNoThrow(packageName, PackageManager.GET_PERMISSIONS, userId);
                if (pi == null || pi.requestedPermissions == null || !ArraysKt.contains(pi.requestedPermissions, PERMISSION)) {
                    continue;
                }

                if (allowed) {
                    SystemService.grantRuntimePermission(packageName, PERMISSION, userId);
                } else {
                    SystemService.revokeRuntimePermission(packageName, PERMISSION, userId);
                }
            }
        }

        configManager.update(uid, mask, value);
    }

    private void sendUserServiceLocked(IBinder binder, String token) {
        Map.Entry<String, UserServiceRecord> entry = null;
        for (Map.Entry<String, UserServiceRecord> e : userServiceRecords.entrySet()) {
            if (e.getValue().token.equals(token)) {
                entry = e;
                break;
            }
        }

        if (entry == null) {
            throw new IllegalArgumentException("unable to find token " + token);
        }

        LOGGER.v("received binder for service record %s", token);

        UserServiceRecord record = entry.getValue();
        record.setBinder(binder);
    }

    private ParcelableListSlice<PackageInfo> getApplications(int userId) {
        List<PackageInfo> list = new ArrayList<>();
        List<Integer> users = new ArrayList<>();
        if (userId == -1) {
            users = SystemService.getUserIdsNoThrow();
        } else {
            users.add(userId);
        }

        for (int user : users) {
            for (PackageInfo pi : SystemService.getInstalledPackagesNoThrow(PackageManager.GET_META_DATA | PackageManager.GET_PERMISSIONS, user)) {
                if (Objects.equals(MANAGER_APPLICATION_ID, pi.packageName)) continue;
                if (pi.applicationInfo == null) continue;

                int uid = pi.applicationInfo.uid;
                int flags = getFlagsForUidInternal(uid, Config.MASK_PERMISSION, false);
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
        if (code == ShizukuApiConstants.BINDER_TRANSACTION_transact) {
            data.enforceInterface(ShizukuApiConstants.BINDER_DESCRIPTOR);
            transactRemote(data, reply, flags);
            return true;
        } else if (code == ServerConstants.BINDER_TRANSACTION_getApplications) {
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
        for (int userId : SystemService.getUserIdsNoThrow()) {
            sendBinderToClient(this, userId);
        }
    }

    private static void sendBinderToClient(Binder binder, int userId) {
        try {
            for (PackageInfo pi : SystemService.getInstalledPackagesNoThrow(PackageManager.GET_PERMISSIONS, userId)) {
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
        for (int userId : SystemService.getUserIdsNoThrow()) {
            sendBinderToManger(binder, userId);
        }
    }

    static void sendBinderToManger(Binder binder, int userId) {
        sendBinderToUserApp(binder, MANAGER_APPLICATION_ID, userId);
    }

    static void sendBinderToUserApp(Binder binder, String packageName, int userId) {
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
            provider = SystemService.getContentProviderExternal(name, userId, token, name);
            if (provider == null) {
                LOGGER.e("provider is null %s %d", name, userId);
                return;
            }

            Bundle extra = new Bundle();
            extra.putParcelable("moe.shizuku.privileged.api.intent.extra.BINDER", new BinderContainer(binder));

            Bundle reply = IContentProviderKt.callCompat(provider, null, null, name, "sendBinder", null, extra);
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
                    SystemService.removeContentProviderExternal(name, token);
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
