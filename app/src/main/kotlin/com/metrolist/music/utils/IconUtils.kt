package com.metrolist.music.utils

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager

object IconUtils {
    const val NAME_PRESET_COUNT = 8

    /**
     * Exactly one launcher alias is ever left enabled: the default icon
     * (dynamic or static, per [dynamicIconEnabled]) when [appNameIndex] is 0,
     * or one of the MainActivityName1..NAME_PRESET_COUNT aliases otherwise.
     * Preset names only ship the dynamic icon, so switching to one always
     * uses it regardless of [dynamicIconEnabled].
     */
    fun applyLauncherAlias(context: Context, dynamicIconEnabled: Boolean, appNameIndex: Int) {
        val pm = context.packageManager

        fun setEnabled(name: String, enabled: Boolean) {
            pm.setComponentEnabledSetting(
                ComponentName(context, "com.metrolist.music.$name"),
                if (enabled) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            )
        }

        setEnabled("MainActivityAlias", appNameIndex == 0 && dynamicIconEnabled)
        setEnabled("MainActivityStatic", appNameIndex == 0 && !dynamicIconEnabled)
        for (i in 1..NAME_PRESET_COUNT) {
            setEnabled("MainActivityName$i", appNameIndex == i)
        }
    }
}
