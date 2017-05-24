package moe.shizuku.server;

import android.content.Intent;
import android.content.pm.IPackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.os.IUserManager;

import java.util.List;

/**
 * Created by Rikka on 2017/5/8.
 */

public class HideApiOverrideM {

    public static Bitmap getUserIcon(IUserManager um, int userHandle) {
        return um.getUserIcon(userHandle);
    }

    public static List<ResolveInfo> queryIntentActivities(IPackageManager packageManager, Intent intent, String resolvedType, int flags, int userId) {
        return packageManager.queryIntentActivities(intent, resolvedType, flags, userId);
    }
}
