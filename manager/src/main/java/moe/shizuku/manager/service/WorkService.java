package moe.shizuku.manager.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Process;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.net.Socket;

import moe.shizuku.ShizukuConstants;
import moe.shizuku.ShizukuState;
import moe.shizuku.api.ShizukuClient;
import moe.shizuku.io.ParcelInputStream;
import moe.shizuku.io.ParcelOutputStream;
import moe.shizuku.manager.Constants;
import moe.shizuku.manager.Intents;
import moe.shizuku.manager.ShizukuManagerSettings;

public class WorkService extends IntentService {

    public WorkService() {
        super("WorkService");
    }

    public static void startAuth(Context context) {
        Intent intent = new Intent(context, WorkService.class);
        intent.setAction(Intents.ACTION_AUTH);
        context.startService(intent);
    }

    public static void startRequestToken(Context context) {
        Intent intent = new Intent(context, WorkService.class);
        intent.setAction(Intents.ACTION_REQUEST_TOKEN);
        context.startService(intent);
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
        ShizukuState state = ShizukuClient.authorize();

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
