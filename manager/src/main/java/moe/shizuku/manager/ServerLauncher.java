package moe.shizuku.manager;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

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

public class ServerLauncher {

    public static final String COMMAND_ADB = "adb shell sh /sdcard/Android/data/moe.shizuku.privileged.api/files/start.sh";
    public static String COMMAND_ROOT[] = new String[2];
    private static String DEX_PATH[] = new String[2];

    static void init(Context context) {
        try {
            copyDex(context);
            writeSH(context);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static File getParent(Context context) {
        return ShizukuManagerApplication.getDeviceProtectedStorageContext(context).getFilesDir();
    }

    private static void copyDex(Context context) throws IOException {
        int apiVersion = Math.min(ShizukuConstants.MAX_SDK, Build.VERSION.SDK_INT);
        String source = String.format(Locale.ENGLISH, "server-%d.dex", apiVersion);
        String target = String.format(Locale.ENGLISH, "server-%d-v%d.dex", apiVersion, ShizukuConstants.SERVER_VERSION);

        File external = context.getExternalFilesDir(null);
        File internal = getParent(context);

        File files[] = new File[external == null ? 1 : 2];
        files[0] = new File(internal, target);
        if (external != null) {
            files[1] = new File(external, target);
        }

        int i = 0;
        for (File file : files) {
            DEX_PATH[i] = file.getAbsolutePath();

            if (file.exists() && !BuildConfig.DEBUG) {
                i++;
                continue;
            }

            InputStream is = context.getAssets().open(source);
            OutputStream os = new FileOutputStream(file);

            IOUtils.copy(is, os);

            os.flush();
            os.close();
            is.close();

            i++;
        }
    }

    private static void writeSH(Context context) throws IOException {
        // adb shell sh /sdcard/Android/data/moe.shizuku.privileged.api/files/start.sh
        String target = "start.sh";

        File external = context.getExternalFilesDir(null);
        File internal = getParent(context);

        File files[] = new File[external == null ? 1 : 2];
        files[0] = new File(internal, target);

        if (external != null) {
            files[1] = new File(external, target);
        }

        int i = 0;
        for (File file : files) {
            if (!file.exists()) {
                //noinspection ResultOfMethodCallIgnored
                file.createNewFile();
            }

            COMMAND_ROOT[i] = "sh " + file.getAbsolutePath();

            BufferedReader is = new BufferedReader(new InputStreamReader(context.getResources().openRawResource(R.raw.start)));
            PrintWriter os = new PrintWriter(new FileWriter(file));
            String line;
            while ((line = is.readLine()) != null) {
                os.println(line
                        .replace("%%%STARTER_PATH%%%", getStarterPath(context))
                        .replace("%%%STARTER_PARAM%%%", getStarterParam(i))
                );
            }
            os.flush();
            os.close();

            i++;
        }
    }

    private static String getStarterParam(int i) {
        return "--path=" + DEX_PATH[i]
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
