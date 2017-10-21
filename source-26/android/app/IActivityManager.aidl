/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.app;

import android.app.ActivityManager;
import android.app.ApplicationErrorReport;
import android.app.ContentProviderHolder;
import android.app.IApplicationThread;
import android.app.IActivityContainer;
import android.app.IActivityContainerCallback;
import android.app.IActivityController;
import android.app.IAppTask;
import android.app.IInstrumentationWatcher;
import android.app.IProcessObserver;
import android.app.IServiceConnection;
import android.app.IStopUserCallback;
import android.app.ITaskStackListener;
import android.app.IUiAutomationConnection;
import android.app.IUidObserver;
import android.app.IUserSwitchObserver;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.PictureInPictureParams;
import android.app.ProfilerInfo;
import android.app.WaitResult;
import android.app.assist.AssistContent;
import android.app.assist.AssistStructure;
import android.content.ComponentName;
import android.content.IIntentReceiver;
import android.content.IIntentSender;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.ApplicationInfo;
import android.content.pm.ConfigurationInfo;
import android.content.pm.IPackageDataObserver;
import android.content.pm.ParceledListSlice;
import android.content.pm.ProviderInfo;
import android.content.pm.UserInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.GraphicBuffer;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Debug;
import android.os.IBinder;
import android.os.IProgressListener;
import android.os.ParcelFileDescriptor;
import android.os.PersistableBundle;
import android.os.StrictMode;
import android.service.voice.IVoiceInteractionSession;
import com.android.internal.app.IVoiceInteractor;
import com.android.internal.os.IResultReceiver;
import com.android.internal.policy.IKeyguardDismissCallback;

import java.util.List;

/**
 * System private API for talking with the activity manager service.  This
 * provides calls from the application back to the activity manager.
 *
 * {@hide}
 */
interface IActivityManager {
    // WARNING: when these transactions are updated, check if they are any callers on the native
    // side. If so, make sure they are using the correct transaction ids and arguments.
    // If a transaction which will also be used on the native side is being inserted, add it to
    // below block of transactions.

    // Since these transactions are also called from native code, these must be kept in sync with
    // the ones in frameworks/native/include/binder/IActivityManager.h
    // =============== Beginning of transactions used on native side as well ======================
    ParcelFileDescriptor openContentUri(in String uriString);
    // =============== End of transactions used on native side as well ============================

    // Special low-level communication with activity manager.
    void handleApplicationCrash(in IBinder app,
            in ApplicationErrorReport.ParcelableCrashInfo crashInfo);
    int startActivity(in IApplicationThread caller, in String callingPackage, in Intent intent,
            in String resolvedType, in IBinder resultTo, in String resultWho, int requestCode,
            int flags, in ProfilerInfo profilerInfo, in Bundle options);
    void unhandledBack();

