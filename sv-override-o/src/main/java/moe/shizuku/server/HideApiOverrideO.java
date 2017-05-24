package moe.shizuku.server;

import android.app.ITaskStackListener;
import android.os.RemoteException;

/**
 * Created by Rikka on 2017/5/8.
 */

public class HideApiOverrideO {

    public static ITaskStackListener.Stub createTaskStackListener(final Runnable r) throws RemoteException {
        return new ITaskStackListener.Stub() {

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
        };
    }
}
