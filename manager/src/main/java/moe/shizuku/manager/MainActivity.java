package moe.shizuku.manager;

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
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import moe.shizuku.ShizukuConstants;
import moe.shizuku.ShizukuState;
import moe.shizuku.manager.service.WorkService;
import moe.shizuku.manager.widget.HtmlTextView;

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

        mStatus = findViewById(R.id.status);
        mStatusText = findViewById(R.id.status_text);
        mStatusIcon = findViewById(R.id.status_icon);

        mStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mStartButton.setEnabled(false);
                mRestartButton.setEnabled(false);

                if (mCheckToRequest) {
                    WorkService.startRequestToken(v.getContext());
                } else {
                    WorkService.startAuth(v.getContext());
                }
            }
        });

        mStartButton = findViewById(R.id.start);
        mRestartButton = findViewById(R.id.restart);

        mRootCard = findViewById(R.id.root);
        mAdbCard = findViewById(R.id.adb);
        mAppsCard = findViewById(R.id.apps);

        mAuthorizedAppsCount = findViewById(R.id.authorized_apps_count);

        mAppsCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(v.getContext(), ManageAppsActivity.class));
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
                    ClipData clip = android.content.ClipData.newPlainText("label", ServerLauncher.COMMAND_ADB);
                    clipboard.setPrimaryClip(clip);

                    Toast.makeText(v.getContext(), getString(R.string.copied_to_clipboard, ServerLauncher.COMMAND_ADB), Toast.LENGTH_SHORT).show();
                } catch (Exception ignored) {
                }
            }
        });

        findViewById(R.id.send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, ServerLauncher.COMMAND_ADB);
                intent = Intent.createChooser(intent, getString(R.string.send_command));
                startActivity(intent);
            }
        });

        mServerStartedReceiver = new ServerStartedReceiver();
        mStartFailedReceiver = new StartResultReceiver();
    }

    private class ServerStartedReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateUI(intent.<ShizukuState>getParcelableExtra(Intents.EXTRA_RESULT));
        }
    }

    private class StartResultReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            mStartButton.setEnabled(true);
            mRestartButton.setEnabled(true);

            int code = intent.getIntExtra(Intents.EXTRA_CODE, 0);
            if (code == 0) {
                return;
            }

            if (code != 99) {
                if (intent.getBooleanExtra(Intents.EXTRA_IS_OLD, false)) {
                    return;
                }

                final StringBuilder sb = new StringBuilder();
                sb.append("code:").append(code).append("\n\n");
                if (intent.hasExtra(Intents.EXTRA_OUTPUT)) {
                    for (String s : intent.getStringArrayListExtra(Intents.EXTRA_OUTPUT)) {
                        sb.append(s).append('\n');
                    }
                }
                sb.append("\n\nSend this to developer may help solve the problem.\n\nYou can temporarily use the old method as an alternative.");

                Dialog dialog = new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Something went wrong")
                        .setMessage(sb.toString().trim())
                        .setNeutralButton("Use alternative method", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ShizukuManagerSettings.setRootLaunchMethod(ShizukuManagerSettings.RootLaunchMethod.ALTERNATIVE);
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
                Toast.makeText(context, R.string.cant_start_no_root, Toast.LENGTH_SHORT).show();
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
                .registerReceiver(mServerStartedReceiver, new IntentFilter(Intents.ACTION_AUTH_RESULT));

        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mStartFailedReceiver, new IntentFilter(Intents.ACTION_START));
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

        WorkService.startAuth(this);
    }

    private void startChoose() {
        final Dialog dialog = new AlertDialog.Builder(MainActivity.this)
                .setTitle(R.string.start_root_choose_launch_method)
                .setView(R.layout.dialog_start_root)
                .show();

        final CheckBox remember = dialog.findViewById(android.R.id.checkbox);

        dialog.findViewById(android.R.id.button1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startNew();

                if (remember.isChecked()) {
                    ShizukuManagerSettings.setRootLaunchMethod(ShizukuManagerSettings.RootLaunchMethod.USUAL);
                }
                dialog.dismiss();
            }
        });

        dialog.findViewById(android.R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startOld();

                if (remember.isChecked()) {
                    ShizukuManagerSettings.setRootLaunchMethod(ShizukuManagerSettings.RootLaunchMethod.ALTERNATIVE);
                }
                dialog.dismiss();
            }
        });
    }

    private void startNew() {
        mStartButton.setEnabled(false);
        mRestartButton.setEnabled(false);

        WorkService.startServer(this);
    }

    private void startOld() {
        mStartButton.setEnabled(false);
        mRestartButton.setEnabled(false);

        WorkService.startServerOld(this);
    }

    private void start() {
        switch (ShizukuManagerSettings.getRootLaunchMethod()) {
            case ShizukuManagerSettings.RootLaunchMethod.ASK:
                startChoose();
                break;
            case ShizukuManagerSettings.RootLaunchMethod.USUAL:
                startNew();
                break;
            case ShizukuManagerSettings.RootLaunchMethod.ALTERNATIVE:
                startOld();
                break;
        }
    }

    private void updateUI(ShizukuState shizukuState) {
        ((HtmlTextView) findViewById(R.id.adb_text)).setHtmlText(getString(R.string.start_server_summary_adb, ServerLauncher.COMMAND_ADB));

        mCheckToRequest = false;

        boolean ok = false;
        boolean running = false;
        switch (shizukuState.getCode()) {
            case ShizukuState.RESULT_OK:
                running = true;
                ok = true;
                break;
            case ShizukuState.RESULT_SERVER_DEAD:
            case ShizukuState.RESULT_UNAUTHORIZED:
                running = true;
                break;
            case ShizukuState.RESULT_UNKNOWN:
                break;
        }

        boolean oldOK = mStatusText.getCurrentTextColor() == ContextCompat.getColor(this, R.color.status_ok);
        if (ok) {
            mStatusIcon.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.status_ok));
            mStatusIcon.setImageDrawable(getDrawable(R.drawable.ic_server_ok_48dp));
            mStatusText.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.status_ok));

            if (!oldOK) {
                View view = (View) mStatusIcon.getParent();
                int centerX = mStatusIcon.getWidth() / 2;
                int centerY = mStatusIcon.getHeight() / 2;
                float radius = (float) Math.sqrt(centerX * centerX + (centerY + mStatusText.getHeight()) * (centerY + mStatusText.getHeight()));
                ViewAnimationUtils.createCircularReveal(view, centerX, centerY, 0, radius).start();
            }
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
                if (shizukuState.versionUnmatched()) {
                    mStatusText.setText(getString(R.string.server_running_update, shizukuState.isRoot() ? "root" : "adb", shizukuState.getVersion(), ShizukuConstants.VERSION));
                } else {
                    mStatusText.setText(getString(R.string.server_running, shizukuState.isRoot() ? "root" : "adb", shizukuState.getVersion()));
                }

                if (shizukuState.isRoot()) {
                    mStartButton.setVisibility(View.GONE);
                    mRestartButton.setVisibility(View.VISIBLE);

                    if (shizukuState.versionUnmatched()) {
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

                if (shizukuState.getCode() == ShizukuState.RESULT_UNAUTHORIZED) {
                    mStatusText.setText(R.string.server_no_token);
                    mCheckToRequest = true;
                } else {
                    mStatusText.setText(R.string.server_require_restart);
                }

                mAppsCard.setVisibility(View.GONE);
            }

            ViewGroup parent = (ViewGroup) mRootCard.getParent();
            if (shizukuState.isRoot()
                    && !parent.getChildAt(2).equals(mRootCard)) {
                parent.removeView(mAdbCard);
                parent.addView(mAdbCard);
            } else if (!shizukuState.isRoot()
                    && !parent.getChildAt(2).equals(mAdbCard)) {
                parent.removeView(mRootCard);
                parent.addView(mRootCard);
            }

            ShizukuManagerSettings.setLastLaunchMode(shizukuState.isRoot() ? ShizukuManagerSettings.LaunchMethod.ROOT : ShizukuManagerSettings.LaunchMethod.ADB);
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
