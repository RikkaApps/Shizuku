package moe.shizuku.api;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Process;
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

    public static final int AUTHORIZATION_REQUEST_CODE = 55608;

    private static UUID sToken = new UUID(0, 0);

    private static TokenUpdateReceiver sTokenUpdateReceiver;

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

    public static void requestAuthorization(Activity activity) {
        Intent intent = new Intent(ShizukuConstants.ACTION_REQUEST_AUTHORIZATION)
                .setPackage(ShizukuConstants.MANAGER_APPLICATION_ID)
                .putExtra(ShizukuConstants.EXTRA_PACKAGE_NAME, activity.getPackageName())
                .putExtra(ShizukuConstants.EXTRA_UID, Process.myUid());
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivityForResult(intent, AUTHORIZATION_REQUEST_CODE);
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
