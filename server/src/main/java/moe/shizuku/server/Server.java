package moe.shizuku.server;

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import hidden.android.content.pm.UserInfo;
import moe.shizuku.server.io.ParcelInputStream;
import moe.shizuku.server.io.ParcelOutputStream;
import moe.shizuku.server.util.Intents;
import moe.shizuku.server.util.ServerLog;

public class Server extends Handler {

    public static void main(String[] args) throws IOException, RemoteException, InterruptedException {
        Looper.prepare();

        Server server = new Server();

        CountDownLatch socketLatch = new CountDownLatch(0x1);
        if (!server.start(socketLatch)) {
            System.exit(1);
            return;
        }

        System.out.println(String.format(Locale.ENGLISH, "Shizuku server started (version %d, %s, pid %d)",
                Protocol.VERSION,
                (HideApiOverride.isRoot(Process.myUid()) ? "root" : "shell"),
                Process.myPid()));

        Looper.loop();

        try {
            socketLatch.await();
        } catch (InterruptedException ignored) {
        }

        System.out.println(String.format(Locale.ENGLISH, "Shizuku server (pid %d) exited", Process.myPid()));
        ServerLog.i("server exit");
        System.exit(0);
    }

    public static final int MESSAGE_EXIT = 1;

    private UUID mToken;
    private RequestHandler.Impl mAPIImpl;

    private Server() {
        super();

        mToken = UUID.randomUUID();
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

    private boolean sendQuit() {
        try {
            Socket socket = new Socket(Protocol.HOST, Protocol.PORT);
            socket.setSoTimeout(100);
            ParcelOutputStream os = new ParcelOutputStream(socket.getOutputStream());
            ParcelInputStream is = new ParcelInputStream(socket.getInputStream());
            os.writeInt(-2);
            is.readException();

            ServerLog.i("send quit to old server");
            return true;
        } catch (Exception e) {
            ServerLog.i("cannot connect to old server");
            return false;
        }
    }

    public boolean start(CountDownLatch socketLatch) throws IOException, RemoteException, InterruptedException {
        if (sendQuit()) {
            Thread.sleep(100);
        }

        ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(Protocol.PORT, 0, Protocol.HOST);
        } catch (IOException e) {
            ServerLog.e("cannot start server", e);
            System.out.println(String.format(Locale.ENGLISH, "Cannot start shizuku server, %s.", e.getMessage()));
            return false;
        }
        serverSocket.setReuseAddress(true);

        SocketThread socket = new SocketThread(this, serverSocket, socketLatch, mToken);
        mAPIImpl = socket;

        Thread socketThread = new Thread(socket);
        socketThread.start();

        ServerLog.i("start version: " + Protocol.VERSION + " token: " + mToken);

        checkPermissions();
        broadcastServerStart();
        registerTaskStackListener();

        return true;
    }

    private void broadcastServerStart() throws RemoteException {
        Intent intent = new Intent(Intents.ACTION_SERVER_STARTED)
                .setPackage(Intents.PACKAGE_NAME)
                .putExtra(Intents.EXTRA_PID, Process.myUid())
                .putExtra(Intents.EXTRA_TOKEN_MOST_SIG, mToken.getMostSignificantBits())
                .putExtra(Intents.EXTRA_TOKEN_LEAST_SIG, mToken.getLeastSignificantBits());

        List<UserInfo> users = mAPIImpl.getUsers(true);
        for (UserInfo user : users) {
            mAPIImpl.broadcastIntent(intent,
                    Intents.permission("RECEIVE_SERVER_STARTED"),
                    user.id);
        }
    }

    private void checkPermissions() throws RemoteException {
        int uid = Process.myUid();
        if (HideApiOverride.isRoot(uid)) {
            return;
        }

        checkPermission(new String[]{
                "android.permission.UPDATE_APP_OPS_STATS",
                "android.permission.GET_APP_OPS_STATS"
        }, uid);
    }

    private void checkPermission(String[] permNames, int uid) throws RemoteException {
        List<String> denied = new ArrayList<>();
        for (String permName : permNames) {
            if (mAPIImpl.checkUidPermission(permName, uid) != PackageManager.PERMISSION_GRANTED) {
                denied.add(permName);
            }
        }
        if (!denied.isEmpty()) {
            System.out.print("WARNING: uid " + uid + " do not have permission");
            for (String permName : denied) {
                System.out.print(" " + permName);
            }
            System.out.println(", some APIs will not work.");
        }
    }

    private void registerTaskStackListener() {
        try {
            IActivityManager am = ActivityManagerNative.getDefault();
            if (BuildUtils.isO()) {
                //HideApiOverride.registerTaskStackListener(am);
                System.out.println("WARNING: skip registerTaskStackListener because API changed on Android O, TASK_STACK_CHANGED broadcast will not send.");
            } else {
                am.registerTaskStackListener(HideApiOverrideO.createTaskStackListener(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            List<ActivityManager.RunningTaskInfo> list = ActivityManagerNative.getDefault().getTasks(1, 0);
                            if (list != null && !list.isEmpty()) {
                                ComponentName component = list.get(0).topActivity;

                                List<UserInfo> users = mAPIImpl.getUsers(true);
                                for (UserInfo user : users) {
                                    mAPIImpl.broadcastIntent(new Intent(
                                                    Intents.ACTION_TASK_STACK_CHANGED,
                                                    Uri.parse("component://" + component.getPackageName() + "/" + component.getClassName())
                                            ),
                                            Intents.permission("RECEIVE_TASK_STACK_CHANGED"), user.id);
                                }
                            }
                        } catch (RemoteException ignored) {
                        }
                    }
                }));
            }
        } catch (Exception e) {
            ServerLog.e(e.getMessage(), e);
        }
    }
}
