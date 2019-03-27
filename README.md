# Shizuku Manager

## 这个应用为何而生？

这个应用的诞生主要有两大目的。

1. 提供一个方便地使用系统服务方式
2. 为部分只需要 adb 权限的应用开发提供便利

以启用/禁用组件为例，一些需要 root 权限的应用可能会采取直接在 su 中执行 `pm disable` 的做法，这样做的劣势在于需要处理文本来得到返回结果，以及速度比不上直接使用 API。

启用/禁用组件实际对应 `android.content.pm.IPackageManager#setComponentEnabledSetting`。如果使用 Shizuku，过程就会变为应用直接与运行在 root 或是 adb 的 Shizuku Server 交互，Shizuku 直接调用 API 并原原本本地返回结果给应用。

另外，仅有 adb 拥有 `setComponentEnabledSetting` 所需要的权限 `android.permission.CHANGE_COMPONENT_ENABLED_STATE`，adb 使用也变为可能。

# 作为普通用户，如何使用 Shizuku？

如果您已经 root 您的设备，直接通过 Shizuku Manager 启动即可。

如果您没有 root 你的设备，您也可以遵照 Shizuku Manager 中的指引，通过 adb 启动服务。使用 adb 并不难，网络上有不少教程可以帮助您学会使用它。

以下是一段展示如何通过设置 Shizuku Server 服务的视频：

<https://youtu.be/Nk24nhn0Jcs>

# 作为开发者，如何使用 Shizuku？

要使用 Shizuku，你至少大致了解 Android 的 binder 机制。

Shizuku Manager app 会引导用户使用 root 或是 adb 方式运行一个进程（Shizuku 服务进程），使用者应用只需要与该进程交互即可实现以 root 或 adb 权限使用 API 的效果。

相对 Shizuku v2，Shizuku v3 （包含在 3.0.0 以上的 app 中）采用了完全不同的设计，直接使用 binder 与使用者应用交互，并提供了更加直接的 API。同时还添加了直接执行指令功能。另外相对 v2，授权流程更加简单。

下面的使用方式只包含 Shizuku v3。如果你需要开发全新的应用，请直接忽略 v2。

**下面的内容请结合 sample 阅读。**

1. 添加依赖
   
   ```
   implementation 'moe.shizuku.privilege:api:3.0.0-alpha6' // Shizuku v3
   ```

   详细版本号可在 https://bintray.com/rikkaw/Shizuku/ 查看。

   后面需要用到的权限声明包含在依赖中，不需要手动添加。
   
2. 获取 binder

   在你的 AndroidManifest.xml 中加入

   ```
   <provider
        android:name="moe.shizuku.api.BinderReceiveProvider"
        android:authorities="${applicationId}.shizuku"
        android:multiprocess="false"
        android:enabled="true"
        android:exported="true"
        android:permission="android.permission.INTERACT_ACROSS_USERS_FULL" />
   ```

   当使用者应用进程启动时，Shizuku v3 服务使用该 Provider 发送 binder 给应用。

   通常，当进入你自己的 Activity 时，该 provider 中的代码应该已被执行（即已经受到 binder），但还是建议你在你的 Activity 中实现一个简单的等待逻辑，详细参考 sample。

   对于多进程的应用，请在使用 Shizuku 前执行 `MultiProcessHelper#initialize` 来从 `BinderReceiveProvider` 所在进程获取 binder。另外由于 `BinderReceiveProvider` 需要被其他进程启动，建议将 `BinderReceiveProvider` 所在进程（`android:process`）指定为与你的应用中最长时间运行的进程的相同。

   目前 Shizuku v3 服务获取应用进程建立的方式是组合 `IActivityManager#registerProcessObserver` 与 `IActivityManager#registerUidObserver`（26 及以上），可以保证应用进程启动时会被发送 binder。但在 API 26 上，adb 缺少权限无法使用 `registerUidObserver`，因此如果你需要在 可能不是由 Activity 启动的进程 中使用 Shizuku，建议使用启动透明 Activity 的方式触发发送 binder。

