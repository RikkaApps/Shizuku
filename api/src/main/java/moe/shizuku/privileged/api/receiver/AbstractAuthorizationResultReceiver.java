package moe.shizuku.privileged.api.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.UUID;

import moe.shizuku.privileged.api.BuildConfig;

/**
 * Created by Rikka on 2017/5/19.
 */

public abstract class AbstractAuthorizationResultReceiver extends BroadcastReceiver {

    private static final String ACTION_AUTHORIZATION = BuildConfig.APPLICATION_ID + ".intent.action.AUTHORIZATION_RESULT";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!ACTION_AUTHORIZATION.equals(intent.getAction())) {
            return;
        }

        long mostSig = intent.getLongExtra("moe.shizuku.privileged.api.intent.extra.TOKEN_MOST_SIG", 0);
        long leastSig = intent.getLongExtra("moe.shizuku.privileged.api.intent.extra.TOKEN_LEAST_SIG", 0);

        if (mostSig == 0 && leastSig == 0) {
            onDenied(context);
        } else {
            onAuthorized(context, new UUID(mostSig, leastSig));
        }
    }

    public abstract void onAuthorized(Context context, UUID token);

    public abstract void onDenied(Context context);
}
