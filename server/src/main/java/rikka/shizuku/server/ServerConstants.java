package rikka.shizuku.server;

public class ServerConstants {

    public static final int MANAGER_APP_NOT_FOUND = 50;

    // Permission name that apps built with the Shizuku API library request in their manifests.
    // Mitsuku only matches against it and never declares it, so it must keep the original name.
    public static final String PERMISSION = "moe.shizuku.manager.permission.API_V23";
    public static final String MANAGER_APPLICATION_ID = "com.vinterminez.mitsuku";
    public static final String REQUEST_PERMISSION_ACTION = MANAGER_APPLICATION_ID + ".intent.action.REQUEST_PERMISSION";

    public static final int BINDER_TRANSACTION_getApplications = 10001;
}
