package moe.shizuku.manager.adapter;

import android.content.pm.PackageInfo;

import moe.shizuku.manager.viewholder.AppViewHolder;
import moe.shizuku.support.recyclerview.BaseRecyclerViewAdapter;
import moe.shizuku.support.recyclerview.ClassCreatorPool;

/**
 * Created by Rikka on 2017/5/20.
 */

public class AppAdapter extends BaseRecyclerViewAdapter<ClassCreatorPool> {

    public AppAdapter() {
        super();

        getCreatorPool().putRule(PackageInfo.class, AppViewHolder.CREATOR);
    }

    @Override
    public ClassCreatorPool onCreateCreatorPool() {
        return new ClassCreatorPool();
    }
}
