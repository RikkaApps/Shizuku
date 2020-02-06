package hidden;

import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.content.pm.PackageInfo;
import android.os.RemoteException;
import android.os.UserHandle;

import androidx.annotation.RequiresApi;

import com.android.internal.app.IAppOpsService;

import java.util.List;

public class HiddenApiBridge {

    public static final int ActivityManager_UID_OBSERVER_ACTIVE = ActivityManager.UID_OBSERVER_ACTIVE;

    public static final int ActivityManager_PROCESS_STATE_UNKNOWN = ActivityManager.PROCESS_STATE_UNKNOWN;

    public static UserHandle createUserHandle(int userId) {
        return new UserHandle(userId);
    }

    public static int UserHandle_getIdentifier(UserHandle userHandle) {
        return userHandle.getIdentifier();
    }

    public static List<?> getOpsForPackage(IAppOpsService appOpsService, int uid, String packageName, int[] ops) throws RemoteException {
        return appOpsService.getOpsForPackage(uid, packageName, ops);
    }

    @RequiresApi(26)
    public static List<?> getUidOps(IAppOpsService appOpsService, int uid, int[] ops) throws RemoteException {
        return appOpsService.getUidOps(uid, ops);
    }

    public static int permissionToOpCode(String permission) {
        return AppOpsManager.permissionToOpCode(permission);
    }

    public static List<?> PackageOps_getOps(Object _packageOps) {
        AppOpsManager.PackageOps packageOps = (AppOpsManager.PackageOps) _packageOps;
        return packageOps.getOps();
    }

    public static int OpEntry_getMode(Object _opEntry) {
        AppOpsManager.OpEntry opEntry = (AppOpsManager.OpEntry) _opEntry;
        return opEntry.getMode();
    }

    public static int ActivityManager_RunningAppProcessInfo_procStateToImportance(int procState) {
        return ActivityManager.RunningAppProcessInfo.procStateToImportance(procState);
    }

    public static String PackageInfo_overlayTarget(PackageInfo packageInfo) {
        return packageInfo.overlayTarget;
    }

}
