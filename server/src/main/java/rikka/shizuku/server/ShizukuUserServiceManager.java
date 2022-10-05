package rikka.shizuku.server;

import android.content.pm.PackageInfo;
import android.util.ArrayMap;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import moe.shizuku.starter.ServiceStarter;
import rikka.hidden.compat.PackageManagerApis;
import rikka.hidden.compat.UserManagerApis;
import rikka.shizuku.server.util.UserHandleCompat;

public class ShizukuUserServiceManager extends UserServiceManager {

    private final Map<UserServiceRecord, ApkChangedListener> apkChangedListeners = new ArrayMap<>();
    private final Map<String, List<UserServiceRecord>> userServiceRecords = Collections.synchronizedMap(new ArrayMap<>());

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

        String packageName = packageInfo.packageName;
        ApkChangedListener listener = new ApkChangedListener() {
            @Override
            public void onApkChanged() {
                String newSourceDir = null;

                for (int userId : UserManagerApis.getUserIdsNoThrow()) {
                    PackageInfo pi = PackageManagerApis.getPackageInfoNoThrow(packageName, 0, userId);
                    if (pi != null && pi.applicationInfo != null && pi.applicationInfo.sourceDir != null) {
                        newSourceDir = pi.applicationInfo.sourceDir;
                        break;
                    }
                }

                if (newSourceDir == null) {
                    LOGGER.v("remove record %s because package %s has been removed", record.token, packageName);
                    record.removeSelf();
                } else {
                    LOGGER.v("update apk listener for record %s since package %s is upgrading", record.token, packageName);
                    ApkChangedObservers.stop(this);
                    ApkChangedObservers.start(newSourceDir, this);
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
