package moe.shizuku.server;

import android.os.Binder;
import android.os.Parcel;
import android.os.RemoteException;

public class BinderWrapper extends Binder {

    @Override
    protected boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        boolean res = super.onTransact(code, data, reply, flags);
        if (res)
            return true;



        return false;
    }
}
