package moe.shizuku.manager.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Process;
import android.util.Log;

import androidx.core.content.ContextCompat;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import moe.shizuku.api.ShizukuClient;
import moe.shizuku.manager.Constants;
import moe.shizuku.manager.legacy.LegacySettings;
import moe.shizuku.manager.legacy.LegacySettings.LaunchMethod;
import moe.shizuku.manager.service.BootCompleteService;

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

        if (LegacySettings.getLastLaunchMode() == LaunchMethod.ROOT) {
            Log.i(Constants.TAG, "start on boot, action=" + intent.getAction());

            boolean isRunning = Single.fromCallable(() -> ShizukuClient.getState().isServerAvailable()).subscribeOn(Schedulers.io())
                    .blockingGet();
            if (isRunning) {
                Log.i(Constants.TAG, "service is running");
                return;
            }

            ContextCompat.startForegroundService(context, new Intent(context, BootCompleteService.class));
        }
    }
}
