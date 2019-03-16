package moe.shizuku.server;

import android.os.Binder;
import android.os.Handler;
import android.os.Process;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static moe.shizuku.server.utils.Logger.LOGGER;

public class SocketThread implements Runnable {

    private final ExecutorService mThreadPool = Executors.newFixedThreadPool(3);

    private final ServerSocket mServerSocket;

    SocketThread(ServerSocket serverSocket, UUID token, Binder binder) {
        mServerSocket = serverSocket;
    }

    private static class HandlerRunnable implements Runnable {

        private final Socket mSocket;

        public HandlerRunnable(Socket socket) {
            mSocket = socket;
        }

        @Override
        public void run() {
            try {
                //mRequestHandler.handle(mSocket);
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

    @Override
    public void run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        for (; ; ) {
            try {
                Socket socket = mServerSocket.accept();
                socket.setSoTimeout(30000);
                //mThreadPool.execute(new HandlerRunnable(socket, mRequestHandler));
            } catch (IOException e) {
                if (SocketException.class.equals(e.getClass()) && "Socket closed".equals(e.getMessage())) {
                    LOGGER.i("server socket is closed");
                    break;
                }
                LOGGER.w("cannot accept", e);
            }
        }
        //mHandler.sendEmptyMessage(ShizukuServer.MESSAGE_EXIT);
        try {
            mServerSocket.close();
        } catch (IOException ignored) {
        }
    }
}
