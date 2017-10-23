package moe.shizuku.manager.utils;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

/**
 * Created by rikka on 2017/10/22.
 */

public class BindServiceHelper implements ServiceConnection {

    public interface OnServiceConnectedListener {
        void onServiceConnected(IBinder service);
    }

    private final Context mContext;
    private final Class<? extends Service> mClass;
    private OnServiceConnectedListener mListener;
    private IBinder mService;

    public BindServiceHelper(Context context, Class<? extends Service> service) {
        mContext = context;
        mClass = service;
    }

    public void bind(OnServiceConnectedListener listener) {
        if (mService != null) {
            if (listener != null) {
                listener.onServiceConnected(mService);
            }
        } else {
            mListener = listener;
            mContext.bindService(new Intent(mContext, mClass), this, Context.BIND_AUTO_CREATE);
        }
    }

    public void unbind() {
        if (mService != null) {
            mService = null;
            mContext.unbindService(this);
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        mService = service;

        if (mListener != null) {
            mListener.onServiceConnected(service);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mService = null;
    }
}
