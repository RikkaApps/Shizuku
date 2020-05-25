package moe.shizuku.sample;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import me.weishu.reflection.Reflection;

public class SampleApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d("ShizukuSample", getClass().getSimpleName() + " onCreate | Process=" + ApplicationUtils.getProcessName());
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        Reflection.unseal(this); // bypass hidden api restriction, https://github.com/tiann/FreeReflection

        Log.d("ShizukuSample", getClass().getSimpleName() + " attachBaseContext | Process=" + ApplicationUtils.getProcessName());

        boolean isProviderProcess = ApplicationUtils.getProcessName().endsWith(":test");
        ShizukuProvider.enableMultiProcessSupport(this, isProviderProcess);
    }
}
