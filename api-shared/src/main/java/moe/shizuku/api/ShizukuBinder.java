package moe.shizuku.api;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Objects;

/**
 * Binder Wrapper for Shizuku transactRemote
 *
 * example:
 *     IPackageManager.Stub.asInterface(new ShizukuBinder(SystemServiceHelper.getSystemService("package")))
 */
public class ShizukuBinder extends Binder {
    private IBinder original;

    public ShizukuBinder(@NonNull IBinder original) {
        this.original = Objects.requireNonNull(original);
    }

    @Override
    protected boolean onTransact(int code, @NonNull Parcel data, @Nullable Parcel reply, int flags) throws RemoteException {
        Parcel data_shizuku = Parcel.obtain();
        try {
            data_shizuku.writeInterfaceToken(ShizukuApiConstants.BINDER_DESCRIPTOR);
            data_shizuku.writeStrongBinder(original);
            data_shizuku.writeInt(code);

            data_shizuku.appendFrom(data ,0 ,data.dataSize());
            ShizukuService.transactRemote(data_shizuku ,reply ,flags);
        } finally {
            data_shizuku.recycle();
        }

        return true;
    }

    @Override
    public void attachInterface(@Nullable IInterface owner, @Nullable String descriptor) {}

    @Nullable
    @Override
    public String getInterfaceDescriptor() {
        try {
            return original.getInterfaceDescriptor();
        } catch (RemoteException e) {
            throw new IllegalStateException(e.getClass().getSimpleName() ,e);
        }
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
    public void dump(@NonNull FileDescriptor fd, @Nullable String[] args) {
        try {
            original.dump(fd ,args);
        } catch (RemoteException e) {
            throw new IllegalStateException(e.getClass().getSimpleName() ,e);
        }
    }

    @Override
    public void dumpAsync(@NonNull FileDescriptor fd, @Nullable String[] args) {
        try {
            original.dumpAsync(fd, args);
        } catch (RemoteException e) {
            throw new IllegalStateException(e.getClass().getSimpleName() ,e);
        }
    }

    @Override
    protected void dump(@NonNull FileDescriptor fd, @NonNull PrintWriter fout, @Nullable String[] args) {
        throw new UnsupportedOperationException("wrapper binder");
    }

    @Override
    public void linkToDeath(@NonNull DeathRecipient recipient, int flags) {
        try {
            original.linkToDeath(recipient, flags);
        } catch (RemoteException e) {
            throw new IllegalStateException(e.getClass().getSimpleName() ,e);
        }
    }

    @Override
    public boolean unlinkToDeath(@NonNull DeathRecipient recipient, int flags) {
        return original.unlinkToDeath(recipient, flags);
    }
}
