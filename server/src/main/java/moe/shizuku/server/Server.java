package moe.shizuku.server;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.UUID;

import moe.shizuku.ShizukuConfiguration;
import moe.shizuku.server.util.ServerLog;
import moe.shizuku.server.util.Utils;

public class Server extends Handler {

    public static final int MESSAGE_EXIT = 1;

    private static UUID sToken;

    public static void main(String[] args) throws IOException, RemoteException, InterruptedException {
        Utils.setOut();

        Looper.prepare();

        UUID token = null;
        if (args.length > 0) {
            try {
                token = UUID.fromString(args[0]);

                ServerLog.i("using token from arg");
            } catch (IllegalArgumentException ignored) {
            }
        }

        if (token == null) {
            setToken(UUID.randomUUID());
        } else {
            setToken(token);
        }

        Server server = new Server();

        if (!server.start()) {
            System.exit(1);
            return;
        }

        Looper.loop();

        ServerLog.i("server exit");
        System.exit(0);
    }

    public static UUID getToken() {
        return sToken;
    }

    public static void setToken(UUID token) {
        sToken = token;
    }

    private Server() {
        super();
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
        /*if (ShizukuClient.stopServer()) {
            Thread.sleep(500);
        }*/

        ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(ShizukuConfiguration.PORT, 0, ShizukuConfiguration.HOST);
        } catch (IOException e) {
            ServerLog.e("cannot start server", e);
            return false;
        }
        serverSocket.setReuseAddress(true);

        SocketThread socket = new SocketThread(this, serverSocket, sToken);

        Thread socketThread = new Thread(socket);
        socketThread.start();
        ServerLog.i("Build.VERSION.SDK_INT: " + Build.VERSION.SDK_INT);
        ServerLog.i("Build.DEVICE: " + Build.DEVICE);
        ServerLog.i("start version: " + ShizukuConfiguration.VERSION + " token: " + sToken);

        return true;
    }
}
