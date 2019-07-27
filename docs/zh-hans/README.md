---
home: true
heroImage: /logo.png
actionText: 快速上手 →
actionLink: /zh-hans/guide/
---

<div style="text-align: center">
  <Bit/>
</div>

<div class="features">
  <div class="feature">
    <h2>优雅地使用系统 API</h2>
    <p>忘掉 root shell 吧，你可以直接「像系统应用一样」直接使用系统 API。此外，使用 Shizuku 要快得多。</p>
  </div>
  <div class="feature">
    <h2>支持 adb 使用</h2>
    <p>如果你的「需要 root 的应用」只需要 adb 权限，则可以使用 Shizuku 轻松地扩大用户群体。</p>
  </div>
  <div class="feature">
    <h2>节省时间</h2>
    <p>Shizuku 有详细的文档引导用户，你只需要让用户安装 Shizuku。</p>
  </div>
</div>

### 就像是系统应用一样简单

```java
private static final IPackageManager PACKAGE_MANAGER = IPackageManager.Stub.asInterface(
    new ShizukuBinderWrapper(SystemServiceHelper.getSystemService("package")));

public static void grantRuntimePermission(String packageName, String permissionName, int userId) {
    try {
        PACKAGE_MANAGER.grantRuntimePermission(packageName, permissionName, userId);
    } catch (RemoteException tr) {
        throw new RuntimeException(tr.getMessage(), tr);
    }
}
```

::: tip
**注意**

还有一些步骤要做，比如检查权限或 Shizuku 是否正在运行。
:::