package moe.shizuku.manager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Process;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Locale;

import moe.shizuku.ShizukuConstants;
import moe.shizuku.support.utils.IOUtils;

/**
 * Created by Rikka on 2017/5/13.
 */

public class ServerLauncher {

    public static String COMMAND_ROOT;
    public static String COMMAND_ROOT_OLD;
    public static String COMMAND_ADB = "adb shell sh /sdcard/Android/data/moe.shizuku.privileged.api/files/start.sh";
    public static String DEX_PATH;

    public static void init(Context context) {
        copyDex(context);
        writeSH(context);
    }

    private static void copyDex(Context context) {
        int apiVersion = Math.min(ShizukuConstants.MAX_SDK, Build.VERSION.SDK_INT);
        String source = String.format(Locale.ENGLISH, "server-%d.dex", apiVersion);
        String target = String.format(Locale.ENGLISH, "server-%d-v%d.dex", apiVersion, ShizukuConstants.SERVER_VERSION);
        File file = new File(context.getExternalFilesDir(null), target);

        DEX_PATH = file.getAbsolutePath();
        COMMAND_ROOT_OLD = "app_process -Djava.class.path=" + DEX_PATH + " /system/bin --nice-name=shizuku_server moe.shizuku.server.ShizukuServer &";

        if (file.exists() && !BuildConfig.DEBUG) {
            return;
        }

        try {
            InputStream is = context.getAssets().open(source);
            OutputStream os = new FileOutputStream(file);

            IOUtils.copy(is, os);

            os.flush();
            os.close();
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeSH(Context context) {
        // adb shell sh /sdcard/Android/data/moe.shizuku.privileged.api/files/start.sh
        try {
            File file = new File(context.getExternalFilesDir(null), "start.sh");
            if (!file.exists()) {
                //noinspection ResultOfMethodCallIgnored
                file.createNewFile();
            }

            COMMAND_ROOT = "sh " + file.getAbsolutePath();

            @SuppressLint("SdCardPath")
            File sdcardFile = new File("/sdcard/Android/data/moe.shizuku.privileged.api/files/start.sh");
            // not user 0 or no /sdcard
            if (Process.myUserHandle().hashCode() != 0
                    || !sdcardFile.exists()) {
                COMMAND_ADB = "adb shell sh" + file.getAbsolutePath();
            }

            BufferedReader is = new BufferedReader(new InputStreamReader(context.getResources().openRawResource(R.raw.start)));
            PrintWriter os = new PrintWriter(new FileWriter(file));
            String line;
            while ((line = is.readLine()) != null) {
                os.println(line
                        .replace("%%%STARTER_PATH%%%", getStarterPath(context))
                        .replace("%%%STARTER_PARAM%%%", getStarterParam())
                );
            }
            os.flush();
            os.close();
        } catch (Exception ignored) {
        }
    }

    private static String getStarterParam() {
        return "--path=" + DEX_PATH
                /*+ " --token=" + UUID.randomUUID()*/;
    }

    private static String getStarterPath(Context context) {
        String path;
        try {
            path = context.getPackageManager().getApplicationInfo(context.getPackageName(), 0).publicSourceDir;
        } catch (PackageManager.NameNotFoundException ignored) {
            throw new RuntimeException("it's impossible");
        }

        File sourceDir = new File(path);
        File libDir = new File(sourceDir.getParentFile(), "lib").listFiles()[0];
        File starter = new File(libDir, "libshizuku.so");
        return "\"" + starter.getAbsolutePath() + "\"";
    }
}
