/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except compliance with the License.
 * You may obtaa copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to writing, software
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

    // Since these transactions are also called from native code, these must be kept sync with
    // the ones frameworks/native/include/binder/IActivityManager.h
    // =============== Beginning of transactions used on native side as well ======================
    ParcelFileDescriptor openContentUri(String uriString);
    // =============== End of transactions used on native side as well ============================

    // Special low-level communication with activity manager.
    void handleApplicationCrash(IBinder app,
            ApplicationErrorReport.ParcelableCrashInfo crashInfo);
    int startActivity(IApplicationThread caller, String callingPackage, Intent intent,
            String resolvedType, IBinder resultTo, String resultWho, int requestCode,
            int flags, ProfilerInfo profilerInfo, Bundle options);
    void unhandledBack();

    boolean finishActivity(IBinder token, int code, Intent data, int finishTask);
    Intent registerReceiver(IApplicationThread caller, String callerPackage,
            IIntentReceiver receiver, IntentFilter filter,
            String requiredPermission, int userId, int flags);
    void unregisterReceiver(IIntentReceiver receiver);
    int broadcastIntent(IApplicationThread caller, Intent intent,
            String resolvedType, IIntentReceiver resultTo, int resultCode,
            String resultData, Bundle map, String[] requiredPermissions,
            int appOp, Bundle options, boolean serialized, boolean sticky, int userId);
    void unbroadcastIntent(IApplicationThread caller, Intent intent, int userId);
    void finishReceiver(IBinder who, int resultCode, String resultData, Bundle map,
            boolean abortBroadcast, int flags);
    void attachApplication(IApplicationThread app);
    void activityIdle(IBinder token, Configuration config,
            boolean stopProfiling);
    void activityPaused(IBinder token);
    void activityStopped(IBinder token, Bundle state,
            PersistableBundle persistentState, CharSequence description);
    String getCallingPackage(IBinder token);
    ComponentName getCallingActivity(IBinder token);
    List<ActivityManager.RunningTaskInfo> getTasks(int maxNum, int flags);
    void moveTaskToFront(int task, int flags, Bundle options);
    void moveTaskBackwards(int task);
    int getTaskForActivity(IBinder token, boolean onlyRoot);
    ContentProviderHolder getContentProvider(IApplicationThread caller,
            String name, int userId, boolean stable);
    void publishContentProviders(IApplicationThread caller,
            List<ContentProviderHolder> providers);
    boolean refContentProvider(IBinder connection, int stableDelta, int unstableDelta);
    void finishSubActivity(IBinder token, String resultWho, int requestCode);
    PendingIntent getRunningServiceControlPanel(ComponentName service);
    ComponentName startService(IApplicationThread caller, Intent service,
            String resolvedType, boolean requireForeground, String callingPackage, int userId);
    int stopService(IApplicationThread caller, Intent service,
            String resolvedType, int userId);
    int bindService(IApplicationThread caller, IBinder token, Intent service,
            String resolvedType, IServiceConnection connection, int flags,
            String callingPackage, int userId);
    boolean unbindService(IServiceConnection connection);
    void publishService(IBinder token, Intent intent, IBinder service);
    void activityResumed(IBinder token);
    void setDebugApp(String packageName, boolean waitForDebugger, boolean persistent);
    void setAlwaysFinish(boolean enabled);
    boolean startInstrumentation(ComponentName className, String profileFile,
            int flags, Bundle arguments, IInstrumentationWatcher watcher,
            IUiAutomationConnection connection, int userId,
            String abiOverride);
    void addInstrumentationResults(IApplicationThread target, Bundle results);
    void finishInstrumentation(IApplicationThread target, int resultCode,
            Bundle results);
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
    boolean updateConfiguration(Configuration values);
    boolean stopServiceToken(ComponentName className, IBinder token, int startId);
    ComponentName getActivityClassForToken(IBinder token);
    String getPackageForToken(IBinder token);
    void setProcessLimit(int max);
    int getProcessLimit();
    int checkPermission(String permission, int pid, int uid);
    int checkUriPermission(Uri uri, int pid, int uid, int mode, int userId,
            IBinder callerToken);
    void grantUriPermission(IApplicationThread caller, String targetPkg, Uri uri,
            int mode, int userId);
    void revokeUriPermission(IApplicationThread caller, String targetPkg, Uri uri,
            int mode, int userId);
    void setActivityController(IActivityController watcher, boolean imAMonkey);
    void showWaitingForDebugger(IApplicationThread who, boolean waiting);
    /*
     * This will deliver the specified signal to all the persistent processes. Currently only
     * SIGUSR1 is delivered. All others are ignored.
     */
    void signalPersistentProcesses(int signal);
    ParceledListSlice<ActivityManager.RecentTaskInfo> getRecentTasks(int maxNum,
            int flags, int userId);
    void serviceDoneExecuting(IBinder token, int type, int startId, int res);
    void activityDestroyed(IBinder token);
    IIntentSender getIntentSender(int type, String packageName, IBinder token,
            String resultWho, int requestCode, Intent[] intents, String[] resolvedTypes,
            int flags, Bundle options, int userId);
    void cancelIntentSender(IIntentSender sender);
    String getPackageForIntentSender(IIntentSender sender);
    void registerIntentSenderCancelListener(IIntentSender sender, IResultReceiver receiver);
    void unregisterIntentSenderCancelListener(IIntentSender sender, IResultReceiver receiver);
    void enterSafeMode();
    boolean startNextMatchingActivity(IBinder callingActivity,
            Intent intent, Bundle options);
    void noteWakeupAlarm(IIntentSender sender, int sourceUid,
            String sourcePkg, String tag);
    void removeContentProvider(IBinder connection, boolean stable);
    void setRequestedOrientation(IBinder token, int requestedOrientation);
    int getRequestedOrientation(IBinder token);
    void unbindFinished(IBinder token, Intent service, boolean doRebind);
    void setProcessImportant(IBinder token, int pid, boolean isForeground, String reason);
    void setServiceForeground(ComponentName className, IBinder token,
            int id, Notification notification, int flags);
    boolean moveActivityTaskToBack(IBinder token, boolean nonRoot);
    void getMemoryInfo(ActivityManager.MemoryInfo outInfo);
    List<ActivityManager.ProcessErrorStateInfo> getProcessesInErrorState();
    boolean clearApplicationUserData(String packageName,
            IPackageDataObserver observer, int userId);
    void forceStopPackage(String packageName, int userId);
    boolean killPids(int[] pids, String reason, boolean secure);
    List<ActivityManager.RunningServiceInfo> getServices(int maxNum, int flags);
    ActivityManager.TaskThumbnail getTaskThumbnail(int taskId);
    ActivityManager.TaskDescription getTaskDescription(int taskId);
    // Retrieve running application processes the system
    List<ActivityManager.RunningAppProcessInfo> getRunningAppProcesses();
    // Get device configuration
    ConfigurationInfo getDeviceConfigurationInfo();
    IBinder peekService(Intent service, String resolvedType, String callingPackage);
    // Turn on/off profiling a particular process.
    boolean profileControl(String process, int userId, boolean start,
            ProfilerInfo profilerInfo, int profileType);
    boolean shutdown(int timeout);
    void stopAppSwitches();
    void resumeAppSwitches();
    boolean bindBackupAgent(String packageName, int backupRestoreMode, int userId);
    void backupAgentCreated(String packageName, IBinder agent);
    void unbindBackupAgent(ApplicationInfo appInfo);
    int getUidForIntentSender(IIntentSender sender);
    int handleIncomingUser(int callingPid, int callingUid, int userId, boolean allowAll,
            boolean requireFull, String name, String callerPackage);
    void addPackageDependency(String packageName);
    void killApplication(String pkg, int appId, int userId, String reason);
    void closeSystemDialogs(String reason);
    Debug.MemoryInfo[] getProcessMemoryInfo(int[] pids);
    void killApplicationProcess(String processName, int uid);
    int startActivityIntentSender(IApplicationThread caller,
            IIntentSender target, IBinder whitelistToken, Intent fillInIntent,
            String resolvedType, IBinder resultTo, String resultWho, int requestCode,
            int flagsMask, int flagsValues, Bundle options);
    void overridePendingTransition(IBinder token, String packageName,
            int enterAnim, int exitAnim);
    // Special low-level communication with activity manager.
    boolean handleApplicationWtf(IBinder app, String tag, boolean system,
            ApplicationErrorReport.ParcelableCrashInfo crashInfo);
    void killBackgroundProcesses(String packageName, int userId);
    boolean isUserAMonkey();
    WaitResult startActivityAndWait(IApplicationThread caller, String callingPackage,
            Intent intent, String resolvedType, IBinder resultTo, String resultWho,
            int requestCode, int flags, ProfilerInfo profilerInfo, Bundle options,
            int userId);
    boolean willActivityBeVisible(IBinder token);
    int startActivityWithConfig(IApplicationThread caller, String callingPackage,
            Intent intent, String resolvedType, IBinder resultTo, String resultWho,
            int requestCode, int startFlags, Configuration newConfig,
            Bundle options, int userId);
    // Retrieve info of applications installed on external media that are currently
    // running.
    List<ApplicationInfo> getRunningExternalApplications();
    void finishHeavyWeightApp();
    // A StrictMode violation to be handled.  The violationMask is a
    // subset of the original StrictMode policy bitmask, with only the
    // bit violated and penalty bits to be executed by the
    // ActivityManagerService remaining set.
    void handleApplicationStrictModeViolation(IBinder app, int violationMask,
            StrictMode.ViolationInfo crashInfo);
    boolean isImmersive(IBinder token);
    void setImmersive(IBinder token, boolean immersive);
    boolean isTopActivityImmersive();
    void crashApplication(int uid, int initialPid, String packageName, int userId, String message);
    String getProviderMimeType(Uri uri, int userId);
    IBinder newUriPermissionOwner(String name);
    void grantUriPermissionFromOwner(IBinder owner, int fromUid, String targetPkg,
            Uri uri, int mode, int sourceUserId, int targetUserId);
    void revokeUriPermissionFromOwner(IBinder owner, Uri uri, int mode, int userId);
    int checkGrantUriPermission(int callingUid, String targetPkg, Uri uri,
            int modeFlags, int userId);
    // Cause the specified process to dump the specified heap.
    boolean dumpHeap(String process, int userId, boolean managed, boolean mallocInfo,
            boolean runGc, String path, ParcelFileDescriptor fd);
    int startActivities(IApplicationThread caller, String callingPackage,
            Intent[] intents, String[] resolvedTypes, IBinder resultTo,
            Bundle options, int userId);
    boolean isUserRunning(int userid, int flags);
    void activitySlept(IBinder token);
    int getFrontActivityScreenCompatMode();
    void setFrontActivityScreenCompatMode(int mode);
    int getPackageScreenCompatMode(String packageName);
    void setPackageScreenCompatMode(String packageName, int mode);
    boolean getPackageAskScreenCompat(String packageName);
    void setPackageAskScreenCompat(String packageName, boolean ask);
    boolean switchUser(int userid);
    void setFocusedTask(int taskId);
    boolean removeTask(int taskId);
    void registerProcessObserver(IProcessObserver observer);
    void unregisterProcessObserver(IProcessObserver observer);
    boolean isIntentSenderTargetedToPackage(IIntentSender sender);
    void updatePersistentConfiguration(Configuration values);
    long[] getProcessPss(int[] pids);
    void showBootMessage(CharSequence msg, boolean always);
    void killAllBackgroundProcesses();
    ContentProviderHolder getContentProviderExternal(String name, int userId,
            IBinder token);
    void removeContentProviderExternal(String name, IBinder token);
    // Get memory information abthe calling process.
    void getMyMemoryState(ActivityManager.RunningAppProcessInfo outInfo);
    boolean killProcessesBelowForeground(String reason);
    UserInfo getCurrentUser();
    boolean shouldUpRecreateTask(IBinder token, String destAffinity);
    boolean navigateUpTo(IBinder token, Intent target, int resultCode,
            Intent resultData);
    /**
     * Informs ActivityManagerService that the keyguard is showing.
     *
     * @param showing True if the keyguard is showing, false otherwise.
     * @param secondaryDisplayShowing The displayId of the secondary display on which the keyguard
     *        is showing, or INVALID_DISPLAY if there is no such display. Only meaningful if
     *        showing is true.
     */
    void setLockScreenShown(boolean showing, int secondaryDisplayShowing);
    boolean finishActivityAffinity(IBinder token);
    // This is not public because you need to be very careful how you
    // manage your activity to make sure it is always the uid you expect.
    int getLaunchedFromUid(IBinder activityToken);
    void unstableProviderDied(IBinder connection);
    boolean isIntentSenderAnActivity(IIntentSender sender);
    int startActivityAsUser(IApplicationThread caller, String callingPackage,
            Intent intent, String resolvedType, IBinder resultTo, String resultWho,
            int requestCode, int flags, ProfilerInfo profilerInfo,
            Bundle options, int userId);
    int stopUser(int userid, boolean force, IStopUserCallback callback);
    void registerUserSwitchObserver(IUserSwitchObserver observer, String name);
    void unregisterUserSwitchObserver(IUserSwitchObserver observer);
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
    void requestTelephonyBugReport(String shareTitle, String shareDescription);

    long inputDispatchingTimedOut(int pid, boolean aboveSystem, String reason);
    void clearPendingBackup();
    Intent getIntentForIntentSender(IIntentSender sender);
    Bundle getAssistContextExtras(int requestType);
    void reportAssistContextExtras(IBinder token, Bundle extras,
            AssistStructure structure, AssistContent content, Uri referrer);
    // This is not public because you need to be very careful how you
    // manage your activity to make sure it is always the uid you expect.
    String getLaunchedFromPackage(IBinder activityToken);
    void killUid(int appId, int userId, String reason);
    void setUserIsMonkey(boolean monkey);
    void hang(IBinder who, boolean allowRestart);
    void moveTaskToStack(int taskId, int stackId, boolean toTop);
    /**
     * Resizes the input stack id to the given bounds.
     *
     * @param stackId Id of the stack to resize.
     * @param bounds Bounds to resize the stack to or {@code null} for fullscreen.
     * @param allowResizeInDockedMode True if the resize should be allowed when the docked stack is
     *                                active.
     * @param preserveWindows True if the windows of activities contained the stack should be
     *                        preserved.
     * @param animate True if the stack resize should be animated.
     * @param animationDuration The duration of the resize animation milliseconds or -1 if the
     *                          default animation duration should be used.
     * @throws RemoteException
     */
    void resizeStack(int stackId, Rect bounds, boolean allowResizeInDockedMode,
            boolean preserveWindows, boolean animate, int animationDuration);
    List<ActivityManager.StackInfo> getAllStackInfos();
    void setFocusedStack(int stackId);
    ActivityManager.StackInfo getStackInfo(int stackId);
    boolean convertFromTranslucent(IBinder token);
    boolean convertToTranslucent(IBinder token, Bundle options);
    void notifyActivityDrawn(IBinder token);
    void reportActivityFullyDrawn(IBinder token, boolean restoredFromBundle);
    void restart();
    void performIdleMaintenance();
    void takePersistableUriPermission(Uri uri, int modeFlags, int userId);
    void releasePersistableUriPermission(Uri uri, int modeFlags, int userId);
    ParceledListSlice<android.content.UriPermission> getPersistedUriPermissions(String packageName, boolean incoming);
    void appNotRespondingViaProvider(IBinder connection);
    Rect getTaskBounds(int taskId);
    int getActivityDisplayId(IBinder activityToken);
    boolean setProcessMemoryTrimLevel(String process, int uid, int level);


    // Start of L transactions
    String getTagForIntentSender(IIntentSender sender, String prefix);
    boolean startUserInBackground(int userid);
    void startLockTaskModeById(int taskId);
    void startLockTaskModeByToken(IBinder token);
    void stopLockTaskMode();
    boolean isInLockTaskMode();
    void setTaskDescription(IBinder token, ActivityManager.TaskDescription values);
    int startVoiceActivity(String callingPackage, int callingPid, int callingUid,
            Intent intent, String resolvedType, IVoiceInteractionSession session,
            IVoiceInteractor interactor, int flags, ProfilerInfo profilerInfo,
            Bundle options, int userId);
    int startAssistantActivity(String callingPackage, int callingPid, int callingUid,
            Intent intent, String resolvedType, Bundle options, int userId);
    Bundle getActivityOptions(IBinder token);
    List<IBinder> getAppTasks(String callingPackage);
    void startSystemLockTaskMode(int taskId);
    void stopSystemLockTaskMode();
    void finishVoiceTask(IVoiceInteractionSession session);
    boolean isTopOfTask(IBinder token);
    void notifyLaunchTaskBehindComplete(IBinder token);
    int startActivityFromRecents(int taskId, Bundle options);
    void notifyEnterAnimationComplete(IBinder token);
    int startActivityAsCaller(IApplicationThread caller, String callingPackage,
            Intent intent, String resolvedType, IBinder resultTo, String resultWho,
            int requestCode, int flags, ProfilerInfo profilerInfo, Bundle options,
            boolean ignoreTargetSecurity, int userId);
    int addAppTask(IBinder activityToken, Intent intent,
            ActivityManager.TaskDescription description, Bitmap thumbnail);
    Point getAppTaskThumbnailSize();
    boolean releaseActivityInstance(IBinder token);
    void releaseSomeActivities(IApplicationThread app);
    void bootAnimationComplete();
    Bitmap getTaskDescriptionIcon(String filename, int userId);
    boolean launchAssistIntent(Intent intent, int requestType, String hint, int userHandle,
            Bundle args);
    void startInPlaceAnimationOnFrontMostApplication(Bundle opts);
    int checkPermissionWithToken(String permission, int pid, int uid,
            IBinder callerToken);
    void registerTaskStackListener(ITaskStackListener listener);


    // Start of M transactions
    void notifyCleartextNetwork(int uid, byte[] firstPacket);
    int createStackOnDisplay(int displayId);
    int getFocusedStackId();
    void setTaskResizeable(int taskId, int resizeableMode);
    boolean requestAssistContextExtras(int requestType, IResultReceiver receiver,
            Bundle receiverExtras, IBinder activityToken,
            boolean focused, boolean newSessionId);
    void resizeTask(int taskId, Rect bounds, int resizeMode);
    int getLockTaskModeState();
    void setDumpHeapDebugLimit(String processName, int uid, long maxMemSize,
            String reportPackage);
    void dumpHeapFinished(String path);
    void setVoiceKeepAwake(IVoiceInteractionSession session, boolean keepAwake);
    void updateLockTaskPackages(int userId, String[] packages);
    void noteAlarmStart(IIntentSender sender, int sourceUid, String tag);
    void noteAlarmFinish(IIntentSender sender, int sourceUid, String tag);
    int getPackageProcessState(String packageName, String callingPackage);
    void showLockTaskEscapeMessage(IBinder token);
    void updateDeviceOwner(String packageName);
    /**
     * Notify the system that the keyguard is going away.
     *
     * @param flags See {@link android.view.WindowManagerPolicy#KEYGUARD_GOING_AWAY_FLAG_TO_SHADE}
     *              etc.
     */
    void keyguardGoingAway(int flags);
    int getUidProcessState(int uid, String callingPackage);
    void registerUidObserver(IUidObserver observer, int which, int cutpoint,
            String callingPackage);
    void unregisterUidObserver(IUidObserver observer);
    boolean isAssistDataAllowedOnCurrentActivity();
    boolean showAssistFromActivity(IBinder token, Bundle args);
    boolean isRootVoiceInteraction(IBinder token);


    // Start of N transactions
    // Start Binder transaction tracking for all applications.
    boolean startBinderTracking();
    // Stop Binder transaction tracking for all applications and dump trace data to the given file
    // descriptor.
    boolean stopBinderTrackingAndDump(ParcelFileDescriptor fd);
    /**
     * Try to place task to provided position. The final position might be different depending on
     * current user and stacks state. The task will be moved to target stack if it's currently in
     * different stack.
     */
    void positionTaskInStack(int taskId, int stackId, int position);
    int getActivityStackId(IBinder token);
    void exitFreeformMode(IBinder token);
    void reportSizeConfigurations(IBinder token, int[] horizontalSizeConfiguration,
            int[] verticalSizeConfigurations, int[] smallestWidthConfigurations);
    boolean moveTaskToDockedStack(int taskId, int createMode, boolean toTop, boolean animate,
            Rect initialBounds);
    void suppressResizeConfigChanges(boolean suppress);
    void moveTasksToFullscreenStack(int fromStackId, boolean onTop);
    boolean moveTopActivityToPinnedStack(int stackId, Rect bounds);
    boolean isAppStartModeDisabled(int uid, String packageName);
    boolean unlockUser(int userid, byte[] token, byte[] secret,
            IProgressListener listener);
    boolean isInMultiWindowMode(IBinder token);
    boolean isInPictureInPictureMode(IBinder token);
    void killPackageDependents(String packageName, int userId);
    boolean enterPictureInPictureMode(IBinder token, PictureInPictureParams params);
    void setPictureInPictureParams(IBinder token, PictureInPictureParams params);
    int getMaxNumPictureInPictureActions(IBinder token);
    void activityRelaunched(IBinder token);
    IBinder getUriPermissionOwnerForActivity(IBinder activityToken);
    /**
     * Resizes the docked stack, and all other stacks as the result of the dock stack bounds change.
     *
     * @param dockedBounds The bounds for the docked stack.
     * @param tempDockedTaskBounds The temporary bounds for the tasks the docked stack, which
     *                             might be different from the stack bounds to allow more
     *                             flexibility while resizing, or {@code null} if they should be the
     *                             same as the stack bounds.
     * @param tempDockedTaskInsetBounds The temporary bounds for the tasks to calculate the insets.
     *                                  When resizing, we usually "freeze" the layof a task. To
     *                                  achieve that, we also need to "freeze" the insets, which
     *                                  gets achieved by changing task bounds but not bounds used
     *                                  to calculate the insets this transient state
     * @param tempOtherTaskBounds The temporary bounds for the tasks all other stacks, or
     *                            {@code null} if they should be the same as the stack bounds.
     * @param tempOtherTaskInsetBounds Like {@code tempDockedTaskInsetBounds}, but for the other
     *                                 stacks.
     * @throws RemoteException
     */
    void resizeDockedStack(Rect dockedBounds, Rect tempDockedTaskBounds,
            Rect tempDockedTaskInsetBounds,
            Rect tempOtherTaskBounds, Rect tempOtherTaskInsetBounds);
    int setVrMode(IBinder token, boolean enabled, ComponentName packageName);
    // Gets the URI permissions granted to an arbitrary package.
    // NOTE: this is different from getPersistedUriPermissions(), which returns the URIs the package
    // granted to another packages (instead of those granted to it).
    ParceledListSlice<android.content.UriPermission> getGrantedUriPermissions(String packageName, int userId);
    // Clears the URI permissions granted to an arbitrary package.
    void clearGrantedUriPermissions(String packageName, int userId);
    boolean isAppForeground(int uid);
    void startLocalVoiceInteraction(IBinder token, Bundle options);
    void stopLocalVoiceInteraction(IBinder token);
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
     * @param tempPinnedTaskBounds The temporary bounds for the tasks the pinned stack, which
     *                             might be different from the stack bounds to allow more
     *                             flexibility while resizing, or {@code null} if they should be the
     *                             same as the stack bounds.
     */
    void resizePinnedStack(Rect pinnedBounds, Rect tempPinnedTaskBounds);
    boolean isVrModePackageEnabled(ComponentName packageName);
    /**
     * Moves all tasks from the docked stack the fullscreen stack and puts the top task of the
     * fullscreen stack into the docked stack.
     */
    void swapDockedAndFullscreenStack();
    void notifyLockedProfile(int userId);
    void startConfirmDeviceCredentialIntent(Intent intent, Bundle options);
    void sendIdleJobTrigger();
    int sendIntentSender(IIntentSender target, IBinder whitelistToken, int code,
            Intent intent, String resolvedType, IIntentReceiver finishedReceiver,
            String requiredPermission, Bundle options);


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
    void requestActivityRelaunch(IBinder token);
    /**
     * Updates override configuration applied to specific display.
     * @param values Update values for display configuration. If null is passed it will request the
     *               Window Manager to compute new config for the specified display.
     * @param displayId Id of the display to apply the config to.
     * @throws RemoteException
     * @return Returns true if the configuration was updated.
     */
    boolean updateDisplayOverrideConfiguration(Configuration values, int displayId);
    void unregisterTaskStackListener(ITaskStackListener listener);
    void moveStackToDisplay(int stackId, int displayId);
    boolean requestAutofillData(IResultReceiver receiver, Bundle receiverExtras,
                                IBinder activityToken, int flags);
    void dismissKeyguard(IBinder token, IKeyguardDismissCallback callback);
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

    void scheduleApplicationInfoChanged(List<String> packageNames, int userId);
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

     void setShowWhenLocked(IBinder token, boolean showWhenLocked);
     void setTurnScreenOn(IBinder token, boolean turnScreenOn);
}
