package moe.shizuku.server;

import android.app.IActivityManager;
import android.content.Intent;
import android.content.pm.UserInfo;
import android.net.LocalServerSocket;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.IUserManager;
import android.os.Parcel;
import android.os.Process;
import android.os.RemoteException;
import android.system.Os;

import java.io.IOException;
import java.util.UUID;

import moe.shizuku.api.BinderContainer;
import moe.shizuku.api.ShizukuApiConstants;
import moe.shizuku.server.api.Api;

import static moe.shizuku.server.utils.Logger.LOGGER;

public class ShizukuService extends IShizukuService.Stub {

    private UUID mToken;

    ShizukuService(UUID token) {
        super();

        if (token == null) {
            mToken = UUID.randomUUID();
        } else {
            LOGGER.i("using token from arg");

            mToken = token;
        }
    }

    /*private int checkCallingPermission(String permission) {
        try {
            return Api.checkPermission(permission,
                    Binder.getCallingPid(),
                    Binder.getCallingUid());
        } catch (Throwable tr) {
            LOGGER.w(tr, "checkCallingPermission");
            return PackageManager.PERMISSION_DENIED;
        }
    }

    private void enforceCallingPermission(String permission, String func) {
        if (Binder.getCallingPid() == Os.getpid()) {
            return;
        }

        if (checkCallingPermission(permission)
                == PackageManager.PERMISSION_GRANTED) {
            return;
        }
        String msg = "Permission Denial: " + func + " from pid="
                + Binder.getCallingPid()
                + ", uid=" + Binder.getCallingUid()
                + " requires " + permission;
        LOGGER.w(msg);
        throw new SecurityException(msg);
    }*/

    private void transactRemote(IBinder binder, int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        LOGGER.i("transactRemote %s code=%d", binder.getClass(), code);
        Parcel newData = Parcel.obtain();
        try {
            newData.appendFrom(data, data.dataPosition(), data.dataAvail());
        } catch (Throwable tr) {
            LOGGER.w(tr, "appendFrom");
            return;
        }
        try {
            long id = Binder.clearCallingIdentity();
            binder.transact(code, newData, reply, flags);
            Binder.restoreCallingIdentity(id);
        } finally {
            newData.recycle();
        }
    }

    @Override
    public int getVersion() {
        return ShizukuApiConstants.SERVER_VERSION;
    }

    @Override
    public int getUid() {
        return Os.getuid();
    }

    @Override
    public int checkPermission(String permission) {
        return 0;
    }

    @Override
    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        LOGGER.i("transact %d calling uid: %d", code, Binder.getCallingUid());
        if (code == ShizukuApiConstants.BINDER_TRANSACTION_transactRemote) {
            data.enforceInterface(ShizukuApiConstants.BINDER_DESCRIPTOR);
            IBinder targetBinder = data.readStrongBinder();
            int targetCode = data.readInt();
            transactRemote(targetBinder, targetCode, data, reply, flags);
            return true;
        }
        return super.onTransact(code, data, reply, flags);
    }

    boolean start() {
        LocalServerSocket serverSocket;
        try {
            serverSocket = new LocalServerSocket(ShizukuApiConstants.SOCKET_NAME);
        } catch (IOException e) {
            LOGGER.e("cannot start server socket", e);
            return false;
        }

        SocketThread socket = new SocketThread(serverSocket, mToken, this);

        Thread socketThread = new Thread(socket);
        socketThread.start();
        LOGGER.i("uid: " + Process.myUid());
        LOGGER.i("api version: " + Build.VERSION.SDK_INT);
        LOGGER.i("device: " + Build.DEVICE);
        LOGGER.i("start version: " + ShizukuApiConstants.SERVER_VERSION + " token: " + mToken);

        // send token to manager app
        sendTokenToManger(mToken, this);

        return true;
    }

    private static void sendTokenToManger(UUID token, Binder binder) {
        try {
            IUserManager um = Api.USER_MANAGER_SINGLETON.get();
            if (um != null) {
                for (UserInfo userInfo : um.getUsers(false)) {
                    sendTokenToManger(token, binder, userInfo.id);
                }
            }
        } catch (Exception e) {
            LOGGER.e("exception when call getUsers, try user 0", e);

            sendTokenToManger(token, binder, 0);
        }
    }

    private static void sendTokenToManger(UUID token, Binder binder, int userId) {
        Intent intent = new Intent(ServerConstants.ACTION_SERVER_STARTED)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .addCategory(Intent.CATEGORY_DEFAULT)
                .setPackage(ShizukuApiConstants.MANAGER_APPLICATION_ID)
                .putExtra(ShizukuApiConstants.EXTRA_BINDER, new BinderContainer(binder))
                .putExtra(ServerConstants.EXTRA_TOKEN_MOST_SIG, token.getMostSignificantBits())
                .putExtra(ServerConstants.EXTRA_TOKEN_LEAST_SIG, token.getLeastSignificantBits());

        try {
            IActivityManager am = Api.ACTIVITY_MANAGER_SINGLETON.get();
            am.startActivityAsUser(null, null, intent, null,
                    null, null, 0, 0, null, null, userId);

            LOGGER.i("send token to manager app in user " + userId);
        } catch (Exception e) {
            LOGGER.e("failed send token to manager app", e);
        }
    }

    static void sendTokenToUserApp(Binder binder, String packageName, int userId) {
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
        } catch (Exception e) {
            LOGGER.e(e, "failed send token to user app%s in user %d", packageName, userId);
        }
    }
}
