package moe.shizuku.server.util;

import android.content.ComponentName;

/**
 * Created by Rikka on 2017/5/21.
 */

public class Intents {

    public static final String PACKAGE_NAME = "moe.shizuku.privileged.api";

    public static final String ACTION_SERVER_STARTED = action("SERVER_STARTED");
    public static final String ACTION_TASK_STACK_CHANGED = action("TASK_STACK_CHANGED");

    public static final String EXTRA_PID = extra("PID");
    public static final String EXTRA_TOKEN_MOST_SIG = extra("TOKEN_MOST_SIG");
    public static final String EXTRA_TOKEN_LEAST_SIG = extra("TOKEN_LEAST_SIG");

    private static String action(String action) {
        return PACKAGE_NAME + ".intent.action." + action;
    }

    private static String extra(String extra) {
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
