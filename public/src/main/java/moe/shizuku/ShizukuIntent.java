package moe.shizuku;

import android.content.ComponentName;

/**
 * Created by rikka on 2017/9/23.
 */

public class ShizukuIntent {

    public static final String MANAGER_APPLICATION_ID = "moe.shizuku.privileged.api";
    public static final String MANAGER_PACKAGE = "moe.shizuku.manager";

    public static final String ACTION_SERVER_STARTED = MANAGER_PACKAGE + ".intent.action.SERVER_STARTED";
    public static final String ACTION_TASK_STACK_CHANGED =  MANAGER_PACKAGE + ".intent.action.TASK_STACK_CHANGED";

    public static final String EXTRA_PID = MANAGER_PACKAGE + ".intent.extra.PID";
    public static final String EXTRA_TOKEN_MOST_SIG = MANAGER_PACKAGE + "intent.extra.TOKEN_MOST_SIG";
    public static final String EXTRA_TOKEN_LEAST_SIG = MANAGER_PACKAGE + "intent.extra.TOKEN_LEAST_SIG";

    public static String permission(String permission) {
        return MANAGER_PACKAGE + ".permission." + permission;
    }

    public static ComponentName componentName(String cls) {
        return new ComponentName(MANAGER_APPLICATION_ID,
                MANAGER_PACKAGE + cls);
    }
}
