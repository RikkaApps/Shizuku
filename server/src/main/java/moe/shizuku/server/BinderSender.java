package moe.shizuku.server;

import android.app.ActivityManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.RemoteException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import moe.shizuku.api.ShizukuApiConstants;
import moe.shizuku.server.api.Api;
import moe.shizuku.server.utils.ArrayUtils;
import moe.shizuku.server.utils.BuildUtils;

import static moe.shizuku.server.utils.Logger.LOGGER;

public class BinderSender {

    private static final String PERMISSION_MANAGER = "moe.shizuku.manager.permission.MANAGER";
    private static final String PERMISSION = BuildUtils.isPreM() ? ShizukuApiConstants.PERMISSION_PRE_23 : ShizukuApiConstants.PERMISSION;

    private static final List<Integer> PID_LIST = new ArrayList<>();

    private static ShizukuService sShizukuService;

    private static class ProcessObserver extends moe.shizuku.server.api.ProcessObserver {

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

                ShizukuService.getPidToken().remove(pid);
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

    private static class UidObserver extends moe.shizuku.server.api.UidObserver {

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
        String[] packages = Api.getPackagesForUid(uid);
        if (packages == null)
            return;

        LOGGER.d("onActive: uid=%d, packages=%s", uid, Arrays.toString(packages));

        int userId = uid / 100000;
        for (String packageName : packages) {
            PackageInfo pi = Api.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS, userId);
            if (pi == null || pi.requestedPermissions == null)
                continue;

            if (ArrayUtils.contains(pi.requestedPermissions, PERMISSION_MANAGER)) {
                boolean granted;
                if (pid == -1)
                    granted = Api.checkPermission(PERMISSION_MANAGER, uid) == PackageManager.PERMISSION_GRANTED;
                else
                    granted = Api.checkPermission(PERMISSION_MANAGER, pid, uid) == PackageManager.PERMISSION_GRANTED;

                if (granted) {
                    ShizukuService.sendBinderToManger(sShizukuService, userId);
                    return;
                }
            } else if (ArrayUtils.contains(pi.requestedPermissions, PERMISSION)) {
                ShizukuService.sendBinderToUserApp(sShizukuService, packageName, userId);
                return;
            }
        }
    }

    public static void register(ShizukuService shizukuService) throws RemoteException {
        sShizukuService = shizukuService;

        Api.registerProcessObserver(new ProcessObserver());

        if (Build.VERSION.SDK_INT >= 26) {
            Api.registerUidObserver(new UidObserver(),
                    ActivityManager.UID_OBSERVER_ACTIVE,
                    ActivityManager.PROCESS_STATE_UNKNOWN,
                    null);
        }
    }
}
