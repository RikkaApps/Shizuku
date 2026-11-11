package moe.shizuku.manager.adb

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdbWirelessDebuggingPolicyTest {

    @Test
    fun disablesWirelessDebuggingAfterSuccessfulStartWhenItWasInitiallyOff() {
        assertTrue(
            AdbWirelessDebuggingPolicy.shouldDisableWirelessDebugging(
                wasEnabled = false,
                isEnabled = true,
                serverStarted = true,
                disableAfterStart = true,
                settingsChangedByManager = true,
            )
        )
    }

    @Test
    fun preservesWirelessDebuggingThatWasAlreadyEnabled() {
        assertFalse(
            AdbWirelessDebuggingPolicy.shouldDisableWirelessDebugging(
                wasEnabled = true,
                isEnabled = true,
                serverStarted = true,
                disableAfterStart = true,
                settingsChangedByManager = true,
            )
        )
    }

    @Test
    fun respectsKeepEnabledPreferenceAfterSuccessfulStart() {
        assertFalse(
            AdbWirelessDebuggingPolicy.shouldDisableWirelessDebugging(
                wasEnabled = false,
                isEnabled = true,
                serverStarted = true,
                disableAfterStart = false,
                settingsChangedByManager = true,
            )
        )
    }

    @Test
    fun rollsBackWirelessDebuggingAfterFailedManagerStartup() {
        assertTrue(
            AdbWirelessDebuggingPolicy.shouldDisableWirelessDebugging(
                wasEnabled = false,
                isEnabled = true,
                serverStarted = false,
                disableAfterStart = false,
                settingsChangedByManager = true,
            )
        )
    }

    @Test
    fun doesNotChangeWirelessDebuggingAfterFailureUnlessManagerEnabledIt() {
        assertFalse(
            AdbWirelessDebuggingPolicy.shouldDisableWirelessDebugging(
                wasEnabled = false,
                isEnabled = true,
                serverStarted = false,
                disableAfterStart = true,
                settingsChangedByManager = false,
            )
        )
    }

    @Test
    fun doesNotWriteWhenWirelessDebuggingIsAlreadyOff() {
        assertFalse(
            AdbWirelessDebuggingPolicy.shouldDisableWirelessDebugging(
                wasEnabled = false,
                isEnabled = false,
                serverStarted = true,
                disableAfterStart = true,
                settingsChangedByManager = true,
            )
        )
    }
}
