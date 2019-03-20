package moe.shizuku.manager.service;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.Build;
import android.os.Process;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import moe.shizuku.ShizukuConstants;
import moe.shizuku.ShizukuState;
import moe.shizuku.api.ShizukuApiConstants;
import moe.shizuku.api.ShizukuClient;
import moe.shizuku.io.ParcelInputStream;
import moe.shizuku.io.ParcelOutputStream;
import moe.shizuku.manager.Constants;
import moe.shizuku.manager.Intents;
import moe.shizuku.manager.R;
import moe.shizuku.manager.legacy.LegacySettings;
import moe.shizuku.manager.utils.NotificationHelper;
import moe.shizuku.server.ServerConstants;
import moe.shizuku.support.app.ForegroundIntentService;

import static moe.shizuku.manager.Constants.NOTIFICATION_CHANNEL_WORK;

public class WorkService extends ForegroundIntentService {

    public WorkService() {
        super("WorkService");
    }

    public static void startAuthV2(Context context) {
        Intent intent = new Intent(context, WorkService.class);
        intent.setAction(Intents.ACTION_AUTH_V2);
        ContextCompat.startForegroundService(context, intent);
    }

    public static void startAuthV3(Context context) {
        Intent intent = new Intent(context, WorkService.class);
        intent.setAction(Intents.ACTION_AUTH_V3);
        ContextCompat.startForegroundService(context, intent);
    }

    public static void startRequestTokenV2(Context context) {
        Intent intent = new Intent(context, WorkService.class);
        intent.setAction(Intents.ACTION_REQUEST_TOKEN_V2);
        ContextCompat.startForegroundService(context, intent);
    }

    public static void startRequestTokenV3(Context context) {
        Intent intent = new Intent(context, WorkService.class);
        intent.setAction(Intents.ACTION_REQUEST_TOKEN_V3);
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

    @TargetApi(Build.VERSION_CODES.O)
    @Override
    public void onCreateNotificationChannel(@NonNull NotificationManager notificationManager) {
        NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_WORK, getString(R.string.channel_service_status), NotificationManager.IMPORTANCE_MIN);
        channel.setSound(null, null);
        channel.setShowBadge(false);
        notificationManager.createNotificationChannel(channel);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (Intents.ACTION_AUTH_V2.equals(action)) {
                handleAuthV2();
            } else if (Intents.ACTION_REQUEST_TOKEN_V2.equals(action)) {
                handleRequestTokenV2();
            } else if (Intents.ACTION_AUTH_V3.equals(action)) {
                handleAuthV3();
            } else if (Intents.ACTION_REQUEST_TOKEN_V3.equals(action)) {
                handleRequestTokenV3();
            }
        }
    }

    private void handleAuthV2() {
        ShizukuState state = ShizukuClient.getState();

        LocalBroadcastManager.getInstance(this)
                .sendBroadcast(new Intent(Intents.ACTION_AUTH_RESULT)
                        .putExtra(Intents.EXTRA_RESULT, state));

        if (state.isAuthorized()) {
            LegacySettings.setLastLaunchMode(state.isRoot()
                    ? LegacySettings.LaunchMethod.ROOT : LegacySettings.LaunchMethod.ADB);
        }
    }

    private void handleRequestTokenV2() {
        try (Socket socket = new Socket(ShizukuConstants.HOST, ShizukuConstants.PORT)) {
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

    private void handleAuthV3() {
        try (LocalSocket socket = new LocalSocket(LocalSocket.SOCKET_STREAM)) {
            socket.connect(new LocalSocketAddress(ShizukuApiConstants.SOCKET_NAME, LocalSocketAddress.Namespace.ABSTRACT));
            socket.setSoTimeout(ShizukuApiConstants.SOCKET_TIMEOUT);
            DataOutputStream os = new DataOutputStream(socket.getOutputStream());
            DataInputStream is = new DataInputStream(socket.getInputStream());
            os.writeInt(ShizukuApiConstants.SOCKET_VERSION_CODE);
            os.writeInt(ServerConstants.SOCKET_ACTION_REQUEST_BINDER);
            is.readInt();
        } catch (Exception e) {
            Log.w(Constants.TAG, "can't connect to server: " + e.getMessage(), e);
        }
    }

    private void handleRequestTokenV3() {
        try (LocalSocket socket = new LocalSocket(LocalSocket.SOCKET_STREAM)) {
            socket.connect(new LocalSocketAddress(ShizukuApiConstants.SOCKET_NAME, LocalSocketAddress.Namespace.ABSTRACT));
            socket.setSoTimeout(ShizukuApiConstants.SOCKET_TIMEOUT);
            DataOutputStream os = new DataOutputStream(socket.getOutputStream());
            DataInputStream is = new DataInputStream(socket.getInputStream());
            os.writeInt(ShizukuApiConstants.SOCKET_VERSION_CODE);
            os.writeInt(ServerConstants.SOCKET_ACTION_REQUEST_BINDER);
            is.readInt();
        } catch (Exception e) {
            Log.w(Constants.TAG, "can't connect to server: " + e.getMessage(), e);
        }
    }
}
