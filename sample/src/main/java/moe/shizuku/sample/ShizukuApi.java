package moe.shizuku.sample;

import android.app.ITaskStackListener;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.ParceledListSlice;
import android.content.pm.UserInfo;
import android.os.Build;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;

import java.util.List;

import moe.shizuku.api.ShizukuService;
import moe.shizuku.api.SystemServiceHelper;

public class ShizukuApi {

    public static void ActivityManager_registerTaskStackListener(ITaskStackListener taskStackListener) {
        if (Build.VERSION.SDK_INT >= 26) {
            Parcel data = SystemServiceHelper.obtainParcel("activity", "android.app.IActivityManager", "registerTaskStackListener");
            Parcel reply = Parcel.obtain();
            data.writeStrongBinder(taskStackListener.asBinder());
            try {
                ShizukuService.transactRemote(data, reply, 0);
                reply.readException();

                Log.i("ShizukuSample", "ActivityManager#registerTaskStackListener");
            } catch (RemoteException e) {
                Log.e("ShizukuSample", "ActivityManager#registerTaskStackListener", e);
            } finally {
                data.recycle();
                reply.recycle();
            }
        }
    }

    public static void ActivityManager_unregisterTaskStackListener(ITaskStackListener taskStackListener) {
        if (Build.VERSION.SDK_INT >= 26) {
            Parcel data = SystemServiceHelper.obtainParcel("activity", "android.app.IActivityManager", "unregisterTaskStackListener");
            Parcel reply = Parcel.obtain();
            data.writeStrongBinder(taskStackListener.asBinder());
            try {
                ShizukuService.transactRemote(data, reply, 0);
                reply.readException();

                Log.i("ShizukuSample", "ActivityManager#unregisterTaskStackListener");
            } catch (RemoteException e) {
                Log.e("ShizukuSample", "ActivityManager#unregisterTaskStackListener", e);
            } finally {
                data.recycle();
                reply.recycle();
            }
        }
    }

    public static List<UserInfo> UserManager_getUsers(boolean excludeDying) {
        Parcel data = SystemServiceHelper.obtainParcel(Context.USER_SERVICE, "android.os.IUserManager", "getUsers");
        Parcel reply = Parcel.obtain();
        data.writeInt(excludeDying ? 1 : 0);

        List<UserInfo> res = null;
        try {
            ShizukuService.transactRemote(data, reply, 0);
            reply.readException();
            res = reply.createTypedArrayList(UserInfo.CREATOR);

            Log.i("ShizukuSample", "UserManager#getUsers");
        } catch (RemoteException e) {
            Log.e("ShizukuSample", "UserManager#getUsers", e);
        } finally {
            data.recycle();
            reply.recycle();
        }
        return res;
    }

    public static List<PackageInfo> PackageManager_getInstalledPackages(int flags, int userId) {
        Parcel data = SystemServiceHelper.obtainParcel("package", "android.content.pm.IPackageManager", "getInstalledPackages");
        Parcel reply = Parcel.obtain();
        data.writeInt(flags);
        data.writeInt(userId);

        try {
            ShizukuService.transactRemote(data, reply, 0);
            reply.readException();
            if (reply.readInt() != 0) {
                //noinspection unchecked
                ParceledListSlice<PackageInfo> listSlice = ParceledListSlice.CREATOR.createFromParcel(reply);
                return listSlice.getList();
            }
            return null;
        } catch (RemoteException tr) {
            Log.e("ShizukuSample", "PackageManager#getInstalledPackages", tr);
        } finally {
            data.recycle();
            reply.recycle();
        }
        return null;
    }
}
