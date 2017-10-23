package moe.shizuku.manager.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import moe.shizuku.manager.ServerLauncher;
import moe.shizuku.manager.ShizukuManagerSettings;
import moe.shizuku.manager.utils.BindServiceHelper;

/**
 * Created by rikka on 2017/10/23.
 */

public class BootCompleteService extends Service {

    private BindServiceHelper mBindServiceHelper;

    @Override
    public void onCreate() {
        mBindServiceHelper = new BindServiceHelper(this, ShellService.class);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mBindServiceHelper.bind(new BindServiceHelper.OnServiceConnectedListener() {
            @Override
            public void onServiceConnected(IBinder binder) {
                ShellService.ShellServiceBinder service = (ShellService.ShellServiceBinder) binder;

                ShellService.Listener listener = new ShellService.Listener() {
                    @Override
                    public void onFailed() {
                        stopSelf();
                    }

                    @Override
                    public void onCommandResult(int commandCode, int exitCode) {
                        stopSelf();
                    }

                    @Override
                    public void onLine(String line) {

                    }
                };

                if (ShizukuManagerSettings.getRootLaunchMethod() == ShizukuManagerSettings.RootLaunchMethod.ALTERNATIVE) {
                    service.run(ServerLauncher.COMMAND_ROOT_OLD, 0, listener);
                } else {
                    service.run(ServerLauncher.COMMAND_ROOT, 0, listener);
                }
            }
        });
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        mBindServiceHelper.unbind();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
