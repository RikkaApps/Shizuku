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
import android.os.Process;
import android.os.RemoteException;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import hidden.android.content.pm.UserInfo;
import moe.shizuku.server.util.Intents;
import moe.shizuku.server.util.ServerLog;

public class Server extends Handler {

    private RequestHandler.Impl mAPIImpl;

    public static void main(String[] args) throws IOException, RemoteException {
        Looper.prepare();
        final Server server = new Server();

        UUID token = UUID.randomUUID();

        ServerLog.i("start version: " + Protocol.VERSION + " token: " + token);

        CountDownLatch socketLatch = new CountDownLatch(0x1);
        ServerSocket serverSocket = new ServerSocket(Protocol.PORT, 0, Protocol.HOST);
        serverSocket.setReuseAddress(true);

        SocketThread socket = new SocketThread(server, serverSocket, socketLatch, token);
        server.mAPIImpl = socket;

        Thread socketThread = new Thread(socket);
        socketThread.start();

        System.out.println("Shizuku server started (version "
                + Protocol.VERSION + ", "
                + (HideApiOverride.isRoot(Process.myUid()) ? "root" : "shell")
                + ")");

        server.checkPermission(new String[]{
                "android.permission.UPDATE_APP_OPS_STATS",
                "android.permission.GET_APP_OPS_STATS"
        }, Process.myUid());

        Intent intent = new Intent(Intents.action("SERVER_STARTED"))
                .setPackage(Intents.PACKAGE_NAME)
                .putExtra(Intents.extra("TOKEN_MOST_SIG"), token.getMostSignificantBits())
                .putExtra(Intents.extra("TOKEN_LEAST_SIG"), token.getLeastSignificantBits());

        List<UserInfo> users = server.mAPIImpl.getUsers(true);
        for (UserInfo user : users) {
            server.mAPIImpl.broadcastIntent(intent,
                    Intents.permission("RECEIVE_SERVER_STARTED"),
                    user.id);
        }

        /*intent = new Intent(PACKAGE_NAME + ".intent.action.CLEAR_TOKEN");

        for (UserInfo user : users) {
            APIs.broadcastIntent(intent,
                    PACKAGE_NAME + ".permission.RECEIVE_CLEAR_TOKEN",
                    user.id);
        }*/

        server.registerTaskStackListener();

        Looper.loop();
    }

    private void checkPermission(String[] permNames, int uid) throws RemoteException {
        if (HideApiOverride.isRoot(uid)) {
            return;
        }

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
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N_MR1
                    && Build.VERSION.PREVIEW_SDK_INT > 0) {
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
                                                    Intents.action("TASK_STACK_CHANGED"),
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
