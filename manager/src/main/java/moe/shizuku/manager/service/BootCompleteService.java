package moe.shizuku.manager.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import moe.shizuku.manager.R;
import moe.shizuku.manager.ServerLauncher;
import moe.shizuku.manager.ShizukuManagerSettings;
import moe.shizuku.manager.utils.BindServiceHelper;
import moe.shizuku.manager.utils.NotificationHelper;

import static moe.shizuku.manager.Constants.NOTIFICATION_CHANNEL_STATUS;
import static moe.shizuku.manager.Constants.NOTIFICATION_ID_STATUS;
import static moe.shizuku.manager.Constants.TAG;

/**
 * Created by rikka on 2017/10/23.
 */

public class BootCompleteService extends Service {

    private BindServiceHelper mBindServiceHelper;

    @Override
    public void onCreate() {
        mBindServiceHelper = new BindServiceHelper(this, ShellService.class);

        NotificationCompat.Builder builder = NotificationHelper.create(this, NOTIFICATION_CHANNEL_STATUS, R.string.notification_service_starting)
                .setOngoing(true);

        if (Build.VERSION.SDK_INT < 26) {
            builder.setPriority(NotificationCompat.PRIORITY_MIN);
        }

        startForeground(NOTIFICATION_ID_STATUS, builder.build());

        Log.d(TAG, "startForeground");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mBindServiceHelper.bind(new BindServiceHelper.OnServiceConnectedListener() {
            @Override
            public void onServiceConnected(IBinder binder) {
                final Context context = BootCompleteService.this;
                ShellService.ShellServiceBinder service = (ShellService.ShellServiceBinder) binder;

                ShellService.Listener listener = new ShellService.Listener() {
                    @Override
                    public void onFailed() {
                        stopSelf();

                        NotificationHelper.notify(context, NOTIFICATION_ID_STATUS, NOTIFICATION_CHANNEL_STATUS, R.string.notification_service_start_no_root);
                    }

                    @Override
                    public void onCommandResult(int commandCode, int exitCode) {
                        stopSelf();

                        if (exitCode == 0) {
                            NotificationHelper.cancel(context, NOTIFICATION_ID_STATUS);
                        } else {
                            NotificationHelper.notify(context, NOTIFICATION_ID_STATUS, NOTIFICATION_CHANNEL_STATUS, R.string.notification_service_start_failed);
                        }
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
