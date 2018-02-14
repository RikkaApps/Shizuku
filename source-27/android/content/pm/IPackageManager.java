/*
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

package android.content.pm;

import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ChangedPackages;
import android.content.pm.InstantAppInfo;
import android.content.pm.FeatureInfo;
import android.content.pm.IDexModuleRegisterCallback;
import android.content.pm.IPackageInstallObserver2;
import android.content.pm.IPackageInstaller;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.IPackageDeleteObserver2;
import android.content.pm.IPackageDataObserver;
import android.content.pm.IPackageMoveObserver;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.IOnPermissionsChangeListener;
import android.content.pm.IntentFilterVerificationInfo;
import android.content.pm.InstrumentationInfo;
import android.content.pm.KeySet;
import android.content.pm.PackageInfo;
import android.content.pm.PackageCleanItem;
import android.content.pm.ParceledListSlice;
import android.content.pm.ProviderInfo;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.pm.SharedLibraryInfo;
import android.content.pm.UserInfo;
import android.content.pm.VerifierDeviceIdentity;
import android.content.pm.VersionedPackage;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.content.IntentSender;

/**
 *  See {@link PackageManager} for documentation on most of the APIs
 *  here.
 *
 *  {@hide}
 */
interface IPackageManager {
    void checkPackageStartable(String packageName, int userId);
    boolean isPackageAvailable(String packageName, int userId);
    PackageInfo getPackageInfo(String packageName, int flags, int userId);
    PackageInfo getPackageInfoVersioned(VersionedPackage versionedPackage,
            int flags, int userId);
    int getPackageUid(String packageName, int flags, int userId);
    int[] getPackageGids(String packageName, int flags, int userId);

    String[] currentToCanonicalPackageNames(String[] names);
    String[] canonicalToCurrentPackageNames(String[] names);

    PermissionInfo getPermissionInfo(String name, String packageName, int flags);

    ParceledListSlice<PermissionInfo> queryPermissionsByGroup(String group, int flags);

    PermissionGroupInfo getPermissionGroupInfo(String name, int flags);

    ParceledListSlice<PermissionGroupInfo> getAllPermissionGroups(int flags);

    ApplicationInfo getApplicationInfo(String packageName, int flags ,int userId);

    ActivityInfo getActivityInfo(ComponentName className, int flags, int userId);

    boolean activitySupportsIntent(ComponentName className, Intent intent,
            String resolvedType);

    ActivityInfo getReceiverInfo(ComponentName className, int flags, int userId);

    ServiceInfo getServiceInfo(ComponentName className, int flags, int userId);

    ProviderInfo getProviderInfo(ComponentName className, int flags, int userId);

    int checkPermission(String permName, String pkgName, int userId);

    int checkUidPermission(String permName, int uid);

    boolean addPermission(PermissionInfo info);

    void removePermission(String name);

    void grantRuntimePermission(String packageName, String permissionName, int userId);

    void revokeRuntimePermission(String packageName, String permissionName, int userId);

    void resetRuntimePermissions();

    int getPermissionFlags(String permissionName, String packageName, int userId);

    void updatePermissionFlags(String permissionName, String packageName, int flagMask,
            int flagValues, int userId);

    void updatePermissionFlagsForAllApps(int flagMask, int flagValues, int userId);

    boolean shouldShowRequestPermissionRationale(String permissionName,
            String packageName, int userId);

    boolean isProtectedBroadcast(String actionName);

    int checkSignatures(String pkg1, String pkg2);

    int checkUidSignatures(int uid1, int uid2);

    List<String> getAllPackages();

    String[] getPackagesForUid(int uid);

    String getNameForUid(int uid);
    String[] getNamesForUids(int[] uids);

    int getUidForSharedUser(String sharedUserName);

    int getFlagsForUid(int uid);

    int getPrivateFlagsForUid(int uid);

    boolean isUidPrivileged(int uid);

    String[] getAppOpPermissionPackages(String permissionName);

    ResolveInfo resolveIntent(Intent intent, String resolvedType, int flags, int userId);

    ResolveInfo findPersistentPreferredActivity(Intent intent, int userId);

    boolean canForwardTo(Intent intent, String resolvedType, int sourceUserId, int targetUserId);

