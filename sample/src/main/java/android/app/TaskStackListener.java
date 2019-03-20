package android.app;

import android.content.ComponentName;
import android.os.IBinder;
import android.os.RemoteException;

/**
 * Created by rikka on 2017/11/6.
 */

public class TaskStackListener extends ITaskStackListener.Stub {

    public void onTaskStackChanged() throws RemoteException {
    }

    public void onActivityPinned(String packageName, int taskId) throws RemoteException {
    }

    public void onActivityUnpinned() throws RemoteException {
    }

    public void onPinnedActivityRestartAttempt(boolean clearedTask) throws RemoteException {
    }

    public void onPinnedStackAnimationStarted() throws RemoteException {
    }

    public void onPinnedStackAnimationEnded() throws RemoteException {
    }

    public void onActivityForcedResizable(String packageName, int taskId, int reason) throws RemoteException {
    }

    public void onActivityDismissingDockedStack() throws RemoteException {
    }

    public void onActivityLaunchOnSecondaryDisplayFailed() throws RemoteException {
    }

    public void onTaskCreated(int taskId, ComponentName componentName) throws RemoteException {
    }

    public void onTaskRemoved(int taskId) throws RemoteException {
    }

    public void onTaskMovedToFront(int taskId) throws RemoteException {
    }

    public void onTaskRemovalStarted(int taskId) {
    }

    public void onTaskDescriptionChanged(int taskId, ActivityManager.TaskDescription td) throws RemoteException {
    }

    public void onActivityRequestedOrientationChanged(int taskId, int requestedOrientation) throws RemoteException {
    }

    public void onTaskProfileLocked(int taskId, int userId) {
    }

    public void onTaskSnapshotChanged(int taskId, ActivityManager.TaskSnapshot snapshot) throws RemoteException {
    }

    @Override
    public IBinder asBinder() {
        throw new UnsupportedOperationException();
    }
}
