package android.os;

import android.support.annotation.RequiresApi;

import java.util.List;

/**
 * Created by Rikka on 2017/5/8.
 */

public interface IUserManager {

    List getUsers(boolean excludeDying) throws RemoteException;

    @RequiresApi(Build.VERSION_CODES.M)
    ParcelFileDescriptor getUserIcon(int userHandle);

    abstract class Stub {

        public static IUserManager asInterface(IBinder binder) {
            throw new UnsupportedOperationException();
        }
    }
}
