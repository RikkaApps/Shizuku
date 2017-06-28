package moe.shizuku.privileged.api;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import moe.shizuku.privileged.api.widget.HtmlTextView;
import moe.shizuku.server.Protocol;
import moe.shizuku.support.utils.Settings;

import static moe.shizuku.privileged.api.ServerLauncher.COMMAND_ADB;

public class MainActivity extends Activity {

    private View mStatus;
    private TextView mStatusText;
    private ImageView mStatusIcon;

    private TextView mStartButton;
    private TextView mRestartButton;

    private View mRootCard;
    private View mAdbCard;
    private View mAppsCard;

    private HtmlTextView mAuthorizedAppsCount;

    private AsyncTask mRefreshTask;
    private AsyncTask mStartTask;

    private BroadcastReceiver mServerStartedReceiver;

    private boolean mCheckToRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ServerLauncher.getToken(getIntent()) != null) {
            ServerLauncher.putToken(this, getIntent());
        }

        mStatus = findViewById(R.id.status);
        mStatusText = (TextView) findViewById(R.id.status_text);
        mStatusIcon = (ImageView) findViewById(R.id.status_icon);

        mStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                check();
            }
        });

        mStartButton = (TextView) findViewById(R.id.start);
        mRestartButton = (TextView) findViewById(R.id.restart);

        mRootCard = findViewById(R.id.root);
        mAdbCard = findViewById(R.id.adb);
        mAppsCard = findViewById(R.id.apps);

        mAuthorizedAppsCount = (HtmlTextView) findViewById(R.id.authorized_apps_count);

        mAppsCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(v.getContext(), AppsActivity.class));
            }
        });

        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start();
            }
        });
        mRestartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start();
            }
        });

        findViewById(R.id.copy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = android.content.ClipData.newPlainText("label", COMMAND_ADB);
                    clipboard.setPrimaryClip(clip);

                    Toast.makeText(v.getContext(), getString(R.string.copied_to_clipboard, COMMAND_ADB), Toast.LENGTH_SHORT).show();
                } catch (Exception ignored) {
                }
            }
        });

        findViewById(R.id.send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, COMMAND_ADB);
                intent = Intent.createChooser(intent, getString(R.string.send_command));
                startActivity(intent);
            }
        });

        check();

        mServerStartedReceiver = new ServerStartedReceiver();
    }

    private class ServerStartedReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            check();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        mAuthorizedAppsCount.setHtmlText(
                getResources().getQuantityString(R.plurals.authorized_apps_count, Permissions.grantedCount(), Permissions.grantedCount()));

        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mServerStartedReceiver, new IntentFilter(getPackageName() + ".intent.action.SERVER_STARTED"));
    }

    @Override
    protected void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(mServerStartedReceiver);
    }

    private void check() {
        if (mRefreshTask == null) {
            if (mCheckToRequest) {
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        ServerLauncher.requestToken();
                    }
                });
                ServerLauncher.requestToken();
            } else {
                mRefreshTask = new RefreshTask().execute(this);
            }
        }
    }

    private void start() {
        if (mStartTask == null) {
            mStartTask = new StartTask().execute(this);
        }
    }

    private class RefreshTask extends AsyncTask<Context, Void, Protocol> {
        @Override
        protected Protocol doInBackground(Context... params) {
            ServerLauncher.writeSH(params[0]);

            return ServerLauncher.authorize(params[0]);
        }

        @Override
        protected void onPreExecute() {
            mStartButton.setEnabled(false);
            mRestartButton.setEnabled(false);
        }

        @Override
        protected void onPostExecute(Protocol protocol) {
            if (isFinishing()) {
                return;
            }

            updateUI(protocol);
        }
    }

    private class StartTask extends AsyncTask<Context, Void, Protocol> {

        @Override
        protected Protocol doInBackground(Context... params) {
            return ServerLauncher.startRoot(params[0]);
        }

        @Override
        protected void onPreExecute() {
            mStartButton.setEnabled(false);
            mRestartButton.setEnabled(false);
        }

        @Override
        protected void onPostExecute(Protocol protocol) {
            if (isFinishing()) {
                return;
            }

            if (protocol.getCode() == Protocol.RESULT_OK) {
                Toast.makeText(MainActivity.this, "Succeed.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Failed.", Toast.LENGTH_SHORT).show();
            }

            updateUI(protocol);
        }
    }

    private void updateUI(Protocol protocol) {
        ((HtmlTextView) findViewById(R.id.adb_text)).setHtmlText(getString(R.string.start_server_summary_adb, COMMAND_ADB));

        mCheckToRequest = false;

        boolean ok = false;
        boolean running = false;
        switch (protocol.getCode()) {
            case Protocol.RESULT_OK:
                running = true;
                ok = true;
                break;
            case Protocol.RESULT_SERVER_DEAD:
            case Protocol.RESULT_UNAUTHORIZED:
                running = true;
                break;
            case Protocol.RESULT_UNKNOWN:
                break;
        }

        if (ok) {
            mStatusIcon.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.status_ok));
            mStatusIcon.setImageDrawable(getDrawable(R.drawable.ic_server_ok_48dp));
            mStatusText.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.status_ok));
        } else {
            mStatusIcon.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.status_warning));
            mStatusIcon.setImageDrawable(getDrawable(R.drawable.ic_server_error_48dp));
            mStatusText.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.status_warning));
        }

        if (!running) {
            mStatusText.setText(R.string.server_not_running);

            mStartButton.setVisibility(View.VISIBLE);
            mRestartButton.setVisibility(View.GONE);

            mAppsCard.setVisibility(View.GONE);
        } else {
            if (ok) {
                if (protocol.versionUnmatched()) {
                    mStatusText.setText(getString(R.string.server_running_update, protocol.isRoot() ? "root" : "adb", protocol.getVersion(), Protocol.VERSION));
                } else {
                    mStatusText.setText(getString(R.string.server_running, protocol.isRoot() ? "root" : "adb", protocol.getVersion()));
                }

                if (protocol.isRoot()) {
                    mStartButton.setVisibility(View.GONE);
                    mRestartButton.setVisibility(View.VISIBLE);

                    if (protocol.versionUnmatched()) {
                        mRestartButton.setText(R.string.server_update);
                    } else {
                        mRestartButton.setText(R.string.server_restart);
                    }
                } else {
                    mStartButton.setVisibility(View.VISIBLE);
                    mRestartButton.setVisibility(View.GONE);
                }

                mAppsCard.setVisibility(View.VISIBLE);
            } else {
                mStartButton.setVisibility(View.GONE);
                mRestartButton.setVisibility(View.VISIBLE);

                if (protocol.getCode() == Protocol.RESULT_UNAUTHORIZED) {
                    mStatusText.setText(R.string.server_no_token);
                    mCheckToRequest = true;
                } else {
                    mStatusText.setText(R.string.server_require_restart);
                }

                mAppsCard.setVisibility(View.GONE);
            }

            ViewGroup parent = (ViewGroup) mRootCard.getParent();
            if (protocol.isRoot()
                    && !parent.getChildAt(2).equals(mRootCard)) {
                parent.removeView(mAdbCard);
                parent.addView(mAdbCard);
            } else if (!protocol.isRoot()
                    && !parent.getChildAt(2).equals(mAdbCard)) {
                parent.removeView(mRootCard);
                parent.addView(mRootCard);
            }

            Settings.putInt("mode", protocol.isRoot() ? 0 : 1);
        }

        mStartButton.setEnabled(true);
        mRestartButton.setEnabled(true);

        mRefreshTask = null;
        mStartTask = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
