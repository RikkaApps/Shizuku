package moe.shizuku.privileged.api.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import moe.shizuku.privileged.api.BuildConfig;

/**
 * Created by Rikka on 2017/5/19.
 */

public abstract class AbstractClearTokenReceiver extends BroadcastReceiver {

    private static final String ACTION_SERVER_STARTED = BuildConfig.APPLICATION_ID + ".intent.action.SERVER_STARTED";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!ACTION_SERVER_STARTED.equals(intent.getAction())) {
            return;
        }

        onClearToken(context);
    }

    public abstract void onClearToken(Context context);
}
