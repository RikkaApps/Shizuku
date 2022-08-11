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

    init {
        updateData()
        setHasStableIds(true)
    }

    companion object {

        private const val ID_STATUS = 0L
        private const val ID_APPS = 1L
        private const val ID_TERMINAL = 2L
        private const val ID_START_ROOT = 3L
        private const val ID_START_WADB = 4L
        private const val ID_START_ADB = 5L
        private const val ID_LEARN_MORE = 6L
        private const val ID_ADB_PERMISSION_LIMITED = 7L
    }

    override fun onCreateCreatorPool(): IndexCreatorPool {
        return IndexCreatorPool()
    }

    fun updateData() {
        val status = homeModel.serviceStatus.value?.data ?: return
        val grantedCount = appsModel.grantedCount.value?.data ?: 0
        val adbPermission = status.permission
        val running = status.isRunning
        val isPrimaryUser = UserHandleCompat.myUserId() == 0

        clear()
        addItem(ServerStatusViewHolder.CREATOR, status, ID_STATUS)

        if (adbPermission) {
            addItem(ManageAppsViewHolder.CREATOR, status to grantedCount, ID_APPS)
            addItem(TerminalViewHolder.CREATOR, status, ID_TERMINAL)
        }

        if (running && !adbPermission) {
            addItem(AdbPermissionLimitedViewHolder.CREATOR, status, ID_ADB_PERMISSION_LIMITED)
        }

        if (isPrimaryUser) {
            val root = EnvironmentUtils.isRooted()
            val rootRestart = running && status.uid == 0

            if (root) {
                addItem(StartRootViewHolder.CREATOR, rootRestart, ID_START_ROOT)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R || EnvironmentUtils.getAdbTcpPort() > 0) {
                addItem(StartWirelessAdbViewHolder.CREATOR, null, ID_START_WADB)
            }

            addItem(StartAdbViewHolder.CREATOR, null, ID_START_ADB)

            if (!root) {
                addItem(StartRootViewHolder.CREATOR, rootRestart, ID_START_ROOT)
            }
        }
        addItem(LearnMoreViewHolder.CREATOR, null, ID_LEARN_MORE)
        notifyDataSetChanged()
    }
}
