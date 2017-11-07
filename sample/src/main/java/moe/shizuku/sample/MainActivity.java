package moe.shizuku.sample;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ITaskStackListener;
import android.app.TaskStackListener;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.os.RemoteException;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import moe.shizuku.api.ShizukuActivityManagerV26;
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
        } else {
            test();
        }

        registerReceiver(mBroadcastReceiver, new IntentFilter(ACTION));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(mBroadcastReceiver);

        if (mTaskStackListener != null) {
            ShizukuActivityManagerV26.unregisterTaskStackListener(mTaskStackListener);
        }
    }

    private ITaskStackListener mTaskStackListener;

    private void test() {
        try {
            Toast.makeText(this, "getUserIcon", Toast.LENGTH_SHORT).show();

            ImageView imageView = new ImageView(this);
            imageView.setImageBitmap(ShizukuCompat.getUserIcon(Process.myUserHandle().hashCode()));

            new AlertDialog.Builder(this)
                    .setView(imageView)
                    .setPositiveButton(android.R.string.ok, null)
                    .show();

            Toast.makeText(this, "broadcastIntent", Toast.LENGTH_SHORT).show();

            //ShizukuCompat.broadcastIntent(new Intent(ACTION));

            Toast.makeText(this, "registerTaskStackListener", Toast.LENGTH_SHORT).show();

            mTaskStackListener = new TaskStackListener() {
                @Override
                public void onTaskStackChanged() throws RemoteException {
                    final String pkg = ShizukuActivityManagerV26.getTasks(1, 0).get(0).topActivity.getPackageName();
                    Log.d("ShizukuSample", pkg);

                    getWindow().getDecorView().post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, pkg, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            };
            ShizukuActivityManagerV26.registerTaskStackListener(mTaskStackListener);
        } catch (RuntimeException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ShizukuClient.REQUEST_CODE_AUTHORIZATION:
                if (resultCode == ShizukuClient.AUTH_RESULT_OK) {
                    ShizukuClient.setToken(data);

                    test();
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
