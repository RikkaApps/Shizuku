package rikka.shizuku.shell;

import android.app.IActivityManager;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.system.Os;
import android.text.TextUtils;

import java.io.File;

import dalvik.system.DexClassLoader;
import hidden.HiddenApiBridgeV23;

public class ShizukuShellLoader {

    private static String[] args;
    private static String callingPackage;
    private static Handler handler;

    private static void requestForBinder() throws RemoteException {
        Binder binder = new Binder() {
            @Override
            protected boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
                if (code == 1) {
                    IBinder binder = data.readStrongBinder();

                    String sourceDir = data.readString();
                    if (binder != null) {
                            handler.post(() -> onBinderReceived(binder, sourceDir));
                     } else {

                    }
                    return true;
                }
                return super.onTransact(code, data, reply, flags);
            }
        };

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

        IBinder amBinder = ServiceManager.getService("activity");
        IActivityManager am;
        if (Build.VERSION.SDK_INT >= 26) {
            am = IActivityManager.Stub.asInterface(amBinder);
        } else {
            am = HiddenApiBridgeV23.ActivityManagerNative_asInterface(amBinder);
        }

        am.startActivityAsUser(null, callingPackage, intent, null, null, null, 0, 0, null, null, Os.getuid() / 100000);
    }

    private static void onBinderReceived(IBinder binder, String sourceDir) {
        String librarySearchPath = sourceDir + "!/lib/" + Build.SUPPORTED_ABIS[0];
        String systemLibrarySearchPath = System.getProperty("java.library.path");
        if (!TextUtils.isEmpty(systemLibrarySearchPath)) {
            librarySearchPath += File.pathSeparatorChar + systemLibrarySearchPath;
        }

        try {
            DexClassLoader classLoader = new DexClassLoader(sourceDir, ".", librarySearchPath, ClassLoader.getSystemClassLoader());
            Class<?> cls = classLoader.loadClass("moe.shizuku.manager.shell.Shell");
            cls.getDeclaredMethod("main", String[].class, String.class, IBinder.class, Handler.class)
                    .invoke(null, args, callingPackage, binder, handler);
        } catch (Throwable tr) {
            tr.printStackTrace(System.err);
            System.err.flush();
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        ShizukuShellLoader.args = args;

        String packageName;
        if (Os.getuid() == 2000) {
            packageName = "com.android.shell";
        } else {
            packageName = System.getenv("BSH_APPLICATION_ID");
            if (TextUtils.isEmpty(packageName) || "PKG".equals(packageName)) {
                abort("BSH_APPLICATION_ID is not set, set this environment variable to the id of current application (package name)");
                System.exit(1);
            }
        }

        ShizukuShellLoader.callingPackage = packageName;

        if (Looper.getMainLooper() == null) {
            Looper.prepareMainLooper();
        }

        handler = new Handler(Looper.getMainLooper());

        try {
            requestForBinder();
        } catch (Throwable tr) {
            tr.printStackTrace(System.err);
            System.err.flush();
            System.exit(1);
        }

        Looper.loop();
        System.exit(0);
    }

    private static void abort(String message) {
        System.err.println(message);
        System.err.flush();
        System.exit(1);
    }
}
