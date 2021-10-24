package moe.shizuku.starter;

import android.app.ActivityThread;
import android.content.Context;
import android.content.ContextHidden;
import android.content.IContentProvider;
import android.ddm.DdmHandleAppName;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.os.UserHandle;
import android.os.UserHandleHidden;
import android.util.Log;

import java.util.Locale;

import dev.rikka.tools.refine.Refine;
import moe.shizuku.api.BinderContainer;
import moe.shizuku.starter.ktx.IContentProviderKt;
import rikka.shizuku.ShizukuApiConstants;
import rikka.shizuku.server.api.SystemService;

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

    private static final String USER_SERVICE_CMD_FORMAT = "(CLASSPATH=%s %s%s /system/bin " +
            "--nice-name=%s moe.shizuku.starter.ServiceStarter " +
            "--token=%s --package=%s --class=%s --uid=%d%s)&";

    public static String commandForUserService(String appProcess, String managerApkPath, String token, String packageName, String classname, String processNameSuffix, int callingUid, boolean debug) {
        String processName = String.format("%s:%s", packageName, processNameSuffix);
        return String.format(Locale.ENGLISH, USER_SERVICE_CMD_FORMAT,
                managerApkPath, appProcess, debug ? (" " + DEBUG_ARGS) : "",
                processName,
                token, packageName, classname, callingUid, debug ? (" " + "--debug-name=" + processName) : "");
    }

    public static void main(String[] args) {
        String name = null;
        String token = null;
        String pkg = null;
        String cls = null;
        int uid = -1;

        for (String arg : args) {
            if (arg.startsWith("--debug-name=")) {
                name = arg.substring(13);
            } else if (arg.startsWith("--token=")) {
                token = arg.substring(8);
            } else if (arg.startsWith("--package=")) {
                pkg = arg.substring(10);
            } else if (arg.startsWith("--class=")) {
                cls = arg.substring(8);
            } else if (arg.startsWith("--uid=")) {
                uid = Integer.parseInt(arg.substring(6));
            }
        }

        int userId = uid / 100000;

        Log.i(TAG, String.format("starting service %s/%s...", pkg, cls));

        if (Looper.getMainLooper() == null) {
            Looper.prepareMainLooper();
        }

        IBinder service = null;
        Context systemContext = ActivityThread.systemMain().getSystemContext();

        DdmHandleAppName.setAppName(name != null ? name : "shizuku_user_service", 0);

        try {
            UserHandle userHandle = Refine.unsafeCast(
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                            ? UserHandleHidden.of(userId)
                            : new UserHandleHidden(userId));
            Context context = Refine.<ContextHidden>unsafeCast(systemContext).createPackageContextAsUser(pkg, Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY, userHandle);
            ClassLoader classLoader = context.getClassLoader();
            Class<?> serviceClass = classLoader.loadClass(cls);
            service = (IBinder) serviceClass.newInstance();
        } catch (Throwable tr) {
            Log.w(TAG, String.format("unable to start service %s/%s...", pkg, cls), tr);
            System.exit(1);
        }

        if (!sendBinder(service, token)) {
            System.exit(1);
        }

        Looper.loop();
        System.exit(0);

        Log.i(TAG, String.format("service %s/%s exited", pkg, cls));
    }

    private static boolean sendBinder(IBinder binder, String token) {
        String packageName = "moe.shizuku.privileged.api";
        String name = packageName + ".shizuku";
        int userId = 0;
        IContentProvider provider = null;

        try {
            provider = SystemService.getContentProviderExternal(name, userId, null, name);
            if (provider == null) {
                Log.e(TAG, String.format("provider is null %s %d", name, userId));
                return false;
            }

            Bundle extra = new Bundle();
            extra.putParcelable(EXTRA_BINDER, new BinderContainer(binder));
            extra.putString(ShizukuApiConstants.USER_SERVICE_ARG_TOKEN, token);

            Bundle reply = IContentProviderKt.callCompat(provider, null, null, name, "sendUserService", null, extra);

            if (reply != null) {
                reply.setClassLoader(BinderContainer.class.getClassLoader());

                Log.i(TAG, String.format("send binder to %s in user %d", packageName, userId));
                BinderContainer container = reply.getParcelable(EXTRA_BINDER);

                if (container != null && container.binder != null && container.binder.pingBinder()) {
                    container.binder.linkToDeath(() -> {
                        Log.i(TAG, "exiting...");
                        System.exit(0);
                    }, 0);
                    return true;
                } else {
                    Log.w(TAG, "server binder not received");
                }
            }

            return false;
        } catch (Throwable tr) {
            Log.e(TAG, String.format("failed send binder to %s in user %d", packageName, userId), tr);
            return false;
        } finally {
            if (provider != null) {
                try {
                    SystemService.removeContentProviderExternal(name, null);
                } catch (Throwable tr) {
                    Log.w(TAG, "removeContentProviderExternal", tr);
                }
            }
        }
    }
}
