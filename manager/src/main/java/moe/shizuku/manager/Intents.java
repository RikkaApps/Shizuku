package moe.shizuku.manager;

/**
 * Created by rikka on 2017/9/23.
 */

public class Intents {

    private static final String PACKAGE = "moe.shizuku.manager";

    // intent service actions
    public static final String ACTION_START_SERVER = PACKAGE + ".service.action.START_SERVER";
    public static final String ACTION_START_SERVER_OLD = PACKAGE + ".service.action.START_SERVER_OLD";
    public static final String ACTION_AUTH = PACKAGE + ".service.action.AUTH";
    public static final String ACTION_REQUEST_TOKEN = PACKAGE + ".service.action.REQUEST_TOKEN";

    // local broadcast actions
    public static final String ACTION_START = PACKAGE + ".intent.action.START";
    public static final String ACTION_AUTH_RESULT = PACKAGE + ".intent.action.AUTH_RESULT";

    // extras
    public static final String EXTRA_CODE = PACKAGE + ".intent.action.CODE";
    public static final String EXTRA_OUTPUT = PACKAGE + ".intent.action.OUTPUT";
    public static final String EXTRA_ERROR = PACKAGE + ".intent.action.ERROR";
    public static final String EXTRA_IS_OLD = PACKAGE + ".intent.action.IS_OLD";
    public static final String EXTRA_RESULT = PACKAGE + ".intent.action.RESULT";
}
