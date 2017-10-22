package moe.shizuku.manager;

import android.app.Application;
import android.content.Context;
import android.os.StrictMode;

import moe.shizuku.support.utils.Settings;

/**
 * Created by Rikka on 2017/5/12.
 */

public class ShizukuManagerApplication extends Application {

    private static boolean sInitialized = false;

    public static void init(Context context) {
        if (sInitialized) {
            return;
        }

        StrictMode.ThreadPolicy tp = new StrictMode.ThreadPolicy.Builder().build();
        StrictMode.setThreadPolicy(tp);

        ShizukuManagerSettings.init(context);
        AuthorizationManager.init(context);
        ServerLauncher.init(context);

        sInitialized = true;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        init(this);
    }
}
