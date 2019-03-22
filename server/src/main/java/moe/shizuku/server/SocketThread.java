package moe.shizuku.server;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Credentials;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.os.Binder;
import android.os.Build;
import android.os.Process;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.SocketException;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import moe.shizuku.api.ShizukuApiConstants;
import moe.shizuku.server.api.Api;
import moe.shizuku.server.utils.BuildUtils;

import static moe.shizuku.server.utils.Logger.LOGGER;

public class SocketThread implements Runnable {

    private final ExecutorService mThreadPool = Executors.newFixedThreadPool(3);

    private final LocalServerSocket mServerSocket;
    private final UUID mToken;
    private final Binder mBinder;

    SocketThread(LocalServerSocket serverSocket, UUID token, Binder binder) {
        mServerSocket = serverSocket;
        mToken = token;
        mBinder = binder;
    }

    private class HandlerRunnable implements Runnable {

        private final LocalSocket mSocket;

        HandlerRunnable(LocalSocket socket) {
            mSocket = socket;
        }

        @Override
        public void run() {
            try {
                DataInputStream is = new DataInputStream(mSocket.getInputStream());
                DataOutputStream os = new DataOutputStream(mSocket.getOutputStream());
                Credentials cred = mSocket.getPeerCredentials();

                handle(cred, is, os);
            } catch (IOException e) {
                LOGGER.w("io error", e);
            } catch (Exception e) {
                LOGGER.w("error", e);
            } finally {
                try {
                    mSocket.close();
                } catch (IOException e) {
                    LOGGER.w("cannot close", e);
                }
            }
        }
    }

    private void handle(Credentials cred, DataInputStream is, DataOutputStream os) throws IOException {
        int version = is.readInt();
        if (version > ShizukuApiConstants.SOCKET_VERSION_CODE)
            return;

        int uid = cred.getUid();
        int gid = cred.getGid();
        int pid = cred.getPid();
        int action = is.readInt();
        switch (action) {
            case ServerConstants.SOCKET_ACTION_MANAGER_REQUEST_BINDER: {
                requestBinderFromManagerApp(uid, os);
                break;
            }
            case ShizukuApiConstants.SOCKET_ACTION_REQUEST_BINDER: {
                requestBinderFromUserApp(uid, pid, is, os);
                break;
            }
        }
        LOGGER.d("handle request: version=%d, uid=%d, action=%d", version, uid, action);
    }

    private void requestBinderFromManagerApp(int uid, DataOutputStream os) throws IOException {
        PackageInfo pi = null;
        try {
            pi = Api.getPackageInfo(ShizukuApiConstants.MANAGER_APPLICATION_ID, 0, uid / 100000);
        } catch (Throwable tr) {
            LOGGER.w(tr, "getPackageInfo");
        }

        if (pi == null || pi.applicationInfo.uid != uid) {
            // not from manager app
            os.writeInt(-1);
        } else {
            os.writeInt(0);
            ShizukuService.sendTokenToManger(mToken, mBinder, uid / 100000);
        }
    }

    private void requestBinderFromUserApp(int uid, int pid, DataInputStream is, DataOutputStream os) throws IOException {
        String packageName = is.readUTF();
        String token;
        if (BuildUtils.isPreM()) {
            token = is.readUTF();
        }

        boolean granted = false;

        if (!BuildUtils.isPreM()) {
            try {
                granted = Api.checkPermission(ShizukuApiConstants.PERMISSION_V23, pid, uid) == PackageManager.PERMISSION_GRANTED;
            } catch (Throwable tr) {
                LOGGER.w(tr, "checkPermission");
            }

            if (!granted) {
                // no permission
                os.writeInt(-1);
            } else {
                os.writeInt(0);
                ShizukuService.sendTokenToUserApp(mBinder, packageName, uid / 100000);
            }
        }
    }


    @Override
    public void run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        for (; ; ) {
            try {
                LocalSocket socket = mServerSocket.accept();
                mThreadPool.execute(new HandlerRunnable(socket));
            } catch (IOException e) {
                if (SocketException.class.equals(e.getClass()) && "Socket closed".equals(e.getMessage())) {
                    LOGGER.i("server socket is closed");
                    break;
                }
                LOGGER.w("cannot accept", e);
            }
        }

        try {
            mServerSocket.close();
        } catch (IOException ignored) {
        }
    }
}