    boolean finishActivity(in IBinder token, int code, in Intent data, int finishTask);
    Intent registerReceiver(in IApplicationThread caller, in String callerPackage,
            in IIntentReceiver receiver, in IntentFilter filter,
            in String requiredPermission, int userId, int flags);
    void unregisterReceiver(in IIntentReceiver receiver);
    int broadcastIntent(in IApplicationThread caller, in Intent intent,
            in String resolvedType, in IIntentReceiver resultTo, int resultCode,
            in String resultData, in Bundle map, in String[] requiredPermissions,
            int appOp, in Bundle options, boolean serialized, boolean sticky, int userId);
    void unbroadcastIntent(in IApplicationThread caller, in Intent intent, int userId);
    oneway void finishReceiver(in IBinder who, int resultCode, in String resultData, in Bundle map,
            boolean abortBroadcast, int flags);
    void attachApplication(in IApplicationThread app);
    oneway void activityIdle(in IBinder token, in Configuration config,
            in boolean stopProfiling);
    void activityPaused(in IBinder token);
    oneway void activityStopped(in IBinder token, in Bundle state,
            in PersistableBundle persistentState, in CharSequence description);
    String getCallingPackage(in IBinder token);
    ComponentName getCallingActivity(in IBinder token);
    List<ActivityManager.RunningTaskInfo> getTasks(int maxNum, int flags);
    void moveTaskToFront(int task, int flags, in Bundle options);
    void moveTaskBackwards(int task);
    int getTaskForActivity(in IBinder token, in boolean onlyRoot);
    ContentProviderHolder getContentProvider(in IApplicationThread caller,
            in String name, int userId, boolean stable);
    void publishContentProviders(in IApplicationThread caller,
            in List<ContentProviderHolder> providers);
    boolean refContentProvider(in IBinder connection, int stableDelta, int unstableDelta);
    void finishSubActivity(in IBinder token, in String resultWho, int requestCode);
    PendingIntent getRunningServiceControlPanel(in ComponentName service);
    ComponentName startService(in IApplicationThread caller, in Intent service,
            in String resolvedType, boolean requireForeground, in String callingPackage, int userId);
    int stopService(in IApplicationThread caller, in Intent service,
            in String resolvedType, int userId);
    int bindService(in IApplicationThread caller, in IBinder token, in Intent service,
            in String resolvedType, in IServiceConnection connection, int flags,
            in String callingPackage, int userId);
    boolean unbindService(in IServiceConnection connection);
    void publishService(in IBinder token, in Intent intent, in IBinder service);
    void activityResumed(in IBinder token);
    void setDebugApp(in String packageName, boolean waitForDebugger, boolean persistent);
    void setAlwaysFinish(boolean enabled);
    boolean startInstrumentation(in ComponentName className, in String profileFile,
            int flags, in Bundle arguments, in IInstrumentationWatcher watcher,
            in IUiAutomationConnection connection, int userId,
            in String abiOverride);
    void addInstrumentationResults(in IApplicationThread target, in Bundle results);
    void finishInstrumentation(in IApplicationThread target, int resultCode,
            in Bundle results);
    /**
     * @return A copy of global {@link Configuration}, contains general settings for the entire
     *         system. Corresponds to the configuration of the default display.
     * @throws RemoteException
     */
    Configuration getConfiguration();
    /**
     * Updates global configuration and applies changes to the entire system.
     * @param values Update values for global configuration. If null is passed it will request the
     *               Window Manager to compute new config for the default display.
     * @throws RemoteException
     * @return Returns true if the configuration was updated.
     */
    boolean updateConfiguration(in Configuration values);
    boolean stopServiceToken(in ComponentName className, in IBinder token, int startId);
    ComponentName getActivityClassForToken(in IBinder token);
    String getPackageForToken(in IBinder token);
    void setProcessLimit(int max);
    int getProcessLimit();
    int checkPermission(in String permission, int pid, int uid);
    int checkUriPermission(in Uri uri, int pid, int uid, int mode, int userId,
            in IBinder callerToken);
    void grantUriPermission(in IApplicationThread caller, in String targetPkg, in Uri uri,
            int mode, int userId);
    void revokeUriPermission(in IApplicationThread caller, in String targetPkg, in Uri uri,
            int mode, int userId);
    void setActivityController(in IActivityController watcher, boolean imAMonkey);
    void showWaitingForDebugger(in IApplicationThread who, boolean waiting);
    /*
     * This will deliver the specified signal to all the persistent processes. Currently only
     * SIGUSR1 is delivered. All others are ignored.
     */
    void signalPersistentProcesses(int signal);
    ParceledListSlice getRecentTasks(int maxNum,
            int flags, int userId);
    oneway void serviceDoneExecuting(in IBinder token, int type, int startId, int res);
    oneway void activityDestroyed(in IBinder token);
    IIntentSender getIntentSender(int type, in String packageName, in IBinder token,
            in String resultWho, int requestCode, in Intent[] intents, in String[] resolvedTypes,
            int flags, in Bundle options, int userId);
    void cancelIntentSender(in IIntentSender sender);
    String getPackageForIntentSender(in IIntentSender sender);
    void registerIntentSenderCancelListener(in IIntentSender sender, in IResultReceiver receiver);
    void unregisterIntentSenderCancelListener(in IIntentSender sender, in IResultReceiver receiver);
    void enterSafeMode();
    boolean startNextMatchingActivity(in IBinder callingActivity,
            in Intent intent, in Bundle options);
    void noteWakeupAlarm(in IIntentSender sender, int sourceUid,
            in String sourcePkg, in String tag);
    void removeContentProvider(in IBinder connection, boolean stable);
    void setRequestedOrientation(in IBinder token, int requestedOrientation);
    int getRequestedOrientation(in IBinder token);
    void unbindFinished(in IBinder token, in Intent service, boolean doRebind);
    void setProcessImportant(in IBinder token, int pid, boolean isForeground, String reason);
    void setServiceForeground(in ComponentName className, in IBinder token,
            int id, in Notification notification, int flags);
    boolean moveActivityTaskToBack(in IBinder token, boolean nonRoot);
    void getMemoryInfo(out ActivityManager.MemoryInfo outInfo);
    List<ActivityManager.ProcessErrorStateInfo> getProcessesInErrorState();
    boolean clearApplicationUserData(in String packageName,
            in IPackageDataObserver observer, int userId);
    void forceStopPackage(in String packageName, int userId);
    boolean killPids(in int[] pids, in String reason, boolean secure);
    List<ActivityManager.RunningServiceInfo> getServices(int maxNum, int flags);
    ActivityManager.TaskThumbnail getTaskThumbnail(int taskId);
    ActivityManager.TaskDescription getTaskDescription(int taskId);
    // Retrieve running application processes in the system
    List<ActivityManager.RunningAppProcessInfo> getRunningAppProcesses();
    // Get device configuration
    ConfigurationInfo getDeviceConfigurationInfo();
    IBinder peekService(in Intent service, in String resolvedType, in String callingPackage);
    // Turn on/off profiling in a particular process.
    boolean profileControl(in String process, int userId, boolean start,
            in ProfilerInfo profilerInfo, int profileType);
    boolean shutdown(int timeout);
    void stopAppSwitches();
    void resumeAppSwitches();
    boolean bindBackupAgent(in String packageName, int backupRestoreMode, int userId);
    void backupAgentCreated(in String packageName, in IBinder agent);
    void unbindBackupAgent(in ApplicationInfo appInfo);
    int getUidForIntentSender(in IIntentSender sender);
    int handleIncomingUser(int callingPid, int callingUid, int userId, boolean allowAll,
            boolean requireFull, in String name, in String callerPackage);
    void addPackageDependency(in String packageName);
    void killApplication(in String pkg, int appId, int userId, in String reason);
    void closeSystemDialogs(in String reason);
    Debug.MemoryInfo[] getProcessMemoryInfo(in int[] pids);
    void killApplicationProcess(in String processName, int uid);
    int startActivityIntentSender(in IApplicationThread caller,
            in IIntentSender target, in IBinder whitelistToken, in Intent fillInIntent,
            in String resolvedType, in IBinder resultTo, in String resultWho, int requestCode,
            int flagsMask, int flagsValues, in Bundle options);
    void overridePendingTransition(in IBinder token, in String packageName,
            int enterAnim, int exitAnim);
    // Special low-level communication with activity manager.
    boolean handleApplicationWtf(in IBinder app, in String tag, boolean system,
            in ApplicationErrorReport.ParcelableCrashInfo crashInfo);
    void killBackgroundProcesses(in String packageName, int userId);
    boolean isUserAMonkey();
    WaitResult startActivityAndWait(in IApplicationThread caller, in String callingPackage,
            in Intent intent, in String resolvedType, in IBinder resultTo, in String resultWho,
            int requestCode, int flags, in ProfilerInfo profilerInfo, in Bundle options,
            int userId);
    boolean willActivityBeVisible(in IBinder token);
    int startActivityWithConfig(in IApplicationThread caller, in String callingPackage,
            in Intent intent, in String resolvedType, in IBinder resultTo, in String resultWho,
            int requestCode, int startFlags, in Configuration newConfig,
            in Bundle options, int userId);
    // Retrieve info of applications installed on external media that are currently
    // running.
    List<ApplicationInfo> getRunningExternalApplications();
    void finishHeavyWeightApp();
    // A StrictMode violation to be handled.  The violationMask is a
    // subset of the original StrictMode policy bitmask, with only the
    // bit violated and penalty bits to be executed by the
    // ActivityManagerService remaining set.
    void handleApplicationStrictModeViolation(in IBinder app, int violationMask,
            in StrictMode.ViolationInfo crashInfo);
    boolean isImmersive(in IBinder token);
    void setImmersive(in IBinder token, boolean immersive);
    boolean isTopActivityImmersive();
    void crashApplication(int uid, int initialPid, in String packageName, int userId, in String message);
    String getProviderMimeType(in Uri uri, int userId);
    IBinder newUriPermissionOwner(in String name);
    void grantUriPermissionFromOwner(in IBinder owner, int fromUid, in String targetPkg,
            in Uri uri, int mode, int sourceUserId, int targetUserId);
    void revokeUriPermissionFromOwner(in IBinder owner, in Uri uri, int mode, int userId);
    int checkGrantUriPermission(int callingUid, in String targetPkg, in Uri uri,
            int modeFlags, int userId);
    // Cause the specified process to dump the specified heap.
    boolean dumpHeap(in String process, int userId, boolean managed, in String path,
            in ParcelFileDescriptor fd);
    int startActivities(in IApplicationThread caller, in String callingPackage,
            in Intent[] intents, in String[] resolvedTypes, in IBinder resultTo,
            in Bundle options, int userId);
    boolean isUserRunning(int userid, int flags);
    oneway void activitySlept(in IBinder token);
    int getFrontActivityScreenCompatMode();
    void setFrontActivityScreenCompatMode(int mode);
    int getPackageScreenCompatMode(in String packageName);
    void setPackageScreenCompatMode(in String packageName, int mode);
    boolean getPackageAskScreenCompat(in String packageName);
    void setPackageAskScreenCompat(in String packageName, boolean ask);
    boolean switchUser(int userid);
    void setFocusedTask(int taskId);
    boolean removeTask(int taskId);
    void registerProcessObserver(in IProcessObserver observer);
    void unregisterProcessObserver(in IProcessObserver observer);
    boolean isIntentSenderTargetedToPackage(in IIntentSender sender);
    void updatePersistentConfiguration(in Configuration values);
    long[] getProcessPss(in int[] pids);
    void showBootMessage(in CharSequence msg, boolean always);
    void killAllBackgroundProcesses();
    ContentProviderHolder getContentProviderExternal(in String name, int userId,
            in IBinder token);
    void removeContentProviderExternal(in String name, in IBinder token);
    // Get memory information about the calling process.
    void getMyMemoryState(out ActivityManager.RunningAppProcessInfo outInfo);
    boolean killProcessesBelowForeground(in String reason);
    UserInfo getCurrentUser();
    boolean shouldUpRecreateTask(in IBinder token, in String destAffinity);
    boolean navigateUpTo(in IBinder token, in Intent target, int resultCode,
            in Intent resultData);
    void setLockScreenShown(boolean showing);
    boolean finishActivityAffinity(in IBinder token);
    // This is not public because you need to be very careful in how you
    // manage your activity to make sure it is always the uid you expect.
    int getLaunchedFromUid(in IBinder activityToken);
    void unstableProviderDied(in IBinder connection);
    boolean isIntentSenderAnActivity(in IIntentSender sender);
    int startActivityAsUser(in IApplicationThread caller, in String callingPackage,
            in Intent intent, in String resolvedType, in IBinder resultTo, in String resultWho,
            int requestCode, int flags, in ProfilerInfo profilerInfo,
            in Bundle options, int userId);
    int stopUser(int userid, boolean force, in IStopUserCallback callback);
    void registerUserSwitchObserver(in IUserSwitchObserver observer, in String name);
    void unregisterUserSwitchObserver(in IUserSwitchObserver observer);
    int[] getRunningUserIds();

