package moe.shizuku.manager.viewholder;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Toast;

import moe.shizuku.manager.R;
import moe.shizuku.manager.ServerLauncher;
import moe.shizuku.support.recyclerview.BaseViewHolder;
import moe.shizuku.support.utils.ClipboardUtils;
import moe.shizuku.support.widget.HtmlCompatTextView;

public class StartAdbViewHolder extends BaseViewHolder<Object> {

    public static final Creator<Object> CREATOR = (inflater, parent) -> new StartAdbViewHolder(inflater.inflate(R.layout.item_home_start_adb, parent, false));

    private HtmlCompatTextView text;

    public StartAdbViewHolder(View itemView) {
        super(itemView);

        text = itemView.findViewById(android.R.id.text1);

        itemView.findViewById(android.R.id.button1).setOnClickListener(v -> {
            Context context = v.getContext();
            if (ClipboardUtils.put(context, ServerLauncher.COMMAND_ADB)) {
                Toast.makeText(context, context.getString(R.string.copied_to_clipboard, ServerLauncher.COMMAND_ADB), Toast.LENGTH_SHORT).show();
            }
        });

        itemView.findViewById(android.R.id.button2).setOnClickListener(v -> {
            Context context = v.getContext();
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, ServerLauncher.COMMAND_ADB);
            intent = Intent.createChooser(intent, context.getString(R.string.send_command));
            context.startActivity(intent);
        });
    }

    @Override
    public void onBind() {
        Context context = itemView.getContext();

        text.setHtmlText(context.getString(R.string.start_service_summary_adb, ServerLauncher.COMMAND_ADB));
    }
}