package moe.shizuku.api;

import android.content.pm.PackageInfo;

import java.io.IOException;
import java.net.Socket;

import moe.shizuku.ShizukuConfiguration;
import moe.shizuku.io.ParcelInputStream;
import moe.shizuku.io.ParcelOutputStream;

/**
 * Created by rikka on 2017/9/23.
 */

public class ShizukuPackageManagerV26 {

    public static PackageInfo getPackageInfo(String packageName, int flags, int userId) {
        try {
            Socket client = new Socket(ShizukuConfiguration.HOST, ShizukuConfiguration.PORT);
            client.setSoTimeout(ShizukuConfiguration.TIMEOUT);
            ParcelOutputStream os = new ParcelOutputStream(client.getOutputStream());
            ParcelInputStream is = new ParcelInputStream(client.getInputStream());
            os.writeInt(0/*ActionsV26.getTasks*/);
            os.writeLong(ShizukuClient.getToken().getMostSignificantBits());
            os.writeLong(ShizukuClient.getToken().getLeastSignificantBits());
            os.writeString(packageName);
            os.writeInt(flags);
            os.writeInt(userId);
            is.readException();
            PackageInfo _result = is.readParcelable(PackageInfo.CREATOR);
            return _result;
        } catch (IOException ignored) {
        }
        return null;
    }
}
