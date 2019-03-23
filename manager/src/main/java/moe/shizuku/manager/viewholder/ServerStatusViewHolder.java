package moe.shizuku.manager.viewholder;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.view.View;
import android.widget.ImageView;

import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import moe.shizuku.ShizukuConstants;
import moe.shizuku.ShizukuState;
import moe.shizuku.api.ShizukuClientHelper;
import moe.shizuku.manager.AppConstants;
import moe.shizuku.manager.R;
import moe.shizuku.manager.model.ServiceStatus;
import moe.shizuku.support.recyclerview.BaseViewHolder;
import moe.shizuku.support.text.HtmlCompat;
import moe.shizuku.support.widget.HtmlCompatTextView;

public class ServerStatusViewHolder extends BaseViewHolder<ServiceStatus> implements View.OnClickListener {

    public static final Creator<ServiceStatus> CREATOR = (inflater, parent) -> new ServerStatusViewHolder(inflater.inflate(R.layout.item_server_status, parent, false));

    private HtmlCompatTextView mStatusText;
    private ImageView mStatusIcon;

    public ServerStatusViewHolder(View itemView) {
        super(itemView);

        mStatusText = itemView.findViewById(android.R.id.text1);
        mStatusIcon = itemView.findViewById(android.R.id.icon);

        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        LocalBroadcastManager.getInstance(v.getContext())
                .sendBroadcast(new Intent(AppConstants.ACTION_REQUEST_REFRESH));
        /*if (mCheckToRequest) {
            WorkService.startRequestTokenV2(v.getContext());
        } else {
            //WorkService.startAuthV2(v.getContext());
        }

        WorkService.startRequestBinder(v.getContext());*/
    }

    @Override
    public void onBind() {
        final Context context = itemView.getContext();
        final ServiceStatus status = getData();
        final ShizukuState v2Status = getData().getV2Status();

        boolean runningV2 = false;
        boolean okV3 = status.isV3Running();
        boolean okV2 = false;
        boolean isRootV2 = v2Status.isRoot(), isRootV3 = status.getUid() == 0;
        int versionV2 = v2Status.getVersion(), versionV3 = status.getVersion();

        switch (v2Status.getCode()) {
            case ShizukuState.STATUS_AUTHORIZED:
                runningV2 = true;
                okV2 = true;
                break;
            case ShizukuState.STATUS_UNAVAILABLE:
            case ShizukuState.STATUS_UNAUTHORIZED:
                runningV2 = true;
                break;
            case ShizukuState.STATUS_UNKNOWN:
                break;
        }

        if (okV2 && okV3) {
            mStatusIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_server_ok_24dp));
            mStatusIcon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.status_ok)));
        } else {
            mStatusIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_server_error_24dp));
            mStatusIcon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.status_warning)));
        }

        String v2Text, v3Text;
        String v2Name = context.getString(R.string.service_name_v2);
        String v3Name = context.getString(R.string.service_name_v3);
        String v2User = isRootV2 ? "root" : "adb";
        String v3User = isRootV3 ? "root" : "adb";

        if (!runningV2) {
            v2Text = context.getString(R.string.server_not_running_tap_retry, v2Name);
        } else {
            if (okV2) {
                if (v2Status.versionUnmatched()) {
                    v2Text = context.getString(R.string.server_running_update, v2Name, v2User, versionV2, ShizukuConstants.SERVER_VERSION);
                } else {
                    v2Text = context.getString(R.string.server_running, v2Name, v2User, versionV2);
                }
            } else {
                if (v2Status.getCode() == ShizukuState.STATUS_UNAUTHORIZED) {
                    v2Text = context.getString(R.string.server_no_token, v2Name);
                } else {
                    v2Text = context.getString(R.string.server_require_restart, v2Name);
                }
            }
        }

        if (okV3) {
            if (status.getVersion() != ShizukuClientHelper.getLatestVersion()) {
                v3Text = context.getString(R.string.server_running_update, v3Name, v3User, versionV3, ShizukuClientHelper.getLatestVersion());
            } else {
                v3Text = context.getString(R.string.server_running,  v3Name, v3User, versionV3);
            }
        } else {
            v3Text = context.getString(R.string.server_not_running_tap_retry, v3Name);
        }

        mStatusText.setHtmlText(context.getString(R.string.server_status_format, v3Text, v2Text), HtmlCompat.FROM_HTML_OPTION_TRIM_WHITESPACE);
    }
}