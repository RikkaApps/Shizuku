---
home: true
heroImage: /logo.png
actionText: Get Started →
actionLink: /guide/
---

<div style="text-align: center">
  <Bit/>
</div>

<div class="features">
  <div class="feature">
    <h2>Use system APIs elegantly</h2>
    <p>Forget about root shell, you can use APIs directly "as a system app". Also, using Shizuku is significantly faster.</p>
  </div>
  <div class="feature">
    <h2>Support adb usage</h2>
    <p>If your "root required app" only needs adb permission, you can easily expand the audience by using Shizuku.</p>
  </div>
  <div class="feature">
    <h2>Save your time</h2>
    <p>Shizuku has detailed documentation to guide users. Only you have to do is let users install Shizuku.</p>
  </div>
</div>

### As Easy as you are a system app

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
**Note**

There a few more steps to do, like checking permission or if Shizuku is running.
:::