package moe.shizuku.manager;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;

import moe.shizuku.manager.adapter.AppAdapter;
import moe.shizuku.manager.authorization.AuthorizationManager;
import moe.shizuku.support.recyclerview.RecyclerViewHelper;

public class ManageAppsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apps);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        AppAdapter adapter = new AppAdapter();
        for (String packageName : AuthorizationManager.getPackages(this)) {
            if (BuildConfig.APPLICATION_ID.equals(packageName)) {
                continue;
            }

            try {
                adapter.getItems().add(getPackageManager().getPackageInfo(packageName, 0));
            } catch (PackageManager.NameNotFoundException ignored) {
            }
            break;
        }

        ((RecyclerView) findViewById(android.R.id.list)).setAdapter(adapter);
        RecyclerViewHelper.fixOverScroll((RecyclerView) findViewById(android.R.id.list));
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
