package moe.shizuku.sample;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import java.io.InputStream;
import java.io.OutputStream;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import moe.shizuku.ShizukuState;
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

    private static boolean v3Failed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!ShizukuClientHelper.isManagerV2Installed(this)) {
            Log.d("ShizukuSample", "Shizuku Manager version is too low");
            return;
        }

        ShizukuClientHelper.setBinderReceivedListener(() -> {
            Log.d("ShizukuSample", "onBinderReceived");

            if (ShizukuService.getBinder() == null) {
                // ShizukuBinderReceiveActivity started with intent without binder, should never happened
                Log.d("ShizukuSample", "binder is null");
                v3Failed = true;
                return;
            } else {
                try {
                    // test the binder first
                    Log.d("ShizukuSample", "server version " + ShizukuService.getVersion());
                } catch (Throwable tr) {
                    // blocked by SELinux or server dead, should never happened
                    Log.i("ShizukuSample", "can't contact with remote", tr);
                    v3Failed = true;
                    return;
                }
            }

            runTestV3();
        });

        final Context context = this;
        int v3Code;
        ShizukuState v2Status;

        // v3 binder not alive, request
        if (!ShizukuService.pingBinder()) {
            v3Code = ShizukuClientHelper.requestBinderNoThrow(BuildConfig.APPLICATION_ID);

            if (v3Code == ShizukuApiConstants.RESULT_NO_PERMISSION) {
                if (!ShizukuClientHelper.isPreM()) {
                    if (ActivityCompat.checkSelfPermission(context, ShizukuApiConstants.PERMISSION) != PackageManager.PERMISSION_GRANTED)
                        ActivityCompat.requestPermissions(this, new String[]{ShizukuApiConstants.PERMISSION}, REQUEST_CODE_PERMISSION_V3);
                } else {
                    Intent intent = ShizukuClientHelper.createPre23AuthorizationIntent(context);
                    if (intent != null) {
                        try {
                            startActivityForResult(intent, REQUEST_CODE_AUTHORIZATION_V3);
                        } catch (Throwable tr) {
                            // activity not found?
                        }
                    }
                }
                // show your waiting ui
            } else if (v3Code == ShizukuApiConstants.RESULT_OK) {
                // show your waiting ui
            } else if (v3Code == ShizukuApiConstants.RESULT_PACKAGE_NOT_MATCHING) {
                // pass wrong package name to ShizukuClientHelper.requestBinder
            } else if (v3Code == ShizukuApiConstants.RESULT_START_ACTIVITY_FAILED) {
                // startActivity failed server side, should never happened
            } else {
                // v3 service not running, fallback to v2
                // if you are developing new app, ignore v2
                v2Status = ShizukuClient.getState();
                if (!v2Status.isAuthorized()) {
                    if (!ShizukuClient.checkSelfPermission(context)) {
                        ActivityCompat.requestPermissions(this, new String[]{ShizukuApiConstants.PERMISSION}, REQUEST_CODE_PERMISSION_V2);
                    } else {
                        ShizukuClient.requestAuthorization(this);
                    }
                    // show your waiting ui
                } else {
                    // v2 code
                }
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
                    ShizukuClientHelper.requestBinderNoThrow(BuildConfig.APPLICATION_ID);
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
                    ShizukuClientHelper.requestBinderNoThrow(BuildConfig.APPLICATION_ID);
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
