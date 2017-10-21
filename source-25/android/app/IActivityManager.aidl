/*
 * Copyright (C) 2006 The Android Open Source Project
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
import android.app.ActivityManager.RunningServiceInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.ActivityManager.StackInfo;
import android.app.ActivityManagerNative;
import android.app.ActivityOptions;
import android.app.ApplicationErrorReport;
import android.app.IActivityContainer;
import android.app.IActivityContainerCallback;
import android.app.IActivityController;
import android.app.IActivityManager;
import android.app.IActivityManager.ContentProviderHolder;
import android.app.IActivityManager.WaitResult;
import android.app.IAppTask;
import android.app.IApplicationThread;
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
import android.app.ProfilerInfo;
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
import android.content.pm.UserInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Debug;
import android.os.IBinder;
import android.os.IProgressListener;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
import android.os.PersistableBundle;
import android.os.RemoteException;
import android.os.StrictMode;
import android.service.voice.IVoiceInteractionSession;
import com.android.internal.app.IVoiceInteractor;
import com.android.internal.os.IResultReceiver;
import java.util.List;

/**
 * System private API for talking with the activity manager service.  This
 * provides calls from the application back to the activity manager.
 *
 * {@hide}
 */
interface IActivityManager {
    int startActivity(IApplicationThread caller, String callingPackage, Intent intent,
            String resolvedType, IBinder resultTo, String resultWho, int requestCode, int flags,
            ProfilerInfo profilerInfo, Bundle options);
    int startActivityAsUser(IApplicationThread caller, String callingPackage, Intent intent,
            String resolvedType, IBinder resultTo, String resultWho, int requestCode, int flags,
            ProfilerInfo profilerInfo, Bundle options, int userId);
    int startActivityAsCaller(IApplicationThread caller, String callingPackage,
            Intent intent, String resolvedType, IBinder resultTo, String resultWho, int requestCode,
            int flags, ProfilerInfo profilerInfo, Bundle options, boolean ignoreTargetSecurity,
            int userId);
    WaitResult startActivityAndWait(IApplicationThread caller, String callingPackage,
            Intent intent, String resolvedType, IBinder resultTo, String resultWho,
            int requestCode, int flags, ProfilerInfo profilerInfo, Bundle options,
            int userId);
    int startActivityWithConfig(IApplicationThread caller, String callingPackage,
            Intent intent, String resolvedType, IBinder resultTo, String resultWho,
            int requestCode, int startFlags, Configuration newConfig,
            Bundle options, int userId);
    int startActivityIntentSender(IApplicationThread caller,
            IntentSender intent, Intent fillInIntent, String resolvedType,
            IBinder resultTo, String resultWho, int requestCode,
            int flagsMask, int flagsValues, Bundle options);
    int startVoiceActivity(String callingPackage, int callingPid, int callingUid,
            Intent intent, String resolvedType, IVoiceInteractionSession session,
            IVoiceInteractor interactor, int flags, ProfilerInfo profilerInfo, Bundle options,
            int userId);
    boolean startNextMatchingActivity(IBinder callingActivity,
            Intent intent, Bundle options);
    int startActivityFromRecents(int taskId, Bundle options);
    boolean finishActivity(IBinder token, int code, Intent data, int finishTask);
    void finishSubActivity(IBinder token, String resultWho, int requestCode);
    boolean finishActivityAffinity(IBinder token);
    void finishVoiceTask(IVoiceInteractionSession session);
    boolean releaseActivityInstance(IBinder token);
    void releaseSomeActivities(IApplicationThread app);
    boolean willActivityBeVisible(IBinder token);
    Intent registerReceiver(IApplicationThread caller, String callerPackage,
            IIntentReceiver receiver, IntentFilter filter,
            String requiredPermission, int userId);
    void unregisterReceiver(IIntentReceiver receiver);
    int broadcastIntent(IApplicationThread caller, Intent intent,
            String resolvedType, IIntentReceiver resultTo, int resultCode,
            String resultData, Bundle map, String[] requiredPermissions,
            int appOp, Bundle options, boolean serialized, boolean sticky, int userId);
    void unbroadcastIntent(IApplicationThread caller, Intent intent, int userId);
    void finishReceiver(IBinder who, int resultCode, String resultData, Bundle map,
            boolean abortBroadcast, int flags);
    void attachApplication(IApplicationThread app);
    void activityResumed(IBinder token);
    void activityIdle(IBinder token, Configuration config,
            boolean stopProfiling);
    void activityPaused(IBinder token);
    void activityStopped(IBinder token, Bundle state,
            PersistableBundle persistentState, CharSequence description);
    void activitySlept(IBinder token);
    void activityDestroyed(IBinder token);
    void activityRelaunched(IBinder token);
    void reportSizeConfigurations(IBinder token, int[] horizontalSizeConfiguration,
            int[] verticalSizeConfigurations, int[] smallestWidthConfigurations);
    String getCallingPackage(IBinder token);
    ComponentName getCallingActivity(IBinder token);
    List<IAppTask> getAppTasks(String callingPackage);
    int addAppTask(IBinder activityToken, Intent intent,
            ActivityManager.TaskDescription description, Bitmap thumbnail);
    Point getAppTaskThumbnailSize();
    List<RunningTaskInfo> getTasks(int maxNum, int flags);
    ParceledListSlice getRecentTasks(int maxNum,
            int flags, int userId);
    ActivityManager.TaskThumbnail getTaskThumbnail(int taskId);
    List<RunningServiceInfo> getServices(int maxNum, int flags);
    List<ActivityManager.ProcessErrorStateInfo> getProcessesInErrorState();
    void moveTaskToFront(int task, int flags, Bundle options);
    boolean moveActivityTaskToBack(IBinder token, boolean nonRoot);
    void moveTaskBackwards(int task);
    void moveTaskToStack(int taskId, int stackId, boolean toTop);
    boolean moveTaskToDockedStack(int taskId, int createMode, boolean toTop, boolean animate,
            Rect initialBounds, boolean moveHomeStackFront);
    boolean moveTopActivityToPinnedStack(int stackId, Rect bounds);

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
    void resizeStack(int stackId, Rect bounds, boolean allowResizeInDockedMode,
            boolean preserveWindows, boolean animate, int animationDuration);

