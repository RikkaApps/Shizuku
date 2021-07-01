package moe.shizuku.common.util;

import android.os.Build;

/**
 * TODO: Replace it with {@link rikka.core.util.BuildUtils}.
 */
public class BuildUtils {

    private static final int SDK = Build.VERSION.SDK_INT;

    private static final int PREVIEW_SDK = SDK >= 23 ? Build.VERSION.PREVIEW_SDK_INT : 0;

    public static boolean atLeast31() {
        return SDK >= 31 || SDK == 30 && PREVIEW_SDK > 0;
    }

    public static boolean atLeast30() {
        return SDK >= 30;
    }

    public static boolean atLeast29() {
        return SDK >= 29;
    }

    public static boolean atLeast28() {
        return SDK >= 28;
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
