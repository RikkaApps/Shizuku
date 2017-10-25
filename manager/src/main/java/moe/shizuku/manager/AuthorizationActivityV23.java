package moe.shizuku.manager;

import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.os.Bundle;

import moe.shizuku.api.ShizukuClient;

/**
 * Created by rikka on 2017/10/23.
 */

public class AuthorizationActivityV23 extends AbstractAuthorizationActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ComponentName component = getCallingActivity();
        if (component == null) {
            setResult(ShizukuClient.AUTH_RESULT_ERROR);
            finish();
            return;
        }

        String packageName = component.getPackageName();
        setResult(true, packageName);
        finish();
    }
}
