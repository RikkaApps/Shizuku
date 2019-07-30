package moe.shizuku.manager.viewholder;

import android.view.View;

import moe.shizuku.manager.Helps;
import moe.shizuku.manager.R;
import moe.shizuku.manager.utils.CustomTabsHelper;
import moe.shizuku.support.recyclerview.BaseViewHolder;

public class LearnMoreViewHolder extends BaseViewHolder<Object> {

    public static final Creator<Object> CREATOR = (inflater, parent) -> new LearnMoreViewHolder(inflater.inflate(R.layout.item_home_learn_more, parent, false));

    public LearnMoreViewHolder(View itemView) {
        super(itemView);

        itemView.setOnClickListener(v -> CustomTabsHelper.launchUrlOrCopy(v.getContext(), Helps.HOME.get()));
    }
}