package moe.shizuku.manager.viewholder;

import android.view.View;

import moe.shizuku.manager.Helps;
import moe.shizuku.manager.R;
import moe.shizuku.manager.utils.CustomTabsHelper;
import moe.shizuku.manager.utils.MultiLocaleEntity;
import moe.shizuku.support.recyclerview.BaseViewHolder;

public class StartAdbViewHolder extends BaseViewHolder<Object> {

    public static final Creator<Object> CREATOR = (inflater, parent) -> new StartAdbViewHolder(inflater.inflate(R.layout.item_home_start_adb, parent, false));

    public StartAdbViewHolder(View itemView) {
        super(itemView);

        itemView.findViewById(android.R.id.button1).setOnClickListener(v -> CustomTabsHelper.launchUrlOrCopy(v.getContext(), Helps.ADB.get()));

        /*itemView.findViewById(android.R.id.button2).setOnClickListener(v -> {
            Context context = v.getContext();
            if (ClipboardUtils.put(context, ServerLauncher.COMMAND_ADB)) {
                Toast.makeText(context, context.getString(R.string.copied_to_clipboard, ServerLauncher.COMMAND_ADB), Toast.LENGTH_SHORT).show();
            }
        });*/
    }
}