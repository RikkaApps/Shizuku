package moe.shizuku.sample.service;

import android.os.CancellationSignal;
import android.os.RemoteException;
import android.system.Os;
import android.util.Log;

import moe.shizuku.sample.HelloJni;
import moe.shizuku.sample.IUserService;

public class MainProcessUserService extends IUserService.Stub {

    private final CancellationSignal mRemoveFromServerSignal;

    /**
     * Constructor required in main process mode.
     */
    public MainProcessUserService(CancellationSignal removeFromServerSignal) {
        this.mRemoveFromServerSignal = removeFromServerSignal;
    }

    /**
     * Reserved destroy method
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
    public String doSomething() throws RemoteException {
        return "pid=" + Os.getpid() + ", uid=" + Os.getuid();
    }
}
