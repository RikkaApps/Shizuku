package moe.shizuku.privileged.api;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;

import moe.shizuku.support.utils.Settings;

import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Created by rikka on 2017/7/18.
 */

public class PASettings {

    @IntDef({
            RootLaunchMethod.ASK,
            RootLaunchMethod.USUAL,
            RootLaunchMethod.ALTERNATIVE,
    })
    @Retention(SOURCE)
    public @interface RootLaunchMethod {
        int ASK = 0;
        int USUAL = 1;
        int ALTERNATIVE = 2;
    }

    public static @RootLaunchMethod int getRootLaunchMethod() {
        switch (Settings.getString("root_launch_method", "ask")) {
            case "ask":
                return RootLaunchMethod.ASK;
            case "usual":
                return RootLaunchMethod.USUAL;
            case "alternative":
                return RootLaunchMethod.ALTERNATIVE;
        }
        return RootLaunchMethod.ASK;
    }

    public static void setRootLaunchMethod(@RootLaunchMethod int method) {
        switch (method) {
            case RootLaunchMethod.ASK:
                Settings.putString("root_launch_method", "ask");
                break;
            case RootLaunchMethod.USUAL:
                Settings.putString("root_launch_method", "usual");
                break;
            case RootLaunchMethod.ALTERNATIVE:
                Settings.putString("root_launch_method", "alternative");
                break;
        }
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
}
