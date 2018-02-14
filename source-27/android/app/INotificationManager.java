/* //device/java/android/android/app/INotificationManager.aidl
**
** Copyright 2007, The Android Open Source Project
**
** Licensed under the Apache License, Version 2.0 (the "License");
** you may not use this file except compliance with the License.
** You may obtaa copy of the License at
**
**     http://www.apache.org/licenses/LICENSE-2.0
**
** Unless required by applicable law or agreed to writing, software
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
            Notification notification, int userId);
    void cancelNotificationWithTag(String pkg, String tag, int id, int userId);

    void setShowBadge(String pkg, int uid, boolean showBadge);
    boolean canShowBadge(String pkg, int uid);
    void setNotificationsEnabledForPackage(String pkg, int uid, boolean enabled);
    boolean areNotificationsEnabledForPackage(String pkg, int uid);
    boolean areNotificationsEnabled(String pkg);
    int getPackageImportance(String pkg);

    void createNotificationChannelGroups(String pkg, ParceledListSlice<NotificationChannelGroup> channelGroupList);
    void createNotificationChannels(String pkg, ParceledListSlice<NotificationChannel> channelsList);
    void createNotificationChannelsForPackage(String pkg, int uid, ParceledListSlice<NotificationChannel>  channelsList);
    ParceledListSlice<NotificationChannelGroup> getNotificationChannelGroupsForPackage(String pkg, int uid, boolean includeDeleted);
    NotificationChannelGroup getNotificationChannelGroupForPackage(String groupId, String pkg, int uid);
    void updateNotificationChannelForPackage(String pkg, int uid, NotificationChannel channel);
    NotificationChannel getNotificationChannel(String pkg, String channelId);
    NotificationChannel getNotificationChannelForPackage(String pkg, int uid, String channelId, boolean includeDeleted);
    void deleteNotificationChannel(String pkg, String channelId);
    ParceledListSlice<NotificationChannelGroup> getNotificationChannels(String pkg);
    ParceledListSlice<NotificationChannelGroup> getNotificationChannelsForPackage(String pkg, int uid, boolean includeDeleted);
    int getNumNotificationChannelsForPackage(String pkg, int uid, boolean includeDeleted);
    int getDeletedChannelCount(String pkg, int uid);
    void deleteNotificationChannelGroup(String pkg, String channelGroupId);
    ParceledListSlice<NotificationChannelGroup> getNotificationChannelGroups(String pkg);
    boolean onlyHasDefaultChannel(String pkg, int uid);

    // TODO: Remove this when callers have been migrated to the equivalent
    // INotificationListener method.
    StatusBarNotification[] getActiveNotifications(String callingPkg);
    StatusBarNotification[] getHistoricalNotifications(String callingPkg, int count);

    void registerListener(INotificationListener listener, ComponentName component, int userid);
    void unregisterListener(INotificationListener listener, int userid);

    void cancelNotificationFromListener(INotificationListener token, String pkg, String tag, int id);
    void cancelNotificationsFromListener(INotificationListener token, String[] keys);

    void snoozeNotificationUntilContextFromListener(INotificationListener token, String key, String snoozeCriterionId);
    void snoozeNotificationUntilFromListener(INotificationListener token, String key, long until);

    void requestBindListener(ComponentName component);
    void requestUnbindListener(INotificationListener token);
    void requestBindProvider(ComponentName component);
    void requestUnbindProvider(IConditionProvider token);

    void setNotificationsShownFromListener(INotificationListener token, String[] keys);

    ParceledListSlice<StatusBarNotification> getActiveNotificationsFromListener(INotificationListener token, String[] keys, int trim);
    ParceledListSlice<StatusBarNotification> getSnoozedNotificationsFromListener(INotificationListener token, int trim);
    void requestHintsFromListener(INotificationListener token, int hints);
    int getHintsFromListener(INotificationListener token);
    void requestInterruptionFilterFromListener(INotificationListener token, int interruptionFilter);
    int getInterruptionFilterFromListener(INotificationListener token);
    void setOnNotificationPostedTrimFromListener(INotificationListener token, int trim);
    void setInterruptionFilter(String pkg, int interruptionFilter);

    void updateNotificationChannelFromPrivilegedListener(INotificationListener token, String pkg, UserHandle user, NotificationChannel channel);
    ParceledListSlice<NotificationChannel> getNotificationChannelsFromPrivilegedListener(INotificationListener token, String pkg, UserHandle user);
    ParceledListSlice<NotificationChannelGroup> getNotificationChannelGroupsFromPrivilegedListener(INotificationListener token, String pkg, UserHandle user);

    void applyEnqueuedAdjustmentFromAssistant(INotificationListener token, Adjustment adjustment);
    void applyAdjustmentFromAssistant(INotificationListener token, Adjustment adjustment);
    void applyAdjustmentsFromAssistant(INotificationListener token, List<Adjustment> adjustments);
    void unsnoozeNotificationFromAssistant(INotificationListener token, String key);

    ComponentName getEffectsSuppressor();
    boolean matchesCallFilter(Bundle extras);
    boolean isSystemConditionProviderEnabled(String path);

    boolean isNotificationListenerAccessGranted(ComponentName listener);
    boolean isNotificationListenerAccessGrantedForUser(ComponentName listener, int userId);
    boolean isNotificationAssistantAccessGranted(ComponentName assistant);
    void setNotificationListenerAccessGranted(ComponentName listener, boolean enabled);
    void setNotificationAssistantAccessGranted(ComponentName assistant, boolean enabled);
    void setNotificationListenerAccessGrantedForUser(ComponentName listener, int userId, boolean enabled);
    void setNotificationAssistantAccessGrantedForUser(ComponentName assistant, int userId, boolean enabled);
    List<String> getEnabledNotificationListenerPackages();
    List<ComponentName> getEnabledNotificationListeners(int userId);

    int getZenMode();
    ZenModeConfig getZenModeConfig();
    void setZenMode(int mode, Uri conditionId, String reason);
    void notifyConditions(String pkg, IConditionProvider provider, Condition[] conditions);
    boolean isNotificationPolicyAccessGranted(String pkg);
    NotificationManager.Policy getNotificationPolicy(String pkg);
    void setNotificationPolicy(String pkg, NotificationManager.Policy policy);
    boolean isNotificationPolicyAccessGrantedForPackage(String pkg);
    void setNotificationPolicyAccessGranted(String pkg, boolean granted);
    void setNotificationPolicyAccessGrantedForUser(String pkg, int userId, boolean granted);
    AutomaticZenRule getAutomaticZenRule(String id);
    List<ZenModeConfig.ZenRule> getZenRules();
    String addAutomaticZenRule(AutomaticZenRule automaticZenRule);
    boolean updateAutomaticZenRule(String id, AutomaticZenRule automaticZenRule);
    boolean removeAutomaticZenRule(String id);
    boolean removeAutomaticZenRules(String packageName);
    int getRuleInstanceCount(ComponentName owner);

    byte[] getBackupPayload(int user);
    void applyRestore(byte[] payload, int user);

    ParceledListSlice<StatusBarNotification> getAppActiveNotifications(String callingPkg, int userId);
}
