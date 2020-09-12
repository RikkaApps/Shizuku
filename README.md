# Shizuku

## Background

When developing apps that requires root, the most common method is to run some commands in the su shell. For example, there is app uses `pm enable/disable` command to enable/disable components.

This method has very big disadvantages:

1. **Extremely slow** (Multiple process creation)
2. Needs to process texts (**Super unreliable**)
3. The possibility is limited to available commands
4. Even if adb has sufficient permissions, the app requires root privileges to run

Shizuku uses a completely different way. See detailed description below.

## User guide & Download

<https://shizuku.rikka.app/>

## How does Shizuku work?

First, we need to talk about how app use system APIs. For example, if the app want to get installed apps, we all know we should use `PackageManager#getInstalledPackages()`. This is actually an interprocess communication (IPC) process of the app process and system server process, just the Android framework did the inner works for us.

Android uses `binder` to do this type of IPC. `Binder` allows server side to learn the uid and pid of client side, so that the system server can check if the app has the permission to do the operation.

Usually, if there is a "manager" (e.g., `PackageManager`) for apps to use, there should be a "service" (e.g., `PackageManagerService`) in the system server process. We can simply think if the app holds the `binder` of the "service", it can communicate with the "service". The app process will receive binders of system services on start.

Shizuku guide users to run a process, Shizuku server, with root or adb first. When the app starts, the `binder` to Shizuku server will also be sent to the app.

The most important feature Shizuku provides is something like be a middle man to receive requests from the app, sent to the system server, and send back the results. You can see `transactRemote` method in `moe.shizuku.server.ShizukuService` class, and `moe.shizuku.api.ShizukuBinderWrapper` class for the detail.

So that, we reached our goal, use system APIs with higher permission. And to the app, it is almost identical to the use system APIs directly.

## Use Shizuku in your app

Note, something is not mentioned below, please be sure to read the [sample](https://github.com/RikkaApps/Shizuku/tree/master/sample).

1. Add dependency

   ```
   maven { url 'https://dl.bintray.com/rikkaw/Shizuku' }
   ```
   
   ```
   // replace <latest version> to the version below
   implementation 'moe.shizuku.privilege:api:<latest version>'
   ```

    [![Download](https://api.bintray.com/packages/rikkaw/Shizuku/api/images/download.svg)](https://bintray.com/rikkaw/Shizuku/api/_latestVersion)

   
2. Add `ShizukuProvider`

   Add to your `AndroidManifest.xml`

   ```
   <provider
        android:name="moe.shizuku.api.ShizukuProvider"
        android:authorities="${applicationId}.shizuku"
        android:multiprocess="false"
        android:enabled="true"
        android:exported="true"
        android:permission="android.permission.INTERACT_ACROSS_USERS_FULL" />
   ```

3. Request permission

   Request `moe.shizuku.manager.permission.API_V23` permission like other [runtime permissions](https://developer.android.com/distribute/best-practices/develop/runtime-permissions).

4. Use

   See sample.

### Attention

1. Adb permissions are limited

   Adb has limited permissions, and different on various system versions. You can see permissions granted to adb [here](https://github.com/aosp-mirror/platform_frameworks_base/blob/master/packages/Shell/AndroidManifest.xml).
   
   Before calling the API, you can use `ShizukuService#getUid` to check if Shizuku is running user adb, or use `ShizukuService#checkPermission` to check if server has sufficient permissions.

2. Hidden API limitation from Android 9

   As of Android 9, the usage of the hidden APIs is limited for normal apps. Please use other methods (such as <https://github.com/tiann/FreeReflection>).

3. Android 8.0 & adb

   At present, the way Shizuku service gets the app process is to combine `IActivityManager#registerProcessObserver` and `IActivityManager#registerUidObserver` (26+) to ensure that the app process will be sent when the app starts. However, on API 26, adb lacks permissions to use `registerUidObserver`, so if you need to use Shizuku in a process that might not be started by an Activity, it is recommended to trigger the send binder by starting a transparent activity.
   
4. Direct use of `transactRemote` requires attention

   * The API may be different under different Android versions, please be sure to check it carefully. In addition, `android.app.IActivityManager` has the aidl form in API 26 and later, and `android.app.IActivityManager$Stub` exists only on API 26.

   * `SystemServiceHelper.getTransactionCode` may not get the correct transaction code, such as `android.content.pm.IPackageManager$Stub.TRANSACTION_getInstalledPackages` does not exist on API 25 and there is `android.content.pm.IPackageManager$Stub.TRANSACTION_getInstalledPackages_47` (this situation has been dealt with, but it is not excluded that there may be other circumstances). This problem is not encountered with the `ShizukuBinderWrapper` method.

## Developing Shizuku itself

The `:server:assembleDebug` task generates debuggable server. You can attach debugger to `shizuku_server` to debug server.