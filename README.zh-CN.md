# Shizuku

帮助普通应用借助一个由 app_process 启动的 Java 进程直接以 adb 或 root 特权使用系统 API。

Shizuku 这个名字来自[这里](https://www.pixiv.net/member_illust.php?mode=medium&illust_id=75586127)。

# Shizuku 为何而生？

Shizuku 的诞生主要有两大目的：

1. 提供一个方便地使用系统 API 的方式
2. 为部分只需要 adb 权限的应用开发提供便利

# Shizuku 与“传统”做法对比

## “传统”做法

以启用/禁用组件为例，一些需要 root 权限的应用直接在 `su` 中执行 `pm disable`。

1. 执行 `su`
2. 执行 `pm disable`
3. (pre-Pie) 使用 app_process 启动 Java 进程（[参见此处](https://android.googlesource.com/platform/frameworks/base/+/oreo-release/cmds/pm/pm)）
4. (Pie+) 执行原生程序 `cmd`（[参见此处](https://android.googlesource.com/platform/frameworks/native/+/pie-release/cmds/cmd/)）
5. 处理参数，通过 binder 与 system server 交互，处理结果输出文字结果

其中每个“执行”都意味着新进程建立，su 内部使用 socket 与 su daemon 交互，大量的时间和性能被消耗在这样的过程中。（部分设计不佳的应用甚至会每次执行指令都执行一次 `su`）

此类做法的缺点在于：

1. **极慢**
2. 需要处理文本来获取结果
3. 功能受制于可用的指令
4. 即使 adb 有足够权限，应用也需要 root 权限才可使用

## Shizuku 做法

Shizuku app 会引导用户使用 root 或是 adb 方式运行一个进程（Shizuku 服务进程）。

1. 应用进程启动时 Shizuku 服务进程发送 binder 至应用进程
2. 应用通过该 binder 与 Shizuku 服务进程交互，Shizuku 服务进程通过 binder 与 system server 交互

Shizuku 的优点在于：

1. 极小额外时间及性能消耗
2. 与直接调用 API 体验几乎一致（应用开发者只许添加少量代码）

# 使用 Shizuku 的应用

[查看列表](https://github.com/RikkaApps/Shizuku/blob/master/APPS.md)

如果你是应用开发者（或已获得开发者同意），可以通过 pull request 的方式将使用 Shizuku 的应用加入列表。

# 作为普通用户，如何使用 Shizuku？

* 已 root 设备

  直接通过 Shizuku app 启动即可。

* 未 root 设备
  
  遵照 Shizuku app 中的指引，通过 adb 启动服务。使用 adb 并不难，网络上有不少教程可以帮助您学会使用它。

  以下是一段展示如何通过设置 Shizuku 视频：

  <https://youtu.be/Nk24nhn0Jcs>

# 作为开发者，如何使用 Shizuku？

## 如何使用

**下面的内容请结合 sample 阅读。**

1. 添加依赖
   
   ```
   implementation 'moe.shizuku.privilege:api:3.0.0-alpha10'
   ```

   版本号可在 https://bintray.com/rikkaw/Shizuku/ 查看。

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