package moe.shizuku.privileged.api.viewholder;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.view.View;
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

        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        final Context context = v.getContext();
        final PackageInfo pi = getData();

        Permissions.toggle(pi.packageName, pi.firstInstallTime);
        getAdapter().notifyItemChanged(getAdapterPosition(), new Object());

        /*boolean granted = Permissions.granted(ai.packageName);

        if (!granted) {
            Permissions.toggle(ai.packageName);

            getAdapter().notifyItemChanged(getAdapterPosition(), new Object());
        } else {
            new AlertDialog.Builder(context)
                    .setTitle("是否要撤销该应用的授权？")
                    .setMessage("如果确认，该应用将会被强行停止。")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            StrictMode.ThreadPolicy tp = new StrictMode.ThreadPolicy.Builder().build();
                            StrictMode.setThreadPolicy(tp);

                            ServerLauncher.forceStopPackage(context, ai.packageName);

                            Permissions.revoke(ai.packageName);
                            getAdapter().notifyItemChanged(getAdapterPosition(), new Object());
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
        }*/
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
