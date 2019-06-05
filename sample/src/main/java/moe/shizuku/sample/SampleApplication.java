package moe.shizuku.sample;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import me.weishu.reflection.Reflection;
import moe.shizuku.api.ShizukuMultiProcessHelper;
import moe.shizuku.api.ShizukuClientHelper;
import moe.shizuku.api.ShizukuService;

public class SampleApplication extends android.app.Application {

    public static final String ACTION_SEND_BINDER = "moe.shizuku.client.intent.action.SEND_BINDER";

    public static String getProcessName() {
        if (Build.VERSION.SDK_INT >= 28)
            return Application.getProcessName();
        else {
            try {
                @SuppressLint("PrivateApi")
                Class<?> activityThread = Class.forName("android.app.ActivityThread");
                Method method = activityThread.getDeclaredMethod("currentProcessName");
                return (String) method.invoke(null);
            } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static boolean v3Failed;
    private static boolean v3TokenValid;

    public static boolean isShizukuV3Failed() {
        return v3Failed;
    }

    public static boolean isShizukuV3TokenValid() {
        return v3TokenValid;
    }

    public static void setShizukuV3TokenValid(boolean v3TokenValid) {
        SampleApplication.v3TokenValid = v3TokenValid;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        Reflection.unseal(base);

        Log.d("ShizukuSample", "initialize " + ShizukuMultiProcessHelper.initialize(this, !getProcessName().endsWith(":test")));

        ShizukuClientHelper.setBinderReceivedListener(() -> {
            Log.d("ShizukuSample", "onBinderReceived");

            if (ShizukuService.getBinder() == null) {
                // ShizukuBinderReceiveProvider started without binder, should never happened
                Log.d("ShizukuSample", "binder is null");
                v3Failed = true;
                return;
            } else {
                try {
                    // test the binder first
                    ShizukuService.pingBinder();

                    if (Build.VERSION.SDK_INT < 23) {
                        String token = ShizukuClientHelper.loadPre23Token(base);
                        v3TokenValid = ShizukuService.setCurrentProcessTokenPre23(token);
                    }

                    LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(ACTION_SEND_BINDER));
                } catch (Throwable tr) {
                    // blocked by SELinux or server dead, should never happened
                    Log.i("ShizukuSample", "can't contact with remote", tr);
                    v3Failed = true;
                    return;
                }
            }
        });
    }
}
