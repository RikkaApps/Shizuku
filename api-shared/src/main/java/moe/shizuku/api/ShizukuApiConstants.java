package moe.shizuku.api;

public class ShizukuApiConstants {

    public static final String MANAGER_APPLICATION_ID = "moe.shizuku.privileged.api";
    public static final int SERVER_VERSION = 1;

    public static final String PERMISSION_PRE_23 = "moe.shizuku.manager.permission.API";
    public static final String PERMISSION = "moe.shizuku.manager.permission.API_V23";

    // binder
    public static final String BINDER_DESCRIPTOR = "moe.shizuku.server.IShizukuService";
    public static final int BINDER_TRANSACTION_transact = 1;

    // intent
    public static final String EXTRA_BINDER = MANAGER_APPLICATION_ID + ".intent.extra.BINDER";

    // for pre-23
    static final String ACTION_REQUEST_AUTHORIZATION = MANAGER_APPLICATION_ID + ".intent.action.REQUEST_AUTHORIZATION";
    static final String EXTRA_TOKEN_MOST_SIG = MANAGER_APPLICATION_ID + ".intent.extra.TOKEN_MOST_SIG";
    static final String EXTRA_TOKEN_LEAST_SIG = MANAGER_APPLICATION_ID + ".intent.extra.TOKEN_LEAST_SIG";
}
