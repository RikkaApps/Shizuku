/**
 * Copyright (c) 2007, The Android Open Source Project
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

package com.android.internal.statusbar;

import com.android.internal.statusbar.IStatusBar;
import com.android.internal.statusbar.StatusBarIcon;
import com.android.internal.statusbar.StatusBarIconList;
import android.service.notification.StatusBarNotification;

/** @hide */
interface IStatusBarService
{
    void expandNotificationsPanel();
    void collapsePanels();
    void disable(int what, IBinder token, String pkg);
    void setIcon(String slot, String iconPackage, int iconId, int iconLevel, String contentDescription);
    void setIconVisibility(String slot, boolean visible);
    void removeIcon(String slot);
    void topAppWindowChanged(boolean menuVisible);
    void setImeWindowStatus(in IBinder token, int vis, int backDisposition,
            boolean showImeSwitcher);
    void expandSettingsPanel();
    void setCurrentUser(int newUserId);

    // ---- Methods below are for use by the status bar policy services ----
    // You need the STATUS_BAR_SERVICE permission
    void registerStatusBar(IStatusBar callbacks, out StatusBarIconList iconList,
            out int[] switches, out List<IBinder> binders);
    void onPanelRevealed(boolean clearNotificationEffects);
    void onPanelHidden();
    // Mark current notifications as "seen" and stop ringing, vibrating, blinking.
    void clearNotificationEffects();
    void onNotificationClick(String key);
    void onNotificationActionClick(String key, int actionIndex);
    void onNotificationError(String pkg, String tag, int id,
            int uid, int initialPid, String message, int userId);
    void onClearAllNotifications(int userId);
    void onNotificationClear(String pkg, String tag, int id, int userId);
    void onNotificationVisibilityChanged(
            in String[] newlyVisibleKeys, in String[] noLongerVisibleKeys);
    void onNotificationExpansionChanged(in String key, in boolean userAction, in boolean expanded);
    void setSystemUiVisibility(int vis, int mask, String cause);
    void setWindowState(int window, int state);

    void showRecentApps(boolean triggeredFromAltTab);
    void hideRecentApps(boolean triggeredFromAltTab, boolean triggeredFromHomeKey);
    void toggleRecentApps();
    void preloadRecentApps();
    void cancelPreloadRecentApps();
}