    /**
     * Moves all tasks from the docked stack in the fullscreen stack and puts the top task of the
     * fullscreen stack into the docked stack.
     */
    void swapDockedAndFullscreenStack();

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
    void resizeDockedStack(Rect dockedBounds, Rect tempDockedTaskBounds,
            Rect tempDockedTaskInsetBounds,
            Rect tempOtherTaskBounds, Rect tempOtherTaskInsetBounds);
    /**
     * Resizes the pinned stack.
     *
     * @param pinnedBounds The bounds for the pinned stack.
     * @param tempPinnedTaskBounds The temporary bounds for the tasks in the pinned stack, which
     *                             might be different from the stack bounds to allow more
     *                             flexibility while resizing, or {@code null} if they should be the
     *                             same as the stack bounds.
     */
    void resizePinnedStack(Rect pinnedBounds, Rect tempPinnedTaskBounds);
    void positionTaskInStack(int taskId, int stackId, int position);
    List<StackInfo> getAllStackInfos();
    StackInfo getStackInfo(int stackId);
    boolean isInHomeStack(int taskId);
    void setFocusedStack(int stackId);
    int getFocusedStackId();
    void setFocusedTask(int taskId);
    void registerTaskStackListener(ITaskStackListener listener);
    int getTaskForActivity(IBinder token, boolean onlyRoot);
    ContentProviderHolder getContentProvider(IApplicationThread caller,
            String name, int userId, boolean stable);
    ContentProviderHolder getContentProviderExternal(String name, int userId, IBinder token);
    void removeContentProvider(IBinder connection, boolean stable);
    void removeContentProviderExternal(String name, IBinder token);
    void publishContentProviders(IApplicationThread caller,
            List<ContentProviderHolder> providers);
    boolean refContentProvider(IBinder connection, int stableDelta, int unstableDelta);
    void unstableProviderDied(IBinder connection);
    void appNotRespondingViaProvider(IBinder connection);
    PendingIntent getRunningServiceControlPanel(ComponentName service);
    ComponentName startService(IApplicationThread caller, Intent service,
            String resolvedType, String callingPackage, int userId);
    int stopService(IApplicationThread caller, Intent service,
            String resolvedType, int userId);
    boolean stopServiceToken(ComponentName className, IBinder token,
            int startId);
    void setServiceForeground(ComponentName className, IBinder token,
            int id, Notification notification, int flags);
    int bindService(IApplicationThread caller, IBinder token, Intent service,
            String resolvedType, IServiceConnection connection, int flags,
            String callingPackage, int userId);
    boolean unbindService(IServiceConnection connection);
    void publishService(IBinder token,
            Intent intent, IBinder service);
    void unbindFinished(IBinder token, Intent service,
            boolean doRebind);
    /* oneway */
    void serviceDoneExecuting(IBinder token, int type, int startId,
            int res);
    IBinder peekService(Intent service, String resolvedType, String callingPackage);

