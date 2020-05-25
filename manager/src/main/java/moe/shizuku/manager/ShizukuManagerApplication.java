package moe.shizuku.manager;

import android.app.Application;
import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelStore;
import androidx.lifecycle.ViewModelStoreOwner;

import com.topjohnwu.superuser.Shell;

import me.weishu.reflection.Reflection;
import moe.shizuku.manager.authorization.AuthorizationManager;
import rikka.material.app.DayNightDelegate;
import rikka.material.app.LocaleDelegate;

public class ShizukuManagerApplication extends Application implements ViewModelStoreOwner {

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
        ShizukuManagerSettings.initialize(context);
        LocaleDelegate.setDefaultLocale(ShizukuManagerSettings.getLocale());
        DayNightDelegate.setApplicationContext(context);
        DayNightDelegate.setDefaultNightMode(ShizukuManagerSettings.getNightMode());
        AuthorizationManager.init(context);
    }

    private ViewModelStore mViewModelStore;

    @Override
    public void onCreate() {
        super.onCreate();

        init(this);
    }

    @NonNull
    @Override
    public ViewModelStore getViewModelStore() {
        if (mViewModelStore == null) {
            mViewModelStore = new ViewModelStore();
        }
        return mViewModelStore;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        Reflection.unseal(base);
    }
}
