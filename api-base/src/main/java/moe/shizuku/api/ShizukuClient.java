package moe.shizuku.api;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
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

    public static class TokenUpdatedReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (!ShizukuConstants.ACTION_UPDATE_TOKEN.equals(intent.getAction())) {
                return;
            }

            ShizukuClient.setToken(intent);
        }
    }

    private static TokenUpdatedReceiver sTokenUpdateReceiver;

    public interface TokenUpdatedListener {

        /**
         * Save token here.
         *
         * @param token Token
         */
        void onTokenUpdated(UUID token);
    }

    private static TokenUpdatedListener sTokenUpdatedListener;

    @SuppressLint("StaticFieldLeak")
    private static Context sContext;

    private static SharedPreferences sPreferences;

    /**
     * Initialize ShizukuClient with built-in token storage logic.
     * <p>Only need call once in Application init.
     *
     * @param context Context
     */
    public static void initialize(Context context) {
        if (getManagerVersion(context) <= 106) {
            return;
        }

        sContext = context;
        sPreferences = context.getSharedPreferences("moe.shizuku.privilege.api.token", Context.MODE_PRIVATE);
        sToken = loadToken(sPreferences);
        sTokenUpdatedListener = new TokenUpdatedListener() {
            @Override
            public void onTokenUpdated(UUID token) {
                saveToken(sPreferences);
            }
        };

        requestToken(context);
        registerTokenUpdateReceiver(context, new TokenUpdatedReceiver());
    }

    public static void setContext(Context context) {
        sContext = context;
    }

    public static Context getContext() {
        return sContext;
    }

    public static void setTokenUpdatedListener(TokenUpdatedListener tokenUpdatedListener) {
        sTokenUpdatedListener = tokenUpdatedListener;
    }

    /**
     * Disable detection of network operations for current thread.
     * <p>
     * Highly not recommended. According to user report, on some Samsung devices, use socket in
     * main thread when battery saver is on will cause ANR.
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
            return context.getPackageManager().getPackageInfo(ShizukuConstants.MANAGER_APPLICATION_ID, 0).versionCode;
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

        if (sTokenUpdatedListener != null) {
            sTokenUpdatedListener.onTokenUpdated(token);
        }
    }

    /**
     * Load token form SharedPreferences.
     *
     * @see #saveToken(SharedPreferences)
     *
     * @param preferences SharedPreferences
     */
    private static UUID loadToken(SharedPreferences preferences) {
        long mostSig = preferences.getLong(KEY_TOKEN_MOST_SIG, 0);
        long leastSig = preferences.getLong(KEY_TOKEN_LEAST_SIG, 0);
        return new UUID(mostSig, leastSig);
    }

    /**
     * Save token to SharedPreferences.
     *
     * @see #loadToken(SharedPreferences)
     *
     * @param preferences SharedPreferences
     */
    private static void saveToken(SharedPreferences preferences) {
        long mostSig = getToken().getLeastSignificantBits();
        long leastSig = getToken().getLeastSignificantBits();
        if (mostSig != 0 && leastSig != 0) {
            preferences.edit()
                    .putLong(KEY_TOKEN_MOST_SIG, mostSig)
                    .putLong(KEY_TOKEN_LEAST_SIG, leastSig)
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
     * Set token from Bundle passed by {@link #requestToken(Context)}.
     *
     * @param bundle bundle
     */
    private static void setToken(Bundle bundle) {
        long mostSig = bundle.getLong(ShizukuConstants.EXTRA_TOKEN_MOST_SIG, 0);
        long leastSig = bundle.getLong(ShizukuConstants.EXTRA_TOKEN_LEAST_SIG, 0);
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
     * Request permission on API 23+.
     *
     * @param activity Activity
     */
    public static void requestPermission(Activity activity) {
        if (Build.VERSION.SDK_INT > 23) {
            activity.requestPermissions(new String[]{PERMISSION_V23}, REQUEST_CODE_PERMISSION);
        }
    }

    /**
     * Request permission on API 23+.
     *
     * @param fragment Fragment
     */
    public static void requestPermission(android.app.Fragment fragment) {
        if (Build.VERSION.SDK_INT > 23) {
            fragment.requestPermissions(new String[]{PERMISSION_V23}, REQUEST_CODE_PERMISSION);
        }
    }

    /**
     * Request permission on API 23+.
     *
     * @param fragment Fragment
     */
    public static void requestPermission(android.support.v4.app.Fragment fragment) {
        if (Build.VERSION.SDK_INT > 23) {
            fragment.requestPermissions(new String[]{PERMISSION_V23}, REQUEST_CODE_PERMISSION);
        }
    }

    /**
     * Request permission on API 23+.
     *
     * @param fragment Fragment
     */
    public static void requestPermission(androidx.fragment.app.Fragment fragment) {
        if (Build.VERSION.SDK_INT > 23) {
            fragment.requestPermissions(new String[]{PERMISSION_V23}, REQUEST_CODE_PERMISSION);
        }
    }

    /**
     * Request token from manager app if user have already granted permission.
     * <p>
     * On API 23+, Shizuku Manager use Android's runtime permission, you should request permission
     * by yourself first.
     *
     * @param context Context
     * @return true if token returned
     */
    public static boolean requestToken(Context context) {
        if (!checkSelfPermission(context)) {
            return false;
        }

        try {
            Bundle bundle = context.getContentResolver().call(
                    ShizukuConstants.TOKEN_PROVIDER_URI, "request", null, null);
            if (bundle != null) {
                setToken(bundle);
                return true;
            }
            return false;
        } catch (Exception e) {
            Log.w(TAG, "can't request token use ContentProvider", e);
        }
        return false;
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
            try {
                activity.startActivityForResult(intent, REQUEST_CODE_AUTHORIZATION);
            } catch (Exception e) {
                Log.w(TAG, "can't startActivityForResult", e);
            }
        }
    }

    /**
     * Request token from manager app.
     * <p>
     * The result will be passed by {@link android.app.Fragment#onActivityResult(int, int, Intent)}.
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
     * @param fragment Fragment
     */
    public static void requestAuthorization(android.app.Fragment fragment) {
        if (!checkSelfPermission(fragment.getActivity())) {
            return;
        }

        Intent intent = new Intent(ShizukuConstants.ACTION_REQUEST_AUTHORIZATION)
                .setPackage(ShizukuConstants.MANAGER_APPLICATION_ID);
        if (intent.resolveActivity(fragment.getActivity().getPackageManager()) != null) {
            try {
                fragment.startActivityForResult(intent, REQUEST_CODE_AUTHORIZATION);
            } catch (Exception e) {
                Log.w(TAG, "can't startActivityForResult", e);
            }
        }
    }

    /**
     * Request token from manager app.
     * <p>
     * The result will be passed by {@link android.support.v4.app.Fragment#onActivityResult(int, int, Intent)}.
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
     * @param fragment Fragment
     */
    public static void requestAuthorization(android.support.v4.app.Fragment fragment) {
        if (!checkSelfPermission(fragment.getActivity())) {
            return;
        }

        Intent intent = new Intent(ShizukuConstants.ACTION_REQUEST_AUTHORIZATION)
                .setPackage(ShizukuConstants.MANAGER_APPLICATION_ID);
        if (intent.resolveActivity(fragment.getActivity().getPackageManager()) != null) {
            try {
                fragment.startActivityForResult(intent, REQUEST_CODE_AUTHORIZATION);
            } catch (Exception e) {
                Log.w(TAG, "can't startActivityForResult", e);
            }
        }
    }

    /**
     * Request token from manager app.
     * <p>
     * The result will be passed by {@link androidx.fragment.app.Fragment#onActivityResult(int, int, Intent)}.
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
     * @param fragment Fragment
     */
    public static void requestAuthorization(androidx.fragment.app.Fragment fragment) {
        if (!checkSelfPermission(fragment.getActivity())) {
            return;
        }

        Intent intent = new Intent(ShizukuConstants.ACTION_REQUEST_AUTHORIZATION)
                .setPackage(ShizukuConstants.MANAGER_APPLICATION_ID);
        if (intent.resolveActivity(fragment.getActivity().getPackageManager()) != null) {
            try {
                fragment.startActivityForResult(intent, REQUEST_CODE_AUTHORIZATION);
            } catch (Exception e) {
                Log.w(TAG, "can't startActivityForResult", e);
            }
        }
    }

    /**
     * Register receiver to receive token update broadcast, old receiver will be unregistered automatically
     *
     * @param context Context
     * @param receiver Receiver
     */
    public static void registerTokenUpdateReceiver(Context context, TokenUpdatedReceiver receiver) {
        unregisterTokenUpdateReceiver(context);

        sTokenUpdateReceiver = receiver;
        context.registerReceiver(sTokenUpdateReceiver,
                new IntentFilter(ShizukuConstants.ACTION_UPDATE_TOKEN),
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
