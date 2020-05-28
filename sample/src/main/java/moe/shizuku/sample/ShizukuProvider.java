package moe.shizuku.sample;

import android.util.Log;

import moe.shizuku.sample.util.ApplicationUtils;

public class ShizukuProvider extends moe.shizuku.api.ShizukuProvider {

    @Override
    public boolean onCreate() {
        boolean res = super.onCreate();
        Log.d("ShizukuSample", getClass().getSimpleName() + " onCreate | Process=" + ApplicationUtils.getProcessName());
        return res;
    }
}