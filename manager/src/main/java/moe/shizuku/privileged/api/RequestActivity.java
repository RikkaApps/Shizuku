package moe.shizuku.privileged.api;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.util.TypedValue;
import android.widget.TextView;

import java.util.UUID;

import moe.shizuku.server.Protocol;

public class RequestActivity extends Activity {

    private static final String ACTION_AUTHORIZATION = BuildConfig.APPLICATION_ID + ".intent.action.AUTHORIZATION_RESULT";
    private static final String PERMISSION_RECEIVE_AUTHORIZATION = BuildConfig.APPLICATION_ID + ".permission.REQUEST_AUTHORIZATION";

    private static final String EXTRA_VERSION = BuildConfig.APPLICATION_ID + ".intent.extra.VERSION";
    private static final String EXTRA_PACKAGE_NAME = BuildConfig.APPLICATION_ID + ".intent.extra.PACKAGE_NAME";
    private static final String EXTRA_UID = BuildConfig.APPLICATION_ID + ".intent.extra.UID";

    public static final int AUTH_RESULT_OK = 0;
    public static final int AUTH_RESULT_USER_DENIED = -1;
    public static final int AUTH_RESULT_ERROR = -2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StrictMode.ThreadPolicy permitNetworkPolicy = new StrictMode.ThreadPolicy.Builder(StrictMode.getThreadPolicy())
                .permitNetwork()
                .build();
        StrictMode.setThreadPolicy(permitNetworkPolicy);

        final String packageName = getIntent().getStringExtra(EXTRA_PACKAGE_NAME);
        int uid = getIntent().getIntExtra(EXTRA_UID, 0);
        if (packageName == null) {
            setResult(AUTH_RESULT_ERROR);
            finish();
            return;
        }

        Protocol protocol = ServerLauncher.authorize(this);
        int msg = 0;
        switch (protocol.getCode()) {
            case Protocol.RESULT_UNAUTHORIZED:
                msg = R.string.auth_manager_no_token;
                break;
            case Protocol.RESULT_SERVER_DEAD:
                msg = R.string.auth_server_dead;
                break;
            case Protocol.RESULT_UNKNOWN:
                msg = R.string.auth_cannot_connect;
                break;
            case Protocol.RESULT_OK:
                break;
        }

        if (msg != 0) {
            new AlertDialog.Builder(this)
                    .setMessage(msg)
                    .setPositiveButton(android.R.string.ok, null)
                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            setResult(AUTH_RESULT_ERROR);
                            finish();
                        }
                    })
                    .setNeutralButton(R.string.open_manager, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(RequestActivity.this, MainActivity.class)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                            setResult(AUTH_RESULT_ERROR);
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
            setResult(AUTH_RESULT_ERROR);
            finish();
            return;
        }

        final long firstInstallTime = pi.firstInstallTime;
        if (Permissions.granted(packageName)) {
            setResult(true, packageName);
            return;
        }

        CharSequence name = pi.applicationInfo.loadLabel(getPackageManager());

        String mode = protocol.isRoot() ? "root" : "adb";
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

        TextView textView = (TextView) dialog.findViewById(android.R.id.message);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.request_dialog_text_size));
    }

    private void setResult(boolean granted, String packageName) {
        if (granted) {
            UUID token = ServerLauncher.getToken(RequestActivity.this);
            Intent intent = new Intent(ACTION_AUTHORIZATION)
                    .setPackage(packageName)
                    .putExtra("moe.shizuku.privileged.api.intent.extra.TOKEN_MOST_SIG", token.getMostSignificantBits())
                    .putExtra("moe.shizuku.privileged.api.intent.extra.TOKEN_LEAST_SIG", token.getLeastSignificantBits());

            //sendBroadcast(intent, PERMISSION_RECEIVE_AUTHORIZATION);

            setResult(AUTH_RESULT_OK, intent);
        } else {
            /*Intent intent = new Intent(ACTION_AUTHORIZATION)
                    .setPackage(packageName);

            sendBroadcast(intent, PERMISSION_RECEIVE_AUTHORIZATION);*/

            setResult(AUTH_RESULT_USER_DENIED);
        }

        finish();
    }
}
