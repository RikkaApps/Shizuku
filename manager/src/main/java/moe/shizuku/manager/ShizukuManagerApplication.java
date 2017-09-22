package moe.shizuku.manager;

import android.app.Application;
import android.os.StrictMode;

import moe.shizuku.support.utils.Settings;

/**
 * Created by Rikka on 2017/5/12.
 */

public class ShizukuManagerApplication extends Application {

    static {
        StrictMode.ThreadPolicy tp = new StrictMode.ThreadPolicy.Builder().build();
        StrictMode.setThreadPolicy(tp);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Settings.init(this);
        Permissions.init(this);
    }
}
