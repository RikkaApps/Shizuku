package moe.shizuku.manager.ktx

import android.content.ComponentName
import android.content.pm.PackageManager

fun PackageManager.setComponentEnabled(componentName: ComponentName, enabled: Boolean) {
    val oldState = getComponentEnabledSetting(componentName)
    val newState = if (enabled) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED
    if (newState != oldState) {
        val flags = PackageManager.DONT_KILL_APP
        setComponentEnabledSetting(componentName, newState, flags)
    }
}

fun PackageManager.isComponentEnabled(componentName: ComponentName, defaultValue: Boolean = true): Boolean {
    return when (getComponentEnabledSetting(componentName)) {
        PackageManager.COMPONENT_ENABLED_STATE_DISABLED -> false
        PackageManager.COMPONENT_ENABLED_STATE_ENABLED -> true
        PackageManager.COMPONENT_ENABLED_STATE_DEFAULT -> defaultValue
        else -> false
    }
}