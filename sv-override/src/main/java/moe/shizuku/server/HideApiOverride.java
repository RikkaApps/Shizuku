package moe.shizuku.server;

import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.app.ITaskStackListener;
import android.content.ComponentName;
import android.content.pm.UserInfo;
import android.os.Parcel;
import android.os.Process;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class HideApiOverride {

    private static final String TAG = "RServer";

    public static final int OP_NONE = getOpNone();

    public static boolean isRoot(int uid) {
        return uid == Process.ROOT_UID;
    }

    private static int getOpNone() {
        try {
            return AppOpsManager.OP_NONE;
        } catch (LinkageError e) {
            Log.w(TAG, "Can't find AppOpsManager.OP_NONE", e);
            return -1;
        }
    }

    @SuppressWarnings("unchecked")
    public static List<hidden.android.app.AppOpsManager.PackageOps> convertPackageOps(List from, boolean m) {
        List<hidden.android.app.AppOpsManager.PackageOps> to = new ArrayList<>();

        if (from == null || from.isEmpty()) {
            return to;
        }

        for (AppOpsManager.PackageOps packageOps : (List<AppOpsManager.PackageOps>) from) {
            hidden.android.app.AppOpsManager.PackageOps toOps = new hidden.android.app.AppOpsManager.PackageOps(
                            packageOps.getPackageName(),
                            packageOps.getUid(),
                            new ArrayList<hidden.android.app.AppOpsManager.OpEntry>());
            to.add(toOps);
            for (AppOpsManager.OpEntry op : packageOps.getOps()) {
                hidden.android.app.AppOpsManager.OpEntry toOp;
                if (m) {
                    toOp = new hidden.android.app.AppOpsManager.OpEntry(
                            op.getOp(),
                            op.getMode(),
                            op.getTime(),
                            op.getRejectTime(),
                            op.getDuration(),
                            op.getProxyUid(),
                            op.getProxyPackageName());
                } else {
                    toOp = new hidden.android.app.AppOpsManager.OpEntry(
                            op.getOp(),
                            op.getMode(),
                            op.getTime(),
                            op.getRejectTime(),
                            op.getDuration());
                }
                toOps.getOps().add(toOp);
            }
        }
        return to;
    }

    @SuppressWarnings("unchecked")
    public static List<hidden.android.content.pm.UserInfo> convertUserInfo(List from) {
        List<hidden.android.content.pm.UserInfo> to = new ArrayList<>();

        for (UserInfo userInfo : (List<UserInfo>) from) {
            to.add(new hidden.android.content.pm.UserInfo(
                    userInfo.id,
                    userInfo.serialNumber,
                    userInfo.name,
                    userInfo.isPrimary(),
                    userInfo.isAdmin(),
                    userInfo.isGuest(),
                    userInfo.isRestricted(),
                    userInfo.isManagedProfile(),
                    userInfo.isEnabled(),
                    userInfo.getUserHandle()
            ));
        }
        return to;
    }

    public static int getIdentifier(UserHandle userHandle) {
        return userHandle.getIdentifier();
    }

    public static ITaskStackListener.Stub createTaskStackListener(final Runnable r) throws RemoteException {
        return new ITaskStackListener.Stub() {

            @Override
            protected boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
                try {
                    return super.onTransact(code, data, reply, flags);
                } catch (AbstractMethodError ignored) {
                    return true;
                }
            }

            @Override
            public void onTaskStackChanged() throws RemoteException {
                r.run();
            }

            @Override
            public void onActivityPinned() throws RemoteException {

            }

            @Override
            public void onPinnedActivityRestartAttempt() throws RemoteException {

            }

            @Override
            public void onPinnedStackAnimationEnded() throws RemoteException {

            }

            @Override
            public void onActivityForcedResizable(String packageName, int taskId) throws RemoteException {

            }

            @Override
            public void onActivityDismissingDockedStack() throws RemoteException {

            }

            @Override
            public void onTaskRemovalStarted(int taskId) throws RemoteException {

            }

            @Override
            public void onTaskMovedToFront(int taskId) throws RemoteException {

            }

            @Override
            public void onTaskDescriptionChanged(int taskId, ActivityManager.TaskDescription taskDescription) throws RemoteException {

            }

            @Override
            public void onTaskSnapshotChanged(int taskId, ActivityManager.TaskSnapshot taskSnapshot) throws RemoteException {

            }

            @Override
            public void onTaskCreated(int taskId, ComponentName componentName) throws RemoteException {

            }

            @Override
            public void onTaskRemoved(int taskId) throws RemoteException {

            }

            @Override
            public void onActivityRequestedOrientationChanged(int a, int b) throws RemoteException {

            }
        };
    }
}
