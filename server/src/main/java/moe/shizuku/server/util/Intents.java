package moe.shizuku.server.util;

import android.content.ComponentName;

/**
 * Created by Rikka on 2017/5/21.
 */

public class Intents {

    public static final String PACKAGE_NAME = "moe.shizuku.privileged.api";

    public static String action(String action) {
        return PACKAGE_NAME + ".intent.action." + action;
    }

    public static String extra(String extra) {
        return PACKAGE_NAME + ".intent.extra." + extra;
    }

    public static String permission(String permission) {
        return PACKAGE_NAME + ".permission." + permission;
    }

    public static ComponentName componentName(String cls) {
        return new ComponentName(PACKAGE_NAME,
                PACKAGE_NAME + cls);
    }
}
