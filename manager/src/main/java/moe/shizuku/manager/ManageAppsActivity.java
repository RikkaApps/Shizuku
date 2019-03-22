package moe.shizuku.manager;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.List;
import java.util.Objects;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;
import moe.shizuku.api.ShizukuClientV3;
import moe.shizuku.manager.adapter.AppsAdapter;
import moe.shizuku.manager.app.BaseActivity;
import moe.shizuku.manager.viewmodel.AppsViewModel;
import moe.shizuku.manager.viewmodel.SharedViewModelProviders;
import moe.shizuku.support.recyclerview.RecyclerViewHelper;

public class ManageAppsActivity extends BaseActivity {

    private AppsViewModel mModel;
    private AppsAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (!ShizukuClientV3.isAlive() && !isFinishing()) {
            LocalBroadcastManager.getInstance(this)
                    .sendBroadcast(new Intent(AppConstants.ACTION_REQUEST_REFRESH));

            finish();
            return;
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apps);

        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mAdapter = new AppsAdapter();

        mModel = SharedViewModelProviders.of(this).get("apps", AppsViewModel.class);
        mModel.observe(this, object -> {
            if (isFinishing())
                return;

            if (object instanceof List) {
                mAdapter.updateData(mModel.getData());
            } else if (object instanceof Throwable) {
                LocalBroadcastManager.getInstance(this)
                        .sendBroadcast(new Intent(AppConstants.ACTION_REQUEST_REFRESH));

                finish();

                Throwable tr = (Throwable) object;
                Toast.makeText(this, Objects.toString(tr, "unknown"), Toast.LENGTH_SHORT).show();
            }
        });
        if (mModel.getData() != null) {
            mAdapter.updateData(mModel.getData());
        }

        RecyclerView recyclerView = findViewById(android.R.id.list);
        recyclerView.setAdapter(mAdapter);

        RecyclerViewHelper.fixOverScroll(recyclerView);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!ShizukuClientV3.isAlive() && !isFinishing()) {
            LocalBroadcastManager.getInstance(this)
                    .sendBroadcast(new Intent(AppConstants.ACTION_REQUEST_REFRESH));
            finish();
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
