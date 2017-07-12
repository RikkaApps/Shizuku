package android.app;

import android.os.Binder;
import android.os.RemoteException;

/**
 * Created by Rikka on 2017/5/7.
 */

public interface ITaskStackListener {

    /** Called whenever there are changes to the state of tasks in a stack. */
    void onTaskStackChanged() throws RemoteException;

    /** Called whenever an Activity is moved to the pinned stack from another stack. */
    void onActivityPinned() throws RemoteException;

    /**
     * Called whenever IActivityManager.startActivity is called on an activity that is already
     * running in the pinned stack and the activity is not actually started, but the task is either
     * brought to the front or a new Intent is delivered to it.
     */
    void onPinnedActivityRestartAttempt() throws RemoteException;

    /**
     * Called whenever the pinned stack is done animating a resize.
     */
    void onPinnedStackAnimationEnded() throws RemoteException;

    /**
     * Called when we launched an activity that we forced to be resizable.
     */
    void onActivityForcedResizable(String packageName, int taskId) throws RemoteException;

    /**
     * Callen when we launched an activity that is dismissed the docked stack.
     */
    void onActivityDismissingDockedStack() throws RemoteException;

    abstract class Stub extends Binder implements ITaskStackListener {

    }
}
