package moe.shizuku.server;

import android.os.Binder;
import android.os.Handler;
import android.os.Process;
import android.os.RemoteException;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import moe.shizuku.server.api.ShizukuRequestHandler;
import moe.shizuku.server.util.ServerLog;

/**
 * Created by rikka on 2017/9/23.
 */

public class SocketThread implements Runnable {

    private final ExecutorService mThreadPool = Executors.newFixedThreadPool(3);

    private final Handler mHandler;
    private final ServerSocket mServerSocket;

    private final ShizukuRequestHandler mRequestHandler;

    SocketThread(Handler handler, ServerSocket serverSocket, UUID token, Binder binder) {
        mHandler = handler;
        mServerSocket = serverSocket;
        mRequestHandler = new ShizukuRequestHandler(mHandler, token, binder);
    }

    private static class HandlerRunnable implements Runnable {

        private final Socket mSocket;
        private final ShizukuRequestHandler mRequestHandler;

        public HandlerRunnable(Socket socket, ShizukuRequestHandler requestHandler) {
            mSocket = socket;
            mRequestHandler = requestHandler;
        }

        @Override
        public void run() {
            try {
                mRequestHandler.handle(mSocket);
            } catch (RemoteException e) {
                ServerLog.w("remote error", e);
            } catch (Exception e) {
                ServerLog.w("error", e);
            } finally {
                try {
                    mSocket.close();
                } catch (IOException e) {
                    ServerLog.w("cannot close", e);
                }
            }
        }
    }

    @Override
    public void run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        for (; ; ) {
            try {
                Socket socket = mServerSocket.accept();
                socket.setSoTimeout(30000);
                mThreadPool.execute(new HandlerRunnable(socket, mRequestHandler));
            } catch (IOException e) {
                if (SocketException.class.equals(e.getClass()) && "Socket closed".equals(e.getMessage())) {
                    ServerLog.i("server socket is closed");
                    break;
                }
                ServerLog.w("cannot accept", e);
            }
        }
        mHandler.sendEmptyMessage(ShizukuServer.MESSAGE_EXIT);
        try {
            mServerSocket.close();
        } catch (IOException ignored) {
        }
    }
}
