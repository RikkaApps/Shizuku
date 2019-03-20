package moe.shizuku.manager;

/**
 * Created by rikka on 2017/9/23.
 */

public class Intents {

    private static final String PACKAGE = "moe.shizuku.manager";

    // intent service actions
    public static final String ACTION_AUTH_V2 = PACKAGE + ".service.action.AUTH_V2";
    public static final String ACTION_REQUEST_TOKEN_V2 = PACKAGE + ".service.action.REQUEST_TOKEN_V2";
    public static final String ACTION_AUTH_V3 = PACKAGE + ".service.action.AUTH_V3";
    public static final String ACTION_REQUEST_TOKEN_V3 = PACKAGE + ".service.action.REQUEST_TOKEN_V3";

    // local broadcast actions
    public static final String ACTION_AUTH_RESULT = PACKAGE + ".intent.action.AUTH_RESULT";

    // extras
    public static final String EXTRA_RESULT = PACKAGE + ".intent.action.RESULT";
}
