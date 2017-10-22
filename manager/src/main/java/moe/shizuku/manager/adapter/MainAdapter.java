package moe.shizuku.manager.adapter;

import android.content.Context;
import android.os.Process;

import java.util.ArrayList;

import moe.shizuku.ShizukuState;
import moe.shizuku.manager.viewholder.ServerStatusViewHolder;
import moe.shizuku.support.recyclerview.BaseRecyclerViewAdapter;
import moe.shizuku.support.recyclerview.CreatorPool;

/**
 * Created by rikka on 2017/10/22.
 */

public class MainAdapter extends BaseRecyclerViewAdapter {


    public MainAdapter() {
        /*super(new ArrayList<>(), new CreatorPool() {

        });*/

        getCreatorPool()
                .putRule(ShizukuState.class, ServerStatusViewHolder.CREATOR);

        getItems().add(ShizukuState.createUnknown());

        // main user
        if (Process.myUid() / 100000 == 0) {

        }

        //setHasStableIds(true);
    }

    /*@Override
    public long getItemId(int position) {
        return getCreatorPool().getCreatorIndex(this, position);
    }*/
}
