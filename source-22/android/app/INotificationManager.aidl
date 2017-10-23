/* //device/java/android/android/app/INotificationManager.aidl
**
** Copyright 2007, The Android Open Source Project
**
** Licensed under the Apache License, Version 2.0 (the "License"); 
** you may not use this file except in compliance with the License. 
** You may obtain a copy of the License at 
**
**     http://www.apache.org/licenses/LICENSE-2.0 
**
** Unless required by applicable law or agreed to in writing, software 
** distributed under the License is distributed on an "AS IS" BASIS, 
** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
** See the License for the specific language governing permissions and 
** limitations under the License.
*/

package android.app;

import android.app.ITransientNotification;
import android.app.Notification;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ParceledListSlice;
import android.net.Uri;
import android.os.Bundle;
import android.service.notification.Condition;
import android.service.notification.IConditionListener;
import android.service.notification.IConditionProvider;
import android.service.notification.INotificationListener;
import android.service.notification.StatusBarNotification;
import android.service.notification.ZenModeConfig;

/** {@hide} */
interface INotificationManager
{
    void cancelAllNotifications(String pkg, int userId);

    void enqueueToast(String pkg, ITransientNotification callback, int duration);
    void cancelToast(String pkg, ITransientNotification callback);
    void enqueueNotificationWithTag(String pkg, String opPkg, String tag, int id,
            in Notification notification, inout int[] idReceived, int userId);
    void cancelNotificationWithTag(String pkg, String tag, int id, int userId);

    void setNotificationsEnabledForPackage(String pkg, int uid, boolean enabled);
    boolean areNotificationsEnabledForPackage(String pkg, int uid);

    void setPackagePriority(String pkg, int uid, int priority);
    int getPackagePriority(String pkg, int uid);

    void setPackageVisibilityOverride(String pkg, int uid, int visibility);
    int getPackageVisibilityOverride(String pkg, int uid);

    // TODO: Remove this when callers have been migrated to the equivalent
    // INotificationListener method.
    StatusBarNotification[] getActiveNotifications(String callingPkg);
    StatusBarNotification[] getHistoricalNotifications(String callingPkg, int count);

    void registerListener(in INotificationListener listener, in ComponentName component, int userid);
    void unregisterListener(in INotificationListener listener, int userid);

    void cancelNotificationFromListener(in INotificationListener token, String pkg, String tag, int id);
    void cancelNotificationsFromListener(in INotificationListener token, in String[] keys);

    ParceledListSlice getActiveNotificationsFromListener(in INotificationListener token, in String[] keys, int trim);
    void requestHintsFromListener(in INotificationListener token, int hints);
    int getHintsFromListener(in INotificationListener token);
    void requestInterruptionFilterFromListener(in INotificationListener token, int interruptionFilter);
    int getInterruptionFilterFromListener(in INotificationListener token);
    void setOnNotificationPostedTrimFromListener(in INotificationListener token, int trim);

    ComponentName getEffectsSuppressor();
    boolean matchesCallFilter(in Bundle extras);
    boolean isSystemConditionProviderEnabled(String path);

    ZenModeConfig getZenModeConfig();
    boolean setZenModeConfig(in ZenModeConfig config);
    oneway void notifyConditions(String pkg, in IConditionProvider provider, in Condition[] conditions);
    oneway void requestZenModeConditions(in IConditionListener callback, int relevance);
    oneway void setZenModeCondition(in Condition condition);
    oneway void setAutomaticZenModeConditions(in Uri[] conditionIds);
    Condition[] getAutomaticZenModeConditions();
}
