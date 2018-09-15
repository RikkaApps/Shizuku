package moe.shizuku.manager;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;

import moe.shizuku.fontprovider.FontProviderClient;

public abstract class BaseActivity extends FragmentActivity {

    private static boolean sFontInitialized = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if (!sFontInitialized && Build.VERSION.SDK_INT < 28) {
            FontProviderClient client = FontProviderClient.create(this);
            if (client != null) {
                client.replace("Noto Sans CJK",
                        "sans-serif", "sans-serif-medium");
            }

            sFontInitialized = true;
        }

        super.onCreate(savedInstanceState);
    }
}
