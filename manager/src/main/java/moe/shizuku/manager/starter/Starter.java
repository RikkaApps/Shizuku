package moe.shizuku.manager.starter;

import android.content.Context;
import android.os.UserManager;
import android.system.ErrnoException;
import android.system.Os;

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

import moe.shizuku.api.ShizukuApiConstants;
import moe.shizuku.manager.BuildConfig;
import moe.shizuku.manager.R;
import moe.shizuku.manager.ShizukuSettings;
import moe.shizuku.manager.ktx.ContextKt;
import moe.shizuku.manager.utils.BuildUtils;
import rikka.core.os.FileUtils;

public class Starter {

    private static String COMMAND;

    private static final String DEX_NAME = String.format(Locale.ENGLISH, "server-v%d.dex", ShizukuApiConstants.SERVER_VERSION);

    public static String getCommandAdb() {
        return "adb shell " + getCommand();
    }

    public static String getCommand() {
        return COMMAND;
    }

    public static void writeFiles(Context context) {
        try {
            File out = getRoot(context);
            try {
                Os.chmod(out.getAbsolutePath(), 0711);
            } catch (ErrnoException e) {
                e.printStackTrace();
            }

            String dexPath = copyDex(context, "server.dex", new File(out, DEX_NAME));

            COMMAND = "sh " + writeShellFile(context, new File(out, "start.sh"), dexPath);

            writeLegacyCompatAdbShellFile(context);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static File getRoot(Context context) {
        return ContextKt.createDeviceProtectedStorageContextCompat(context).getFilesDir().getParentFile();
    }

    private static String copyDex(Context context, String source, File out) throws IOException {
        if (out.exists() && !BuildConfig.DEBUG) {
            return out.getAbsolutePath();
        }

        InputStream is = context.getAssets().open(source);
        OutputStream os = new FileOutputStream(out);

        FileUtils.copy(is, os);

        os.flush();
        os.close();
        is.close();

        try {
            Os.chmod(out.getAbsolutePath(), 0644);
        } catch (ErrnoException e) {
            e.printStackTrace();
        }

        return out.getAbsolutePath();
    }

    private static String writeShellFile(Context context, File out, String dex) throws IOException {
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
                    .replace("%%%STARTER_PARAM%%%", getStarterParam(dex))
                    .replace("%%%LIBRARY_PATH%%%", getLibPath(context, "libhelper.so"))
            );
        }
        os.flush();
        os.close();

        try {
            Os.chmod(out.getAbsolutePath(), 0644);
        } catch (ErrnoException e) {
            e.printStackTrace();
        }

        return out.getAbsolutePath();
    }

    private static void writeLegacyCompatAdbShellFile(Context context) throws IOException {
        UserManager um = (UserManager) context.getSystemService(Context.USER_SERVICE);
        if (um == null || !BuildUtils.atLeast24() || !um.isUserUnlocked()) {
            return;
        }
        File parent = context.getExternalFilesDir(null);
        if (parent == null) {
            return;
        }
        File out = new File(parent, "start.sh");
        PrintWriter os = new PrintWriter(new FileWriter(out));
        os.println("#!/system/bin/sh");
        os.println("echo \"↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓ PLEASE READ ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓\"");
        os.println("echo \"The start script has been moved to /data, this compatibility script will be eventually removed.\"");
        os.println("echo \"Open Shizuku app to view the new command.\"");
        os.println("echo \"↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑ PLEASE READ ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑\"");
        os.println("sleep 10");
        os.println(getCommand());
        os.flush();
        os.close();
    }

    private static String getStarterParam(String dex) {
        Objects.requireNonNull(dex);

        return "--path=" + dex
                + (ShizukuSettings.isKeepSuContext() ? "" : " --use-shell-context");
    }

    private static String getLibPath(Context context, String name) {
        String path = context.getApplicationInfo().publicSourceDir;

        File sourceDir = new File(path);
        File libDir = new File(sourceDir.getParentFile(), "lib").listFiles()[0];
        File starter = new File(libDir, name);
        return starter.getAbsolutePath();
    }
}
