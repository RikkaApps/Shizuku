package moe.shizuku.manager.management

import android.content.pm.PackageInfo
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Job
import moe.shizuku.manager.Helps
import moe.shizuku.manager.R
import moe.shizuku.manager.authorization.AuthorizationManager
import moe.shizuku.manager.databinding.AppListEmptyBinding
import moe.shizuku.manager.databinding.AppListItemBinding
import moe.shizuku.manager.ktx.toHtml
import moe.shizuku.manager.utils.AppIconCache
import moe.shizuku.manager.utils.ShizukuSystemApis
import moe.shizuku.manager.utils.UserHandleCompat
import rikka.html.text.HtmlCompat
import rikka.recyclerview.BaseViewHolder
import rikka.recyclerview.BaseViewHolder.Creator
import rikka.shizuku.Shizuku

class EmptyViewHolder(private val binding: AppListEmptyBinding) : BaseViewHolder<Any>(binding.root) {

    companion object {
        @JvmField
        val CREATOR = Creator<Any> { inflater: LayoutInflater, parent: ViewGroup? -> EmptyViewHolder(AppListEmptyBinding.inflate(inflater, parent, false)) }
    }

}
