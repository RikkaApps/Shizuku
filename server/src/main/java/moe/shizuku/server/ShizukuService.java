package moe.shizuku.server;

import android.content.IContentProvider;
import android.content.pm.PackageManager;
import android.content.pm.UserInfo;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IUserManager;
import android.os.Parcel;
import android.os.RemoteException;
import android.system.Os;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import moe.shizuku.api.BinderContainer;
import moe.shizuku.api.ShizukuApiConstants;
import moe.shizuku.server.api.Api;
import moe.shizuku.server.api.RemoteProcessHolder;
import moe.shizuku.server.reflection.ContentProviderHolderHelper;
import moe.shizuku.server.reflection.IContentProviderHelper;
import moe.shizuku.server.utils.BuildUtils;

import static moe.shizuku.server.utils.Logger.LOGGER;

public class ShizukuService extends IShizukuService.Stub {

    private static final String PERMISSION_MANAGER = "moe.shizuku.manager.permission.MANAGER";
    private static final String PERMISSION = BuildUtils.isPreM() ? ShizukuApiConstants.PERMISSION_PRE_23 : ShizukuApiConstants.PERMISSION;

    private static final Map<Integer, String> PID_TOKEN = new HashMap<>();

    private String mToken;

    ShizukuService(UUID token) {
        super();

        if (token == null) {
            mToken = UUID.randomUUID().toString();
        } else {
            LOGGER.i("using token from arg");

            mToken = token.toString();
        }

        BinderSender.register(this);
    }

    static Map<Integer, String> getPidToken() {
        return PID_TOKEN;
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

        if (!BuildUtils.isPreM() || !checkToken)
            return;

        String token = PID_TOKEN.get(Binder.getCallingPid());
        if (mToken.equals(token))
            return;

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
            Object holder = Api.getContentProviderExternal(name, userId, token);
            if (holder == null) {
                LOGGER.e("can't find provider %s in user %d", name, userId);
                return;
            }

            provider = ContentProviderHolderHelper.getProvider(holder);
            if (provider == null) {
                LOGGER.e("provider is null %s %d", name, userId);
                return;
            }

            Bundle extra = new Bundle();
            extra.putParcelable(ShizukuApiConstants.EXTRA_BINDER, new BinderContainer(binder));

            Bundle reply = IContentProviderHelper.call(provider, null, name, "sendBinder", null, extra);

            LOGGER.i("send binder to user app %s in user %d", packageName, userId);
        } catch (Throwable tr) {
            LOGGER.e(tr, "failed send binder to user app %s in user %d", packageName, userId);
        } finally {
            if (provider != null) {
                try {
                    Api.removeContentProviderExternal(name, token);
                } catch (Throwable tr) {
                    LOGGER.w(tr, "removeContentProviderExternal");
                }
            }
        }
    }
}
