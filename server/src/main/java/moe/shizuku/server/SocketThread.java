package moe.shizuku.server;

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.wifi.IWifiManager;
import android.net.wifi.WifiConfiguration;
import android.os.Build;
import android.os.Handler;
import android.os.IUserManager;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;

import com.android.internal.app.IAppOpsService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import hidden.android.app.AppOpsManager;
import hidden.android.content.pm.UserInfo;
import hidden.android.net.wifi.WrappedWifiConfiguration;
import moe.shizuku.server.content.IntentReceiver;
import moe.shizuku.server.io.ParcelInputStream;
import moe.shizuku.server.io.ParcelOutputStream;
import moe.shizuku.server.util.Intents;
import moe.shizuku.server.util.ServerLog;

/**
 * Created by Rikka on 2017/5/4.
 */

class SocketThread implements Runnable, RequestHandler.Impl {

    private final Handler mHandler;
    private final ServerSocket mServerSocket;
    private final CountDownLatch mCountDownLatch;

    private final UUID mToken;

    private final RequestHandler mRequestHandler;

    SocketThread(Handler handler, ServerSocket serverSocket, CountDownLatch socketLatch, UUID token) {
        mHandler = handler;
        mServerSocket = serverSocket;
        mCountDownLatch = socketLatch;
        mToken = token;
        mRequestHandler = new RequestHandler(this);
    }

