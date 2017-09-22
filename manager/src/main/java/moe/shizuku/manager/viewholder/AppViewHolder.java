package moe.shizuku.manager.viewholder;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import java.util.List;

import moe.shizuku.manager.Permissions;
import moe.shizuku.manager.R;
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

        icon = itemView.findViewById(android.R.id.icon);
        name = itemView.findViewById(android.R.id.title);
        pkg = itemView.findViewById(android.R.id.summary);
        switch_widget = itemView.findViewById(R.id.switch_widget);

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
