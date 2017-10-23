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
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ParceledListSlice;
import android.net.Uri;
import android.os.Bundle;
import android.os.UserHandle;
import android.service.notification.Adjustment;
import android.service.notification.Condition;
import android.service.notification.IConditionListener;
import android.service.notification.IConditionProvider;
import android.service.notification.INotificationListener;
import android.service.notification.StatusBarNotification;
import android.app.AutomaticZenRule;
import android.service.notification.ZenModeConfig;

/** {@hide} */
interface INotificationManager
{
    void cancelAllNotifications(String pkg, int userId);

    void clearData(String pkg, int uid, boolean fromApp);
    void enqueueToast(String pkg, ITransientNotification callback, int duration);
    void cancelToast(String pkg, ITransientNotification callback);
    void enqueueNotificationWithTag(String pkg, String opPkg, String tag, int id,
            in Notification notification, int userId);
    void cancelNotificationWithTag(String pkg, String tag, int id, int userId);

    void setShowBadge(String pkg, int uid, boolean showBadge);
    boolean canShowBadge(String pkg, int uid);
    void setNotificationsEnabledForPackage(String pkg, int uid, boolean enabled);
    boolean areNotificationsEnabledForPackage(String pkg, int uid);
    boolean areNotificationsEnabled(String pkg);
    int getPackageImportance(String pkg);

    void createNotificationChannelGroups(String pkg, in ParceledListSlice channelGroupList);
    void createNotificationChannels(String pkg, in ParceledListSlice channelsList);
    void createNotificationChannelsForPackage(String pkg, int uid, in ParceledListSlice channelsList);
    ParceledListSlice getNotificationChannelGroupsForPackage(String pkg, int uid, boolean includeDeleted);
    NotificationChannelGroup getNotificationChannelGroupForPackage(String groupId, String pkg, int uid);
    void updateNotificationChannelForPackage(String pkg, int uid, in NotificationChannel channel);
    NotificationChannel getNotificationChannel(String pkg, String channelId);
    NotificationChannel getNotificationChannelForPackage(String pkg, int uid, String channelId, boolean includeDeleted);
    void deleteNotificationChannel(String pkg, String channelId);
    ParceledListSlice getNotificationChannels(String pkg);
    ParceledListSlice getNotificationChannelsForPackage(String pkg, int uid, boolean includeDeleted);
    int getNumNotificationChannelsForPackage(String pkg, int uid, boolean includeDeleted);
    int getDeletedChannelCount(String pkg, int uid);
    void deleteNotificationChannelGroup(String pkg, String channelGroupId);
    ParceledListSlice getNotificationChannelGroups(String pkg);
    boolean onlyHasDefaultChannel(String pkg, int uid);

    // TODO: Remove this when callers have been migrated to the equivalent
    // INotificationListener method.
    StatusBarNotification[] getActiveNotifications(String callingPkg);
    StatusBarNotification[] getHistoricalNotifications(String callingPkg, int count);

    void registerListener(in INotificationListener listener, in ComponentName component, int userid);
    void unregisterListener(in INotificationListener listener, int userid);

    void cancelNotificationFromListener(in INotificationListener token, String pkg, String tag, int id);
    void cancelNotificationsFromListener(in INotificationListener token, in String[] keys);

    void snoozeNotificationUntilContextFromListener(in INotificationListener token, String key, String snoozeCriterionId);
    void snoozeNotificationUntilFromListener(in INotificationListener token, String key, long until);

    void requestBindListener(in ComponentName component);
    void requestUnbindListener(in INotificationListener token);
    void requestBindProvider(in ComponentName component);
    void requestUnbindProvider(in IConditionProvider token);

    void setNotificationsShownFromListener(in INotificationListener token, in String[] keys);

    ParceledListSlice getActiveNotificationsFromListener(in INotificationListener token, in String[] keys, int trim);
    ParceledListSlice getSnoozedNotificationsFromListener(in INotificationListener token, int trim);
    void requestHintsFromListener(in INotificationListener token, int hints);
    int getHintsFromListener(in INotificationListener token);
    void requestInterruptionFilterFromListener(in INotificationListener token, int interruptionFilter);
    int getInterruptionFilterFromListener(in INotificationListener token);
    void setOnNotificationPostedTrimFromListener(in INotificationListener token, int trim);
    void setInterruptionFilter(String pkg, int interruptionFilter);

    void updateNotificationChannelFromPrivilegedListener(in INotificationListener token, String pkg, in UserHandle user, in NotificationChannel channel);
    ParceledListSlice getNotificationChannelsFromPrivilegedListener(in INotificationListener token, String pkg, in UserHandle user);
    ParceledListSlice getNotificationChannelGroupsFromPrivilegedListener(in INotificationListener token, String pkg, in UserHandle user);

    void applyEnqueuedAdjustmentFromAssistant(in INotificationListener token, in Adjustment adjustment);
    void applyAdjustmentFromAssistant(in INotificationListener token, in Adjustment adjustment);
    void applyAdjustmentsFromAssistant(in INotificationListener token, in List<Adjustment> adjustments);
    void unsnoozeNotificationFromAssistant(in INotificationListener token, String key);

    ComponentName getEffectsSuppressor();
    boolean matchesCallFilter(in Bundle extras);
    boolean isSystemConditionProviderEnabled(String path);

    int getZenMode();
    ZenModeConfig getZenModeConfig();
    oneway void setZenMode(int mode, in Uri conditionId, String reason);
    oneway void notifyConditions(String pkg, in IConditionProvider provider, in Condition[] conditions);
    boolean isNotificationPolicyAccessGranted(String pkg);
    NotificationManager.Policy getNotificationPolicy(String pkg);
    void setNotificationPolicy(String pkg, in NotificationManager.Policy policy);
    String[] getPackagesRequestingNotificationPolicyAccess();
    boolean isNotificationPolicyAccessGrantedForPackage(String pkg);
    void setNotificationPolicyAccessGranted(String pkg, boolean granted);
    AutomaticZenRule getAutomaticZenRule(String id);
    List<ZenModeConfig.ZenRule> getZenRules();
    String addAutomaticZenRule(in AutomaticZenRule automaticZenRule);
    boolean updateAutomaticZenRule(String id, in AutomaticZenRule automaticZenRule);
    boolean removeAutomaticZenRule(String id);
    boolean removeAutomaticZenRules(String packageName);
    int getRuleInstanceCount(in ComponentName owner);

    byte[] getBackupPayload(int user);
    void applyRestore(in byte[] payload, int user);

    ParceledListSlice getAppActiveNotifications(String callingPkg, int userId);
}
