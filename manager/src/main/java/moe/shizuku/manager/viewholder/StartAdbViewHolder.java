package moe.shizuku.manager.viewholder;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Checkable;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import moe.shizuku.manager.Helps;
import moe.shizuku.manager.R;
import moe.shizuku.manager.ServerLauncher;
import moe.shizuku.manager.ShizukuManagerSettings;
import moe.shizuku.manager.utils.CustomTabsHelper;
import moe.shizuku.manager.widget.ExpandableLayout;
import rikka.core.util.ClipboardUtils;
import rikka.html.text.HtmlCompat;
import rikka.recyclerview.BaseViewHolder;

public class StartAdbViewHolder extends BaseViewHolder<Object> implements View.OnClickListener, Checkable {

    public static final Creator<Object> CREATOR = (inflater, parent) -> new StartAdbViewHolder(inflater.inflate(R.layout.item_home_start_adb, parent, false));

    private Checkable expandableButton;
    private ExpandableLayout expandableLayout;

    public StartAdbViewHolder(View itemView) {
        super(itemView);

        expandableButton = itemView.findViewById(android.R.id.text2);
        ((View) expandableButton).setOnClickListener(this);
        expandableLayout = itemView.findViewById(R.id.expandable);

        itemView.findViewById(android.R.id.button1).setOnClickListener(v -> CustomTabsHelper.launchUrlOrCopy(v.getContext(), Helps.ADB.get()));

        itemView.findViewById(android.R.id.button2).setOnClickListener(v -> {
            Context context = v.getContext();
            new AlertDialog.Builder(context)
                    .setTitle(R.string.view_command)
                    .setMessage(HtmlCompat.fromHtml(context.getString(R.string.view_command_message, ServerLauncher.COMMAND_ADB)))
                    .setPositiveButton(R.string.copy_command, (dialog, which) -> {
                        if (ClipboardUtils.put(context, ServerLauncher.COMMAND_ADB)) {
                            Toast.makeText(context, context.getString(R.string.copied_to_clipboard, ServerLauncher.COMMAND_ADB), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .setNeutralButton(R.string.send_command, (dialog, which) -> {
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setType("text/plain");
                        intent.putExtra(Intent.EXTRA_TEXT, ServerLauncher.COMMAND_ADB);
                        intent = Intent.createChooser(intent, context.getString(R.string.send_command));
                        context.startActivity(intent);
                    })
                    .show();
        });
    }

    @Override
    public void onClick(View v) {
        setChecked(!isChecked());
        syncViewState();
    }

    @Override
    public void onBind() {
        syncViewState();
    }

    @Override
    public void setChecked(boolean checked) {
        ShizukuManagerSettings.getPreferences().edit().putBoolean("adb_help_expanded", checked).apply();
    }

    @Override
    public boolean isChecked() {
        return ShizukuManagerSettings.getPreferences().getBoolean("adb_help_expanded", true);
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