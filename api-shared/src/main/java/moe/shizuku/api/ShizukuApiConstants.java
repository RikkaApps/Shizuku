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
    public static final String ACTION_SEND_BINDER = "moe.shizuku.client.intent.action.SEND_BINDER";
    public static final String EXTRA_BINDER = MANAGER_APPLICATION_ID + ".intent.extra.BINDER";

    // socket
    public static final String SOCKET_NAME = "shizuku_server";

    public static final int SOCKET_ACTION_PING = 0;
    public static final int SOCKET_ACTION_REQUEST_BINDER = 1;

    public static final int SOCKET_TIMEOUT = 2000;
    public static final int SOCKET_VERSION_CODE = 1;

    public static final int RESULT_OK = 0;
    public static final int RESULT_NO_PERMISSION = -1;
    public static final int RESULT_PACKAGE_NOT_MATCHING = -2;
    public static final int RESULT_START_ACTIVITY_FAILED = -3;
    public static final int RESULT_IO_EXCEPTION = 0x3FFFFFFF;
    public static final int RESULT_EXCEPTION = 0x3FFFFFFF + 0x1;

    // for pre-23
    static final String ACTION_REQUEST_AUTHORIZATION = MANAGER_APPLICATION_ID + ".intent.action.REQUEST_AUTHORIZATION";
    static final String EXTRA_TOKEN_MOST_SIG = MANAGER_APPLICATION_ID + ".intent.extra.TOKEN_MOST_SIG";
    static final String EXTRA_TOKEN_LEAST_SIG = MANAGER_APPLICATION_ID + ".intent.extra.TOKEN_LEAST_SIG";
}