    ParceledListSlice<ResolveInfo> queryIntentActivities(Intent intent,
            String resolvedType, int flags, int userId);

    ParceledListSlice<ResolveInfo> queryIntentActivityOptions(
            ComponentName caller, Intent[] specifics,
            String[] specificTypes, Intent intent,
            String resolvedType, int flags, int userId);

    ParceledListSlice<ResolveInfo> queryIntentReceivers(Intent intent,
            String resolvedType, int flags, int userId);

    ResolveInfo resolveService(Intent intent,
            String resolvedType, int flags, int userId);

    ParceledListSlice<ResolveInfo> queryIntentServices(Intent intent,
            String resolvedType, int flags, int userId);

    ParceledListSlice<ResolveInfo> queryIntentContentProviders(Intent intent,
            String resolvedType, int flags, int userId);

    /**
     * This implements getInstalledPackages via a "last returned row"
     * mechanism that is not exposed the API. This is to get around the IPC
     * limit that kicks when flags are included that bloat up the data
     * returned.
     */
    ParceledListSlice<PackageInfo> getInstalledPackages(int flags, int userId);

    /**
     * This implements getPackagesHoldingPermissions via a "last returned row"
     * mechanism that is not exposed the API. This is to get around the IPC
     * limit that kicks when flags are included that bloat up the data
     * returned.
     */
    ParceledListSlice<PackageInfo> getPackagesHoldingPermissions(String[] permissions,
            int flags, int userId);

    /**
     * This implements getInstalledApplications via a "last returned row"
     * mechanism that is not exposed the API. This is to get around the IPC
     * limit that kicks when flags are included that bloat up the data
     * returned.
     */
    ParceledListSlice<ApplicationInfo> getInstalledApplications(int flags, int userId);

    /**
     * Retrieve all applications that are marked as persistent.
     *
     * @return A List&lt;applicationInfo> containing one entry for each persistent
     *         application.
     */
    ParceledListSlice<ApplicationInfo> getPersistentApplications(int flags);

    ProviderInfo resolveContentProvider(String name, int flags, int userId);

    /**
     * Retrieve sync information for all content providers.
     *
     * @param outNames Filled with a list of the root names of the content
     *                 providers that can sync.
     * @param outInfo Filled with a list of the ProviderInfo for each
     *                name 'outNames'.
     */
    void querySyncProviders(List<String> outNames,
            List<ProviderInfo> outInfo);

    ParceledListSlice<ProviderInfo> queryContentProviders(
            String processName, int uid, int flags, String metaDataKey);

    InstrumentationInfo getInstrumentationInfo(
            ComponentName className, int flags);

    ParceledListSlice<InstrumentationInfo> queryInstrumentation(
            String targetPackage, int flags);

    /** @deprecated Use PackageInstaller instead */
    void installPackageAsUser(String originPath,
            IPackageInstallObserver2 observer,
            int flags,
            String installerPackageName,
            int userId);

    void finishPackageInstall(int token, boolean didLaunch);

    void setInstallerPackageName(String targetPackage, String installerPackageName);

    void setApplicationCategoryHint(String packageName, int categoryHint, String callerPackageName);

    /** @deprecated rawr, don't call AIDL methods directly! */
    void deletePackageAsUser(String packageName, int versionCode,
            IPackageDeleteObserver observer, int userId, int flags);

    /**
     * Delete a package for a specific user.
     *
     * @param versionedPackage The package to delete.
     * @param observer a callback to use to notify when the package deletion finished.
     * @param userId the id of the user for whom to delete the package
     * @param flags - possible values: {@link #DONT_DELETE_DATA}
     */
    void deletePackageVersioned(VersionedPackage versionedPackage,
            IPackageDeleteObserver2 observer, int userId, int flags);

    String getInstallerPackageName(String packageName);

    void resetApplicationPreferences(int userId);

    ResolveInfo getLastChosenActivity(Intent intent,
            String resolvedType, int flags);

    void setLastChosenActivity(Intent intent, String resolvedType, int flags,
            IntentFilter filter, int match, ComponentName activity);

    void addPreferredActivity(IntentFilter filter, int match,
            ComponentName[] set, ComponentName activity, int userId);

