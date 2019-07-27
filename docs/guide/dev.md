# How to use Shizuku (Developer)

::: tip
Please read the following content in conjunction with [sample](https://github.com/RikkaApps/Shizuku/tree/master/sample).
:::

1. Add dependency

   ```
   Implementation 'moe.shizuku.privilege:api:3.0.0-alpha10'
   ```

   The version numbers can be found at <https://bintray.com/rikkaw/Shizuku/>.

   The permission declarations that need to be used later are included in the dependency, so you do not need to be add them manually.
   
2. Get the binder

   Add to your `AndroidManifest.xml`

   ```
   <provider
        android:name="moe.shizuku.api.ShizukuBinderReceiveProvider"
        android:authorities="${applicationId}.shizuku"
        android:multiprocess="false"
        android:enabled="true"
        android:exported="true"
        android:permission="android.permission.INTERACT_ACROSS_USERS_FULL" />
   ```

   When the user app process starts, the Shizuku service uses this Provider to send the binder to the app.

   Usually, when you enter your Activity, the code in the provider should have been executed (ie already received the binder), but it is recommended that you implement a simple wait logic, as detailed in sample.

3. Authorization

   Before using the received binder, you need to check the permission.

   For API 23 and above, the [runtime permission mechanism](https://developer.android.com/distribute/best-practices/develop/runtime-permissions) is used directly. Just make sure to get the `moe.shizuku.manager.permission.API_V23` permission first.

   For pre-API 23, it is required to start the Shizuku app to get the token. For the specific process, please refer to sample.

4. Use: binder transact (use `ShizukuBinderWrapper`)

   The `ShizukuBinderWrapper` has been added since API `3.0.0-alpha8`. The approximate usage is as follows. For full usage and other references, please refer to sample.

   ```
   IPackageManager pm = IPackageManager.Stub.asInterface(new ShizukuBinderWrapper(SystemServiceHelper.getSystemService("package")));
   pm.getInstalledPackages(0, 0);
   ```

5. Use: binder transact (use `transactRemote`, **not recommended**)

   ::: warning
   This method is more cumbersome to use and more prone to problems (see  "Attention"), it is recommended to use the method above.
   :::
   
   **See sample for complete usage and other references.**

6. Use: execute command directly
   
   Please refer to sample.

## Attention

1. Adb permissions are limited

   Adb has limited permissions, and different on various system versions. The permissions owned by adb can be viewed [here](https://github.com/aosp-mirror/platform_frameworks_base/blob/master/packages/Shell/AndroidManifest.xml).
   
   Before calling the API, you can use `ShizukuService#getUid` to check if Shizuku is running on the adb user, use `ShizukuService#checkPermission` to check if there is permission.

2. Android 9 hidden api problem

   As of Android 9, the usage of the hidden APIs is limited for normal apps.

   At the moment Shizuku is launched, it will try to use `setting put global hidden_api_blacklist_exemptions *`, but it not seems to work on some devices. Please use other methods (such as <https://github.com/tiann/FreeReflection>).

3. SELinux issues

   Currently, Shizuku running under root will set the context to be the same as adb shell (`u:r:shell:s0`).

4. Multi-process apps

   For multi-process apps, execute `ShizukuMultiProcessHelper#initialize` to get the binder from the process `ShizukuBinderReceiveProvider` is running before using Shizuku. In addition, since `ShizukuBinderReceiveProvider` needs to be started by other processes, it is recommended to specify the process of `ShizukuBinderReceiveProvider` (`android:process`) to be the same as the longest running process in your app.

5. Android 8.0 & adb

   At present, the way Shizuku service gets the app process is to combine `IActivityManager#registerProcessObserver` and `IActivityManager#registerUidObserver` (26+) to ensure that the app process will be sent when the app starts. However, on API 26, adb lacks permissions to use `registerUidObserver`, so if you need to use Shizuku in a process that might not be started by an Activity, it is recommended to trigger the send binder by starting a transparent activity.
   
6. Do not abuse the "execute command directly" feature

7. Direct use of `transactRemote` requires attention

   * The API may be different under different Android versions, please be sure to check it carefully. In addition, `android.app.IActivityManager` has the aidl form in API 26 and later, and `android.app.IActivityManager$Stub` exists only on API 26.

   * `SystemServiceHelper.getTransactionCode` may not get the correct transaction code, such as `android.content.pm.IPackageManager$Stub.TRANSACTION_getInstalledPackages` does not exist on API 25 and there is `android.content.pm.IPackageManager$Stub.TRANSACTION_getInstalledPackages_47` (this situation has been dealt with, but it is not excluded that there may be other circumstances). This problem is not encountered with the `ShizukuBinderWrapper` method.
