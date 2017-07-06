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
    private static final String ACTION_AUTH = "moe.shizuku.privileged.api.service.action.AUTH";

    public WorkService() {
        super("WorkService");
    }

    public static void startServer(Context context) {
        Intent intent = new Intent(context, WorkService.class);
        intent.setAction(ACTION_START_SERVER);
        context.startService(intent);
    }

    public static void startAuth(Context context) {
        Intent intent = new Intent(context, WorkService.class);
        intent.setAction(ACTION_AUTH);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_START_SERVER.equals(action)) {
                handleStartServer();
            } else if (ACTION_AUTH.equals(action)) {
                handleAuth();
            }
        }
    }

    private void handleStartServer() {
        Shell.Result result = ServerLauncher.startRoot(this);
        if (result.getExitCode() != 0) {
            Intent intent = new Intent(getPackageName() + ".intent.action.START_FAILED")
                    .putExtra(getPackageName() + "intent.extra.CODE", result.getExitCode())
                    .putStringArrayListExtra(getPackageName() + "intent.extra.OUTPUT", new ArrayList<>(result.getOutput()));
            if (!TextUtils.isEmpty(result.getErrorMessage())) {
                intent.putExtra(getPackageName() + "intent.extra.ERROR", result.getErrorMessage());
            }
            LocalBroadcastManager.getInstance(this)
                    .sendBroadcast(intent);
        }/* else {
            handleAuth();
        }*/
    }

    private void handleAuth() {
        LocalBroadcastManager.getInstance(this)
                .sendBroadcast(new Intent(getPackageName() + ".intent.action.AUTH_RESULT")
                        .putExtra(getPackageName() + "intent.extra.RESULT",
                                ServerLauncher.authorize(this)));
    }
}
