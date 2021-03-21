package rikka.shizuku.cmd;

import android.app.IActivityManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.system.Os;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import hidden.HiddenApiBridgeV23;
import rikka.shizuku.Shizuku;

public class ShizukuCmd {

    private static void requestForBinder(String packageName) throws RemoteException {
        Binder binder = new Binder() {
            @Override
            protected boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
                if (code == 1) {
                    verbose("Received reply");

                    IBinder binder = data.readStrongBinder();
                    if (binder != null) {
                        try {
                            Shizuku.onBinderReceived(binder, packageName);
                        } catch (Throwable e) {
                            abort("Please check if the current SHIZUKU_APPLICATION_ID, " + packageName + ", is correct");
                        }
                    }
                    return true;
                }
                return super.onTransact(code, data, reply, flags);
            }
        };

        IBinder amBinder = ServiceManager.getService("activity");
        IActivityManager am;
        if (Build.VERSION.SDK_INT >= 26) {
            am = IActivityManager.Stub.asInterface(amBinder);
        } else {
            am = HiddenApiBridgeV23.ActivityManagerNative_asInterface(amBinder);
        }

        Bundle data = new Bundle();
        data.putBinder("binder", binder);

        Intent intent = Intent.createChooser(
                new Intent("rikka.shizuku.intent.action.REQUEST_BINDER")
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
                        .putExtra("data", data),
                "Request binder from Shizuku"
        );

        verbose("Start activity...");
        am.startActivityAsUser(null, packageName, intent, null, null, null, 0, 0, null, null, Os.getuid() / 100000);
    }

    private static boolean verboseMessageAllowed = false;
    private static boolean preserveEnvironment = false;

    public static void main(String[] args) {
        if (BuildConfig.DEBUG) {
            System.out.println("args: " + Arrays.toString(args));
        }

        if (args.length == 0) {
            printHelp();
            return;
        }

        List<String> cmds = new ArrayList<>();

        for (String arg : args) {
            switch (arg) {
                case "--verbose":
                    verboseMessageAllowed = true;
                    break;
                case "--help":
                case "-h":
                    printHelp();
                    return;
                case "--version":
                case "-v":
                    System.out.println(BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ")");
                    return;
                case "-m":
                case "-p":
                case "--preserve-environment":
                    preserveEnvironment = true;
                    break;
                default:
                    cmds.add(arg);
                    break;
            }
        }

        String packageName;
        if (Os.getuid() == 2000) {
            packageName = "com.android.shell";
        } else {
            packageName = System.getenv("SHIZUKU_APPLICATION_ID");
            if (TextUtils.isEmpty(packageName) || "PKG".equals(packageName)) {
                abort("SHIZUKU_APPLICATION_ID is not set, set this environment variable to the id of current application (package name)");
            }
        }

        Looper.prepareMainLooper();

        verbose("Requesting binder from Shizuku app...");
        verbose("If this never ends, please check:");
        verbose("1. Shizuku app is install");
        verbose("2. If your system or you are using third-party tools to prevent Shizuku app from running");
        verbose("3. If this terminal app is in foreground (Android 10 background start activity limitation");

        Shizuku.addBinderReceivedListener(() -> {
            try {
                preExec(cmds.toArray(new String[0]));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        try {
            requestForBinder(packageName);
        } catch (Throwable e) {
            System.out.println(Log.getStackTraceString(e));
            abort("Failed to request binder from Shizuku app");
        }

        Looper.loop();
    }

    private static void preExec(String[] args) throws InterruptedException {
        if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
            doExec(args);
        } else if (Shizuku.shouldShowRequestPermissionRationale()) {
            abort("Permission denied, please check Shizuku app");
        } else {
            verbose("Requesting permission...");

            Shizuku.addRequestPermissionResultListener(new Shizuku.OnRequestPermissionResultListener() {
                @Override
                public void onRequestPermissionResult(int requestCode, int grantResult) {
                    Shizuku.removeRequestPermissionResultListener(this);

                    if (grantResult == PackageManager.PERMISSION_GRANTED) {
                        try {
                            doExec(args);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        abort("Permission denied, please check Shizuku app");
                    }
                }
            });
            Shizuku.requestPermission(0);

        }
    }

    private static void doExec(String[] args) throws InterruptedException {
        Process process;
        InputStream in;
        InputStream err;
        OutputStream out;

        try {
            if (preserveEnvironment) {
                List<String> envList = new ArrayList<>();
                for (Map.Entry<String, String> entry : System.getenv().entrySet()) {
                    envList.add(entry.getKey() + "=" + entry.getValue());
                }
                String[] env = envList.toArray(new String[0]);
                String cwd = new File("").getAbsolutePath();

                verbose("cwd: " + cwd);
                verbose("env: " + Arrays.toString(env));

                verbose("Starting command " + args[0] + "...");
                process = Shizuku.newProcess(args, env, cwd);
            } else {
                verbose("Starting command " + args[0] + "...");
                process = Shizuku.newProcess(args, null, null);
            }

            in = process.getInputStream();
            err = process.getErrorStream();
            out = process.getOutputStream();
        } catch (Throwable e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
            abort(e.getMessage());
            return;
        }

        CountDownLatch latch = new CountDownLatch(2);

        new TransferThread(in, System.out, latch).start();
        new TransferThread(err, System.out, latch).start();
        new TransferThread(System.in, out, null).start();

        int exitCode = process.waitFor();
        latch.await();

        verbose("Command " + args[0] + " exited with " + exitCode);
        System.exit(exitCode);
    }

    private static class TransferThread extends Thread {

        private final InputStream input;
        private final OutputStream output;
        private final CountDownLatch latch;

        public TransferThread(InputStream input, OutputStream output, CountDownLatch latch) {
            this.input = input;
            this.output = output;
            this.latch = latch;
        }

        @Override
        public void run() {
            byte[] buf = new byte[8192];
            int len;
            try {
                while ((len = input.read(buf)) != -1) {
                    output.write(buf, 0, len);
                    output.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (latch != null) {
                latch.countDown();
            }
        }
    }

    private static void verbose(String message) {
        if (!verboseMessageAllowed) return;

        System.out.println("[ " + message + " ]");
        System.out.flush();
    }

    private static void abort(String message) {
        System.err.println("[ " + message + " ]");
        System.err.flush();
        System.exit(1);
    }

    private static void printHelp() {
        System.out.println("usage: shizuku [OPTION]... [CMD]...\n" +
                "Run command through Shizuku.\n\n" +
                "Options:\n" +
                "-h, --help               print this help\n" +
                "-v, --version            print the version of the shizuku tool\n" +
                "--verbose                print more messages\n" +
                "-m, -p,\n" +
                "--preserve-environment   preserve the entire environment\n" +
                "\n" +
                "This file can be used in adb shell or terminal apps.\n" +
                "For terminal apps, the environment variable SHIZUKU_APPLICATION_ID needs to be set to the first.");
    }
}
