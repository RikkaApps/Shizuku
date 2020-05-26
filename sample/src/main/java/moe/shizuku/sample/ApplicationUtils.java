package moe.shizuku.sample;

import android.annotation.SuppressLint;
import android.app.Application;
import android.os.Build;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ApplicationUtils {

    private static Application application;

    public static Application getApplication() {
        return application;
    }

    public static void setApplication(Application application) {
        ApplicationUtils.application = application;
    }

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
}
