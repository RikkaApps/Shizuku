package moe.shizuku.api;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;

public class ShizukuBinderReceiveActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i("ShizukuClient", "receiver activity started");

        Intent intent = getIntent();
        if (intent == null) {
            finish();
            return;
        }

        intent.setExtrasClassLoader(getClassLoader());
        BinderContainer container = intent.getParcelableExtra(ShizukuApiConstants.EXTRA_BINDER);
        if (container != null && container.binder != null) {
            Log.i("ShizukuClient", "binder received");

            ShizukuService.setBinder(container.binder);
        }

        // In order for user app report error, we always call listener even if the binder is null.
        // This activity is protected by INTERACT_ACROSS_USERS_FULL permission, other user apps can't use.
        if (ShizukuClientHelper.getBinderReceivedListener() != null) {
            ShizukuClientHelper.getBinderReceivedListener().onBinderReceived();
        }
        finish();
    }
}
