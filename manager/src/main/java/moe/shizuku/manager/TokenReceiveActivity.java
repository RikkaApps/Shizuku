package moe.shizuku.manager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import moe.shizuku.ShizukuConstants;
import moe.shizuku.manager.authorization.AuthorizationManager;
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
            ShizukuManagerSettings.putToken(ShizukuManagerApplication.getDeviceProtectedStorageContext(context), intent);

            /*BinderHolder binderHolder = intent.getParcelableExtra(ShizukuConstants.EXTRA_BINDER);
            if (binderHolder != null) {
                Parcel data = Parcel.obtain();
                Parcel reply = Parcel.obtain();
                try {
                    binderHolder.binder.transact(1, data, reply, 0);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                data.recycle();
                reply.recycle();
            }*/

            WorkService.startAuth(context);

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
