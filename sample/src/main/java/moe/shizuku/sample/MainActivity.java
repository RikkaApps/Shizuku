package moe.shizuku.sample;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import moe.shizuku.api.ShizukuApiConstants;
import moe.shizuku.api.ShizukuClient;
import moe.shizuku.api.ShizukuClientV3;

public class MainActivity extends Activity {

    private static final int REQUEST_CODE_PERMISSION_V2 = ShizukuClient.REQUEST_CODE_PERMISSION;
    private static final int REQUEST_CODE_AUTHORIZATION_V2 = ShizukuClient.REQUEST_CODE_AUTHORIZATION;
    private static final int REQUEST_CODE_PERMISSION_V3 = 1;
    private static final int REQUEST_CODE_AUTHORIZATION_V3 = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (ShizukuClientV3.isManagerV3Installed(this)) {
            ShizukuClientV3.setBinderReceivedListener(() -> {
                Log.d("ShizukuSample", "onBinderReceived");
                runTestV3();
            });

            if (!ShizukuClientV3.isAlive()) {
                if (!ShizukuClientV3.isPreM()) {
                    if (ShizukuClientV3.checkSelfPermission(this))
                        ShizukuClientV3.requestBinderSync(this, 5000);
                    else
                        ActivityCompat.requestPermissions(this, new String[]{ShizukuApiConstants.PERMISSION}, REQUEST_CODE_PERMISSION_V3);
                } else {
                    if (!ShizukuClientV3.requestBinderSync(this, 5000)) {
                        Intent intent = ShizukuClientV3.createPre23AuthorizationIntent(this);
                        if (intent != null) {
                            try {
                                startActivityForResult(intent, REQUEST_CODE_AUTHORIZATION_V3);
                            } catch (Throwable tr) {

                            }
                        }
                    }
                }
            } else {
                runTestV3();
            }
        } else {
            // v2
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
                    ActivityCompat.requestPermissions(this, new String[]{ShizukuClient.PERMISSION_V23}, REQUEST_CODE_PERMISSION_V2);
                }
            } else {
                // runTestV2();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        /*if (mTaskStackListener != null) {
            ShizukuApi.ActivityManager_unregisterTaskStackListener(mTaskStackListener);
        }*/
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_AUTHORIZATION_V2: {
                if (resultCode == ShizukuClient.AUTH_RESULT_OK) {
                    ShizukuClient.setToken(data);
                    // runTestV2();
                } else {
                    // user denied or error
                }
                return;
            }
            case REQUEST_CODE_AUTHORIZATION_V3: {
                if (resultCode == ShizukuClient.AUTH_RESULT_OK) {
                    ShizukuClientV3.setPre23Token(data);
                    ShizukuClientV3.requestBinderSync(this, 5000);
                } else {
                    // user denied or error
                }
                return;
            }
            default: {
                super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_PERMISSION_V2: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ShizukuClient.requestAuthorization(this);
                } else {
                    // denied
                }
                break;
            }
            case REQUEST_CODE_PERMISSION_V3: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ShizukuClientV3.requestBinderSync(this, 5000);
                } else {
                    // denied
                }
                break;
            }
        }
    }

    //private ITaskStackListener mTaskStackListener;

    private void runTestV3() {
        /*try {
            mTaskStackListener = new TaskStackListener() {
                @Override
                public void onTaskStackChanged() throws RemoteException {
                    Log.d("ShizukuSample", "onTaskStackChanged");
                }
            };
            ShizukuApi.ActivityManager_registerTaskStackListener(mTaskStackListener);
        } catch (Throwable tr) {
            Log.e("ShizukuSample", "registerTaskStackListener", tr);
        }*/

        try {
            Log.d("ShizukuSample", "getUsers: " + ShizukuApi.UserManager_getUsers(true));
        } catch (Throwable tr) {
            Log.e("ShizukuSample", "registerTaskStackListener", tr);
        }

        try {
            Log.d("ShizukuSample", "getInstalledPackages: " + ShizukuApi.PackageManager_getInstalledPackages(0, 0));
        } catch (Throwable tr) {
            Log.e("ShizukuSample", "registerTaskStackListener", tr);
        }
    }
}
