package android.content.pm;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;

public interface IPackageInstallerCallback extends IInterface {

    void onSessionCreated(int sessionId)
            throws RemoteException;

    void onSessionBadgingChanged(int sessionId)
            throws RemoteException;

    void onSessionActiveChanged(int sessionId, boolean active)
            throws RemoteException;

    void onSessionProgressChanged(int sessionId, float progress)
            throws RemoteException;

    void onSessionFinished(int sessionId, boolean success)
            throws RemoteException;

    abstract class Stub extends Binder implements IPackageInstallerCallback {

        public static IPackageInstallerCallback asInterface(IBinder obj) {
            throw new RuntimeException("STUB");
        }

        @Override
        public IBinder asBinder() {
            throw new RuntimeException("STUB");
        }
    }
}
