package moe.shizuku.server;

import moe.shizuku.api.ShizukuApiConstants;

public class ServerConstants {

    public static final int SOCKET_ACTION_REQUEST_BINDER = 100;

    public static final String EXTRA_TOKEN_LEAST_SIG = ShizukuApiConstants.MANAGER_APPLICATION_ID + ".intent.extra.TOKEN_LEAST_SIG";
    public static final String EXTRA_TOKEN_MOST_SIG = ShizukuApiConstants.MANAGER_APPLICATION_ID + ".intent.extra.TOKEN_MOST_SIG";
    public static final String ACTION_UPDATE_TOKEN = ShizukuApiConstants.MANAGER_APPLICATION_ID + ".intent.action.UPDATE_TOKEN";
    public static final String ACTION_REQUEST_AUTHORIZATION = ShizukuApiConstants.MANAGER_APPLICATION_ID + ".intent.action.REQUEST_AUTHORIZATION";
    public static final String ACTION_SERVER_STARTED = ShizukuApiConstants.MANAGER_APPLICATION_ID + ".intent.action.SERVER_STARTED";

    public static final int MANAGER_APP_NOT_FOUND = 1;
    public static final int PERMISSION_NOT_GRANTED = 2;

}
