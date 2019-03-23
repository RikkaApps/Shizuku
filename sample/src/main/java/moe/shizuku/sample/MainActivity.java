package moe.shizuku.sample;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import java.io.InputStream;
import java.io.OutputStream;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import moe.shizuku.api.RemoteProcess;
import moe.shizuku.api.ShizukuApiConstants;
import moe.shizuku.api.ShizukuService;
import moe.shizuku.api.ShizukuClient;
import moe.shizuku.api.ShizukuClientHelper;

public class MainActivity extends Activity {

    private static final int REQUEST_CODE_PERMISSION_V2 = ShizukuClient.REQUEST_CODE_PERMISSION;
    private static final int REQUEST_CODE_AUTHORIZATION_V2 = ShizukuClient.REQUEST_CODE_AUTHORIZATION;
    private static final int REQUEST_CODE_PERMISSION_V3 = 1;
    private static final int REQUEST_CODE_AUTHORIZATION_V3 = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (ShizukuClientHelper.isManagerV3Installed(this)) {
            ShizukuClientHelper.setBinderReceivedListener(() -> {
                Log.d("ShizukuSample", "onBinderReceived");
                runTestV3();
            });

            if (!ShizukuService.pingBinder()) {
                if (!ShizukuClientHelper.isPreM()) {
                    if (ActivityCompat.checkSelfPermission(this, ShizukuApiConstants.PERMISSION) == PackageManager.PERMISSION_GRANTED)
                        ShizukuClientHelper.requestBinderNoThrow(this);
                    else
                        ActivityCompat.requestPermissions(this, new String[]{ShizukuApiConstants.PERMISSION}, REQUEST_CODE_PERMISSION_V3);
                } else {
                    if (ShizukuClientHelper.requestBinderNoThrow(this) == ShizukuApiConstants.SOCKET_NO_PERMISSION) {
                        Intent intent = ShizukuClientHelper.createPre23AuthorizationIntent(this);
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
                    ShizukuClientHelper.setPre23Token(data, this);
                    ShizukuClientHelper.requestBinderNoThrow(this);
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
                    ShizukuClientHelper.requestBinderNoThrow(this);
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

        try {
            RemoteProcess remoteProcess = ShizukuService.newProcess(new String[]{"sh"}, null, null);
            InputStream is = remoteProcess.getInputStream();
            OutputStream os = remoteProcess.getOutputStream();
            os.write("echo test\n".getBytes());
            os.write("id\n".getBytes());
            os.write("exit\n".getBytes());
            os.close();

            StringBuilder sb = new StringBuilder();
            int c;
            while ((c = is.read()) != -1) {
                sb.append((char) c);
            }
            is.close();

            Log.d("ShizukuSample", "newProcess: " + remoteProcess);
            Log.d("ShizukuSample", "waitFor: " + remoteProcess.waitFor());
            Log.d("ShizukuSample", "output: " + sb);
        } catch (Throwable tr) {
            Log.e("ShizukuSample", "newProcess", tr);
        }
    }
}
