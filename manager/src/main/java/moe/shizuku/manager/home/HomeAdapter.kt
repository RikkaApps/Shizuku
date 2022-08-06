package moe.shizuku.manager.home

import android.os.Build
import moe.shizuku.manager.management.AppsViewModel
import moe.shizuku.manager.utils.EnvironmentUtils
import moe.shizuku.manager.utils.UserHandleCompat
import rikka.recyclerview.IdBasedRecyclerViewAdapter
import rikka.recyclerview.IndexCreatorPool
import rikka.shizuku.Shizuku

class HomeAdapter(private val homeModel: HomeViewModel, private val appsModel: AppsViewModel) :
    IdBasedRecyclerViewAdapter(ArrayList()) {

    override fun onCreateCreatorPool(): IndexCreatorPool {
        return IndexCreatorPool()
    }

    fun updateData() {
        val status = homeModel.serviceStatus.value?.data ?: return
        val grantedCount = appsModel.grantedCount.value?.data ?: 0
        val running = Shizuku.pingBinder()
        val isPrimaryUser = UserHandleCompat.myUserId() == 0

        clear()
        addItem(ServerStatusViewHolder.CREATOR, status, 0)

        addItem(ManageAppsViewHolder.CREATOR, status to grantedCount, 1)
        addItem(TerminalViewHolder.CREATOR, status, 1)

        if (isPrimaryUser) {
            val root = EnvironmentUtils.isRooted()
            val rootRestart = running && status.uid == 0

            if (root) {
                addItem(StartRootViewHolder.CREATOR, rootRestart, 3)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R || EnvironmentUtils.getAdbTcpPort() > 0) {
                addItem(StartWirelessAdbViewHolder.CREATOR, null, 4)
            }

            addItem(StartAdbViewHolder.CREATOR, null, 2)

            if (!root) {
                addItem(StartRootViewHolder.CREATOR, rootRestart, 3)
            }
        }
        addItem(LearnMoreViewHolder.CREATOR, null, 100)
        notifyDataSetChanged()
    }

    init {
        updateData()
        setHasStableIds(true)
    }
}
