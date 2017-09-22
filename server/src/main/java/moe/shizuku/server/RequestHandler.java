package moe.shizuku.server;

import android.content.Intent;
import android.os.RemoteException;

import java.io.IOException;
import java.net.Socket;
import java.util.UUID;

import moe.shizuku.ShizukuIntent;
import moe.shizuku.ShizukuState;
import moe.shizuku.api.ShizukuClient;
import moe.shizuku.io.ParcelInputStream;
import moe.shizuku.io.ParcelOutputStream;

/**
 * Created by rikka on 2017/9/23.
 */

public class RequestHandler {

    private static boolean isActionRequireAuthorization(int action) {
        return action != ShizukuClient.ACTION_GET_VERSION
                && action != ShizukuClient.ACTION_AUTHORIZE
                && action != ShizukuClient.ACTION_REQUEST_STOP
                && action != ShizukuClient.ACTION_SEND_TOKEN;
    }

    public static boolean handle(Socket socket, UUID token) throws IOException, RemoteException {
        ParcelInputStream is = new ParcelInputStream(socket.getInputStream());
        ParcelOutputStream os = new ParcelOutputStream(socket.getOutputStream());
        int action = is.readInt();
        if (isActionRequireAuthorization(action)) {
            long most = is.readLong();
            long least = is.readLong();
            if (most != token.getMostSignificantBits()
                    && least != token.getLeastSignificantBits()) {
                os.writeException(new SecurityException("unauthorized"));
                is.close();
                os.flush();
                os.close();
                return true;
            }
        }
        switch (action) {
            case ShizukuClient.ACTION_GET_VERSION:
                version(os);
                break;
            case ShizukuClient.ACTION_REQUEST_STOP:
                stop(os);
                break;
            case ShizukuClient.ACTION_AUTHORIZE:
                authorize(is, os);
                break;
            case ShizukuClient.ACTION_SEND_TOKEN:
                sendTokenToManger(is, os);
                break;
            /*case :
                default:*/
        }
        is.close();
        os.flush();
        os.close();
        return true;
    }

    private static boolean isServerDead() {
        try {
            //ActivityManagerNative.getDefault();
            return false;
        } catch (Exception e) {
            return true;
        }
    }
    public static void version(ParcelOutputStream os) throws RemoteException, IOException {
        os.writeNoException();
        if (isServerDead()) {
            os.writeParcelable(ShizukuState.createServerDead());
        } else {
            os.writeParcelable(ShizukuState.createOk());
        }
    }

    public static void authorize(ParcelInputStream is, ParcelOutputStream os) throws RemoteException, IOException {
        long most = is.readLong();
        long least = is.readLong();

        os.writeNoException();
        if (isServerDead()) {
            os.writeParcelable(ShizukuState.createServerDead());
        } else if (most != Server.getToken().getMostSignificantBits()
                && least != Server.getToken().getLeastSignificantBits()) {
            os.writeParcelable(ShizukuState.createUnauthorized());
        } else {
            os.writeParcelable(ShizukuState.createOk());
        }

        is.close();
        os.flush();
        os.close();
    }

    private static void stop(ParcelOutputStream os) throws IOException {
        os.writeNoException();

        //mHandler.sendEmptyMessage(Server.MESSAGE_EXIT);
    }


    private static void sendTokenToManger(ParcelInputStream is, ParcelOutputStream os) throws IOException, RemoteException {
        int uid = is.readInt();
        int userId = uid / 100000;

        Intent intent = new Intent(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_LAUNCHER)
                .setComponent(ShizukuIntent.componentName(".MainActivity"))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                .putExtra(ShizukuIntent.EXTRA_TOKEN_MOST_SIG, Server.getToken().getMostSignificantBits())
                .putExtra(ShizukuIntent.EXTRA_TOKEN_LEAST_SIG, Server.getToken().getLeastSignificantBits());

        //startActivity(intent, userId);

        os.writeNoException();
    }


    /*public static void getPackageInfo(ParcelInputStream is, ParcelOutputStream os) throws IOException, RemoteException {
        String packageName = is.readString();
        int flags = is.readInt();
        int userId = is.readInt();

        try {
            PackageInfo result = PackageManagerDelegate.getPackageInfo(packageName, flags, userId);
            os.writeNoException();
            os.writeParcelable(result);
        } catch (Throwable tr) {
            if (!(tr instanceof IOException)) {
                os.writeException(tr);
                ServerLog.eStack("error when call getPackageInfo(" + packageName + ", " + flags + ")\n" + tr.getMessage(), tr);
            }
        }
    }*/
}
