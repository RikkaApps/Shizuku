package moe.shizuku.server;

import android.app.ITaskStackListener;
import android.os.Parcel;
import android.os.RemoteException;

/**
 * Created by Rikka on 2017/5/8.
 */

public class HideApiOverrideO {

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
        };
    }
}