    boolean bindBackupAgent(String packageName, int backupRestoreMode, int userId);
    void clearPendingBackup();
    void backupAgentCreated(String packageName, IBinder agent);
    void unbindBackupAgent(ApplicationInfo appInfo);
    void killApplicationProcess(String processName, int uid);

    boolean startInstrumentation(ComponentName className, String profileFile,
            int flags, Bundle arguments, IInstrumentationWatcher watcher,
            IUiAutomationConnection connection, int userId,
            String abiOverride);
    void finishInstrumentation(IApplicationThread target,
            int resultCode, Bundle results);

    Configuration getConfiguration();
    void updateConfiguration(Configuration values);
    void setRequestedOrientation(IBinder token,
            int requestedOrientation);
    int getRequestedOrientation(IBinder token);

    ComponentName getActivityClassForToken(IBinder token);
    String getPackageForToken(IBinder token);

    IIntentSender getIntentSender(int type,
            String packageName, IBinder token, String resultWho,
            int requestCode, Intent[] intents, String[] resolvedTypes,
            int flags, Bundle options, int userId);
    void cancelIntentSender(IIntentSender sender);
    boolean clearApplicationUserData(String packageName,
            IPackageDataObserver observer, int userId);
    String getPackageForIntentSender(IIntentSender sender);
    int getUidForIntentSender(IIntentSender sender);

    int handleIncomingUser(int callingPid, int callingUid, int userId, boolean allowAll,
            boolean requireFull, String name, String callerPackage);

    void setProcessLimit(int max);
    int getProcessLimit();

    void setProcessForeground(IBinder token, int pid,
            boolean isForeground);

    int checkPermission(String permission, int pid, int uid);
    int checkPermissionWithToken(String permission, int pid, int uid, IBinder callerToken);

    int checkUriPermission(Uri uri, int pid, int uid, int mode, int userId,
            IBinder callerToken);
    void grantUriPermission(IApplicationThread caller, String targetPkg, Uri uri,
            int mode, int userId);
    void revokeUriPermission(IApplicationThread caller, Uri uri, int mode, int userId);
    void takePersistableUriPermission(Uri uri, int modeFlags, int userId);
    void releasePersistableUriPermission(Uri uri, int modeFlags, int userId);
    ParceledListSlice getPersistedUriPermissions(
            String packageName, boolean incoming);

    // Gets the URI permissions granted to an arbitrary package.
    // NOTE: this is different from getPersistedUriPermissions(), which returns the URIs the package
    // granted to another packages (instead of those granted to it).
    ParceledListSlice getGrantedUriPermissions(String packageName, int userId);

    // Clears the URI permissions granted to an arbitrary package.
    void clearGrantedUriPermissions(String packageName, int userId);

    void showWaitingForDebugger(IApplicationThread who, boolean waiting);

    void getMemoryInfo(ActivityManager.MemoryInfo outInfo);

    void killBackgroundProcesses(String packageName, int userId);
    void killAllBackgroundProcesses();
    void killPackageDependents(String packageName, int userId);
    void forceStopPackage(String packageName, int userId);

    // Note: probably don't want to allow applications access to these.
    void setLockScreenShown(boolean showing, boolean occluded);

