package moe.shizuku.lang;

/**
 * Created by rikka on 2017/6/15.
 */

public class ShizukuRemoteException extends RuntimeException {

    public ShizukuRemoteException() {
        super();
    }

    public ShizukuRemoteException(String message) {
        super(message);
    }

    public ShizukuRemoteException(String message, Throwable cause) {
        super(message, cause);
    }

    public ShizukuRemoteException(Throwable cause) {
        super(cause);
    }
}
