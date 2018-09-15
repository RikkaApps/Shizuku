package moe.shizuku.manager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.IntDef;
import android.util.Log;

import java.lang.annotation.Retention;
import java.util.UUID;

import moe.shizuku.ShizukuConstants;
import moe.shizuku.api.ShizukuClient;
import moe.shizuku.support.utils.Settings;

import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Created by rikka on 2017/7/18.
 */

public class ShizukuManagerSettings {

    public static void init(Context context) {
        Settings.init(context);
        ShizukuClient.setToken(ShizukuManagerSettings.getToken(context));
    }

    @IntDef({
            LaunchMethod.UNKNOWN,
            LaunchMethod.ROOT,
            LaunchMethod.ADB,
    })
    @Retention(SOURCE)
    public @interface LaunchMethod {
        int UNKNOWN = -1;
        int ROOT = 0;
        int ADB = 1;
    }

    public static @LaunchMethod int getLastLaunchMode() {
        return Settings.getInt("mode", LaunchMethod.UNKNOWN);
    }

    public static void setLastLaunchMode(@LaunchMethod int method) {
        Settings.putInt("mode", method);
    }

    public static UUID getToken(Context context) {
        context = ShizukuManagerApplication.getDeviceProtectedStorageContext(context);

        SharedPreferences preferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        long mostSig = preferences.getLong("token_most", 0);
        long leastSig = preferences.getLong("token_least", 0);
        return new UUID(mostSig, leastSig);
    }

    public static void putToken(Context context, Intent intent) {
        context = ShizukuManagerApplication.getDeviceProtectedStorageContext(context);

        long mostSig = intent.getLongExtra(ShizukuConstants.EXTRA_TOKEN_MOST_SIG, 0);
        long leastSig = intent.getLongExtra(ShizukuConstants.EXTRA_TOKEN_LEAST_SIG, 0);

        SharedPreferences preferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        preferences.edit()
                .putLong("token_most", mostSig)
                .putLong("token_least", leastSig)
                .apply();

        UUID token = new UUID(mostSig, leastSig);
        ShizukuClient.setToken(token);

        Log.i(Constants.TAG, "token update: " + token);
    }
}
