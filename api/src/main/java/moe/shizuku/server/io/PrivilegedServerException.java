package moe.shizuku.server.io;

/**
 * Created by rikka on 2017/6/15.
 */

public class PrivilegedServerException extends RuntimeException {

    public PrivilegedServerException() {
        super();
    }

    public PrivilegedServerException(String message) {
        super(message);
    }

    public PrivilegedServerException(String message, Throwable cause) {
        super(message, cause);
    }

    public PrivilegedServerException(Throwable cause) {
        super(cause);
    }
}
