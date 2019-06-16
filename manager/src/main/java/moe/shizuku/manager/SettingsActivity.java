package moe.shizuku.manager;

import android.os.Bundle;
import android.view.MenuItem;

import java.io.IOException;

import moe.shizuku.manager.app.BaseActivity;

public class SettingsActivity extends BaseActivity {

    private boolean isStartServiceV2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(android.R.id.content, new SettingsFragment())
                    .commit();

            isStartServiceV2 = ShizukuManagerSettings.isStartServiceV2();
        } else {
            isStartServiceV2 = savedInstanceState.getBoolean("start_v2");
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("start_v2", isStartServiceV2);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (ShizukuManagerSettings.isStartServiceV2() != isStartServiceV2) {
            try {
                ServerLauncher.writeSH(this);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
