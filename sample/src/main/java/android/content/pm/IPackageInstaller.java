package android.content.pm;

import android.content.IntentSender;
import android.graphics.Bitmap;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;

import androidx.annotation.RequiresApi;

import java.util.List;

public interface IPackageInstaller extends IInterface {

    int createSession(PackageInstaller.SessionParams params, String installerPackageName, int userId)
            throws RemoteException;

    void updateSessionAppIcon(int sessionId, Bitmap appIcon)
            throws RemoteException;

    void updateSessionAppLabel(int sessionId, String appLabel)
            throws RemoteException;

    void abandonSession(int sessionId)
            throws RemoteException;

    IPackageInstallerSession openSession(int sessionId)
            throws RemoteException;

    PackageInstaller.SessionInfo getSessionInfo(int sessionId)
            throws RemoteException;

    ParceledListSlice<PackageInstaller.SessionInfo> getAllSessions(int userId)
            throws RemoteException;

    ParceledListSlice<PackageInstaller.SessionInfo> getMySessions(String installerPackageName, int userId)
            throws RemoteException;

    @RequiresApi(29)
    ParceledListSlice<PackageInstaller.SessionInfo> getStagedSessions()
            throws RemoteException;

    void registerCallback(IPackageInstallerCallback callback, int userId)
            throws RemoteException;

    void unregisterCallback(IPackageInstallerCallback callback)
            throws RemoteException;

    // removed from 26
    void uninstall(String packageName, String callerPackageName, int flags,
                   IntentSender statusReceiver, int userId);

    @RequiresApi(26)
    void uninstall(VersionedPackage versionedPackage, String callerPackageName, int flags,
                   IntentSender statusReceiver, int userId)
            throws RemoteException;

    @RequiresApi(29)
    void installExistingPackage(String packageName, int installFlags, int installReason,
                                IntentSender statusReceiver, int userId, List<String> whiteListedPermissions)
            throws RemoteException;

    void setPermissionsResult(int sessionId, boolean accepted)
            throws RemoteException;

    abstract class Stub extends Binder implements IPackageInstaller {

        public static IPackageInstaller asInterface(IBinder binder) {
            throw new UnsupportedOperationException();
        }
    }
}
