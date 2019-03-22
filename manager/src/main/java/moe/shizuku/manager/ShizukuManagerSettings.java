package moe.shizuku.manager;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import java.lang.annotation.Retention;
import java.util.UUID;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import moe.shizuku.ShizukuConstants;
import moe.shizuku.api.ShizukuClient;
import moe.shizuku.manager.utils.EmptySharedPreferencesImpl;

import static java.lang.annotation.RetentionPolicy.SOURCE;

public class ShizukuManagerSettings {

    private static SharedPreferences sPreferences;

    @NonNull
    private static Context getSettingsStorageContext(@NonNull Context context) {
        Context storageContext;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            storageContext = context.createDeviceProtectedStorageContext();
        } else {
            storageContext = context;
        }

        storageContext = new ContextWrapper(storageContext) {
            @Override
            public SharedPreferences getSharedPreferences(String name, int mode) {
                try {
                    return super.getSharedPreferences(name, mode);
                } catch (IllegalStateException e) {
                    // SharedPreferences in credential encrypted storage are not available until after user is unlocked
                    return new EmptySharedPreferencesImpl();
                }
            }
        };

        return storageContext;
    }

    public static void initialize(Context context) {
        if (sPreferences == null) {
            sPreferences = getSettingsStorageContext(context)
                    .getSharedPreferences("settings", Context.MODE_PRIVATE);
        }

        ShizukuClient.setToken(ShizukuManagerSettings.getToken());
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

    @LaunchMethod
    public static int getLastLaunchMode() {
        return sPreferences.getInt("mode", LaunchMethod.UNKNOWN);
    }

    public static void setLastLaunchMode(@LaunchMethod int method) {
        sPreferences.edit().putInt("mode", method).apply();
    }

    public static UUID getToken() {
        final SharedPreferences preferences = sPreferences;
        long mostSig = preferences.getLong("token_most", 0);
        long leastSig = preferences.getLong("token_least", 0);
        return new UUID(mostSig, leastSig);
    }

    public static void putToken(Intent intent) {
        long mostSig = intent.getLongExtra(ShizukuConstants.EXTRA_TOKEN_MOST_SIG, 0);
        long leastSig = intent.getLongExtra(ShizukuConstants.EXTRA_TOKEN_LEAST_SIG, 0);

        UUID token = new UUID(mostSig, leastSig);
        putToken(token);
    }

    public static void putToken(UUID token) {
        long mostSig = token.getMostSignificantBits();
        long leastSig = token.getLeastSignificantBits();

        SharedPreferences preferences = sPreferences;
        preferences.edit()
                .putLong("token_most", mostSig)
                .putLong("token_least", leastSig)
                .apply();

        ShizukuClient.setToken(token);

        Log.i(AppConstants.TAG, "token update: " + token);
    }
}
