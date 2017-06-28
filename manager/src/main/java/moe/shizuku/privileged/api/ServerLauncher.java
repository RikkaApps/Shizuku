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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.Socket;
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

    private static final int SERVER_TIMEOUT = 5000;
    private static final int SERVER_CHECK_INTERVAL = 50;

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

    public static void forceStopPackage(Context context, String packageName) {
        try {
            UUID token = getToken(context);

            Socket client = new Socket(Protocol.HOST, Protocol.PORT);
            client.setSoTimeout(SERVER_TIMEOUT);
            ParcelOutputStream os = new ParcelOutputStream(client.getOutputStream());
            ParcelInputStream is = new ParcelInputStream(client.getInputStream());
            os.writeInt(Actions.forceStopPackage);
            os.writeLong(token.getMostSignificantBits());
            os.writeLong(token.getLeastSignificantBits());
            os.writeString(packageName);
            os.writeInt(Process.myUid() / 100000);
            is.readException();
        } catch (Exception ignored) {
        }
    }

    private static void sendQuit() {
        try {
            Socket socket = new Socket(Protocol.HOST, Protocol.PORT);
            socket.setSoTimeout(100);
            ParcelOutputStream os = new ParcelOutputStream(socket.getOutputStream());
            ParcelInputStream is = new ParcelInputStream(socket.getInputStream());
            os.writeInt(-2);
            is.readException();

            Log.i("RServer", "send quit to old server");

            Thread.sleep(100);
        } catch (IOException | InterruptedException ignored) {
        }
    }

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
                COMMAND_ADB = "adb shell sh " + file.getAbsolutePath();
            }

            String path = context.getPackageManager().getApplicationInfo(context.getPackageName(), 0).publicSourceDir;

            BufferedWriter os = new BufferedWriter(new FileWriter(file));
            os.write("#!/system/bin/sh\n");
            os.write("if [ -f " + path + " ]\n");
            os.write("then\n");
            os.write("\texec app_process -Djava.class.path=" + path + " /system/bin --nice-name=rikka_server moe.shizuku.server.Server &");
            os.write("else\n");
            os.write("\techo \"Apk file not exist, please open Shizuku Manager and try again.\"\n");
            os.write("fi\n");
            os.flush();
            os.close();
        } catch (Exception ignored) {
        }
    }

    @WorkerThread
    public static void startRoot(String path) {
        if (Shell.SU.available()) {
            sendQuit();

            Shell.SU.run("app_process -Djava.class.path=" + path + " /system/bin --nice-name=rikka_server moe.shizuku.server.Server &", SERVER_TIMEOUT);
        }
    }

    @WorkerThread
    public static Protocol startRoot(Context context) {
        if (Shell.SU.available()) {
            sendQuit();

            String path = null;
            try {
                path = context.getPackageManager().getApplicationInfo(context.getPackageName(), 0).publicSourceDir;
            } catch (PackageManager.NameNotFoundException ignored) {
            }

            long time = System.currentTimeMillis();
            Shell.SU.run(new String[]{
                  "app_process -Djava.class.path=" + path + " /system/bin --nice-name=rikka_server moe.shizuku.server.Server &"
            }, SERVER_TIMEOUT);

            while (System.currentTimeMillis() - time < SERVER_TIMEOUT) {
                try {
                    Thread.sleep(SERVER_CHECK_INTERVAL);
                    Protocol protocol = getVersion();
                    if (protocol.getCode() == Protocol.RESULT_OK) {
                        Log.d("RServer", "server started in " + (System.currentTimeMillis() - time) + "ms");
                        return protocol;
                    }
                } catch (Exception ignored) {
                }
            }

            Log.i("RServer", "server not started in " + (System.currentTimeMillis() - time) + "ms");
            return Protocol.createUnknown();
        } else {
            Log.i("RServer", "cant start server because no root permission");
        }
        return Protocol.createUnknown();
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
