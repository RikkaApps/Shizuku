package moe.shizuku.privileged.api;

import android.app.ActivityManager;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;

import hidden.android.app.AppOpsManager;
import hidden.android.content.pm.UserInfo;
import hidden.android.net.wifi.WrappedWifiConfiguration;

import moe.shizuku.server.Protocol;

interface IServerInterface {

    // Server
    Protocol version();
    Protocol authorize(long most, long least);

    // ActivityManager
    List<ActivityManager.RunningTaskInfo> getTasks(int maxNum, int flags);
    int broadcastIntent(in Intent intent, String requiredPermissions, int userId);
    void forceStopPackage(String packageName, int userId);
    int startActivity(in Intent intent, int userId);

    // UserManager
    List<UserInfo> getUsers(boolean excludeDying);
    Bitmap getUserIcon(int userHandle);

    // PackageManager
    int getPackageUid(String packageName, int flags, int userId);
    PackageInfo getPackageInfo(String packageName, int flags, int userId);
    ApplicationInfo getApplicationInfo(String packageName, int flags ,int userId);
    List<PackageInfo> getInstalledPackages(int flags, int userId);
    List<ApplicationInfo> getInstalledApplications(int flags, int userId);
    List<ResolveInfo> queryIntentActivities(in Intent intent, String resolvedType, int flags, int userId);
    int checkPermission(String permName, String pkgName, int userId);
    int checkUidPermission(String permName, int uid);
    int checkServerPermission(String permName);

    // AppOpsManager
    List<AppOpsManager.PackageOps> getOpsForPackage(int uid, String packageName, in int[] ops);
    void setMode(int code, int uid, String packageName, int mode);
    void setMode(in int[] code, int uid, String packageName, in int[] mode);
    void resetAllModes(int reqUserId, String reqPackageName);

    // WifiManager
    List<WrappedWifiConfiguration> getPrivilegedConfiguredNetworks();
}
