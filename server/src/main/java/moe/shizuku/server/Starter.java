package moe.shizuku.server;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.os.Looper;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemService;
import android.system.Os;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import moe.shizuku.api.ShizukuApiConstants;
import moe.shizuku.server.api.Api;

import static moe.shizuku.server.utils.Logger.LOGGER;

public class Starter {

    private static void fixFilesOwner() {
        if (Process.myUid() == 0) {
            try {
                Os.chown("/data/local/tmp/shizuku_starter", 2000, 2000);
                //Os.chown("/data/local/tmp/shizuku/libhelper.so", 2000, 2000);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    private static void waitServiceManager() {
        try {
            SystemService.waitForState("servicemanager", SystemService.State.RUNNING, 1000);
        } catch (TimeoutException e) {
            LOGGER.w("waitForState timeout.");
        }
    }

    private static void waitSystemService(String name) {
        while (ServiceManager.getService(name) == null) {
            try {
                LOGGER.i("service " + name + " is not started, wait 1s.");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                LOGGER.w(e.getMessage(), e);
            }
        }
    }

    private static void checkManagerApp() {
        try {
            ApplicationInfo ai = Api.getApplicationInfo(ShizukuApiConstants.MANAGER_APPLICATION_ID, 0, 0);
            if (ai == null)
                System.exit(ServerConstants.MANAGER_APP_NOT_FOUND);
        } catch (Throwable tr) {
            LOGGER.e(tr, "checkManagerApp");
            System.exit(ServerConstants.MANAGER_APP_NOT_FOUND);
        }
    }

    private static void disableHiddenApiBlacklist() {
        try {
            java.lang.Process process = new ProcessBuilder(new String[]{"settings", "put", "global", "hidden_api_blacklist_exemptions", "*"}).start();

            int res;
            if ((res = process.waitFor()) == 0) {
                LOGGER.i("disabled hidden api blacklist");
            } else {
                LOGGER.w("failed to disable hidden api blacklist, res=" + res);
            }
        } catch (Throwable tr) {
            LOGGER.w("failed to disable hidden api blacklist", tr);
        }
    }

    private static UUID getToken(String[] args) {
        if (args.length > 0) {
            try {
                return UUID.fromString(args[0]);
            } catch (IllegalArgumentException ignored) {
            }
        }
        return null;
    }

    public static void main(String[] args) throws IOException, RemoteException, InterruptedException {
        fixFilesOwner();

        waitServiceManager();
        waitSystemService("package");
        waitSystemService("activity");
        waitSystemService(Context.USER_SERVICE);
        waitSystemService(Context.APP_OPS_SERVICE);

        checkManagerApp();

        if (Build.VERSION.SDK_INT >= 28) {
            disableHiddenApiBlacklist();
        }

        LOGGER.i("server v3");

        Looper.prepare();

        ShizukuService server = new ShizukuService(getToken(args));
        server.sendBinderToManager();
        server.sendBinderToClients();
        Looper.loop();

        LOGGER.i("server exit");
        System.exit(0);
    }
}
