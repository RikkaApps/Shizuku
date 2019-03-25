package moe.shizuku.server.reflection;

import android.annotation.SuppressLint;
import android.content.IContentProvider;
import android.os.Build;

import java.lang.reflect.Field;

@SuppressLint("PrivateApi")
public class ContentProviderHolderHelper {

    private static Field field_provider;

    static {
        try {
            Class<?> cls;

            if (Build.VERSION.SDK_INT >= 26)
                cls = Class.forName("android.app.ContentProviderHolder");
            else
                cls = Class.forName("android.app.IActivityManager$ContentProviderHolder");

            //noinspection JavaReflectionMemberAccess
            field_provider = cls.getDeclaredField("provider");
            field_provider.setAccessible(true);
        } catch (Throwable tr) {
            tr.printStackTrace();
        }
    }

    public static IContentProvider getProvider(Object contentProviderHolder) {
        if (field_provider != null) {
            try {
                return (IContentProvider) field_provider.get(contentProviderHolder);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
