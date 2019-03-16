package moe.shizuku.server.utils;

import android.os.IInterface;

public abstract class BinderSingleton<T extends IInterface> {

    private T mInstance;

    protected abstract T create();

    public final T get() {
        synchronized (this) {
            if (mInstance == null || !mInstance.asBinder().pingBinder()) {
                mInstance = create();
            }

            return mInstance;
        }
    }
}
