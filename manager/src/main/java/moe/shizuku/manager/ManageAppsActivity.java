package moe.shizuku.manager;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import moe.shizuku.manager.adapter.AppAdapter;
import moe.shizuku.manager.authorization.AuthorizationManager;
import moe.shizuku.manager.utils.AppNameComparator;
import moe.shizuku.support.recyclerview.RecyclerViewHelper;

public class ManageAppsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apps);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        AppAdapter adapter = new AppAdapter();

        List<PackageInfo> list = new ArrayList<>();
        for (String packageName : AuthorizationManager.getPackages(this)) {
            if (BuildConfig.APPLICATION_ID.equals(packageName)) {
                continue;
            }

            try {
                list.add(getPackageManager().getPackageInfo(packageName, 0));
            } catch (PackageManager.NameNotFoundException ignored) {
            }
        }

        Collections.sort(list, new AppNameComparator(this).getPackageInfoComparator());
        adapter.getItems().addAll(list);

        RecyclerView recyclerView = findViewById(android.R.id.list);
        recyclerView.setAdapter(adapter);

        RecyclerViewHelper.fixOverScroll(recyclerView);
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
