package moe.shizuku.sample;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.v4.app.ActivityCompat;
import android.widget.ImageView;
import android.widget.Toast;

import moe.shizuku.api.ShizukuClient;
import moe.shizuku.api.ShizukuUserManagerV26;

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

        ShizukuClient.setPermitNetworkThreadPolicy();
        ShizukuClient.setContext(getApplicationContext());

        ShizukuClient.loadToken(getSharedPreferences("token", MODE_PRIVATE));

        if (ShizukuClient.checkSelfPermission(this)) {
            ShizukuClient.requestAuthorization(this);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{ShizukuClient.PERMISSION_V23}, REQUEST_CODE_PERMISSION);
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
                    ShizukuClient.saveToken(getSharedPreferences("token", MODE_PRIVATE));

                    Toast.makeText(this, "Testing broadcast", Toast.LENGTH_SHORT).show();

                    try {
                        ParcelFileDescriptor pfd = ShizukuUserManagerV26.getUserIcon(0);

                        Bitmap bitmap = BitmapFactory.decodeFileDescriptor(pfd.getFileDescriptor());
                        ImageView imageView = new ImageView(this);
                        imageView.setImageBitmap(bitmap);

                        new AlertDialog.Builder(this)
                                .setView(imageView)
                                .setPositiveButton(android.R.string.ok, null)
                                .show();

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
