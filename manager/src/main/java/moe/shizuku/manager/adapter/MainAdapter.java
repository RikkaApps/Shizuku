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
import moe.shizuku.support.recyclerview.BaseViewHolder;
import moe.shizuku.support.recyclerview.ClassCreatorPool;

/**
 * Created by rikka on 2017/10/22.
 */

public class MainAdapter extends BaseRecyclerViewAdapter<MainAdapter.MainCreatorPool> {

    private static final Object ITEM_ADB = new Object();

    @Override
    public MainCreatorPool onCreateCreatorPool() {
        return new MainCreatorPool();
    }

    public static class MainCreatorPool extends ClassCreatorPool {

        @Override
        public int getCreatorIndex(BaseRecyclerViewAdapter adapter, int position) {
            if (adapter.getItemAt(position) == ITEM_ADB) {
                return 3;
            }
            return super.getCreatorIndex(adapter, position);
        }

        @Override
        public BaseViewHolder.Creator getCreator(int index) {
            if (index == 3) {
                return StartAdbViewHolder.CREATOR;
            }
            return super.getCreator(index);
        }
    }

    public MainAdapter(Context context) {
        super(new ArrayList<>(), new MainCreatorPool());

        getCreatorPool()
                .putRule(ShizukuState.class, ServerStatusViewHolder.CREATOR)
                .putRule(Integer.class, ManageAppsViewHolder.CREATOR)
                .putRule(Boolean.class, StartRootViewHolder.CREATOR);

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
                getItems().add(ITEM_ADB);
                getItems().add(rootRestart);
            } else {
                getItems().add(rootRestart);
                getItems().add(ITEM_ADB);
            }
        }

        notifyDataSetChanged();
    }
}
