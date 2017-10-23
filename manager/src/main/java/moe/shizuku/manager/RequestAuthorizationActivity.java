package moe.shizuku.manager;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Html;
import android.util.TypedValue;
import android.widget.TextView;

import java.util.UUID;

import moe.shizuku.ShizukuConstants;
import moe.shizuku.ShizukuState;
import moe.shizuku.api.ShizukuClient;

public class RequestAuthorizationActivity extends Activity {

    private static final String ACTION_AUTHORIZATION = BuildConfig.APPLICATION_ID + ".intent.action.AUTHORIZATION_RESULT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StrictMode.ThreadPolicy permitNetworkPolicy = new StrictMode.ThreadPolicy.Builder(StrictMode.getThreadPolicy())
                .permitNetwork()
                .build();
        StrictMode.setThreadPolicy(permitNetworkPolicy);

        final String packageName = getIntent().getStringExtra(ShizukuConstants.EXTRA_PACKAGE_NAME);
        int uid = getIntent().getIntExtra(ShizukuConstants.EXTRA_UID, 0);
        if (packageName == null) {
            setResult(ShizukuConstants.AUTH_RESULT_ERROR);
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
                            setResult(ShizukuConstants.AUTH_RESULT_ERROR);
                            finish();
                        }
                    })
                    .setNeutralButton(R.string.open_manager, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(RequestAuthorizationActivity.this, MainActivity.class)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                            setResult(ShizukuConstants.AUTH_RESULT_ERROR);
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
            setResult(ShizukuConstants.AUTH_RESULT_ERROR);
            finish();
            return;
        }

        final long firstInstallTime = pi.firstInstallTime;
        if (Permissions.granted(packageName)) {
            setResult(true, packageName);
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
                        Permissions.grant(packageName, firstInstallTime);

                        setResult(true, packageName);
                    }
                })
                .setNegativeButton(R.string.auth_deny, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Permissions.revoke(packageName);

                        setResult(false, packageName);
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

    private void setResult(boolean granted, String packageName) {
        if (granted) {
            UUID token = ShizukuManagerSettings.getToken(this);
            Intent intent = new Intent(ACTION_AUTHORIZATION)
                    .setPackage(packageName)
                    .putExtra(ShizukuConstants.EXTRA_TOKEN_MOST_SIG, token.getMostSignificantBits())
                    .putExtra(ShizukuConstants.EXTRA_TOKEN_LEAST_SIG, token.getLeastSignificantBits());

            setResult(ShizukuConstants.AUTH_RESULT_OK, intent);
        } else {
            setResult(ShizukuConstants.AUTH_RESULT_USER_DENIED);
        }

        finish();
    }
}
