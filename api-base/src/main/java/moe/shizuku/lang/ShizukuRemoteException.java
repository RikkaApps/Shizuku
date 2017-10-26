package moe.shizuku.lang;

import android.os.RemoteException;

/**
 * Created by rikka on 2017/6/15.
 */

public class ShizukuRemoteException extends RemoteException {

    public ShizukuRemoteException() {
        super();
    }

    public ShizukuRemoteException(String message) {
        super(message);
    }

    public ShizukuRemoteException(String message, Throwable cause) {
        super(message);
        initCause(cause);
    }
}
