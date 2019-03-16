package moe.shizuku.manager.viewholder;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import moe.shizuku.manager.R;
import moe.shizuku.manager.ServerLauncher;
import moe.shizuku.support.recyclerview.BaseViewHolder;
import moe.shizuku.support.utils.ClipboardUtils;
import moe.shizuku.support.widget.HtmlCompatTextView;

/**
 * Created by rikka on 2017/10/23.
 */
public class StartAdbViewHolder extends BaseViewHolder<Object> {

    public static final Creator<Object> CREATOR = new Creator<Object>() {
        @Override
        public BaseViewHolder<Object> createViewHolder(LayoutInflater inflater, ViewGroup parent) {
            return new StartAdbViewHolder(inflater.inflate(R.layout.item_start_adb, parent, false));
        }
    };

    private HtmlCompatTextView text;

    public StartAdbViewHolder(View itemView) {
        super(itemView);

        text = itemView.findViewById(android.R.id.text1);

        itemView.findViewById(android.R.id.button1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = v.getContext();
                if (ClipboardUtils.put(context, ServerLauncher.COMMAND_ADB)) {
                    Toast.makeText(context, context.getString(R.string.copied_to_clipboard, ServerLauncher.COMMAND_ADB), Toast.LENGTH_SHORT).show();
                }
            }
        });

        itemView.findViewById(android.R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = v.getContext();
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, ServerLauncher.COMMAND_ADB);
                intent = Intent.createChooser(intent, context.getString(R.string.send_command));
                context.startActivity(intent);
            }
        });
    }

    @Override
    public void onBind() {
        Context context = itemView.getContext();

        text.setHtmlText(context.getString(R.string.start_server_summary_adb, ServerLauncher.COMMAND_ADB));
    }
}