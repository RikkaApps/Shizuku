package moe.shizuku.sample;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Process;
import android.support.v4.app.ActivityCompat;
import android.widget.ImageView;
import android.widget.Toast;

import moe.shizuku.api.ShizukuClient;

import static moe.shizuku.api.ShizukuClient.REQUEST_CODE_PERMISSION;

public class MainActivity extends Activity {

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                new AlertDialog.Builder(context)
                        //.setMessage(ShizukuCompat.getOpsForPackage(Process.myUid(), BuildConfig.APPLICATION_ID, null).toString())
                        .setMessage(ShizukuCompat.getInstalledPackages(0, 0).toString())
                        .setPositiveButton(android.R.string.ok, null)
                        .show();
            } catch (RuntimeException e) {
                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    };

    private static final String ACTION = "QAQAQ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!ShizukuClient.isManagerInstalled(this)) {
            return;
        }

        // Highly not recommended.
        ShizukuClient.setPermitNetworkThreadPolicy();

        ShizukuClient.initialize(getApplicationContext());

        if (!ShizukuClient.getState().isAuthorized()) {
            if (ShizukuClient.checkSelfPermission(this)) {
                ShizukuClient.requestAuthorization(this);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{ShizukuClient.PERMISSION_V23}, REQUEST_CODE_PERMISSION);
            }
        }

        registerReceiver(mBroadcastReceiver, new IntentFilter(ACTION));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ShizukuClient.REQUEST_CODE_AUTHORIZATION:
                if (resultCode == ShizukuClient.AUTH_RESULT_OK) {
                    ShizukuClient.setToken(data);

                    try {
                        Toast.makeText(this, "getUserIcon", Toast.LENGTH_SHORT).show();

                        ImageView imageView = new ImageView(this);
                        imageView.setImageBitmap(ShizukuCompat.getUserIcon(Process.myUserHandle().hashCode()));

                        new AlertDialog.Builder(this)
                                .setView(imageView)
                                .setPositiveButton(android.R.string.ok, null)
                                .show();

                        Toast.makeText(this, "broadcastIntent", Toast.LENGTH_SHORT).show();

                        ShizukuCompat.broadcastIntent(new Intent(ACTION));
                    } catch (RuntimeException e) {
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // user denied or error
                }
                return;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_PERMISSION:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ShizukuClient.requestAuthorization(this);
                } else {
                    // denied
                }
                break;
        }
    }
}
