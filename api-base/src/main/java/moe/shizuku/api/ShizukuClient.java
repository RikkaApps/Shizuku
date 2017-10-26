package moe.shizuku.api;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
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

    private static final String ACTION_AUTHORIZE = "Shizuku_authorize";

    private static final String KEY_TOKEN_MOST_SIG = "moe.shizuku.privilege.api.token_most";
    private static final String KEY_TOKEN_LEAST_SIG = "moe.shizuku.privilege.api.token_least";

    /**
     * Activity result: ok, token is returned.
     */
    public static final int AUTH_RESULT_OK = Activity.RESULT_OK;

    /**
     * Activity result: user denied request (only API pre-23).
     */
    public static final int AUTH_RESULT_USER_DENIED = Activity.RESULT_CANCELED;

    /**
     * Activity result: error, such as manager app itself not authorized.
     */
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

    /**
     * Get stored token.
     *
     * @see #setToken(UUID)
     * @see #setToken(Intent)
     *
     * @return token
     */
    public static UUID getToken() {
        return sToken;
    }

    /**
     * Set token.
     *
     * @param token token
     */
    public static void setToken(UUID token) {
        sToken = token;
    }

    /**
     * Load token form SharedPreferences.
     *
     * @see #saveToken(SharedPreferences)
     *
     * @param preferences SharedPreferences
     */
    public static void loadToken(SharedPreferences preferences) {
        long mostSig = preferences.getLong(KEY_TOKEN_MOST_SIG, 0);
        long leastSig = preferences.getLong(KEY_TOKEN_LEAST_SIG, 0);
        if (mostSig != 0 && leastSig != 0) {
            setToken(new UUID(mostSig, leastSig));
        }
    }

    /**
     * Save token to SharedPreferences.
     *
     * @see #loadToken(SharedPreferences)
     *
     * @param preferences SharedPreferences
     */
    public static void saveToken(SharedPreferences preferences) {
        long mostSig = getToken().getLeastSignificantBits();
        long leastSig = getToken().getLeastSignificantBits();
        if (mostSig != 0 && leastSig != 0) {
            preferences.edit()
                    .putLong(KEY_TOKEN_MOST_SIG, mostSig)
                    .putLong(KEY_TOKEN_MOST_SIG, leastSig)
                    .apply();
        }
    }

    /**
     * Set token from Intent passed by {@link Activity#onActivityResult(int, int, Intent)}
     * after call {@link #requestAuthorization(Activity)}.
     *
     * @param intent Intent
     */
    public static void setToken(Intent intent) {
        long mostSig = intent.getLongExtra(ShizukuConstants.EXTRA_TOKEN_MOST_SIG, 0);
        long leastSig = intent.getLongExtra(ShizukuConstants.EXTRA_TOKEN_LEAST_SIG, 0);
        if (mostSig != 0 && leastSig != 0) {
            setToken(new UUID(mostSig, leastSig));
        }
    }

    /**
     * Return if this current app have permission to start manager app's token request activity.
     * <p>
     * On API 23+, Shizuku Manager use Android's runtime permission, if false is returned, you
     * should use {@link Activity#requestPermissions(String[], int)} to request permission.<br>
     * On API pre-23, Shizuku Manager use its own permission control, permission granted by
     * Android by default, so true is always returned.
     *
     * @param context Context
     * @return if current app have permission to get (or request) token
     */
    public static boolean checkSelfPermission(Context context) {
        if (Build.VERSION.SDK_INT > 23) {
            return context.checkSelfPermission(PERMISSION_V23) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }

    /**
     * Request token from manager app.
     * <p>
     * The result will be passed by {@link Activity#onActivityResult(int, int, Intent)}.
     * <p>
     * On API 23+, Shizuku Manager use Android's runtime permission, you should use request
     * permission by yourself first.
     *
     * @see #REQUEST_CODE_AUTHORIZATION
     * @see #AUTH_RESULT_OK
     * @see #AUTH_RESULT_USER_DENIED
     * @see #AUTH_RESULT_ERROR
     *
     * @see #checkSelfPermission(Context)
     *
     * @param activity Activity
     */
    public static void requestAuthorization(Activity activity) {
        if (!checkSelfPermission(activity)) {
            return;
        }

        Intent intent = new Intent(ShizukuConstants.ACTION_REQUEST_AUTHORIZATION)
                .setPackage(ShizukuConstants.MANAGER_APPLICATION_ID);
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivityForResult(intent, REQUEST_CODE_AUTHORIZATION);
        }
    }

    /**
     * Register receiver to receive token update broadcast, old receiver will be unregistered automatically
     *
     * @see TokenUpdateReceiver
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

    /**
     * Unregister receiver set previously. Nothing will happen if no receiver is registered.
     *
     * @param context Context
     */
    public static void unregisterTokenUpdateReceiver(Context context) {
        if (sTokenUpdateReceiver != null) {
            context.unregisterReceiver(sTokenUpdateReceiver);
        }
        sTokenUpdateReceiver = null;
    }

    /**
     * Return a {@link ShizukuState} instance that describes server status and if the client is
     * authorized.
     *
     * @see ShizukuState#isAuthorized()
     * @see ShizukuState#isRoot()
     * @see ShizukuState#isServerAvailable()
     *
     * @return status
     */
    public static ShizukuState getState() {
        return getState(getToken());
    }

    private static ShizukuState getState(UUID token) {
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
}
