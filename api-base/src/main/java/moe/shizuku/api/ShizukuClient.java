package moe.shizuku.api;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Process;
import android.os.StrictMode;
import android.util.Log;

import java.net.Socket;
import java.util.UUID;

import moe.shizuku.ShizukuConstants;
import moe.shizuku.ShizukuState;
import moe.shizuku.io.ParcelInputStream;
import moe.shizuku.io.ParcelOutputStream;

/**
 * Created by rikka on 2017/9/23.
 */

public class ShizukuClient {

    private static final String TAG = "ShizukuClient";

    public static final String ACTION_GET_VERSION = "Shizuku_getVersion";
    public static final String ACTION_REQUEST_STOP = "Shizuku_requestStop";
    public static final String ACTION_AUTHORIZE = "Shizuku_authorize";
    public static final String ACTION_SEND_TOKEN = "Shizuku_sendToken";

    public static final int AUTH_RESULT_OK = Activity.RESULT_OK;
    public static final int AUTH_RESULT_USER_DENIED = Activity.RESULT_CANCELED;
    public static final int AUTH_RESULT_ERROR = 1;

    public static final int REQUEST_CODE_AUTHORIZATION = 55608;
    public static final int REQUEST_CODE_PERMISSION = 55609;

    public static final String PERMISSION = "moe.shizuku.manager.permission.API";
    public static final String PERMISSION_V23 = "moe.shizuku.manager.permission.API_V23";

    private static UUID sToken = new UUID(0, 0);

    private static TokenUpdateReceiver sTokenUpdateReceiver;

    /**
     * Disable detection of network operations for current thread.
     */
    public static void setPermitNetworkThreadPolicy() {
        StrictMode.ThreadPolicy permitNetworkPolicy = new StrictMode.ThreadPolicy.Builder(StrictMode.getThreadPolicy())
                .permitNetwork()
                .build();
        StrictMode.setThreadPolicy(permitNetworkPolicy);
    }

    /**
     * Return if the manager app is installed.
     * @param context Context.
     * @return is the manager app installed
     */
    public static boolean isManagerInstalled(Context context) {
        return getManagerVersion(context) != -1;
    }

    /**
     * Return manager app version code.
     *
     * @param context Context
     * @return version code or -1 if not installed
     */
    public static int getManagerVersion(Context context) {
        try {
            return context.getPackageManager().getApplicationInfo(ShizukuConstants.MANAGER_APPLICATION_ID, 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            return -1;
        }
    }

    public static UUID getToken() {
        return sToken;
    }

    public static void setToken(UUID token) {
        sToken = token;
    }

    public static void setToken(Intent intent) {
        long mostSig = intent.getLongExtra(ShizukuConstants.EXTRA_TOKEN_MOST_SIG, 0);
        long leastSig = intent.getLongExtra(ShizukuConstants.EXTRA_TOKEN_LEAST_SIG, 0);
        if (mostSig != 0 && leastSig != 0) {
            setToken(new UUID(mostSig, leastSig));
        }
    }

    public static boolean checkSelfPermission(Context context) {
        if (Build.VERSION.SDK_INT > 23) {
            return context.checkSelfPermission(PERMISSION_V23) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }

    /**
     * Request token from manager app.
     *
     * @param activity Activity
     */
    public static void requestAuthorization(Activity activity) {
        if (!checkSelfPermission(activity)) {
            return;
        }

        Intent intent = new Intent(ShizukuConstants.ACTION_REQUEST_AUTHORIZATION)
                .setPackage(ShizukuConstants.MANAGER_APPLICATION_ID)
                .putExtra(ShizukuConstants.EXTRA_PACKAGE_NAME, activity.getPackageName())
                .putExtra(ShizukuConstants.EXTRA_UID, Process.myUid());
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivityForResult(intent, REQUEST_CODE_AUTHORIZATION);
        }
    }

    /**
     * Register receiver to receive token update broadcast, old receiver will be unregistered.
     *
     * @param context Context
     * @param receiver Receiver
     */
    public static void registerTokenUpdateReceiver(Context context, TokenUpdateReceiver receiver) {
        unregisterTokenUpdateReceiver(context);

        sTokenUpdateReceiver = receiver;
        context.registerReceiver(sTokenUpdateReceiver,
                new IntentFilter(ShizukuConstants.MANAGER_APPLICATION_ID + ".intent.action.UPDATE_TOKEN"),
                ShizukuConstants.MANAGER_APPLICATION_ID + ".permission.RECEIVE_SERVER_STARTED",
                null);
    }

    public static void unregisterTokenUpdateReceiver(Context context) {
        if (sTokenUpdateReceiver != null) {
            context.unregisterReceiver(sTokenUpdateReceiver);
        }
        sTokenUpdateReceiver = null;
    }

    public static ShizukuState getState() {
        try {
            Socket client = new Socket(ShizukuConstants.HOST, ShizukuConstants.PORT);
            client.setSoTimeout(ShizukuConstants.TIMEOUT);
            ParcelOutputStream os = new ParcelOutputStream(client.getOutputStream());
            ParcelInputStream is = new ParcelInputStream(client.getInputStream());
            os.writeString(ACTION_GET_VERSION);
            is.readException();
            return is.readParcelable(ShizukuState.CREATOR);
        } catch (Exception e) {
            Log.w(TAG, "can't connect to server: " + e.getMessage());
        }
        return ShizukuState.createUnknown();
    }

    public static ShizukuState authorize() {
        return authorize(getToken());
    }

    public static ShizukuState authorize(UUID token) {
        try {
            Socket client = new Socket(ShizukuConstants.HOST, ShizukuConstants.PORT);
            client.setSoTimeout(ShizukuConstants.TIMEOUT);
            ParcelOutputStream os = new ParcelOutputStream(client.getOutputStream());
            ParcelInputStream is = new ParcelInputStream(client.getInputStream());
            os.writeString(ACTION_AUTHORIZE);
            os.writeLong(token.getMostSignificantBits());
            os.writeLong(token.getLeastSignificantBits());
            is.readException();
            return is.readParcelable(ShizukuState.CREATOR);
        } catch (Exception e) {
            Log.w(TAG, "can't connect to server: " + e.getMessage());
        }
        return ShizukuState.createUnknown();
    }

    public static boolean stopServer() {
        try {
            Socket socket = new Socket(ShizukuConstants.HOST, ShizukuConstants.PORT);
            socket.setSoTimeout(ShizukuConstants.TIMEOUT);
            ParcelOutputStream os = new ParcelOutputStream(socket.getOutputStream());
            ParcelInputStream is = new ParcelInputStream(socket.getInputStream());
            os.writeString(ACTION_REQUEST_STOP);
            is.readException();

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static void sendTokenToManager() {
        try {
            Socket socket = new Socket(ShizukuConstants.HOST, ShizukuConstants.PORT);
            socket.setSoTimeout(ShizukuConstants.TIMEOUT);
            ParcelOutputStream os = new ParcelOutputStream(socket.getOutputStream());
            ParcelInputStream is = new ParcelInputStream(socket.getInputStream());
            os.writeString(ACTION_SEND_TOKEN);
            os.writeInt(Process.myUid());
            is.readException();
        } catch (Exception e) {
            Log.w(TAG, "can't connect to server: " + e.getMessage());
        }
    }
}
