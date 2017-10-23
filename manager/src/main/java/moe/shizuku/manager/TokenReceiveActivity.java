package moe.shizuku.manager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import moe.shizuku.manager.service.WorkService;

/**
 * Created by rikka on 2017/9/23.
 */

public class TokenReceiveActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Context context = this;
        Intent intent = getIntent();

        if (intent != null) {
            ShizukuManagerSettings.putToken(context, intent);

            WorkService.startAuth(context);

            /*
            // broadcast new token to other apps
            intent = new Intent(intent);
            intent.setComponent(null);
            intent.setAction(BuildConfig.APPLICATION_ID + ".intent.action.UPDATE_TOKEN");
            intent.setFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY | Intent.FLAG_EXCLUDE_STOPPED_PACKAGES);

            Permissions.init(context);
            for (String packageName : Permissions.getGranted()) {
                context.sendBroadcast(intent.setPackage(packageName), BuildConfig.APPLICATION_ID + ".permission.REQUEST_AUTHORIZATION");
            }*/
        }

        finish();
    }
}