3. 授权

   在使用收到的 binder 之前先需要确认自身权限是否足够。

   对 API 23 及以上，直接使用了运行时权限机制，只需要保证先获取 `moe.shizuku.manager.permission.API_V23` 权限即可。

   对 API 23 以下，需要额外一步启动 Shizuku Manager app 获取 token 的过程，具体流程请参考 sample。

4. 使用：binder transact
   
   **要使用 Shizuku，你需要了解你所要使用的 API 的这样的过程。**

   以 `PackageManager#getInstalledPackages` 为例，如果是在自身进程执行，最终会执行 `android.content.pm.IPackageManager$Stub` 中的这样的过程。

   ```
   Parcel data = Parcel.obtain();
   Parcel reply = Parcel.obtain();

   ParceledListSlice result;
   try {
       data.writeInterfaceToken("android.content.pm.IPackageManager");
       data.writeInt(flags);
       data.writeInt(userId);
       mRemote.transact(TRANSACTION_getInstalledPackages, data, reply, 0);
       reply.readException();
       if (0 != _reply.readInt()) {
            result = (ParceledListSlice)ParceledListSlice.CREATOR.createFromParcel(_reply);
       } else {
           result = null;
       }
   } finally {
       reply.recycle();
       data.recycle();
   }
   return result;
   ```

   在使用 Shizuku 时，需要执行下面这样的过程。

   ```
   Parcel data = Parcel.obtain();
   Parcel reply = Parcel.obtain();
   data.writeInterfaceToken(ShizukuApiConstants.BINDER_DESCRIPTOR);
   data.writeStrongBinder(SystemServiceHelper.getSystemService("package")); // 第一个是你希望在 Shizuku 服务进程使用的 binder
   data.writeInt(SystemServiceHelper.getTransactionCode("android.content.pm.IPackageManager", "getInstalledPackages")); // 第二个是 transact code
   // 原本 data parcel 的内容
   data.writeInterfaceToken("android.content.pm.IPackageManager");
   data.writeInt(flags);
   data.writeInt(userId);

   try {
       ShizukuService.transactRemote(data, reply, 0);
	   
	   // reply parcel 读法与原先一致
       reply.readException();
       if (reply.readInt() != 0) {
           //noinspection unchecked
           ParceledListSlice<PackageInfo> listSlice = ParceledListSlice.CREATOR.createFromParcel(reply);
           return listSlice.getList();
       }
       return null;
   } finally {
       data.recycle();
       reply.recycle();
   }
   return null;
   ```

   这样将原本 应用 -> IPackageManager 的过程变为了 应用 -> Shizuku 服务。Shizuku 服务会截取 data parcel 后半部分内容，直接使用应用传递来的 reply parcel 来对发来的 binder 执行 transact。 

   **完整用法及其他参考请参看 sample。**

   不同 Android 版本下 API 可能不同，请务必仔细检查。此外，`android.app.IActivityManager` 在 API 26 及以后才存在 aidl 形式， `android.app.IActivityManager$Stub` 只在 API 26 以上存在。

5. 使用：直接运行指令
     
   请参看 sample。

   为了避免 SELinux 问题，目前运行在 root 下的 Shizuku 会将 context 设为与 adb shell 相同，请务必注意。

   adb 与 root 权限相差较大，如果你需要开发 root 权限才可以使用的应用，建议不使用 Shizuku。

6. 其他使用方法及注意事项

   [adb 拥有的权限](https://github.com/aosp-mirror/platform_frameworks_base/blob/master/packages/Shell/AndroidManifest.xml)有限，可以先使用 `ShizukuService#getUid` 及 `ShizukuService#checkPermission` 检查是否为 adb 及是否有权限。

   关于 Android 9 hidden api 问题，目前启动 Shizuku 时会尝试使用 `setting put global hidden_api_blacklist_exemptions *`，但在部分设备上似乎无效，请自行使用其他方式。