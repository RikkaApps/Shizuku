package moe.shizuku.manager.viewholder;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.NonNull;
import moe.shizuku.manager.R;
import moe.shizuku.manager.authorization.AuthorizationManager;
import moe.shizuku.support.recyclerview.BaseViewHolder;

public class AppViewHolder extends BaseViewHolder<PackageInfo> implements View.OnClickListener {

    public static final Creator<PackageInfo> CREATOR = (inflater, parent) -> new AppViewHolder(inflater.inflate(R.layout.item_applist, parent, false));

    private ImageView icon;
    private TextView name;
    private TextView pkg;
    private Switch switch_widget;
    private View v3;

    public AppViewHolder(View itemView) {
        super(itemView);

        icon = itemView.findViewById(android.R.id.icon);
        name = itemView.findViewById(android.R.id.title);
        pkg = itemView.findViewById(android.R.id.summary);
        switch_widget = itemView.findViewById(R.id.switch_widget);
        v3 = itemView.findViewById(android.R.id.text1);

        itemView.setFilterTouchesWhenObscured(true);
        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        final Context context = v.getContext();
        final PackageInfo pi = getData();

        if (AuthorizationManager.granted(pi.packageName)) {
            AuthorizationManager.revoke(pi.packageName);
        } else {
            AuthorizationManager.grant(pi.packageName);
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
        switch_widget.setChecked(AuthorizationManager.granted(ai.packageName));

        v3.setVisibility((ai.metaData == null || !ai.metaData.getBoolean("moe.shizuku.client.V3_SUPPORT")) ? View.VISIBLE : View.GONE);
        v3.setVisibility(View.GONE);
    }

    @Override
    public void onBind(@NonNull List<Object> payloads) {
        Context context = itemView.getContext();
        ApplicationInfo ai = getData().applicationInfo;

        switch_widget.setChecked(AuthorizationManager.granted(ai.packageName));
    }
}
