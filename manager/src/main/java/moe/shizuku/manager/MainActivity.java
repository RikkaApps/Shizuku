package moe.shizuku.manager;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
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

import moe.shizuku.ShizukuConstants;
import moe.shizuku.ShizukuState;
import moe.shizuku.manager.adapter.MainAdapter;
import moe.shizuku.manager.service.ShellService;
import moe.shizuku.manager.service.WorkService;
import moe.shizuku.manager.utils.BindServiceHelper;
import moe.shizuku.manager.widget.HtmlTextView;
import moe.shizuku.support.recyclerview.RecyclerViewHelper;

/**
 * TODO notify user when not running in main user
 */
public class MainActivity extends BaseActivity {

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
    //private BroadcastReceiver mStartFailedReceiver;

    private boolean mCheckToRequest;

    private BindServiceHelper mBindServiceHelper = new BindServiceHelper(this, ShellService.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MainAdapter adapter = new MainAdapter();

        RecyclerView recyclerView = findViewById(android.R.id.list);
        recyclerView.setAdapter(adapter);

        RecyclerViewHelper.fixOverScroll(recyclerView);

        /*mStatus = findViewById(R.id.status);
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
                if (ClipboardUtils.put(v.getContext(), ServerLauncher.COMMAND_ADB)) {
                    Toast.makeText(v.getContext(), getString(R.string.copied_to_clipboard, ServerLauncher.COMMAND_ADB), Toast.LENGTH_SHORT).show();
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


        mBindServiceHelper.bind(null);*/

        mServerStartedReceiver = new ServerStartedReceiver();
    }

    private class ServerStartedReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //updateUI(intent.<ShizukuState>getParcelableExtra(Intents.EXTRA_RESULT));
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

        /*mAuthorizedAppsCount.setHtmlText(
                getResources().getQuantityString(R.plurals.authorized_apps_count, AuthorizationManager.grantedCount(), AuthorizationManager.grantedCount()));

        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mServerStartedReceiver, new IntentFilter(Intents.ACTION_AUTH_RESULT));*/

        /*LocalBroadcastManager.getInstance(this)
                .registerReceiver(mStartFailedReceiver, new IntentFilter(Intents.ACTION_START));*/
    }

    @Override
    protected void onStop() {
        super.onStop();

        /*LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(mServerStartedReceiver);*/

        /*LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(mStartFailedReceiver);*/
    }

    private void check() {
        //mStartButton.setEnabled(false);
        //mRestartButton.setEnabled(false);

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


    @Override
    protected void onDestroy() {
        super.onDestroy();

        mBindServiceHelper.unbind();
    }

    private void startShell(final String command) {
        final StringBuilder sb = new StringBuilder();

        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(R.layout.dialog_shell)
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, null)
                .setNeutralButton(R.string.send_command, new DialogInterface.OnClickListener() {
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

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setVisibility(View.GONE);

        final TextView textView = dialog.findViewById(android.R.id.text1);

        mBindServiceHelper.bind(new BindServiceHelper.OnServiceConnectedListener() {

            @Override
            public void onServiceConnected(IBinder binder) {
                ShellService.ShellServiceBinder service = (ShellService.ShellServiceBinder) binder;

                service.run(command, 0, new ShellService.Listener() {
                    @Override
                    public void onFailed() {
                        if (isFinishing()) {
                            return;
                        }

                        mStartButton.setEnabled(true);
                        mRestartButton.setEnabled(true);

                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);

                        textView.setText(R.string.cannot_start_no_root);
                    }

                    @Override
                    public void onCommandResult(int commandCode, int exitCode) {
                        if (isFinishing()) {
                            return;
                        }

                        mStartButton.setEnabled(true);
                        mRestartButton.setEnabled(true);

                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);

                        if (exitCode != 0) {
                            sb.append('\n').append("Send this to developer may help solve the problem.");
                            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onLine(String line) {
                        if (isFinishing()) {
                            return;
                        }

                        if (sb.length() > 0) {
                            sb.append('\n');
                        }

                        sb.append(line);

                        textView.setText(sb.toString());
                    }
                });
            }
        });
    }

    private void startNew() {
        mStartButton.setEnabled(false);
        mRestartButton.setEnabled(false);

        startShell(ServerLauncher.COMMAND_ROOT);
    }

    private void startOld() {
        mStartButton.setEnabled(false);
        mRestartButton.setEnabled(false);

        startShell(ServerLauncher.COMMAND_ROOT_OLD);
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
