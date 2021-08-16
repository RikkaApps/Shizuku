package moe.shizuku.server;

import static moe.shizuku.server.utils.Logger.LOGGER;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.RemoteException;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import $android.app.ActivityManager;
import kotlin.collections.ArraysKt;
import rikka.shizuku.server.api.ProcessObserverAdapter;
import rikka.shizuku.server.api.UidObserverAdapter;
import rikka.shizuku.service.api.SystemService;

public class BinderSender {

    private static final String PERMISSION_MANAGER = "moe.shizuku.manager.permission.MANAGER";
    private static final String PERMISSION = "moe.shizuku.manager.permission.API_V23";

    private static final List<Integer> PID_LIST = new ArrayList<>();

    private static ShizukuService sShizukuService;

    private static class ProcessObserver extends ProcessObserverAdapter {

        @Override
        public void onForegroundActivitiesChanged(int pid, int uid, boolean foregroundActivities) throws RemoteException {
            LOGGER.d("onForegroundActivitiesChanged: pid=%d, uid=%d, foregroundActivities=%s", pid, uid, foregroundActivities ? "true" : "false");

            if (PID_LIST.contains(pid) || !foregroundActivities) {
                return;
            }
            PID_LIST.add(pid);

            onActive(uid, pid);
        }

        @Override
        public void onProcessDied(int pid, int uid) {
            LOGGER.d("onProcessDied: pid=%d, uid=%d", pid, uid);

            int index = PID_LIST.indexOf(pid);
            if (index != -1) {
                PID_LIST.remove(index);
            }
        }

        @Override
        public void onProcessStateChanged(int pid, int uid, int procState) throws RemoteException {
            LOGGER.d("onProcessStateChanged: pid=%d, uid=%d, procState=%d", pid, uid, procState);

            if (PID_LIST.contains(pid)) {
                return;
            }
            PID_LIST.add(pid);

            onActive(uid, pid);
        }
    }

    private static class UidObserver extends UidObserverAdapter {

        @Override
        public void onUidActive(int uid) throws RemoteException {
            LOGGER.d("onUidActive: uid=%d", uid);

            onActive(uid);
        }
    }

    private static void onActive(int uid) throws RemoteException {
        onActive(uid, -1);
    }

    private static void onActive(int uid, int pid) throws RemoteException {
        List<String> packages = SystemService.getPackagesForUidNoThrow(uid);
        if (packages.isEmpty())
            return;

        LOGGER.d("onActive: uid=%d, packages=%s", uid, TextUtils.join(", ", packages));

        int userId = uid / 100000;
        for (String packageName : packages) {
            PackageInfo pi = SystemService.getPackageInfoNoThrow(packageName, PackageManager.GET_PERMISSIONS, userId);
            if (pi == null || pi.requestedPermissions == null)
                continue;

            if (ArraysKt.contains(pi.requestedPermissions, PERMISSION_MANAGER)) {
                boolean granted;
                if (pid == -1)
                    granted = SystemService.checkPermission(PERMISSION_MANAGER, uid) == PackageManager.PERMISSION_GRANTED;
                else
                    granted = SystemService.checkPermission(PERMISSION_MANAGER, pid, uid) == PackageManager.PERMISSION_GRANTED;

                if (granted) {
                    ShizukuService.sendBinderToManger(sShizukuService, userId);
                    return;
                }
            } else if (ArraysKt.contains(pi.requestedPermissions, PERMISSION)) {
                ShizukuService.sendBinderToUserApp(sShizukuService, packageName, userId);
                return;
            }
        }
    }

    public static void register(ShizukuService shizukuService) {
        sShizukuService = shizukuService;

        try {
            SystemService.registerProcessObserver(new ProcessObserver());
        } catch (Throwable tr) {
            LOGGER.e(tr, "registerProcessObserver");
        }

        if (Build.VERSION.SDK_INT >= 26) {
            try {
                SystemService.registerUidObserver(new UidObserver(),
                        ActivityManager.UID_OBSERVER_ACTIVE,
                        ActivityManager.PROCESS_STATE_UNKNOWN,
                        null);
            } catch (Throwable tr) {
                LOGGER.e(tr, "registerUidObserver");
            }
        }
    }
}
