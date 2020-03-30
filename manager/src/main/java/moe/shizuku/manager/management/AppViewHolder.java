package moe.shizuku.manager.management;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import java.util.List;

import moe.shizuku.api.ShizukuService;
import moe.shizuku.manager.R;
import moe.shizuku.manager.authorization.AuthorizationManager;
import rikka.html.text.HtmlCompat;
import rikka.recyclerview.BaseViewHolder;

public class AppViewHolder extends BaseViewHolder<PackageInfo> implements View.OnClickListener {

    public static final Creator<PackageInfo> CREATOR = (inflater, parent) -> new AppViewHolder(inflater.inflate(R.layout.item_applist, parent, false));

    private ImageView icon;
    private TextView name;
    private TextView pkg;
    private Switch switch_widget;
    private View v3;
    private View root;

    public AppViewHolder(View itemView) {
        super(itemView);

        icon = itemView.findViewById(android.R.id.icon);
        name = itemView.findViewById(android.R.id.title);
        pkg = itemView.findViewById(android.R.id.summary);
        switch_widget = itemView.findViewById(R.id.switch_widget);
        v3 = itemView.findViewById(R.id.requires_shizuku_v2);
        root = itemView.findViewById(R.id.requires_root);

        itemView.setFilterTouchesWhenObscured(true);
        itemView.setOnClickListener(this);
    }

    private String getPackageName() {
        return getData().packageName;
    }

    private int getUid() {
        return getData().applicationInfo.uid;
    }

    @Override
    public void onClick(View v) {
        final Context context = v.getContext();

        try {
            if (AuthorizationManager.granted(getPackageName(), getUid())) {
                AuthorizationManager.revoke(getPackageName(), getUid());
            } else {
                AuthorizationManager.grant(getPackageName(), getUid());
            }
        } catch (SecurityException e) {
            int uid;
            try {
                uid = ShizukuService.getUid();
            } catch (Throwable ex) {
                return;
            }

            if (uid != 0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context)
                        .setTitle(R.string.adb_no_permission_title)
                        .setMessage(HtmlCompat.fromHtml(context.getString(R.string.adb_no_permission_message)))
                        .setPositiveButton(android.R.string.ok, null);
                try {
                    builder.show();
                } catch (Throwable ignored) {
                }
            }
        }

        getAdapter().notifyItemChanged(getAdapterPosition(), new Object());
    }

    @Override
    public void onBind() {
        Context context = itemView.getContext();
        PackageManager pm = itemView.getContext().getPackageManager();
        ApplicationInfo ai = getData().applicationInfo;

        icon.setImageDrawable(ai.loadIcon(pm));
        name.setText(ai.loadLabel(pm));
        pkg.setText(ai.packageName);
        switch_widget.setChecked(AuthorizationManager.granted(getPackageName(), getUid()));

        v3.setVisibility((ai.metaData == null || !ai.metaData.getBoolean("moe.shizuku.client.V3_SUPPORT")) ? View.VISIBLE : View.GONE);
        root.setVisibility((ai.metaData != null && ai.metaData.getBoolean("moe.shizuku.client.V3_REQUIRES_ROOT")) ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onBind(@NonNull List<Object> payloads) {
        Context context = itemView.getContext();
        ApplicationInfo ai = getData().applicationInfo;

        switch_widget.setChecked(AuthorizationManager.granted(getPackageName(), getUid()));
    }
}
