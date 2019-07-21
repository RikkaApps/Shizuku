# 如何使用 Shizuku（開發者）

::: tip
下面的內容請結合 sample 閱讀。
:::

1. 新增依賴
   
   ```
   implementation 'moe.shizuku.privilege:api:3.0.0-alpha10'
   ```

   版本號可在 <https://bintray.com/rikkaw/Shizuku/> 檢視。

   後面需要用到的許可權宣告包含在依賴中，不需要手動新增。
   
2. 取得 binder

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

   當使用者應用程序啟動時，Shizuku v3 服務使用該 Provider 傳送 binder 給程式。

   通常，當進入你自己的 Activity 時，該 provider 中的程式碼應該已被執行（即已經收到 binder），但還是建議你實現一個簡單的等待邏輯，詳細參考 sample。

3. 授權

   在使用收到的 binder 之前先需要確認自身許可權。

   對 API 23 及以上，直接使用了[執行時許可權機制](https://developer.android.com/distribute/best-practices/develop/runtime-permissions)，只需要保證先取得 `moe.shizuku.manager.permission.API_V23` 許可權即可。

   對 API 23 以下，需要啟動 Shizuku app 取得 token，具體流程請參考 sample。

4. 使用：binder transact（使用 `ShizukuBinderWrapper`）

   API `3.0.0-alpha8` 起增加了 `ShizukuBinderWrapper`，大致使用方法如下，完整用法及其他參考請參看 sample。

   ```
   IPackageManager pm = IPackageManager.Stub.asInterface(new ShizukuBinderWrapper(SystemServiceHelper.getSystemService("package")));
   pm.getInstalledPackages(0, 0);
   ```

5. 使用：binder transact（使用 `transactRemote`，**不推薦**）

   > 這種方式使用起來更加麻煩且更容易遇到問題（最後的「特別注意」），更推薦使用上面的方法。

   請參看 sample。

6. 使用：直接執行指令
     
   請參看 sample。

## 特別注意

1. adb 許可權有限

   adb 所擁有的許可權有限，且不同系統版本也有所差別。adb 所擁有的許可權可以[在此](https://github.com/aosp-mirror/platform_frameworks_base/blob/master/packages/Shell/AndroidManifest.xml)查閱。
   
   在呼叫 API 前，你可以先使用 `ShizukuService#getUid` 檢查 Shizuku 是否執行在 adb 使用者， `ShizukuService#checkPermission` 檢查是否有許可權。

2. Android 9 hidden api 問題

   從 Android 9 起，應用使用隱藏 API 受限。

   目前啟動 Shizuku 時會嘗試使用 `setting put global hidden_api_blacklist_exemptions *`，但在部分裝置上似乎無效，請自行使用其他方式（如 <https://github.com/tiann/FreeReflection>）。

3. SELinux 問題

   目前執行在 root 下的 Shizuku 會將 context 設為與 adb shell 相同（`u:r:shell:s0`）。

4. 多程序程式

   對於多程序的程式，請在使用 Shizuku 前執行 `ShizukuMultiProcessHelper#initialize` 來從 `ShizukuBinderReceiveProvider` 所在程序取得 binder。另外由於 `ShizukuBinderReceiveProvider` 需要被其他程序啟動，建議將 `ShizukuBinderReceiveProvider` 所在程序（`android:process`）指定為與你的程式中最長時間執行的程序的相同。

5. Android 8.0 & adb

   目前 Shizuku v3 服務獲取應用程序建立的方式是組合 `IActivityManager#registerProcessObserver` 與 `IActivityManager#registerUidObserver`（26 及以上），可以保證應用程序啟動時會被髮送 binder。但在 API 26 上，adb 缺少許可權無法使用 `registerUidObserver`，因此如果你需要在可能不是由 Activity 啟動的程序中使用 Shizuku，建議使用啟動透明 Activity 的方式觸發傳送 binder。
   
5. 請勿濫用「直接執行指令」指令功能

6. 直接使用 `transactRemote` 需要注意

   * 不同 Android 版本下 API 可能不同，請務必仔細檢查。此外，`android.app.IActivityManager` 在 API 26 及以後才存在 aidl 形式， `android.app.IActivityManager$Stub` 只在 API 26 以上存在。

   * `SystemServiceHelper.getTransactionCode` 可能不能取得正確的 transaction code，比如在 API 25 上不存在 `android.content.pm.IPackageManager$Stub.TRANSACTION_getInstalledPackages` 而存在 `android.content.pm.IPackageManager$Stub.TRANSACTION_getInstalledPackages_47`（這種情況已處理，但不排除還可能有其他情況）。使用 `ShizukuBinderWrapper` 方式不會遇到此問題。
