package moe.shizuku.server.util;

import android.annotation.SuppressLint;
import android.app.ActivityManagerNative;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Created by rikka on 2017/9/23.
 */

public class Utils {

    @SuppressLint("SdCardPath")
    private static final String LOG_FILE = "/sdcard/Android/moe.shizuku.privileged.api/files/shizuku_server.log";

    public static void setOut() {
        try {
            File file = new File(LOG_FILE);
            if (!file.exists()) {
                //noinspection ResultOfMethodCallIgnored
                if (!file.createNewFile()) {
                    ServerLog.w("failed to create log file");
                    return;
                }
            }
            PrintStream os = new PrintStream(file);

            System.setOut(os);
            System.setErr(os);

            ServerLog.v("set output to " + LOG_FILE);
        } catch (IOException e) {
            ServerLog.w("failed to set output: " + e.getMessage());
        }
    }

    public static boolean isServerDead() {
        try {
            //noinspection deprecation
            return !ActivityManagerNative.isSystemReady();
        } catch (Exception e) {
            return true;
        }
    }
}
