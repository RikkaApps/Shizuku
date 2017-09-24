package moe.shizuku.generator.helper;

/**
 * Created by rikka on 2017/9/24.
 */

public class ApiClassHelper {

    private static String SUFFIX = "V26";

    public static void setApiVersion(int apiVersion) {
        ApiClassHelper.SUFFIX = "V" + apiVersion;
    }
}
