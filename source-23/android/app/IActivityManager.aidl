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
import android.content.IContentProvider;
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
    boolean finishActivity(IBinder token, int code, Intent data, boolean finishTask);
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
    String getCallingPackage(IBinder token);
    ComponentName getCallingActivity(IBinder token);
    List<IAppTask> getAppTasks(String callingPackage);
    int addAppTask(IBinder activityToken, Intent intent,
            ActivityManager.TaskDescription description, Bitmap thumbnail);
    Point getAppTaskThumbnailSize();
    List<RunningTaskInfo> getTasks(int maxNum, int flags);
    List<ActivityManager.RecentTaskInfo> getRecentTasks(int maxNum,
            int flags, int userId);
    ActivityManager.TaskThumbnail getTaskThumbnail(int taskId);
    List<RunningServiceInfo> getServices(int maxNum, int flags);
    List<ActivityManager.ProcessErrorStateInfo> getProcessesInErrorState();
    void moveTaskToFront(int task, int flags, Bundle options);
    boolean moveActivityTaskToBack(IBinder token, boolean nonRoot);
    void moveTaskBackwards(int task);
    void moveTaskToStack(int taskId, int stackId, boolean toTop);
    void resizeStack(int stackId, Rect bounds);
    List<StackInfo> getAllStackInfos();
    StackInfo getStackInfo(int stackId);
    boolean isInHomeStack(int taskId);
    void setFocusedStack(int stackId);
    int getFocusedStackId();
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
            int id, Notification notification, boolean keepNotification);
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

    boolean bindBackupAgent(ApplicationInfo appInfo, int backupRestoreMode);
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
    ParceledListSlice<android.content.UriPermission> getPersistedUriPermissions(
            String packageName, boolean incoming);

    void showWaitingForDebugger(IApplicationThread who, boolean waiting);

    void getMemoryInfo(ActivityManager.MemoryInfo outInfo);

    void killBackgroundProcesses(String packageName, int userId);
    void killAllBackgroundProcesses();
    void forceStopPackage(String packageName, int userId);

    // Note: probably don't want to allow applications access to these.
    void setLockScreenShown(boolean shown);

    void unhandledBack();
    ParcelFileDescriptor openContentUri(Uri uri);
    void setDebugApp(
        String packageName, boolean waitForDebugger, boolean persistent)
       ;
    void setAlwaysFinish(boolean enabled);
    void setActivityController(IActivityController watcher)
       ;

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

    void killApplicationWithAppId(String pkg, int appid, String reason);

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
    int stopUser(int userid, IStopUserCallback callback);
    UserInfo getCurrentUser();
    boolean isUserRunning(int userid, boolean orStopping);
    int[] getRunningUserIds();

    boolean removeTask(int taskId);

    void registerProcessObserver(IProcessObserver observer);
    void unregisterProcessObserver(IProcessObserver observer);

    void registerUidObserver(IUidObserver observer);
    void unregisterUidObserver(IUidObserver observer);

    boolean isIntentSenderTargetedToPackage(IIntentSender sender);

    boolean isIntentSenderAnActivity(IIntentSender sender);

    Intent getIntentForIntentSender(IIntentSender sender);

    String getTagForIntentSender(IIntentSender sender, String prefix);

    void updatePersistentConfiguration(Configuration values);

    long[] getProcessPss(int[] pids);

    void showBootMessage(CharSequence msg, boolean always);

    void keyguardWaitingForActivityDrawn();

    void keyguardGoingAway(boolean disableWindowAnimations,
            boolean keyguardGoingToNotificationShade);

    boolean shouldUpRecreateTask(IBinder token, String destAffinity);

    boolean navigateUpTo(IBinder token, Intent target, int resultCode, Intent resultData);

    // These are not because you need to be very careful in how you
    // manage your activity to make sure it is always the uid you expect.
    int getLaunchedFromUid(IBinder activityToken);
    String getLaunchedFromPackage(IBinder activityToken);

    void registerUserSwitchObserver(IUserSwitchObserver observer);
    void unregisterUserSwitchObserver(IUserSwitchObserver observer);

    void requestBugReport();

    long inputDispatchingTimedOut(int pid, boolean aboveSystem, String reason);

    Bundle getAssistContextExtras(int requestType);

    boolean requestAssistContextExtras(int requestType, IResultReceiver receiver,
            IBinder activityToken);

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

    IActivityContainer createVirtualActivityContainer(IBinder parentActivityToken,
            IActivityContainerCallback callback);

    IActivityContainer createStackOnDisplay(int displayId);

    void deleteActivityContainer(IActivityContainer container);

    int getActivityDisplayId(IBinder activityToken);

    void startLockTaskModeOnCurrent();

    void startLockTaskMode(int taskId);

    //void startLockTaskMode(IBinder token);

    void stopLockTaskMode();

    void stopLockTaskModeOnCurrent();

    boolean isInLockTaskMode();

    int getLockTaskModeState();

    void showLockTaskEscapeMessage(IBinder token);

    void setTaskDescription(IBinder token, ActivityManager.TaskDescription values);
    void setTaskResizeable(int taskId, boolean resizeable);
    void resizeTask(int taskId, Rect bounds);
    Bitmap getTaskDescriptionIcon(String filename);

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

        public IContentProvider provider;

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