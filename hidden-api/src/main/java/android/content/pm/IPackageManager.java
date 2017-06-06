package android.content.pm;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.RequiresApi;

public interface IPackageManager {

    PackageInfo getPackageInfo(String packageName, int flags, int userId);

    ApplicationInfo getApplicationInfo(String packageName, int flags ,int userId);

    /**
     * deprecated since API 24
     */
    int getPackageUid(String packageName, int userId) throws RemoteException;

    @RequiresApi(Build.VERSION_CODES.N)
    int getPackageUid(String packageName, int flags, int userId) throws RemoteException;


    ParceledListSlice<PackageInfo> getInstalledPackages(int flags, int userId) throws RemoteException;

    ParceledListSlice<ApplicationInfo> getInstalledApplications(int flags, int userId) throws RemoteException;

    ResolveInfo resolveIntent(Intent intent, String resolvedType, int flags, int userId);

    ParceledListSlice<ResolveInfo> queryIntentActivities(Intent intent,
                                            String resolvedType, int flags, int userId);

    @RequiresApi(Build.VERSION_CODES.N)
    ParceledListSlice<ResolveInfo> queryIntentActivityOptions(
            ComponentName caller, Intent[] specifics,
            String[] specificTypes,Intent intent,
            String resolvedType, int flags, int userId);

    @RequiresApi(Build.VERSION_CODES.N)
    ParceledListSlice<ResolveInfo> queryIntentReceivers(Intent intent, String resolvedType, int flags, int userId);

    ResolveInfo resolveService(Intent intent, String resolvedType, int flags, int userId);

    @RequiresApi(Build.VERSION_CODES.N)
    ParceledListSlice<ResolveInfo> queryIntentServices(Intent intent, String resolvedType, int flags, int userId);

    @RequiresApi(Build.VERSION_CODES.N)
    ParceledListSlice<ResolveInfo> queryIntentContentProviders(Intent intent, String resolvedType, int flags, int userId);

    int checkPermission(String permName, String pkgName, int userId);

    int checkUidPermission(String permName, int uid);

    class Stub {

        public static IPackageManager asInterface(IBinder binder) {
            throw new UnsupportedOperationException();
        }
    }
}