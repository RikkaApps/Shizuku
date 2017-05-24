package android.app;

import android.content.IIntentReceiver;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.RequiresApi;

import java.util.List;

/**
 * Created by Rikka on 2017/5/5.
 */

public interface IActivityManager {

    int startActivity(IApplicationThread caller, String callingPackage, Intent intent,
                      String resolvedType, IBinder resultTo, String resultWho, int requestCode, int flags,
                      ProfilerInfo profilerInfo, Bundle options) throws RemoteException;

    int startActivityAsUser(IApplicationThread caller, String callingPackage, Intent intent,
                            String resolvedType, IBinder resultTo, String resultWho, int requestCode, int flags,
                            ProfilerInfo profilerInfo, Bundle options, int userId) throws RemoteException;

    /* mAm.startActivityAsUser(null, null, intent, mimeType,
                        null, null, 0, mStartFlags, profilerInfo,
                        options != null ? options.toBundle() : null, mUserId);*/

    int broadcastIntent(IApplicationThread caller, Intent intent,
                        String resolvedType, IIntentReceiver resultTo, int resultCode,
                        String resultData, Bundle map, String requiredPermissions,
                        int appOp, boolean serialized, boolean sticky, int userId) throws RemoteException;

    @RequiresApi(Build.VERSION_CODES.M)
    int broadcastIntent(IApplicationThread caller, Intent intent,
                        String resolvedType, IIntentReceiver resultTo, int resultCode,
                        String resultData, Bundle map, String[] requiredPermissions,
                        int appOp, Bundle options, boolean serialized, boolean sticky, int userId) throws RemoteException;

    List<ActivityManager.RunningTaskInfo> getTasks(int maxNum, int flags) throws RemoteException;

    void registerTaskStackListener(ITaskStackListener listener) throws RemoteException;

    String getProviderMimeType(Uri uri, int userId) throws RemoteException;

    void forceStopPackage(final String packageName, int userId) throws RemoteException;
}
