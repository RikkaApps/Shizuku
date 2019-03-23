package moe.shizuku.api;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.Build;

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

public class ShizukuClientHelper {

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    public interface OnBinderReceivedListener {
        void onBinderReceived();
    }

    private static OnBinderReceivedListener sBinderReceivedListener;

    private static UUID sToken = new UUID(0, 0);

    public static OnBinderReceivedListener getBinderReceivedListener() {
        return sBinderReceivedListener;
    }

    public static void setBinderReceivedListener(OnBinderReceivedListener binderReceivedListener) {
        sBinderReceivedListener = binderReceivedListener;
    }

    public static boolean isManagerV3Installed(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(ShizukuApiConstants.MANAGER_APPLICATION_ID, 0).versionCode >= 183;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static boolean isManagerV2Installed(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(ShizukuApiConstants.MANAGER_APPLICATION_ID, 0).versionCode >= 106;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static int getManagerVersionCode(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(ShizukuApiConstants.MANAGER_APPLICATION_ID, 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            return -1;
        }
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

    public static int isServerRunning() throws IOException {
        try (LocalSocket socket = new LocalSocket(LocalSocket.SOCKET_STREAM)) {
            socket.connect(new LocalSocketAddress(ShizukuApiConstants.SOCKET_NAME, LocalSocketAddress.Namespace.ABSTRACT));
            socket.setSoTimeout(ShizukuApiConstants.SOCKET_TIMEOUT);
            DataOutputStream os = new DataOutputStream(socket.getOutputStream());
            DataInputStream is = new DataInputStream(socket.getInputStream());
            os.writeInt(ShizukuApiConstants.SOCKET_VERSION_CODE);
            os.writeInt(ShizukuApiConstants.SOCKET_ACTION_PING);
            return is.readInt();
        }
    }

    public static int requestBinderNoThrow(final Context context) {
        Callable<Integer> task = new Callable<Integer>() {
            public Integer call() throws IOException {
                return requestBinder(context);
            }
        };

        Future<Integer> future = EXECUTOR.submit(task);
        try {
            return future.get(ShizukuApiConstants.SOCKET_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            // handle the timeout
        } catch (Throwable tr) {
            // handle other error
        } finally {
            future.cancel(true);
        }
        return ShizukuApiConstants.SOCKET_EXCEPTION;
    }

    public static boolean isServerRunningNoThrow() {
        Callable<Integer> task = new Callable<Integer>() {
            public Integer call() throws IOException {
                return isServerRunning();
            }
        };

        Future<Integer> future = EXECUTOR.submit(task);
        try {
            return future.get(ShizukuApiConstants.SOCKET_TIMEOUT, TimeUnit.MILLISECONDS) == ShizukuApiConstants.SOCKET_OK;
        } catch (TimeoutException e) {
            // handle the timeout
        } catch (Throwable tr) {
            // handle other error
        } finally {
            future.cancel(true);
        }
        return false;
    }

    public static boolean isPreM() {
        return Build.VERSION.SDK_INT < 23;
    }

    public static void setPre23Token(Intent intent, Context context) {
        if (!isPreM())
            throw new IllegalStateException("token is not required from API 23");

        long mostSig = intent.getLongExtra(ShizukuApiConstants.EXTRA_TOKEN_MOST_SIG, 0);
        long leastSig = intent.getLongExtra(ShizukuApiConstants.EXTRA_TOKEN_LEAST_SIG, 0);
        if (mostSig != 0 && leastSig != 0) {
            setPre23Token(new UUID(mostSig, leastSig), context);
        }
    }

    private static void setPre23Token(UUID token, Context context) {
        if (!isPreM())
            throw new IllegalStateException("token is not required from API 23");

        sToken = token;
        savePre23Token(context, token);
    }

    private static final String KEY_TOKEN_MOST_SIG = "moe.shizuku.privilege.api.token_most";
    private static final String KEY_TOKEN_LEAST_SIG = "moe.shizuku.privilege.api.token_least";

    public static void loadPre23Token(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("moe.shizuku.privilege.api.token", Context.MODE_PRIVATE);
        long mostSig = preferences.getLong(KEY_TOKEN_MOST_SIG, 0);
        long leastSig = preferences.getLong(KEY_TOKEN_LEAST_SIG, 0);
        sToken = new UUID(mostSig, leastSig);
    }

    private static void savePre23Token(Context context, UUID token) {
        SharedPreferences preferences = context.getSharedPreferences("moe.shizuku.privilege.api.token", Context.MODE_PRIVATE);
        long mostSig = token.getLeastSignificantBits();
        long leastSig = token.getLeastSignificantBits();
        if (mostSig != 0 && leastSig != 0) {
            preferences.edit()
                    .putLong(KEY_TOKEN_MOST_SIG, mostSig)
                    .putLong(KEY_TOKEN_LEAST_SIG, leastSig)
                    .apply();
        }
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
