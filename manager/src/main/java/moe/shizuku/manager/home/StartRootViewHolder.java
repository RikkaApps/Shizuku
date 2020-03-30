package moe.shizuku.manager.home;

import android.app.Activity;
import android.content.Context;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Checkable;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ShareCompat;

import moe.shizuku.manager.R;
import moe.shizuku.manager.starter.ServerLauncher;
import moe.shizuku.manager.ShizukuManagerSettings;
import moe.shizuku.manager.starter.ShellService;
import moe.shizuku.manager.utils.BindServiceHelper;
import moe.shizuku.manager.widget.ExpandableLayout;
import rikka.core.util.ContextUtils;
import rikka.html.widget.HtmlCompatTextView;
import rikka.recyclerview.BaseViewHolder;

public class StartRootViewHolder extends BaseViewHolder<Boolean> implements View.OnClickListener, Checkable {

    public static final Creator<Boolean> CREATOR = (inflater, parent) -> new StartRootViewHolder(inflater.inflate(R.layout.item_home_start_root, parent, false));

    private Checkable expandableButton;
    private ExpandableLayout expandableLayout;

    private View start;
    private View restart;

    private AlertDialog mAlertDialog;

    private BindServiceHelper mBindServiceHelper;

    public StartRootViewHolder(View itemView) {
        super(itemView);

        expandableButton = itemView.findViewById(android.R.id.text2);
        ((View) expandableButton).setOnClickListener(this);
        expandableLayout = itemView.findViewById(R.id.expandable);

        mBindServiceHelper = new BindServiceHelper(itemView.getContext(), ShellService.class);

        View.OnClickListener listener = this::onStartClicked;

        start = itemView.findViewById(android.R.id.button1);
        restart = itemView.findViewById(android.R.id.button2);

        start.setOnClickListener(listener);
        restart.setOnClickListener(listener);

        itemView.<HtmlCompatTextView>findViewById(android.R.id.text1).setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void onStartClicked(View v) {
        startServer(v.getContext());
    }

    private void startShell(final Context context, final String... command) {
        start.setEnabled(false);
        restart.setEnabled(false);

        final StringBuilder sb = new StringBuilder();

        mAlertDialog = new AlertDialog.Builder(context)
                .setView(R.layout.dialog_shell)
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, null)
                .setNeutralButton(R.string.send_command, (dialog, which) -> {
                    Activity activity = ContextUtils.getActivity(context);
                    if (activity == null) {
                        return;
                    }

                    ShareCompat.IntentBuilder.from(activity)
                            .setText(sb.toString())
                            .setType("text/plain")
                            .setChooserTitle(R.string.send_command)
                            .startChooser();
                })
                .show();

        mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        mAlertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setVisibility(View.GONE);

        final TextView textView = mAlertDialog.findViewById(android.R.id.text1);
        if (textView == null) {
            return;
        }

        textView.setText(R.string.starting_shell);

        mBindServiceHelper.bind(binder -> {
            ShellService.ShellServiceBinder service = (ShellService.ShellServiceBinder) binder;

            service.run(command, new ShellService.Listener() {
                @Override
                public void onFailed() {
                    mBindServiceHelper.unbind();

                    if (mAlertDialog == null) {
                        return;
                    }

                    start.setEnabled(true);
                    restart.setEnabled(true);

                    mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);

                    textView.setText(R.string.cannot_start_no_root);
                }

                @Override
                public void onCommandResult(int exitCode) {
                    mBindServiceHelper.unbind();

                    if (mAlertDialog == null) {
                        return;
                    }

                    mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);

                    if (exitCode != 0) {
                        sb.append('\n').append("Send this to developer may help solve the problem.");
                        mAlertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setVisibility(View.VISIBLE);

                        start.setEnabled(true);
                        restart.setEnabled(true);
                    }
                }

                @Override
                public void onLine(String line) {
                    if (mAlertDialog == null) {
                        return;
                    }

                    if (sb.length() > 0) {
                        sb.append('\n');
                    }

                    sb.append(line);

                    textView.setText(sb.toString());
                }
            });
        });
    }

    private void startServer(Context context) {
        start.setEnabled(false);
        restart.setEnabled(false);

        if (ServerLauncher.COMMAND_ROOT == null) {
            ServerLauncher.writeFiles(context, false);
        }
        startShell(context, ServerLauncher.COMMAND_ROOT);
    }

    @Override
    public void onBind() {
        start.setEnabled(true);
        restart.setEnabled(true);

        if (getData()) {
            start.setVisibility(View.GONE);
            restart.setVisibility(View.VISIBLE);
        } else {
            start.setVisibility(View.VISIBLE);
            restart.setVisibility(View.GONE);
        }

        syncViewState();
    }

    @Override
    public void onRecycle() {
        super.onRecycle();

        mAlertDialog = null;
    }

    @Override
    public void onClick(View v) {
        setChecked(!isChecked());
        syncViewState();
    }

    @Override
    public void setChecked(boolean checked) {
        ShizukuManagerSettings.getPreferences().edit().putBoolean("root_help_expanded", checked).apply();
    }

    @Override
    public boolean isChecked() {
        return ShizukuManagerSettings.getPreferences().getBoolean("root_help_expanded", true);
    }

    @Override
    public void toggle() {
        setChecked(!isChecked());
    }

    private void syncViewState() {
        expandableButton.setChecked(isChecked());
        expandableLayout.setExpanded(isChecked());
    }
}