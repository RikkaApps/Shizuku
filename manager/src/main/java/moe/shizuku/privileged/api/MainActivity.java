package moe.shizuku.privileged.api;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import moe.shizuku.privileged.api.service.WorkService;
import moe.shizuku.privileged.api.widget.HtmlTextView;
import moe.shizuku.server.Protocol;
import moe.shizuku.support.utils.Settings;

import static moe.shizuku.privileged.api.ServerLauncher.COMMAND_ADB;

public class MainActivity extends AppCompatActivity {

    private View mStatus;
    private TextView mStatusText;
    private ImageView mStatusIcon;

    private TextView mStartButton;
    private TextView mRestartButton;

    private View mRootCard;
    private View mAdbCard;
    private View mAppsCard;

    private HtmlTextView mAuthorizedAppsCount;

    private BroadcastReceiver mServerStartedReceiver;
    private BroadcastReceiver mStartFailedReceiver;

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

        mServerStartedReceiver = new ServerStartedReceiver();
        mStartFailedReceiver = new StartFailedReceiver();
    }

    private class ServerStartedReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateUI(intent.<Protocol>getParcelableExtra(getPackageName() + "intent.extra.RESULT"));
        }
    }

    private class StartFailedReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            mStartButton.setEnabled(true);
            mRestartButton.setEnabled(true);

            int code = intent.getIntExtra(getPackageName() + "intent.extra.CODE", 0);
            if (code == 0) {
                return;
            }

            if (code != 99) {
                final StringBuilder sb = new StringBuilder();
                sb.append("code:").append(code).append("\n\n");
                //sb.append("OUT:\n");
                if (intent.hasExtra(getPackageName() + "intent.extra.OUTPUT")) {
                    for (String s : intent.getStringArrayListExtra(getPackageName() + "intent.extra.OUTPUT")) {
                        sb.append(s).append('\n');
                    }
                }
                /*sb.append("\nERR:\n");
                if (intent.hasExtra(getPackageName() + "intent.extra.ERROR")) {
                    sb.append(intent.getStringExtra(getPackageName() + "intent.extra.ERROR"));
                }*/
                sb.append("\n\nSend this to developer may help solve the problem.\n\nYou can temporarily use the old method as an alternative.");

                Dialog dialog = new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Something went wrong")
                        .setMessage(sb.toString().trim())
                        .setNeutralButton("Try old method", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Settings.getBoolean("root_use_old", true);
                                WorkService.startServerOld(MainActivity.this);
                            }
                        })
                        .setPositiveButton(R.string.send_command, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ShareCompat.IntentBuilder.from(MainActivity.this)
                                        .setText(sb.toString())
                                        .setType("text/plain")
                                        .setChooserTitle(R.string.send_command)
                                        .startChooser();
                            }
                        })
                        .show();

                ((TextView) dialog.findViewById(android.R.id.message)).setTextIsSelectable(true);
                ((TextView) dialog.findViewById(android.R.id.message)).setTypeface(Typeface.create("monospace", Typeface.NORMAL));
            } else {
                Toast.makeText(context, "Can\'t start server because not root permission.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        check();
    }

    @Override
    protected void onStart() {
        super.onStart();

        mAuthorizedAppsCount.setHtmlText(
                getResources().getQuantityString(R.plurals.authorized_apps_count, Permissions.grantedCount(), Permissions.grantedCount()));

        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mServerStartedReceiver, new IntentFilter(getPackageName() + ".intent.action.AUTH_RESULT"));

        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mStartFailedReceiver, new IntentFilter(getPackageName() + ".intent.action.START"));
    }

    @Override
    protected void onStop() {
        super.onStop();

        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(mServerStartedReceiver);

        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(mStartFailedReceiver);
    }

    private void check() {
        mStartButton.setEnabled(false);
        mRestartButton.setEnabled(false);

        if (mCheckToRequest) {
            WorkService.startRequestToken(this);
        } else {
            WorkService.startAuth(this);
        }
    }

    private void start() {
        mStartButton.setEnabled(false);
        mRestartButton.setEnabled(false);

        if (!Settings.getBoolean("root_use_old", false)) {
            WorkService.startServer(this);
        } else {
            WorkService.startServerOld(this);
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
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
