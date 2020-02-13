package moe.shizuku.server.utils;

import android.os.Build;

public class BuildUtils {

    private static final int SDK = Build.VERSION.SDK_INT;

    public static boolean atLeast29() {
        return SDK >= 29;
    }

    public static boolean atLeast26() {
        return SDK >= 26;
    }

    public static boolean atLeast24() {
        return SDK >= 24;
    }

    public static boolean atLeast23() {
        return SDK >= 23;
    }
}