    @Override
    public void run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        for (; ; ) {
            try {
                Socket socket = mServerSocket.accept();
                boolean quit = !mRequestHandler.handle(socket, mToken);
                socket.close();

                if (quit) {
                    break;
                }
            } catch (IOException e) {
                if (SocketException.class.equals(e.getClass()) && "Socket closed".equals(e.getMessage())) {
                    ServerLog.i("server socket is closed");
                    break;
                }
                ServerLog.w("cannot accept", e);
            } catch (RemoteException e) {
                ServerLog.w("remote error", e);
            } catch (Exception e) {
                ServerLog.w("error", e);
            }
        }
        try {
            mServerSocket.close();
        } catch (IOException ignored) {
        }
        mCountDownLatch.countDown();
    }

    @Override
    public boolean requireAuthorization(int action) {
        return action != Actions.authorize
                && action != Actions.version
                && action >= 0;
    }

    @Override
    public boolean handleUnknownAction(int action, ParcelInputStream is, ParcelOutputStream os) throws IOException, RemoteException {
        switch (action) {
            case -1:
                sendTokenToManger(is, os);
                break;
            case -2:
                quit(os);
                return false;
        }
        return true;
    }

    private void sendTokenToManger(ParcelInputStream is, ParcelOutputStream os) throws IOException, RemoteException {
        int uid = is.readInt();
        int userId = uid / 100000;

        Intent intent = new Intent(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_LAUNCHER)
                .setComponent(Intents.componentName(".MainActivity"))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                .putExtra(Intents.EXTRA_TOKEN_MOST_SIG, mToken.getMostSignificantBits())
                .putExtra(Intents.EXTRA_TOKEN_LEAST_SIG, mToken.getLeastSignificantBits());

        startActivity(intent, userId);

        os.writeNoException();
    }

    private void quit(ParcelOutputStream os) throws IOException {
        os.writeNoException();

        mHandler.sendEmptyMessage(Server.MESSAGE_EXIT);
    }

    @Override
    public Protocol version() throws RemoteException {
        try {
            ActivityManagerNative.getDefault();
            return new Protocol(Protocol.RESULT_OK);
        } catch (Exception e) {
            return new Protocol(Protocol.RESULT_SERVER_DEAD);
        }
    }

    @Override
    public Protocol authorize(long most, long least) throws RemoteException {
        try {
            ActivityManagerNative.getDefault();
        } catch (Exception e) {
            return new Protocol(Protocol.RESULT_SERVER_DEAD);
        }

        if (most == mToken.getMostSignificantBits()
                && least == mToken.getLeastSignificantBits()) {
            return new Protocol(Protocol.RESULT_OK);
        }

        return new Protocol(Protocol.RESULT_UNAUTHORIZED);
    }

    @Override
    public List<ActivityManager.RunningTaskInfo> getTasks(int maxNum, int flags) throws RemoteException {
        return ActivityManagerNative.getDefault().getTasks(maxNum, flags);
    }

    @Override
    public int broadcastIntent(Intent intent, String requiredPermissions, int userId) throws RemoteException {
        CountDownLatch latch = new CountDownLatch(0x1);
        IntentReceiver receiver = new IntentReceiver(latch);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return ActivityManagerNative.getDefault().broadcastIntent(null, intent, null, receiver, 0, null, null, new String[]{requiredPermissions},
                    HideApiOverride.OP_NONE, null, true, false, userId);
        } else {
            return ActivityManagerNative.getDefault().broadcastIntent(null, intent, null, receiver, 0, null, null, requiredPermissions,
                    HideApiOverride.OP_NONE, true, false, userId);
        }
    }

    @Override
    public void forceStopPackage(String packageName, int userId) throws RemoteException {
        ActivityManagerNative.getDefault().forceStopPackage(packageName, userId);
    }

    @Override
    public int startActivity(Intent intent, int userId) throws RemoteException {
        IActivityManager am = ActivityManagerNative.getDefault();
        String mimeType = intent.getType();
        if (mimeType == null && intent.getData() != null
                && "content".equals(intent.getData().getScheme())) {
            mimeType = am.getProviderMimeType(intent.getData(), userId);
        }

        return am.startActivityAsUser(null, null, intent, mimeType,
                null, null, 0, 0, null, null, userId);
    }

    @Override
    public List<UserInfo> getUsers(boolean excludeDying) throws RemoteException {
        IUserManager userManager = IUserManager.Stub.asInterface(ServiceManager.getService(Context.USER_SERVICE));
        return HideApiOverride.convertUserInfo(userManager.getUsers(excludeDying));
    }

    @Override
    public Bitmap getUserIcon(int userHandle) throws RemoteException {
        IUserManager userManager = IUserManager.Stub.asInterface(ServiceManager.getService(Context.USER_SERVICE));

        Bitmap bitmap = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ParcelFileDescriptor fd = userManager.getUserIcon(userHandle);
            if (fd != null) {
                try {
                    bitmap = BitmapFactory.decodeFileDescriptor(fd.getFileDescriptor());
                } finally {
                    try {
                        fd.close();
                    } catch (IOException ignored) {
                    }
                }

            }
        } else {
            bitmap = HideApiOverrideM.getUserIcon(userManager, userHandle);
        }
        return bitmap;
    }

    @Override
    public int getPackageUid(String packageName, int flags, int userId) throws RemoteException {
        IPackageManager packageManager = IPackageManager.Stub.asInterface(ServiceManager.getService("package"));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return packageManager.getPackageUid(packageName, flags, userId);
        } else {
            return packageManager.getPackageUid(packageName, userId);
        }
    }

    @Override
    public PackageInfo getPackageInfo(String packageName, int flags, int userId) throws RemoteException {
        IPackageManager packageManager = IPackageManager.Stub.asInterface(ServiceManager.getService("package"));
        return packageManager.getPackageInfo(packageName, flags, userId);
    }

    @Override
    public ApplicationInfo getApplicationInfo(String packageName, int flags, int userId) throws RemoteException {
        IPackageManager packageManager = IPackageManager.Stub.asInterface(ServiceManager.getService("package"));
        return packageManager.getApplicationInfo(packageName, flags, userId);
    }

    @Override
    public List<PackageInfo> getInstalledPackages(int flags, int userId) throws RemoteException {
        IPackageManager packageManager = IPackageManager.Stub.asInterface(ServiceManager.getService("package"));
        List<PackageInfo> list = packageManager.getInstalledPackages(flags, userId).getList();
        if ((flags & 0x40000000) != 0) {
            Intent intentToResolve = new Intent(Intent.ACTION_MAIN);
            intentToResolve.addCategory(Intent.CATEGORY_INFO);

            for (PackageInfo pi : list) {
                intentToResolve.setPackage(pi.packageName);

                List<ResolveInfo> ris = queryIntentActivities(intentToResolve, null, 0, userId);
                if (ris == null || ris.size() <= 0) {
                    // reuse the intent instance
                    intentToResolve.removeCategory(Intent.CATEGORY_INFO);
                    intentToResolve.addCategory(Intent.CATEGORY_LAUNCHER);
                    ris = queryIntentActivities(intentToResolve, null, 0, userId);
                }
                if (ris != null && !ris.isEmpty()) {
                    pi.applicationInfo.flags |= (1<<25);
                } else {
                    pi.applicationInfo.flags &= ~(1<<25);
                }
            }
        }
        return list;
    }

    @Override
    public List<ApplicationInfo> getInstalledApplications(int flags, int userId) throws RemoteException {
        IPackageManager packageManager = IPackageManager.Stub.asInterface(ServiceManager.getService("package"));
        return packageManager.getInstalledApplications(flags, userId).getList();
    }

    @Override
    public List<ResolveInfo> queryIntentActivities(Intent intent, String resolvedType, int flags, int userId) throws RemoteException {
        IPackageManager packageManager = IPackageManager.Stub.asInterface(ServiceManager.getService("package"));
        List<ResolveInfo> list;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            list = packageManager.queryIntentActivities(intent, resolvedType, flags, userId).getList();
        } else {
            list = HideApiOverrideM.queryIntentActivities(packageManager, intent, resolvedType, flags, userId);
        }
        return list;
    }

    @Override
    public int checkPermission(String permName, String pkgName, int userId) throws RemoteException {
        IPackageManager packageManager = IPackageManager.Stub.asInterface(ServiceManager.getService("package"));
        return packageManager.checkPermission(permName, pkgName, userId);
    }

    @Override
    public int checkUidPermission(String permName, int uid) throws RemoteException {
        IPackageManager packageManager = IPackageManager.Stub.asInterface(ServiceManager.getService("package"));
        return packageManager.checkUidPermission(permName, uid);
    }

    @Override
    public int checkServerPermission(String permName) throws RemoteException {
        IPackageManager packageManager = IPackageManager.Stub.asInterface(ServiceManager.getService("package"));
        return packageManager.checkUidPermission(permName, Process.myUid());
    }

    @Override
    public List<AppOpsManager.PackageOps> getOpsForPackage(int uid, String packageName, int[] ops) throws RemoteException {
        IAppOpsService appOpsService = IAppOpsService.Stub.asInterface(ServiceManager.getService(Context.APP_OPS_SERVICE));
        return HideApiOverride.convertPackageOps(
                appOpsService.getOpsForPackage(uid, packageName, ops), Build.VERSION.SDK_INT >= Build.VERSION_CODES.M);
    }

    @Override
    public void setMode(int code, int uid, String packageName, int mode) throws RemoteException {
        IAppOpsService appOpsService = IAppOpsService.Stub.asInterface(ServiceManager.getService(Context.APP_OPS_SERVICE));
        appOpsService.setMode(code, uid, packageName, mode);
    }

    @Override
    public void resetAllModes(int reqUserId, String reqPackageName) throws RemoteException {
        IAppOpsService appOpsService = IAppOpsService.Stub.asInterface(ServiceManager.getService(Context.APP_OPS_SERVICE));
        appOpsService.resetAllModes(reqUserId, reqPackageName);
    }

    @Override
    public AppOpsManager.PackageOps getOpsForPackage2(int userId, String packageName, int[] ops) throws RemoteException {
        int uid = getPackageUid(packageName, 0, userId);
        List<AppOpsManager.PackageOps> list = getOpsForPackage(uid, packageName, ops);
        if (list != null && !list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

    @Override
    public AppOpsManager.PackageOps setMode2(int[] code, int userId, String packageName, int[] mode) throws RemoteException {
        int uid = getPackageUid(packageName, 0, userId);
        for (int i = 0; i < code.length; i++) {
            setMode(code[i], uid, packageName, mode[i]);
        }
        return getOpsForPackage2(userId, packageName, null);
    }

    @Override
    public AppOpsManager.PackageOps resetAllModes2(int reqUserId, String reqPackageName) throws RemoteException {
        resetAllModes(reqUserId, reqPackageName);
        return getOpsForPackage2(reqUserId, reqPackageName, null);
    }

    @Override
    public List<WrappedWifiConfiguration> getPrivilegedConfiguredNetworks() throws RemoteException {
        IWifiManager wifiManager = IWifiManager.Stub.asInterface(ServiceManager.getService(Context.WIFI_SERVICE));
        List<WrappedWifiConfiguration> wrapped = new ArrayList<>();
        for (WifiConfiguration c : wifiManager.getPrivilegedConfiguredNetworks()) {
            wrapped.add(new WrappedWifiConfiguration(c));
        }
        return wrapped;
    }
}
