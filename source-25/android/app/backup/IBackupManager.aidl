/*
 * Copyright (C) 2009 The Android Open Source Project
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

package android.app.backup;

import android.app.backup.IBackupObserver;
import android.app.backup.IFullBackupRestoreObserver;
import android.app.backup.IRestoreSession;
import android.os.ParcelFileDescriptor;
import android.content.Intent;

/**
 * Direct interface to the Backup Manager Service that applications invoke on.  The only
 * operation currently needed is a simple notification that the app has made changes to
 * data it wishes to back up, so the system should run a backup pass.
 *
 * Apps will use the {@link android.app.backup.BackupManager} class rather than going through
 * this Binder interface directly.
 * 
 * {@hide}
 */
interface IBackupManager {
    /**
     * Tell the system service that the caller has made changes to its
     * data, and therefore needs to undergo an incremental backup pass.
     *
     * Any application can invoke this method for its own package, but
     * only callers who hold the android.permission.BACKUP permission
     * may invoke it for arbitrary packages.
     */
    void dataChanged(String packageName);

    /**
     * Erase all backed-up data for the given package from the given storage
     * destination.
     *
     * Any application can invoke this method for its own package, but
     * only callers who hold the android.permission.BACKUP permission
     * may invoke it for arbitrary packages.
     */
    void clearBackupData(String transportName, String packageName);

    /**
     * Notifies the Backup Manager Service that an agent has become available.  This
     * method is only invoked by the Activity Manager.
     */
    void agentConnected(String packageName, IBinder agent);

    /**
     * Notify the Backup Manager Service that an agent has unexpectedly gone away.
     * This method is only invoked by the Activity Manager.
     */
    void agentDisconnected(String packageName);

    /**
     * Notify the Backup Manager Service that an application being installed will
     * need a data-restore pass.  This method is only invoked by the Package Manager.
     */
    void restoreAtInstall(String packageName, int token);

    /**
     * Enable/disable the backup service entirely.  When disabled, no backup
     * or restore operations will take place.  Data-changed notifications will
     * still be observed and collected, however, so that changes made while the
     * mechanism was disabled will still be backed up properly if it is enabled
     * at some point in the future.
     *
     * <p>Callers must hold the android.permission.BACKUP permission to use this method.
     */
    void setBackupEnabled(boolean isEnabled);

    /**
     * Enable/disable automatic restore of application data at install time.  When
     * enabled, installation of any package will involve the Backup Manager.  If data
     * exists for the newly-installed package, either from the device's current [enabled]
     * backup dataset or from the restore set used in the last wholesale restore operation,
     * that data will be supplied to the new package's restore agent before the package
     * is made generally available for launch.
     *
     * <p>Callers must hold  the android.permission.BACKUP permission to use this method.
     *
     * @param doAutoRestore When true, enables the automatic app-data restore facility.  When
     *   false, this facility will be disabled.
     */
    void setAutoRestore(boolean doAutoRestore);

    /**
     * Indicate that any necessary one-time provisioning has occurred.
     *
     * <p>Callers must hold the android.permission.BACKUP permission to use this method.
     */
    void setBackupProvisioned(boolean isProvisioned);

    /**
     * Report whether the backup mechanism is currently enabled.
     *
     * <p>Callers must hold the android.permission.BACKUP permission to use this method.
     */
    boolean isBackupEnabled();

    /**
     * Set the device's backup password.  Returns {@code true} if the password was set
     * successfully, {@code false} otherwise.  Typically a failure means that an incorrect
     * current password was supplied.
     *
     * <p>Callers must hold the android.permission.BACKUP permission to use this method.
     */
    boolean setBackupPassword(in String currentPw, in String newPw);

    /**
     * Reports whether a backup password is currently set.  If not, then a null or empty
     * "current password" argument should be passed to setBackupPassword().
     *
     * <p>Callers must hold the android.permission.BACKUP permission to use this method.
     */
    boolean hasBackupPassword();

    /**
     * Schedule an immediate backup attempt for all pending updates.  This is
     * primarily intended for transports to use when they detect a suitable
     * opportunity for doing a backup pass.  If there are no pending updates to
     * be sent, no action will be taken.  Even if some updates are pending, the
     * transport will still be asked to confirm via the usual requestBackupTime()
     * method.
     *
     * <p>Callers must hold the android.permission.BACKUP permission to use this method.
     */
    void backupNow();

