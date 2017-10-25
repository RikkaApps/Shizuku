package moe.shizuku.manager.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Process;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import moe.shizuku.manager.Constants;
import moe.shizuku.manager.ShizukuManagerSettings;
import moe.shizuku.manager.ShizukuManagerSettings.LaunchMethod;
import moe.shizuku.manager.service.BootCompleteService;

/**
 * Created by Rikka on 2017/5/24.
 */

public class BootCompleteReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            return;
        }

        if (Process.myUid() / 100000 > 0) {
            return;
        }

        if (ShizukuManagerSettings.getLastLaunchMode() == LaunchMethod.ROOT) {
            Log.i(Constants.TAG, "start on boot");

            ContextCompat.startForegroundService(context, new Intent(context, BootCompleteService.class));
        }
    }
}
