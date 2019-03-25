package moe.shizuku.server.reflection;

import android.content.IContentProvider;
import android.os.Bundle;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class IContentProviderHelper {

    private static Method method_call;
    private static int method_call_paramCount;

    static {
        try {
            for (Method method : IContentProvider.class.getDeclaredMethods()) {
                if ("call".equals(method.getName())) {
                    method_call = method;
                    method_call_paramCount = method.getParameterTypes().length;
                }
            }
        } catch (Throwable tr) {
            tr.printStackTrace();
        }
    }

    public static Bundle call(IContentProvider provider, String callingPkg, String authority, String method, String arg, Bundle extras) throws InvocationTargetException, IllegalAccessException {
        if (method_call_paramCount == 5) {
            return (Bundle) method_call.invoke(provider, callingPkg, authority, method, arg, extras);
        } else if (method_call_paramCount == 4) {
            return (Bundle) method_call.invoke(provider, callingPkg, method, arg, extras);
        } else {
            throw new NoSuchMethodError("android.content.IContentProvider#call");
        }
    }
}
