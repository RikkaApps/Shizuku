package moe.shizuku.manager.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import java.util.ArrayList;

import moe.shizuku.api.ShizukuClient;
import moe.shizuku.libsuperuser.Shell;
import moe.shizuku.manager.Intents;
import moe.shizuku.manager.ServerLauncher;
import moe.shizuku.manager.ShizukuManagerSettings;

public class WorkService extends IntentService {

    public WorkService() {
        super("WorkService");
    }

    public static void startServer(Context context) {
        Intent intent = new Intent(context, WorkService.class);
        intent.setAction(Intents.ACTION_START_SERVER);
        context.startService(intent);
    }

    public static void startServerOld(Context context) {
        Intent intent = new Intent(context, WorkService.class);
        intent.setAction(Intents.ACTION_START_SERVER_OLD);
        context.startService(intent);
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
            if (Intents.ACTION_START_SERVER.equals(action)) {
                handleStartServer();
            } else if (Intents.ACTION_START_SERVER_OLD.equals(action)) {
                handleStartServerOld();
            } else if (Intents.ACTION_AUTH.equals(action)) {
                handleAuth();
            } else if (Intents.ACTION_REQUEST_TOKEN.equals(action)) {
                handleRequestToken();
            }
        }
    }

    private void handleStartServer() {
        Shell.Result result = ServerLauncher.startRoot();
        Intent intent = new Intent(Intents.ACTION_START)
                .putExtra(Intents.EXTRA_CODE, result.getExitCode())
                .putStringArrayListExtra(Intents.EXTRA_OUTPUT, new ArrayList<>(result.getOutput()));
        if (!TextUtils.isEmpty(result.getErrorMessage())) {
            intent.putExtra(Intents.EXTRA_ERROR, result.getErrorMessage());
        }
        LocalBroadcastManager.getInstance(this)
                .sendBroadcast(intent);
    }

    private void handleStartServerOld() {
        Shell.Result result = ServerLauncher.startRootOld();
        Intent intent = new Intent(Intents.ACTION_START)
                .putExtra(Intents.EXTRA_IS_OLD, true)
                .putExtra(Intents.EXTRA_CODE, result.getExitCode());

        LocalBroadcastManager.getInstance(this)
                .sendBroadcast(intent);
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
