package moe.shizuku.manager.viewholder;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import moe.shizuku.ShizukuConstants;
import moe.shizuku.ShizukuState;
import moe.shizuku.api.ShizukuClientHelper;
import moe.shizuku.manager.AppConstants;
import moe.shizuku.manager.R;
import moe.shizuku.manager.ShizukuManagerSettings;
import moe.shizuku.manager.model.ServiceStatus;
import moe.shizuku.manager.widget.MaterialCircleIconView;
import moe.shizuku.support.recyclerview.BaseViewHolder;
import moe.shizuku.support.text.HtmlCompat;
import moe.shizuku.support.widget.HtmlCompatTextView;

public class ServerStatusViewHolder extends BaseViewHolder<ServiceStatus> implements View.OnClickListener {

    public static final Creator<ServiceStatus> CREATOR = (inflater, parent) -> new ServerStatusViewHolder(inflater.inflate(R.layout.item_home_server_status, parent, false));

    private HtmlCompatTextView mStatusText;
    private HtmlCompatTextView mStatusSummary;
    private MaterialCircleIconView mStatusIcon;

    public ServerStatusViewHolder(View itemView) {
        super(itemView);

        mStatusText = itemView.findViewById(android.R.id.text1);
        mStatusSummary = itemView.findViewById(android.R.id.text2);
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

        boolean startV2 = ShizukuManagerSettings.isStartServiceV2();
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

        if (!startV2) {
            okV2 = true;
        }

        if (okV2 && okV3) {
            mStatusIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_server_ok_24dp));
            mStatusIcon.setColorName("blue");
        } else {
            mStatusIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_server_error_24dp));
            mStatusIcon.setColorName("blue_grey");
        }

        String v2Title, v3Title, v2Summary, v3Summary;
        String v2Name = context.getString(R.string.service_legacy_name);
        String v3Name = context.getString(R.string.service_name);
        String v2User = isRootV2 ? "root" : "adb";
        String v3User = isRootV3 ? "root" : "adb";
        if (startV2) {
            if (!runningV2) {
                v2Title = context.getString(R.string.service_not_running_tap_retry, v2Name);
                v2Summary = "";
            } else {
                if (okV2) {
                    v2Title = context.getString(R.string.service_running, v2Name);
                    if (v2Status.versionUnmatched()) {
                        v2Summary = context.getString(R.string.service_version_update, v2User, versionV2, ShizukuConstants.SERVER_VERSION);
                    } else {
                        v2Summary = context.getString(R.string.service_version, v2User, versionV2);
                    }
                } else {
                    if (v2Status.getCode() == ShizukuState.STATUS_UNAUTHORIZED) {
                        v2Title = context.getString(R.string.service_legacy_no_token, v2Name);
                    } else {
                        v2Title = context.getString(R.string.service_legacy_require_restart, v2Name);
                    }
                    v2Summary = "";
                }
            }
        } else {
            v2Title = "";
            v2Summary = "";
        }

        if (okV3) {
            v3Title = context.getString(R.string.service_running, v3Name);
            if (status.getVersion() != ShizukuClientHelper.getLatestVersion()) {
                v3Summary = context.getString(R.string.service_version_update, v3User, versionV3, ShizukuClientHelper.getLatestVersion());
            } else {
                v3Summary = context.getString(R.string.service_version, v3User, versionV3);
            }
        } else {
            v3Title = context.getString(R.string.service_not_running_tap_retry, v3Name);
            v3Summary = "";
        }

        if (!startV2) {
            mStatusText.setHtmlText(String.format("<font face=\"sans-serif-medium\">%1$s</font>", v3Title), HtmlCompat.FROM_HTML_OPTION_TRIM_WHITESPACE);
            mStatusSummary.setHtmlText(String.format("%1$s", v3Summary), HtmlCompat.FROM_HTML_OPTION_TRIM_WHITESPACE);
        } else {
            mStatusText.setHtmlText(String.format("<font face=\"sans-serif-medium\">%1$s (%2$s)</font>", v3Title, v3Summary), HtmlCompat.FROM_HTML_OPTION_TRIM_WHITESPACE);
            mStatusSummary.setHtmlText(String.format("%1$s (%2$s)", v2Title, v2Summary), HtmlCompat.FROM_HTML_OPTION_TRIM_WHITESPACE);
        }
    }
}