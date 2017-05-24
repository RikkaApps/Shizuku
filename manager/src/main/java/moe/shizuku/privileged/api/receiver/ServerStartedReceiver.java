package moe.shizuku.privileged.api.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import moe.shizuku.privileged.api.ServerLauncher;

/**
 * Created by Rikka on 2017/5/19.
 */

public class ServerStartedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        ServerLauncher.putToken(context, intent);

        LocalBroadcastManager.getInstance(context)
                .sendBroadcast(intent);
    }
}
