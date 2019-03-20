package moe.shizuku.api;

public class ShizukuApiConstants {

    public static final String MANAGER_APPLICATION_ID = "moe.shizuku.privileged.api";
    public static final String EXTRA_BINDER = MANAGER_APPLICATION_ID + ".intent.extra.BINDER";

    public static final String PERMISSION_V23 = "moe.shizuku.manager.permission.API_V23";

    public static final String SOCKET_NAME = "shizuku_server";
    public static final int SOCKET_TIMEOUT = 2000;
    public static final int SOCKET_VERSION_CODE = 1;
    public static final int SOCKET_ACTION_REQUEST_BINDER = 1;
    public static final int SERVER_VERSION = 1;

    public static final String BINDER_DESCRIPTOR = "moe.shizuku.server.IShizukuService";
    public static final int BINDER_TRANSACTION_transactRemote = 1;

    public static final String ACTION_SEND_BINDER = "moe.shizuku.server.intent.action.SEND_BINDER";
}
