package moe.shizuku.manager;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.UserManager;

import com.topjohnwu.superuser.Shell;

import moe.shizuku.manager.legacy.LegacySettings;

public class ShizukuManagerApplication extends Application {

    static {
        Shell.Config.setFlags(Shell.FLAG_REDIRECT_STDERR);
    }

    private static boolean sInitialized = false;

    public static boolean isUserUnlocked(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            UserManager um = context.getSystemService(UserManager.class);
            if (um != null && !um.isUserUnlocked()) {
                return um.isUserUnlocked();
            }
        }
        return true;
    }

    public static Context getDeviceProtectedStorageContext(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return context.createDeviceProtectedStorageContext();
        }
        return context;
    }

    public static Context getDeviceProtectedStorageContextIfNeeded(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (!isUserUnlocked(context)) {
                return context.createDeviceProtectedStorageContext();
            }
        }
        return context;
    }

    public static void init(Context context) {
        if (sInitialized) {
            return;
        }

        LegacySettings.init(getDeviceProtectedStorageContext(context));
        ServerLauncher.init(context);

        sInitialized = true;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        init(this);
    }
}
