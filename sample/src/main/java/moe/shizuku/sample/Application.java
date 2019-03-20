package moe.shizuku.sample;

import android.content.Context;

import me.weishu.reflection.Reflection;

public class Application extends android.app.Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        Reflection.unseal(base);
    }
}
