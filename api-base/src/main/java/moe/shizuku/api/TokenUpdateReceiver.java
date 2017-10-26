package moe.shizuku.api;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.UUID;

import moe.shizuku.ShizukuConstants;

/**
 * An extent of {@link BroadcastReceiver} that automatically set new token from broadcast.
 */

public abstract class TokenUpdateReceiver extends BroadcastReceiver {

    private static final String ACTION_AUTHORIZATION = ShizukuConstants.MANAGER_APPLICATION_ID + ".intent.action.UPDATE_TOKEN";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!ACTION_AUTHORIZATION.equals(intent.getAction())) {
            return;
        }

        ShizukuClient.setToken(intent);

        long mostSig = intent.getLongExtra(ShizukuConstants.EXTRA_TOKEN_MOST_SIG, 0);
        long leastSig = intent.getLongExtra(ShizukuConstants.EXTRA_TOKEN_LEAST_SIG, 0);

        if (mostSig != 0 && leastSig != 0) {
            onTokenUpdated(context, new UUID(mostSig, leastSig));
        }
    }

    /**
     * Called when Intent contains valid token, save token here.
     *
     * @param context Context
     * @param token new token
     */
    public abstract void onTokenUpdated(Context context, UUID token);
}
