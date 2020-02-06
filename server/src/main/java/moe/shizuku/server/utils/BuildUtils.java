package moe.shizuku.server.utils;

import android.os.Build;

public class BuildUtils {

    private static final int SDK = Build.VERSION.SDK_INT;

    public static boolean isPreM() {
        return SDK < 23;
    }

    public static boolean atLeast29() {
        return Build.VERSION.SDK_INT >= 29;
    }

    public static boolean atLeast26() {
        return Build.VERSION.SDK_INT >= 26;
    }

    public static boolean atLeast24() {
        return Build.VERSION.SDK_INT >= 24;
    }
}
