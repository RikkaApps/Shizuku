package moe.shizuku.sample;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;

import moe.shizuku.api.ShizukuApiConstants;
import moe.shizuku.sample.databinding.MainActivityBinding;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class MainActivity extends Activity {

    private static final int REQUEST_CODE_BUTTON1 = 1;

    private MainActivityBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("ShizukuSample", getClass().getSimpleName() + " onCreate | Process=" + ApplicationUtils.getProcessName());

        super.onCreate(savedInstanceState);

        binding = MainActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.text1.setText("Waiting for binder");
        binding.text2.setText("This Activity is running in process \"" + ApplicationUtils.getProcessName() + "\"");
        binding.button1.setOnClickListener((v) ->button1());

        ShizukuProvider.addBinderReceivedListenerSticky(() -> binding.text1.setText("Binder received"));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PERMISSION_GRANTED) {
            switch (requestCode) {
                case REQUEST_CODE_BUTTON1: {
                    button1();
                    break;
                }
            }
        } else {
            binding.text1.setText("User denied permission");
        }
    }

    private void button1() {
        // Shizuku uses runtime permission, learn more https://developer.android.com/training/permissions/requesting
        if (checkSelfPermission(ShizukuApiConstants.PERMISSION) == PERMISSION_GRANTED) {
            try {
                binding.text3.setText("getUsers: " + ShizukuApi.UserManager_getUsers(true));
            } catch (Throwable tr) {
                binding.text3.setText("getUsers: " + tr);
            }
        } else if (shouldShowRequestPermissionRationale(ShizukuApiConstants.PERMISSION)) {
            binding.text3.setText("User denied permission (shouldShowRequestPermissionRationale=true)");
        } else {
            requestPermissions(new String[]{ShizukuApiConstants.PERMISSION}, REQUEST_CODE_BUTTON1);
            binding.text1.setText("Binder received, waiting for permission");
        }
    }
}
