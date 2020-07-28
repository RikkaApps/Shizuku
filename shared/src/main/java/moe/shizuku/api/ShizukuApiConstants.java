package moe.shizuku.api;

public class ShizukuApiConstants {

    public static final String MANAGER_APPLICATION_ID = "moe.shizuku.privileged.api";
    public static final int SERVER_VERSION = 10;

    public static final String PERMISSION = "moe.shizuku.manager.permission.API_V23";

    // binder
    public static final String BINDER_DESCRIPTOR = "moe.shizuku.server.IShizukuService";
    public static final int BINDER_TRANSACTION_transact = 1;

    // intent
    public static final String EXTRA_BINDER = MANAGER_APPLICATION_ID + ".intent.extra.BINDER";

    // user service
    public static final String USER_SERVICE_ARG_CLASSNAME = "shizuku:user-service-arg-classname";
    public static final String USER_SERVICE_ARG_PACKAGE_NAME = "shizuku:user-service-arg-package-name";
    public static final String USER_SERVICE_ARG_VERSION_CODE = "shizuku:user-service-arg-version-code";
    public static final String USER_SERVICE_ARG_ALWAYS_RECREATE = "shizuku:user-service-arg-always-recreate";

}
