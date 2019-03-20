package moe.shizuku.manager.legacy;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;

import moe.shizuku.ShizukuState;
import moe.shizuku.api.ShizukuClient;
import moe.shizuku.manager.MainActivity;
import moe.shizuku.manager.R;

public final class AuthorizationActivityV23 extends AuthorizationActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ComponentName component = getCallingActivity();
        if (component == null) {
            setResult(ShizukuClient.AUTH_RESULT_ERROR);
            finish();
            return;
        }

        ShizukuState shizukuState = getServerState();
        int msg = 0;
        switch (shizukuState.getCode()) {
            case ShizukuState.STATUS_UNAUTHORIZED:
                msg = R.string.auth_manager_no_token;
                break;
            case ShizukuState.STATUS_UNAVAILABLE:
                msg = R.string.auth_server_dead;
                break;
            case ShizukuState.STATUS_UNKNOWN:
                msg = R.string.auth_cannot_connect;
                break;
            case ShizukuState.STATUS_AUTHORIZED:
                break;
        }

        if (msg != 0) {
            new AlertDialog.Builder(this)
                    .setMessage(msg)
                    .setPositiveButton(android.R.string.ok, null)
                    .setNeutralButton(R.string.open_manager, (dialog, which) -> startActivity(new Intent(AuthorizationActivityV23.this, MainActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)))
                    .setOnDismissListener(dialog -> {
                        setResult(ShizukuClient.AUTH_RESULT_ERROR);
                        finish();
                    })
                    .setCancelable(false)
                    .show();
            return;
        }

        String packageName = component.getPackageName();
        setResult(true, packageName);
        finish();
    }
}
