package moe.shizuku.manager.legacy;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import moe.shizuku.manager.AppConstants;
import moe.shizuku.manager.Manifest;
import moe.shizuku.manager.authorization.AuthorizationManager;

public class TokenReceiveActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Context context = this;
        Intent intent = getIntent();

        if (intent != null) {
            ShizukuLegacy.putToken(intent);

            LocalBroadcastManager.getInstance(this)
                    .sendBroadcast(new Intent(AppConstants.ACTION_REQUEST_REFRESH));

            //broadcast new token to other apps
            intent = new Intent(intent);
            intent.setAction(ShizukuLegacy.ACTION_UPDATE_TOKEN);
            intent.setFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);

            for (PackageInfo pi : AuthorizationManager.getGrantedPackages(PackageManager.GET_META_DATA)) {
                // send token only to v2 apps only
                ApplicationInfo ai = pi.applicationInfo;
                if (ai.metaData != null && ai.metaData.getBoolean("moe.shizuku.client.V3_SUPPORT")) {
                    continue;
                }

                context.sendBroadcast(intent.setPackage(pi.packageName), Manifest.permission.API);
            }
        }

        finish();
    }
}
