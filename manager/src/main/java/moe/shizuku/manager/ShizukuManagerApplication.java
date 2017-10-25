package moe.shizuku.manager;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.os.StrictMode;

import static moe.shizuku.manager.Constants.NOTIFICATION_CHANNEL_STATUS;

/**
 * Created by Rikka on 2017/5/12.
 */

public class ShizukuManagerApplication extends Application {

    private static boolean sInitialized = false;

    public static void init(Context context) {
        if (sInitialized) {
            return;
        }

        StrictMode.ThreadPolicy tp = new StrictMode.ThreadPolicy.Builder().build();
        StrictMode.setThreadPolicy(tp);

        ShizukuManagerSettings.init(context);
        ServerLauncher.init(context);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

        if (notificationManager != null
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_STATUS, context.getString(R.string.channel_service_status), NotificationManager.IMPORTANCE_MIN);
            channel.setShowBadge(false);

            notificationManager.createNotificationChannel(channel);
        }

        sInitialized = true;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        init(this);
    }
}
