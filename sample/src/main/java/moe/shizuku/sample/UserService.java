package moe.shizuku.sample;

import android.os.CancellationSignal;
import android.os.Process;
import android.util.Log;

public class UserService extends IUserService.Stub {

    private final CancellationSignal mRemoveFromServerSignal;

    public UserService(CancellationSignal removeFromServerSignal) {
        this.mRemoveFromServerSignal = removeFromServerSignal;
    }

    /**
     * Reserved cleanup method
     */
    @Override
    public void cleanup() {
        Log.i("UserService", "cleanup");
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
