package moe.shizuku.server;

import android.app.IActivityManager;
import android.content.Intent;
import android.content.pm.UserInfo;
import android.os.Binder;
import android.os.IUserManager;
import android.os.Looper;
import android.os.Parcel;
import android.os.Process;
import android.os.RemoteException;
import android.system.Os;

import java.io.IOException;
import java.util.UUID;

import moe.shizuku.api.BinderHolder;
import moe.shizuku.server.api.Api;

import static moe.shizuku.server.utils.Logger.LOGGER;

public class ShizukuServer {

    private UUID mToken;

    private ShizukuServer(UUID token) {
        super();

        if (token == null) {
            mToken = UUID.randomUUID();
        } else {
            LOGGER.i("using token from arg");

            mToken = token;
        }
    }

    private Binder mBinder = new Binder() {

        @Override
        protected boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            LOGGER.i("bky " + code);
            return super.onTransact(code, data, reply, flags);
        }
    };

    public boolean start() throws IOException, InterruptedException {
        /*if (Compat.VERSION == ShizukuConstants.MAX_SDK) {
            if (Build.VERSION.SDK_INT == ShizukuConstants.MAX_SDK && Build.VERSION.PREVIEW_SDK_INT > 0) {
                Logger.w("preview system detect, some API may not work");
            }
        } else if (Compat.VERSION != Build.VERSION.SDK_INT) {
            Logger.e("API version not matched, please open Shizuku Manager and try again.");
            return false;
        }

        if (Build.VERSION.SDK_INT >= 28) {
            disableHiddenApiBlacklist();
        }

        if (stopServer()) {
            Logger.i("old server found, send stop...");
            Thread.sleep(500);
        }

        ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(ShizukuConstants.PORT, 0, ShizukuConstants.HOST);
        } catch (IOException e) {
            Logger.e("cannot start server", e);
            return false;
        }
        serverSocket.setReuseAddress(true);

        SocketThread socket = new SocketThread(this, serverSocket, mToken, mBinder);

        Thread socketThread = new Thread(socket);
        socketThread.start();
        Logger.i("uid: " + Process.myUid());
        Logger.i("api version: " + Build.VERSION.SDK_INT);
        Logger.i("device: " + Build.DEVICE);
        Logger.i("start version: " + ShizukuConstants.SERVER_VERSION + " token: " + mToken);*/

        // send token to manager app
        sendTokenToManger(mToken, mBinder);

        return true;
    }

    private static void disableHiddenApiBlacklist() {
        try {
            java.lang.Process process = new ProcessBuilder(new String[]{"settings", "put", "global", "hidden_api_blacklist_exemptions", "*"}).start();

            int res;
            if ((res = process.waitFor()) == 0) {
                LOGGER.i("disabled hidden api blacklist");
            } else {
                LOGGER.w("failed to disable hidden api blacklist, res=" + res);
            }
        } catch (Throwable tr) {
            LOGGER.w("failed to disable hidden api blacklist", tr);
        }
    }

    public static void sendTokenToManger(UUID token, Binder binder) {
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

    public static void sendTokenToManger(UUID token, Binder binder, int userId) {
        Intent intent = new Intent(ShizukuConstants.ACTION_SERVER_STARTED)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .addCategory(Intent.CATEGORY_DEFAULT)
                .setPackage(ShizukuConstants.MANAGER_APPLICATION_ID)
                .putExtra(ShizukuConstants.EXTRA_BINDER, new BinderHolder(binder))
                .putExtra(ShizukuConstants.EXTRA_TOKEN_MOST_SIG, token.getMostSignificantBits())
                .putExtra(ShizukuConstants.EXTRA_TOKEN_LEAST_SIG, token.getLeastSignificantBits());

        //ServerLog.i("bky " + binder.pingBinder());

        try {
            IActivityManager am = Api.ACTIVITY_MANAGER_SINGLETON.get();
            am.startActivityAsUser(null, null, intent, null,
                    null, null, 0, 0, null, null, userId);

            LOGGER.i("send token to manager app in user " + userId);
        } catch (Exception e) {
            LOGGER.e("failed send token to manager app", e);
        }
    }

    private static UUID getToken(String[] args) {
        if (args.length > 0) {
            try {
                return UUID.fromString(args[0]);
            } catch (IllegalArgumentException ignored) {
            }
        }
        return null;
    }

    private static void fixStartOwner() {
        if (Process.myUid() == 0) {
            try {
                Os.chown("/data/local/tmp/shizuku_starter", 2000, 2000);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws IOException, RemoteException, InterruptedException {
        fixStartOwner();

        LOGGER.i("server v3, uid=%d", Os.getuid());

        Looper.prepare();

        ShizukuServer server = new ShizukuServer(getToken(args));
        if (!server.start()) {
            System.exit(1);
            return;
        }

        Looper.loop();

        LOGGER.i("server exit");
        System.exit(0);
    }
}
