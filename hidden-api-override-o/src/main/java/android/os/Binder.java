package android.os;

/**
 * Created by rikka on 2017/7/12.
 */

public class Binder {

    protected boolean onTransact(int code, Parcel data, Parcel reply,
                                 int flags) throws RemoteException {
        throw new UnsupportedOperationException();
    }
}
