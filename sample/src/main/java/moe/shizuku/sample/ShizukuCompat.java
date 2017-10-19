package moe.shizuku.sample;

import android.content.Intent;
import android.os.Build;
import android.os.Process;
import android.support.annotation.WorkerThread;

import java.util.List;

import moe.shizuku.api.ShizukuActivityManagerV21;
import moe.shizuku.api.ShizukuActivityManagerV26;
import moe.shizuku.api.ShizukuAppOpsServiceV21;
import moe.shizuku.api.ShizukuAppOpsServiceV26;
import moe.shizuku.lang.ShizukuRemoteException;

/**
 * Created by rikka on 2017/10/19.
 */

@WorkerThread
public class ShizukuCompat {

    public static void broadcastIntent(Intent intent) {
        if (Build.VERSION.SDK_INT == 21) {
            ShizukuActivityManagerV21.broadcastIntent(
                    null, intent, null, null, 0, null, null, null,-1, true, false, Process.myUserHandle().hashCode());
        } else {
            ShizukuActivityManagerV26.broadcastIntent(
                    null, intent, null, null, 0, null, null, null,-1, null, true, false, Process.myUserHandle().hashCode());
        }
    }

    public static List getOpsForPackage(int uid, String packageName, int[] ops) throws ShizukuRemoteException {
        if (Build.VERSION.SDK_INT == 21) {
            return ShizukuAppOpsServiceV21.getOpsForPackage(Process.myUid(), BuildConfig.APPLICATION_ID, null);
        } else {
            return ShizukuAppOpsServiceV26.getOpsForPackage(Process.myUid(), BuildConfig.APPLICATION_ID, null);
        }
    }
}
