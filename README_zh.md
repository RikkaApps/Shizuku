# Shizuku
中文版译者： @CharlotteFallices (#75), @RikkaW, @haruue

## 背景

开发需要 root 权限的应用程序时，最常见的方法是在 su shell 中运行一些命令。例如通过使用 `pm enable / disable` 命令来启用/禁用组件。

这种方法主要的缺点有：

1. **极慢**（需要创建多个进程）
2. 需要处理文本（**非常不可靠**）
3. 能做的事情受限于可用的命令
4. 即使 adb 具有足够的权限，也需要 root 权限才能运行

Shizuku 使用完全不同的方式，请参阅下面的详细说明。

## 用户指南 & 下载

<https://shizuku.rikka.app/>

## Shizuku 是如何工作的？

首先，我们需要知道应用程序如何使用系统 API。例如，如果应用程序需要安装应用程序，通常我们应该使用 `PackageManager#getInstalledPackages()`。这实际上是应用程序进程和系统服务进程的进程间通信（IPC），最终的内部工作由 Android 框架为我们完成。

Android 使用 `binder` 来执行这种类型的 IPC。在 `binder` 通信中，服务端可以获取客户端的 uid 和 pid，因此系统服务进程可以检查应用程序是否具有执行相应操作的权限。

通常来说，如果有可供应用程序使用的「Manager」（例如 `PackageManager`），则系统服务进程中也应该有一个对应的「Service」（例如 `PackageManagerService`）。我们可以这样认为：如果应用程序拥有某个「Service」 的 `binder`，那么它就能与对应的「Service」通信。应用进程在启动的时候就会获得系统服务的 `binder`。

Shizuku 首先引导用户使用 root 或者 adb 启动一个特殊的进程——Shizuku 服务。当应用程序启动时，Shizuku 服务的 `binder` 也会被发送给应用程序。

Shizuku 提供的最重要的功能，就是像中间人一样接收应用程序的请求，将该请求转发给系统服务，然后将系统服务的结果返回给应用程序。如需了解更多，请参阅 `moe.shizuku.server.ShizukuService` 类中的 `transactRemote` 方法，以及 `moe.shizuku.api.ShizukuBinderWrapper` 类。

综上，我们实现了使用高权限调用系统 API 的目标。并且对于应用程序来说，这与直接调用系统 API 相差无几。

## 在你的应用程序中使用 Shizuku

注意，有些细节没有在下面提及。请参阅 [sample](https://github.com/RikkaApps/Shizuku/tree/master/sample)。

1. 添加依赖项

   ```groovy
   maven { url 'https://dl.bintray.com/rikkaw/Shizuku' }
   ```

   ```groovy
   // 将 <latest version> 替换为最新版本
   implementation 'moe.shizuku.privilege:api:<latest version>'
   ```

   [![Download](https://api.bintray.com/packages/rikkaw/Shizuku/api/images/download.svg)](https://bintray.com/rikkaw/Shizuku/api/_latestVersion)


2. 添加 `ShizukuProvider`

   将这些代码添加至你的项目的 `AndroidManifest.xml`

   ```xml
   <provider
        android:name="moe.shizuku.api.ShizukuProvider"
        android:authorities="${applicationId}.shizuku"
        android:multiprocess="false"
        android:enabled="true"
        android:exported="true"
        android:permission="android.permission.INTERACT_ACROSS_USERS_FULL" />
   ```

3. 请求权限

   像请求其它 [运行时权限](https://developer.android.com/distribute/best-practices/develop/runtime-permissions) 一样请求 `moe.shizuku.manager.permission.API_V23` 权限。

4. 使用 Shizuku 调用特权 API

   请参阅 sample。

### 特别注意

1. ADB 的权限是有限的

   ADB 仅具有有限的权限，并且在各个版本的系统上会有所不同。您可以在 [此处](https://github.com/aosp-mirror/platform_frameworks_base/blob/master/packages/Shell/AndroidManifest.xml) 看到授予给 ADB 的权限.

   在调用 API 之前，您可以通过 `ShizukuService#getUid` 方法检查 Shizuku 是否在 ADB 模式运行，或者通过 `ShizukuService#checkPermission` 方法检查 Shizuku 服务是否具有足够的权限。

2. Android 9 之后的隐藏 API 限制

   从 Android 9 开始，Android 对常规应用能使用的非 SDK 接口实施了限制。请自行使用别的方式绕过此限制（例如 <https://github.com/tiann/FreeReflection>）。

3. Android 8.0 & ADB

   为了获知应用程序的创建，Shizuku 服务目前使用的方式是结合 `IActivityManager#registerProcessObserver` 以及 `IActivityManager#registerUidObserver` (26 及以上) 来保证能在应用程序启动时收到 binder。然而，在 API 26 上，ADB 缺少调用 `registerUidObserver` 所需的权限。因此，如果你需要在可能不是由 Activity 启动的进程中使用 Shizuku，建议启动一个透明的 Activity 来触发发送 binder。

4. 直接使用 `transactRemote` 需要注意的事项

   * 不同 Android 版本的 API 可能不同，请务必仔细检查。此外， `android.app.IActivityManager` 只在 API 26 及以后才存在 aidl 形式， 因此 `android.app.IActivityManager$Stub` 只在 API 26 以上存在。

   * `SystemServiceHelper.getTransactionCode` 可能不能获得正确的 transaction code，比如在 API 25 上不存在 `android.content.pm.IPackageManager$Stub.TRANSACTION_getInstalledPackages` 而存在 `android.content.pm.IPackageManager$Stub.TRANSACTION_getInstalledPackages_47`（这种情况已处理，但不排除还可能有其他情况）。使用 `ShizukuBinderWrapper` 方式不会遇到此问题。

## 开发 Shizuku 自身

使用 `:server:assembleDebug` task 来生成可调试的 Shizuku 服务。 你可以将调试器附加到 `shizku_server` 进程上来调试 Shizuku 服务。


