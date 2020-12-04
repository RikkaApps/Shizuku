package moe.shizuku.api;

import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.FileDescriptor;
import java.util.Objects;

/**
 * Binder wrapper to use ShizukuService#transactRemote more conveniently.
 * <p>
 * example:
 * <br><code>IPackageManager pm = IPackageManager.Stub.asInterface(new ShizukuBinder(SystemServiceHelper.getSystemService("package")));
 * <br>pm.getInstalledPackages(0, 0);</code>
 */
public class ShizukuBinderWrapper implements IBinder {

    private final IBinder original;

    public ShizukuBinderWrapper(@NonNull IBinder original) {
        this.original = Objects.requireNonNull(original);
    }

    @Override
    public boolean transact(int code, @NonNull Parcel data, @Nullable Parcel reply, int flags) throws RemoteException {
        Parcel newData = Parcel.obtain();
        try {
            newData.writeInterfaceToken(ShizukuApiConstants.BINDER_DESCRIPTOR);
            newData.writeStrongBinder(original);
            newData.writeInt(code);
            newData.appendFrom(data, 0, data.dataSize());
            ShizukuService.transactRemote(newData, reply, flags);
        } finally {
            newData.recycle();
        }
        return true;
    }

    @Nullable
    @Override
    public String getInterfaceDescriptor() throws RemoteException {
        return original.getInterfaceDescriptor();
    }

    @Override
    public boolean pingBinder() {
        return original.pingBinder();
    }

    @Override
    public boolean isBinderAlive() {
        return original.isBinderAlive();
    }

    @Nullable
    @Override
    public IInterface queryLocalInterface(@NonNull String descriptor) {
        return null;
    }

    @Override
    public void dump(@NonNull FileDescriptor fd, @Nullable String[] args) throws RemoteException {
        original.dump(fd, args);
    }

    @Override
    public void dumpAsync(@NonNull FileDescriptor fd, @Nullable String[] args) throws RemoteException {
        original.dumpAsync(fd, args);
    }

    @Override
    public void linkToDeath(@NonNull DeathRecipient recipient, int flags) throws RemoteException {
        original.linkToDeath(recipient, flags);
    }

    @Override
    public boolean unlinkToDeath(@NonNull DeathRecipient recipient, int flags) {
        return original.unlinkToDeath(recipient, flags);
    }
}
