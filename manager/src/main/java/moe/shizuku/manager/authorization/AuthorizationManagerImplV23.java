package moe.shizuku.manager.authorization;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Process;
import android.support.v4.content.ContextCompat;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

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

    public static int checkPermission(String permName, String pkgName, int userId) throws ShizukuRemoteException {
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
            throw new ShizukuRemoteException("Problem connect to shizuku server.", e);
        }
    }

    public static void grantRuntimePermission(String packageName, String permissionName, int userId) throws ShizukuRemoteException {
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
            throw new ShizukuRemoteException("Problem connect to shizuku server.", e);
        }
    }

    public static void revokeRuntimePermission(String packageName, String permissionName, int userId) throws ShizukuRemoteException {
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
            throw new ShizukuRemoteException("Problem connect to shizuku server.", e);
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
    public boolean granted(Context context, String packageName) {
        return checkPermission(Manifest.permission.API_V23, packageName, Process.myUid() / 100000)
                == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void grant(Context context, String packageName) {
        grantRuntimePermission(packageName, Manifest.permission.API_V23, Process.myUid() / 100000);
    }

    @Override
    public void revoke(Context context, String packageName) {
        revokeRuntimePermission(packageName, Manifest.permission.API_V23, Process.myUid() / 100000);
    }
}