    // Deprecated - This method is only used by a few internal components and it will soon be
    // replaced by a proper bug report API (which will be restricted to a few, pre-defined apps).
    // No new code should be calling it.
    void requestBugReport(int bugreportType);

    /**
     *  Takes a telephony bug report and notifies the user with the title and description
     *  that are passed to this API as parameters
     *
     *  @param shareTitle should be a valid legible string less than 50 chars long
     *  @param shareDescription should be less than 91 bytes when encoded into UTF-8 format
     *
     *  @throws IllegalArgumentException if shareTitle or shareDescription is too big or if the
     *          paremeters cannot be encoding to an UTF-8 charset.
     */
    void requestTelephonyBugReport(in String shareTitle, in String shareDescription);

    long inputDispatchingTimedOut(int pid, boolean aboveSystem, in String reason);
    void clearPendingBackup();
    Intent getIntentForIntentSender(in IIntentSender sender);
    Bundle getAssistContextExtras(int requestType);
    void reportAssistContextExtras(in IBinder token, in Bundle extras,
            in AssistStructure structure, in AssistContent content, in Uri referrer);
    // This is not public because you need to be very careful in how you
    // manage your activity to make sure it is always the uid you expect.
    String getLaunchedFromPackage(in IBinder activityToken);
    void killUid(int appId, int userId, in String reason);
    void setUserIsMonkey(boolean monkey);
    void hang(in IBinder who, boolean allowRestart);
    IActivityContainer createVirtualActivityContainer(in IBinder parentActivityToken,
            in IActivityContainerCallback callback);
    void moveTaskToStack(int taskId, int stackId, boolean toTop);
    /**
     * Resizes the input stack id to the given bounds.
     *
     * @param stackId Id of the stack to resize.
     * @param bounds Bounds to resize the stack to or {@code null} for fullscreen.
     * @param allowResizeInDockedMode True if the resize should be allowed when the docked stack is
     *                                active.
     * @param preserveWindows True if the windows of activities contained in the stack should be
     *                        preserved.
     * @param animate True if the stack resize should be animated.
     * @param animationDuration The duration of the resize animation in milliseconds or -1 if the
     *                          default animation duration should be used.
     * @throws RemoteException
     */
    void resizeStack(int stackId, in Rect bounds, boolean allowResizeInDockedMode,
            boolean preserveWindows, boolean animate, int animationDuration);
    List<ActivityManager.StackInfo> getAllStackInfos();
    void setFocusedStack(int stackId);
    ActivityManager.StackInfo getStackInfo(int stackId);
    boolean convertFromTranslucent(in IBinder token);
    boolean convertToTranslucent(in IBinder token, in Bundle options);
    void notifyActivityDrawn(in IBinder token);
    void reportActivityFullyDrawn(in IBinder token);
    void restart();
    void performIdleMaintenance();
    void takePersistableUriPermission(in Uri uri, int modeFlags, int userId);
    void releasePersistableUriPermission(in Uri uri, int modeFlags, int userId);
    ParceledListSlice getPersistedUriPermissions(in String packageName, boolean incoming);
    void appNotRespondingViaProvider(in IBinder connection);
    Rect getTaskBounds(int taskId);
    int getActivityDisplayId(in IBinder activityToken);
    boolean setProcessMemoryTrimLevel(in String process, int uid, int level);


