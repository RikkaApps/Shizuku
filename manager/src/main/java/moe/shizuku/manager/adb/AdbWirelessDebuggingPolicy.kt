package moe.shizuku.manager.adb

/** Pure decision logic for restoring Wireless debugging after a Shizuku start attempt. */
internal object AdbWirelessDebuggingPolicy {

    fun shouldDisableWirelessDebugging(
        wasEnabled: Boolean,
        isEnabled: Boolean,
        serverStarted: Boolean,
        disableAfterStart: Boolean,
        settingsChangedByManager: Boolean,
    ): Boolean {
        if (!isEnabled || wasEnabled) return false

        return if (serverStarted) {
            disableAfterStart
        } else {
            settingsChangedByManager
        }
    }
}
