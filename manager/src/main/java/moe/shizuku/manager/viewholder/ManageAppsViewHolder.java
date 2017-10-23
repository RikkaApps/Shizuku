package moe.shizuku.manager.viewholder;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import moe.shizuku.manager.ManageAppsActivity;
import moe.shizuku.manager.R;
import moe.shizuku.manager.widget.HtmlTextView;
import moe.shizuku.support.recyclerview.BaseViewHolder;

/**
 * Created by rikka on 2017/10/23.
 */
public class ManageAppsViewHolder extends BaseViewHolder<Integer> {

    public static final Creator<Integer> CREATOR = new Creator<Integer>() {
        @Override
        public BaseViewHolder<Integer> createViewHolder(LayoutInflater inflater, ViewGroup parent) {
            return new ManageAppsViewHolder(inflater.inflate(R.layout.item_manage_apps, parent, false));
        }
    };

    private HtmlTextView text;

    public ManageAppsViewHolder(View itemView) {
        super(itemView);

        text = itemView.findViewById(android.R.id.text1);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.getContext().startActivity(new Intent(v.getContext(), ManageAppsActivity.class));
            }
        });
    }

    @Override
    public void onBind() {
        Context context = itemView.getContext();

        text.setHtmlText(context.getResources().getQuantityString(R.plurals.authorized_apps_count, getData(), getData()));
    }

    /*@Override
    public void onBind(@NonNull List<Object> payloads) {
        super.onBind(payloads);
    }*/
}