    /**
     * Write a full backup of the given package to the supplied file descriptor.
     * The fd may be a socket or other non-seekable destination.  If no package names
     * are supplied, then every application on the device will be backed up to the output.
     *
     * <p>This method is <i>synchronous</i> -- it does not return until the backup has
     * completed.
     *
     * <p>Callers must hold the android.permission.BACKUP permission to use this method.
     *
     * @param fd The file descriptor to which a 'tar' file stream is to be written
     * @param includeApks If <code>true</code>, the resulting tar stream will include the
     *     application .apk files themselves as well as their data.
     * @param includeObbs If <code>true</code>, the resulting tar stream will include any
     *     application expansion (OBB) files themselves belonging to each application.
     * @param includeShared If <code>true</code>, the resulting tar stream will include
     *     the contents of the device's shared storage (SD card or equivalent).
     * @param allApps If <code>true</code>, the resulting tar stream will include all
     *     installed applications' data, not just those named in the <code>packageNames</code>
     *     parameter.
     * @param allIncludesSystem If {@code true}, then {@code allApps} will be interpreted
     *     as including packages pre-installed as part of the system. If {@code false},
     *     then setting {@code allApps} to {@code true} will mean only that all 3rd-party
     *     applications will be included in the dataset.
     * @param packageNames The package names of the apps whose data (and optionally .apk files)
     *     are to be backed up.  The <code>allApps</code> parameter supersedes this.
     */
    void fullBackup(in ParcelFileDescriptor fd, boolean includeApks, boolean includeObbs,
            boolean includeShared, boolean doWidgets, boolean allApps, boolean allIncludesSystem,
            boolean doCompress, in String[] packageNames);

    /**
     * Perform a full-dataset backup of the given applications via the currently active
     * transport.
     *
     * @param packageNames The package names of the apps whose data are to be backed up.
     */
    void fullTransportBackup(in String[] packageNames);

    /**
     * Restore device content from the data stream passed through the given socket.  The
     * data stream must be in the format emitted by fullBackup().
     *
     * <p>Callers must hold the android.permission.BACKUP permission to use this method.
     */
    void fullRestore(in ParcelFileDescriptor fd);

    /**
     * Confirm that the requested full backup/restore operation can proceed.  The system will
     * not actually perform the operation described to fullBackup() / fullRestore() unless the
     * UI calls back into the Backup Manager to confirm, passing the correct token.  At
     * the same time, the UI supplies a callback Binder for progress notifications during
     * the operation.
     *
     * <p>The password passed by the confirming entity must match the saved backup or
     * full-device encryption password in order to perform a backup.  If a password is
     * supplied for restore, it must match the password used when creating the full
     * backup dataset being used for restore.
     *
     * <p>Callers must hold the android.permission.BACKUP permission to use this method.
     */
    void acknowledgeFullBackupOrRestore(int token, boolean allow,
            in String curPassword, in String encryptionPassword,
            IFullBackupRestoreObserver observer);

    /**
     * Identify the currently selected transport.  Callers must hold the
     * android.permission.BACKUP permission to use this method.
     */
    String getCurrentTransport();

    /**
     * Request a list of all available backup transports' names.  Callers must
     * hold the android.permission.BACKUP permission to use this method.
     */
    String[] listAllTransports();

    /**
     * Retrieve the list of whitelisted transport components.  Callers do </i>not</i> need
     * any special permission.
     *
     * @return The names of all whitelisted transport components defined by the system.
     */
    String[] getTransportWhitelist();

    /**
     * Specify the current backup transport.  Callers must hold the
     * android.permission.BACKUP permission to use this method.
     *
     * @param transport The name of the transport to select.  This should be one
     * of {@link BackupManager.TRANSPORT_GOOGLE} or {@link BackupManager.TRANSPORT_ADB}.
     * @return The name of the previously selected transport.  If the given transport
     *   name is not one of the currently available transports, no change is made to
     *   the current transport setting and the method returns null.
     */
    String selectBackupTransport(String transport);

