package moe.shizuku.manager;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;

import moe.shizuku.manager.app.BaseActivity;

public class SettingsActivity extends BaseActivity {

    private boolean isStartServiceV2;
    private boolean isKeepSuContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(android.R.id.content, new SettingsFragment())
                    .commit();

            isStartServiceV2 = ShizukuManagerSettings.isStartServiceV2();
            isKeepSuContext = ShizukuManagerSettings.isKeepSuContext();
        } else {
            isStartServiceV2 = savedInstanceState.getBoolean("start_v2", false);
            isKeepSuContext = savedInstanceState.getBoolean("keep_su_context", true);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("start_v2", isStartServiceV2);
        outState.putBoolean("keep_su_context", isKeepSuContext);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (ShizukuManagerSettings.isStartServiceV2() != isStartServiceV2
                || ShizukuManagerSettings.isKeepSuContext() != isKeepSuContext) {
            ServerLauncher.writeFiles(this, true);
            if (ServerLauncher.COMMAND_ROOT != null) {
                ServerLauncher.writeFiles(this, false);
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
