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

    private static boolean sFontProviderInitialized;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if (!sFontProviderInitialized) {
            FontProviderClient.create(this, new FontProviderClient.Callback() {
                @Override
                public boolean onServiceConnected(FontProviderClient client, ServiceConnection serviceConnection) {
                    client.replace("sans-serif", "Noto Sans CJK");
                    client.replace("sans-serif-medium", "Noto Sans CJK");
                    return true;
                }
            });

            sFontProviderInitialized = true;
        }

        super.onCreate(savedInstanceState);
    }
}