    // Start of L transactions
    String getTagForIntentSender(in IIntentSender sender, in String prefix);
    boolean startUserInBackground(int userid);
    void startLockTaskModeById(int taskId);
    void startLockTaskModeByToken(in IBinder token);
    void stopLockTaskMode();
    boolean isInLockTaskMode();
    void setTaskDescription(in IBinder token, in ActivityManager.TaskDescription values);
    int startVoiceActivity(in String callingPackage, int callingPid, int callingUid,
            in Intent intent, in String resolvedType, in IVoiceInteractionSession session,
            in IVoiceInteractor interactor, int flags, in ProfilerInfo profilerInfo,
            in Bundle options, int userId);
    int startAssistantActivity(in String callingPackage, int callingPid, int callingUid,
            in Intent intent, in String resolvedType, in Bundle options, int userId);
    Bundle getActivityOptions(in IBinder token);
    List<IBinder> getAppTasks(in String callingPackage);
    void startSystemLockTaskMode(int taskId);
    void stopSystemLockTaskMode();
    void finishVoiceTask(in IVoiceInteractionSession session);
    boolean isTopOfTask(in IBinder token);
    boolean requestVisibleBehind(in IBinder token, boolean visible);
    boolean isBackgroundVisibleBehind(in IBinder token);
    void backgroundResourcesReleased(in IBinder token);
    void notifyLaunchTaskBehindComplete(in IBinder token);
    int startActivityFromRecents(int taskId, in Bundle options);
    void notifyEnterAnimationComplete(in IBinder token);
    int startActivityAsCaller(in IApplicationThread caller, in String callingPackage,
            in Intent intent, in String resolvedType, in IBinder resultTo, in String resultWho,
            int requestCode, int flags, in ProfilerInfo profilerInfo, in Bundle options,
            boolean ignoreTargetSecurity, int userId);
    int addAppTask(in IBinder activityToken, in Intent intent,
            in ActivityManager.TaskDescription description, in Bitmap thumbnail);
    Point getAppTaskThumbnailSize();
    boolean releaseActivityInstance(in IBinder token);
    void releaseSomeActivities(in IApplicationThread app);
    void bootAnimationComplete();
    Bitmap getTaskDescriptionIcon(in String filename, int userId);
    boolean launchAssistIntent(in Intent intent, int requestType, in String hint, int userHandle,
            in Bundle args);
    void startInPlaceAnimationOnFrontMostApplication(in Bundle opts);
    int checkPermissionWithToken(in String permission, int pid, int uid,
            in IBinder callerToken);
    void registerTaskStackListener(in ITaskStackListener listener);


