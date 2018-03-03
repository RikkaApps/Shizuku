# Shizuku Manager

## 这个应用为何而生？

在 Android 系统中，许多 API 需要应用是系统应用或拥有框架签名才能调用。

一些需要 root 权限才能使用的应用可能仅仅只是需要调用那些的 API。由于该限制，他们通常会采取一些不需要直接调用API的变通方式，但执行速度通常会比直接调用 API 慢很多。
为了更好的用户体验，我们需要一个更快更好的方案。Shizuku Server 就此诞生，同时也让免 root 使用成为可能（通过 adb）。

## 什么是 Shizuku Server？

Shizuku Server 是一个通过 root 或者 adb 启动的进程，普通应用可以通过与该进程交互来调用自身无权调用的 API。调用速度与直接调用 API 几乎没有差距。

## 什么是 Shizuku Manager？

Shizuku Manager 用于启动 Shizuku Server 及管理使用该服务的应用。为了避免 Shizuku Server 被恶意应用调用，普通应用需要先向 Shizuku Manager 请求授权后才能使用。若需要在不同用户下安装使用服务的应用，需要在对应用户中安装 Shizuku Manager 才能授权。

## 如何启动 Shizuku server？

如果您已经 root 您的设备，直接通过 Shizuku Manager 启动即可。

如果您没有 root 你的设备，您也可以遵照 Shizuku Manager 中的指引，通过 adb 启动服务。使用 adb 并不难，网络上有不少教程可以帮助您学会使用它。

以下是一段展示如何通过设置 Shizuku Server 服务的视频：

<https://youtu.be/Nk24nhn0Jcs>

# 开发者如何适配？

1. 添加依赖
```
    implementation 'moe.shizuku.privilege:api-base:9'
    implementation 'moe.shizuku.privilege:api-26:4'
```
详细版本号可在 https://bintray.com/rikkaw/Shizuku/ 查看。

需注意，如果要使用的 API 在不同 Android Version 里不一致的话，就需要添加多个形如 `api-24`、`api-25`、`api-26` 这样的依赖。

2. 初始化

Shizuku Manager 需要在 `Application.onCreate()` 或其他初始化的时机里调用 `ShizukuClient.initialize(context);` 以在有权限时更新 token。

3. 授权

第一次调用之前，需要用户进行授权操作。

详细的示例代码见 <https://github.com/RikkaApps/Shizuku/blob/master/sample/src/main/java/moe/shizuku/sample/MainActivity.java>。其中步骤简述如下：

在 manifest 中添加权限
```
<uses-permission android:name="moe.shizuku.manager.permission.API_V23"/>
<uses-permission android:name="moe.shizuku.manager.permission.API"/>
```
其中 API_V23 用于 6.0 以上的动态 Android 权限，API 用于 6.0 以下。

对 6.0 以上，首先调用系统流程申请权限：
```
ActivityCompat.requestPermissions(this, new String[]{ShizukuClient.PERMISSION_V23}, REQUEST_CODE_PERMISSION);
```
在`onRequestPermissionsResult()`中回调确认后，再调用 `ShizukuClient.requestAuthorization(this);` 即可在 `onActivityResult()` 中收到并存储结果 token。
```
 @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ShizukuClient.REQUEST_CODE_AUTHORIZATION:
                if (resultCode == ShizukuClient.AUTH_RESULT_OK) {
                    ShizukuClient.setToken(data);
                    // done.
                } else {
                    // user denied or error.
                }
                return;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }
```

3. 调用

以冻结/解冻应用为例，我们需要调用的是 `IPackageManager.setApplicationEnabledSetting` 所以直接使用 `ShizukuPackageManagerV26.setApplicationEnabledSetting` 即可，参数和返回值都与原始方法相同。

如果原始方法在不同版本 Android 上的参数或名称不同，则需要自己处理并调用不同版本的 Shizuku API。


