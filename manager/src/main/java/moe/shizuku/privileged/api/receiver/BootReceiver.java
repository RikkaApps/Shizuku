package moe.shizuku.privileged.api.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.util.Log;

import moe.shizuku.privileged.api.ServerLauncher;
import moe.shizuku.support.utils.Settings;

/**
 * Created by Rikka on 2017/5/24.
 */

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (!"android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            return;
        }

        if (Settings.getInt("mode", -1) == 0) {
            Log.i("RServer", "start on boot");

            try {
                final String path = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).applicationInfo.publicSourceDir;
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        if (path != null) {
                            ServerLauncher.startRoot(path);
                        }
                    }
                });
            } catch (PackageManager.NameNotFoundException ignored) {
            }
        }
    }
}
