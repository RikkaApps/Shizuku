package moe.shizuku.server;

import android.app.IActivityManager;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.UserInfo;
import android.os.Binder;
import android.os.IBinder;
import android.os.IUserManager;
import android.os.Parcel;
import android.os.RemoteException;
import android.system.Os;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import moe.shizuku.api.BinderContainer;
import moe.shizuku.api.ShizukuApiConstants;
import moe.shizuku.server.api.Api;
import moe.shizuku.server.utils.ArrayUtils;
import moe.shizuku.server.utils.BuildUtils;
import moe.shizuku.server.utils.RemoteProcessHolder;

import static moe.shizuku.server.utils.Logger.LOGGER;

public class ShizukuService extends IShizukuService.Stub {

    private static final String PERMISSION_MANAGER = "moe.shizuku.manager.permission.MANAGER";
    private static final String PERMISSION = BuildUtils.isPreM() ? ShizukuApiConstants.PERMISSION_PRE_23 : ShizukuApiConstants.PERMISSION;

    private static final Map<Integer, String> PID_TOKEN = new HashMap<>();

    private class ProcessObserver extends moe.shizuku.server.api.ProcessObserver {

        private final List<Integer> pids = new ArrayList<>();

        @Override
        public void onForegroundActivitiesChanged(int pid, int uid, boolean foregroundActivities) throws RemoteException {
            LOGGER.d("onForegroundActivitiesChanged: pid=%d, uid=%d, foregroundActivities=%s", pid, uid, foregroundActivities ? "true" : "false");

            if (pids.contains(pid) || !foregroundActivities) {
                return;
            }
            pids.add(pid);

            String[] packages = Api.getPackagesForUid(uid);
            if (packages == null)
                return;

            LOGGER.d("new process: packages=%s", Arrays.toString(packages));

            int userId = uid / 100000;
            for (String packageName : packages) {
                PackageInfo pi = Api.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS, userId);
                if (pi == null || pi.requestedPermissions == null)
                    continue;

                if (ArrayUtils.contains(pi.requestedPermissions, PERMISSION_MANAGER)) {
                    if (Api.checkPermission(PERMISSION_MANAGER, pid, uid) == PackageManager.PERMISSION_GRANTED) {
                        sendBinderToManger(ShizukuService.this, userId);
                        return;
                    }
                } else if (ArrayUtils.contains(pi.requestedPermissions, PERMISSION)) {
                    sendBinderToUserApp(ShizukuService.this, packageName, userId);
                    return;
                }
            }
        }