    void replacePreferredActivity(IntentFilter filter, int match,
            ComponentName[] set, ComponentName activity, int userId);

    void clearPackagePreferredActivities(String packageName);

    int getPreferredActivities(List<IntentFilter> outFilters,
            List<ComponentName> outActivities, String packageName);

    void addPersistentPreferredActivity(IntentFilter filter, ComponentName activity, int userId);

    void clearPackagePersistentPreferredActivities(String packageName, int userId);

    void addCrossProfileIntentFilter(IntentFilter intentFilter, String ownerPackage,
            int sourceUserId, int targetUserId, int flags);

    void clearCrossProfileIntentFilters(int sourceUserId, String ownerPackage);

    String[] setPackagesSuspendedAsUser(String[] packageNames, boolean suspended, int userId);
    boolean isPackageSuspendedForUser(String packageName, int userId);

    /**
     * Backup/restore support - only the system uid may use these.
     */
    byte[] getPreferredActivityBackup(int userId);
    void restorePreferredActivities(byte[] backup, int userId);
    byte[] getDefaultAppsBackup(int userId);
    void restoreDefaultApps(byte[] backup, int userId);
    byte[] getIntentFilterVerificationBackup(int userId);
    void restoreIntentFilterVerification(byte[] backup, int userId);
    byte[] getPermissionGrantBackup(int userId);
    void restorePermissionGrants(byte[] backup, int userId);

    /**
     * Report the set of 'Home' activity candidates, plus (if any) which of them
     * is the current "always use this one" setting.
     */
     ComponentName getHomeActivities(List<ResolveInfo> outHomeCandidates);

    void setHomeActivity(ComponentName className, int userId);

    /**
     * As per {@link android.content.pm.PackageManager#setComponentEnabledSetting}.
     */
    void setComponentEnabledSetting(ComponentName componentName,
            int newState, int flags, int userId);

    /**
     * As per {@link android.content.pm.PackageManager#getComponentEnabledSetting}.
     */
    int getComponentEnabledSetting(ComponentName componentName, int userId);

    /**
     * As per {@link android.content.pm.PackageManager#setApplicationEnabledSetting}.
     */
    void setApplicationEnabledSetting(String packageName, int newState, int flags,
            int userId, String callingPackage);

    /**
     * As per {@link android.content.pm.PackageManager#getApplicationEnabledSetting}.
     */
    int getApplicationEnabledSetting(String packageName, int userId);

    /**
     * Logs process start information (including APK hash) to the security log.
     */
    void logAppProcessStartIfNeeded(String processName, int uid, String seinfo, String apkFile,
            int pid);

    /**
     * As per {@link android.content.pm.PackageManager#flushPackageRestrictionsAsUser}.
     */
    void flushPackageRestrictionsAsUser(int userId);

    /**
     * Set whether the given package should be considered stopped, making
     * it not visible to implicit intents that filter stopped packages.
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
     void freeStorageAndNotify(String volumeUuid, long freeStorageSize,
             int storageFlags, IPackageDataObserver observer);

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
     void freeStorage(String volumeUuid, long freeStorageSize,
             int storageFlags, IntentSender pi);

    /**
     * Delete all the cache files an applications cache directory
     * @param packageName The package name of the application whose cache
     * files need to be deleted
     * @param observer a callback used to notify when the deletion is finished.
     */
    void deleteApplicationCacheFiles(String packageName, IPackageDataObserver observer);

    /**
     * Delete all the cache files an applications cache directory
     * @param packageName The package name of the application whose cache
     * files need to be deleted
     * @param userId the user to delete application cache for
     * @param observer a callback used to notify when the deletion is finished.
     */
    void deleteApplicationCacheFilesAsUser(String packageName, int userId, IPackageDataObserver observer);

    /**
     * Clear the user data directory of an application.
     * @param packageName The package name of the application whose cache
     * files need to be deleted
     * @param observer a callback used to notify when the operation is completed.
     */
    void clearApplicationUserData(String packageName, IPackageDataObserver observer, int userId);

    /**
     * Clear the profile data of an application.
     * @param packageName The package name of the application whose profile data
     * need to be deleted
     */
    void clearApplicationProfileData(String packageName);

