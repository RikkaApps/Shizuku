package android.content.pm;

import android.content.ComponentName;
import android.content.Intent;

import java.util.List;

/**
 * Created by Rikka on 2017/5/16.
 */

public interface IPackageManager {
    List<ResolveInfo> queryIntentActivities(Intent intent, String resolvedType, int flags, int userId);

    List<ResolveInfo> queryIntentActivityOptions(
            ComponentName caller, Intent[] specifics,
            String[] specificTypes, Intent intent,
            String resolvedType, int flags, int userId);

    List<ResolveInfo> queryIntentReceivers(Intent intent, String resolvedType, int flags, int userId);

    List<ResolveInfo> queryIntentServices(Intent intent, String resolvedType, int flags, int userId);

    List<ResolveInfo> queryIntentContentProviders(Intent intent, String resolvedType, int flags, int userId);
}
