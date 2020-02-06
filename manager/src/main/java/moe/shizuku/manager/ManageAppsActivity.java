package moe.shizuku.manager;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Objects;

import moe.shizuku.api.ShizukuService;
import moe.shizuku.manager.adapter.AppsAdapter;
import moe.shizuku.manager.app.BaseActivity;
import moe.shizuku.manager.utils.CustomTabsHelper;
import moe.shizuku.manager.viewmodel.AppsViewModel;
import moe.shizuku.manager.viewmodel.SharedViewModelProviders;
import rikka.recyclerview.RecyclerViewHelper;

public class ManageAppsActivity extends BaseActivity {

    private AppsViewModel mModel;
    private AppsAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (!ShizukuService.pingBinder() && !isFinishing()) {
            LocalBroadcastManager.getInstance(this)
                    .sendBroadcast(new Intent(AppConstants.ACTION_REQUEST_REFRESH));

            finish();
            return;
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apps);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mAdapter = new AppsAdapter();

        mModel = SharedViewModelProviders.of(this).get("apps", AppsViewModel.class);
        mModel.getPackages().observe(this, object -> {
            if (isFinishing())
                return;

            if (object.error == null) {
                mAdapter.updateData(object.data);
            } else {
                LocalBroadcastManager.getInstance(this)
                        .sendBroadcast(new Intent(AppConstants.ACTION_REQUEST_REFRESH));

                finish();

                Throwable tr = object.error;
                Toast.makeText(this, Objects.toString(tr, "unknown"), Toast.LENGTH_SHORT).show();

                tr.printStackTrace();
            }
        });
        if (mModel.getPackages().getValue() != null && mModel.getPackages().getValue().data != null) {
            mAdapter.updateData(mModel.getPackages().getValue().data);
        } else {
            mModel.load(this);
        }

        RecyclerView recyclerView = findViewById(android.R.id.list);
        recyclerView.setAdapter(mAdapter);

        RecyclerViewHelper.fixOverScroll(recyclerView);

        mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount, @Nullable Object payload) {
                mModel.loadCount();
            }

        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!ShizukuService.pingBinder() && !isFinishing()) {
            LocalBroadcastManager.getInstance(this)
                    .sendBroadcast(new Intent(AppConstants.ACTION_REQUEST_REFRESH));
            finish();
        }

        mAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.apps, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (item.getItemId() == R.id.action_view_apps) {
            CustomTabsHelper.launchUrlOrCopy(this, Helps.APPS.get());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
