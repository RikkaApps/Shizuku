package moe.shizuku.manager;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;
import moe.shizuku.ShizukuState;
import moe.shizuku.manager.adapter.MainAdapter;
import moe.shizuku.manager.service.WorkService;
import moe.shizuku.support.recyclerview.RecyclerViewHelper;


public class MainActivity extends BaseActivity {

    private class ServerStartedReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            mAdapter.updateData(context, intent.<ShizukuState>getParcelableExtra(Intents.EXTRA_RESULT));
        }
    }

    private BroadcastReceiver mServerStartedReceiver = new ServerStartedReceiver();

    private MainAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ServerLauncher.init(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAdapter = new MainAdapter(this);

        RecyclerView recyclerView = findViewById(android.R.id.list);
        recyclerView.setAdapter(mAdapter);

        RecyclerViewHelper.fixOverScroll(recyclerView);

        mServerStartedReceiver = new ServerStartedReceiver();

        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mServerStartedReceiver, new IntentFilter(Intents.ACTION_AUTH_RESULT));
    }

    @Override
    protected void onResume() {
        super.onResume();

        check();
    }

    private void check() {
        WorkService.startAuth(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(mServerStartedReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_about:
                Dialog dialog = new AlertDialog.Builder(this)
                        .setView(R.layout.dialog_about)
                        .show();

                ((TextView) dialog.findViewById(R.id.icon_credits)).setMovementMethod(LinkMovementMethod.getInstance());
                ((TextView) dialog.findViewById(R.id.icon_credits)).setText(Html.fromHtml(getString(R.string.about_icon_credits)));
                return true;
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
