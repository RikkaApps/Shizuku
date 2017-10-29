package moe.shizuku;

import android.net.Uri;

import java.net.InetAddress;

/**
 * Created by rikka on 2017/9/23.
 */

public final class ShizukuConstants {

    public static final int PORT = 55609;
    public static final InetAddress HOST = InetAddress.getLoopbackAddress();
    public static final int TIMEOUT = 10000;
    public static final int SERVER_VERSION = 26;
    public static final int MAX_SDK = 26;

    public static final String MANAGER_APPLICATION_ID = "moe.shizuku.privileged.api";

    public static final String ACTION_SERVER_STARTED = MANAGER_APPLICATION_ID + ".intent.action.SERVER_STARTED";
    public static final String ACTION_REQUEST_AUTHORIZATION = MANAGER_APPLICATION_ID + ".intent.action.REQUEST_AUTHORIZATION";
    public static final String ACTION_UPDATE_TOKEN = MANAGER_APPLICATION_ID + ".intent.action.UPDATE_TOKEN";

    public static final String EXTRA_TOKEN_MOST_SIG = MANAGER_APPLICATION_ID + ".intent.extra.TOKEN_MOST_SIG";
    public static final String EXTRA_TOKEN_LEAST_SIG = MANAGER_APPLICATION_ID + ".intent.extra.TOKEN_LEAST_SIG";

    public static final Uri TRANSFER_PROVIDER_URI = Uri.parse("content://moe.shizuku.manager.transferprovider");
    public static final String TRANSFER_PROVIDER_NAME = "moe.shizuku.manager.transferprovider";
    public static final String TRANSFER_PROVIDER_METHOD_GET = "get";
    public static final String TRANSFER_PROVIDER_METHOD_PUT = "put";
    public static final String TRANSFER_PROVIDER_TYPE_PARCELABLE = "type_parcelable";
    public static final String TRANSFER_PROVIDER_KEY_ID = "id";
    public static final String TRANSFER_PROVIDER_KEY_DATA = "data";

    public static final Uri TOKEN_PROVIDER_URI = Uri.parse("content://moe.shizuku.manager.tokenprovider");
}
