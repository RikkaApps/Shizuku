package moe.shizuku.sample;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Process;
import android.os.StrictMode;

import moe.shizuku.ShizukuConstants;
import moe.shizuku.api.ShizukuAppOpsServiceV26;
import moe.shizuku.api.ShizukuClient;

public class MainActivity extends Activity {

    static {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ShizukuClient.requestAuthorization(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ShizukuClient.AUTHORIZATION_REQUEST_CODE) {
            if (resultCode == ShizukuConstants.AUTH_RESULT_OK) {
                ShizukuClient.setToken(data);

                new AlertDialog.Builder(this)
                        .setMessage(ShizukuAppOpsServiceV26.getOpsForPackage(Process.myUid(), BuildConfig.APPLICATION_ID, null).toString())
                        .show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
