package moe.shizuku.manager.adapter;

import android.content.Context;
import android.os.Process;

import java.util.ArrayList;

import moe.shizuku.api.ShizukuClientV3;
import moe.shizuku.manager.ShizukuManagerSettings;
import moe.shizuku.manager.ShizukuManagerSettings.LaunchMethod;
import moe.shizuku.manager.viewholder.ManageAppsViewHolder;
import moe.shizuku.manager.viewholder.ServerStatusViewHolder;
import moe.shizuku.manager.viewholder.StartAdbViewHolder;
import moe.shizuku.manager.viewholder.StartRootViewHolder;
import moe.shizuku.manager.viewmodel.AppsViewModel;
import moe.shizuku.manager.viewmodel.HomeViewModel;
import moe.shizuku.server.IShizukuService;
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
        final IShizukuService service = ShizukuClientV3.get();

        clear();
        addItem(ServerStatusViewHolder.CREATOR, vm.getServiceStatus(), 0);

        addItem(ManageAppsViewHolder.CREATOR, mAppsModel.getData() != null ? mAppsModel.getData().size() : 0, 1);

        if (Process.myUid() / 100000 == 0) {
            boolean adb = ShizukuManagerSettings.getLastLaunchMode() == LaunchMethod.ADB;
            boolean rootRestart = vm.getServiceStatus().getV2Status().isRoot();
            if (service != null) {
                try {
                    rootRestart |= service.getUid() == 0;
                } catch (Throwable ignored) {
                }
            }

            if (adb) {
                addItem(StartAdbViewHolder.CREATOR, null, 2);
                addItem(StartRootViewHolder.CREATOR, rootRestart, 3);
            } else {
                addItem(StartRootViewHolder.CREATOR, rootRestart, 3);
                addItem(StartAdbViewHolder.CREATOR, null, 2);
            }
        } else {

        }

        notifyDataSetChanged();
    }
}
