package moe.shizuku.manager.service;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import moe.shizuku.manager.AppConstants;
import moe.shizuku.manager.R;
import moe.shizuku.manager.utils.NotificationHelper;
import rikka.core.app.ForegroundIntentService;

import static moe.shizuku.manager.AppConstants.NOTIFICATION_CHANNEL_WORK;

public class WorkService extends ForegroundIntentService {

    public WorkService() {
        super("WorkService");
    }

    @Override
    public int getForegroundServiceNotificationId() {
        return AppConstants.NOTIFICATION_ID_WORK;
    }

    @Override
    public Notification onStartForeground() {
        NotificationCompat.Builder builder = NotificationHelper.create(this, NOTIFICATION_CHANNEL_WORK, R.string.notification_working)
                .setOngoing(true);

        if (Build.VERSION.SDK_INT < 26) {
            builder.setPriority(NotificationCompat.PRIORITY_MIN);
        }

        return builder.build();
    }

    @TargetApi(Build.VERSION_CODES.O)
    @Override
    public void onCreateNotificationChannel(@NonNull NotificationManager notificationManager) {
        NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_WORK, getString(R.string.notification_channel_service_status), NotificationManager.IMPORTANCE_MIN);
        channel.setSound(null, null);
        channel.setShowBadge(false);
        notificationManager.createNotificationChannel(channel);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
        }
    }
}
