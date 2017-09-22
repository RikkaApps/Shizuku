package moe.shizuku.manager;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;

import moe.shizuku.manager.adapter.AppAdapter;
import moe.shizuku.utils.recyclerview.helper.RecyclerViewHelper;

public class AppsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apps);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // TODO sort by name
        AppAdapter adapter = new AppAdapter();
        for (PackageInfo pi : getPackageManager().getInstalledPackages(PackageManager.GET_PERMISSIONS)) {
            if (BuildConfig.APPLICATION_ID.equals(pi.packageName)
                    || pi.requestedPermissions == null) {
                continue;
            }

            for (String perm : pi.requestedPermissions) {
                if ("moe.shizuku.privileged.api.permission.REQUEST_AUTHORIZATION".equals(perm)) {
                    adapter.getItems().add(pi);
                    break;
                }
            }
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
