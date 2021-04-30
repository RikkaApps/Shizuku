package moe.shizuku.manager.utils;

import android.app.UiModeManager;
import android.content.Context;
import android.content.res.Configuration;

public class EnvironmentUtils {

    private EnvironmentUtils() {
    }

    public static boolean isWatch(Context context) {
        return context.getSystemService(UiModeManager.class).getCurrentModeType()
                == Configuration.UI_MODE_TYPE_WATCH;
    }
}
