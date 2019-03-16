package moe.shizuku.server;

public class ShizukuConstants {

    public static final String MANAGER_APPLICATION_ID = "moe.shizuku.privileged.api";

    public static final String ACTION_SERVER_STARTED = MANAGER_APPLICATION_ID + ".intent.action.SERVER_STARTED";
    public static final String ACTION_REQUEST_AUTHORIZATION = MANAGER_APPLICATION_ID + ".intent.action.REQUEST_AUTHORIZATION";
    public static final String ACTION_UPDATE_TOKEN = MANAGER_APPLICATION_ID + ".intent.action.UPDATE_TOKEN";

    public static final String EXTRA_TOKEN_MOST_SIG = MANAGER_APPLICATION_ID + ".intent.extra.TOKEN_MOST_SIG";
    public static final String EXTRA_TOKEN_LEAST_SIG = MANAGER_APPLICATION_ID + ".intent.extra.TOKEN_LEAST_SIG";
    public static final String EXTRA_BINDER = MANAGER_APPLICATION_ID + ".intent.extra.BINDER";
}
