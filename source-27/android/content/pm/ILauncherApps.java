/**
 * Copyright (c) 2014, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except compliance with the License.
 * You may obtaa copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.content.pm;

import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.IOnAppsChangedListener;
import android.content.pm.ParceledListSlice;
import android.content.pm.ResolveInfo;
import android.content.pm.ShortcutInfo;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.ParcelFileDescriptor;

import java.util.List;

/**
 * {@hide}
 */
interface ILauncherApps {
    void addOnAppsChangedListener(String callingPackage, IOnAppsChangedListener listener);
    void removeOnAppsChangedListener(IOnAppsChangedListener listener);
    ParceledListSlice<ResolveInfo> getLauncherActivities(
            String callingPackage, String packageName, UserHandle user);
    ActivityInfo resolveActivity(
            String callingPackage, ComponentName component, UserHandle user);
    void startActivityAsUser(String callingPackage,
            ComponentName component, Rect sourceBounds,
            Bundle opts, UserHandle user);
    void showAppDetailsAsUser(
            String callingPackage, ComponentName component, Rect sourceBounds,
            Bundle opts, UserHandle user);
    boolean isPackageEnabled(String callingPackage, String packageName, UserHandle user);
    boolean isActivityEnabled(
            String callingPackage, ComponentName component, UserHandle user);
    ApplicationInfo getApplicationInfo(
            String callingPackage, String packageName, int flags, UserHandle user);

    ParceledListSlice<ShortcutInfo> getShortcuts(String callingPackage, long changedSince, String packageName,
            List<String> shortcutIds, ComponentName componentName, int flags, UserHandle user);
    void pinShortcuts(String callingPackage, String packageName, List<String> shortcutIds,
            UserHandle user);
    boolean startShortcut(String callingPackage, String packageName, String id,
            Rect sourceBounds, Bundle startActivityOptions, int userId);

    int getShortcutIconResId(String callingPackage, String packageName, String id,
            int userId);
    ParcelFileDescriptor getShortcutIconFd(String callingPackage, String packageName, String id,
            int userId);

    boolean hasShortcutHostPermission(String callingPackage);

    ParceledListSlice<ResolveInfo> getShortcutConfigActivities(
            String callingPackage, String packageName, UserHandle user);
    IntentSender getShortcutConfigActivityIntent(String callingPackage, ComponentName component,
            UserHandle user);
}
