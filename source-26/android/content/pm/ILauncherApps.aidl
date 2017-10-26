/**
 * Copyright (c) 2014, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
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
    void addOnAppsChangedListener(String callingPackage, in IOnAppsChangedListener listener);
    void removeOnAppsChangedListener(in IOnAppsChangedListener listener);
    ParceledListSlice<ResolveInfo> getLauncherActivities(
            String callingPackage, String packageName, in UserHandle user);
    ActivityInfo resolveActivity(
            String callingPackage, in ComponentName component, in UserHandle user);
    void startActivityAsUser(String callingPackage,
            in ComponentName component, in Rect sourceBounds,
            in Bundle opts, in UserHandle user);
    void showAppDetailsAsUser(
            String callingPackage, in ComponentName component, in Rect sourceBounds,
            in Bundle opts, in UserHandle user);
    boolean isPackageEnabled(String callingPackage, String packageName, in UserHandle user);
    boolean isActivityEnabled(
            String callingPackage, in ComponentName component, in UserHandle user);
    ApplicationInfo getApplicationInfo(
            String callingPackage, String packageName, int flags, in UserHandle user);

    ParceledListSlice<ShortcutInfo> getShortcuts(String callingPackage, long changedSince, String packageName,
            in List<String> shortcutIds, in ComponentName componentName, int flags, in UserHandle user);
    void pinShortcuts(String callingPackage, String packageName, in List<String> shortcutIds,
            in UserHandle user);
    boolean startShortcut(String callingPackage, String packageName, String id,
            in Rect sourceBounds, in Bundle startActivityOptions, int userId);

    int getShortcutIconResId(String callingPackage, String packageName, String id,
            int userId);
    ParcelFileDescriptor getShortcutIconFd(String callingPackage, String packageName, String id,
            int userId);

    boolean hasShortcutHostPermission(String callingPackage);

    ParceledListSlice<ResolveInfo> getShortcutConfigActivities(
            String callingPackage, String packageName, in UserHandle user);
    IntentSender getShortcutConfigActivityIntent(String callingPackage, in ComponentName component,
            in UserHandle user);
}
