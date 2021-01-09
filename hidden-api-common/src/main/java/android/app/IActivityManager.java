package android.app;

import android.content.IIntentReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;

import androidx.annotation.RequiresApi;

public interface IActivityManager extends IInterface {

    @RequiresApi(29)
    ContentProviderHolder getContentProviderExternal(String name, int userId, IBinder token, String tag)
            throws RemoteException;

    @RequiresApi(26)
    ContentProviderHolder getContentProviderExternal(String name, int userId, IBinder token)
            throws RemoteException;

    void removeContentProviderExternal(String name, IBinder token)
            throws RemoteException;

    int checkPermission(String permission, int pid, int uid)
            throws RemoteException;

    void registerProcessObserver(IProcessObserver observer)
            throws RemoteException;

    void registerUidObserver(IUidObserver observer, int which, int cutpoint, String callingPackage)
            throws RemoteException;

    void forceStopPackage(String packageName, int userId)
            throws RemoteException;

    int startActivityAsUser(IApplicationThread caller, String callingPackage,
                            Intent intent, String resolvedType, IBinder resultTo, String resultWho,
                            int requestCode, int flags, ProfilerInfo profilerInfo,
                            Bundle options, int userId)
            throws RemoteException;

    @RequiresApi(26)
    abstract class Stub extends Binder implements IActivityManager {

        public static IActivityManager asInterface(IBinder obj) {
            throw new RuntimeException("STUB");
        }
    }
}
