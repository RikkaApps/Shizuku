package moe.shizuku.server;

import android.os.Build;

/**
 * Created by rikka on 2017/6/10.
 */

public class BuildUtils {

    public static boolean isO() {
        return Build.VERSION.SDK_INT == Build.VERSION_CODES.N_MR1 && Build.VERSION.PREVIEW_SDK_INT > 0
                || Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1;
    }
}
