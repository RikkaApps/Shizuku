package moe.shizuku.manager;

/**
 * Created by rikka on 2017/9/23.
 */

public class Intents {

    private static final String PACKAGE = "moe.shizuku.manager";

    // intent service actions
    public static final String ACTION_AUTH = PACKAGE + ".service.action.AUTH";
    public static final String ACTION_REQUEST_TOKEN = PACKAGE + ".service.action.REQUEST_TOKEN";

    // local broadcast actions
    public static final String ACTION_AUTH_RESULT = PACKAGE + ".intent.action.AUTH_RESULT";

    // extras
    public static final String EXTRA_RESULT = PACKAGE + ".intent.action.RESULT";
}
