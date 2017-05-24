package moe.shizuku.privileged.api;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;

import moe.shizuku.privileged.api.adapter.AppAdapter;

public class AppsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apps);

        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
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
                    adapter.addItem(R.layout.item_app, pi);
                    break;
                }
            }
        }

        ((RecyclerView) findViewById(android.R.id.list)).setAdapter(adapter);
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
