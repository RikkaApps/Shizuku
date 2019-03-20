package moe.shizuku.manager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import moe.shizuku.ShizukuConstants;
import moe.shizuku.api.BinderContainer;
import moe.shizuku.api.ShizukuApiConstants;
import moe.shizuku.api.ShizukuManager;
import moe.shizuku.manager.legacy.LegacySettings;
import moe.shizuku.manager.legacy.authorization.AuthorizationManager;
import moe.shizuku.manager.service.WorkService;

public class TokenReceiveActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Context context = this;
        Intent intent = getIntent();

        if (intent != null) {
            intent.setExtrasClassLoader(getClassLoader());

            LegacySettings.putToken(ShizukuManagerApplication.getDeviceProtectedStorageContext(context), intent);

            BinderContainer container = intent.getParcelableExtra(ShizukuApiConstants.EXTRA_BINDER);
            if (container != null && container.binder != null) {
                ShizukuManager.setRemote(container.binder);
            }

            WorkService.startAuthV2(context);

            //broadcast new token to other apps
            intent = new Intent(intent);
            intent.setAction(ShizukuConstants.ACTION_UPDATE_TOKEN);
            intent.setFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);

            for (String packageName : AuthorizationManager.getGrantedPackages(context)) {
                context.sendBroadcast(intent.setPackage(packageName), Manifest.permission.API);
            }
        }

        finish();
    }
}
