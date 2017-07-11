package moe.shizuku.server;

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;
import java.util.UUID;

import hidden.android.content.pm.UserInfo;
import moe.shizuku.server.util.Intents;
import moe.shizuku.server.util.ServerLog;

public class Server extends Handler {

    public static void main(String[] args) throws IOException, RemoteException, InterruptedException {
        Looper.prepare();

        UUID token = null;
        if (args.length > 0) {
            try {
                token = UUID.fromString(args[0]);

                ServerLog.i("using token from arg");
            } catch (IllegalArgumentException ignored) {
            }
        }

        Server server = new Server(token);

        if (!server.start()) {
            System.exit(1);
            return;
        }

        Looper.loop();

        ServerLog.i("server exit");
        System.exit(0);
    }

    public static final int MESSAGE_EXIT = 1;

    private UUID mToken;
    private RequestHandler.Impl mAPIImpl;

    private Server(UUID token) {
        super();

        if (token == null) {
            mToken = UUID.randomUUID();
        } else {
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
        ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(Protocol.PORT, 0, Protocol.HOST);
        } catch (IOException e) {
            ServerLog.e("cannot start server", e);
            return false;
        }
        serverSocket.setReuseAddress(true);

        SocketThread socket = new SocketThread(this, serverSocket, mToken);
        mAPIImpl = socket;

        Thread socketThread = new Thread(socket);
        socketThread.start();

        ServerLog.i("start version: " + Protocol.VERSION + " token: " + mToken);

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

        ServerLog.i("broadcastServerStart");

        List<UserInfo> users = mAPIImpl.getUsers(true);
        for (UserInfo user : users) {
            mAPIImpl.broadcastIntent(intent,
                    Intents.permission("RECEIVE_SERVER_STARTED"),
                    user.id);
        }
    }

    private void registerTaskStackListener() {
        try {
            IActivityManager am = ActivityManagerNative.getDefault();

            Runnable broadcastRunnable = new Runnable() {
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
            };
            if (BuildUtils.isO()) {
                //am.registerTaskStackListener(HideApiOverride.createTaskStackListener(broadcastRunnable));
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                am.registerTaskStackListener(HideApiOverrideO.createTaskStackListener(broadcastRunnable));
            }
        } catch (Exception e) {
            ServerLog.e(e.getMessage(), e);
        }
    }
}
