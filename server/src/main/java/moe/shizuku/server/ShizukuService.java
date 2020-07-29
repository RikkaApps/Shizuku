package moe.shizuku.server;

import android.content.IContentProvider;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.SELinux;
import android.os.SystemProperties;
import android.system.Os;
import android.util.ArrayMap;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import dalvik.system.DexClassLoader;
import moe.shizuku.api.BinderContainer;
import moe.shizuku.api.ShizukuApiConstants;
import moe.shizuku.server.api.RemoteProcessHolder;
import moe.shizuku.server.api.SystemService;
import moe.shizuku.server.ktx.IContentProviderKt;
import moe.shizuku.server.utils.ArrayUtils;
import moe.shizuku.server.utils.UserHandleCompat;

import static moe.shizuku.api.ShizukuApiConstants.USER_SERVICE_ARG_ALWAYS_RECREATE;
import static moe.shizuku.api.ShizukuApiConstants.USER_SERVICE_ARG_CLASSNAME;
import static moe.shizuku.api.ShizukuApiConstants.USER_SERVICE_ARG_DEBUGGABLE;
import static moe.shizuku.api.ShizukuApiConstants.USER_SERVICE_ARG_ID;
import static moe.shizuku.api.ShizukuApiConstants.USER_SERVICE_ARG_PACKAGE_NAME;
import static moe.shizuku.api.ShizukuApiConstants.USER_SERVICE_ARG_PROCESS_NAME;
import static moe.shizuku.api.ShizukuApiConstants.USER_SERVICE_ARG_VERSION_CODE;
import static moe.shizuku.api.ShizukuApiConstants.USER_SERVICE_TRANSACTION_destroy;
import static moe.shizuku.server.utils.Logger.LOGGER;

public class ShizukuService extends IShizukuService.Stub {

    private static final String PERMISSION_MANAGER = "moe.shizuku.manager.permission.MANAGER";
    private static final String PERMISSION = ShizukuApiConstants.PERMISSION;

    public static void main(String[] args) {
        LOGGER.i("starting server...");

        Looper.prepare();
        new ShizukuService();
        //DdmHandleAppName.setAppName("shizuku_server", 0);
        Looper.loop();

        LOGGER.i("server exited");
        System.exit(0);
    }

    private static final String USER_SERVICE_CMD_DEBUG;

    static {
        int sdk = Build.VERSION.SDK_INT;
        if (sdk >= 30) {
            USER_SERVICE_CMD_DEBUG = "-Xcompiler-option" + " --debuggable" +
                    " -XjdwpProvider:adbconnection" +
                    " -XjdwpOptions:suspend=n,server=y";
        } else if (sdk >= 28) {
            USER_SERVICE_CMD_DEBUG = "-Xcompiler-option" + "--debuggable" +
                    " -XjdwpProvider:internal" +
                    " -XjdwpOptions:transport=dt_android_adb,suspend=n,server=y";
        } else {
            USER_SERVICE_CMD_DEBUG = "-Xcompiler-option" + " --debuggable" +
                    " -agentlib:jdwp=transport=dt_android_adb,suspend=n,server=y";
        }
    }

    @SuppressWarnings({"FieldCanBeLocal"})
    private final Handler mainHandler = new Handler(Looper.myLooper());
    //private final Context systemContext = HiddenApiBridge.getSystemContext();
    private final Map<String, UserServiceRecord> userServiceRecords = Collections.synchronizedMap(new ArrayMap<>());

    ShizukuService() {
        super();

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
        if (Binder.getCallingPid() == Os.getpid()) {
            return;
        }

        if (checkCallingPermission(PERMISSION_MANAGER) == PackageManager.PERMISSION_GRANTED)
            return;

        String msg = "Permission Denial: " + func + " from pid="
                + Binder.getCallingPid()
                + " requires " + PERMISSION_MANAGER;
        LOGGER.w(msg);
        throw new SecurityException(msg);
    }

    private void enforceCallingPermission(String func) {
        if (Binder.getCallingPid() == Os.getpid()) {
            return;
        }

        if (checkCallingPermission(PERMISSION_MANAGER) == PackageManager.PERMISSION_GRANTED
                || checkCallingPermission(PERMISSION) == PackageManager.PERMISSION_GRANTED)
            return;

        String msg = "Permission Denial: " + func + " from pid="
                + Binder.getCallingPid()
                + " requires " + PERMISSION;
        LOGGER.w(msg);
        throw new SecurityException(msg);
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
            throw new RemoteException(e.getMessage());
        }

