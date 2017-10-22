package moe.shizuku.sample;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Process;
import android.os.StrictMode;
import android.widget.Toast;

import moe.shizuku.api.ShizukuClient;

public class MainActivity extends Activity {

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            new AlertDialog.Builder(context)
                    .setMessage(ShizukuCompat.getOpsForPackage(Process.myUid(), BuildConfig.APPLICATION_ID, null).toString())
                    .show();
        }
    };

    private static final String ACTION = "QAQAQ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        ShizukuClient.requestAuthorization(this);

        registerReceiver(mBroadcastReceiver, new IntentFilter(ACTION));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ShizukuClient.AUTHORIZATION_REQUEST_CODE) {
            if (resultCode == ShizukuClient.AUTH_RESULT_OK) {
                ShizukuClient.setToken(data);

                Toast.makeText(this, "Testing broadcast", Toast.LENGTH_SHORT).show();

                ShizukuCompat.broadcastIntent(new Intent(ACTION));
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
