package moe.shizuku.server.api;

import android.app.IActivityManager.ContentProviderHolder;
import android.content.IContentProvider;
import android.content.IIntentReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.UserInfo;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;

import java.util.List;

import moe.shizuku.server.delegate.ActivityManagerDelegate;
import moe.shizuku.server.delegate.UserManagerDelegate;

/**
 * Created by rikka on 2017/10/19.
 */

public class Compat {

    public static final int VERSION = 23;

    public static List<UserInfo> getUsers() throws RemoteException {
        return UserManagerDelegate.getUsers(true);
    }

    public static String getProviderMimeType(Uri uri, int userId) throws RemoteException {
        return ActivityManagerDelegate.getProviderMimeType(uri, userId);
    }

    public static void startActivityAsUser(Intent intent, String mimeType, int userId) throws RemoteException {
        ActivityManagerDelegate.startActivityAsUser(null, null, intent, mimeType,
                null, null, 0, 0, null, null, userId);
    }

    public static Intent registerReceiver(IIntentReceiver.Stub receiver, IntentFilter intentFilter, int userId) throws RemoteException {
        return ActivityManagerDelegate.registerReceiver(null, null, receiver, intentFilter, null, userId);
    }

    public static IContentProvider getContentProvider(String name, int userId, IBinder token) throws RemoteException {
        ContentProviderHolder holder = ActivityManagerDelegate.getContentProviderExternal(name, userId, token);
        if (holder == null) {
            return null;
        }
        return holder.provider;
    }
}
