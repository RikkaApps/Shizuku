package moe.shizuku.privileged.api;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Process;
import android.os.StrictMode;
import android.os.UserHandle;
import android.support.annotation.NonNull;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import hidden.android.app.AppOpsManager;
import hidden.android.content.pm.UserInfo;
import moe.shizuku.privileged.api.receiver.TokenUpdateReceiver;
import moe.shizuku.server.Protocol;

/**
 * Created by Rikka on 2017/5/6.
 */

public final class PrivilegedAPIs extends AbstractPrivilegedAPIs {

    private static final String PACKAGE_NAME = "moe.shizuku.privileged.api";
    public static final int VERSION = BuildConfig.VERSION_CODE;

    public static final int REQUEST_CODE_AUTH = 55608;

    public static final int AUTH_RESULT_OK = 0;
    public static final int AUTH_RESULT_USER_DENIED = -1;
    public static final int AUTH_RESULT_ERROR = -2;

    private static final UUID TOKEN_EMPTY = new UUID(0, 0);

    private static boolean isRoot = false;

    public static boolean installed(Context context) {
        try {
            context.getPackageManager().getPackageInfo(PACKAGE_NAME, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static void setPermitNetworkThreadPolicy() {
        StrictMode.ThreadPolicy permitNetworkPolicy = new StrictMode.ThreadPolicy.Builder(StrictMode.getThreadPolicy())
                .permitNetwork()
                .build();
        StrictMode.setThreadPolicy(permitNetworkPolicy);
    }

    private TokenUpdateReceiver mTokenUpdateReceiver;

    /**
     * Register receiver to receive token update broadcast, old receiver will be unregistered.
     */
    public void registerTokenUpdateReceiver(Context context, TokenUpdateReceiver receiver) {
        unregisterTokenUpdateReceiver(context);

        mTokenUpdateReceiver = receiver;
        context.registerReceiver(mTokenUpdateReceiver,
                new IntentFilter(PACKAGE_NAME + ".intent.action.UPDATE_TOKEN"),
                PACKAGE_NAME + ".permission.RECEIVE_SERVER_STARTED",
                null);
    }

    public void unregisterTokenUpdateReceiver(Context context) {
        if (mTokenUpdateReceiver != null) {
            context.unregisterReceiver(mTokenUpdateReceiver);
        }
        mTokenUpdateReceiver = null;
    }

    public PrivilegedAPIs(UUID token) {
        this.token = token;
    }

    public void updateToken(@NonNull UUID token) {
        this.token = token;
    }

    public UUID getToken() {
        return this.token;
    }

    public boolean authorized() {
        return authorized(token);
    }

    public boolean authorized(UUID token) {
        try {
            Protocol result = authorize(token.getMostSignificantBits(), token.getLeastSignificantBits());
            return result != null && result.getCode() == Protocol.RESULT_OK;
        } catch (Exception ignored) {
            return false;
        }
    }

    /**
     * Request authorization, if unauthorized, manger will be started to request authorization from user.
     *
     * @param activity The activity that will receiver result {@link Activity#onActivityResult} callback.
     *
     * @return true if is requesting or authorized
     *
     * @see #AUTH_RESULT_OK
     * @see #AUTH_RESULT_USER_DENIED
     * @see #AUTH_RESULT_ERROR
     */
    public boolean requstAuthorization(Activity activity) {
        try {
            Protocol result = authorize(token.getMostSignificantBits(), token.getLeastSignificantBits());
            /*if (result == null) {
                return false;
            }

            int code = result.getCode();
            if (code == Protocol.RESULT_UNAUTHORIZED
                    && !fragment.getActivity().getPackageName().equals(PACKAGE_NAME)) {*/
            if (result == null
                    || result.getCode() == Protocol.RESULT_UNAUTHORIZED) {
                Intent intent = new Intent(PACKAGE_NAME + ".intent.action.AUTHORIZATION")
                        .setComponent(new ComponentName(PACKAGE_NAME,
                                PACKAGE_NAME + ".RequestActivity"))
                        .putExtra(PACKAGE_NAME + ".intent.extra.PACKAGE_NAME", activity.getPackageName())
                        .putExtra(PACKAGE_NAME + ".intent.extra.UID", Process.myUid());

                if (activity.getPackageManager().resolveActivity(intent, 0) != null) {
                    activity.startActivityForResult(intent, REQUEST_CODE_AUTH);
                    return true;
                }
            }
            return false;
        } catch (Exception ignored) {
            return false;
        }
    }

    /**
     * Request authorization, if unauthorized, manger will be started to request authorization from user.
     *
     * @param fragment The activity that will receiver result {@link Fragment#onActivityResult} callback.
     *
     * @return true if is requesting or authorized
     *
     * @see #AUTH_RESULT_OK
     * @see #AUTH_RESULT_USER_DENIED
     * @see #AUTH_RESULT_ERROR
     */
    public boolean requstAuthorization(Fragment fragment) {
        try {
            Protocol result = authorize(token.getMostSignificantBits(), token.getLeastSignificantBits());
            /*if (result == null) {
                return false;
            }

            int code = result.getCode();
            if (code == Protocol.RESULT_UNAUTHORIZED
                    && !fragment.getActivity().getPackageName().equals(PACKAGE_NAME)) {*/
            if (result == null
                    || result.getCode() == Protocol.RESULT_UNAUTHORIZED) {
                Intent intent = new Intent(PACKAGE_NAME + ".intent.action.AUTHORIZATION")
                        .setComponent(new ComponentName(PACKAGE_NAME,
                                PACKAGE_NAME + ".RequestActivity"))
                        .putExtra(PACKAGE_NAME + ".intent.extra.PACKAGE_NAME", fragment.getActivity().getPackageName())
                        .putExtra(PACKAGE_NAME + ".intent.extra.UID", Process.myUid());

                if (fragment.getActivity().getPackageManager().resolveActivity(intent, 0) != null) {
                    fragment.startActivityForResult(intent, REQUEST_CODE_AUTH);
                    return true;
                }
            }
            return false;
        } catch (Exception ignored) {
            return false;
        }
    }

    /**
     * Request and update token from manager (must authorize corresponding package in manger first).
     *
     * @return If success authorized token will be returned
     */
    public UUID requestToken(Context context) {
        try {
            UUID token = this.token;

            Cursor cursor = context.getContentResolver().query(Uri.parse("content://moe.shizuku.privileged.api.auth"), null, null, null, null);
            if (cursor != null) {
                cursor.moveToPosition(0);
                cursor.getLong(0);

                token = new UUID(cursor.getLong(0), cursor.getLong(1));

                cursor.close();
            }

            Protocol result = authorize(token.getMostSignificantBits(), token.getLeastSignificantBits());
            if (result == null) {
                return null;
            }
            if (result.getCode() == Protocol.RESULT_OK) {
                updateToken(token);
                return token;
            }
            return null;
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * Return whether server is running on root user (the value will be updated when authorized).
     */
    public static boolean isRoot() {
        return isRoot;
    }

    @Override
    public Protocol authorize(long most, long least) {
        Protocol protocol = super.authorize(most, least);
        if (protocol != null
                && protocol.getCode() == Protocol.RESULT_OK) {
            isRoot = protocol.isRoot();
        }
        return protocol;
    }

    /**
     * Return foreground packageName, result form getTasks(1, 0).get(0).
     */
    public String getForegroundPackageName() {
        List<ActivityManager.RunningTaskInfo> tasks = getTasks(1, 0);
        if (tasks != null && !tasks.isEmpty()) {
            return tasks.get(0).topActivity.getPackageName();
        }
        return null;
    }

    /**
     * Return a list of the tasks that are currently running, with
     * the most recent being first and older ones after in order.  Note that
     * "running" does not mean any of the task's code is currently loaded or
     * activity -- the task may have been frozen by the system, so that it
     * can be restarted in its previous state when next brought to the
     * foreground.
     *
     * @param maxNum The maximum number of entries to return in the list.  The
     * actual number returned may be smaller, depending on how many tasks the
     * user has started.
     *
     * @param flags
     *
     * @return Returns a list of RunningTaskInfo records describing each of
     * the running tasks.
     */
    @Override
    public List<ActivityManager.RunningTaskInfo> getTasks(int maxNum, int flags) {
        return super.getTasks(maxNum, flags);
    }

    /**
     * Returns information for all users on this device, including ones marked for deletion.
     * To retrieve only users that are alive, use {@link #getUsers(boolean)}.
     *
     * @return the list of users that exist on the device.
     */
    public List<UserInfo> getUsers() {
        return super.getUsers(false);
    }

    /**
     * Returns information for all users on this device.
     *
     * @param excludeDying specify if the list should exclude users being
     *            removed.
     * @return the list of users that were created.
     */
    @Override
    public List<UserInfo> getUsers(boolean excludeDying) {
        return super.getUsers(excludeDying);
    }

    /**
     * Returns a file descriptor for the user's photo. PNG data can be read from this file.
     * @param userHandle the user whose photo we want to read.
     * @return a {@link Bitmap} of the user's photo, or null if there's no photo.
     * see com.android.internal.util.UserIcons#getDefaultUserIcon for a default.
     */
    @Override
    public Bitmap getUserIcon(int userHandle) {
        return super.getUserIcon(userHandle);
    }

    /**
     * Return the UID associated with the given package name.
     * <p>
     * Note that the same package will have different UIDs under different
     * {@link UserHandle} on the same device.
     *
     * @param packageName The full name (i.e. com.google.apps.contacts) of the
     *            desired package.
     * @return Returns an integer UID who owns the given package name.
     */
    @Override
    public int getPackageUid(String packageName, int flags, int userId) {
        return super.getPackageUid(packageName, flags, userId);
    }

    /**
     * Return a List of all packages that are installed on the device, for a specific user.
     * Requesting a list of installed packages for another user
     * will require the permission INTERACT_ACROSS_USERS_FULL.
     *
     * @param flags Additional option flags. Use any combination of
     *         {@link PackageManager#GET_ACTIVITIES}, {@link PackageManager#GET_CONFIGURATIONS},
     *         {@link PackageManager#GET_GIDS}, {@link PackageManager#GET_INSTRUMENTATION},
     *         {@link PackageManager#GET_INTENT_FILTERS}, {@link PackageManager#GET_META_DATA},
     *         {@link PackageManager#GET_PERMISSIONS}, {@link PackageManager#GET_PROVIDERS},
     *         {@link PackageManager#GET_RECEIVERS}, {@link PackageManager#GET_SERVICES},
     *         {@link PackageManager#GET_SHARED_LIBRARY_FILES}, {@link PackageManager#GET_SIGNATURES},
     *         {@link PackageManager#GET_URI_PERMISSION_PATTERNS}, {@link PackageManager#GET_UNINSTALLED_PACKAGES},
     *         {@link PackageManager#MATCH_DISABLED_COMPONENTS}, {@link PackageManager#MATCH_DISABLED_UNTIL_USED_COMPONENTS},
     *         {@link PackageManager#MATCH_UNINSTALLED_PACKAGES}
     *         to modify the data returned.
     * @param userId The user for whom the installed packages are to be listed
     *
     * @return A List of PackageInfo objects, one for each installed package,
     *         containing information about the package.  In the unlikely case
     *         there are no installed packages, an empty list is returned. If
     *         flag {@code MATCH_UNINSTALLED_PACKAGES} is set, the package
     *         information is retrieved from the list of uninstalled
     *         applications (which includes installed applications as well as
     *         applications with data directory i.e. applications which had been
     *         deleted with {@code DONT_DELETE_DATA} flag set).
     *
     * @see PackageManager#GET_ACTIVITIES
     * @see PackageManager#GET_CONFIGURATIONS
     * @see PackageManager#GET_GIDS
     * @see PackageManager#GET_INSTRUMENTATION
     * @see PackageManager#GET_INTENT_FILTERS
     * @see PackageManager#GET_META_DATA
     * @see PackageManager#GET_PERMISSIONS
     * @see PackageManager#GET_PROVIDERS
     * @see PackageManager#GET_RECEIVERS
     * @see PackageManager#GET_SERVICES
     * @see PackageManager#GET_SHARED_LIBRARY_FILES
     * @see PackageManager#GET_SIGNATURES
     * @see PackageManager#GET_URI_PERMISSION_PATTERNS
     * @see PackageManager#MATCH_DISABLED_COMPONENTS
     * @see PackageManager#MATCH_DISABLED_UNTIL_USED_COMPONENTS
     * @see PackageManager#MATCH_UNINSTALLED_PACKAGES
     **/
    @Override
    public List<PackageInfo> getInstalledPackages(int flags, int userId) {
        return super.getInstalledPackages(flags, userId);
    }

    /**
     * Return a List of all application packages that are installed on the
     * device. If flag GET_UNINSTALLED_PACKAGES has been set, a list of all
     * applications including those deleted with {@code DONT_DELETE_DATA} (partially
     * installed apps with data directory) will be returned.
     *
     * @param flags Additional option flags. Use any combination of
     * {@link PackageManager#GET_META_DATA}, {@link PackageManager#GET_SHARED_LIBRARY_FILES},
     * {@link PackageManager#MATCH_SYSTEM_ONLY}, {@link PackageManager#MATCH_UNINSTALLED_PACKAGES}
     * to modify the data returned.
     *
     * @return A List of ApplicationInfo objects, one for each installed application.
     *         In the unlikely case there are no installed packages, an empty list
     *         is returned. If flag {@code MATCH_UNINSTALLED_PACKAGES} is set, the
     *         application information is retrieved from the list of uninstalled
     *         applications (which includes installed applications as well as
     *         applications with data directory i.e. applications which had been
     *         deleted with {@code DONT_DELETE_DATA} flag set).
     *
     * @see PackageManager#GET_META_DATA
     * @see PackageManager#GET_SHARED_LIBRARY_FILES
     * @see PackageManager#MATCH_DISABLED_UNTIL_USED_COMPONENTS
     * @see PackageManager#MATCH_SYSTEM_ONLY
     * @see PackageManager#MATCH_UNINSTALLED_PACKAGES
     */
    @Override
    public List<ApplicationInfo> getInstalledApplications(int flags, int userId) {
        return super.getInstalledApplications(flags, userId);
    }

    /**
     * Retrieve overall information about an application package that is
     * installed on the system.
     *
     * @param packageName The full name (i.e. com.google.apps.contacts) of the
     *         desired package.
     * @param flags Additional option flags. Use any combination of
     *         {@link PackageManager#GET_ACTIVITIES}, {@link PackageManager#GET_CONFIGURATIONS},
     *         {@link PackageManager#GET_GIDS}, {@link PackageManager#GET_INSTRUMENTATION},
     *         {@link PackageManager#GET_INTENT_FILTERS}, {@link PackageManager#GET_META_DATA},
     *         {@link PackageManager#GET_PERMISSIONS}, {@link PackageManager#GET_PROVIDERS},
     *         {@link PackageManager#GET_RECEIVERS}, {@link PackageManager#GET_SERVICES},
     *         {@link PackageManager#GET_SHARED_LIBRARY_FILES}, {@link PackageManager#GET_SIGNATURES},
     *         {@link PackageManager#GET_URI_PERMISSION_PATTERNS}, {@link PackageManager#GET_UNINSTALLED_PACKAGES},
     *         {@link PackageManager#MATCH_DISABLED_COMPONENTS}, {@link PackageManager#MATCH_DISABLED_UNTIL_USED_COMPONENTS},
     *         {@link PackageManager#MATCH_UNINSTALLED_PACKAGES}
     *         to modify the data returned.
     *
     * @return A PackageInfo object containing information about the
     *         package. If flag {@code MATCH_UNINSTALLED_PACKAGES} is set and if the
     *         package is not found in the list of installed applications, the
     *         package information is retrieved from the list of uninstalled
     *         applications (which includes installed applications as well as
     *         applications with data directory i.e. applications which had been
     *         deleted with {@code DONT_DELETE_DATA} flag set).
     * @see PackageManager#GET_ACTIVITIES
     * @see PackageManager#GET_CONFIGURATIONS
     * @see PackageManager#GET_GIDS
     * @see PackageManager#GET_INSTRUMENTATION
     * @see PackageManager#GET_INTENT_FILTERS
     * @see PackageManager#GET_META_DATA
     * @see PackageManager#GET_PERMISSIONS
     * @see PackageManager#GET_PROVIDERS
     * @see PackageManager#GET_RECEIVERS
     * @see PackageManager#GET_SERVICES
     * @see PackageManager#GET_SHARED_LIBRARY_FILES
     * @see PackageManager#GET_SIGNATURES
     * @see PackageManager#GET_URI_PERMISSION_PATTERNS
     * @see PackageManager#MATCH_DISABLED_COMPONENTS
     * @see PackageManager#MATCH_DISABLED_UNTIL_USED_COMPONENTS
     * @see PackageManager#MATCH_UNINSTALLED_PACKAGES
     */
    @Override
    public PackageInfo getPackageInfo(String packageName, int flags, int userId) {
        return super.getPackageInfo(packageName, flags, userId);
    }

    /**
     * Retrieve all of the information we know about a particular
     * package/application.
     *
     * @param packageName The full name (i.e. com.google.apps.contacts) of an
     *         application.
     * @param flags Additional option flags. Use any combination of
     *         {@link PackageManager#GET_META_DATA}, {@link PackageManager#GET_SHARED_LIBRARY_FILES},
     *         {@link PackageManager#MATCH_SYSTEM_ONLY}, {@link PackageManager#MATCH_UNINSTALLED_PACKAGES}
     *         to modify the data returned.
     *
     * @return An {@link ApplicationInfo} containing information about the
     *         package. If flag {@code MATCH_UNINSTALLED_PACKAGES} is set and if the
     *         package is not found in the list of installed applications, the
     *         application information is retrieved from the list of uninstalled
     *         applications (which includes installed applications as well as
     *         applications with data directory i.e. applications which had been
     *         deleted with {@code DONT_DELETE_DATA} flag set).
     *
     * @see PackageManager#GET_META_DATA
     * @see PackageManager#GET_SHARED_LIBRARY_FILES
     * @see PackageManager#MATCH_DISABLED_UNTIL_USED_COMPONENTS
     * @see PackageManager#MATCH_SYSTEM_ONLY
     * @see PackageManager#MATCH_UNINSTALLED_PACKAGES
     */
    @Override
    public ApplicationInfo getApplicationInfo(String packageName, int flags, int userId) {
        return super.getApplicationInfo(packageName, flags, userId);
    }

    /**
     * Retrieve current operation state for one application.
     *
     * @param uid The uid of the application of interest.
     * @param packageName The name of the application of interest.
     * @param ops The set of operations you are interested in, or null if you want all of them.
     */
    @Override
    public List<AppOpsManager.PackageOps> getOpsForPackage(int uid, String packageName, int[] ops) {
        return super.getOpsForPackage(uid, packageName, ops);
    }

    /*@Override
    public void setMode(int code, int uid, String packageName, int mode) {
        super.setMode(code, uid, packageName, mode);
    }

    @Override
    public void resetAllModes(int reqUserId, String reqPackageName) {
        super.resetAllModes(reqUserId, reqPackageName);
    }

    @Override
    public int broadcastIntent(Intent intent, String requiredPermissions, int userId) {
        return super.broadcastIntent(intent, requiredPermissions, userId);
    }*/

    /**
     * Returns a "good" intent to launch a front-door activity in a package.
     * This is used, for example, to implement an "open" button when browsing
     * through packages.  The current implementation looks first for a main
     * activity in the category {@link Intent#CATEGORY_INFO}, and next for a
     * main activity in the category {@link Intent#CATEGORY_LAUNCHER}. Returns
     * <code>null</code> if neither are found.
     *
     * @param packageName The name of the package to inspect.
     *
     * @return A fully-qualified {@link Intent} that can be used to launch the
     * main activity in the package. Returns <code>null</code> if the package
     * does not contain such an activity, or if <em>packageName</em> is not
     * recognized.
     */
    public Intent getLaunchIntentForPackage(String packageName, int userId) {
        Intent intentToResolve = new Intent(Intent.ACTION_MAIN);
        intentToResolve.addCategory(Intent.CATEGORY_INFO);
        intentToResolve.setPackage(packageName);

        List<ResolveInfo> ris = queryIntentActivities(intentToResolve, 0, userId);
        if (ris == null || ris.size() <= 0) {
            // reuse the intent instance
            intentToResolve.removeCategory(Intent.CATEGORY_INFO);
            intentToResolve.addCategory(Intent.CATEGORY_LAUNCHER);
            intentToResolve.setPackage(packageName);
            ris = queryIntentActivities(intentToResolve, 0, userId);
        }
        if (ris == null || ris.size() <= 0) {
            return null;
        }
        Intent intent = new Intent(intentToResolve);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setClassName(ris.get(0).activityInfo.packageName,
                ris.get(0).activityInfo.name);
        return intent;
    }

    private List<ResolveInfo> queryIntentActivities(Intent intent, int flags, int userId) {
        List<ResolveInfo> list = queryIntentActivities(intent, intent.getType(), flags, userId);
        if (list == null) {
            return Collections.emptyList();
        }
        return list;
    }
}
