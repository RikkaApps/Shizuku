package moe.shizuku.manager.legacy;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;

import moe.shizuku.api.ShizukuService;
import moe.shizuku.manager.home.HomeActivity;
import moe.shizuku.manager.R;

public final class AuthorizationActivityV23 extends AuthorizationActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ComponentName component = getCallingActivity();
        if (component == null) {
            setResult(ShizukuLegacy.ShizukuClient.AUTH_RESULT_ERROR);
            finish();
            return;
        }

        if (!checkNotLegacyOnApi30()) {
            return;
        }

        int msg = 0;
        if (isV3()) {
            if (!ShizukuService.pingBinder()) {
                msg = R.string.auth_cannot_connect;
            }
        } else {
            ShizukuLegacy.ShizukuState shizukuState = getLegacyServerState();
            switch (shizukuState.getCode()) {
                case ShizukuLegacy.ShizukuState.STATUS_UNAUTHORIZED:
                    msg = R.string.auth_manager_no_token;
                    break;
                case ShizukuLegacy.ShizukuState.STATUS_UNAVAILABLE:
                    msg = R.string.auth_service_dead;
                    break;
                case ShizukuLegacy.ShizukuState.STATUS_UNKNOWN:
                    msg = R.string.auth_cannot_connect;
                    break;
                case ShizukuLegacy.ShizukuState.STATUS_AUTHORIZED:
                    break;
            }
        }

        if (msg != 0) {
            new AlertDialog.Builder(this)
                    .setMessage(msg)
                    .setPositiveButton(android.R.string.ok, null)
                    .setNeutralButton(R.string.open_manager, (dialog, which) -> startActivity(new Intent(AuthorizationActivityV23.this, HomeActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)))
                    .setOnDismissListener(dialog -> {
                        setResult(ShizukuLegacy.ShizukuClient.AUTH_RESULT_ERROR);
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