    // Start of M transactions
    void notifyCleartextNetwork(int uid, in byte[] firstPacket);
    IActivityContainer createStackOnDisplay(int displayId);
    int getFocusedStackId();
    void setTaskResizeable(int taskId, int resizeableMode);
    boolean requestAssistContextExtras(int requestType, in IResultReceiver receiver,
            in Bundle receiverExtras, in IBinder activityToken,
            boolean focused, boolean newSessionId);
    void resizeTask(int taskId, in Rect bounds, int resizeMode);
    int getLockTaskModeState();
    void setDumpHeapDebugLimit(in String processName, int uid, long maxMemSize,
            in String reportPackage);
    void dumpHeapFinished(in String path);
    void setVoiceKeepAwake(in IVoiceInteractionSession session, boolean keepAwake);
    void updateLockTaskPackages(int userId, in String[] packages);
    void noteAlarmStart(in IIntentSender sender, int sourceUid, in String tag);
    void noteAlarmFinish(in IIntentSender sender, int sourceUid, in String tag);
    int getPackageProcessState(in String packageName, in String callingPackage);
    oneway void showLockTaskEscapeMessage(in IBinder token);
    void updateDeviceOwner(in String packageName);
    /**
     * Notify the system that the keyguard is going away.
     *
     * @param flags See {@link android.view.WindowManagerPolicy#KEYGUARD_GOING_AWAY_FLAG_TO_SHADE}
     *              etc.
     */
    void keyguardGoingAway(int flags);
    int getUidProcessState(int uid, in String callingPackage);
    void registerUidObserver(in IUidObserver observer, int which, int cutpoint,
            String callingPackage);
    void unregisterUidObserver(in IUidObserver observer);
    boolean isAssistDataAllowedOnCurrentActivity();
    boolean showAssistFromActivity(in IBinder token, in Bundle args);
    boolean isRootVoiceInteraction(in IBinder token);


