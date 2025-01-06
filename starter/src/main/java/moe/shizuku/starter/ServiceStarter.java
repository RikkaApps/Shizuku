package moe.shizuku.starter;

import android.content.IContentProvider;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.util.Pair;

import java.util.Locale;

import moe.shizuku.api.BinderContainer;
import moe.shizuku.starter.util.IContentProviderCompat;
import rikka.hidden.compat.ActivityManagerApis;
import rikka.shizuku.ShizukuApiConstants;
import rikka.shizuku.server.UserService;

public class ServiceStarter {

    private static final String TAG = "ShizukuServiceStarter";

    private static final String EXTRA_BINDER = "moe.shizuku.privileged.api.intent.extra.BINDER";

    public static final String DEBUG_ARGS;

    static {
        int sdk = Build.VERSION.SDK_INT;
        if (sdk >= 30) {
            DEBUG_ARGS = "-Xcompiler-option" + " --debuggable" +
                    " -XjdwpProvider:adbconnection" +
                    " -XjdwpOptions:suspend=n,server=y";
        } else if (sdk >= 28) {
            DEBUG_ARGS = "-Xcompiler-option" + " --debuggable" +
                    " -XjdwpProvider:internal" +
                    " -XjdwpOptions:transport=dt_android_adb,suspend=n,server=y";
        } else {
            DEBUG_ARGS = "-Xcompiler-option" + " --debuggable" +
                    " -agentlib:jdwp=transport=dt_android_adb,suspend=n,server=y";
        }
    }

    private static final String USER_SERVICE_CMD_FORMAT = "(CLASSPATH='%s' %s%s /system/bin " +
            "--nice-name='%s' moe.shizuku.starter.ServiceStarter " +
            "--token='%s' --package='%s' --class='%s' --uid=%d%s)&";

    // DeathRecipient will automatically be unlinked when all references to the
    // binder is dropped, so we hold the reference here.
    @SuppressWarnings("FieldCanBeLocal")
    private static IBinder shizukuBinder;

    public static String commandForUserService(String appProcess, String managerApkPath, String token, String packageName, String classname, String processNameSuffix, int callingUid, boolean debug) {
        String processName = String.format("%s:%s", packageName, processNameSuffix);
        return String.format(Locale.ENGLISH, USER_SERVICE_CMD_FORMAT,
                managerApkPath, appProcess, debug ? (" " + DEBUG_ARGS) : "",
                processName,
                token, packageName, classname, callingUid, debug ? (" " + "--debug-name=" + processName) : "");
    }

    private static final int MAX_RETRIES = 50;
    private static final int RETRY_DELAY_MS = 200;
    private static Handler handler;

    public static void main(String[] args) {
        if (Looper.getMainLooper() == null) {
            Looper.prepareMainLooper();
        }
        handler = new Handler(Looper.getMainLooper());
        retryCount = 0;
        IBinder service;
        String token;

        UserService.setTag(TAG);
        Pair<IBinder, String> result = UserService.create(args);

        if (result == null) {
            System.exit(1);
            return;
        }

        service = result.first;
        token = result.second;

        sendBinder(service, token);

        Looper.loop();
        System.exit(0);

        Log.i(TAG, "service exited");
    }

    private static int retryCount;
    static String packageName = "moe.shizuku.privileged.api";
    static IContentProvider provider = null;

    private static void sendBinder(IBinder binder, String token) {
        String name = packageName + ".shizuku";
        int userId = 0;
        Runnable retryRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    provider = ActivityManagerApis.getContentProviderExternal(name, userId, null, name);
                    if (provider == null) {
                        retryCount++;
                        Log.w(TAG, String.format("provider is null %s %d,try times %d", name, userId, retryCount));
                        if (retryCount < MAX_RETRIES) {
                            handler.postDelayed(this, RETRY_DELAY_MS);
                        } else {
                            Log.e(TAG, String.format("provider is null %s %d", name, userId));
                            handler.removeCallbacks(this);
                            System.exit(1);
                        }
                    } else {
                        processProvider(provider,binder,token,packageName,userId,this);
                    }

                } catch (Throwable tr) {
                    Log.e(TAG, String.format("failed send binder to %s in user %d", packageName, userId), tr);
                    handler.removeCallbacks(this);
                    System.exit(1);
                } finally {
                    if (provider != null) {
                        try {
                            ActivityManagerApis.removeContentProviderExternal(name, null);
                        } catch (Throwable tr) {
                            Log.w(TAG, "removeContentProviderExternal", tr);
                        }
                    }
                }
            }
        };
        handler.post(retryRunnable);
    }
    private static boolean retryProviderPingBinder = true;
    private static void processProvider(IContentProvider provider, IBinder binder, String token, String packageName, int userId, Runnable retryRunnable) {
        String name = packageName + ".shizuku";
        if (!provider.asBinder().pingBinder()) {
            Log.e(TAG, String.format("provider is dead %s %d", name, userId));

            if (retryProviderPingBinder) {
                // For unknown reason, sometimes this could happens
                // Kill Shizuku app and try again could work
                ActivityManagerApis.forceStopPackageNoThrow(packageName, userId);
                Log.e(TAG, String.format("kill %s in user %d and try again", packageName, userId));
                handler.postDelayed(retryRunnable, 1000);
                retryProviderPingBinder = false;
                return;
            }
            handler.removeCallbacks(retryRunnable);
            System.exit(1);
        }

        if (!retryProviderPingBinder) {
            Log.e(TAG, "retry works");
        }

        Bundle extra = new Bundle();
        extra.putParcelable(EXTRA_BINDER, new BinderContainer(binder));
        extra.putString(ShizukuApiConstants.USER_SERVICE_ARG_TOKEN, token);

        Bundle reply = null;
        try {
            reply = IContentProviderCompat.call(provider, null, null, name, "sendUserService", null, extra);
        } catch (Throwable tr) {
            Log.e(TAG, String.format("failed send binder to %s in user %d", packageName, userId), tr);
            handler.removeCallbacks(retryRunnable);
            System.exit(1);
        }

        if (reply != null) {
            reply.setClassLoader(BinderContainer.class.getClassLoader());

            Log.i(TAG, String.format("send binder to %s in user %d", packageName, userId));
            BinderContainer container = reply.getParcelable(EXTRA_BINDER);

            if (container != null && container.binder != null && container.binder.pingBinder()) {
                shizukuBinder = container.binder;
                try {
                    shizukuBinder.linkToDeath(() -> {
                        Log.i(TAG, "exiting...");
                        handler.removeCallbacks(retryRunnable);
                        System.exit(0);
                    }, 0);
                } catch (Throwable tr) {
                    Log.e(TAG, String.format("failed send binder to %s in user %d", packageName, userId), tr);
                    handler.removeCallbacks(retryRunnable);
                    System.exit(1);
                }
                return;
            } else {
                Log.w(TAG, "server binder not received");
            }
        }
        handler.removeCallbacks(retryRunnable);
        System.exit(1);

    }
}
