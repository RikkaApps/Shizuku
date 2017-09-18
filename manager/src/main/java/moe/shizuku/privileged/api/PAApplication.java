package moe.shizuku.privileged.api;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.StrictMode;
import android.util.Log;

import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;
import moe.shizuku.support.utils.Settings;

/**
 * Created by Rikka on 2017/5/12.
 */

public class PAApplication extends Application {

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