    // Start of N transactions
    // Start Binder transaction tracking for all applications.
    boolean startBinderTracking();
    // Stop Binder transaction tracking for all applications and dump trace data to the given file
    // descriptor.
    boolean stopBinderTrackingAndDump(in ParcelFileDescriptor fd);
    /**
     * Try to place task to provided position. The final position might be different depending on
     * current user and stacks state. The task will be moved to target stack if it's currently in
     * different stack.
     */
    void positionTaskInStack(int taskId, int stackId, int position);
    int getActivityStackId(in IBinder token);
    void exitFreeformMode(in IBinder token);
    void reportSizeConfigurations(in IBinder token, in int[] horizontalSizeConfiguration,
            in int[] verticalSizeConfigurations, in int[] smallestWidthConfigurations);
    boolean moveTaskToDockedStack(int taskId, int createMode, boolean toTop, boolean animate,
            in Rect initialBounds);
    void suppressResizeConfigChanges(boolean suppress);
    void moveTasksToFullscreenStack(int fromStackId, boolean onTop);
    boolean moveTopActivityToPinnedStack(int stackId, in Rect bounds);
    boolean isAppStartModeDisabled(int uid, in String packageName);
    boolean unlockUser(int userid, in byte[] token, in byte[] secret,
            in IProgressListener listener);
    boolean isInMultiWindowMode(in IBinder token);
    boolean isInPictureInPictureMode(in IBinder token);
    void killPackageDependents(in String packageName, int userId);
    boolean enterPictureInPictureMode(in IBinder token, in PictureInPictureParams params);
    void setPictureInPictureParams(in IBinder token, in PictureInPictureParams params);
    int getMaxNumPictureInPictureActions(in IBinder token);
    void activityRelaunched(in IBinder token);
    IBinder getUriPermissionOwnerForActivity(in IBinder activityToken);
    /**
     * Resizes the docked stack, and all other stacks as the result of the dock stack bounds change.
     *
     * @param dockedBounds The bounds for the docked stack.
     * @param tempDockedTaskBounds The temporary bounds for the tasks in the docked stack, which
     *                             might be different from the stack bounds to allow more
     *                             flexibility while resizing, or {@code null} if they should be the
     *                             same as the stack bounds.
     * @param tempDockedTaskInsetBounds The temporary bounds for the tasks to calculate the insets.
     *                                  When resizing, we usually "freeze" the layout of a task. To
     *                                  achieve that, we also need to "freeze" the insets, which
     *                                  gets achieved by changing task bounds but not bounds used
     *                                  to calculate the insets in this transient state
     * @param tempOtherTaskBounds The temporary bounds for the tasks in all other stacks, or
     *                            {@code null} if they should be the same as the stack bounds.
     * @param tempOtherTaskInsetBounds Like {@code tempDockedTaskInsetBounds}, but for the other
     *                                 stacks.
     * @throws RemoteException
     */
    void resizeDockedStack(in Rect dockedBounds, in Rect tempDockedTaskBounds,
            in Rect tempDockedTaskInsetBounds,
            in Rect tempOtherTaskBounds, in Rect tempOtherTaskInsetBounds);
    int setVrMode(in IBinder token, boolean enabled, in ComponentName packageName);
    // Gets the URI permissions granted to an arbitrary package.
    // NOTE: this is different from getPersistedUriPermissions(), which returns the URIs the package
    // granted to another packages (instead of those granted to it).
    ParceledListSlice getGrantedUriPermissions(in String packageName, int userId);
    // Clears the URI permissions granted to an arbitrary package.
    void clearGrantedUriPermissions(in String packageName, int userId);
    boolean isAppForeground(int uid);
    void startLocalVoiceInteraction(in IBinder token, in Bundle options);
    void stopLocalVoiceInteraction(in IBinder token);
    boolean supportsLocalVoiceInteraction();
    void notifyPinnedStackAnimationStarted();
    void notifyPinnedStackAnimationEnded();
    void removeStack(int stackId);
    void makePackageIdle(String packageName, int userId);
    int getMemoryTrimLevel();
    /**
     * Resizes the pinned stack.
     *
     * @param pinnedBounds The bounds for the pinned stack.
     * @param tempPinnedTaskBounds The temporary bounds for the tasks in the pinned stack, which
     *                             might be different from the stack bounds to allow more
     *                             flexibility while resizing, or {@code null} if they should be the
     *                             same as the stack bounds.
     */
    void resizePinnedStack(in Rect pinnedBounds, in Rect tempPinnedTaskBounds);
    boolean isVrModePackageEnabled(in ComponentName packageName);
    /**
     * Moves all tasks from the docked stack in the fullscreen stack and puts the top task of the
     * fullscreen stack into the docked stack.
     */
    void swapDockedAndFullscreenStack();
    void notifyLockedProfile(int userId);
    void startConfirmDeviceCredentialIntent(in Intent intent, in Bundle options);
    void sendIdleJobTrigger();
    int sendIntentSender(in IIntentSender target, in IBinder whitelistToken, int code,
            in Intent intent, in String resolvedType, in IIntentReceiver finishedReceiver,
            in String requiredPermission, in Bundle options);


