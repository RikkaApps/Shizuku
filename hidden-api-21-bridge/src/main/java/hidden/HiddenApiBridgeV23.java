package hidden;

import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.content.IContentProvider;
import android.os.IBinder;
import android.os.RemoteException;

public class HiddenApiBridgeV23 {

    public static IContentProvider getContentProviderExternal_provider(IActivityManager am, String name, int userId, IBinder token) throws RemoteException {
        IActivityManager.ContentProviderHolder holder = am.getContentProviderExternal(name, userId, token);
        if (holder != null) {
            return holder.provider;
        }
        return null;
    }

    public static IActivityManager ActivityManagerNative_asInterface(IBinder binder) {
        return ActivityManagerNative.asInterface(binder);
    }
}
