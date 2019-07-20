# 简介

Shizuku 可以帮助普通应用借助一个由 app_process 启动的 Java 进程直接以 adb 或 root 特权使用系统 API。

> Shizuku 这个名字来自[这里](https://www.pixiv.net/member_illust.php?mode=medium&illust_id=75586127)。

## Shizuku 为何而生？

Shizuku 的诞生主要有两大目的：

1. 提供一个方便地使用系统 API 的方式
2. 为部分只需要 adb 权限的应用开发提供便利

## Shizuku 与“传统”做法对比

### “传统”做法

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

### Shizuku 做法

Shizuku app 会引导用户使用 root 或是 adb 方式运行一个进程（Shizuku 服务进程）。

1. 应用进程启动时 Shizuku 服务进程发送 binder 至应用进程
2. 应用通过该 binder 与 Shizuku 服务进程交互，Shizuku 服务进程通过 binder 与 system server 交互

Shizuku 的优点在于：

1. 极小额外时间及性能消耗
2. 与直接调用 API 体验几乎一致（应用开发者只许添加少量代码）