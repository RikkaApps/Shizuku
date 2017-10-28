package moe.shizuku.manager.authorization;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Process;
import android.util.Log;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import moe.shizuku.ShizukuConstants;
import moe.shizuku.api.ShizukuClient;
import moe.shizuku.io.ParcelInputStream;
import moe.shizuku.io.ParcelOutputStream;
import moe.shizuku.lang.ShizukuRemoteException;
import moe.shizuku.manager.Manifest;

/**
 * Created by rikka on 2017/10/23.
 */

public class AuthorizationManagerImplV23 implements AuthorizationManagerImpl {

    public static int checkPermission(String permName, String pkgName, int userId) throws RuntimeException {
        try {
            Socket client = new Socket(ShizukuConstants.HOST, ShizukuConstants.PORT);
            client.setSoTimeout(ShizukuConstants.TIMEOUT);
            ParcelOutputStream os = new ParcelOutputStream(client.getOutputStream());
            ParcelInputStream is = new ParcelInputStream(client.getInputStream());
            os.writeString("PackageManager_checkPermission");
            os.writeLong(ShizukuClient.getToken().getMostSignificantBits());
            os.writeLong(ShizukuClient.getToken().getLeastSignificantBits());
            os.writeString(permName);
            os.writeString(pkgName);
            os.writeInt(userId);
            is.readException();
            int _result = is.readInt();
            return _result;
        } catch (IOException e) {
            throw new RuntimeException("Problem connect to shizuku server.", e);
        } catch (ShizukuRemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public static void grantRuntimePermission(String packageName, String permissionName, int userId) throws RuntimeException {
        try {
            Socket client = new Socket(ShizukuConstants.HOST, ShizukuConstants.PORT);
            client.setSoTimeout(ShizukuConstants.TIMEOUT);
            ParcelOutputStream os = new ParcelOutputStream(client.getOutputStream());
            ParcelInputStream is = new ParcelInputStream(client.getInputStream());
            os.writeString("PackageManager_grantRuntimePermission");
            os.writeLong(ShizukuClient.getToken().getMostSignificantBits());
            os.writeLong(ShizukuClient.getToken().getLeastSignificantBits());
            os.writeString(packageName);
            os.writeString(permissionName);
            os.writeInt(userId);
            is.readException();
        } catch (IOException e) {
            throw new RuntimeException("Problem connect to shizuku server.", e);
        } catch (ShizukuRemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public static void revokeRuntimePermission(String packageName, String permissionName, int userId) throws RuntimeException {
        try {
            Socket client = new Socket(ShizukuConstants.HOST, ShizukuConstants.PORT);
            client.setSoTimeout(ShizukuConstants.TIMEOUT);
            ParcelOutputStream os = new ParcelOutputStream(client.getOutputStream());
            ParcelInputStream is = new ParcelInputStream(client.getInputStream());
            os.writeString("PackageManager_revokeRuntimePermission");
            os.writeLong(ShizukuClient.getToken().getMostSignificantBits());
            os.writeLong(ShizukuClient.getToken().getLeastSignificantBits());
            os.writeString(packageName);
            os.writeString(permissionName);
            os.writeInt(userId);
            is.readException();
        } catch (IOException e) {
            throw new RuntimeException("Problem connect to shizuku server.", e);
        } catch (ShizukuRemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override
    public List<String> getPackages(Context context) {
        List<String> packages = new ArrayList<>();

        for (PackageInfo pi : context.getPackageManager().getInstalledPackages(PackageManager.GET_PERMISSIONS)) {
            if (pi.requestedPermissions != null) {
                for (String p : pi.requestedPermissions) {
                    if (Manifest.permission.API_V23.equals(p)) {
                        packages.add(pi.packageName);
                        break;
                    }
                }
            }
        }
        return packages;
    }

    @Override
    public boolean granted(Context context, final String packageName) {
        try {
            return Single
                    .fromCallable(new Callable<Integer>() {
                        @Override
                        public Integer call() throws Exception {
                            return checkPermission(Manifest.permission.API_V23, packageName, Process.myUid() / 100000);
                        }
                    })
                    .subscribeOn(Schedulers.io())
                    .toFuture()
                    .get() == PackageManager.PERMISSION_GRANTED;
        } catch (Exception ignored) {
        }
        return false;
    }

    @Override
    public void grant(Context context, final String packageName) {
        try {
            Single
                    .fromCallable(new Callable<Object>() {
                        @Override
                        public Object call() throws Exception {
                            grantRuntimePermission(packageName, Manifest.permission.API_V23, Process.myUid() / 100000);
                            return null;
                        }
                    })
                    .subscribeOn(Schedulers.io())
                    .toFuture()
                    .get();
        } catch (Exception ignored) {
        }
    }

    @Override
    public void revoke(Context context, final String packageName) {
        try {
            Single
                    .fromCallable(new Callable<Object>() {
                        @Override
                        public Object call() throws Exception {
                            revokeRuntimePermission(packageName, Manifest.permission.API_V23, Process.myUid() / 100000);
                            return null;
                        }
                    })
                    .subscribeOn(Schedulers.io())
                    .toFuture()
                    .get();
        } catch (Exception ignored) {
        }
    }
}
