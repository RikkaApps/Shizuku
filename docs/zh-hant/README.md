---
home: true
heroImage: /logo.png
actionText: 快速上手 →
actionLink: /zh-hant/guide/
---

<div style="text-align: center">
  <Bit/>
</div>

<div class="features">
  <div class="feature">
    <h2>優雅地使用系統 API</h2>
    <p>忘掉 root shell 吧，你可以直接「像系統程式一樣」直接使用系統 API。此外，使用 Shizuku 要快得多。</p>
  </div>
  <div class="feature">
    <h2>支援 adb 使用</h2>
    <p>如果你的「需要 root 的程式」只需要 adb 權限，則可以使用 Shizuku 輕鬆地擴大用戶羣體。</p>
  </div>
  <div class="feature">
    <h2>節省時間</h2>
    <p>Shizuku 有詳細的文檔引導使用者，你只需要讓使用者安裝 Shizuku。</p>
  </div>
</div>

### 就像是系統程序一樣簡單

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

還有一些步驟要做，比如檢查權限或 Shizuku 是否正在執行。
:::