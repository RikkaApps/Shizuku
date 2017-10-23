package moe.shizuku.manager;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Html;
import android.util.TypedValue;
import android.widget.TextView;

import moe.shizuku.ShizukuConstants;
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

        final String packageName = getIntent().getStringExtra(ShizukuConstants.EXTRA_PACKAGE_NAME);
        int uid = getIntent().getIntExtra(ShizukuConstants.EXTRA_UID, 0);
        if (packageName == null) {
            setResult(ShizukuClient.AUTH_RESULT_ERROR);
            finish();
            return;
        }

        ShizukuState shizukuState = ShizukuClient.authorize(ShizukuManagerSettings.getToken(this));
        int msg = 0;
        switch (shizukuState.getCode()) {
            case ShizukuState.RESULT_UNAUTHORIZED:
                msg = R.string.auth_manager_no_token;
                break;
            case ShizukuState.RESULT_SERVER_DEAD:
                msg = R.string.auth_server_dead;
                break;
            case ShizukuState.RESULT_UNKNOWN:
                msg = R.string.auth_cannot_connect;
                break;
            case ShizukuState.RESULT_OK:
                break;
        }

        if (msg != 0) {
            new AlertDialog.Builder(this)
                    .setMessage(msg)
                    .setPositiveButton(android.R.string.ok, null)
                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            setResult(ShizukuClient.AUTH_RESULT_ERROR);
                            finish();
                        }
                    })
                    .setNeutralButton(R.string.open_manager, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(AuthorizationActivity.this, MainActivity.class)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                            setResult(ShizukuClient.AUTH_RESULT_ERROR);
                            finish();
                        }
                    })
                    .show();
            return;
        }

        PackageInfo pi;
        try {
            pi = getPackageManager().getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException ignored) {
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
                .setPositiveButton(R.string.auth_allow, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AuthorizationManager.grant(AuthorizationActivity.this, packageName);

                        setResult(true, packageName);
                    }
                })
                .setNegativeButton(R.string.auth_deny, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AuthorizationManager.revoke(AuthorizationActivity.this, packageName);

                        setResult(false, packageName);
                    }
                })
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        finish();
                    }
                })
                .setCancelable(false)
                .create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                AlertDialog dialog = (AlertDialog) dialogInterface;
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setFilterTouchesWhenObscured(true);
            }
        });
        dialog.show();

        TextView textView = dialog.findViewById(android.R.id.message);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.request_dialog_text_size));
    }
}
