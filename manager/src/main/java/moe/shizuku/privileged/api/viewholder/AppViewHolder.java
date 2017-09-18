package moe.shizuku.privileged.api.viewholder;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import java.util.List;

import moe.shizuku.privileged.api.Permissions;
import moe.shizuku.privileged.api.R;
import moe.shizuku.privileged.api.ServerLauncher;
import moe.shizuku.utils.recyclerview.BaseViewHolder;

/**
 * Created by Rikka on 2017/5/20.
 */

public class AppViewHolder extends BaseViewHolder<PackageInfo> implements View.OnClickListener {

    public static final Creator<PackageInfo> CREATOR = new Creator<PackageInfo>() {
        @Override
        public BaseViewHolder<PackageInfo> createViewHolder(LayoutInflater inflater, ViewGroup parent) {
            return new AppViewHolder(inflater.inflate(R.layout.item_app, parent, false));
        }
    };

    private ImageView icon;
    private TextView name;
    private TextView pkg;
    private Switch switch_widget;

    public AppViewHolder(View itemView) {
        super(itemView);

        icon = (ImageView) itemView.findViewById(android.R.id.icon);
        name = (TextView) itemView.findViewById(android.R.id.title);
        pkg = (TextView) itemView.findViewById(android.R.id.summary);
        switch_widget = (Switch) itemView.findViewById(R.id.switch_widget);

        itemView.setFilterTouchesWhenObscured(true);
        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        final Context context = v.getContext();
        final PackageInfo pi = getData();

        Permissions.toggle(pi.packageName, pi.firstInstallTime);
        getAdapter().notifyItemChanged(getAdapterPosition(), new Object());
    }

    @Override
    public void onBind() {
        PackageManager pm = itemView.getContext().getPackageManager();
        ApplicationInfo ai = getData().applicationInfo;

        icon.setImageDrawable(ai.loadIcon(pm));
        name.setText(ai.loadLabel(pm));
        pkg.setText(ai.packageName);
        switch_widget.setChecked(Permissions.granted(ai.packageName));
    }

    @Override
    public void onBind(@NonNull List<Object> payloads) {
        ApplicationInfo ai = getData().applicationInfo;

        switch_widget.setChecked(Permissions.granted(ai.packageName));
    }
}
