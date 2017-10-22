package moe.shizuku.manager.viewholder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import moe.shizuku.ShizukuConstants;
import moe.shizuku.ShizukuState;
import moe.shizuku.manager.Intents;
import moe.shizuku.manager.R;
import moe.shizuku.manager.ShizukuManagerSettings;
import moe.shizuku.manager.service.WorkService;
import moe.shizuku.support.recyclerview.BaseViewHolder;

/**
 * Created by rikka on 2017/10/22.
 */
public class ServerStatusViewHolder extends BaseViewHolder<ShizukuState> implements View.OnClickListener {

    public static final Creator<ShizukuState> CREATOR = new Creator<ShizukuState>() {
        @Override
        public BaseViewHolder<ShizukuState> createViewHolder(LayoutInflater inflater, ViewGroup parent) {
            return new ServerStatusViewHolder(inflater.inflate(R.layout.item_server_status, parent, false));
        }
    };

    private class ServerStartedReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            setData(intent.<ShizukuState>getParcelableExtra(Intents.EXTRA_RESULT), new Object());
        }
    }

    private BroadcastReceiver mServerStartedReceiver = new ServerStartedReceiver();

    private TextView mStatusText;
    private ImageView mStatusIcon;

    private boolean mCheckToRequest;

    public ServerStatusViewHolder(View itemView) {
        super(itemView);

        mStatusText = itemView.findViewById(R.id.status_text);
        mStatusIcon = itemView.findViewById(R.id.status_icon);

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

        LocalBroadcastManager.getInstance(itemView.getContext())
                .registerReceiver(mServerStartedReceiver, new IntentFilter(Intents.ACTION_AUTH_RESULT));

        ShizukuState shizukuState = getData();

        mCheckToRequest = false;

        boolean ok = false;
        boolean running = false;
        switch (shizukuState.getCode()) {
            case ShizukuState.RESULT_OK:
                running = true;
                ok = true;
                break;
            case ShizukuState.RESULT_SERVER_DEAD:
            case ShizukuState.RESULT_UNAUTHORIZED:
                running = true;
                break;
            case ShizukuState.RESULT_UNKNOWN:
                break;
        }

        boolean oldOK = mStatusText.getCurrentTextColor() == ContextCompat.getColor(context, R.color.status_ok);
        if (ok) {
            mStatusIcon.setBackgroundColor(ContextCompat.getColor(context, R.color.status_ok));
            mStatusIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_server_ok_48dp));
            mStatusText.setTextColor(ContextCompat.getColor(context, R.color.status_ok));

            if (!oldOK) {
                View view = (View) mStatusIcon.getParent();
                int centerX = mStatusIcon.getWidth() / 2;
                int centerY = mStatusIcon.getHeight() / 2;
                float radius = (float) Math.sqrt(centerX * centerX + (centerY + mStatusText.getHeight()) * (centerY + mStatusText.getHeight()));
                ViewAnimationUtils.createCircularReveal(view, centerX, centerY, 0, radius).start();
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
                    mStatusText.setText(context.getString(R.string.server_running_update, shizukuState.isRoot() ? "root" : "adb", shizukuState.getVersion(), ShizukuConstants.VERSION));
                } else {
                    mStatusText.setText(context.getString(R.string.server_running, shizukuState.isRoot() ? "root" : "adb", shizukuState.getVersion()));
                }
            } else {
                if (shizukuState.getCode() == ShizukuState.RESULT_UNAUTHORIZED) {
                    mStatusText.setText(R.string.server_no_token);
                    mCheckToRequest = true;
                } else {
                    mStatusText.setText(R.string.server_require_restart);
                }
            }

            ShizukuManagerSettings.setLastLaunchMode(shizukuState.isRoot() ? ShizukuManagerSettings.LaunchMethod.ROOT : ShizukuManagerSettings.LaunchMethod.ADB);
        }
    }

    @Override
    public void onBind(@NonNull List<Object> payloads) {
        onBind();
    }

    @Override
    public void onRecycle() {
        super.onRecycle();

        LocalBroadcastManager.getInstance(itemView.getContext())
                .unregisterReceiver(mServerStartedReceiver);
    }
}