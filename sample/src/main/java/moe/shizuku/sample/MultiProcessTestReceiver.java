package moe.shizuku.sample;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import moe.shizuku.api.ShizukuService;

public class MultiProcessTestReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("ShizukuSample", "MultiProcessTestReceiver onReceive binder: " + ShizukuService.getBinder());
    }
}