   /**
     * Get package statistics including the code, data and cache size for
     * an already installed package
     * @param packageName The package name of the application
     * @param userHandle Which user the size should be retrieved for
     * @param observer a callback to use to notify when the asynchronous
     * retrieval of information is complete.
     */
    void getPackageSizeInfo(String packageName, int userHandle, IPackageStatsObserver observer);

    /**
     * Get a list of shared libraries that are available on the
     * system.
     */
    String[] getSystemSharedLibraryNames();

    /**
     * Get a list of features that are available on the
     * system.
     */
    ParceledListSlice<FeatureInfo> getSystemAvailableFeatures();

    boolean hasSystemFeature(String name, int version);

    void enterSafeMode();
    boolean isSafeMode();
    void systemReady();
    boolean hasSystemUidErrors();

    /**
     * Ask the package manager to fstrim the disk if needed.
     */
    void performFstrimIfNeeded();

    /**
     * Ask the package manager to update packages if needed.
     */
    void updatePackagesIfNeeded();

    /**
     * Notify the package manager that a package is going to be used and why.
     *
     * See PackageManager.NOTIFY_PACKAGE_USE_* for reasons.
     */
    void notifyPackageUse(String packageName, int reason);

    /**
     * Notify the package manager that a list of dex files have been loaded.
     *
     * @param loadingPackageName the name of the package who performs the load
     * @param classLoadersNames the names of the class loaders present the loading chain. The
     *    list encodes the class loader chathe natural order. The first class loader has
     *    the second one as its parent and so on. The dex files present the class path of the
     *    first class loader will be recorded the usage file.
     * @param classPaths the class paths corresponding to the class loaders names from
     *     {@param classLoadersNames}. The the first element corresponds to the first class loader
     *     and so on. A classpath is represented as a list of dex files separated by
     *     {@code File.pathSeparator}.
     *     The dex files found the first class path will be recorded the usage file.
     * @param loaderIsa the ISA of the loader process
     */
    void notifyDexLoad(String loadingPackageName, List<String> classLoadersNames,
            List<String> classPaths, String loaderIsa);

    /**
     * Register an application dex module with the package manager.
     * The package manager will keep track of the given module for future optimizations.
     *
     * Dex module optimizations will disable the classpath checking at runtime. The client bares
     * the responsibility to ensure that the static assumptions on classes the optimized code
     * hold at runtime (e.g. there's no duplicate classes the classpath).
     *
     * Note that the package manager already keeps track of dex modules loaded with
     * {@link dalvik.system.DexClassLoader} and {@link dalvik.system.PathClassLoader}.
     * This can be called for an eager registration.
     *
     * The call might take a while and the results will be posted on the mathread, using
     * the given callback.
     *
     * If the module is intended to be shared with other apps, make sure that the file
     * permissions allow for it.
     * If at registration time the permissions allow for others to read it, the module would
     * be marked as a shared module which might undergo a different optimization strategy.
     * (usually shared modules will generated larger optimizations artifacts,
     * taking more disk space).
     *
     * @param packageName the package name to which the dex module belongs
     * @param dexModulePath the absolute path of the dex module.
     * @param isSharedModule whether or not the module is intended to be used by other apps.
     * @param callback if not null,
     *   {@link android.content.pm.IDexModuleRegisterCallback.IDexModuleRegisterCallback#onDexModuleRegistered}
     *   will be called once the registration finishes.
     */
     void registerDexModule(String packageName, String dexModulePath,
             boolean isSharedModule, IDexModuleRegisterCallback callback);

    /**
     * Ask the package manager to perform a dex-opt with the given compiler filter.
     *
     * Note: exposed only for the shell command to allow moving packages explicitly to a
     *       definite state.
     */
    boolean performDexOptMode(String packageName, boolean checkProfiles,
            String targetCompilerFilter, boolean force, boolean bootComplete, String splitName);

    /**
     * Ask the package manager to perform a dex-opt with the given compiler filter on the
     * secondary dex files belonging to the given package.
     *
     * Note: exposed only for the shell command to allow moving packages explicitly to a
     *       definite state.
     */
    boolean performDexOptSecondary(String packageName,
            String targetCompilerFilter, boolean force);

