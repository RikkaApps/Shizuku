# Shizuku

## Background

开发需要root权限的应用程序时,最常见的方法是在`su shell`中运行一些命令.例如通过使用`pm enable / disable`命令来启用/禁用组件.

这种方法主要的缺点有:

1. **极慢**(使用多个进程）
2. 需要处理文本(**并不稳定**)
3. 使用方式受限于可用命令
4. 即使adb具有足够的权限,该应用也需要root权限才能运行

Shizuku使用完全不同的方式,请参阅下面的详细说明.

## User guide & Download

至[`https://shizuku.rikka.app/`](https://shizuku.rikka.app/).

## How does Shizuku work?

首先,我们需要知道应用程序如何使用系统API. 例如,如果应用程序需要安装应用程序,通常我们应该使用`PackageManager#getInstalledPackages()`.这实际上是应用程序进程和系统服务进程的进程间通信(IPC)进程,只是Android框架为我们完成了内部工作.

Android使用`binder`来执行这种类型的IPC. `Binder`允许服务进程获取客户端的uid和pid,以便系统服务进程可以检查应用程序是否具有执行操作的权限.

通常，如果有供应用使用的`Manager`(例如`PackageManager`),则系统服务进程中也应该有一个`binder`(例如`PackageManagerService`).我们可以简单地认为,如果应用程序拥有`service`的`binder`,那么它可以与`service`进行通信.进程将在启动时接收系统服务的`binder`.

Shizuku指导用户先使用`root`或`adb`运行进程Shizuku服务.当应用程序启动时,至Shizuku服务器的`binder`也将发送至该应用程序.

Shizuku主要像中间人一样从应用程序接收请求,发送到系统服务,然后将结果发送回去.您可以在`moe.shizuku.server.ShizukuService`类和`moe.shizuku.api.ShizukuBinderWrapper`类中查看`transactRemote`方法.

因此,我们可以使用具有更高权限的系统API.对于应用程序来说,它与直接使用系统API相差无几.

## Use Shizuku in your app

如需完整的实例,请参阅[此处](https://github.com/RikkaApps/Shizuku/tree/master/sample).

1. 添加依赖项

   ```java
   maven { url 'https://dl.bintray.com/rikkaw/Shizuku' }
   ```
   
   ```java
   //以<latest version>替换
   implementation 'moe.shizuku.privilege:api:<latest version>'
   ```

[![Download](https://api.bintray.com/packages/rikkaw/Shizuku/api/images/download.svg)](https://bintray.com/rikkaw/Shizuku/api/_latestVersion)

   
2. 添加 `ShizukuProvider`

   添加至 `AndroidManifest.xml`

   ```java
   <provider
        android:name="moe.shizuku.api.ShizukuProvider"
        android:authorities="${applicationId}.shizuku"
        android:multiprocess="false"
        android:enabled="true"
        android:exported="true"
        android:permission="android.permission.INTERACT_ACROSS_USERS_FULL" />
   ```

3. 发起权限请求

像请求其它[运行时权限](https://developer.android.com/distribute/best-practices/develop/runtime-permissions)一样请求 `moe.shizuku.manager.permission.API_V23` 权限 .

4. 使用

请参阅[此处](https://github.com/RikkaApps/Shizuku/tree/master/sample).

### Attention

1. ADB的部分权限通常是受限的

ADB仅具有有限的权限,并且在各个系统上都有所不同,您可以在[此处](https://github.com/aosp-mirror/platform_frameworks_base/blob/master/packages/Shell/AndroidManifest.xml)看到授予ADB的权限.
   
在调用API之前,您可以使用`ShizukuService#getUid`检查Shizuku是否正在使用基于ADB,或者使用`ShizukuService#checkPermission`检查服务是否具有足够的权限.

2. `Android9`后隐式API调用将受限

自Android 9起,对于常规应用程序,隐式API的调用受到限制,请使用如<https://github.com/tiann/FreeReflection>的其他方法.

3. Android 8.0 & ADB

目前,Shizuku服务获取应用程序进程的方式是将`IActivityManager#registerProcessObserver` 和 `IActivityManager#registerUidObserver`(26+)结合起来,以确保在应用程序启动时将被授权于相应进程.但是,在`API26`上,ADB缺少使用`registerUidObserver`的权限,因此,如果您需要在可能不会启动于Activity的进程中使用Shizuku,建议您通过启动透明活动来触发绑定程序.
   
4. 当需要直接使用`transactRemote`时

- 在不同的Android版本中,API可能会有所不同,请务必仔细检查.另外,`android.app.IActivityManager`在API 26及更高版本中具有AIDL形式而`android.app.IActivityManager$Stub`仅在API 26中存在.

- `SystemServiceHelper.getTransactionCode`可能无法获取正确的交易代码,例如API 25上不是 `android.content.pm.IPackageManager$Stub.TRANSACTION_getInstalledPackages`,而是`android.content.pm.IPackageManager$Stub.TRANSACTION_getInstalledPackages_47`(已经解决了这种情况,但不排除可能有其他情况).`ShizukuBinderWrapper`方法则不会遇到此问题.

## Developing Shizuku itself

The `:server:assembleDebug` task generates debuggable server. You can attach debugger to `shizuku_server` to debug server.
`:server:assembleDebug`任务用于生成可调试服务,您可以将调试器附加到`shizuku_server`以调试服务.

## License
Copyright by [RikkaApps](https://github.com/RikkaApps),translatated documents copyright by [CharlotteFallices](https://github.com/CharlotteFallices) under [Artistic License 2.0](http://www.perlfoundation.org/attachment/legal/artistic-2_0.txt)
