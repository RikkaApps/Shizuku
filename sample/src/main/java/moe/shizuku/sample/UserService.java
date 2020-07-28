package moe.shizuku.sample;

import android.os.CancellationSignal;
import android.os.Process;
import android.util.Log;

public class UserService extends IUserService.Stub {

    private CancellationSignal mRemoveFromServerSignal;

    /**
     * Constructor required in stand-alone process mode.
     */
    public UserService() {
    }

    /**
     * Constructor required in main process mode.
     */
    public UserService(CancellationSignal removeFromServerSignal) {
        this();
        this.mRemoveFromServerSignal = removeFromServerSignal;
    }

    /**
     * Reserved cleanup method
     */
    @Override
    public void destroy() {
        Log.i("UserService", "destroy");
    }

    @Override
    public void exit() {
        if (mRemoveFromServerSignal != null) {
            mRemoveFromServerSignal.cancel();
        }
    }

    @Override
    public int getPid() {
        return Process.myPid();
    }
}
