package moe.shizuku.manager.legacy;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import moe.shizuku.api.ShizukuService;
import moe.shizuku.manager.AppConstants;
import moe.shizuku.manager.MainActivity;
import moe.shizuku.manager.R;
import moe.shizuku.manager.authorization.AuthorizationManager;

public final class AuthorizationActivityV21 extends AuthorizationActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ComponentName component = getCallingActivity();
        if (component == null) {
            setResult(ShizukuLegacy.ShizukuClient.AUTH_RESULT_ERROR);
            finish();
            return;
        }

        final String packageName = component.getPackageName();

        String mode = "unknown";
        int msg = 0;
        if (isV3()) {
            if (!ShizukuService.pingBinder()) {
                msg = R.string.auth_cannot_connect;
            } else {
                try {
                    mode = ShizukuService.getUid() == 0 ? "root" : "adb";
                } catch (Throwable ignored) {
                }
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
            mode = shizukuState.isRoot() ? "root" : "adb";
        }

        if (msg != 0) {
            new AlertDialog.Builder(this)
                    .setMessage(msg)
                    .setPositiveButton(android.R.string.ok, null)
                    .setNeutralButton(R.string.open_manager, (dialog, which) -> startActivity(new Intent(AuthorizationActivityV21.this, MainActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)))
                    .setOnDismissListener(dialog -> {
                        setResult(ShizukuLegacy.ShizukuClient.AUTH_RESULT_ERROR);
                        finish();
                    })
                    .setCancelable(false)
                    .show();
            return;
        }

        PackageInfo pi;
        try {
            pi = getPackageManager().getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException ignored) {
            Log.wtf(AppConstants.TAG, "auth | package not found: " + packageName);

            setResult(ShizukuLegacy.ShizukuClient.AUTH_RESULT_ERROR);
            finish();
            return;
        }

        if (AuthorizationManager.granted(packageName, pi.applicationInfo.uid)) {
            setResult(true, packageName);
            finish();
            return;
        }

        CharSequence name = pi.applicationInfo.loadLabel(getPackageManager());

        CharSequence message = Html.fromHtml(getString(R.string.auth_message, name, mode));

        Dialog dialog = new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton(R.string.auth_allow, (d, which) -> {
                    AuthorizationManager.grant(packageName, pi.applicationInfo.uid);

                    setResult(true, packageName);
                })
                .setNegativeButton(R.string.auth_deny, (d, which) -> {
                    AuthorizationManager.revoke(packageName, pi.applicationInfo.uid);

                    setResult(false, packageName);
                })
                .setOnDismissListener(d -> finish())
                .setCancelable(false)
                .create();

        dialog.setOnShowListener(d -> {
            AlertDialog alertDialog = (AlertDialog) d;
            alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setFilterTouchesWhenObscured(true);
        });
        dialog.show();

        TextView textView = dialog.findViewById(android.R.id.message);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.request_dialog_text_size));
    }
}
