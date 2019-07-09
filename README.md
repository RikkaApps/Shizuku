# Shizuku

Help normal apps uses system APIs directly with adb/root privileges with a Java process started with app_process.

The name Shizuku coms from [a character](https://www.pixiv.net/member_illust.php?mode=medium&illust_id=75586127).

[中文说明](https://github.com/RikkaApps/Shizuku/blob/master/README.zh-CN.md)

# Why is Shizuku born?

The birth of Shizuku has two main purposes.

1. Provide a convenient way to use system APIs
2. Convenient for the development of some apps that only requires adb permissions

# Shizuku vs. "Old school" method

## "Old school" method

For example, to enable/disable components, some apps that require root privileges execute `pm disable` directly in `su`.

1. Execute `su`
2. Execute `pm disable`
3. (pre-Pie) Start the Java process with app_process ([see here](https://android.googlesource.com/platform/frameworks/base/+/oreo-release/cmds/pm/pm))
4. (Pie+) Execute the native program `cmd` ([see here](https://android.googlesource.com/platform/frameworks/native/+/pie-release/cmds/cmd/))
5. Process the parameters, interact with the system server through the binder, and process the result to output the text result.

Each of the "Execute" means a new process creation, su internally uses sockets to interact with the su daemon, and a lot of time and performance are consumed in such process. (Some poorly designed app will even execute `su` **every time** for each command)

The disadvantages of this type of method are:

1. **Extremely slow**
2. Need to process the text to get the result
3. Features are subject to available commands
4. Even if adb has sufficient permissions, the app requires root privileges to run

## Shizuku method

The Shizuku app will direct the user to run a process (Shizuku service process) using root or adb.

1. When the app process starts, the Shizuku service process sends the binder to the app process.
2. The app interacts with the Shizuku service through the binder, and the Shizuku service process interacts with the system server through the binder.

The advantages of Shizuku are:

1. Minimal extra time and performance consumption
2. It is almost identical to the direct invocation API experience (app developers only need to add a small amount of code)

# Apps using Shizuku

[View list](https://github.com/RikkaApps/Shizuku/blob/master/APPS.md)

If you are an app developer (or have obtained developer's consent), you can add apps using Shizuku to the list using pull request.

# As a user, how to use Shizuku?

* rooted devices

  Start directly in Shizuku app.

* not root devices

  Follow the instructions in Shizuku app to start the service through adb. Using adb is not difficult, there are many tutorials on the web that can help you learn to use it.

  Here's an video showing how to set up Shizuku:

  <https://youtu.be/Nk24nhn0Jcs>

# As a developer, how to use Shizuku?

## Usage

**Please read the following content in conjunction with sample.**

1. Add dependency

   ```
   Implementation 'moe.shizuku.privilege:api:3.0.0-alpha10'
   ```

   The version numbers can be found at https://bintray.com/rikkaw/Shizuku/.

   The permission declarations that need to be used later are included in the dependency, so you do not need to be add them manually.
   
2. Get the binder

   Add to your AndroidManifest.xml

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
   IPackageManager pm = IPackageManager.Stub.asInterface(new ShizukuBinder(SystemServiceHelper.getSystemService("package")));
   pm.getInstalledPackages(0, 0);
   ```

5. Use: binder transact (use `transactRemote`, **not recommended**)

   > This method is more cumbersome to use and more prone to problems (see  "Attention"), it is recommended to use the method above.

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
