/*
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

package android.content.pm;

import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ContainerEncryptionParams;
import android.content.pm.FeatureInfo;
import android.content.pm.IPackageInstallObserver2;
import android.content.pm.IPackageInstaller;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.IPackageDeleteObserver2;
import android.content.pm.IPackageDataObserver;
import android.content.pm.IPackageMoveObserver;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.InstrumentationInfo;
import android.content.pm.KeySet;
import android.content.pm.PackageInfo;
import android.content.pm.ManifestDigest;
import android.content.pm.PackageCleanItem;
import android.content.pm.ParceledListSlice;
import android.content.pm.ProviderInfo;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.pm.UserInfo;
import android.content.pm.VerificationParams;
import android.content.pm.VerifierDeviceIdentity;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.content.IntentSender;

/**
 *  See {@link PackageManager} for documentation on most of the APIs
 *  here.
 * 
 *  {@hide}
 */
interface IPackageManager {
    boolean isPackageAvailable(String packageName, int userId);
    PackageInfo getPackageInfo(String packageName, int flags, int userId);
    int getPackageUid(String packageName, int userId);
    int[] getPackageGids(String packageName);
    
    String[] currentToCanonicalPackageNames(in String[] names);
    String[] canonicalToCurrentPackageNames(in String[] names);

    PermissionInfo getPermissionInfo(String name, int flags);
    
    List<PermissionInfo> queryPermissionsByGroup(String group, int flags);
    
    PermissionGroupInfo getPermissionGroupInfo(String name, int flags);
    
    List<PermissionGroupInfo> getAllPermissionGroups(int flags);
    
    ApplicationInfo getApplicationInfo(String packageName, int flags ,int userId);

    ActivityInfo getActivityInfo(in ComponentName className, int flags, int userId);

    boolean activitySupportsIntent(in ComponentName className, in Intent intent,
            String resolvedType);

    ActivityInfo getReceiverInfo(in ComponentName className, int flags, int userId);

    ServiceInfo getServiceInfo(in ComponentName className, int flags, int userId);

    ProviderInfo getProviderInfo(in ComponentName className, int flags, int userId);

    int checkPermission(String permName, String pkgName);
    
    int checkUidPermission(String permName, int uid);
    
    boolean addPermission(in PermissionInfo info);
    
    void removePermission(String name);

    void grantPermission(String packageName, String permissionName);

    void revokePermission(String packageName, String permissionName);

    boolean isProtectedBroadcast(String actionName);
    
    int checkSignatures(String pkg1, String pkg2);
    
    int checkUidSignatures(int uid1, int uid2);
    
    String[] getPackagesForUid(int uid);
    
    String getNameForUid(int uid);
    
    int getUidForSharedUser(String sharedUserName);

    int getFlagsForUid(int uid);

    boolean isUidPrivileged(int uid);

    String[] getAppOpPermissionPackages(String permissionName);

    ResolveInfo resolveIntent(in Intent intent, String resolvedType, int flags, int userId);

    boolean canForwardTo(in Intent intent, String resolvedType, int sourceUserId, int targetUserId);

    List<ResolveInfo> queryIntentActivities(in Intent intent, 
            String resolvedType, int flags, int userId);

    List<ResolveInfo> queryIntentActivityOptions(
            in ComponentName caller, in Intent[] specifics,
            in String[] specificTypes, in Intent intent,
            String resolvedType, int flags, int userId);

    List<ResolveInfo> queryIntentReceivers(in Intent intent,
            String resolvedType, int flags, int userId);

    ResolveInfo resolveService(in Intent intent,
            String resolvedType, int flags, int userId);

    List<ResolveInfo> queryIntentServices(in Intent intent,
            String resolvedType, int flags, int userId);

    List<ResolveInfo> queryIntentContentProviders(in Intent intent,
            String resolvedType, int flags, int userId);

    /**
     * This implements getInstalledPackages via a "last returned row"
     * mechanism that is not exposed in the API. This is to get around the IPC
     * limit that kicks in when flags are included that bloat up the data
     * returned.
     */
    ParceledListSlice getInstalledPackages(int flags, in int userId);

    /**
     * This implements getPackagesHoldingPermissions via a "last returned row"
     * mechanism that is not exposed in the API. This is to get around the IPC
     * limit that kicks in when flags are included that bloat up the data
     * returned.
     */
    ParceledListSlice getPackagesHoldingPermissions(in String[] permissions,
            int flags, int userId);

    /**
     * This implements getInstalledApplications via a "last returned row"
     * mechanism that is not exposed in the API. This is to get around the IPC
     * limit that kicks in when flags are included that bloat up the data
     * returned.
     */
    ParceledListSlice getInstalledApplications(int flags, int userId);

    /**
     * Retrieve all applications that are marked as persistent.
     * 
     * @return A List&lt;applicationInfo> containing one entry for each persistent
     *         application.
     */
    List<ApplicationInfo> getPersistentApplications(int flags);

