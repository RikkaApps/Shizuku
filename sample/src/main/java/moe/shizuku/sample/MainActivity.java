package moe.shizuku.sample;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import java.io.InputStream;
import java.io.OutputStream;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import moe.shizuku.api.RemoteProcess;
import moe.shizuku.api.ShizukuApiConstants;
import moe.shizuku.api.ShizukuClientHelper;
import moe.shizuku.api.ShizukuClientHelperPre23;
import moe.shizuku.api.ShizukuService;

public class MainActivity extends Activity {

    private static final int REQUEST_CODE_PERMISSION_V3 = 1;
    private static final int REQUEST_CODE_AUTHORIZATION_V3 = 2;

    private BroadcastReceiver mBinderReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LocalBroadcastManager.getInstance(this).registerReceiver(mBinderReceiver, new IntentFilter(SampleApplication.ACTION_SEND_BINDER));

        if (!ShizukuClientHelper.isManagerV2Installed(this)) {
            Log.d("ShizukuSample", "Shizuku version is too low");
            return;
        }

        showWaiting();
        checkShizuku();

        sendBroadcast(new Intent("moe.shizuku.sample.TEST").setPackage(getPackageName()));
    }

    private void showWaiting() {
        // show loading ui
    }

    private void checkShizuku() {
        // Shizuku v3 service will send binder via Content Provider to this process when this activity comes foreground.

        // Wait a few seconds here for binder

        // Thread.sleep(1000); // run codes below in a work thread
        if (!ShizukuService.pingBinder()) {
            if (SampleApplication.isShizukuV3Failed()) {
                // provider started with no binder included, binder calls blocked by SELinux or server dead, should never happened
                // notify user
            }

            // Shizuku v3 may not running, notify user

            // if your app support Shizuku v2, run old v2 codes here
            // for new apps, recommended to ignore v2
        } else {
            // Shizuku v3 binder received
            runTestV3();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBinderReceiver);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // only called in API pre-23
            case REQUEST_CODE_AUTHORIZATION_V3: {
                if (resultCode == Activity.RESULT_OK) {
                    String token = ShizukuClientHelperPre23.setPre23Token(data, this);
                    if (ShizukuService.pingBinder()) {
                        try {
                            // each of your process need to call this
                            boolean valid = ShizukuService.setTokenPre23(token);
                            SampleApplication.setShizukuV3TokenValid(valid);
                        } catch (RemoteException e) {
                        }

                        if (SampleApplication.isShizukuV3TokenValid()) {
                            runTestV3();
                        }
                    } else {
                        // server dead?
                    }
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_PERMISSION_V3: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    runTestV3();
                } else {
                    // denied
                }
                break;
            }
        }
    }

    //private ITaskStackListener mTaskStackListener;

    private void runTestV3() {
        if (!ShizukuClientHelperPre23.isPreM()) {
            // on API 23+, Shizuku v3 uses runtime permission
            if (ActivityCompat.checkSelfPermission(this, ShizukuApiConstants.PERMISSION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{ShizukuApiConstants.PERMISSION}, REQUEST_CODE_PERMISSION_V3);
                return;
            }
        } else if (!SampleApplication.isShizukuV3TokenValid()){
            // on API pre-23, Shizuku v3 uses old token, get token from Shizuku app
            Intent intent = ShizukuClientHelperPre23.createPre23AuthorizationIntent(this);
            if (intent != null) {
                try {
                    startActivityForResult(intent, REQUEST_CODE_AUTHORIZATION_V3);
                } catch (Throwable tr) {
                    // should never happened
                }
            } else {
                // activity not found
            }
            return;
        }

        try {
            Log.d("ShizukuSample", "getUsers: " + ShizukuApi.UserManager_getUsers(true));
        } catch (Throwable tr) {
            Log.e("ShizukuSample", "getUsers", tr);
        }

        try {
            Log.d("ShizukuSample", "getInstalledPackages: " + ShizukuApi.PackageManager_getInstalledPackages(0, 0));
        } catch (Throwable tr) {
            Log.e("ShizukuSample", "getInstalledPackages", tr);
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
