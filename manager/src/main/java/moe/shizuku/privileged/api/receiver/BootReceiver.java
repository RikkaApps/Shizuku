package moe.shizuku.privileged.api.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import moe.shizuku.privileged.api.PASettings;
import moe.shizuku.privileged.api.PASettings.LaunchMethod;
import moe.shizuku.privileged.api.PASettings.RootLaunchMethod;
import moe.shizuku.privileged.api.service.WorkService;

/**
 * Created by Rikka on 2017/5/24.
 */

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (!"android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            return;
        }

        if (PASettings.getLastLaunchMode() == LaunchMethod.ROOT) {
            Log.i("RServer", "start on boot");

            if (PASettings.getRootLaunchMethod() == RootLaunchMethod.ALTERNATIVE) {
                WorkService.startServerOld(context);
            } else {
                WorkService.startServer(context);
            }
        }
    }
}
