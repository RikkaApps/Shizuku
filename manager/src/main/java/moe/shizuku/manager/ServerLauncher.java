package moe.shizuku.manager;

import android.content.Context;
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
import java.util.Objects;
import java.util.UUID;

import moe.shizuku.api.ShizukuApiConstants;
import moe.shizuku.manager.legacy.ShizukuLegacy;
import rikka.core.util.IOUtils;

public class ServerLauncher {

    public static final String COMMAND_ADB = "adb shell sh /sdcard/Android/data/moe.shizuku.privileged.api/files/start.sh";
    public static String COMMAND_ROOT;
    private static final String[] DEX_PATH = new String[2];
    private static final String[] DEX_LEGACY_PATH = new String[2];

    private static final String V2_DEX_NAME = String.format(Locale.ENGLISH, "server-legacy-v%d-api%d.dex", ShizukuLegacy.SERVER_VERSION, Math.min(ShizukuLegacy.MAX_SDK, Build.VERSION.SDK_INT));
    private static final String V3_DEX_NAME = String.format(Locale.ENGLISH, "server-v%d.dex", ShizukuApiConstants.SERVER_VERSION);

    public static void writeFiles(Context context, boolean external) {
        try {
            File out;
            if (external)
                out = context.getExternalFilesDir(null);
            else
                out = getParent(context);

            if (out == null)
                return;

            int apiVersion = Math.min(ShizukuLegacy.MAX_SDK, Build.VERSION.SDK_INT);
            String source = String.format(Locale.ENGLISH, "server-v2-%d.dex", apiVersion);
            int i = external ? 1 : 0;

            DEX_LEGACY_PATH[i] = copyDex(context, source, new File(out, V2_DEX_NAME));
            DEX_PATH[i] = copyDex(context, "server.dex", new File(out, V3_DEX_NAME));

            String command = writeShellFile(context, new File(out, "start.sh"), DEX_LEGACY_PATH[i], DEX_PATH[i]);
            if (!external) {
                COMMAND_ROOT = command;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static File getParent(Context context) {
        return ShizukuManagerApplication.getDeviceProtectedStorageContext(context).getFilesDir();
    }

    private static String copyDex(Context context, String source, File out) throws IOException {
        if (out.exists() && !BuildConfig.DEBUG) {
            return out.getAbsolutePath();
        }

        InputStream is = context.getAssets().open(source);
        OutputStream os = new FileOutputStream(out);

        IOUtils.copy(is, os);

        os.flush();
        os.close();
        is.close();

        return out.getAbsolutePath();
    }

    private static String writeShellFile(Context context, File out, String dexLegacy, String dex) throws IOException {
        if (!out.exists()) {
            //noinspection ResultOfMethodCallIgnored
            out.createNewFile();
        }

        BufferedReader is = new BufferedReader(new InputStreamReader(context.getResources().openRawResource(R.raw.start)));
        PrintWriter os = new PrintWriter(new FileWriter(out));
        String line;
        while ((line = is.readLine()) != null) {
            os.println(line
                    .replace("%%%STARTER_PATH%%%", getLibPath(context, "libshizuku.so"))
                    .replace("%%%STARTER_PARAM%%%", getStarterParam(dexLegacy, dex))
                    .replace("%%%LIBRARY_PATH%%%", getLibPath(context, "libhelper.so"))
            );
        }
        os.flush();
        os.close();

        return "sh " + out.getAbsolutePath();
    }

    private static String getStarterParam(String dexLegacy, String dex) {
        Objects.requireNonNull(dexLegacy);
        Objects.requireNonNull(dex);

        return "--path-legacy=" + dexLegacy
                + " --path=" + dex
                + " --token=" + UUID.randomUUID()
                + (ShizukuManagerSettings.isStartServiceV2() ? "" : " --no-v2")
                + (ShizukuManagerSettings.isKeepSuContext() ? "" : " --use-shell-context");
    }

    private static String getLibPath(Context context, String name) {
        String path = context.getApplicationInfo().publicSourceDir;

        File sourceDir = new File(path);
        File libDir = new File(sourceDir.getParentFile(), "lib").listFiles()[0];
        File starter = new File(libDir, name);
        return starter.getAbsolutePath();
    }
}
