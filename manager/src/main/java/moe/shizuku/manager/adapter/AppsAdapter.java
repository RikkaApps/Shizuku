package moe.shizuku.manager.adapter;

import android.content.pm.PackageInfo;

import java.util.List;

import moe.shizuku.manager.viewholder.AppViewHolder;
import moe.shizuku.support.recyclerview.BaseRecyclerViewAdapter;
import moe.shizuku.support.recyclerview.ClassCreatorPool;

public class AppsAdapter extends BaseRecyclerViewAdapter<ClassCreatorPool> {

    public AppsAdapter() {
        super();

        getCreatorPool().putRule(PackageInfo.class, AppViewHolder.CREATOR);
        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        return getItemAt(position).hashCode();
    }

    @Override
    public ClassCreatorPool onCreateCreatorPool() {
        return new ClassCreatorPool();
    }

    public void updateData(List<PackageInfo> data) {
        getItems().clear();
        getItems().addAll(data);
        notifyDataSetChanged();
    }
}
