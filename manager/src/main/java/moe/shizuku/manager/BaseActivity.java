package moe.shizuku.manager;

import android.content.ServiceConnection;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import moe.shizuku.fontprovider.FontProviderClient;

/**
 * Created by rikka on 2017/10/21.
 */

public abstract class BaseActivity extends AppCompatActivity {

    private static boolean sFontInitialized = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if (!sFontInitialized) {
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
