package moe.shizuku.server;

import android.content.Intent;
import android.content.pm.UserInfo;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.Process;
import android.os.RemoteException;
import android.system.Os;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;

import moe.shizuku.ShizukuConstants;
import moe.shizuku.api.BinderHolder;
import moe.shizuku.io.ParcelInputStream;
import moe.shizuku.io.ParcelOutputStream;
import moe.shizuku.server.api.Compat;
import moe.shizuku.server.util.ServerLog;

public class ShizukuServer extends Handler {

    public static final int MESSAGE_EXIT = 1;

    private UUID mToken;

    private ShizukuServer(UUID token) {
        super();

        if (token == null) {
            mToken = UUID.randomUUID();
        } else {
            ServerLog.i("using token from arg");

            mToken = token;
        }
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case MESSAGE_EXIT:
                //noinspection ConstantConditions
                Looper.myLooper().quit();
            break;
        }
    }

    private Binder mBinder = new Binder() {

        @Override
        protected boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            //ServerLog.i("bky " + code);
            return super.onTransact(code, data, reply, flags);
        }
    };

    public boolean start() throws IOException, InterruptedException {
        if (Compat.VERSION == ShizukuConstants.MAX_SDK) {
            /*if (Build.VERSION.SDK_INT > ShizukuConstants.MAX_SDK) {
                ServerLog.w("unsupported system (" + Build.VERSION.SDK_INT + ") detected, some API may not work");
            } else */if (Build.VERSION.SDK_INT == ShizukuConstants.MAX_SDK && Build.VERSION.PREVIEW_SDK_INT > 0) {
                ServerLog.w("preview system detect, some API may not work");
            }
        } else if (Compat.VERSION != Build.VERSION.SDK_INT) {
            ServerLog.e("API version not matched, please open Shizuku Manager and try again.");
            return false;
        }

        if (Build.VERSION.SDK_INT >= 28) {
            disableHiddenApiBlacklist();
        }

        if (stopServer()) {
            ServerLog.i("old server found, send stop...");
            Thread.sleep(500);
        }

        ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(ShizukuConstants.PORT, 0, ShizukuConstants.HOST);
        } catch (IOException e) {
            ServerLog.e("cannot start server", e);
            return false;
        }
        serverSocket.setReuseAddress(true);

        SocketThread socket = new SocketThread(this, serverSocket, mToken, mBinder);

        Thread socketThread = new Thread(socket);
        socketThread.start();
        ServerLog.i("uid: " + Process.myUid());
        ServerLog.i("api version: " + Build.VERSION.SDK_INT);
        ServerLog.i("device: " + Build.DEVICE);
        ServerLog.i("start version: " + ShizukuConstants.SERVER_VERSION + " token: " + mToken);

        // send token to manager app
        sendTokenToManger(mToken, mBinder);

        return true;
    }

    private static void disableHiddenApiBlacklist() {
        try {
            java.lang.Process process = new ProcessBuilder(new String[]{"settings", "put", "global", "hidden_api_blacklist_exemptions", "*"}).start();

            int res;
            if ((res = process.waitFor()) == 0) {
                ServerLog.i("disabled hidden api blacklist");
            } else {
                ServerLog.w("failed to disable hidden api blacklist, res=" + res);
            }
        } catch (Throwable tr) {
            ServerLog.w("failed to disable hidden api blacklist", tr);
        }
    }

    public static void sendTokenToManger(UUID token, Binder binder) {
        try {
            for (UserInfo userInfo : Compat.getUsers()) {
                sendTokenToManger(token, binder, userInfo.id);
            }
        } catch (Exception e) {
            ServerLog.e("exception when call getUsers, try user 0", e);

            sendTokenToManger(token, binder, 0);
        }
    }

    public static void sendTokenToManger(UUID token, Binder binder, int userId) {
        Intent intent = new Intent(ShizukuConstants.ACTION_SERVER_STARTED)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .addCategory(Intent.CATEGORY_DEFAULT)
                .setPackage(ShizukuConstants.MANAGER_APPLICATION_ID)
                //.putExtra(ShizukuConstants.EXTRA_BINDER, new BinderHolder(binder))
                .putExtra(ShizukuConstants.EXTRA_TOKEN_MOST_SIG, token.getMostSignificantBits())
                .putExtra(ShizukuConstants.EXTRA_TOKEN_LEAST_SIG, token.getLeastSignificantBits());

        //ServerLog.i("bky " + binder.pingBinder());

        try {
            String mimeType = intent.getType();
            if (mimeType == null && intent.getData() != null
                    && "content".equals(intent.getData().getScheme())) {
                mimeType = Compat.getProviderMimeType(intent.getData(), userId);
            }
            Compat.startActivityAsUser(intent, mimeType, userId);

            ServerLog.i("send token to manager app in user " + userId);
        } catch (Exception e) {
            ServerLog.e("failed send token to manager app", e);
        }
    }

    public static void main(String[] args) throws IOException, RemoteException, InterruptedException {
        // fix owner
        if (Process.myUid() == 0) {
            try {
                Os.chown("/data/local/tmp/shizuku_starter", 2000, 2000);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        Looper.prepare();

        ShizukuServer server = new ShizukuServer(getToken(args));

        if (!server.start()) {
            System.exit(1);
            return;
        }

        Looper.loop();

        ServerLog.i("server exit");
        System.exit(0);
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

    private static boolean stopServer() {
        try {
            Socket socket = new Socket(ShizukuConstants.HOST, ShizukuConstants.PORT);
            socket.setSoTimeout(ShizukuConstants.TIMEOUT);
            ParcelOutputStream os = new ParcelOutputStream(socket.getOutputStream());
            ParcelInputStream is = new ParcelInputStream(socket.getInputStream());
            os.writeString("Shizuku_requestStop");
            is.readException();

            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
