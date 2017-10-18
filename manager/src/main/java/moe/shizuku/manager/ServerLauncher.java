package moe.shizuku.manager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Process;
import android.support.annotation.WorkerThread;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;

import moe.shizuku.libsuperuser.Shell;

/**
 * Created by Rikka on 2017/5/13.
 */

public class ServerLauncher {

    private static final int TIMEOUT = 10000;

    public static String COMMAND_ROOT = "sh /sdcard/Android/data/moe.shizuku.privileged.api/files/start.sh --skip-check";
    public static String COMMAND_ADB = "adb shell sh /sdcard/Android/data/moe.shizuku.privileged.api/files/start.sh";

    public static void writeSH(Context context) {
        // adb shell sh /sdcard/Android/data/moe.shizuku.privileged.api/files/start.sh
        try {
            File file = new File(context.getExternalFilesDir(null), "start.sh");
            if (!file.exists()) {
                //noinspection ResultOfMethodCallIgnored
                file.createNewFile();
            }

            @SuppressLint("SdCardPath")
            File sdcardFile = new File("/sdcard/Android/data/moe.shizuku.privileged.api/files/start.sh");
            // not user 0 or no /sdcard
            if (Process.myUserHandle().hashCode() != 0
                    || !sdcardFile.exists()) {
                COMMAND_ROOT = "sh " + file.getAbsolutePath() + " --skip-check";
                COMMAND_ADB = "adb shell sh" + file.getAbsolutePath();
            }

            String starterPath = starter(context);

            BufferedReader is = new BufferedReader(new InputStreamReader(context.getResources().openRawResource(R.raw.start)));
            PrintWriter os = new PrintWriter(new FileWriter(file));
            String line;
            while ((line = is.readLine()) != null) {
                os.println(line
                        .replace("%%%STARTER_PATH%%%", starterPath)
                        .replace("%%%STARTER_PARAM%%%", starterParam(context))
                );
            }
            os.flush();
            os.close();
        } catch (Exception ignored) {
        }
    }

    private static String starter(Context context) {
        String path = publicSourceDir(context);

        File sourceDir = new File(path);
        File libDir = new File(sourceDir.getParentFile(), "lib").listFiles()[0];
        File starter = new File(libDir, "libshizuku.so");
        return starter.getAbsolutePath();
    }

    private static String starterParam(Context context) {
        String path = publicSourceDir(context);

        return "--fallback-path=" + path
                /*+ " --token=" + UUID.randomUUID()*/;
    }

    private static String publicSourceDir(Context context) {
        try {
            return context.getPackageManager().getApplicationInfo(context.getPackageName(), 0).publicSourceDir;
        } catch (PackageManager.NameNotFoundException ignored) {
            throw new RuntimeException();
        }
    }

    @WorkerThread
    public static Shell.Result startRoot(Context context) {
        if (Shell.SU.available()) {
            long time = System.currentTimeMillis();

            Shell.Result result = Shell.SU.run(COMMAND_ROOT, TIMEOUT);
            Log.d("RServer", "start root result " + result.getExitCode() + " in " + (System.currentTimeMillis() - time) + "ms");
            return result;
        } else {
            return new Shell.Result(99, new ArrayList<String>());
        }
    }

    @WorkerThread
    public static Shell.Result startRootOld(Context context) {
        if (Shell.SU.available()) {
            String path;
            try {
                path = context.getPackageManager().getApplicationInfo(context.getPackageName(), 0).publicSourceDir;
            } catch (PackageManager.NameNotFoundException ignored) {
                throw new RuntimeException();
            }

            return Shell.SU.run("app_process -Djava.class.path=" + path + " /system/bin --nice-name=shizuku_server moe.shizuku.server.ShizukuServer &", TIMEOUT);
        } else {
            return new Shell.Result(99, new ArrayList<String>());
        }
    }
}
