# 簡介

Shizuku 可以幫助普通程式藉助一個由 app_process 啟動的 Java 程序直接以 adb 或 root 特權使用系統 API。

Shizuku 這個名字來自[這裡](https://www.pixiv.net/member_illust.php?mode=medium&illust_id=75586127)。

## Shizuku 為何而生？

Shizuku 的誕生主要有兩大目的：

1. 提供一個方便地使用系統 API 的方式
2. 為部分只需要 adb 許可權的程式開發提供便利

## Shizuku 與「傳統」做法對比

### 「傳統」做法

以啟用/停用元件為例，一些需要 root 許可權的程式直接在 `su` 中執行 `pm disable`。

1. 執行 `su`
2. 執行 `pm disable`
3. (pre-Pie) 使用 app_process 啟動 Java 程序（[參見此處](https://android.googlesource.com/platform/frameworks/base/+/oreo-release/cmds/pm/pm)）
4. (Pie+) 執行原生程式 `cmd`（[參見此處](https://android.googlesource.com/platform/frameworks/native/+/pie-release/cmds/cmd/)）
5. 處理引數，通過 binder 與 system server 互動，處理結果輸出文字結果

其中每個「執行」都意味著新程序建立，su 內部使用 socket 與 su daemon 互動，這樣的過程中消耗大量的時間和效能。（部分設計不佳的程式甚至會每次執行指令都執行一次 `su`）

此類做法的缺點在於：

1. **極慢**
2. 需要處理文字來獲取結果
3. 功能受制於可用的指令
4. 即使 adb 有足夠許可權，程式也需要 root 許可權才可使用

### Shizuku 做法

Shizuku app 會引導使用者使用 root 或是 adb 方式執行一個程序（Shizuku 服務程序）。

1. 應用程序啟動時 Shizuku 服務程序傳送 binder 至應用程序
2. 應用通過該 binder 與 Shizuku 服務程序互動，Shizuku 服務程序通過 binder 與 system server 互動

Shizuku 的優點在於：

1. 極小額外時間及效能消耗
2. 與直接呼叫 API 體驗幾乎一致（程式開發者只許新增少量程式碼）
