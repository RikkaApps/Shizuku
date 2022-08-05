package moe.shizuku.manager.management;

import android.content.pm.PackageInfo;

import java.util.List;

import rikka.recyclerview.BaseRecyclerViewAdapter;
import rikka.recyclerview.ClassCreatorPool;

public class AppsAdapter extends BaseRecyclerViewAdapter<ClassCreatorPool> {

    public AppsAdapter() {
        super();

        getCreatorPool().putRule(PackageInfo.class, AppViewHolder.CREATOR);
        getCreatorPool().putRule(Object.class, EmptyViewHolder.CREATOR);
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
        if (data.isEmpty()) {
            getItems().add(new Object());
        } else {
            getItems().addAll(data);
        }
        notifyDataSetChanged();
    }
}
