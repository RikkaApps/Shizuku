package moe.shizuku.server;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.UUID;

import moe.shizuku.ShizukuConfiguration;
import moe.shizuku.ShizukuIntent;
import moe.shizuku.api.ShizukuClient;
import moe.shizuku.server.delegate.ActivityManagerDelegate;
import moe.shizuku.server.util.ServerLog;
import moe.shizuku.server.util.Utils;

public class Server extends Handler {

    public static final int MESSAGE_EXIT = 1;

    private UUID mToken;

    private Server(UUID token) {
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
        if (ShizukuClient.stopServer()) {
            ServerLog.i("old server found, send stop...");
            Thread.sleep(500);
        }

        ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(ShizukuConfiguration.PORT, 0, ShizukuConfiguration.HOST);
        } catch (IOException e) {
            ServerLog.e("cannot start server", e);
            return false;
        }
        serverSocket.setReuseAddress(true);

        SocketThread socket = new SocketThread(this, serverSocket, mToken);

        Thread socketThread = new Thread(socket);
        socketThread.start();
        ServerLog.i("Build.VERSION.SDK_INT: " + Build.VERSION.SDK_INT);
        ServerLog.i("Build.DEVICE: " + Build.DEVICE);
        ServerLog.i("start version: " + ShizukuConfiguration.VERSION + " token: " + mToken);

        // send token to manager app
        sendTokenToManger(mToken, 0);

        return true;
    }

    public static void sendTokenToManger(UUID token, int userId) {
        Intent intent = new Intent(ShizukuIntent.ACTION_SERVER_STARTED)
                .addCategory(Intent.CATEGORY_DEFAULT)
                .setComponent(new ComponentName(ShizukuIntent.MANAGER_APPLICATION_ID, ShizukuIntent.MANAGER_PACKAGE + ".TokenServerStartActivity"))
                .putExtra(ShizukuIntent.EXTRA_TOKEN_MOST_SIG, token.getMostSignificantBits())
                .putExtra(ShizukuIntent.EXTRA_TOKEN_LEAST_SIG, token.getLeastSignificantBits());

        try {
            String mimeType = intent.getType();
            if (mimeType == null && intent.getData() != null
                    && "content".equals(intent.getData().getScheme())) {
                mimeType = ActivityManagerDelegate.getProviderMimeType(intent.getData(), userId);
            }
            ActivityManagerDelegate.startActivityAsUser(null, null, intent, mimeType,
                    null, null, 0, 0, null, null, userId);

            ServerLog.i("send token to manager app in user " + userId);
        } catch (RemoteException e) {
            ServerLog.e("failed send token to manager app", e);
        }
    }

    public static void main(String[] args) throws IOException, RemoteException, InterruptedException {
        Utils.setOut();

        Looper.prepare();

        Server server = new Server(getToken(args));

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
