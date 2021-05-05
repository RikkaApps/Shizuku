package moe.shizuku.manager.utils;

import android.app.NotificationManager;
import android.content.Context;

import androidx.annotation.StringRes;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import moe.shizuku.manager.R;

public class NotificationHelper {

    public static NotificationCompat.Builder create(Context context, String channel, @StringRes int text) {
        return new NotificationCompat.Builder(context, channel)
                .setContentTitle(context.getString(text))
                .setColor(ContextCompat.getColor(context, R.color.notification))
                .setSmallIcon(R.drawable.ic_noti_24dp)
                .setWhen(System.currentTimeMillis())
                .setShowWhen(true);
    }

    public static void notify(Context context, int id, String channel, @StringRes int text) {
        notify(context, id, create(context, channel, text));
    }

    public static void notify(Context context, int id, NotificationCompat.Builder builder) {
        final NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        if (notificationManager == null) {
            return;
        }

        notificationManager.notify(id, builder.build());
    }

    public static void cancel(Context context, int id) {
        final NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        if (notificationManager == null) {
            return;
        }

        notificationManager.cancel(id);
    }
}