    void unhandledBack();
    ParcelFileDescriptor openContentUri(Uri uri);
    void setDebugApp(
        String packageName, boolean waitForDebugger, boolean persistent);
    void setAlwaysFinish(boolean enabled);
    void setActivityController(IActivityController watcher, boolean imAMonkey);
    void setLenientBackgroundCheck(boolean enabled);
    int getMemoryTrimLevel();

    void enterSafeMode();

    void noteWakeupAlarm(IIntentSender sender, int sourceUid, String sourcePkg, String tag);
    void noteAlarmStart(IIntentSender sender, int sourceUid, String tag);
    void noteAlarmFinish(IIntentSender sender, int sourceUid, String tag);

    boolean killPids(int[] pids, String reason, boolean secure);
    boolean killProcessesBelowForeground(String reason);

    // Special low-level communication with activity manager.
    void handleApplicationCrash(IBinder app,
            ApplicationErrorReport.CrashInfo crashInfo);
    boolean handleApplicationWtf(IBinder app, String tag, boolean system,
            ApplicationErrorReport.CrashInfo crashInfo);

    // A StrictMode violation to be handled.  The violationMask is a
    // subset of the original StrictMode policy bitmask, with only the
    // bit violated and penalty bits to be executed by the
    // ActivityManagerService remaining set.
    void handleApplicationStrictModeViolation(IBinder app, int violationMask,
            StrictMode.ViolationInfo crashInfo);

    /*
     * This will deliver the specified signal to all the persistent processes. Currently only
     * SIGUSR1 is delivered. All others are ignored.
     */
    void signalPersistentProcesses(int signal);
    // Retrieve running application processes in the system
    List<ActivityManager.RunningAppProcessInfo> getRunningAppProcesses();
    // Retrieve info of applications installed on external media that are currently
    // running.
    List<ApplicationInfo> getRunningExternalApplications();
    // Get memory information about the calling process.
    void getMyMemoryState(ActivityManager.RunningAppProcessInfo outInfo);
    // Get device configuration
    ConfigurationInfo getDeviceConfigurationInfo();

    // Turn on/off profiling in a particular process.
    boolean profileControl(String process, int userId, boolean start,
            ProfilerInfo profilerInfo, int profileType);

    boolean shutdown(int timeout);

    void stopAppSwitches();
    void resumeAppSwitches();

    void addPackageDependency(String packageName);

    void killApplication(String pkg, int appId, int userId, String reason);

    void closeSystemDialogs(String reason);

    Debug.MemoryInfo[] getProcessMemoryInfo(int[] pids);

    void overridePendingTransition(IBinder token, String packageName,
            int enterAnim, int exitAnim);

    boolean isUserAMonkey();

    void setUserIsMonkey(boolean monkey);

    void finishHeavyWeightApp();

    boolean convertFromTranslucent(IBinder token);
    boolean convertToTranslucent(IBinder token, ActivityOptions options);
    void notifyActivityDrawn(IBinder token);
    ActivityOptions getActivityOptions(IBinder token);

    void bootAnimationComplete();

    void setImmersive(IBinder token, boolean immersive);
    boolean isImmersive(IBinder token);
    boolean isTopActivityImmersive();
    boolean isTopOfTask(IBinder token);

    void crashApplication(int uid, int initialPid, String packageName,
            String message);

    String getProviderMimeType(Uri uri, int userId);

    IBinder newUriPermissionOwner(String name);
    IBinder getUriPermissionOwnerForActivity(IBinder activityToken);
    void grantUriPermissionFromOwner(IBinder owner, int fromUid, String targetPkg,
            Uri uri, int mode, int sourceUserId, int targetUserId);
    void revokeUriPermissionFromOwner(IBinder owner, Uri uri,
            int mode, int userId);

    int checkGrantUriPermission(int callingUid, String targetPkg, Uri uri,
            int modeFlags, int userId);

    // Cause the specified process to dump the specified heap.
    boolean dumpHeap(String process, int userId, boolean managed, String path,
        ParcelFileDescriptor fd);

    int startActivities(IApplicationThread caller, String callingPackage,
            Intent[] intents, String[] resolvedTypes, IBinder resultTo,
            Bundle options, int userId);

