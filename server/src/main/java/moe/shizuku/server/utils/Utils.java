package moe.shizuku.server.utils;

import android.annotation.SuppressLint;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import static moe.shizuku.server.utils.Logger.LOGGER;

public class Utils {

    @SuppressLint("SdCardPath")
    private static final String LOG_FILE = "/sdcard/Android/data/moe.shizuku.privileged.api/files/shizuku_server.log";

    public static void setOut() {
        try {
            File file = new File(LOG_FILE);
            if (!file.exists()) {
                //noinspection ResultOfMethodCallIgnored
                if (!file.createNewFile()) {
                    LOGGER.w("failed to create log file");
                    return;
                }
            }
            PrintStream os = new PrintStream(file);

            System.setOut(os);
            System.setErr(os);

            //ServerLog.v("set output to " + LOG_FILE);
        } catch (IOException e) {
            LOGGER.w("failed to set output: " + e.getMessage());
        }
    }

    public static void disableHiddenApiBlacklist() {
        try {
            java.lang.Process process = new ProcessBuilder("settings", "put", "global", "hidden_api_blacklist_exemptions", "*").start();

            int res;
            if ((res = process.waitFor()) == 0) {
                LOGGER.i("disabled hidden api blacklist");
            } else {
                LOGGER.w("failed to disable hidden api blacklist, res=" + res);
            }
        } catch (Throwable tr) {
            LOGGER.w("failed to disable hidden api blacklist", tr);
        }
    }
}
