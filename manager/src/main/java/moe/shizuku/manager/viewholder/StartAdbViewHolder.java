package moe.shizuku.manager.viewholder;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;

import moe.shizuku.manager.R;
import moe.shizuku.manager.ShizukuManagerSettings;
import moe.shizuku.manager.utils.CustomTabsHelper;
import moe.shizuku.manager.utils.MultiLocaleEntity;
import moe.shizuku.support.recyclerview.BaseViewHolder;
import moe.shizuku.support.text.HtmlCompat;
import moe.shizuku.support.widget.HtmlCompatTextView;

public class StartAdbViewHolder extends BaseViewHolder<Object> {

    public static final Creator<Object> CREATOR = (inflater, parent) -> new StartAdbViewHolder(inflater.inflate(R.layout.item_home_start_adb, parent, false));

    private HtmlCompatTextView text;

    private static void startActivity(Context context, String url) {
        Uri uri = Uri.parse(url);
        if (!CustomTabsHelper.launchHelp(context, uri)) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(uri);

            try {
                context.startActivity(intent);
            } catch (Throwable tr) {
                try {
                    ((ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE))
                            .setPrimaryClip(new ClipData(ClipData.newPlainText("label", url)));

                    new AlertDialog.Builder(context)
                            .setTitle(R.string.dialog_cannot_open_browser_title)
                            .setMessage(HtmlCompat.fromHtml(context.getString(R.string.toast_copied_to_clipboard_with_text, url)))
                            .setPositiveButton(android.R.string.ok, null)
                            .show();
                } catch (Throwable ignored) {
                }
            }
        }
    }

    private static final MultiLocaleEntity HELP = new MultiLocaleEntity();

    static {
        HELP.put("zh-CN", "https://rikka.app/shizuku/docs/zh-CN/?doc=adb&title=使用%20adb%20启动");
        HELP.put("zh-TW", "https://rikka.app/shizuku/docs/zh-TW/?doc=adb&title=使用%20adb%20启动");
        HELP.put("en", "https://rikka.app/shizuku/docs/en/?doc=adb&title=Start%20with%20adb");
    }

    public StartAdbViewHolder(View itemView) {
        super(itemView);

        text = itemView.findViewById(android.R.id.text1);

        itemView.findViewById(android.R.id.button1).setOnClickListener(v -> {
            startActivity(v.getContext(), HELP.get());
        });

        /*itemView.findViewById(android.R.id.button2).setOnClickListener(v -> {
            Context context = v.getContext();
            if (ClipboardUtils.put(context, ServerLauncher.COMMAND_ADB)) {
                Toast.makeText(context, context.getString(R.string.copied_to_clipboard, ServerLauncher.COMMAND_ADB), Toast.LENGTH_SHORT).show();
            }
        });*/
    }
}