    ProviderInfo resolveContentProvider(String name, int flags, int userId);

    /**
     * Retrieve sync information for all content providers.
     * 
     * @param outNames Filled in with a list of the root names of the content
     *                 providers that can sync.
     * @param outInfo Filled in with a list of the ProviderInfo for each
     *                name in 'outNames'.
     */
    void querySyncProviders(inout List<String> outNames,
            inout List<ProviderInfo> outInfo);

    List<ProviderInfo> queryContentProviders(
            String processName, int uid, int flags);

    InstrumentationInfo getInstrumentationInfo(
            in ComponentName className, int flags);

    List<InstrumentationInfo> queryInstrumentation(
            String targetPackage, int flags);

    void installPackage(in String originPath,
            in IPackageInstallObserver2 observer,
            int flags,
            in String installerPackageName,
            in VerificationParams verificationParams,
            in String packageAbiOverride);

    void installPackageAsUser(in String originPath,
            in IPackageInstallObserver2 observer,
            int flags,
            in String installerPackageName,
            in VerificationParams verificationParams,
            in String packageAbiOverride,
            int userId);

    void finishPackageInstall(int token);

    void setInstallerPackageName(in String targetPackage, in String installerPackageName);

    /** @deprecated rawr, don't call AIDL methods directly! */
    void deletePackageAsUser(in String packageName, IPackageDeleteObserver observer,
            int userId, int flags);

    /**
     * Delete a package for a specific user.
     *
     * @param packageName The fully qualified name of the package to delete.
     * @param observer a callback to use to notify when the package deletion in finished.
     * @param userId the id of the user for whom to delete the package
     * @param flags - possible values: {@link #DONT_DELETE_DATA}
     */
    void deletePackage(in String packageName, IPackageDeleteObserver2 observer, int userId, int flags);

    String getInstallerPackageName(in String packageName);

    void addPackageToPreferred(String packageName);

    void removePackageFromPreferred(String packageName);

    List<PackageInfo> getPreferredPackages(int flags);

    void resetPreferredActivities(int userId);

    ResolveInfo getLastChosenActivity(in Intent intent,
            String resolvedType, int flags);

    void setLastChosenActivity(in Intent intent, String resolvedType, int flags,
            in IntentFilter filter, int match, in ComponentName activity);

    void addPreferredActivity(in IntentFilter filter, int match,
            in ComponentName[] set, in ComponentName activity, int userId);

    void replacePreferredActivity(in IntentFilter filter, int match,
            in ComponentName[] set, in ComponentName activity, int userId);

    void clearPackagePreferredActivities(String packageName);

    int getPreferredActivities(out List<IntentFilter> outFilters,
            out List<ComponentName> outActivities, String packageName);

    void addPersistentPreferredActivity(in IntentFilter filter, in ComponentName activity, int userId);

    void clearPackagePersistentPreferredActivities(String packageName, int userId);

    void addCrossProfileIntentFilter(in IntentFilter intentFilter, String ownerPackage,
            int ownerUserId, int sourceUserId, int targetUserId, int flags);

    void clearCrossProfileIntentFilters(int sourceUserId, String ownerPackage, int ownerUserId);

    /**
     * Report the set of 'Home' activity candidates, plus (if any) which of them
     * is the current "always use this one" setting.
     */
     ComponentName getHomeActivities(out List<ResolveInfo> outHomeCandidates);

    /**
     * As per {@link android.content.pm.PackageManager#setComponentEnabledSetting}.
     */
    void setComponentEnabledSetting(in ComponentName componentName,
            in int newState, in int flags, int userId);

    /**
     * As per {@link android.content.pm.PackageManager#getComponentEnabledSetting}.
     */
    int getComponentEnabledSetting(in ComponentName componentName, int userId);
    
    /**
     * As per {@link android.content.pm.PackageManager#setApplicationEnabledSetting}.
     */
    void setApplicationEnabledSetting(in String packageName, in int newState, int flags,
            int userId, String callingPackage);
    
    /**
     * As per {@link android.content.pm.PackageManager#getApplicationEnabledSetting}.
     */
    int getApplicationEnabledSetting(in String packageName, int userId);
    
    /**
     * Set whether the given package should be considered stopped, making
     * it not visible to implicit intents that filter out stopped packages.
     */
    void setPackageStoppedState(String packageName, boolean stopped, int userId);

    /**
     * Free storage by deleting LRU sorted list of cache files across
     * all applications. If the currently available free storage
     * on the device is greater than or equal to the requested
     * free storage, no cache files are cleared. If the currently
     * available storage on the device is less than the requested
     * free storage, some or all of the cache files across
     * all applications are deleted (based on last accessed time)
     * to increase the free storage space on the device to
     * the requested value. There is no guarantee that clearing all
     * the cache files from all applications will clear up
     * enough storage to achieve the desired value.
     * @param freeStorageSize The number of bytes of storage to be
     * freed by the system. Say if freeStorageSize is XX,
     * and the current free storage is YY,
     * if XX is less than YY, just return. if not free XX-YY number
     * of bytes if possible.
     * @param observer call back used to notify when
     * the operation is completed
     */
     void freeStorageAndNotify(in long freeStorageSize,
             IPackageDataObserver observer);

