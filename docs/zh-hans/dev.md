# 如何使用 Shizuku（开发者）

::: tip
下面的内容请结合 sample 阅读。
:::

1. 添加依赖
   
   ```
   implementation 'moe.shizuku.privilege:api:3.0.0-alpha10'
   ```

   版本号可在 <https://bintray.com/rikkaw/Shizuku/> 查看。

   后面需要用到的权限声明包含在依赖中，不需要手动添加。
   
2. 获取 binder

   在你的 AndroidManifest.xml 中加入

   ```
   <provider
        android:name="moe.shizuku.api.ShizukuBinderReceiveProvider"
        android:authorities="${applicationId}.shizuku"
        android:multiprocess="false"
        android:enabled="true"
        android:exported="true"
        android:permission="android.permission.INTERACT_ACROSS_USERS_FULL" />
   ```

   当使用者应用进程启动时，Shizuku v3 服务使用该 Provider 发送 binder 给应用。

   通常，当进入你自己的 Activity 时，该 provider 中的代码应该已被执行（即已经收到 binder），但还是建议你实现一个简单的等待逻辑，详细参考 sample。

3. 授权

   在使用收到的 binder 之前先需要确认自身权限。

   对 API 23 及以上，直接使用了[运行时权限机制](https://developer.android.com/distribute/best-practices/develop/runtime-permissions)，只需要保证先获取 `moe.shizuku.manager.permission.API_V23` 权限即可。

   对 API 23 以下，需要启动 Shizuku app 获取 token，具体流程请参考 sample。

4. 使用：binder transact（使用 `ShizukuBinderWrapper`）

   API `3.0.0-alpha8` 起增加了 `ShizukuBinderWrapper`，大致使用方法如下，完整用法及其他参考请参看 sample。

   ```
   IPackageManager pm = IPackageManager.Stub.asInterface(new ShizukuBinder(SystemServiceHelper.getSystemService("package")));
   pm.getInstalledPackages(0, 0);
   ```

5. 使用：binder transact（使用 `transactRemote`，**不推荐**）

   > 这种方式使用起来更加麻烦且更容易遇到问题（最后的“特别注意”），更推荐使用上面的方法。

   请参看 sample。

6. 使用：直接运行指令
     
   请参看 sample。

## 特别注意

1. adb 权限有限

   adb 所拥有的权限有限，且不同系统版本也有所差别。adb 所拥有的权限可以[在此](https://github.com/aosp-mirror/platform_frameworks_base/blob/master/packages/Shell/AndroidManifest.xml)查阅。
   
   在调用 API 前，你可以先使用 `ShizukuService#getUid` 检查 Shizuku 是否运行在 adb 用户， `ShizukuService#checkPermission` 检查是否有权限。

2. Android 9 hidden api 问题

   从 Android 9 起，应用使用隐藏 API 受限。

   目前启动 Shizuku 时会尝试使用 `setting put global hidden_api_blacklist_exemptions *`，但在部分设备上似乎无效，请自行使用其他方式（如 <https://github.com/tiann/FreeReflection>）。

3. SELinux 问题

   目前运行在 root 下的 Shizuku 会将 context 设为与 adb shell 相同（`u:r:shell:s0`）。

4. 多进程应用

   对于多进程的应用，请在使用 Shizuku 前执行 `ShizukuMultiProcessHelper#initialize` 来从 `ShizukuBinderReceiveProvider` 所在进程获取 binder。另外由于 `ShizukuBinderReceiveProvider` 需要被其他进程启动，建议将 `ShizukuBinderReceiveProvider` 所在进程（`android:process`）指定为与你的应用中最长时间运行的进程的相同。

5. Android 8.0 & adb

   目前 Shizuku v3 服务获取应用进程建立的方式是组合 `IActivityManager#registerProcessObserver` 与 `IActivityManager#registerUidObserver`（26 及以上），可以保证应用进程启动时会被发送 binder。但在 API 26 上，adb 缺少权限无法使用 `registerUidObserver`，因此如果你需要在可能不是由 Activity 启动的进程中使用 Shizuku，建议使用启动透明 Activity 的方式触发发送 binder。
   
5. 请勿滥用“直接运行指令”指令功能

6. 直接使用 `transactRemote` 需要注意

   * 不同 Android 版本下 API 可能不同，请务必仔细检查。此外，`android.app.IActivityManager` 在 API 26 及以后才存在 aidl 形式， `android.app.IActivityManager$Stub` 只在 API 26 以上存在。

   * `SystemServiceHelper.getTransactionCode` 可能不能获得正确的 transaction code，比如在 API 25 上不存在 `android.content.pm.IPackageManager$Stub.TRANSACTION_getInstalledPackages` 而存在 `android.content.pm.IPackageManager$Stub.TRANSACTION_getInstalledPackages_47`（这种情况已处理，但不排除还可能有其他情况）。使用 `ShizukuBinderWrapper` 方式不会遇到此问题。