package moe.shizuku.manager.adapter;

import android.content.pm.PackageInfo;

import moe.shizuku.manager.viewholder.AppViewHolder;
import moe.shizuku.utils.recyclerview.BaseRecyclerViewAdapter;

/**
 * Created by Rikka on 2017/5/20.
 */

public class AppAdapter extends BaseRecyclerViewAdapter {

    public AppAdapter() {
        super();

        putRule(PackageInfo.class, AppViewHolder.CREATOR);
    }
}
