package moe.shizuku.manager.adapter;

import android.content.Context;
import android.os.Process;

import java.util.ArrayList;

import moe.shizuku.ShizukuState;
import moe.shizuku.manager.authorization.AuthorizationManager;
import moe.shizuku.manager.ShizukuManagerSettings;
import moe.shizuku.manager.ShizukuManagerSettings.LaunchMethod;
import moe.shizuku.manager.viewholder.ManageAppsViewHolder;
import moe.shizuku.manager.viewholder.ServerStatusViewHolder;
import moe.shizuku.manager.viewholder.StartAdbViewHolder;
import moe.shizuku.manager.viewholder.StartRootViewHolder;
import moe.shizuku.support.recyclerview.BaseRecyclerViewAdapter;
import moe.shizuku.support.recyclerview.CreatorPool;

/**
 * Created by rikka on 2017/10/22.
 */

public class MainAdapter extends BaseRecyclerViewAdapter {

    private static class MainCreatorPool extends CreatorPool {

        @Override
        public int getCreatorIndex(BaseRecyclerViewAdapter adapter, int position) {
            if (adapter.getItemAt(position) == null) {
                return 3;
            }
            return super.getCreatorIndex(adapter, position);
        }
    }

    public MainAdapter(Context context) {
        super(new ArrayList<>(), new MainCreatorPool());

        getCreatorPool()
                .putRule(ShizukuState.class, ServerStatusViewHolder.CREATOR)
                .putRule(Integer.class, ManageAppsViewHolder.CREATOR)
                .putRule(Boolean.class, StartRootViewHolder.CREATOR)
                .putRule(Object.class, StartAdbViewHolder.CREATOR);

        updateData(context, ShizukuState.createUnknown());

        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        return getCreatorPool().getCreatorIndex(this, position);
    }

    public void updateData(Context context, ShizukuState state) {
        getItems().clear();

        getItems().add(state);

        if (state.isAuthorized()) {
            getItems().add(AuthorizationManager.getGrantedPackages(context).size());
        }

        if (Process.myUid() / 100000 == 0) {
            boolean adb = ShizukuManagerSettings.getLastLaunchMode() == LaunchMethod.ADB;
            boolean rootRestart = state.isRoot();

            if (adb) {
                getItems().add(null);
                getItems().add(rootRestart);
            } else {
                getItems().add(rootRestart);
                getItems().add(null);
            }
        }

        notifyDataSetChanged();
    }
}
