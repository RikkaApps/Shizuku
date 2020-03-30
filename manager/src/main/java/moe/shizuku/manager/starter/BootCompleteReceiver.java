package moe.shizuku.manager.starter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Process;
import android.util.Log;

import androidx.core.content.ContextCompat;

import moe.shizuku.api.ShizukuService;
import moe.shizuku.manager.AppConstants;
import moe.shizuku.manager.ShizukuManagerSettings;
import moe.shizuku.manager.ShizukuManagerSettings.LaunchMethod;
import moe.shizuku.manager.starter.BootCompleteService;

public class BootCompleteReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (!Intent.ACTION_LOCKED_BOOT_COMPLETED.equals(intent.getAction())
                && !Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            return;
        }

        if (Process.myUid() / 100000 > 0) {
            return;
        }

        if (ShizukuManagerSettings.getLastLaunchMode() == LaunchMethod.ROOT) {
            Log.i(AppConstants.TAG, "start on boot, action=" + intent.getAction());

            if (ShizukuService.pingBinder()) {
                Log.i(AppConstants.TAG, "service is running");
                return;
            }

            ContextCompat.startForegroundService(context, new Intent(context, BootCompleteService.class));
        }
    }
}
