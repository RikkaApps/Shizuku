package moe.shizuku;

import java.net.InetAddress;

/**
 * Created by rikka on 2017/9/23.
 */

public class ShizukuConstants {

    public static final int PORT = 55609;
    public static final InetAddress HOST = InetAddress.getLoopbackAddress();
    public static final int TIMEOUT = 1000;
    public static final int VERSION = 25;

    public static final String MANAGER_APPLICATION_ID = "moe.shizuku.privileged.api";
    public static final String MANAGER_PACKAGE = "moe.shizuku.manager";

    public static final String ACTION_SERVER_STARTED = MANAGER_PACKAGE + ".intent.action.SERVER_STARTED";

    public static final String EXTRA_PID = MANAGER_PACKAGE + ".intent.extra.PID";
    public static final String EXTRA_TOKEN_MOST_SIG = MANAGER_PACKAGE + "intent.extra.TOKEN_MOST_SIG";
    public static final String EXTRA_TOKEN_LEAST_SIG = MANAGER_PACKAGE + "intent.extra.TOKEN_LEAST_SIG";
}
