package moe.shizuku.manager.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import moe.shizuku.manager.BuildConfig;
import moe.shizuku.manager.Permissions;
import moe.shizuku.manager.ShizukuManagerSettings;
import moe.shizuku.manager.service.WorkService;

/**
 * Created by Rikka on 2017/5/19.
 */

public class ServerStartedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("RServer", "ServerStartedReceiver");

        ShizukuManagerSettings.putToken(context, intent);

        WorkService.startAuth(context);

        intent = new Intent(intent);
        intent.setComponent(null);
        intent.setAction(BuildConfig.APPLICATION_ID + ".intent.action.UPDATE_TOKEN");
        intent.setFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY | Intent.FLAG_EXCLUDE_STOPPED_PACKAGES);

        Permissions.init(context);
        for (String packageName : Permissions.getGranted()) {
            context.sendBroadcast(intent.setPackage(packageName), BuildConfig.APPLICATION_ID + ".permission.REQUEST_AUTHORIZATION");
        }
    }
}
