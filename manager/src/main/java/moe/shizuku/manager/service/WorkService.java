package moe.shizuku.manager.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Process;
import android.util.Log;

import java.net.Socket;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import moe.shizuku.ShizukuConstants;
import moe.shizuku.ShizukuState;
import moe.shizuku.api.ShizukuClient;
import moe.shizuku.io.ParcelInputStream;
import moe.shizuku.io.ParcelOutputStream;
import moe.shizuku.manager.Constants;
import moe.shizuku.manager.Intents;
import moe.shizuku.manager.R;
import moe.shizuku.manager.ShizukuManagerSettings;
import moe.shizuku.manager.utils.NotificationHelper;
import moe.shizuku.support.app.ForegroundIntentService;

import static moe.shizuku.manager.Constants.NOTIFICATION_CHANNEL_WORK;

public class WorkService extends ForegroundIntentService {

    public WorkService() {
        super("WorkService");
    }

    public static void startAuth(Context context) {
        Intent intent = new Intent(context, WorkService.class);
        intent.setAction(Intents.ACTION_AUTH);
        ContextCompat.startForegroundService(context, intent);
    }

    public static void startRequestToken(Context context) {
        Intent intent = new Intent(context, WorkService.class);
        intent.setAction(Intents.ACTION_REQUEST_TOKEN);
        ContextCompat.startForegroundService(context, intent);
    }

    @Override
    public int getForegroundServiceNotificationId() {
        return Constants.NOTIFICATION_ID_WORK;
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

    @Override
    public void onCreate() {
        super.onCreate();

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (notificationManager != null
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_WORK, getString(R.string.channel_service_status), NotificationManager.IMPORTANCE_MIN);
            channel.setSound(null, null);
            channel.setShowBadge(false);

            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (Intents.ACTION_AUTH.equals(action)) {
                handleAuth();
            } else if (Intents.ACTION_REQUEST_TOKEN.equals(action)) {
                handleRequestToken();
            }
        }
    }

    private void handleAuth() {
        ShizukuState state = ShizukuClient.getState();

        LocalBroadcastManager.getInstance(this)
                .sendBroadcast(new Intent(Intents.ACTION_AUTH_RESULT)
                        .putExtra(Intents.EXTRA_RESULT, state));

        if (state.isAuthorized()) {
            ShizukuManagerSettings.setLastLaunchMode(state.isRoot()
                    ? ShizukuManagerSettings.LaunchMethod.ROOT : ShizukuManagerSettings.LaunchMethod.ADB);
        }
    }

    private void handleRequestToken() {
        try {
            Socket socket = new Socket(ShizukuConstants.HOST, ShizukuConstants.PORT);
            socket.setSoTimeout(ShizukuConstants.TIMEOUT);
            ParcelOutputStream os = new ParcelOutputStream(socket.getOutputStream());
            ParcelInputStream is = new ParcelInputStream(socket.getInputStream());
            os.writeString("Shizuku_sendToken");
            os.writeInt(Process.myUid());
            is.readException();
        } catch (Exception e) {
            Log.w(Constants.TAG, "can't connect to server: " + e.getMessage());
        }
    }
}
