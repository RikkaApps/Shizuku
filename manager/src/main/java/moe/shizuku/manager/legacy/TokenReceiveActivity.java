package moe.shizuku.manager.legacy;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import moe.shizuku.ShizukuConstants;
import moe.shizuku.manager.AppConstants;
import moe.shizuku.manager.Manifest;
import moe.shizuku.manager.ShizukuManagerSettings;
import moe.shizuku.manager.authorization.AuthorizationManager;

public class TokenReceiveActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Context context = this;
        Intent intent = getIntent();

        if (intent != null) {
            ShizukuManagerSettings.putToken(intent);

            LocalBroadcastManager.getInstance(this)
                    .sendBroadcast(new Intent(AppConstants.ACTION_REQUEST_REFRESH));

            //broadcast new token to other apps
            intent = new Intent(intent);
            intent.setAction(ShizukuConstants.ACTION_UPDATE_TOKEN);
            intent.setFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);

            for (String packageName : AuthorizationManager.getGrantedPackages()) {
                context.sendBroadcast(intent.setPackage(packageName), Manifest.permission.API);
            }
        }

        finish();
    }
}
