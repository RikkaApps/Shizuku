package moe.shizuku.manager;

import android.app.Application;
import android.content.Context;
import android.os.Build;

import com.topjohnwu.superuser.Shell;

import me.weishu.reflection.Reflection;
import moe.shizuku.manager.authorization.AuthorizationManager;
import rikka.material.app.DayNightDelegate;
import rikka.material.app.LocaleDelegate;

public class ShizukuApplication extends Application {

    static {
        Shell.Config.setFlags(Shell.FLAG_REDIRECT_STDERR);
    }

    public static Context getDeviceProtectedStorageContext(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return context.createDeviceProtectedStorageContext();
        }
        return context;
    }

    public static void init(Context context) {
        ShizukuSettings.initialize(context);
        LocaleDelegate.setDefaultLocale(ShizukuSettings.getLocale());
        DayNightDelegate.setApplicationContext(context);
        DayNightDelegate.setDefaultNightMode(ShizukuSettings.getNightMode());
        AuthorizationManager.init(context);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        init(this);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        Reflection.unseal(base);
    }
}