    // Start of N MR1 transactions
    void setVrThread(int tid);
    void setRenderThread(int tid);
    /**
     * Lets activity manager know whether the calling process is currently showing "top-level" UI
     * that is not an activity, i.e. windows on the screen the user is currently interacting with.
     *
     * <p>This flag can only be set for persistent processes.
     *
     * @param hasTopUi Whether the calling process has "top-level" UI.
     */
    void setHasTopUi(boolean hasTopUi);

    // Start of O transactions
    void requestActivityRelaunch(in IBinder token);
    /**
     * Updates override configuration applied to specific display.
     * @param values Update values for display configuration. If null is passed it will request the
     *               Window Manager to compute new config for the specified display.
     * @param displayId Id of the display to apply the config to.
     * @throws RemoteException
     * @return Returns true if the configuration was updated.
     */
    boolean updateDisplayOverrideConfiguration(in Configuration values, int displayId);
    void unregisterTaskStackListener(ITaskStackListener listener);
    void moveStackToDisplay(int stackId, int displayId);
    boolean requestAutofillData(in IResultReceiver receiver, in Bundle receiverExtras,
                                in IBinder activityToken, int flags);
    void dismissKeyguard(in IBinder token, in IKeyguardDismissCallback callback);
    int restartUserInBackground(int userId);

