package rikka.shizuku.server;

import android.content.pm.PackageInfo;
import android.util.ArrayMap;

import java.io.File;
import java.util.Map;

import moe.shizuku.starter.ServiceStarter;
import rikka.hidden.compat.PackageManagerApis;
import rikka.shizuku.server.util.UserHandleCompat;

public class ShizukuUserServiceManager extends UserServiceManager {

    private final Map<UserServiceRecord, ApkChangedListener> apkChangedListeners = new ArrayMap<>();

    public ShizukuUserServiceManager() {
        super();
    }

    @Override
    public String getUserServiceStartCmd(
            UserServiceRecord record, String key, String token, String packageName,
            String classname, String processNameSuffix, int callingUid, boolean use32Bits, boolean debug) {

        String appProcess = "/system/bin/app_process";
        if (use32Bits && new File("/system/bin/app_process32").exists()) {
            appProcess = "/system/bin/app_process32";
        }
        return ServiceStarter.commandForUserService(
                appProcess,
                ShizukuService.getManagerApplicationInfo().sourceDir,
                token, packageName, classname, processNameSuffix, callingUid, debug);
    }

    @Override
    public void onUserServiceRecordCreated(UserServiceRecord record, PackageInfo packageInfo) {
        super.onUserServiceRecordCreated(record, packageInfo);

        int userId = UserHandleCompat.getUserId(packageInfo.applicationInfo.uid);
        String packageName = packageInfo.packageName;
        ApkChangedListener listener = new ApkChangedListener() {
            @Override
            public void onApkChanged() {
                PackageInfo pi = PackageManagerApis.getPackageInfoNoThrow(packageName, 0, userId);
                if (pi == null) {
                    LOGGER.v("remove record %s because package %d:%s has been removed", record.token, userId, packageName);
                    record.removeSelf();
                } else {
                    LOGGER.v("update apk listener for record %s since package %d:%s is upgrading", record.token, userId, packageName);
                    ApkChangedObservers.stop(this);
                    ApkChangedObservers.start(pi.applicationInfo.sourceDir, this);
                }
            }
        };

        ApkChangedObservers.start(packageInfo.applicationInfo.sourceDir, listener);
        apkChangedListeners.put(record, listener);
    }

    @Override
    public void onUserServiceRecordRemoved(UserServiceRecord record) {
        super.onUserServiceRecordRemoved(record);
        ApkChangedListener listener = apkChangedListeners.get(record);
        if (listener != null) {
            ApkChangedObservers.stop(listener);
            apkChangedListeners.remove(record);
        }
    }
}
