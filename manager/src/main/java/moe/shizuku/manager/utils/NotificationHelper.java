package moe.shizuku.manager.utils;

import android.app.NotificationManager;
import android.content.Context;
import android.support.annotation.StringRes;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

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
        final NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager == null) {
            return;
        }

        notificationManager.notify(id, builder.build());
    }

    public static void cancel(Context context, int id) {
        final NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager == null) {
            return;
        }

        notificationManager.cancel(id);
    }
}
