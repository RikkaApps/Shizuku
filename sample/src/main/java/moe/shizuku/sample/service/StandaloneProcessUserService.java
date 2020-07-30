package moe.shizuku.sample.service;

import android.os.RemoteException;
import android.system.Os;
import android.util.Log;

import moe.shizuku.sample.HelloJni;
import moe.shizuku.sample.IUserService;

public class StandaloneProcessUserService extends IUserService.Stub {

    /**
     * Constructor required in stand-alone process mode.
     */
    public StandaloneProcessUserService() {
    }

    /**
     * Reserved destroy method
     */
    @Override
    public void destroy() {
        Log.i("UserService", "destroy");
        System.exit(0);
    }

    @Override
    public void exit() {
        destroy();
    }

    @Override
    public String doSomething() throws RemoteException {
        return "pid=" + Os.getpid() + ", uid=" + Os.getuid() + ", " + HelloJni.stringFromJNI();
    }
}