        @Override
        public void onProcessDied(int pid, int uid) {
            LOGGER.d("onProcessDied: pid=%d, uid=%d", pid, uid);

            int index = pids.indexOf(pid);
            if (index != -1) {
                pids.remove(index);

                PID_TOKEN.remove(pid);
            }
        }
    }

    private String mToken;

    ShizukuService(UUID token) {
        super();

        if (token == null) {
            mToken = UUID.randomUUID().toString();
        } else {
            LOGGER.i("using token from arg");

            mToken = token.toString();
        }

        try {
            Api.registerProcessObserver(new ProcessObserver());
        } catch (RemoteException e) {
            LOGGER.e(e, "registerProcessObserver");
        }
    }

    private int checkCallingPermission(String permission) {
        try {
            return Api.checkPermission(permission,
                    Binder.getCallingPid(),
                    Binder.getCallingUid());
        } catch (Throwable tr) {
            LOGGER.w(tr, "checkCallingPermission");
            return PackageManager.PERMISSION_DENIED;
        }
    }

    private void enforceCallingPermission(String func, String permission) {
        if (Binder.getCallingPid() == Os.getpid()) {
            return;
        }

        if (checkCallingPermission(permission) == PackageManager.PERMISSION_GRANTED)
            return;

        String msg = "Permission Denial: " + func + " from pid="
                + Binder.getCallingPid()
                + ", uid=" + Binder.getCallingUid()
                + " requires " + permission;
        LOGGER.w(msg);
        throw new SecurityException(msg);
    }

    private void enforceCallingPermission(String func, boolean checkToken) {
        if (Binder.getCallingPid() == Os.getpid()) {
            return;
        }

        if (checkCallingPermission(PERMISSION_MANAGER) == PackageManager.PERMISSION_GRANTED)
            return;

        enforceCallingPermission(func, PERMISSION);

        if (BuildUtils.isPreM() && checkToken) {
            String token = PID_TOKEN.get(Binder.getCallingPid());
            if (mToken.equals(token))
                return;
        }

        String msg = "Permission Denial: " + func + " from pid="
                + Binder.getCallingPid()
                + " requires a valid token, call setPidToken first";
        LOGGER.w(msg);
        throw new SecurityException(msg);
    }

    private void transactRemote(Parcel data, Parcel reply, int flags) throws RemoteException {
        IBinder targetBinder = data.readStrongBinder();
        int targetCode = data.readInt();

        enforceCallingPermission("transactRemote", true);

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
    public int getVersion() {
        enforceCallingPermission("getVersion", true);
        return ShizukuApiConstants.SERVER_VERSION;
    }

    @Override
    public int getUid() {
        enforceCallingPermission("getUid", true);
        return Os.getuid();
    }

    @Override
    public int checkPermission(String permission) throws RemoteException {
        enforceCallingPermission("checkPermission", true);
        return Api.checkPermission(permission, Os.getuid());
    }

    @Override
    public String getToken() {
        enforceCallingPermission("getToken", PERMISSION_MANAGER);
        return mToken;
    }

    @Override
    public boolean setPidToken(String token) {
        enforceCallingPermission("setPidToken", false);

        if (!BuildUtils.isPreM()) {
            throw new IllegalStateException("calling setToken on API 23+");
        }

        PID_TOKEN.put(Binder.getCallingPid(), token);
        return mToken.equals(token);
    }

    @Override
    public IRemoteProcess newProcess(String[] cmd, String[] env, String dir) throws RemoteException {
        enforceCallingPermission("newProcess", true);

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
    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        //LOGGER.d("transact: code=%d, calling uid=%d", code, Binder.getCallingUid());
        if (code == ShizukuApiConstants.BINDER_TRANSACTION_transact) {
            data.enforceInterface(ShizukuApiConstants.BINDER_DESCRIPTOR);
            transactRemote(data, reply, flags);
            return true;
        }
        return super.onTransact(code, data, reply, flags);
    }

    boolean sendBinderToManager() {
        sendBinderToManger(this);

        return true;
    }

    private static void sendBinderToManger(Binder binder) {
        try {
            IUserManager um = Api.USER_MANAGER_SINGLETON.get();
            if (um != null) {
                for (UserInfo userInfo : um.getUsers(false)) {
                    sendBinderToManger(binder, userInfo.id);
                }
            }
        } catch (Throwable tr) {
            LOGGER.e("exception when call getUsers, try user 0", tr);

            sendBinderToManger(binder, 0);
        }
    }

    static boolean sendBinderToManger(Binder binder, int userId) {
        return sendBinderToUserApp(binder, ShizukuApiConstants.MANAGER_APPLICATION_ID, userId);
    }

    static boolean sendBinderToUserApp(Binder binder, String packageName, int userId) {
        Intent intent = new Intent(ShizukuApiConstants.ACTION_SEND_BINDER)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .addCategory(Intent.CATEGORY_DEFAULT)
                .setPackage(packageName)
                .putExtra(ShizukuApiConstants.EXTRA_BINDER, new BinderContainer(binder));

        try {
            IActivityManager am = Api.ACTIVITY_MANAGER_SINGLETON.get();
            am.startActivityAsUser(null, null, intent, null,
                    null, null, 0, 0, null, null, userId);

            LOGGER.i("send token to user app %s in user %d", packageName, userId);
            return true;
        } catch (Throwable tr) {
            LOGGER.e(tr, "failed send token to user app%s in user %d", packageName, userId);
            return false;
        }
    }
}
