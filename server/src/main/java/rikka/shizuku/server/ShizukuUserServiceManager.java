package rikka.shizuku.server;

import static moe.shizuku.server.utils.Logger.LOGGER;

import android.util.ArrayMap;

import java.util.Map;
import java.util.concurrent.Executor;

import moe.shizuku.server.ApkChangedObserver;
import moe.shizuku.server.ApkChangedObservers;
import moe.shizuku.server.ShizukuService;
import moe.shizuku.starter.ServiceStarter;

public class ShizukuUserServiceManager extends UserServiceManager {

    private final Map<UserServiceRecord, ApkChangedObserver> apkChangedObservers = new ArrayMap<>();

    public ShizukuUserServiceManager(Executor executor) {
        super(executor);
    }

    @Override
    public String getUserServiceStartCmd(UserServiceRecord record, String key, String token, String packageName, String classname, String processNameSuffix, int callingUid, boolean debug) {
        return ServiceStarter.commandForUserService(
                ShizukuService.getManagerApplicationInfo().sourceDir,
                token, packageName, classname, processNameSuffix, callingUid, debug);
    }

    @Override
    public void onUserServiceRecordCreated(UserServiceRecord record, String apkPath) {
        super.onUserServiceRecordCreated(record, apkPath);
        apkChangedObservers.put(record, ApkChangedObservers.start(apkPath, () -> {
            LOGGER.v("remove record %s because apk changed", record.token);
            record.removeSelf();
        }));
    }

    @Override
    public void onUserServiceRecordRemoved(UserServiceRecord record) {
        super.onUserServiceRecordRemoved(record);
        ApkChangedObserver observer = apkChangedObservers.get(record);
        if (observer != null) {
            observer.stopWatching();
            apkChangedObservers.remove(record);
        }
    }
}
