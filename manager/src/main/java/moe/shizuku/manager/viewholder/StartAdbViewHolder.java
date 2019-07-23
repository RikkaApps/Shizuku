package moe.shizuku.manager.viewholder;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Toast;

import moe.shizuku.manager.Helps;
import moe.shizuku.manager.R;
import moe.shizuku.manager.ServerLauncher;
import moe.shizuku.manager.utils.CustomTabsHelper;
import moe.shizuku.support.recyclerview.BaseViewHolder;
import moe.shizuku.support.text.HtmlCompat;
import moe.shizuku.support.utils.ClipboardUtils;

public class StartAdbViewHolder extends BaseViewHolder<Object> {

    public static final Creator<Object> CREATOR = (inflater, parent) -> new StartAdbViewHolder(inflater.inflate(R.layout.item_home_start_adb, parent, false));

    public StartAdbViewHolder(View itemView) {
        super(itemView);

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
}