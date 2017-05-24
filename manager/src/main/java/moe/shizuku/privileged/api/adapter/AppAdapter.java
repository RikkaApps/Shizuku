package moe.shizuku.privileged.api.adapter;

import android.content.pm.PackageInfo;
import android.view.View;

import moe.shizuku.privileged.api.viewholder.AppViewHolder;
import moe.shizuku.utils.recyclerview.BaseRecyclerViewAdapter;
import moe.shizuku.utils.recyclerview.BaseViewHolder;

/**
 * Created by Rikka on 2017/5/20.
 */

public class AppAdapter extends BaseRecyclerViewAdapter<PackageInfo> {

    @Override
    public BaseViewHolder<PackageInfo> onCreateViewHolder(View view, int viewType) {
        return new AppViewHolder(view);
    }
}