    int getFrontActivityScreenCompatMode();
    void setFrontActivityScreenCompatMode(int mode);
    int getPackageScreenCompatMode(String packageName);
    void setPackageScreenCompatMode(String packageName, int mode);
    boolean getPackageAskScreenCompat(String packageName);
    void setPackageAskScreenCompat(String packageName, boolean ask);

    // Multi-user APIs
    boolean switchUser(int userid);
    boolean startUserInBackground(int userid);
    boolean unlockUser(int userid, byte[] token, byte[] secret, IProgressListener listener);
    int stopUser(int userid, boolean force, IStopUserCallback callback);
    UserInfo getCurrentUser();
    boolean isUserRunning(int userid, int flags);
    int[] getRunningUserIds();

    boolean removeTask(int taskId);

    void registerProcessObserver(IProcessObserver observer);
    void unregisterProcessObserver(IProcessObserver observer);

    void registerUidObserver(IUidObserver observer, int which);
    void unregisterUidObserver(IUidObserver observer);

    boolean isIntentSenderTargetedToPackage(IIntentSender sender);

    boolean isIntentSenderAnActivity(IIntentSender sender);

    Intent getIntentForIntentSender(IIntentSender sender);

    String getTagForIntentSender(IIntentSender sender, String prefix);

    void updatePersistentConfiguration(Configuration values);

    long[] getProcessPss(int[] pids);

    void showBootMessage(CharSequence msg, boolean always);

    void keyguardWaitingForActivityDrawn();

    /**
     * Notify the system that the keyguard is going away.
     *
     * @param flags See {@link android.view.WindowManagerPolicy#KEYGUARD_GOING_AWAY_FLAG_TO_SHADE}
     *              etc.
     */
    void keyguardGoingAway(int flags);

    boolean shouldUpRecreateTask(IBinder token, String destAffinity);

    boolean navigateUpTo(IBinder token, Intent target, int resultCode, Intent resultData);

    // These are not because you need to be very careful in how you
    // manage your activity to make sure it is always the uid you expect.
    int getLaunchedFromUid(IBinder activityToken);
    String getLaunchedFromPackage(IBinder activityToken);

    void registerUserSwitchObserver(IUserSwitchObserver observer, String name);
    void unregisterUserSwitchObserver(IUserSwitchObserver observer);

    void requestBugReport(int bugreportType);

    long inputDispatchingTimedOut(int pid, boolean aboveSystem, String reason);

    Bundle getAssistContextExtras(int requestType);

    boolean requestAssistContextExtras(int requestType, IResultReceiver receiver,
            Bundle receiverExtras,
            IBinder activityToken, boolean focused, boolean newSessionId);

    void reportAssistContextExtras(IBinder token, Bundle extras,
            AssistStructure structure, AssistContent content, Uri referrer);

    boolean launchAssistIntent(Intent intent, int requestType, String hint, int userHandle,
            Bundle args);

    boolean isAssistDataAllowedOnCurrentActivity();

    boolean showAssistFromActivity(IBinder token, Bundle args);

    void killUid(int appId, int userId, String reason);

    void hang(IBinder who, boolean allowRestart);

    void reportActivityFullyDrawn(IBinder token);

    void restart();

    void performIdleMaintenance();

    void sendIdleJobTrigger();

    IActivityContainer createVirtualActivityContainer(IBinder parentActivityToken,
            IActivityContainerCallback callback);

    IActivityContainer createStackOnDisplay(int displayId);

    void deleteActivityContainer(IActivityContainer container);

    int getActivityDisplayId(IBinder activityToken);

    void startSystemLockTaskMode(int taskId);

    void startLockTaskMode(int taskId);

    //void startLockTaskMode(IBinder token);

    void stopLockTaskMode();

    void stopSystemLockTaskMode();

    boolean isInLockTaskMode();

    int getLockTaskModeState();

    void showLockTaskEscapeMessage(IBinder token);

    void setTaskDescription(IBinder token, ActivityManager.TaskDescription values);
    void setTaskResizeable(int taskId, int resizeableMode);
    void resizeTask(int taskId, Rect bounds, int resizeMode);

    Rect getTaskBounds(int taskId);
    Bitmap getTaskDescriptionIcon(String filename, int userId);

    void startInPlaceAnimationOnFrontMostApplication(ActivityOptions opts);

