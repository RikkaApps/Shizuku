package moe.shizuku.manager.viewholder;

import android.content.Context;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.UUID;

import androidx.core.content.ContextCompat;
import moe.shizuku.ShizukuConstants;
import moe.shizuku.ShizukuState;
import moe.shizuku.api.ShizukuClient;
import moe.shizuku.manager.R;
import moe.shizuku.manager.service.WorkService;
import moe.shizuku.support.recyclerview.BaseViewHolder;

public class ServerStatusViewHolder extends BaseViewHolder<ShizukuState> implements View.OnClickListener {

    public static final Creator<ShizukuState> CREATOR = (inflater, parent) -> new ServerStatusViewHolder(inflater.inflate(R.layout.item_server_status, parent, false));

    private TextView mStatusText;
    private ImageView mStatusIcon;

    private boolean mCheckToRequest;
    private UUID mToken;

    public ServerStatusViewHolder(View itemView) {
        super(itemView);

        mStatusText = itemView.findViewById(android.R.id.text1);
        mStatusIcon = itemView.findViewById(android.R.id.icon);

        mToken = ShizukuClient.getToken();

        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (mCheckToRequest) {
            WorkService.startRequestToken(v.getContext());
        } else {
            WorkService.startAuth(v.getContext());
        }
    }

    @Override
    public void onBind() {
        Context context = itemView.getContext();

        ShizukuState shizukuState = getData();

        mCheckToRequest = false;

        boolean ok = false;
        boolean running = false;
        switch (shizukuState.getCode()) {
            case ShizukuState.STATUS_AUTHORIZED:
                running = true;
                ok = true;
                break;
            case ShizukuState.STATUS_UNAVAILABLE:
            case ShizukuState.STATUS_UNAUTHORIZED:
                running = true;
                break;
            case ShizukuState.STATUS_UNKNOWN:
                break;
        }

        if (ok) {
            mStatusIcon.setBackgroundColor(ContextCompat.getColor(context, R.color.status_ok));
            mStatusIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_server_ok_48dp));
            mStatusText.setTextColor(ContextCompat.getColor(context, R.color.status_ok));

            if (!ShizukuClient.getToken().equals(mToken)) {
                mToken = ShizukuClient.getToken();

                final View view = (View) mStatusIcon.getParent();
                view.post(() -> {
                    int centerX = mStatusIcon.getWidth() / 2;
                    int centerY = mStatusIcon.getHeight() / 2;
                    float radius = (float) Math.sqrt(centerX * centerX + (centerY + mStatusText.getHeight()) * (centerY + mStatusText.getHeight()));

                    ViewAnimationUtils.createCircularReveal(view, centerX, centerY, 0, radius).start();
                });
            }
        } else {
            mStatusIcon.setBackgroundColor(ContextCompat.getColor(context, R.color.status_warning));
            mStatusIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_server_error_48dp));
            mStatusText.setTextColor(ContextCompat.getColor(context, R.color.status_warning));
        }

        if (!running) {
            mStatusText.setText(R.string.server_not_running);
        } else {
            if (ok) {
                if (shizukuState.versionUnmatched()) {
                    mStatusText.setText(context.getString(R.string.server_running_update, shizukuState.isRoot() ? "root" : "adb", shizukuState.getVersion(), ShizukuConstants.SERVER_VERSION));
                } else {
                    mStatusText.setText(context.getString(R.string.server_running, shizukuState.isRoot() ? "root" : "adb", shizukuState.getVersion()));
                }
            } else {
                if (shizukuState.getCode() == ShizukuState.STATUS_UNAUTHORIZED) {
                    mStatusText.setText(R.string.server_no_token);
                    mCheckToRequest = true;
                } else {
                    mStatusText.setText(R.string.server_require_restart);
                }
            }
        }
    }
}