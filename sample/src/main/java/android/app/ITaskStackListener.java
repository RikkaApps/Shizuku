package android.app;

import android.content.ComponentName;
import android.os.Binder;
import android.os.IInterface;
import android.os.RemoteException;

/**
 * Created by rikka on 2017/11/6.
 */

public interface ITaskStackListener extends IInterface {

    void onTaskStackChanged() throws RemoteException;

    void onActivityPinned(String var1, int var2) throws RemoteException;

    void onActivityUnpinned() throws RemoteException;

    void onPinnedActivityRestartAttempt(boolean var1) throws RemoteException;

    void onPinnedStackAnimationStarted() throws RemoteException;

    void onPinnedStackAnimationEnded() throws RemoteException;

    void onActivityForcedResizable(String var1, int var2, int var3) throws RemoteException;

    void onActivityDismissingDockedStack() throws RemoteException;

    void onActivityLaunchOnSecondaryDisplayFailed() throws RemoteException;

    void onTaskCreated(int var1, ComponentName var2) throws RemoteException;

    void onTaskRemoved(int var1) throws RemoteException;

    void onTaskMovedToFront(int var1) throws RemoteException;

    void onTaskDescriptionChanged(int var1, ActivityManager.TaskDescription var2) throws RemoteException;

    void onActivityRequestedOrientationChanged(int var1, int var2) throws RemoteException;

    void onTaskRemovalStarted(int var1) throws RemoteException;

    void onTaskProfileLocked(int var1, int var2) throws RemoteException;

    void onTaskSnapshotChanged(int var1, ActivityManager.TaskSnapshot var2) throws RemoteException;

    abstract class Stub extends Binder implements ITaskStackListener {

    }
}
