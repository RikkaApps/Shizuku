package moe.shizuku.manager.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import moe.shizuku.api.ShizukuClient;
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
        LocalBroadcastManager.getInstance(this)
                .sendBroadcast(new Intent(Intents.ACTION_AUTH_RESULT)
                        .putExtra(Intents.EXTRA_RESULT, ShizukuClient.authorize(ShizukuManagerSettings.getToken(this))));
    }

    private void handleRequestToken() {
        ShizukuClient.sendTokenToManager();
    }
}
