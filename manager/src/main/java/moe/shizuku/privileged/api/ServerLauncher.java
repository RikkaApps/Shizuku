package moe.shizuku.privileged.api;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Process;
import android.support.annotation.IntDef;
import android.support.annotation.WorkerThread;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.Socket;
import java.util.ArrayList;
import java.util.UUID;

import moe.shizuku.libsuperuser.Shell;
import moe.shizuku.server.Actions;
import moe.shizuku.server.Protocol;
import moe.shizuku.server.io.ParcelInputStream;
import moe.shizuku.server.io.ParcelOutputStream;

/**
 * Created by Rikka on 2017/5/13.
 */

public class ServerLauncher {

    private static final int SERVER_TIMEOUT = 10000;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({SERVER_RUNNING, SERVER_STOPPED, SERVER_VERSION_NOT_MATCHED})
    public @interface ServerStatus {}

    public static final int SERVER_RUNNING = 0;
    public static final int SERVER_STOPPED = -1;
    public static final int SERVER_VERSION_NOT_MATCHED = -2;

    @ServerStatus
    public static int getStatus() {
        Protocol version = getVersion();
        if (version == null) {
            return SERVER_STOPPED;

        }
        if (!version.versionUnmatched()) {
            Log.d("RServer", "server running version: " + version.getVersion());

            return SERVER_RUNNING;
        }
        return SERVER_VERSION_NOT_MATCHED;
    }

    public static Protocol getVersion() {
        try {
            Socket client = new Socket(Protocol.HOST, Protocol.PORT);
            client.setSoTimeout(SERVER_TIMEOUT);
            ParcelOutputStream os = new ParcelOutputStream(client.getOutputStream());
            ParcelInputStream is = new ParcelInputStream(client.getInputStream());
            os.writeInt(Actions.version);
            is.readException();
            return is.readParcelable(Protocol.CREATOR);
        } catch (Exception ignored) {
        }
        return Protocol.createUnknown();
    }

    public static Protocol authorize(Context context) {
        try {
            UUID token = getToken(context);

            Socket client = new Socket(Protocol.HOST, Protocol.PORT);
            client.setSoTimeout(SERVER_TIMEOUT);
            ParcelOutputStream os = new ParcelOutputStream(client.getOutputStream());
            ParcelInputStream is = new ParcelInputStream(client.getInputStream());
            os.writeInt(Actions.authorize);
            os.writeLong(token.getMostSignificantBits());
            os.writeLong(token.getLeastSignificantBits());
            is.readException();
            return is.readParcelable(Protocol.CREATOR);
        } catch (Exception ignored) {
            return Protocol.createUnknown();
        }
    }

    public static void requestToken() {
        try {
            Socket client = new Socket(Protocol.HOST, Protocol.PORT);
            client.setSoTimeout(SERVER_TIMEOUT);
            ParcelOutputStream os = new ParcelOutputStream(client.getOutputStream());
            ParcelInputStream is = new ParcelInputStream(client.getInputStream());
            os.writeInt(-1);
            os.writeInt(Process.myUid());
            is.readException();
        } catch (Exception ignored) {
        }
    }

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

            Shell.Result result = Shell.SU.run(COMMAND_ROOT, SERVER_TIMEOUT);
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

            return Shell.SU.run("app_process -Djava.class.path=" + path + " /system/bin --nice-name=rikka_server2 moe.shizuku.server.Server &", SERVER_TIMEOUT);
        } else {
            return new Shell.Result(99, new ArrayList<String>());
        }
    }

    public static UUID getToken(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        long mostSig = preferences.getLong("token_most", 0);
        long leastSig = preferences.getLong("token_least", 0);
        return new UUID(mostSig, leastSig);
    }

    public static UUID getToken(Intent intent) {
        long mostSig = intent.getLongExtra("moe.shizuku.privileged.api.intent.extra.TOKEN_MOST_SIG", 0);
        long leastSig = intent.getLongExtra("moe.shizuku.privileged.api.intent.extra.TOKEN_LEAST_SIG", 0);
        if (mostSig != 0 && leastSig != 0) {
            return new UUID(mostSig, leastSig);
        } else {
            return null;
        }
    }

    public static void putToken(Context context, Intent intent) {
        long mostSig = intent.getLongExtra("moe.shizuku.privileged.api.intent.extra.TOKEN_MOST_SIG", 0);
        long leastSig = intent.getLongExtra("moe.shizuku.privileged.api.intent.extra.TOKEN_LEAST_SIG", 0);

        SharedPreferences preferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        preferences.edit()
                .putLong("token_most", mostSig)
                .putLong("token_least", leastSig)
                .apply();

        Log.i("RServer", "token update: " + new UUID(mostSig, leastSig));
    }
}