    /**
     * Get the configuration Intent, if any, from the given transport.  Callers must
     * hold the android.permission.BACKUP permission in order to use this method.
     *
     * @param transport The name of the transport to query.
     * @return An Intent to use with Activity#startActivity() to bring up the configuration
     *   UI supplied by the transport.  If the transport has no configuration UI, it should
     *   return {@code null} here.
     */
    Intent getConfigurationIntent(String transport);

    /**
     * Get the destination string supplied by the given transport.  Callers must
     * hold the android.permission.BACKUP permission in order to use this method.
     *
     * @param transport The name of the transport to query.
     * @return A string describing the current backup destination.  This string is used
     *   verbatim by the Settings UI as the summary text of the "configure..." item.
     */
    String getDestinationString(String transport);

    /**
     * Get the manage-data UI intent, if any, from the given transport.  Callers must
     * hold the android.permission.BACKUP permission in order to use this method.
     */
    Intent getDataManagementIntent(String transport);

    /**
     * Get the manage-data menu label, if any, from the given transport.  Callers must
     * hold the android.permission.BACKUP permission in order to use this method.
     */
    String getDataManagementLabel(String transport);

    /**
     * Begin a restore session.  Either or both of packageName and transportID
     * may be null.  If packageName is non-null, then only the given package will be
     * considered for restore.  If transportID is null, then the restore will use
     * the current active transport.
     * <p>
     * This method requires the android.permission.BACKUP permission <i>except</i>
     * when transportID is null and packageName is the name of the caller's own
     * package.  In that case, the restore session returned is suitable for supporting
     * the BackupManager.requestRestore() functionality via RestoreSession.restorePackage()
     * without requiring the app to hold any special permission.
     *
     * @param packageName The name of the single package for which a restore will
     *        be requested.  May be null, in which case all packages in the restore
     *        set can be restored.
     * @param transportID The name of the transport to use for the restore operation.
     *        May be null, in which case the current active transport is used.
     * @return An interface to the restore session, or null on error.
     */
    IRestoreSession beginRestoreSession(String packageName, String transportID);

    /**
     * Notify the backup manager that a BackupAgent has completed the operation
     * corresponding to the given token.
     *
     * @param token The transaction token passed to the BackupAgent method being
     *        invoked.
     * @param result In the case of a full backup measure operation, the estimated
     *        total file size that would result from the operation. Unused in all other
     *        cases.
     * {@hide}
     */
    void opComplete(int token, long result);

    /**
     * Make the device's backup and restore machinery (in)active.  When it is inactive,
     * the device will not perform any backup operations, nor will it deliver data for
     * restore, although clients can still safely call BackupManager methods.
     *
     * @param whichUser User handle of the defined user whose backup active state
     *     is to be adjusted.
     * @param makeActive {@code true} when backup services are to be made active;
     *     {@code false} otherwise.
     */
    void setBackupServiceActive(int whichUser, boolean makeActive);

    /**
     * Queries the activity status of backup service as set by {@link #setBackupServiceActive}.
     * @param whichUser User handle of the defined user whose backup active state
     *     is being queried.
     */
    boolean isBackupServiceActive(int whichUser);

    /**
     * Ask the framework which dataset, if any, the given package's data would be
     * restored from if we were to install it right now.
     *
     * <p>Callers must hold the android.permission.BACKUP permission to use this method.
     *
     * @param packageName The name of the package whose most-suitable dataset we
     *     wish to look up
     * @return The dataset token from which a restore should be attempted, or zero if
     *     no suitable data is available.
     */
    long getAvailableRestoreToken(String packageName);

    /**
     * Ask the framework whether this app is eligible for backup.
     *
     * <p>Callers must hold the android.permission.BACKUP permission to use this method.
     *
     * @param packageName The name of the package.
     * @return Whether this app is eligible for backup.
     */
    boolean isAppEligibleForBackup(String packageName);

    /**
     * Request an immediate backup, providing an observer to which results of the backup operation
     * will be published. The Android backup system will decide for each package whether it will
     * be full app data backup or key/value-pair-based backup.
     *
     * <p>If this method returns zero (meaning success), the OS will attempt to backup all provided
     * packages using the remote transport.
     *
     * @param observer The {@link BackupObserver} to receive callbacks during the backup
     * operation.
     *
     * @return Zero on success; nonzero on error.
     */
    int requestBackup(in String[] packages, IBackupObserver observer);
}
