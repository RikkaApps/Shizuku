package moe.shizuku.manager.adapter;

import android.content.Context;
import android.os.Process;

import java.util.ArrayList;

import moe.shizuku.api.ShizukuService;
import moe.shizuku.manager.ShizukuManagerSettings;
import moe.shizuku.manager.ShizukuManagerSettings.LaunchMethod;
import moe.shizuku.manager.viewholder.ManageAppsViewHolder;
import moe.shizuku.manager.viewholder.ServerStatusViewHolder;
import moe.shizuku.manager.viewholder.StartAdbViewHolder;
import moe.shizuku.manager.viewholder.StartRootViewHolder;
import moe.shizuku.manager.viewmodel.AppsViewModel;
import moe.shizuku.manager.viewmodel.HomeViewModel;
import moe.shizuku.support.recyclerview.IdBasedRecyclerViewAdapter;
import moe.shizuku.support.recyclerview.IndexCreatorPool;

public class HomeAdapter extends IdBasedRecyclerViewAdapter {

    private HomeViewModel mHomeModel;
    private AppsViewModel mAppsModel;

    public HomeAdapter(Context context, HomeViewModel homeModel, AppsViewModel appsModel) {
        super(new ArrayList<>());

        mHomeModel = homeModel;
        mAppsModel = appsModel;

        updateData(context);

        setHasStableIds(true);
    }

    @Override
    public IndexCreatorPool onCreateCreatorPool() {
        return new IndexCreatorPool();
    }

    public void updateData(Context context) {
        final HomeViewModel vm = mHomeModel;
        final boolean v3 = ShizukuService.pingBinder();

        clear();
        addItem(ServerStatusViewHolder.CREATOR, vm.getServiceStatus(), 0);

        addItem(ManageAppsViewHolder.CREATOR,
                (mAppsModel.getGrantedCount().getValue() != null && mAppsModel.getGrantedCount().getValue().data != null)
                        ? mAppsModel.getGrantedCount().getValue().data : 0, 1);

        if (Process.myUid() / 100000 == 0) {
            boolean root = ShizukuManagerSettings.getLastLaunchMode() == LaunchMethod.ROOT;
            boolean rootRestart = vm.getServiceStatus().getUid() == 0;
            if (v3) {
                try {
                    rootRestart |= ShizukuService.getUid() == 0;
                } catch (Throwable ignored) {
                }
            }

            if (root) {
                addItem(StartRootViewHolder.CREATOR, rootRestart, 3);
                addItem(StartAdbViewHolder.CREATOR, null, 2);
            } else {
                addItem(StartAdbViewHolder.CREATOR, null, 2);
                addItem(StartRootViewHolder.CREATOR, rootRestart, 3);
            }
        } else {

        }

        notifyDataSetChanged();
    }
}
