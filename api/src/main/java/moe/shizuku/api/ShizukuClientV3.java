package moe.shizuku.api;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.Build;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import moe.shizuku.server.IShizukuService;

public class ShizukuClientV3 {

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    public interface OnBinderReceivedListener {
        void onBinderReceived();
    }

    private static OnBinderReceivedListener sBinderReceivedListener;

    private static IShizukuService sRemote;

    private static UUID sToken = new UUID(0, 0);

    public static OnBinderReceivedListener getBinderReceivedListener() {
        return sBinderReceivedListener;
    }

    public static void setBinderReceivedListener(OnBinderReceivedListener binderReceivedListener) {
        sBinderReceivedListener = binderReceivedListener;
    }

    public static void setRemoteBinder(IBinder remote) {
        sRemote = IShizukuService.Stub.asInterface(remote);
    }

    public static IBinder getRemoteBinder() {
        return sRemote != null ? sRemote.asBinder() : null;
    }

    public static boolean isRemoteAlive() {
        if (sRemote == null)
            return false;

        return sRemote.asBinder().pingBinder();
    }

    public static boolean transactRemote(Parcel data, Parcel reply, int flags) throws RemoteException {
        return sRemote.asBinder().transact(ShizukuApiConstants.BINDER_TRANSACTION_transact, data, reply, flags);
    }

    public static RemoteProcess newRemoteProcess(String[] cmd, String[] env, String dir) throws RemoteException {
        return new RemoteProcess(sRemote.newProcess(cmd, env, dir));
    }

    public static int getRemoteUid() throws RemoteException {
        return sRemote.getUid();
    }

    public static int getRemoteVersion() throws RemoteException {
        return sRemote.getVersion();
    }

    public static int checkRemotePermission(String permission) throws RemoteException {
        return sRemote.checkPermission(permission);
    }

    public static int getLatestVersion() {
        return ShizukuApiConstants.SERVER_VERSION;
    }

    public static int requestBinder(Context context) throws IOException {
        try (LocalSocket socket = new LocalSocket(LocalSocket.SOCKET_STREAM)) {
            socket.connect(new LocalSocketAddress(ShizukuApiConstants.SOCKET_NAME, LocalSocketAddress.Namespace.ABSTRACT));
            socket.setSoTimeout(ShizukuApiConstants.SOCKET_TIMEOUT);
            DataOutputStream os = new DataOutputStream(socket.getOutputStream());
            DataInputStream is = new DataInputStream(socket.getInputStream());
            os.writeInt(ShizukuApiConstants.SOCKET_VERSION_CODE);
            os.writeInt(ShizukuApiConstants.SOCKET_ACTION_REQUEST_BINDER);
            os.writeUTF(context.getPackageName());
            if (isPreM()) {
                os.writeUTF(sToken.toString());
            }
            return is.readInt();
        }
    }

    public static boolean requestBinderSync(Context context, long timeout) {
        Callable<Integer> task = new Callable<Integer>() {
            public Integer call() throws IOException {
                return requestBinder(context);
            }
        };

        Future<Integer> future = EXECUTOR.submit(task);
        try {
            return future.get(timeout, TimeUnit.MILLISECONDS) == 0;
        } catch (TimeoutException e) {
            // handle the timeout
        } catch (Throwable tr) {
            // handle other error
        } finally {
            future.cancel(true);
        }
        return false;
    }

    public static boolean isManagerV3Installed(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(ShizukuApiConstants.MANAGER_APPLICATION_ID, 0).versionCode >= 179;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static boolean isPreM() {
        return Build.VERSION.SDK_INT < 23;
    }

    public static void setPre23Token(Intent intent) {
        if (!isPreM())
            throw new IllegalStateException("token is not required from API 23");

        long mostSig = intent.getLongExtra(ShizukuApiConstants.EXTRA_TOKEN_MOST_SIG, 0);
        long leastSig = intent.getLongExtra(ShizukuApiConstants.EXTRA_TOKEN_LEAST_SIG, 0);
        if (mostSig != 0 && leastSig != 0) {
            setPre23Token(new UUID(mostSig, leastSig));
        }
    }

    public static void setPre23Token(UUID token) {
        if (!isPreM())
            throw new IllegalStateException("token is not required from API 23");

        sToken = token;
    }

    public static Intent createPre23AuthorizationIntent(Context context) {
        if (!isPreM())
            throw new IllegalStateException("authorization intent is not required from API 23");

        Intent intent = new Intent(ShizukuApiConstants.ACTION_REQUEST_AUTHORIZATION)
                .setPackage(ShizukuApiConstants.MANAGER_APPLICATION_ID);
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            return intent;
        }
        return null;
    }
}