    boolean requestVisibleBehind(IBinder token, boolean visible);
    boolean isBackgroundVisibleBehind(IBinder token);
    void backgroundResourcesReleased(IBinder token);

    void notifyLaunchTaskBehindComplete(IBinder token);
    void notifyEnterAnimationComplete(IBinder token);

    void notifyCleartextNetwork(int uid, byte[] firstPacket);

    void setDumpHeapDebugLimit(String processName, int uid, long maxMemSize,
            String reportPackage);
    void dumpHeapFinished(String path);

    void setVoiceKeepAwake(IVoiceInteractionSession session, boolean keepAwake);
    void updateLockTaskPackages(int userId, String[] packages);
    void updateDeviceOwner(String packageName);

    int getPackageProcessState(String packageName, String callingPackage);

    boolean setProcessMemoryTrimLevel(String process, int uid, int level);

    boolean isRootVoiceInteraction(IBinder token);

    // Start Binder transaction tracking for all applications.
    boolean startBinderTracking();

    // Stop Binder transaction tracking for all applications and dump trace data to the given file
    // descriptor.
    boolean stopBinderTrackingAndDump(ParcelFileDescriptor fd);

    int getActivityStackId(IBinder token);
    void exitFreeformMode(IBinder token);

    void suppressResizeConfigChanges(boolean suppress);

    void moveTasksToFullscreenStack(int fromStackId, boolean onTop);

    int getAppStartMode(int uid, String packageName);

    boolean isInMultiWindowMode(IBinder token);

    boolean isInPictureInPictureMode(IBinder token);

    void enterPictureInPictureMode(IBinder token);

    int setVrMode(IBinder token, boolean enabled, ComponentName packageName);

    boolean isVrModePackageEnabled(ComponentName packageName);

    boolean isAppForeground(int uid);

    void startLocalVoiceInteraction(IBinder token, Bundle options);

    void stopLocalVoiceInteraction(IBinder token);

    boolean supportsLocalVoiceInteraction();

    void notifyPinnedStackAnimationEnded();

    void removeStack(int stackId);

    void notifyLockedProfile(int userId);

    void startConfirmDeviceCredentialIntent(Intent intent);

    int sendIntentSender(IIntentSender target, int code, Intent intent, String resolvedType,
            IIntentReceiver finishedReceiver, String requiredPermission, Bundle options);

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

    /**
     * Returns if the target of the PendingIntent can be fired directly, without triggering
     * a work profile challenge. This can happen if the PendingIntent is to start direct-boot
     * aware activities, and the target user is in RUNNING_LOCKED state, i.e. we should allow
     * direct-boot aware activity to bypass work challenge when the user hasn't unlocked yet.
     * @param intent the {@link  PendingIntent} to be tested.
     * @return {@code true} if the intent should not trigger a work challenge, {@code false}
     *     otherwise.
     * @throws RemoteException
     */
    boolean canBypassWorkChallenge(PendingIntent intent);

    public static class WaitResult implements Parcelable {
        protected WaitResult(Parcel in) {
            throw new RuntimeException("Stub!");
        }

        public static Creator<WaitResult> CREATOR = new Creator<WaitResult>() {
            @Override
            public WaitResult createFromParcel(Parcel in) {
                throw new RuntimeException("Stub!");
            }

            @Override
            public WaitResult[] newArray(int size) {
                throw new RuntimeException("Stub!");
            }
        };

        @Override
        public int describeContents() {
            throw new RuntimeException("Stub!");
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            throw new RuntimeException("Stub!");
        }
    }

    public static class ContentProviderHolder implements Parcelable {

        protected ContentProviderHolder(Parcel in) {
            throw new RuntimeException("Stub!");
        }

        public static Creator<ContentProviderHolder> CREATOR = new Creator<ContentProviderHolder>() {
            @Override
            public ContentProviderHolder createFromParcel(Parcel in) {
                throw new RuntimeException("Stub!");
            }

            @Override
            public ContentProviderHolder[] newArray(int size) {
                throw new RuntimeException("Stub!");
            }
        };

        @Override
        public int describeContents() {
            throw new RuntimeException("Stub!");
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            throw new RuntimeException("Stub!");
        }
    }
}