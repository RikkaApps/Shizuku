package moe.shizuku.starter;

import android.content.Context;
import android.content.IContentProvider;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.os.UserHandle;
import android.system.Os;
import android.util.Log;

import hidden.HiddenApiBridge;
import moe.shizuku.api.BinderContainer;
import moe.shizuku.api.ShizukuApiConstants;
import moe.shizuku.starter.api.SystemService;
import moe.shizuku.starter.ktx.IContentProviderKt;

public class ServiceStarter {

    private static final String TAG = "ShizukuServiceStarter";

    public static void main(String[] args) {
        String token = args[0];
        String packageName = args[1];
        String classname = args[2];
        int uid = Integer.parseInt(args[3]);
        int appId = uid % 100000;
        int userId = uid / 100000;

        Log.i(TAG, String.format("starting service %s/%s...", packageName, classname));

        Looper.prepare();

        IBinder service = null;
        Context systemContext = HiddenApiBridge.getSystemContext();
        try {
            UserHandle userHandle = HiddenApiBridge.createUserHandle(userId);
            Context context = HiddenApiBridge.Context_createPackageContextAsUser(systemContext, packageName, Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY, userHandle);
            ClassLoader classLoader = context.getClassLoader();
            Class<?> serviceClass = classLoader.loadClass(classname);
            service = (IBinder) serviceClass.newInstance();
        } catch (Throwable tr) {
            Log.w(TAG, String.format("unable to start service %s/%s...", packageName, classname), tr);
            System.exit(1);
        }

        if (!sendBinder(service, token)) {
            System.exit(1);
        }

        Looper.loop();
        System.exit(0);

        Log.i(TAG, String.format("service %s/%s exited", packageName, classname));
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
            extra.putParcelable(ShizukuApiConstants.EXTRA_BINDER, new BinderContainer(binder));
            extra.putString(ShizukuApiConstants.USER_SERVICE_ARG_TOKEN, token);

            Bundle reply = IContentProviderKt.callCompat(provider, null, null, name, "sendUserService", null, extra);

            if (reply != null) {
                reply.setClassLoader(BinderContainer.class.getClassLoader());

                Log.i(TAG, String.format("send binder to %s in user %d", packageName, userId));
                BinderContainer container = reply.getParcelable(ShizukuApiConstants.EXTRA_BINDER);

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
