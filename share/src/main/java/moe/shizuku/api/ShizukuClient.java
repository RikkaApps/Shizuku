package moe.shizuku.api;

import android.app.Activity;
import android.content.Intent;
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

    public static final int ACTION_GET_VERSION = 1;
    public static final int ACTION_REQUEST_STOP = 2;
    public static final int ACTION_AUTHORIZE = 3;
    public static final int ACTION_SEND_TOKEN = 4;

    public static final int AUTHORIZATION_REQUEST_CODE = 55608;

    private static UUID sToken = new UUID(0, 0);

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

    public static ShizukuState getState() {
        try {
            Socket client = new Socket(ShizukuConstants.HOST, ShizukuConstants.PORT);
            client.setSoTimeout(ShizukuConstants.TIMEOUT);
            ParcelOutputStream os = new ParcelOutputStream(client.getOutputStream());
            ParcelInputStream is = new ParcelInputStream(client.getInputStream());
            os.writeInt(ACTION_GET_VERSION);
            is.readException();
            return is.readParcelable(ShizukuState.CREATOR);
        } catch (Exception e) {
            Log.w(TAG, "can't connect to server", e);
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
            os.writeInt(ACTION_AUTHORIZE);
            os.writeLong(token.getMostSignificantBits());
            os.writeLong(token.getLeastSignificantBits());
            is.readException();
            return is.readParcelable(ShizukuState.CREATOR);
        } catch (Exception e) {
            Log.w(TAG, "can't connect to server", e);
        }
        return ShizukuState.createUnknown();
    }

    public static boolean stopServer() {
        try {
            Socket socket = new Socket(ShizukuConstants.HOST, ShizukuConstants.PORT);
            socket.setSoTimeout(ShizukuConstants.TIMEOUT);
            ParcelOutputStream os = new ParcelOutputStream(socket.getOutputStream());
            ParcelInputStream is = new ParcelInputStream(socket.getInputStream());
            os.writeInt(ACTION_REQUEST_STOP);
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
            os.writeInt(ACTION_SEND_TOKEN);
            os.writeInt(Process.myUid());
            is.readException();
        } catch (Exception e) {
            Log.w(TAG, "can't connect to server", e);
        }
    }
}
