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
import java.util.UUID;

import moe.shizuku.ShizukuConstants;
import moe.shizuku.api.ShizukuApiConstants;
import moe.shizuku.support.utils.IOUtils;

public class ServerLauncher {

    public static final String COMMAND_ADB = "adb shell sh /sdcard/Android/data/moe.shizuku.privileged.api/files/start.sh";
    public static String[] COMMAND_ROOT = new String[2];
    private static String[] DEX_PATH = new String[2];
    private static String[] DEX_LEGACY_PATH = new String[2];

    private static final String V2_DEX_NAME;
    private static final String V3_DEX_NAME;

    static {
        int apiVersion = Math.min(ShizukuConstants.MAX_SDK, Build.VERSION.SDK_INT);
        V2_DEX_NAME = String.format(Locale.ENGLISH, "server-legacy-v%d-api%d.dex", ShizukuConstants.SERVER_VERSION, apiVersion);
        V3_DEX_NAME = String.format(Locale.ENGLISH, "server-v%d.dex", ShizukuApiConstants.SERVER_VERSION);
    }

    public static void writeFiles(Context context) {
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
        String source = String.format(Locale.ENGLISH, "server-v2-%d.dex", apiVersion);

        copyDex(context, source, V2_DEX_NAME, DEX_LEGACY_PATH);
        copyDex(context, "server.dex", V3_DEX_NAME, DEX_PATH);
    }

    private static void copyDex(Context context, String source, String target, String[] out) throws IOException {
        File external = context.getExternalFilesDir(null);
        File internal = getParent(context);

        File[] files = new File[external == null ? 1 : 2];
        files[0] = new File(internal, target);
        if (external != null) {
            files[1] = new File(external, target);
        }

        int i = 0;
        for (File file : files) {
            out[i] = file.getAbsolutePath();

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

    public static void writeSH(Context context) throws IOException {
        // adb shell sh /sdcard/Android/data/moe.shizuku.privileged.api/files/start.sh
        String target = "start.sh";

        File external = context.getExternalFilesDir(null);
        File internal = getParent(context);

        File[] files = new File[external == null ? 1 : 2];
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
                        .replace("%%%STARTER_PATH%%%", getLibPath(context, "libshizuku.so"))
                        .replace("%%%STARTER_PARAM%%%", getStarterParam(i))
                        .replace("%%%LIBRARY_PATH%%%", getLibPath(context, "libhelper.so"))
                );
            }
            os.flush();
            os.close();

            i++;
        }
    }

    private static String getStarterParam(int i) {
        return "--path-legacy=" + DEX_LEGACY_PATH[i]
                + " --path=" + DEX_PATH[i]
                + " --token=" + UUID.randomUUID()
                + (ShizukuManagerSettings.isStartServiceV2() ? "" : " --no-v2");
    }

    private static String getLibPath(Context context, String name) {
        String path;
        try {
            path = context.getPackageManager().getApplicationInfo(context.getPackageName(), 0).publicSourceDir;
        } catch (PackageManager.NameNotFoundException ignored) {
            throw new RuntimeException();
        }

        File sourceDir = new File(path);
        File libDir = new File(sourceDir.getParentFile(), "lib").listFiles()[0];
        File starter = new File(libDir, name);
        return starter.getAbsolutePath();
    }
}
