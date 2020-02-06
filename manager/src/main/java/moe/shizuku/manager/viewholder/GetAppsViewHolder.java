package moe.shizuku.manager.viewholder;

import android.view.View;

import moe.shizuku.manager.Helps;
import moe.shizuku.manager.R;
import moe.shizuku.manager.utils.CustomTabsHelper;
import rikka.recyclerview.BaseViewHolder;

public class GetAppsViewHolder extends BaseViewHolder<Object> {

    public static final Creator<Object> CREATOR = (inflater, parent) -> new GetAppsViewHolder(inflater.inflate(R.layout.item_home_get_apps, parent, false));

    public GetAppsViewHolder(View itemView) {
        super(itemView);

        itemView.setOnClickListener(v -> CustomTabsHelper.launchUrlOrCopy(v.getContext(), Helps.APPS.get()));
    }
}