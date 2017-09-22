package moe.shizuku.manager.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import moe.shizuku.manager.ShizukuManagerSettings;
import moe.shizuku.manager.ShizukuManagerSettings.LaunchMethod;
import moe.shizuku.manager.ShizukuManagerSettings.RootLaunchMethod;
import moe.shizuku.manager.service.WorkService;

/**
 * Created by Rikka on 2017/5/24.
 */

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (!"android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            return;
        }

        if (ShizukuManagerSettings.getLastLaunchMode() == LaunchMethod.ROOT) {
            Log.i("RServer", "start on boot");

            if (ShizukuManagerSettings.getRootLaunchMethod() == RootLaunchMethod.ALTERNATIVE) {
                WorkService.startServerOld(context);
            } else {
                WorkService.startServer(context);
            }
        }
    }
}
