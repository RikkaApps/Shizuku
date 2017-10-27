package moe.shizuku.server.api;

import android.content.IContentProvider;
import android.content.IIntentReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.UserInfo;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;

import java.util.List;

/**
 * Created by rikka on 2017/10/19.
 */

public class Compat {

    public static int VERSION;

    public static List<UserInfo> getUsers() throws RemoteException {
        throw new RuntimeException("STUB");
    }

    public static String getProviderMimeType(Uri uri, int userId) throws RemoteException {
        throw new RuntimeException("STUB");
    }

    public static void startActivityAsUser(Intent intent, String mimeType, int userId) throws RemoteException {
        throw new RuntimeException("STUB");
    }

    public static Intent registerReceiver(IIntentReceiver.Stub receiver, IntentFilter intentFilter, int userId) throws RemoteException {
        throw new RuntimeException("STUB");
    }

    public static IContentProvider getContentProvider(String name, int userId, IBinder token) throws RemoteException {
        throw new RuntimeException("STUB");
    }
}