        return new RemoteProcessHolder(process);
    }

    @Override
    public String getSELinuxContext() throws RemoteException {
        enforceCallingPermission("getSELinuxContext");

        try {
            return SELinux.getContext();
        } catch (Throwable tr) {
            throw new RemoteException(tr.getMessage());
        }
    }

    @Override
    public String getSystemProperty(String name, String defaultValue) throws RemoteException {
        enforceCallingPermission("getSystemProperty");

        try {
            return SystemProperties.get(name, defaultValue);
        } catch (Throwable tr) {
            throw new RemoteException(tr.getMessage());
        }
    }

    @Override
    public void setSystemProperty(String name, String value) throws RemoteException {
        enforceCallingPermission("setSystemProperty");

        try {
            SystemProperties.set(name, value);
        } catch (Throwable tr) {
            throw new RemoteException(tr.getMessage());
        }
    }

    private class UserServiceRecord implements DeathRecipient, ApkChangedListener {

        public boolean standalone;
        public int versionCode;
        public String token;
        public CountDownLatch latch;
        public IBinder service;
        public ApkChangedObserver apkChangedObserver;

        public UserServiceRecord(IBinder service, int versionCode, String apkPath) {
            this.standalone = false;
            this.service = service;
            this.versionCode = versionCode;
            this.token = generateTokenForUserService();
            this.apkChangedObserver = ApkChangedObservers.start(apkPath, this);
        }

        public UserServiceRecord(int versionCode, String apkPath) {
            this.standalone = true;
            this.versionCode = versionCode;
            this.token = generateTokenForUserService();
            this.latch = new CountDownLatch(1);
            this.apkChangedObserver = ApkChangedObservers.start(apkPath, this);
        }

        public void onBinderReceived(IBinder binder) {
            this.service = binder;
            try {
                binder.linkToDeath(this, 0);
            } catch (Throwable tr) {
                LOGGER.w(tr, "linkToDeath " + token);
            }
            this.latch.countDown();
            this.apkChangedObserver.startWatching();
        }

        @Override
        public void binderDied() {
            LOGGER.i("%s is dead", token);
            removeUserServiceImpl(this);
        }

        @Override
        public void onApkChanged() {
            LOGGER.i("remove %s because apk changed", token);
            removeUserServiceImpl(this);
        }

        public void cleanup() {
            if (standalone) {
                unlinkToDeath(this, 0);
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
    public boolean removeUserService(Bundle options) {
        enforceCallingPermission("removeUserService");

        int uid = Binder.getCallingUid();
        int appId = UserHandleCompat.getAppId(uid);
        int userId = UserHandleCompat.getUserId(uid);

        String packageName = options.getString(USER_SERVICE_ARG_PACKAGE_NAME);
        String id = Objects.requireNonNull(options.getString(USER_SERVICE_ARG_ID), "id is null");
        String key = packageName + ":" + id;

        ensureCallingPackageForUserService(packageName, appId, userId);

        UserServiceRecord record = userServiceRecords.get(key);
        if (record == null) return false;
        return removeUserServiceImpl(record);
    }

    private boolean removeUserServiceImpl(UserServiceRecord record) {
        boolean res = userServiceRecords.values().remove(record);
        if (res) {
            record.cleanup();
        }
        return res;
    }

    @Override
    public IBinder addUserService(Bundle options) {
        enforceCallingPermission("addUserService");

        return addUserServiceImpl(options);
    }

    private IBinder addUserServiceImpl(Bundle options) {
        Objects.requireNonNull(options, "options is null");

        int uid = Binder.getCallingUid();
        int appId = UserHandleCompat.getAppId(uid);
        int userId = UserHandleCompat.getUserId(uid);

        String id = Objects.requireNonNull(options.getString(USER_SERVICE_ARG_ID), "id is null");
        String packageName = options.getString(USER_SERVICE_ARG_PACKAGE_NAME);
        String classname = Objects.requireNonNull(options.getString(USER_SERVICE_ARG_CLASSNAME), "classname is null");
        int versionCode = options.getInt(USER_SERVICE_ARG_VERSION_CODE, 1);
        boolean alwaysRecreate = options.getBoolean(USER_SERVICE_ARG_ALWAYS_RECREATE, false);
        String processNameSuffix = options.getString(USER_SERVICE_ARG_PROCESS_NAME);
        boolean debug = options.getBoolean(USER_SERVICE_ARG_DEBUGGABLE, false);
        boolean standalone = processNameSuffix != null;
        String key = packageName + ":" + id;

        PackageInfo packageInfo = ensureCallingPackageForUserService(packageName, appId, userId);
        ApplicationInfo applicationInfo = packageInfo.applicationInfo;

        UserServiceRecord record = userServiceRecords.get(key);
        if (record != null) {
            if (record.versionCode != versionCode) {
                LOGGER.v("destroy %s because version code not matched (old=%d, new=%d)", key, record.versionCode, versionCode);
            } else if (record.standalone != standalone) {
                LOGGER.v("destroy %s because standalone not matched (old=%s, new=%s)", key, Boolean.toString(record.standalone), Boolean.toString(standalone));
            } else if (alwaysRecreate) {
                LOGGER.v("destroy %s because always recreate", key);
            } else if (record.service == null || !record.service.pingBinder()) {
                LOGGER.v("%s is dead", key);
            } else {
                LOGGER.v("found existing %s", key);
                return record.service;
            }

            removeUserServiceImpl(record);
        }

        LOGGER.v("creating new %s...", key);

        if (!standalone) {
            return startUserServiceLocalProcess(packageName, classname, key, versionCode, applicationInfo);
        } else {
            return startUserServiceNewProcess(packageName, processNameSuffix, uid, classname, key, versionCode, applicationInfo, debug);
        }
    }

    private String generateTokenForUserService() {
        return UUID.randomUUID().toString() + "-" + System.currentTimeMillis();
    }

    private IBinder startUserServiceLocalProcess(String packageName, String classname, String key, int versionCode, ApplicationInfo applicationInfo) {
        IBinder service;

        try {
            ClassLoader classLoader;
            classLoader = new DexClassLoader(applicationInfo.sourceDir, "/data/local/shizuku/user/" + key, applicationInfo.nativeLibraryDir, ClassLoader.getSystemClassLoader());

            // createPackageContext gets old apk path after reinstall
            /*UserHandle userHandle = HiddenApiBridge.createUserHandle(UserHandleCompat.getUserId(applicationInfo.uid));
            Context context = HiddenApiBridge.Context_createPackageContextAsUser(systemContext, packageName, Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY, userHandle);
            classLoader = context.getClassLoader();*/

            Class<?> serviceClass = classLoader.loadClass(classname);
            Constructor<?> constructor;

            try {
                constructor = serviceClass.getConstructor(CancellationSignal.class);

                CancellationSignal cancellationSignal = new CancellationSignal();
                cancellationSignal.setOnCancelListener(() -> {
                    UserServiceRecord record = userServiceRecords.get(key);
                    if (record != null) {
                        removeUserServiceImpl(record);
                        LOGGER.v("remove %s by user", key);
                    }
                });
                service = (IBinder) constructor.newInstance(cancellationSignal);
            } catch (Throwable e) {
                LOGGER.w("constructor with CancellationSignal not found");
                constructor = serviceClass.getConstructor();
                service = (IBinder) constructor.newInstance();
            }
        } catch (Throwable tr) {
            LOGGER.w(tr, "unable to create service %s", key);
            return null;
        }

        UserServiceRecord record = new UserServiceRecord(service, versionCode, applicationInfo.sourceDir);
        userServiceRecords.put(key, record);

        LOGGER.v("created %s: version=%d, token=%s", key, versionCode, record.token);
        return record.service;
    }

    private static final String USER_SERVICE_CMD_FORMAT = "(CLASSPATH=/data/local/tmp/shizuku/starter-v%d.dex /system/bin/app_process%s /system/bin " +
            "--nice-name=%s %s " +
            "--token=%s --package=%s --class=%s --uid=%d%s)&";

    private IBinder startUserServiceNewProcess(String packageName, String processNameSuffix, int callingUid, String classname, String key, int versionCode, ApplicationInfo applicationInfo, boolean debug) {
        UserServiceRecord record = new UserServiceRecord(versionCode, applicationInfo.sourceDir);
        userServiceRecords.put(key, record);

        String processName = String.format("%s:%s", packageName, processNameSuffix);
        String cmd = String.format(Locale.ENGLISH, USER_SERVICE_CMD_FORMAT,
                ShizukuApiConstants.SERVER_VERSION, debug ? (" " + USER_SERVICE_CMD_DEBUG) : "",
                processName, "moe.shizuku.starter.ServiceStarter",
                record.token, packageName, classname, callingUid, debug ? (" " + "--debug-name=" + processName) : "");

        java.lang.Process process;
        int exitCode;
        try {
            process = Runtime.getRuntime().exec("sh");
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

        try {
            if (!record.latch.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("binder for " + key + " not received in 5s");
            }
        } catch (InterruptedException e) {
            throw new IllegalStateException(e.getMessage());
        }
        return record.service;
    }

    @Override
    public void sendUserService(IBinder binder, Bundle options) {
        enforceManager("sendUserService");

        sendUserServiceImpl(binder, options);
    }

    private void sendUserServiceImpl(IBinder binder, Bundle options) {
        Objects.requireNonNull(binder, "binder is null");
        String token = Objects.requireNonNull(options.getString(ShizukuApiConstants.USER_SERVICE_ARG_TOKEN), "token is null");

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

        LOGGER.v("received %s", token);

        UserServiceRecord record = entry.getValue();
        record.onBinderReceived(binder);
    }

    @Override
    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        //LOGGER.d("transact: code=%d, calling uid=%d", code, Binder.getCallingUid());
        if (code == ShizukuApiConstants.BINDER_TRANSACTION_transact) {
            data.enforceInterface(ShizukuApiConstants.BINDER_DESCRIPTOR);
            transactRemote(data, reply, flags);
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

                if (ArrayUtils.contains(pi.requestedPermissions, PERMISSION)) {
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
        sendBinderToUserApp(binder, ShizukuApiConstants.MANAGER_APPLICATION_ID, userId);
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
            extra.putParcelable(ShizukuApiConstants.EXTRA_BINDER, new BinderContainer(binder));

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
}
