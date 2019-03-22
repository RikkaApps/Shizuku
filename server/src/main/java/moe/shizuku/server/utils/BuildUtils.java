package moe.shizuku.server.utils;

import android.os.Build;

public class BuildUtils {

    private static final int SDK = Build.VERSION.SDK_INT;

    public static boolean isPreM() {
        return SDK < 23;
    }
}
