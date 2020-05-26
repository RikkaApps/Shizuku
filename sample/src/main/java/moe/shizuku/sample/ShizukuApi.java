package moe.shizuku.sample;

import android.content.Context;
import android.content.pm.IPackageInstaller;
import android.content.pm.IPackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.ParceledListSlice;
import android.content.pm.UserInfo;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import moe.shizuku.api.ShizukuBinderWrapper;
import moe.shizuku.api.ShizukuService;
import moe.shizuku.api.SystemServiceHelper;

public class ShizukuApi {

    // method 1: use ShizukuBinderWrapper (recommend)
    private static final IPackageManager PACKAGE_MANAGER = IPackageManager.Stub.asInterface(new ShizukuBinderWrapper(SystemServiceHelper.getSystemService("package")));

    private static IPackageManager getPackageManager() {
        return PACKAGE_MANAGER;
    }

    public static IPackageInstaller PackageManager_getPackageInstaller() {
        try {
            IPackageInstaller packageInstaller = getPackageManager().getPackageInstaller();
            return IPackageInstaller.Stub.asInterface(new ShizukuBinderWrapper(packageInstaller.asBinder()));
        } catch (Throwable tr) {
            throw new RuntimeException(tr.getMessage(), tr);
        }
    }

    // method 2: use transactRemote directly
    public static List<UserInfo> UserManager_getUsers(boolean excludeDying) {
        Parcel data = SystemServiceHelper.obtainParcel(Context.USER_SERVICE, "android.os.IUserManager", "getUsers");
        Parcel reply = Parcel.obtain();
        data.writeInt(excludeDying ? 1 : 0);

        List<UserInfo> res = null;
        try {
            ShizukuService.transactRemote(data, reply, 0);
            reply.readException();
            res = reply.createTypedArrayList(UserInfo.CREATOR);
        } catch (RemoteException e) {
            Log.e("ShizukuSample", "UserManager#getUsers", e);
        } finally {
            data.recycle();
            reply.recycle();
        }
        return res;
    }
}
