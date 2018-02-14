/**
 * Copyright (c) 2007, The Android Open Source Project
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

package com.android.internal.statusbar;

import android.content.ComponentName;
import android.graphics.Rect;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;

import com.android.internal.statusbar.IStatusBar;
import com.android.internal.statusbar.StatusBarIcon;
import com.android.internal.statusbar.NotificationVisibility;

/** @hide */
interface IStatusBarService
{
    void expandNotificationsPanel();
    void collapsePanels();
    void togglePanel();
    void disable(int what, IBinder token, String pkg);
    void disableForUser(int what, IBinder token, String pkg, int userId);
    void disable2(int what, IBinder token, String pkg);
    void disable2ForUser(int what, IBinder token, String pkg, int userId);
    void setIcon(String slot, String iconPackage, int iconId, int iconLevel, String contentDescription);
    void setIconVisibility(String slot, boolean visible);
    void removeIcon(String slot);
    void setImeWindowStatus(IBinder token, int vis, int backDisposition,
            boolean showImeSwitcher);
    void expandSettingsPanel(String subPanel);

    // ---- Methods below are for use by the status bar policy services ----
    // You need the STATUS_BAR_SERVICE permission
    void registerStatusBar(IStatusBar callbacks, List<String> iconSlots,
            List<StatusBarIcon> iconList,
            int[] switches, List<IBinder> binders, Rect fullscreenStackBounds,
            Rect dockedStackBounds);
    void onPanelRevealed(boolean clearNotificationEffects, int numItems);
    void onPanelHidden();
    // Mark current notifications as "seen" and stop ringing, vibrating, blinking.
    void clearNotificationEffects();
    void onNotificationClick(String key);
    void onNotificationActionClick(String key, int actionIndex);
    void onNotificationError(String pkg, String tag, int id,
            int uid, int initialPid, String message, int userId);
    void onClearAllNotifications(int userId);
    void onNotificationClear(String pkg, String tag, int id, int userId);
    void onNotificationVisibilityChanged( NotificationVisibility[] newlyVisibleKeys,
            NotificationVisibility[] noLongerVisibleKeys);
    void onNotificationExpansionChanged(String key, boolean userAction, boolean expanded);
    void setSystemUiVisibility(int vis, int mask, String cause);

    void onGlobalActionsShown();
    void onGlobalActionsHidden();

    /**
     * These methods are needed for global actions control which the UI is shown sysui.
     */
    void shutdown();
    void reboot(boolean safeMode);

    void addTile(ComponentName tile);
    void remTile(ComponentName tile);
    void clickTile(ComponentName tile);
    void handleSystemKey(int key);
}