    /**
     * Ask the package manager to dump profiles associated with a package.
     */
    void dumpProfiles(String packageName);

    void forceDexOpt(String packageName);

    /**
     * Execute the background dexopt job immediately.
     */
    boolean runBackgroundDexoptJob();

    /**
     * Reconcile the information we have abthe secondary dex files belonging to
     * {@code packagName} and the actual dex files. For all dex files that were
     * deleted, update the internal records and delete the generated oat files.
     */
    void reconcileSecondaryDexFiles(String packageName);

    /**
     * Update status of external media on the package manager to scan and
     * install packages installed on the external media. Like say the
     * StorageManagerService uses this to call into the package manager to update
     * status of sdcard.
     */
    void updateExternalMediaStatus(boolean mounted, boolean reportStatus);

    PackageCleanItem nextPackageToClean(PackageCleanItem lastPackage);

    int getMoveStatus(int moveId);

    void registerMoveCallback(IPackageMoveObserver callback);
    void unregisterMoveCallback(IPackageMoveObserver callback);

    int movePackage(String packageName, String volumeUuid);
    int movePrimaryStorage(String volumeUuid);

    boolean addPermissionAsync(PermissionInfo info);

    boolean setInstallLocation(int loc);
    int getInstallLocation();

    int installExistingPackageAsUser(String packageName, int userId, int installFlags,
            int installReason);

    void verifyPendingInstall(int id, int verificationCode);
    void extendVerificationTimeout(int id, int verificationCodeAtTimeout, long millisecondsToDelay);

    void verifyIntentFilter(int id, int verificationCode, List<String> failedDomains);
    int getIntentVerificationStatus(String packageName, int userId);
    boolean updateIntentVerificationStatus(String packageName, int status, int userId);
    ParceledListSlice<IntentFilterVerificationInfo> getIntentFilterVerifications(String packageName);
    ParceledListSlice<IntentFilter> getAllIntentFilters(String packageName);

    boolean setDefaultBrowserPackageName(String packageName, int userId);
    String getDefaultBrowserPackageName(int userId);

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
    boolean isPackageSignedByKeySet(String packageName, KeySet ks);
    boolean isPackageSignedByKeySetExactly(String packageName, KeySet ks);

    void addOnPermissionsChangeListener(IOnPermissionsChangeListener listener);
    void removeOnPermissionsChangeListener(IOnPermissionsChangeListener listener);
    void grantDefaultPermissionsToEnabledCarrierApps(String[] packageNames, int userId);
    void grantDefaultPermissionsToEnabledImsServices(String[] packageNames, int userId);

    boolean isPermissionRevokedByPolicy(String permission, String packageName, int userId);

    String getPermissionControllerPackageName();

    ParceledListSlice<InstantAppInfo> getInstantApps(int userId);
    byte[] getInstantAppCookie(String packageName, int userId);
    boolean setInstantAppCookie(String packageName, byte[] cookie, int userId);
    Bitmap getInstantAppIcon(String packageName, int userId);
    boolean isInstantApp(String packageName, int userId);

    boolean setRequiredForSystemUser(String packageName, boolean systemUserApp);

    /**
     * Sets whether or not an update is available. Ostensibly for instant apps
     * to force exteranl resolution.
     */
    void setUpdateAvailable(String packageName, boolean updateAvaialble);

    String getServicesSystemSharedLibraryPackageName();
    String getSharedSystemSharedLibraryPackageName();

    ChangedPackages getChangedPackages(int sequenceNumber, int userId);

    boolean isPackageDeviceAdminOnAnyUser(String packageName);

    List<String> getPreviousCodePaths(String packageName);

    int getInstallReason(String packageName, int userId);

    ParceledListSlice<SharedLibraryInfo> getSharedLibraries(String packageName, int flags, int userId);

    boolean canRequestPackageInstalls(String packageName, int userId);

    void deletePreloadsFileCache();

    ComponentName getInstantAppResolverComponent();

    ComponentName getInstantAppResolverSettingsComponent();

    ComponentName getInstantAppInstallerComponent();

    String getInstantAppAndroidId(String packageName, int userId);
}
