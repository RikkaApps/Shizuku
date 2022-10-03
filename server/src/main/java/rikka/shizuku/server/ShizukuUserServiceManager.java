package rikka.shizuku.server;

import android.util.ArrayMap;

import java.io.File;
import java.util.Map;

import moe.shizuku.starter.ServiceStarter;

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
    public void onUserServiceRecordCreated(UserServiceRecord record, String apkPath) {
        super.onUserServiceRecordCreated(record, apkPath);
        ApkChangedListener listener = () -> {
            LOGGER.v("remove record %s because apk changed", record.token);
            record.removeSelf();
        };
        ApkChangedObservers.start(apkPath, listener);
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
