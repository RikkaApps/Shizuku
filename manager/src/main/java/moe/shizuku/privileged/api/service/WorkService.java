package moe.shizuku.privileged.api.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import java.util.ArrayList;

import moe.shizuku.libsuperuser.Shell;
import moe.shizuku.privileged.api.ServerLauncher;

public class WorkService extends IntentService {

    private static final String ACTION_START_SERVER = "moe.shizuku.privileged.api.service.action.START_SERVER";
    private static final String ACTION_START_SERVER_OLD = "moe.shizuku.privileged.api.service.action.START_SERVER_OLD";
    private static final String ACTION_AUTH = "moe.shizuku.privileged.api.service.action.AUTH";
    private static final String ACTION_REQUEST_TOKEN = "moe.shizuku.privileged.api.service.action.REQUEST_TOKEN";

    public WorkService() {
        super("WorkService");
    }

    public static void startServer(Context context) {
        Intent intent = new Intent(context, WorkService.class);
        intent.setAction(ACTION_START_SERVER);
        context.startService(intent);
    }

    public static void startServerOld(Context context) {
        Intent intent = new Intent(context, WorkService.class);
        intent.setAction(ACTION_START_SERVER_OLD);
        context.startService(intent);
    }

    public static void startAuth(Context context) {
        Intent intent = new Intent(context, WorkService.class);
        intent.setAction(ACTION_AUTH);
        context.startService(intent);
    }

    public static void startRequestToken(Context context) {
        Intent intent = new Intent(context, WorkService.class);
        intent.setAction(ACTION_REQUEST_TOKEN);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_START_SERVER.equals(action)) {
                handleStartServer();
            } else if (ACTION_START_SERVER_OLD.equals(action)) {
                handleStartServerOld();
            } else if (ACTION_AUTH.equals(action)) {
                handleAuth();
            } else if (ACTION_REQUEST_TOKEN.equals(action)) {
                handleRequestToken();
            }
        }
    }

    private void handleStartServer() {
        Shell.Result result = ServerLauncher.startRoot(this);
        Intent intent = new Intent(getPackageName() + ".intent.action.START")
                .putExtra(getPackageName() + ".intent.extra.CODE", result.getExitCode())
                .putStringArrayListExtra(getPackageName() + ".intent.extra.OUTPUT", new ArrayList<>(result.getOutput()));
        if (!TextUtils.isEmpty(result.getErrorMessage())) {
            intent.putExtra(getPackageName() + ".intent.extra.ERROR", result.getErrorMessage());
        }
        LocalBroadcastManager.getInstance(this)
                .sendBroadcast(intent);
    }

    private void handleStartServerOld() {
        Shell.Result result = ServerLauncher.startRootOld(this);
        Intent intent = new Intent(getPackageName() + ".intent.action.START")
                .putExtra(getPackageName() + ".intent.extra.IS_OLD", true)
                .putExtra(getPackageName() + ".intent.extra.CODE", result.getExitCode());

        LocalBroadcastManager.getInstance(this)
                .sendBroadcast(intent);
    }

    private void handleAuth() {
        ServerLauncher.writeSH(this);

        LocalBroadcastManager.getInstance(this)
                .sendBroadcast(new Intent(getPackageName() + ".intent.action.AUTH_RESULT")
                        .putExtra(getPackageName() + "intent.extra.RESULT",
                                ServerLauncher.authorize(this)));
    }

    private void handleRequestToken() {
        ServerLauncher.requestToken();
    }
}
