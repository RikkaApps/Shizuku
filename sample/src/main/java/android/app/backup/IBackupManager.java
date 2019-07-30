package android.app.backup;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.ParcelFileDescriptor;

public interface IBackupManager extends IInterface {

    void adbBackup(int userId, ParcelFileDescriptor fd, boolean includeApks, boolean includeObbs,
                   boolean includeShared, boolean doWidgets, boolean allApps,
                   boolean allIncludesSystem, boolean doCompress, boolean doKeyValue,
                   String[] packageNames) throws android.os.RemoteException;

    void adbRestore(int userId, ParcelFileDescriptor fd) throws android.os.RemoteException;

    void adbBackup(ParcelFileDescriptor fd, boolean includeApks, boolean includeObbs,
                   boolean includeShared, boolean doWidgets, boolean allApps,
                   boolean allIncludesSystem, boolean doCompress, boolean doKeyValue,
                   String[] packageNames) throws android.os.RemoteException;

    void adbRestore(ParcelFileDescriptor fd) throws android.os.RemoteException;

    abstract class Stub extends Binder implements IBackupManager {

        public static IBackupManager asInterface(IBinder obj) {
            throw new UnsupportedOperationException();
        }
    }
}
