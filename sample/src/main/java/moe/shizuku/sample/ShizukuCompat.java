package moe.shizuku.sample;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.Process;
import android.support.annotation.WorkerThread;

import java.util.List;

import moe.shizuku.api.ShizukuActivityManagerV22;
import moe.shizuku.api.ShizukuActivityManagerV26;
import moe.shizuku.api.ShizukuAppOpsServiceV26;
import moe.shizuku.api.ShizukuPackageManagerV26;
import moe.shizuku.lang.ShizukuRemoteException;

/**
 * Created by rikka on 2017/10/19.
 */

@WorkerThread
public class ShizukuCompat {

    public static void broadcastIntent(Intent intent) throws RuntimeException {
        if (Build.VERSION.SDK_INT < 23) {
            ShizukuActivityManagerV22.broadcastIntent(
                    null, intent, null, null, 0, null, null, null,-1, true, false, Process.myUserHandle().hashCode());
        } else {
            ShizukuActivityManagerV26.broadcastIntent(
                    null, intent, null, null, 0, null, null, null,-1, null, true, false, Process.myUserHandle().hashCode());
        }
    }

    public static List getOpsForPackage(int uid, String packageName, int[] ops) throws RuntimeException {
        return ShizukuAppOpsServiceV26.getOpsForPackage(Process.myUid(), BuildConfig.APPLICATION_ID, null);
    }

    public static List<PackageInfo> getInstalledPackages(int flags, int userId) throws RuntimeException {
        return ShizukuPackageManagerV26.getInstalledPackages(flags, userId);
    }
}
