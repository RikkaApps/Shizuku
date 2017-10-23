package moe.shizuku.server;

import android.content.Intent;
import android.content.pm.UserInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.UUID;

import moe.shizuku.ShizukuConstants;
import moe.shizuku.api.ShizukuClient;
import moe.shizuku.server.api.Compat;
import moe.shizuku.server.util.ServerLog;
import moe.shizuku.server.util.Utils;

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

    public boolean start() throws IOException, RemoteException, InterruptedException {
        if (Compat.VERSION != Build.VERSION.SDK_INT) {
            ServerLog.e("api version not matching, please open Shizuku Manager and try again.");
            return false;
        }

        if (ShizukuClient.stopServer()) {
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

        SocketThread socket = new SocketThread(this, serverSocket, mToken);

        Thread socketThread = new Thread(socket);
        socketThread.start();
        ServerLog.i("uid: " + Process.myUid());
        ServerLog.i("api version: " + Build.VERSION.SDK_INT);
        ServerLog.i("device: " + Build.DEVICE);
        ServerLog.i("start version: " + ShizukuConstants.SERVER_VERSION + " token: " + mToken);

        // send token to manager app
        sendTokenToManger(mToken);

        return true;
    }

    public static void sendTokenToManger(UUID token) {
        try {
            for (UserInfo userInfo : Compat.getUsers()) {
                sendTokenToManger(token, userInfo.id);
            }
        } catch (RemoteException e) {
            ServerLog.e("failed send token to manager app (filed to get users)", e);
        }
    }

    public static void sendTokenToManger(UUID token, int userId) {
        Intent intent = new Intent(ShizukuConstants.ACTION_SERVER_STARTED)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .addCategory(Intent.CATEGORY_DEFAULT)
                .setPackage(ShizukuConstants.MANAGER_APPLICATION_ID)
                .putExtra(ShizukuConstants.EXTRA_TOKEN_MOST_SIG, token.getMostSignificantBits())
                .putExtra(ShizukuConstants.EXTRA_TOKEN_LEAST_SIG, token.getLeastSignificantBits());

        try {
            String mimeType = intent.getType();
            if (mimeType == null && intent.getData() != null
                    && "content".equals(intent.getData().getScheme())) {
                mimeType = Compat.getProviderMimeType(intent.getData(), userId);
            }
            Compat.startActivityAsUser(intent, mimeType, userId);

            ServerLog.i("send token to manager app in user " + userId);
        } catch (RemoteException e) {
            ServerLog.e("failed send token to manager app", e);
        }
    }

    public static void main(String[] args) throws IOException, RemoteException, InterruptedException {
        Utils.setOut();

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
}
