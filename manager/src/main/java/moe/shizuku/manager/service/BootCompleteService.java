package moe.shizuku.manager.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import moe.shizuku.manager.R;
import moe.shizuku.manager.ServerLauncher;
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

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (notificationManager != null
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_STATUS, getString(R.string.channel_service_status), NotificationManager.IMPORTANCE_MIN);
            channel.setSound(null, null);
            channel.setShowBadge(false);

            notificationManager.createNotificationChannel(channel);
        }

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
        mBindServiceHelper.bind(binder -> {
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


            service.run(new String[]{ServerLauncher.COMMAND_ROOT[0]}, 0, listener);
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