    /** Cancels the window transitions for the given task. */
    void cancelTaskWindowTransition(int taskId);

    /** Cancels the thumbnail transitions for the given task. */
    void cancelTaskThumbnailTransition(int taskId);

    /**
     * @param taskId the id of the task to retrieve the sAutoapshots for
     * @param reducedResolution if set, if the snapshot needs to be loaded from disk, this will load
     *                          a reduced resolution of it, which is much faster
     * @return a graphic buffer representing a screenshot of a task
     */
    ActivityManager.TaskSnapshot getTaskSnapshot(int taskId, boolean reducedResolution);

    void scheduleApplicationInfoChanged(in List<String> packageNames, int userId);
    void setPersistentVrThread(int tid);

    void waitForNetworkStateUpdate(long procStateSeq);

    /**
     * See {@link android.app.Activity#setDisablePreviewScreenshots}
     */
    void setDisablePreviewScreenshots(IBinder token, boolean disable);

    /**
     * Return the user id of last resumed activity.
     */
    int getLastResumedActivityUserId();

    /**
     * Add a bare uid to the background restrictions whitelist.  Only the system uid may call this.
     */
     void backgroundWhitelistUid(int uid);

    // WARNING: when these transactions are updated, check if they are any callers on the native
    // side. If so, make sure they are using the correct transaction ids and arguments.
    // If a transaction which will also be used on the native side is being inserted, add it
    // alongside with other transactions of this kind at the top of this file.
}