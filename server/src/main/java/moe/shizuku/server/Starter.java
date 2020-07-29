package moe.shizuku.server;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.ddm.DdmHandleAppName;
import android.os.ServiceManager;

import moe.shizuku.api.ShizukuApiConstants;
import moe.shizuku.server.api.SystemService;

import static moe.shizuku.server.utils.Logger.LOGGER;

public class Starter {

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
        ApplicationInfo ai = SystemService.getApplicationInfoNoThrow(ShizukuApiConstants.MANAGER_APPLICATION_ID, 0, 0);
        if (ai == null)
            System.exit(ServerConstants.MANAGER_APP_NOT_FOUND);
    }

    public static void main(String[] args) {
        if (args.length > 0) {
            for (String arg : args) {
                if (arg.equals("--debug")) {
                    DdmHandleAppName.setAppName("shizuku_server", 0);
                }
            }
        }

        waitSystemService("package");
        waitSystemService("activity");
        waitSystemService(Context.USER_SERVICE);
        waitSystemService(Context.APP_OPS_SERVICE);

        checkManagerApp();

        ShizukuService.main(args);
    }
}
