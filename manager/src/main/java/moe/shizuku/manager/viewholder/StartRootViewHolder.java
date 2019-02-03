package moe.shizuku.manager.viewholder;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.core.app.ShareCompat;
import moe.shizuku.manager.R;
import moe.shizuku.manager.ServerLauncher;
import moe.shizuku.manager.service.ShellService;
import moe.shizuku.manager.utils.BindServiceHelper;
import moe.shizuku.support.recyclerview.BaseViewHolder;
import moe.shizuku.support.utils.ContextUtils;

public class StartRootViewHolder extends BaseViewHolder<Boolean> {

    public static final Creator<Boolean> CREATOR = (inflater, parent) -> new StartRootViewHolder(inflater.inflate(R.layout.item_start_root, parent, false));

    private View start;
    private View restart;

    private AlertDialog mAlertDialog;

    private BindServiceHelper mBindServiceHelper;

    public StartRootViewHolder(View itemView) {
        super(itemView);

        mBindServiceHelper = new BindServiceHelper(itemView.getContext(), ShellService.class);

        View.OnClickListener listener = this::onStartClicked;

        start = itemView.findViewById(android.R.id.button1);
        restart = itemView.findViewById(android.R.id.button2);

        start.setOnClickListener(listener);
        restart.setOnClickListener(listener);
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

        startShell(context, ServerLauncher.COMMAND_ROOT[0]);
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
    }

    @Override
    public void onRecycle() {
        super.onRecycle();

        mAlertDialog = null;
    }
}