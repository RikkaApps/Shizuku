package moe.shizuku.api;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.Build;
import android.os.IBinder;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import moe.shizuku.server.IShizukuService;

public class ShizukuManager {

    public interface OnBinderReceivedListener {
        void onBinderReceived();
    }

    private static OnBinderReceivedListener sBinderReceivedListener;

    private static IShizukuService sRemote;

    public static OnBinderReceivedListener getBinderReceivedListener() {
        return sBinderReceivedListener;
    }

    public static void setBinderReceivedListener(OnBinderReceivedListener binderReceivedListener) {
        sBinderReceivedListener = binderReceivedListener;
    }

    public static void setRemote(IShizukuService remote) {
        sRemote = remote;
    }

    public static void setRemote(IBinder remote) {
        sRemote = IShizukuService.Stub.asInterface(remote);
    }

    public static IShizukuService get() {
        return sRemote;
    }

    public static IShizukuService getThrow() {
        if (sRemote == null)
            throw new NullPointerException();
        return sRemote;
    }

    public static IBinder getBinder() {
        return sRemote != null ? sRemote.asBinder() : null;
    }

    public static IBinder getBinderThrow() {
        return getThrow().asBinder();
    }

    public static void requestBinder(Context context) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Callable<Integer> task = new Callable<Integer>() {
            public Integer call() {
                try (LocalSocket socket = new LocalSocket(LocalSocket.SOCKET_STREAM)) {
                    socket.connect(new LocalSocketAddress(ShizukuApiConstants.SOCKET_NAME, LocalSocketAddress.Namespace.ABSTRACT));
                    socket.setSoTimeout(ShizukuApiConstants.SOCKET_TIMEOUT);
                    DataOutputStream os = new DataOutputStream(socket.getOutputStream());
                    DataInputStream is = new DataInputStream(socket.getInputStream());
                    os.writeInt(ShizukuApiConstants.SOCKET_VERSION_CODE);
                    os.writeInt(ShizukuApiConstants.SOCKET_ACTION_REQUEST_BINDER);
                    os.writeUTF(context.getPackageName());
                    if (Build.VERSION.SDK_INT < 23) {
                        os.writeUTF("TODO token");
                    }
                    return is.readInt();
                } catch (Throwable tr) {

                }
                return null;
            }
        };

        Future<Integer> future = executor.submit(task);
        try {
            future.get();
        } catch (InterruptedException e) {
            // handle the interrupts
        } catch (ExecutionException e) {
            // handle other exceptions
        } finally {
            future.cancel(true);
        }
    }

    public static boolean isV3(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(ShizukuApiConstants.MANAGER_APPLICATION_ID, 0).versionCode >= 179;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static boolean checkSelfPermission(Context context) {
        if (Build.VERSION.SDK_INT > 23) {
            return context.checkSelfPermission(ShizukuApiConstants.PERMISSION_V23) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }
}
