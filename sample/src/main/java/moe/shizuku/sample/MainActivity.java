package moe.shizuku.sample;

import android.app.Activity;
import android.app.ITaskStackListener;
import android.app.TaskStackListener;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import moe.shizuku.api.ShizukuApiConstants;
import moe.shizuku.api.ShizukuClientV3;

public class MainActivity extends Activity {

    private static final int REQUEST_CODE_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TODO V2
        if (!ShizukuClientV3.isManagerV3Installed(this)) {
            Toast.makeText(MainActivity.this, "ShizukuManager#isManagerV3Installed false", Toast.LENGTH_SHORT).show();
            return;
        }

        ShizukuClientV3.setBinderReceivedListener(() -> {
            Toast.makeText(this, "onBinderReceived", Toast.LENGTH_SHORT).show();
        });

        if (ShizukuClientV3.get() == null) {
            if (ShizukuClientV3.checkSelfPermission(this)) {
                //ShizukuManager.requestAuthorization(this);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{ShizukuApiConstants.PERMISSION_V23}, REQUEST_CODE_PERMISSION);
            }
            ShizukuClientV3.requestBinder(this);
        } else {
            test();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mTaskStackListener != null) {
            ShizukuApi.unregisterTaskStackListener(mTaskStackListener);
        }
    }

    private ITaskStackListener mTaskStackListener;

    private void test() {
        try {
            mTaskStackListener = new TaskStackListener() {
                @Override
                public void onTaskStackChanged() throws RemoteException {
                    Log.d("ShizukuSample", "onTaskStackChanged");
                }
            };
            ShizukuApi.registerTaskStackListener(mTaskStackListener);
        } catch (RuntimeException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /*@Override
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
    }*/

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //ShizukuClient.requestAuthorization(this);
                } else {
                    // denied
                }
                break;
        }
    }
}
