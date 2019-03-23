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

            if (ShizukuClientHelper.getBinderReceivedListener() != null) {
                ShizukuClientHelper.getBinderReceivedListener().onBinderReceived();
            }
        }
        finish();
    }
}
