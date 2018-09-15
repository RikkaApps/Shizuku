package moe.shizuku.manager;

import android.app.AlertDialog;
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

import moe.shizuku.ShizukuState;
import moe.shizuku.api.ShizukuClient;
import moe.shizuku.manager.authorization.AuthorizationManager;

/**
 * Created by rikka on 2017/10/23.
 */

public class AuthorizationActivity extends AbstractAuthorizationActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ComponentName component = getCallingActivity();
        if (component == null) {
            setResult(ShizukuClient.AUTH_RESULT_ERROR);
            finish();
            return;
        }

        final String packageName = component.getPackageName();

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
                    .setNeutralButton(R.string.open_manager, (dialog, which) -> startActivity(new Intent(AuthorizationActivity.this, MainActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)))
                    .setOnDismissListener(dialog -> {
                        setResult(ShizukuClient.AUTH_RESULT_ERROR);
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
            Log.wtf(Constants.TAG, "auth | package not found: " + packageName);

            setResult(ShizukuClient.AUTH_RESULT_ERROR);
            finish();
            return;
        }

        if (AuthorizationManager.granted(this, packageName)) {
            setResult(true, packageName);
            finish();
            return;
        }

        CharSequence name = pi.applicationInfo.loadLabel(getPackageManager());

        String mode = shizukuState.isRoot() ? "root" : "adb";
        CharSequence message = Html.fromHtml(getString(R.string.auth_message, name, mode));

        Dialog dialog = new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton(R.string.auth_allow, (d, which) -> {
                    AuthorizationManager.grant(AuthorizationActivity.this, packageName);

                    setResult(true, packageName);
                })
                .setNegativeButton(R.string.auth_deny, (d, which) -> {
                    AuthorizationManager.revoke(AuthorizationActivity.this, packageName);

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