    /**
     * Free storage by deleting LRU sorted list of cache files across
     * all applications. If the currently available free storage
     * on the device is greater than or equal to the requested
     * free storage, no cache files are cleared. If the currently
     * available storage on the device is less than the requested
     * free storage, some or all of the cache files across
     * all applications are deleted (based on last accessed time)
     * to increase the free storage space on the device to
     * the requested value. There is no guarantee that clearing all
     * the cache files from all applications will clear up
     * enough storage to achieve the desired value.
     * @param freeStorageSize The number of bytes of storage to be
     * freed by the system. Say if freeStorageSize is XX,
     * and the current free storage is YY,
     * if XX is less than YY, just return. if not free XX-YY number
     * of bytes if possible.
     * @param pi IntentSender call back used to
     * notify when the operation is completed.May be null
     * to indicate that no call back is desired.
     */
     void freeStorage(in long freeStorageSize,
             in IntentSender pi);
     
    /**
     * Delete all the cache files in an applications cache directory
     * @param packageName The package name of the application whose cache
     * files need to be deleted
     * @param observer a callback used to notify when the deletion is finished.
     */
    void deleteApplicationCacheFiles(in String packageName, IPackageDataObserver observer);
    
    /**
     * Clear the user data directory of an application.
     * @param packageName The package name of the application whose cache
     * files need to be deleted
     * @param observer a callback used to notify when the operation is completed.
     */
    void clearApplicationUserData(in String packageName, IPackageDataObserver observer, int userId);
    
   /**
     * Get package statistics including the code, data and cache size for
     * an already installed package
     * @param packageName The package name of the application
     * @param userHandle Which user the size should be retrieved for
     * @param observer a callback to use to notify when the asynchronous
     * retrieval of information is complete.
     */
    void getPackageSizeInfo(in String packageName, int userHandle, IPackageStatsObserver observer);
    
    /**
     * Get a list of shared libraries that are available on the
     * system.
     */
    String[] getSystemSharedLibraryNames();

    /**
     * Get a list of features that are available on the
     * system.
     */
    FeatureInfo[] getSystemAvailableFeatures();

    boolean hasSystemFeature(String name);
    
    void enterSafeMode();
    boolean isSafeMode();
    void systemReady();
    boolean hasSystemUidErrors();

    /**
     * Ask the package manager to perform boot-time dex-opt of all
     * existing packages.
     */
    void performBootDexOpt();

    /**
     * Ask the package manager to perform dex-opt (if needed) on the given
     * package and for the given instruction set if it already hasn't done
     * so.
     *
     * If the supplied instructionSet is null, the package manager will use
     * the packages default instruction set.
     *
     * In most cases, apps are dexopted in advance and this function will
     * be a no-op.
     */
    boolean performDexOptIfNeeded(String packageName, String instructionSet);

    void forceDexOpt(String packageName);

    /**
     * Update status of external media on the package manager to scan and
     * install packages installed on the external media. Like say the
     * MountService uses this to call into the package manager to update
     * status of sdcard.
     */
    void updateExternalMediaStatus(boolean mounted, boolean reportStatus);

    PackageCleanItem nextPackageToClean(in PackageCleanItem lastPackage);

    void movePackage(String packageName, IPackageMoveObserver observer, int flags);
    
    boolean addPermissionAsync(in PermissionInfo info);

    boolean setInstallLocation(int loc);
    int getInstallLocation();

    int installExistingPackageAsUser(String packageName, int userId);

    void verifyPendingInstall(int id, int verificationCode);
    void extendVerificationTimeout(int id, int verificationCodeAtTimeout, long millisecondsToDelay);

    VerifierDeviceIdentity getVerifierDeviceIdentity();

    boolean isFirstBoot();
    boolean isOnlyCoreApps();
    boolean isUpgrade();

    void setPermissionEnforced(String permission, boolean enforced);
    boolean isPermissionEnforced(String permission);

    /** Reflects current DeviceStorageMonitorService state */
    boolean isStorageLow();

    boolean setApplicationHiddenSettingAsUser(String packageName, boolean hidden, int userId);
    boolean getApplicationHiddenSettingAsUser(String packageName, int userId);

    IPackageInstaller getPackageInstaller();

    boolean setBlockUninstallForUser(String packageName, boolean blockUninstall, int userId);
    boolean getBlockUninstallForUser(String packageName, int userId);

    KeySet getKeySetByAlias(String packageName, String alias);
    KeySet getSigningKeySet(String packageName);
    boolean isPackageSignedByKeySet(String packageName, in KeySet ks);
    boolean isPackageSignedByKeySetExactly(String packageName, in KeySet ks);
}
