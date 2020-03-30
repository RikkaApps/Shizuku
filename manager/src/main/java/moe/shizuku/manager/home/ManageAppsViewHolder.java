package moe.shizuku.manager.home;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import moe.shizuku.api.ShizukuService;
import moe.shizuku.manager.management.ApplicationManagementActivity;
import moe.shizuku.manager.R;
import rikka.html.widget.HtmlCompatTextView;
import rikka.recyclerview.BaseViewHolder;

public class ManageAppsViewHolder extends BaseViewHolder<Integer> implements View.OnClickListener {

    public static final Creator<Integer> CREATOR = (inflater, parent) -> new ManageAppsViewHolder(inflater.inflate(R.layout.item_home_manage_apps, parent, false));

    private HtmlCompatTextView title;
    private HtmlCompatTextView summary;

    public ManageAppsViewHolder(View itemView) {
        super(itemView);

        title = itemView.findViewById(android.R.id.text1);
        summary = itemView.findViewById(android.R.id.text2);

        itemView.setOnClickListener(this);
    }

    @Override
    public void onBind() {
        Context context = itemView.getContext();

        title.setHtmlText(context.getResources().getQuantityString(R.plurals.authorized_apps_count, getData(), getData()));

        if (!ShizukuService.pingBinder()) {
            itemView.setEnabled(false);
            summary.setHtmlText(context.getString(R.string.service_not_running, context.getString(R.string.service_name)));
        } else {
            itemView.setEnabled(true);
            summary.setHtmlText(context.getString(R.string.view_authorized_apps));
        }
    }

    @Override
    public void onClick(View v) {
        v.getContext().startActivity(new Intent(v.getContext(), ApplicationManagementActivity.class));
    }
}